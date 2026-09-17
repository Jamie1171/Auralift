"""Package APKs built together. Signing keys remain on the ephemeral CI runner."""
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import zipfile

root = Path(__file__).resolve().parents[1]
revision = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=root, text=True).strip()
version = re.search(r'versionName = "([^"]+)"', (root / 'app/build.gradle.kts').read_text()).group(1)
dest = root / 'build/firebase-kit'
dest.mkdir(parents=True, exist_ok=True)
sdk = Path(os.environ['ANDROID_HOME']) / 'build-tools/35.0.0'
inputs = {
    'app': root / 'app/build/outputs/apk/play/debug/app-play-debug.apk',
    'tests': root / 'app/build/outputs/apk/androidTest/play/debug/app-play-debug-androidTest.apk',
}
manifest = {'sourceCommit': revision, 'version': version, 'files': [], 'testResults': 'See the matching GitHub Actions run; packaging is not a test pass.'}
if os.environ.get('GITHUB_RUN_ID'):
    manifest['workflowRunUrl'] = f"https://github.com/{os.environ['GITHUB_REPOSITORY']}/actions/runs/{os.environ['GITHUB_RUN_ID']}"
certificates = []
for role, source in inputs.items():
    with zipfile.ZipFile(source) as archive:
        assert archive.testzip() is None, source
        assert len(archive.namelist()) == len(set(archive.namelist())), source
    verified = subprocess.check_output([str(sdk / 'apksigner'), 'verify', '--verbose', '--print-certs', str(source)], text=True)
    cert = re.search(r'Signer #1 certificate SHA-256 digest: ([a-f0-9]+)', verified).group(1)
    certificates.append(cert)
    name = f'Auralift-Firebase-{version}-{revision[:8]}-{role}.apk'
    shutil.copy2(source, dest / name)
    manifest['files'].append({'role': role, 'file': name, 'bytes': source.stat().st_size,
        'sha256': hashlib.sha256(source.read_bytes()).hexdigest(), 'certificateSha256': cert})
assert len(set(certificates)) == 1, 'App and instrumentation APKs must have matching signatures'
(dest / 'build-identity.json').write_text(json.dumps(manifest, indent=2) + '\n')
(dest / 'SHA256SUMS.txt').write_text(''.join(f"{f['sha256']}  {f['file']}\n" for f in manifest['files']))
for doc in ['FIREBASE-AUTOMATION.md', 'HUMAN-TEST-GOALS.md']:
    shutil.copy2(root / 'docs' / doc, dest / doc)
print(json.dumps(manifest, indent=2))
