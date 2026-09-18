#!/usr/bin/env python3
"""Check shipped language coverage, format arguments and legal document archives."""
from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
resources = root / 'app/src/main/res'
base = None
for folder in ('values', 'values-es', 'values-fr'):
    nodes = list(ET.parse(resources / folder / 'strings.xml').getroot())
    strings = {node.attrib['name']: ''.join(node.itertext()) for node in nodes}
    assert len(strings) == len(nodes), f'{folder}: duplicate resource'
    base = base or strings
    assert strings.keys() == base.keys(), f'{folder}: different resource keys'
    for key, text in strings.items():
        assert text.strip(), f'{folder}/{key}: empty translation'
        assert sorted(re.findall(r'%\d+\$[sdf]', text)) == sorted(re.findall(r'%\d+\$[sdf]', base[key])), f'{folder}/{key}: format arguments'
    print(f'{folder}: {len(strings)} strings, all format arguments matched')

store = (root / 'app/src/main/java/com/jamiewardle/auralift/legal/TermsStore.kt').read_text()
version = re.search(r'CURRENT_VERSION = "([^"]+)"', store)[1]
for language in ('en', 'es', 'fr'):
    folder = root / 'app/src/main/assets/legal' / (language if language != 'en' else '')
    for page, count in [('terms', 10), ('privacy', 13)]:
        data = (folder / f'{page}.txt').read_text()
        assert len(re.findall(r'^\d+\. ', data, re.M)) == count, f'{language}/{page}: sections missing'
        assert 'auraforgelabssupport+auralift@gmail.com' in data and 'AuraForge Labs' in data
        assert not re.search(r'\[TODO|finalized before|preview terms|final public publisher', data, re.I)
        if page == 'terms':
            assert version in data
            archive = root / f'docs/legal/terms-{version}-{language}.txt'
            assert archive.read_bytes() == (folder / f'{page}.txt').read_bytes()
    print(f'{language}: complete terms/privacy, exact versioned terms archive')
