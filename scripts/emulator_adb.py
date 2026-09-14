"""ADB setup for the isolated CI emulator, with recorded, bounded recovery."""
import json
import re
import subprocess
import time


class EmulatorAdb:
    def __init__(self, transcript, serial='emulator-5554'):
        if not re.fullmatch(r'emulator-\d+', serial):
            raise ValueError('This smoke driver only targets an isolated emulator')
        self.serial = serial
        self.transcript = transcript

    def __call__(self, *args, check=True, timeout=60):
        command = ['adb', '-s', self.serial, *args]
        entry = {'command': command}
        try:
            result = subprocess.run(command, capture_output=True, text=True, timeout=timeout)
            entry.update(returncode=result.returncode, stdout=result.stdout, stderr=result.stderr)
        except subprocess.TimeoutExpired as exc:
            entry['timeoutSeconds'] = timeout
            # TimeoutExpired may carry bytes even when text=True.
            entry['stdout'] = exc.stdout.decode(errors='replace') if isinstance(exc.stdout, bytes) else exc.stdout
            entry['stderr'] = exc.stderr.decode(errors='replace') if isinstance(exc.stderr, bytes) else exc.stderr
            raise
        finally:
            with self.transcript.open('a') as stream:
                stream.write(json.dumps(entry) + '\n')
        if check:
            result.check_returncode()
        return result.stdout.strip()


def ensure_emulator_root(adb, evidence, timeout=90):
    """Verify UID 0 after adbd restarts; retry setup only, never the app checks.

    A root request can disconnect its own transport. Its exit status is not proof
    of the final state: reconnect, then require a successful `id -u` returning 0.
    Every command shares a deadline, and the transcript retains failed requests.
    """
    deadline = time.monotonic() + timeout
    evidence.update(serial=adb.serial, rootRequests=0, verifiedUid=None)

    def call(*args, **kwargs):
        remaining = deadline - time.monotonic()
        if remaining <= 0:
            raise TimeoutError('Emulator root setup exceeded its deadline')
        return adb(*args, timeout=min(10, remaining), **kwargs)

    # Never send a root request to a physical or non-debuggable Android build.
    call('wait-for-device')
    if call('shell', 'getprop', 'ro.kernel.qemu') != '1':
        raise RuntimeError('Expected the isolated Android emulator')
    if call('shell', 'getprop', 'ro.debuggable') != '1':
        raise RuntimeError('Expected a debuggable emulator image')

    last_error = 'Root UID not observed'
    for attempt in range(3):
        try:
            call('wait-for-device')
            if call('shell', 'id', '-u') == '0':
                evidence['verifiedUid'] = 0
                return
            evidence['rootRequests'] += 1
            call('root')
        except (subprocess.CalledProcessError, subprocess.TimeoutExpired) as exc:
            last_error = repr(exc)
        # Poll the restarted daemon before issuing another root request. A failed
        # request only recovers if an independent, successful shell proves UID 0.
        for _ in range(5):
            try:
                call('wait-for-device')
                if call('shell', 'id', '-u') == '0':
                    evidence['verifiedUid'] = 0
                    return
            except (subprocess.CalledProcessError, subprocess.TimeoutExpired) as exc:
                last_error = repr(exc)
            time.sleep(min(1, max(0, deadline - time.monotonic())))
    raise RuntimeError(f'Emulator did not provide a verified root shell: {last_error}')
