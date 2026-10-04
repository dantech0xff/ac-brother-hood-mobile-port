#!/usr/bin/env python3
"""Tiny javap -c pseudo-decompiler (stack simulation) for the AC Brotherhood
bytecode audit.  Static analysis of the committed javap text only -- the JAR is
never loaded or run.  Usage:
    bcdec.py <javap.txt> <method-header-substring> [--desc DESCRIPTOR]
             [--from OFF --to OFF]
Prints one statement per line, labels at jump targets.  Conditions are kept
as the bytecode states them (no inversion fixes): `if (A op B) goto Lxxx`
means the JUMP is taken when the condition holds, i.e. the statements that
FOLLOW it run when the condition is false.

  <method-header-substring>  matches the header line, e.g. 'void e()'.  Several
      overloads can match (`a(i)`, `a(int)` ...): pass --desc '(Li;)V' (the
      `descriptor:` line under the header) to pick exactly one.
  --from / --to  byte offsets, `from <= off < to`.  Start at a statement boundary:
      operands pushed before the range print as `<?>`.

This is a reading aid, NOT an oracle: it flattens nothing and names nothing, but
a stack-merge at a join label can print a ternary hint (`(a | b)`).  When a line
looks odd, read the raw instructions with scripts/rawm.py.  A bare `x` in an
expression is a field of the file's own class; `i.x` is declared in class i.
"""
import re, sys

INSN = re.compile(r'^\s+(\d+): (\w+)\s*(.*)$')
SWITCH_ENTRY = re.compile(r'^\s+(-?\d+|default): (\d+)\s*$')

def load_method(path, header, desc=None):
    lines = open(path, errors='replace').read().split('\n')
    def is_header(l):
        return l.startswith('  ') and not l.startswith('   ') and l.rstrip().endswith(';') and '(' in l
    hits = []
    for i, l in enumerate(lines):
        if is_header(l) and header in l:
            d = lines[i + 1].split('descriptor:', 1)[1].strip() if i + 1 < len(lines) and 'descriptor:' in lines[i + 1] else ''
            if desc is None or d == desc:
                hits.append((i, l.strip(), d))
    if not hits:
        raise SystemExit('method not found: %s%s' % (header, (' ' + desc) if desc else ''))
    if len(hits) > 1:
        sys.stderr.write('warning: %d methods match %r -- using the first; pass --desc to pick one:\n' % (len(hits), header))
        for _, h, d in hits:
            sys.stderr.write('    %s    %s\n' % (h, d))
    start = hits[0][0]
    end = len(lines)
    for j in range(start + 1, len(lines)):
        if is_header(lines[j]):
            end = j
            break
    return lines[start:end]

def parse(lines):
    insns = []          # (off, op, arg, comment)
    switches = {}       # off -> list[(key, target)]
    i = 0
    while i < len(lines):
        m = INSN.match(lines[i])
        if m:
            off, op, rest = int(m.group(1)), m.group(2), m.group(3)
            comment = ''
            if '//' in rest:
                rest, comment = rest.split('//', 1)
                comment = comment.strip()
            rest = rest.strip()
            if op in ('tableswitch', 'lookupswitch'):
                ent = []
                i += 1
                while i < len(lines) and not lines[i].strip().startswith('}'):
                    sm = SWITCH_ENTRY.match(lines[i])
                    if sm:
                        ent.append((sm.group(1), int(sm.group(2))))
                    i += 1
                switches[off] = ent
                insns.append((off, op, '', comment))
            else:
                insns.append((off, op, rest, comment))
        i += 1
    return insns, switches

def fname(comment):
    # "Field Q:I" / "Field i.aA:I" / "Field k.aS:Lg;"
    m = re.match(r'Field (?:(\w+)\.)?(\w+):(.+)$', comment)
    if m:
        cls, n, _ = m.groups()
        return (cls + '.' + n) if cls else n
    return comment

def mname(comment):
    m = re.match(r'(?:Method|InterfaceMethod) (?:(\S+)\.)?([^:]+):(\S+)$', comment)
    if m:
        cls, n, d = m.groups()
        return (cls + '.' + n) if cls else n, d
    return comment, ''

def nargs(desc):
    m = re.match(r'\((.*)\)(.*)', desc)
    if not m:
        return 0, True
    a, r = m.groups()
    n = 0
    i = 0
    while i < len(a):
        c = a[i]
        if c == 'L':
            i = a.index(';', i) + 1
            n += 1
        elif c == '[':
            while a[i] == '[':
                i += 1
            if a[i] == 'L':
                i = a.index(';', i) + 1
            else:
                i += 1
            n += 1
        else:
            i += 1
            n += 1
    return n, r == 'V'

CMP = {'eq': '==', 'ne': '!=', 'lt': '<', 'ge': '>=', 'gt': '>', 'le': '<='}

def simulate(insns, switches, locals_names=None):
    locals_names = locals_names or {}
    targets = set()
    for off, op, arg, c in insns:
        if op.startswith('if') or op == 'goto':
            targets.add(int(arg))
    for off, ent in switches.items():
        for k, t in ent:
            targets.add(t)
    out = []
    stack = []
    snaps = {}

    def L(n):
        return locals_names.get(n, 'r%d' % n) if n != 0 else 'this'

    def pop():
        return stack.pop() if stack else '<?>'

    def snap(t):
        if t in snaps and snaps[t] != list(stack):
            # keep the first, remember divergent one as ternary hint
            a = snaps[t]
            if len(a) == len(stack) and a != stack:
                snaps[t] = [x if x == y else '(%s | %s)' % (x, y) for x, y in zip(a, stack)]
        else:
            snaps[t] = list(stack)

    unreachable = False
    for idx, (off, op, arg, c) in enumerate(insns):
        if off in targets:
            if unreachable or not stack:
                if off in snaps:
                    stack = list(snaps[off])
            elif off in snaps and snaps[off] != stack and len(snaps[off]) == len(stack):
                stack = [x if x == y else '(%s ? %s : %s)' % ('?', y, x) for x, y in zip(snaps[off], stack)]
            out.append('L%d:' % off)
            unreachable = False
        o = op
        if o == 'nop':
            continue
        if o in ('aload_0', 'aload_1', 'aload_2', 'aload_3', 'iload_0', 'iload_1', 'iload_2', 'iload_3'):
            stack.append(L(int(o[-1])))
        elif o in ('aload', 'iload'):
            stack.append(L(int(arg)))
        elif o in ('astore_0', 'astore_1', 'astore_2', 'astore_3', 'istore_0', 'istore_1', 'istore_2', 'istore_3'):
            out.append('  %s = %s' % (L(int(o[-1])), pop()))
        elif o in ('astore', 'istore'):
            out.append('  %s = %s' % (L(int(arg)), pop()))
        elif o == 'iconst_m1':
            stack.append('-1')
        elif o.startswith('iconst_'):
            stack.append(o[-1])
        elif o in ('bipush', 'sipush'):
            stack.append(arg)
        elif o in ('ldc', 'ldc_w', 'ldc2_w'):
            cm = re.match(r'(?:int|float|long|double|String)\s*(.*)$', c)
            stack.append(('"%s"' % cm.group(1)) if c.startswith('String') else (cm.group(1) if cm else c))
        elif o == 'aconst_null':
            stack.append('null')
        elif o == 'getstatic':
            stack.append(fname(c))
        elif o == 'putstatic':
            out.append('  %s = %s' % (fname(c), pop()))
        elif o == 'getfield':
            r = pop()
            stack.append('%s.%s' % (r, fname(c)) if r != 'this' else fname(c))
        elif o == 'putfield':
            v = pop(); r = pop()
            out.append('  %s = %s' % (('%s.%s' % (r, fname(c))) if r != 'this' else fname(c), v))
        elif o in ('iaload', 'aaload', 'saload', 'baload', 'caload'):
            ix = pop(); a = pop()
            stack.append('%s[%s]' % (a, ix))
        elif o in ('iastore', 'aastore', 'sastore', 'bastore', 'castore'):
            v = pop(); ix = pop(); a = pop()
            out.append('  %s[%s] = %s' % (a, ix, v))
        elif o in ('iadd', 'isub', 'imul', 'idiv', 'irem', 'ishl', 'ishr', 'iushr', 'iand', 'ior', 'ixor'):
            b = pop(); a = pop()
            sym = {'iadd': '+', 'isub': '-', 'imul': '*', 'idiv': '/', 'irem': '%', 'ishl': '<<', 'ishr': '>>',
                   'iushr': '>>>', 'iand': '&', 'ior': '|', 'ixor': '^'}[o]
            stack.append('(%s %s %s)' % (a, sym, b))
        elif o == 'ineg':
            stack.append('(-%s)' % pop())
        elif o in ('i2s', 'i2b', 'i2c'):
            stack.append('(%s)%s' % ({'i2s': 'short', 'i2b': 'byte', 'i2c': 'char'}[o], pop()))
        elif o == 'iinc':
            v, k = [x.strip() for x in arg.replace(',', ' ').split()]
            nm = L(int(v))
            # pending stack entries hold the OLD value (post-increment idiom
            # `iload v; iinc v,k`): re-express them against the new value
            for si in range(len(stack)):
                stack[si] = re.sub(r'(?<![\w.])%s(?![\w])' % re.escape(nm),
                                   '(%s - %s)' % (nm, k) if int(k) >= 0 else '(%s + %d)' % (nm, -int(k)), stack[si])
            out.append('  %s += %s' % (nm, k))
        elif o == 'dup':
            stack.append(stack[-1] if stack else '<?>')
        elif o == 'dup_x1':
            a = pop(); b = pop(); stack += [a, b, a]
        elif o == 'dup_x2':
            a = pop(); b = pop(); c2 = pop(); stack += [a, c2, b, a]
        elif o == 'dup2':
            a = pop(); b = pop(); stack += [b, a, b, a]
        elif o == 'pop':
            v = pop()
            if v and not re.match(r'^[\w.\[\]]+$', v):
                out.append('  ' + v)
        elif o == 'pop2':
            pop(); pop()
        elif o == 'swap':
            a = pop(); b = pop(); stack += [a, b]
        elif o == 'arraylength':
            stack.append('%s.length' % pop())
        elif o == 'new':
            stack.append('new ' + c.replace('class ', ''))
        elif o == 'newarray':
            stack.append('new %s[%s]' % (arg, pop()))
        elif o == 'anewarray':
            stack.append('new %s[%s]' % (c.replace('class ', ''), pop()))
        elif o == 'multianewarray':
            stack.append('new %s[...]' % c)
        elif o == 'checkcast':
            pass
        elif o == 'instanceof':
            stack.append('(%s instanceof %s)' % (pop(), c.replace('class ', '')))
        elif o in ('invokevirtual', 'invokespecial', 'invokestatic', 'invokeinterface'):
            name, desc = mname(c)
            n, isvoid = nargs(desc)
            args = [pop() for _ in range(n)][::-1]
            recv = None
            if o != 'invokestatic':
                recv = pop()
            if o == 'invokespecial' and name.endswith('<init>'):
                # constructor: replace the matching 'new X' on the stack top copy
                expr = '%s(%s)' % (recv.replace('new ', 'new ', 1), ', '.join(args))
                if stack and stack[-1] == recv:
                    stack[-1] = expr
                else:
                    out.append('  ' + expr)
            else:
                if recv is None:
                    expr = '%s(%s)' % (name, ', '.join(args))
                elif recv == 'this':
                    expr = '%s(%s)' % (name, ', '.join(args))
                else:
                    expr = '%s.%s(%s)' % (recv, name.split('.')[-1], ', '.join(args))
                if isvoid:
                    out.append('  ' + expr)
                else:
                    stack.append(expr)
        elif o in ('return',):
            out.append('  return'); unreachable = True
        elif o in ('ireturn', 'areturn'):
            out.append('  return %s' % pop()); unreachable = True
        elif o == 'athrow':
            out.append('  throw %s' % pop()); unreachable = True
        elif o == 'goto':
            t = int(arg)
            snap(t)
            out.append('  goto L%d' % t); unreachable = True
        elif o in ('ifeq', 'ifne', 'iflt', 'ifge', 'ifgt', 'ifle'):
            v = pop(); t = int(arg)
            snap(t)
            out.append('  if (%s %s 0) goto L%d' % (v, CMP[o[2:]], t))
        elif o in ('if_icmpeq', 'if_icmpne', 'if_icmplt', 'if_icmpge', 'if_icmpgt', 'if_icmple'):
            b = pop(); a = pop(); t = int(arg)
            snap(t)
            out.append('  if (%s %s %s) goto L%d' % (a, CMP[o[7:]], b, t))
        elif o in ('if_acmpeq', 'if_acmpne'):
            b = pop(); a = pop(); t = int(arg)
            snap(t)
            out.append('  if (%s %s %s) goto L%d' % (a, '==' if o.endswith('eq') else '!=', b, t))
        elif o in ('ifnull', 'ifnonnull'):
            v = pop(); t = int(arg)
            snap(t)
            out.append('  if (%s %s null) goto L%d' % (v, '==' if o == 'ifnull' else '!=', t))
        elif o in ('tableswitch', 'lookupswitch'):
            v = pop()
            ent = switches.get(off, [])
            out.append('  switch (%s) {' % v)
            for k, t in ent:
                out.append('    %s -> L%d' % (k, t))
                snap(t)
            out.append('  }'); unreachable = True
        else:
            out.append('  <%s %s %s>' % (o, arg, c))
    return out

def main():
    if len(sys.argv) < 3 or sys.argv[1] in ('-h', '--help'):
        sys.exit(__doc__)
    path, header = sys.argv[1], sys.argv[2]
    lo = hi = desc = None
    a = sys.argv[3:]
    while a:
        if a[0] == '--from': lo = int(a[1]); a = a[2:]
        elif a[0] == '--to': hi = int(a[1]); a = a[2:]
        elif a[0] == '--desc': desc = a[1]; a = a[2:]
        else: a = a[1:]
    lines = load_method(path, header, desc)
    insns, switches = parse(lines)
    if lo is not None or hi is not None:
        insns = [x for x in insns if (lo is None or x[0] >= lo) and (hi is None or x[0] < hi)]
    for l in simulate(insns, switches, {1: 'rec'} if 'short[]' in header else {}):
        print(l)

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
