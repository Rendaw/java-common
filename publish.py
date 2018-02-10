#!/usr/bin/env python
import subprocess
import logging
import argparse
import os
import re
import xml.etree.ElementTree

logging.basicConfig(level=logging.DEBUG)

parser = argparse.ArgumentParser()
parser.add_argument('-f', '--force', action='store_true')
args = parser.parse_args()

if not args.force and subprocess.call(['git', 'diff-index', '--quiet', 'HEAD']) != 0:  # noqa
    raise RuntimeError('Working directory must be clean.')

with open('pom.xml', 'r') as pom_source:
    pom_text = pom_source.read()
pom_text = re.sub('xmlns="[^"]+"', '', pom_text)
pom = xml.etree.ElementTree.fromstring(pom_text)
version = pom.find('./version').text

subprocess.check_call(['git', 'tag', 'v' + version])
subprocess.check_call(['git', 'push', '--tags'])

env = os.environ.copy()
env['JAVA_HOME'] = '/usr/lib/jvm/java-8-jdk'
subprocess.check_call(['mvn', 'clean', 'deploy', '-P', 'release'], env=env)