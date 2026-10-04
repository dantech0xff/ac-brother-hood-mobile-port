#!/usr/bin/env python3
"""Who reads / writes / calls this member?  Every use, with the enclosing method and byte offset.

Queries the machine-readable inventory (reconstructed-project/inventory/field-accesses.json and
calls.json) that scripts/verify-static-reconstruction.py regenerates from the JAR -- static data,
nothing is loaded or run.

usage:
    xref.py <Class.member> [--desc DESCRIPTOR] [--reads | --writes] [--kind field|call] [--inventory DIR]

  <Class.member>  ORIGINAL (obfuscated) names: g = player, i = entity / NPC FSMs, k = world / HUD,
                  j = canvas / math / UI plumbing.  The class is the one that DECLARES the member
                  (the javap constant-pool comments always name the declaring class; see rawm.py).
  --desc          a field's type ('Li;', 'I') or a method's descriptor ('(Li;)V'); needed to tell
                  overloads apart -- `i.a` alone lists the call sites of EVERY `a(...)` in i
  --reads/--writes  fields only: getfield+getstatic / putfield+putstatic
  --kind          restrict to fields or calls (default: both, whichever exist)

Examples (each reproduces a fact proven in slice 416):
    xref.py i.ac --writes             6 stores: g.a(I)V @37, g.e()V @12688, g.as()V @248, g.au()V @1431
                                      are RAW (no `P & 256` bookkeeping); i.a(Li;)V @24/@33 is the setter
    xref.py i.a --desc '(Li;)V'       the 14 callers of that setter
    xref.py k.u --writes              the dialog kind: only k.b(IIII)Z @8 and k.ag()V @87

Why this exists: the commonest port bug is a name that means two things -- `g.L/M` (the player's
gauge point, instance) vs `i.L/M` (the touch anchor, static); `i.bn` (static) vs a Kotlin copy nobody
writes.  Before you port or "fix" a field, run this on it and make sure the Kotlin has a writer AND a
reader at every site listed here.
"""
import argparse
import json
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
DEFAULT_INV = os.path.join(HERE, '..', 'reconstructed-project', 'inventory')
WRITES = ('putfield', 'putstatic')
READS = ('getfield', 'getstatic')


def load(inv, name):
    with open(os.path.join(inv, name), encoding='utf-8') as f:
        return json.load(f)


def show(title, rows, with_desc):
    rows = sorted(rows, key=lambda r: (r['caller'], r['bytecode_offset']))
    print('%s: %d' % (title, len(rows)))
    for r in rows:
        extra = ('  %s' % r['target_descriptor']) if with_desc else ''
        print('  %-40s @%-6d %-13s %s.%s%s' % (r['caller'], r['bytecode_offset'], r['opcode'],
                                              r['target_class'], r['target_name'], extra))
    return len(rows)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('member', help='Class.member, e.g. i.ac')
    ap.add_argument('--desc')
    g = ap.add_mutually_exclusive_group()
    g.add_argument('--reads', action='store_true')
    g.add_argument('--writes', action='store_true')
    ap.add_argument('--kind', choices=('field', 'call'))
    ap.add_argument('--inventory', default=DEFAULT_INV)
    a = ap.parse_args()
    if '.' not in a.member:
        sys.exit('expected Class.member (e.g. i.ac), got %r' % a.member)
    cls, name = a.member.rsplit('.', 1)

    shown = 0
    if a.kind in (None, 'field'):
        rows = [r for r in load(a.inventory, 'field-accesses.json')
                if r['target_class'] == cls and r['target_name'] == name
                and (a.desc is None or r['target_descriptor'] == a.desc)
                and (not a.reads or r['opcode'] in READS) and (not a.writes or r['opcode'] in WRITES)]
        if rows:
            shown += show('field %s.%s' % (cls, name), rows, a.desc is None)
    if a.kind in (None, 'call') and not (a.reads or a.writes):
        rows = [r for r in load(a.inventory, 'calls.json')
                if r['target_class'] == cls and r['target_name'] == name
                and (a.desc is None or r['target_descriptor'] == a.desc)]
        if rows:
            shown += show('call  %s.%s%s' % (cls, name, (':' + a.desc) if a.desc else ''), rows, a.desc is None)
    if not shown:
        sys.exit('no use of %s found (check the class: the declaring class, not the receiver type; '
                 'and --desc if given)' % a.member)


def run():
    try:
        main()
        sys.stdout.flush()
    except BrokenPipeError:
        os.dup2(os.open(os.devnull, os.O_WRONLY), sys.stdout.fileno())
        sys.exit(0)


if __name__ == '__main__':
    run()
