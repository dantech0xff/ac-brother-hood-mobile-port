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
    private static c[] m;
    static short j;

    public c() {
    }

    static void a(short[] r4) {
        c r0 = new c();
        r0.k = r4[1];
        r0.a = r4[2];
        r0.b = r4[3];
        r0.c = r4[4];
        r0.d = r4[5];
        r0.e = (byte) r4[6];
        r0.f = (byte) r4[7];
        r0.g = r4[8];
        m[l] = r0;
        l = (short) (l + 1);
    }

    static void a(c r4, i r5) {
        c r0 = new c();
        r0.k = j;
        j = (short) (j + 1);
        r0.a = r4.a + r5.ak;
        r0.b = r4.b;
        r0.c = r4.c;
        r0.d = r4.d;
        r0.e = r4.e;
        r0.f = r4.f;
        r0.g = r4.g;
        m[l] = r0;
        l = (short) (l + 1);
    }

    static void a() {
        int r4 = 0;
    L4:
        if (r4 >= m.length) goto L6;
        m[r4] = null;
        r4 = r4 + 1;
        goto L4
    L6:
        l = 0;
        j = 10000;
    }

    static c a(int r3) {
        if (r3 >= 0) goto L6;
        return null;
    L6:
        int r4 = 0;
    L8:
        if (r4 >= l) goto L16;
        if (m[r4] == null) goto L15;
        if (m[r4].k != r3) goto L15;
        return m[r4];
    L15:
        r4 = r4 + 1;
        goto L8
    L16:
        return null;
    }

    static {
        l = 0;
        m = new c[400];
        j = 10000;
    }
}
