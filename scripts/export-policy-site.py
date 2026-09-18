#!/usr/bin/env python3
"""Render the shipped legal assets into static GitHub Pages files (no JS)."""
import argparse
from html import escape
from pathlib import Path
import re

parser = argparse.ArgumentParser()
parser.add_argument('destination', type=Path)
args = parser.parse_args()
assets = Path(__file__).resolve().parents[1] / 'app/src/main/assets/legal'
args.destination.mkdir(parents=True, exist_ok=True)
labels = {
 'en': ('Privacy policy', 'Terms of use', 'Skip to content', 'Document language', 'Keep a copy using your browser’s Print or Save options.'),
 'es': ('Política de privacidad', 'Condiciones de uso', 'Ir al contenido', 'Idioma del documento', 'Guarda una copia con las opciones Imprimir o Guardar del navegador.'),
 'fr': ('Politique de confidentialité', 'Conditions d’utilisation', 'Aller au contenu', 'Langue du document', 'Conservez une copie avec les options Imprimer ou Enregistrer de votre navigateur.'),
}
names = {'en': 'English', 'es': 'Español', 'fr': 'Français'}
def filename(page, language):
 return f'{page}{"" if language == "en" else "-"+language}.html'
def linked(text):
 text = escape(text)
 text = re.sub(r'https://[^\s<)]+', lambda m: '<a href="'+m[0].rstrip('.')+'">'+m[0].rstrip('.')+'</a>'+('.' if m[0].endswith('.') else ''), text)
 return text.replace('auralift.support@gmail.com', '<a href="mailto:auralift.support@gmail.com">auralift.support@gmail.com</a>')
for lang,(privacy,terms,skip,langlabel,footer) in labels.items():
 for page,title in [('privacy',privacy),('terms',terms)]:
  content = (assets / (lang if lang != 'en' else '') / f'{page}.txt').read_text()
  lines = content.strip().splitlines()
  body=[]
  for line in lines[1:]:
   if not line: continue
   tag='h2' if re.match(r'^\d+\. ',line) or line in ['Document languages','Idiomas de los documentos','Langues des documents'] else 'p'
   body.append(f'<{tag}>{linked(line)}</{tag}>')
  nav=' '.join(f'<a href="{filename(page,l)}" lang="{l}" hreflang="{l}"'+(' aria-current="page"' if l==lang else '')+f'>{n}</a>' for l,n in names.items())
  html=f'''<!doctype html>
<html lang="{lang}">
<head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>{escape(title)} | Auralift</title><link rel="stylesheet" href="styles.css"></head>
<body>
<a class="skip" href="#main">{skip}</a>
<header><a class="brand" href="index.html">Auralift</a><nav aria-label="Auralift"><a href="{filename('privacy',lang)}">{privacy}</a><a href="{filename('terms',lang)}">{terms}</a></nav></header>
<main id="main">
<nav aria-label="{langlabel}">{nav}</nav>
<h1>{escape(title)}</h1>
{chr(10).join(body)}
</main>
<footer><a href="mailto:auralift.support@gmail.com">auralift.support@gmail.com</a><p>{footer}</p></footer>
</body></html>
'''
  (args.destination/filename(page,lang)).write_text(html)
print('Rendered 6 policy pages from shipped offline documents')
