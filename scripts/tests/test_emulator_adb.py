"""Exercise the driver against daemon states, including the failed CI handshake."""
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from emulator_adb import EmulatorAdb, ensure_emulator_root


class FakeDaemon:
    def __init__(self, root_outcomes=('success',), qemu='1', debuggable='1', id_exit=0):
        self.root_outcomes = iter(root_outcomes)
        self.qemu = qemu
        self.debuggable = debuggable
        self.id_exit = id_exit
        self.uid = '2000'
        self.root_requests = 0
        self.commands = []

    def run(self, command, **kwargs):
        self.commands.append(command)
        if command[:3] != ['adb', '-s', 'emulator-5554']:
            raise AssertionError(f'Unscoped emulator command: {command}')
        args = tuple(command[3:])
        stdout, stderr, code = '', '', 0
        if args == ('shell', 'getprop', 'ro.kernel.qemu'):
            stdout = self.qemu
        elif args == ('shell', 'getprop', 'ro.debuggable'):
            stdout = self.debuggable
        elif args == ('shell', 'id', '-u'):
            stdout, code = self.uid, self.id_exit
        elif args == ('root',):
            self.root_requests += 1
            outcome = next(self.root_outcomes, 'no_change')
            if outcome in ('success', 'lost_reply', 'timeout_after_restart'):
                self.uid = '0'
            if outcome in ('lost_reply', 'not_sent'):
                code, stderr = 1, "adb: protocol fault (couldn't read status length)"
            elif outcome == 'timeout_after_restart':
                raise subprocess.TimeoutExpired(command, kwargs['timeout'], output=b'restarting adbd')
            else:
                stdout = 'restarting adbd as root'
        elif args != ('wait-for-device',):
            raise AssertionError(f'Unexpected command: {command}')
        return subprocess.CompletedProcess(command, code, stdout, stderr)


class EmulatorRootTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.log = Path(self.temp.name) / 'adb.jsonl'
        self.adb = EmulatorAdb(self.log)
        self.evidence = {}
        self.sleep = patch('emulator_adb.time.sleep').start()
        self.addCleanup(patch.stopall)

    def prepare(self, daemon, **kwargs):
        with patch('emulator_adb.subprocess.run', side_effect=daemon.run):
            ensure_emulator_root(self.adb, self.evidence, **kwargs)

    def test_healthy_restart_requires_verified_uid(self):
        daemon = FakeDaemon()
        self.prepare(daemon)
        self.assertEqual(self.evidence['verifiedUid'], 0)
        self.assertEqual(daemon.root_requests, 1)
        self.assertEqual(daemon.commands[-1][3:], ['shell', 'id', '-u'])

    def test_protocol_failure_after_restart_recovers_and_retains_stderr(self):
        daemon = FakeDaemon(root_outcomes=('lost_reply',))
        self.prepare(daemon)
        self.assertEqual(self.evidence['verifiedUid'], 0)
        self.assertEqual(daemon.root_requests, 1)
        entries = [json.loads(line) for line in self.log.read_text().splitlines()]
        failed = [entry for entry in entries if entry['returncode'] != 0]
        self.assertEqual(len(failed), 1)
        self.assertIn('protocol fault', failed[0]['stderr'])
        self.assertEqual(entries[-1]['stdout'], '0')
        self.assertEqual(entries[-1]['returncode'], 0)

    def test_lost_request_is_reissued_only_when_shell_remains_unprivileged(self):
        daemon = FakeDaemon(root_outcomes=('not_sent', 'success'))
        self.prepare(daemon)
        self.assertEqual(self.evidence['verifiedUid'], 0)
        self.assertEqual(daemon.root_requests, 2)

    def test_success_message_without_root_is_a_failure(self):
        daemon = FakeDaemon(root_outcomes=('no_change',))
        with self.assertRaisesRegex(RuntimeError, 'verified root shell'):
            self.prepare(daemon)
        self.assertIsNone(self.evidence['verifiedUid'])
        self.assertEqual(daemon.root_requests, 3)

    def test_a_failed_uid_command_cannot_prove_root(self):
        daemon = FakeDaemon(id_exit=1)
        daemon.uid = '0'
        with self.assertRaisesRegex(RuntimeError, 'verified root shell'):
            self.prepare(daemon)
        self.assertIsNone(self.evidence['verifiedUid'])

    def test_timeout_after_restart_still_requires_successful_root_shell(self):
        daemon = FakeDaemon(root_outcomes=('timeout_after_restart',))
        self.prepare(daemon)
        self.assertEqual(self.evidence['verifiedUid'], 0)
        entries = [json.loads(line) for line in self.log.read_text().splitlines()]
        self.assertTrue(any('timeoutSeconds' in entry for entry in entries))
        self.assertEqual(entries[-1]['returncode'], 0)

    def test_physical_and_non_debuggable_images_are_rejected_before_root(self):
        for config in ({'qemu': '0'}, {'debuggable': '0'}):
            with self.subTest(config=config):
                daemon = FakeDaemon(**config)
                with self.assertRaises(RuntimeError):
                    self.prepare(daemon)
                self.assertEqual(daemon.root_requests, 0)

    def test_persistent_disconnect_exhausts_one_shared_deadline(self):
        daemon = FakeDaemon(root_outcomes=('not_sent',))
        now = [0.0]

        def disconnect_after_root(command, **kwargs):
            if daemon.root_requests and command[3:] == ['wait-for-device']:
                now[0] += kwargs['timeout']
                raise subprocess.TimeoutExpired(command, kwargs['timeout'])
            return daemon.run(command, **kwargs)

        with patch('emulator_adb.subprocess.run', side_effect=disconnect_after_root), \
             patch('emulator_adb.time.monotonic', side_effect=lambda: now[0]):
            with self.assertRaisesRegex(TimeoutError, 'deadline'):
                ensure_emulator_root(self.adb, self.evidence, timeout=25)
        self.assertEqual(now[0], 25)
        self.assertIsNone(self.evidence['verifiedUid'])


if __name__ == '__main__':
    unittest.main()
