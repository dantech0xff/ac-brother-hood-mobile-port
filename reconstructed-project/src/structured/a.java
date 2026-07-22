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
    private int k;
    private static int j = 40;
    private static int l = -1;
    private static final int m = 1;
    private static boolean n = false;

    a() {
        e();
    }

    a(b bVar, int i, int i2) {
        e();
        this.a = i;
        this.b = i2;
        a(bVar);
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

    final void a(b bVar) {
        this.d = bVar;
        if (bVar != null) {
            a(l, -1);
        } else {
            this.e = l;
        }
    }

    final void a(int i, int i2) {
        if (this.i || i != this.e) {
            this.e = i;
            a(0);
            this.h = i2 - 1;
            this.i = false;
        }
    }

    final int a(int i) {
        if (this.e < 0) {
            return -1;
        }
        int iA = a();
        while (i > iA) {
            i -= iA;
        }
        this.f = i;
        this.g = 0;
        return i;
    }

    final int a() {
        if (this.e >= 0) {
            return this.d.b(this.e);
        }
        return -1;
    }

    private int f() {
        if (this.e >= 0) {
            return this.d.a(this.e, this.f) * j;
        }
        return 0;
    }

    final boolean b() {
        if (this.e < 0) {
            return true;
        }
        if (this.h < 0) {
            return false;
        }
        return this.i;
    }

    final void c() {
        if (this.e >= 0 && !this.i) {
            if (this.k == -1) {
                this.d.a(j.a, this.e, this.f, this.a, this.b, this.c, 0, 0);
                return;
            }
            int iB = this.d.b();
            this.d.l(this.k);
            this.d.a(j.a, this.e, this.f, this.a, this.b, this.c, 0, 0);
            this.d.l(iB);
        }
    }

    final void b(int i) {
        if (this.i || this.e < 0) {
            return;
        }
        int iF = f();
        int i2 = iF;
        if (iF == 0) {
            return;
        }
        while (true) {
            if (this.g < i2) {
                break;
            }
            this.g -= i2;
            if (this.f < this.d.b(this.e) - 1) {
                this.f++;
            } else if (this.h == 0) {
                this.i = true;
                break;
            } else {
                if (this.h > 0) {
                    this.h--;
                }
                this.f = 0;
            }
            int iF2 = f();
            i2 = iF2;
            if (iF2 == 0) {
                break;
            }
        }
        this.g += i;
    }

    @Override // java.lang.Runnable
    public final void run() {
    }

    static void d() {
        for (int i = 0; i < m; i++) {
            try {
            } catch (Exception unused) {
                return;
            }
        }
    }

    static {
        short[] sArr = {-1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8};
        short[] sArr2 = {7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, Short.MAX_VALUE};
    }
}
