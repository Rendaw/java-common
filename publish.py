#!/usr/bin/env python
import subprocess
import logging
import argparse
import os

logging.basicConfig(level=logging.DEBUG)

parser = argparse.ArgumentParser()
parser.add_argument('stack', choices=('production', 'development'))
parser.add_argument('-f', '--force', action='store_true')
args = parser.parse_args()

if not args.force and subprocess.call(['git', 'diff-index', '--quiet', 'HEAD']) != 0:  # noqa
    raise RuntimeError('Working directory must be clean.')

subprocess.check_call(['git', 'tag', args.version])
subprocess.check_call(['git', 'push', '--tags'])

env = os.environ.copy()
env['JAVA_HOME'] = '/usr/lib/jvm/java-8-jdk'
subprocess.check_call(['mvn', 'clean', 'publish', '-P', 'release'], env=env)