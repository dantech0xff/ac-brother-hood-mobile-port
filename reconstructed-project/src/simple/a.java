package defpackage;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:a.class */
final class a implements Runnable {
    int a;
    int b;
    int c;
    b d;
    int e;
    private int f;
    private int g;
    private int h;
    private boolean i;
    private static int j;
    private int k;
    private static int l;
    private static final int m = 0;
    private static boolean n;

    a() {
        e();
    }

    a(b r4, int r5, int r6) {
        e();
        this.a = r5;
        this.b = r6;
        a(r4);
    }

    private void e() {
        this.a = 0;
        this.b = 0;
        this.e = l;
        this.f = 0;
        this.d = null;
        this.c = 0;
        this.g = 0;
        this.h = 1;
        this.k = -1;
        this.i = true;
    }

    final void a(b r5) {
        this.d = r5;
        if (r5 == null) goto L6;
        a(l, -1);
        return;
    L6:
        this.e = l;
    }

    final void a(int r5, int r6) {
        if (this.i == false) goto L5;
    L6:
        this.e = r5;
        a(0);
        this.h = r6 - 1;
        this.i = false;
        return;
    L5:
        if (r5 != this.e) goto L6;
    }

    final int a(int r4) {
        if (this.e >= 0) goto L6;
        return -1;
    L6:
        int r0 = a();
    L8:
        if (r4 <= r0) goto L10;
        r4 = r4 - r0;
        goto L8
    L10:
        this.f = r4;
        this.g = 0;
        return r4;
    }

    final int a() {
        if (this.e >= 0) goto L5;
        return -1;
    L5:
        return this.d.b(this.e);
    }

    private int f() {
        if (this.e >= 0) goto L5;
        return 0;
    L5:
        return this.d.a(this.e, this.f) * j;
    }

    final boolean b() {
        if (this.e >= 0) goto L7;
        return true;
    L7:
        if (this.h >= 0) goto L11;
        return false;
    L11:
        return this.i;
    }

    final void c() {
        if (this.e >= 0) goto L6;
        return;
    L6:
        if (this.i == false) goto L9;
        return;
    L9:
        if (this.k == (-1)) goto L12;
        int r0 = this.d.b();
        this.d.l(this.k);
        this.d.a(j.a, this.e, this.f, this.a, this.b, this.c, 0, 0);
        this.d.l(r0);
        return;
    L12:
        this.d.a(j.a, this.e, this.f, this.a, this.b, this.c, 0, 0);
    }

    final void b(int r5) {
        if (this.i == false) goto L5;
        return;
    L5:
        if (this.e < 0) goto L30;
        int r0 = f();
        int r6 = r0;
        if (r0 != 0) goto L11;
        return;
    L11:
        if (this.g < r6) goto L24;
        this.g -= r6;
        if (this.f >= (this.d.b(this.e) - 1)) goto L16;
        this.f++;
    L22:
        int r02 = f();
        r6 = r02;
        if (r02 != 0) goto L11;
    L16:
        if (this.h == 0) goto L17;
        if (this.h <= 0) goto L21;
        this.h--;
    L21:
        this.f = 0;
        goto L22
    L17:
        this.i = true;
    L24:
        this.g += r5;
        return;
    }

    @Override // java.lang.Runnable
    public final void run() {
    }

    static void d() {
        int r7 = 0;
    L9:
        if (r7 >= m) goto L6;
        r7 = r7 + 1;     // Catch: Exception -> L7
        goto L9
    L6:
        return;
    }

    static {
        j = 40;
        l = -1;
        m = 1;
        n = false;
        short[] r0 = {-1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8};
        short[] r02 = {7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, Short.MAX_VALUE};
    }
}
