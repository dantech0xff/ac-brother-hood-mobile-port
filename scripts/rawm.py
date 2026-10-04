#!/usr/bin/env python3
"""Raw bytecode dump of ONE method from a committed javap text -- the authority
for every behaviour claim in this repository (a decompiler's view is only a hint).
Static analysis of text; the JAR is never loaded or run.

usage:
    rawm.py <javap.txt> <method-header-substring> [<from> <to>] [--desc DESCRIPTOR]

  <javap.txt>   reconstructed-project/bytecode/<class>.javap.txt
                (classes: g = player, i = entity / NPC FSMs, k = world / HUD,
                 j = canvas / math / UI plumbing, ...)
  <header>      substring of the method header line, e.g. 'void e()' or 'boolean a(int)'
  <from> <to>   inclusive byte offsets (default: the whole method)
  --desc        pick one overload by the `descriptor:` line under the header,
                e.g. --desc '(Li;)V'  (several `a(...)` / `b(...)` overloads exist)

Output, one instruction per line:   <offset> <opcode> <operand> // <constant-pool comment>
tableswitch / lookupswitch entries are printed as   <key> -> <target>.

Reading the constant-pool comments (checked over every field access and call site
that targets one of the 12 original classes -- no exception):
  * `Field i.ah:I`, `Method k.v:(I)Z`  name the class that DECLARES the member;
  * a bare `Field v:Z` / `Method d:(I)V` belongs to the file's OWN class
    (in g.javap.txt that is `g.v`, `g.d(I)V` -- not `i.v`).
A subclass can re-declare a name (`g.L` vs `i.L`, `g.n()` vs `g.e()` ...): they are
different members, and only the class in the comment tells them apart.
"""
import argparse
import re
import sys

HEADER = re.compile(r'^  \S.*\(.*\)(?: throws .*)?;$')
INSN = re.compile(r'^\s+(\d+): (\S.*)$')
SWITCH_ENTRY = re.compile(r'^\s+(-?\d+|default): (\d+)\s*$')


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('javap')
    ap.add_argument('header')
    ap.add_argument('lo', nargs='?', type=int)
    ap.add_argument('hi', nargs='?', type=int)
    ap.add_argument('--desc')
    a = ap.parse_intermixed_args()
    lo = a.lo if a.lo is not None else 0
    hi = a.hi if a.hi is not None else 10 ** 9

    matched = []
    selected = awaiting_desc = in_switch = False
    cur_header = ''
    switch_off = 0
    for line in open(a.javap, encoding='utf-8', errors='replace'):
        line = line.rstrip('\n')
        if line.startswith('  ') and not line.startswith('   ') and HEADER.match(line):
            cur_header, selected, awaiting_desc, in_switch = line.strip(), False, True, False
            continue
        if awaiting_desc and line.strip().startswith('descriptor:'):
            desc = line.split('descriptor:', 1)[1].strip()
            selected = a.header in cur_header and (a.desc is None or desc == a.desc)
            awaiting_desc = False
            if selected:
                matched.append((cur_header, desc))
                print('== %s    %s' % (cur_header, desc))
            continue
        if not selected:
            continue
        if in_switch:
            if line.strip().startswith('}'):
                in_switch = False
                continue
            m = SWITCH_ENTRY.match(line)
            if m and lo <= switch_off <= hi:
                print('      %s -> %s' % (m.group(1), m.group(2)))
            continue
        m = INSN.match(line)
        if not m:
            continue
        off = int(m.group(1))
        text = re.sub(r'\s+', ' ', m.group(2))
        if text.startswith(('tableswitch', 'lookupswitch')):
            in_switch, switch_off = True, off
        if lo <= off <= hi:
            print(str(off).rjust(5), text[:160])

    if not matched:
        sys.exit('method not found: %s%s' % (a.header, (' ' + a.desc) if a.desc else ''))
    if len(matched) > 1:
        sys.stderr.write('warning: %d methods match %r -- all are printed; pass --desc to pick one:\n'
                         % (len(matched), a.header))
        for h, d in matched:
            sys.stderr.write('    %s    %s\n' % (h, d))


def run():
    """main() with a quiet exit when the reader closes the pipe (`... | head`)."""
    import os
    try:
        main()
        sys.stdout.flush()
    except BrokenPipeError:
        os.dup2(os.open(os.devnull, os.O_WRONLY), sys.stdout.fileno())
        sys.exit(0)


if __name__ == '__main__':
    run()
