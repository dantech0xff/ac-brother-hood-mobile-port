#!/usr/bin/env python3
"""Mutation check for a port slice: put each OLD (wrong) behaviour back and make sure the tests FAIL.

A mutant that survives means the slice has a fix without a test that would have caught the bug.
Run it AFTER the fix and its tests are written (the working tree holds the fix), before committing.

usage:
    python3 scripts/mutation-check.py MUTANTS.py [--check] [--tests FILTER ...] [--gradle-args "..."]
                                         [--timeout SECONDS] [ID ...]
    python3 scripts/mutation-check.py --restore        # put files back after a killed run

MUTANTS.py defines
    BASE = "rewrite/core/src/main/kotlin/com/acrebuild/core/"       # optional prefix, repo-relative
    MUTANTS = [
        # (id, file under BASE, [(old, new), ...])  -- every `old` must occur EXACTLY once
        ("G1 S184 end gate", "PlayerFsm.kt",
         [("if (p.animFinished() || aN == null) {", "if (p.animFinished()) {")]),
    ]
`new` is the old behaviour restored (or a deletion); keep each mutant to the one behaviour a test pins.

  --check        only verify that every anchor exists once (no build, no file written)
  --tests F      run `:core:test --tests F` instead of the whole suite (repeatable; iterate fast with
                 this -- the final check that goes in the plan must be the FULL suite: a mutant is
                 killed if ANY test fails, and a narrow filter reports false survivors)
  --gradle-args  replaces the default "--offline --no-build-cache -q" (e.g. "-q" with network)
  ID             run only the mutants with these ids -- the whole id, or just its first word
                 (`G2` selects "G2 S184 drag sign" but not "G20 ...")

Results: KILLED (a test failed -- the failing test names come from the JUnit XML, never from the build
banner), SURVIVED (write the missing test), ERROR (the mutant did not compile, or the build died with
no failing test: NOT a kill -- fix the replacement).  Exit code 0 = all killed, 1 = a survivor,
2 = an error.

Safety (the source files are edited IN PLACE): the original bytes are kept in a journal under the
system temp dir and restored in `finally` (also on Ctrl-C / SIGTERM); the run refuses to start if a
previous run left a journal (--restore puts everything back); after every restore the bytes are
compared with the originals.  After a run `git diff` must be exactly what it was before it.
"""
import hashlib
import importlib.util
import json
import os
import re
import shlex
import shutil
import signal
import subprocess
import sys
import tempfile
import time
import xml.etree.ElementTree as ET

ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'))
GRADLE_DIR = os.path.join(ROOT, 'rewrite')
JOURNAL_DIR = os.path.join(tempfile.gettempdir(), 'acport-mutation-check')
JOURNAL = os.path.join(JOURNAL_DIR, 'journal.json')


def read_bytes(path):
    with open(path, 'rb') as f:
        return f.read()


def sha(b):
    return hashlib.sha256(b).hexdigest()


def restore_from_journal():
    if not os.path.exists(JOURNAL):
        return 0
    with open(JOURNAL, encoding='utf-8') as f:
        entries = json.load(f)
    n = 0
    for path, backup in entries.items():
        shutil.copyfile(backup, path)
        n += 1
    shutil.rmtree(JOURNAL_DIR, ignore_errors=True)
    return n


def load_mutants(path):
    spec = importlib.util.spec_from_file_location('mutants', path)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    base = getattr(mod, 'BASE', '')
    muts = getattr(mod, 'MUTANTS')
    return [(mid, os.path.join(ROOT, base, fn), edits) for (mid, fn, edits) in muts]


def mutate(text, edits, mid):
    for old, new in edits:
        n = text.count(old)
        if n != 1:
            raise SystemExit('%s: anchor must occur exactly once, found %d: %r' % (mid, n, old[:100]))
        text = text.replace(old, new, 1)
    return text


def run_gradle(args, tests, timeout):
    cmd = ['./gradlew', ':core:test'] + shlex.split(args)
    for t in tests:
        cmd += ['--tests', t]
    env = dict(os.environ, LANG='C.UTF-8', LC_ALL='C.UTF-8')
    started = time.time()
    try:
        r = subprocess.run(cmd, cwd=GRADLE_DIR, env=env, capture_output=True, text=True, timeout=timeout)
    except subprocess.TimeoutExpired:
        return None, 'timeout after %ds' % timeout, started
    return r.returncode, r.stdout + r.stderr, started


def failing_tests(started):
    """`Class > test` of every failed testcase in the JUnit XML written by THIS run."""
    out = []
    results = os.path.join(GRADLE_DIR, 'core', 'build', 'test-results', 'test')
    if not os.path.isdir(results):
        return out
    for name in sorted(os.listdir(results)):
        path = os.path.join(results, name)
        if not name.endswith('.xml') or os.path.getmtime(path) < started - 1:
            continue
        try:
            root = ET.parse(path).getroot()
        except ET.ParseError:
            continue
        for tc in root.iter('testcase'):
            if tc.find('failure') is not None or tc.find('error') is not None:
                out.append('%s > %s' % (tc.get('classname', '?').rsplit('.', 1)[-1], tc.get('name')))
    return out


def classify(rc, out, started):
    """-> (verdict, detail).  A red build is a KILL only when a testcase failed."""
    if rc is None:
        return 'ERROR', out
    if rc == 0:
        return 'SURVIVED', ''
    if re.search(r"Execution failed for task ':[\w:-]*compile", out) or re.search(r'^e: ', out, re.M):
        first = next((l for l in out.split('\n') if l.startswith('e: ')), 'compile error')
        return 'ERROR', 'does not compile: ' + first[:200]
    failing = failing_tests(started)
    if failing:
        return 'KILLED', '; '.join(failing[:3]) + (' (+%d)' % (len(failing) - 3) if len(failing) > 3 else '')
    tail = '\n'.join((out or '').strip().split('\n')[-12:])
    return 'ERROR', 'build failed but no testcase failed -- not a kill\n' + tail


def main():
    argv = sys.argv[1:]
    if not argv or argv[0] in ('-h', '--help'):
        sys.exit(__doc__)
    if '--restore' in argv:
        print('restored %d file(s)' % restore_from_journal())
        return 0
    check = '--check' in argv
    tests, gradle_args = [], None
    timeout = 3600
    rest = []
    it = iter([a for a in argv if a != '--check'])
    for a in it:
        if a == '--tests':
            tests.append(next(it))
        elif a == '--gradle-args':
            gradle_args = next(it)
        elif a == '--timeout':
            timeout = int(next(it))
        else:
            rest.append(a)
    if not rest:
        sys.exit('missing MUTANTS.py')
    mutants_file, selected = rest[0], rest[1:]
    if gradle_args is None:
        gradle_args = '--offline --no-build-cache -q'
    mutants = [m for m in load_mutants(mutants_file)
               if not selected or any(m[0] == s or m[0].startswith(s + ' ') for s in selected)]
    if not mutants:
        sys.exit('no mutant selected')

    if check:
        for mid, path, edits in mutants:
            mutate(open(path, encoding='utf-8', newline='').read(), edits, mid)
            print('anchor ok  ', mid)
        return 0

    if os.path.exists(JOURNAL):
        sys.exit('a previous run left %s -- run `python3 scripts/mutation-check.py --restore` first' % JOURNAL)
    os.makedirs(JOURNAL_DIR)
    originals = {}
    for _, path, _ in mutants:
        if path not in originals:
            originals[path] = read_bytes(path)
    entries = {}
    for i, (path, data) in enumerate(originals.items()):
        backup = os.path.join(JOURNAL_DIR, 'orig%d' % i)
        with open(backup, 'wb') as f:
            f.write(data)
        entries[path] = backup
    with open(JOURNAL, 'w', encoding='utf-8') as f:
        json.dump(entries, f)

    def on_term(signum, frame):
        raise SystemExit(130)
    signal.signal(signal.SIGTERM, on_term)

    survivors, errors, killed = [], [], 0
    try:
        for mid, path, edits in mutants:
            text = originals[path].decode('utf-8')
            mutated = mutate(text, edits, mid)
            with open(path, 'w', encoding='utf-8', newline='') as f:
                f.write(mutated)
            try:
                rc, out, started = run_gradle(gradle_args, tests, timeout)
            finally:
                with open(path, 'wb') as f:
                    f.write(originals[path])
                if sha(read_bytes(path)) != sha(originals[path]):
                    sys.exit('FATAL: %s was not restored byte-for-byte -- run --restore' % path)
            verdict, detail = classify(rc, out, started)
            if verdict == 'KILLED':
                killed += 1
            elif verdict == 'SURVIVED':
                survivors.append(mid)
            else:
                errors.append(mid)
            print('%-9s %s%s' % (verdict, mid, ('  ' + detail) if detail else ''), flush=True)
    finally:
        restore_from_journal()
        for path, data in originals.items():
            if sha(read_bytes(path)) != sha(data):
                print('FATAL: %s differs from its original after the run' % path, file=sys.stderr)
    print('killed %d / %d   survivors %s   errors %s' % (killed, len(mutants), survivors, errors))
    return 2 if errors else (1 if survivors else 0)


if __name__ == '__main__':
    sys.exit(main())
