"""Black-box smoke check of an optimized APK; no production test hooks or credentials."""
import hashlib
import json
from pathlib import Path
import re
import struct
import subprocess
import time
import xml.etree.ElementTree as ET
import zipfile

out = Path('build/page-size-results')
out.mkdir(parents=True, exist_ok=True)
apk = out / 'Auralift-optimized-page-test.apk'
package = 'com.jamiewardle.auralift'
report = {'result': 'not_run', 'checks': [], 'nativeLibraries': [],
          'scope': 'Optimized APK on x86-64 16 KB emulator. No acoustic or ARM certification.'}


def adb(*args, check=True):
    return subprocess.run(['adb', *args], check=check, capture_output=True, text=True).stdout.strip()


def capture(label):
    adb('shell', 'uiautomator', 'dump', '/sdcard/auralift-window.xml')
    raw = adb('shell', 'cat', '/sdcard/auralift-window.xml')
    (out / f'{label}.xml').write_text(raw)
    png = subprocess.run(['adb', 'exec-out', 'screencap', '-p'], check=True, capture_output=True).stdout
    (out / f'{label}.png').write_bytes(png)
    return ET.fromstring(raw)


def find(text, label, timeout=25):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        root = capture(label)
        for node in root.iter('node'):
            if node.get('package') == package and text in (node.get('text'), node.get('content-desc')):
                return node
        time.sleep(0.5)
    raise AssertionError(f'UI never showed {text!r}; inspect {label}.png and logcat')


def tap(text, label):
    node = find(text, label)
    bounds = list(map(int, re.findall(r'\d+', node.attrib['bounds'])))
    x1, y1, x2, y2 = bounds
    assert x2 > x1 and y2 > y1, node.attrib
    adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))


def native_layout():
    # Record both LOAD and RELRO, rather than treating ZIP alignment as sufficient.
    with zipfile.ZipFile(apk) as z:
        for name in z.namelist():
            if not name.endswith('.so'):
                continue
            data = z.read(name)
            assert data[:4] == b'\x7fELF'
            endian = '<' if data[5] == 1 else '>'
            is64 = data[4] == 2
            offset = struct.unpack_from(endian + ('Q' if is64 else 'I'), data, 32 if is64 else 28)[0]
            stride, count = struct.unpack_from(endian + 'HH', data, 54 if is64 else 42)
            entry = {'path': name, 'loadAligned16k': True, 'relroEndAligned16k': None}
            for i in range(count):
                p = struct.unpack_from(endian + ('IIQQQQQQ' if is64 else 'IIIIIIII'), data, offset+i*stride)
                kind, vaddr, memsz, align = p[0], p[3] if is64 else p[2], p[6] if is64 else p[5], p[7]
                if kind == 1:
                    entry['loadAligned16k'] &= align >= 16384
                if kind == 0x6474e552:
                    entry['relroEndAligned16k'] = (vaddr + memsz) % 16384 == 0
            report['nativeLibraries'].append(entry)
    report['staticFlags'] = [e['path'] for e in report['nativeLibraries']
                             if not e['loadAligned16k'] or e['relroEndAligned16k'] is not True]


try:
    report['apkSha256'] = hashlib.sha256(apk.read_bytes()).hexdigest()
    native_layout()
    report['pageSize'] = int(adb('shell', 'getconf', 'PAGE_SIZE'))
    report['api'] = int(adb('shell', 'getprop', 'ro.build.version.sdk'))
    report['abi'] = adb('shell', 'getprop', 'ro.product.cpu.abi')
    (out / 'memory-before.txt').write_text(adb('shell', 'cat', '/proc/meminfo'))
    assert report['pageSize'] == 16384, 'Wrong emulator: 16 KB was not active'
    # An isolated rootable emulator; disable compatibility workarounds explicitly.
    adb('root')
    adb('wait-for-device')
    for key, value in [('bionic.linker.16kb.app_compat.enabled', 'false'), ('pm.16kb.app_compat.disabled', 'true')]:
        adb('shell', 'setprop', key, value)
        assert adb('shell', 'getprop', key) == value
    report['compatibilityWorkarounds'] = 'disabled'
    # Give the black-box controls enough viewport space; this is a native-loader
    # check, not evidence of small-screen layout coverage.
    adb('shell', 'wm', 'density', '320')
    report['displayDensity'] = 320
    adb('logcat', '-c')
    assert 'Success' in adb('install', '-g', str(apk))
    adb('shell', 'am', 'start', '-W', '-n', package + '/.MainActivity')
    find('Enable boost', 'initial')
    report['checks'].append('optimized first launch')
    tap('+5 dB', 'select-gain')
    tap('Enable boost', 'enable')
    tap('Enable Auralift', 'acknowledge')
    find('Turn boost off', 'running')
    assert adb('shell', 'pidof', package, check=False), 'App process disappeared; inspect logcat for crash or memory pressure'
    (out / 'app-memory-running.txt').write_text(adb('shell', 'dumpsys', 'meminfo', package))
    report['checks'].append('gain selection and service start through public UI')
    adb('shell', 'input', 'keyevent', 'KEYCODE_HOME')
    time.sleep(10)
    assert adb('shell', 'pidof', package, check=False), 'App process died in background'
    adb('shell', 'am', 'start', '-W', '-n', package + '/.MainActivity')
    tap('Turn boost off', 'stop')
    find('Enable boost', 'stopped')
    report['checks'].append('background return and Stop through public UI')
    report['result'] = 'passed'
except BaseException as exc:
    report['result'] = 'failed'
    report['failure'] = repr(exc)
    raise
finally:
    (out / 'page-size-report.json').write_text(json.dumps(report, indent=2) + '\n')
    (out / 'logcat.txt').write_text(adb('logcat', '-d', check=False))
    (out / 'memory-after.txt').write_text(adb('shell', 'cat', '/proc/meminfo', check=False))
    adb('shell', 'am', 'force-stop', package, check=False)
    print(json.dumps(report, indent=2))
