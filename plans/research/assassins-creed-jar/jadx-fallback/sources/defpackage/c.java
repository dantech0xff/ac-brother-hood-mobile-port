package defpackage;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:c.class */
public final class c {
    int a;
    int b;
    private short k;
    short c;
    short d;
    byte e;
    byte f;
    short g;
    short h;
    short i;
    private static short l;
    private static defpackage.c[] m;
    static short j;

    public c() {
            r2 = this;
            r0 = r2
            r0.<init>()
            return
    }

    static void a(short[] r4) {
            c r0 = new c
            r1 = r0
            r1.<init>()
            r1 = r0
            r5 = r1
            r1 = r4
            r2 = 1
            short r1 = r1[r2]
            r0.k = r1
            r0 = r5
            r1 = r4
            r2 = 2
            short r1 = r1[r2]
            r0.a = r1
            r0 = r5
            r1 = r4
            r2 = 3
            short r1 = r1[r2]
            r0.b = r1
            r0 = r5
            r1 = r4
            r2 = 4
            short r1 = r1[r2]
            r0.c = r1
            r0 = r5
            r1 = r4
            r2 = 5
            short r1 = r1[r2]
            r0.d = r1
            r0 = r5
            r1 = r4
            r2 = 6
            short r1 = r1[r2]
            byte r1 = (byte) r1
            r0.e = r1
            r0 = r5
            r1 = r4
            r2 = 7
            short r1 = r1[r2]
            byte r1 = (byte) r1
            r0.f = r1
            r0 = r5
            r1 = r4
            r2 = 8
            short r1 = r1[r2]
            r0.g = r1
            c[] r0 = defpackage.c.m
            short r1 = defpackage.c.l
            r2 = r5
            r0[r1] = r2
            short r0 = defpackage.c.l
            r1 = 1
            int r0 = r0 + r1
            short r0 = (short) r0
            defpackage.c.l = r0
            return
    }

    static void a(defpackage.c r4, defpackage.i r5) {
            c r0 = new c
            r1 = r0
            r1.<init>()
            r1 = r0
            r6 = r1
            short r1 = defpackage.c.j
            r0.k = r1
            short r0 = defpackage.c.j
            r1 = 1
            int r0 = r0 + r1
            short r0 = (short) r0
            defpackage.c.j = r0
            r0 = r6
            r1 = r4
            int r1 = r1.a
            r2 = r5
            int r2 = r2.ak
            int r1 = r1 + r2
            r0.a = r1
            r0 = r6
            r1 = r4
            int r1 = r1.b
            r0.b = r1
            r0 = r6
            r1 = r4
            short r1 = r1.c
            r0.c = r1
            r0 = r6
            r1 = r4
            short r1 = r1.d
            r0.d = r1
            r0 = r6
            r1 = r4
            byte r1 = r1.e
            r0.e = r1
            r0 = r6
            r1 = r4
            byte r1 = r1.f
            r0.f = r1
            r0 = r6
            r1 = r4
            short r1 = r1.g
            r0.g = r1
            c[] r0 = defpackage.c.m
            short r1 = defpackage.c.l
            r2 = r6
            r0[r1] = r2
            short r0 = defpackage.c.l
            r1 = 1
            int r0 = r0 + r1
            short r0 = (short) r0
            defpackage.c.l = r0
            return
    }

    static void a() {
            r0 = 0
            r4 = r0
        L2:
            r0 = r4
            c[] r1 = defpackage.c.m
            int r1 = r1.length
            if (r0 >= r1) goto L16
            c[] r0 = defpackage.c.m
            r1 = r4
            r2 = 0
            r0[r1] = r2
            int r4 = r4 + 1
            goto L2
        L16:
            r0 = 0
            defpackage.c.l = r0
            r0 = 10000(0x2710, float:1.4013E-41)
            defpackage.c.j = r0
            return
    }

    static defpackage.c a(int r3) {
            r0 = r3
            if (r0 >= 0) goto L6
            r0 = 0
            return r0
        L6:
            r0 = 0
            r4 = r0
        L8:
            r0 = r4
            short r1 = defpackage.c.l
            if (r0 >= r1) goto L2f
            c[] r0 = defpackage.c.m
            r1 = r4
            r0 = r0[r1]
            if (r0 == 0) goto L29
            c[] r0 = defpackage.c.m
            r1 = r4
            r0 = r0[r1]
            short r0 = r0.k
            r1 = r3
            if (r0 != r1) goto L29
            c[] r0 = defpackage.c.m
            r1 = r4
            r0 = r0[r1]
            return r0
        L29:
            int r4 = r4 + 1
            goto L8
        L2f:
            r0 = 0
            return r0
    }

    static {
            r0 = 0
            defpackage.c.l = r0
            r0 = 400(0x190, float:5.6E-43)
            c[] r0 = new defpackage.c[r0]
            defpackage.c.m = r0
            r0 = 10000(0x2710, float:1.4013E-41)
            defpackage.c.j = r0
            return
    }
}
