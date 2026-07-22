package defpackage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Random;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:j.class */
public abstract class j extends Canvas implements Runnable {
    static boolean b;
    static int c;
    private static long w;
    private static long x;
    private static Display y;
    private static boolean z;
    private static MIDlet A;
    private long C;
    static int f;
    private static long H;
    private static int I;
    static int g;
    static j h;
    private static int J;
    private static int K;
    private static int L;
    private static int M;
    private static int N;
    private static int P;
    private static Hashtable Q;
    private static Hashtable R;
    static Random j;
    static int k;
    static int l;
    private static int[] T;
    private static int[] U;
    private static int[] V;
    private static String Z;
    private static InputStream aa;
    private static int ab;
    private static byte[] ac;
    private static int ad;
    private static int ae;
    private static short af;
    private static int[] ag;
    private static short ah;
    private static short[] ai;
    private static int aj;
    private static int ak;
    private static boolean al;
    private static byte[] am;
    private static byte[][] an;
    private static byte[] ao;
    private static int ap;
    private static long aq;
    private static long ar;
    private static byte[] as;
    private static int at;
    private static short[] au;
    private static int aw;
    private static byte[] ay;
    private static int[] az;
    private static String[] aA;
    private static int aB;
    private static int[] aC;
    private static Image aD;
    private static int aI;
    private static int aJ;
    private static int aK;
    private static int aL;
    private static int aM;
    public static Graphics a = null;
    private static Graphics u = null;
    private static Graphics v = null;
    static int d = 400;
    static int e = 240;
    private static int B = 62;
    private static int D = -1;
    private static Image E = null;
    private static Graphics F = null;
    private static boolean G = true;
    private static int O = -9999;
    private static int S = 24;
    static final int i = 256;
    static final int m = 256;
    static final int n = (90 * m) / 360;
    private static int W = (180 * m) / 360;
    static final int o = (270 * m) / 360;
    private static int X = (360 * m) / 360;
    private static int Y = 804;
    private static int av = 0;
    private static String ax = "UTF-8";
    private static boolean aE = false;
    static final boolean p = false;
    private static boolean aF = false;
    static int q = 0;
    static int[][] r = null;
    private static Image aG = null;
    static Graphics s = null;
    static int t = 0;
    private static int aH = 0;

    abstract void a();

    public j(Object obj, Object obj2) {
        h = this;
        c = -1;
        z = true;
        A = (MIDlet) obj;
        y = (Display) obj2;
        j();
        R = new Hashtable();
        Hashtable hashtable = new Hashtable();
        Q = hashtable;
        hashtable.put(new Integer(48), new Integer(6));
        Q.put(new Integer(49), new Integer(7));
        Q.put(new Integer(50), new Integer(1));
        Q.put(new Integer(51), new Integer(9));
        Q.put(new Integer(52), new Integer(3));
        Q.put(new Integer(53), new Integer(5));
        Q.put(new Integer(54), new Integer(4));
        Q.put(new Integer(55), new Integer(13));
        Q.put(new Integer(56), new Integer(2));
        Q.put(new Integer(57), new Integer(15));
        Q.put(new Integer(35), new Integer(17));
        Q.put(new Integer(42), new Integer(16));
        Q.put(new Integer(-6), new Integer(18));
        Q.put(new Integer(-7), new Integer(19));
        R.put(new Integer(-5), new Integer(5));
        R.put(new Integer(-1), new Integer(1));
        R.put(new Integer(-2), new Integer(2));
        R.put(new Integer(-3), new Integer(3));
        R.put(new Integer(-4), new Integer(4));
        H = System.currentTimeMillis();
        this.C = H;
    }

    protected final void b() {
        if (c >= 0) {
            return;
        }
        d = 400;
        e = 240;
        a(System.currentTimeMillis());
        c = 0;
        new Thread(this).start();
    }

    protected void c() {
        if (b) {
            return;
        }
        b = true;
        a.d();
    }

    protected void d() {
        if (b) {
            long jCurrentTimeMillis = System.currentTimeMillis();
            w = jCurrentTimeMillis;
            H = jCurrentTimeMillis;
            this.C = jCurrentTimeMillis;
            b = false;
            j();
            if (!z) {
                repaint();
            }
            k();
        }
    }

    public void hideNotify() {
        c();
    }

    public void showNotify() {
        d();
    }

    public void sizeChanged(int i2, int i3) {
    }

    private void j() {
        setFullScreenMode(true);
        if (y == null || y.getCurrent() == this) {
            return;
        }
        y.setCurrent(this);
    }

    @Override // java.lang.Runnable
    public void run() throws InterruptedException {
        try {
            j();
            z = false;
            while (c >= 0) {
                if (b) {
                    this.C = Math.min(this.C, System.currentTimeMillis());
                    Thread.sleep(1L);
                } else {
                    repaint();
                    serviceRepaints();
                    long jCurrentTimeMillis = System.currentTimeMillis();
                    this.C = Math.min(this.C, jCurrentTimeMillis);
                    Thread.sleep(Math.max(1L, B - (jCurrentTimeMillis - this.C)));
                    this.C = System.currentTimeMillis();
                }
            }
        } catch (Exception unused) {
            c = -1;
        }
        an = null;
        A.notifyDestroyed();
    }

    public void paint(Graphics graphics) {
        v = graphics;
        long jCurrentTimeMillis = System.currentTimeMillis() - x;
        x = System.currentTimeMillis();
        if (jCurrentTimeMillis > 3000 && x != 0) {
            c();
            d();
        }
        if (b || z) {
            return;
        }
        z = true;
        J = M;
        K = L;
        M = 0;
        N = 0;
        if (P > 0) {
            if (P != Integer.MAX_VALUE) {
                P -= f;
            }
            k();
        }
        long jCurrentTimeMillis2 = System.currentTimeMillis();
        w = jCurrentTimeMillis2;
        int i2 = (int) (jCurrentTimeMillis2 - H);
        f = i2;
        if (i2 < 0) {
            f = 0;
        }
        if (f > 1000) {
            f = 1000;
        }
        H = w;
        I += f;
        g++;
        try {
            u = graphics;
            a = graphics;
            a();
        } catch (Exception unused) {
            c = -1;
        }
        z = false;
        v = null;
    }

    protected void keyPressed(int i2) {
        int iK = 1 << k(i2);
        M |= iK;
        L |= iK;
    }

    protected void keyReleased(int i2) {
        int iK = 1 << k(i2);
        N |= iK;
        L &= iK ^ (-1);
    }

    private static byte k(int i2) {
        Integer num = new Integer(i2);
        if (Q == null) {
            return (byte) 0;
        }
        Integer num2 = (Integer) Q.get(num);
        if (num2 != null) {
            return num2.byteValue();
        }
        Integer num3 = (Integer) R.get(num);
        if (num3 != null) {
            return num3.byteValue();
        }
        return (byte) 0;
    }

    private static void k() {
        J = 0;
        K = 0;
        L = 0;
        M = 0;
        N = 0;
    }

    static void a(String str, int i2, int i3) throws IOException {
        a(str);
        T = (int[]) f(0);
        U = (int[]) f(1);
        e();
    }

    static final int a(int i2) {
        return (i2 + (i >> 1)) >> 8;
    }

    static final void a(long j2) {
        if (j == null) {
            j = new Random(j2);
        } else {
            j.setSeed(j2);
        }
    }

    static int a(int i2, int i3) {
        if (i3 == i2) {
            return i3;
        }
        int iNextInt = j.nextInt();
        int i4 = iNextInt;
        if (iNextInt < 0) {
            i4 = -i4;
        }
        return i2 + (i4 % (i3 - i2));
    }

    static int b(int i2) {
        if (i2 < 0) {
            i2 = -i2;
        }
        int i3 = i2 & (X - 1);
        if (i3 <= n) {
            return T[i3];
        }
        if (i3 < W) {
            return -T[W - i3];
        }
        if (i3 <= o) {
            return -T[i3 - W];
        }
        return T[X - i3];
    }

    static int c(int i2) {
        int iB = b(i2);
        if (iB == 0) {
            return Integer.MAX_VALUE;
        }
        return (b(n - i2) << 8) / iB;
    }

    private static int b(int i2, int i3, int i4) {
        while (i2 + 1 < i3) {
            int i5 = (i2 + i3) >> 1;
            if (i4 > c(i5)) {
                i2 = i5;
            } else {
                i3 = i5;
            }
        }
        return i4 >= c(i3) ? i3 : i2;
    }

    static int b(int i2, int i3) {
        if (V == null) {
            V = new int[i + 1];
            for (int i4 = 0; i4 < i + 1; i4++) {
                int[] iArr = V;
                int i5 = i4;
                int i6 = i;
                int i7 = i4;
                iArr[i5] = i6 > 0 ? i7 > 0 ? b(0, n, (i7 * i) / i6) : i7 == 0 ? 0 : b(o, X, (i7 * i) / i6) : i6 == 0 ? i7 > 0 ? n : i7 == 0 ? 0 : o : i7 > 0 ? b(n, W, (i7 * i) / i6) : i7 == 0 ? W : b(W, o, (i7 * i) / i6);
            }
        }
        if (i2 == 0) {
            if (i3 > 0) {
                return n;
            }
            if (i3 == 0) {
                return 0;
            }
            return o;
        }
        if (i2 > 0) {
            if (i3 >= 0) {
                if (i2 >= i3) {
                    return V[(i3 * i) / i2];
                }
                return n - V[(i2 * i) / i3];
            }
            int i8 = -i3;
            if (i2 >= i8) {
                return X - V[(i8 * i) / i2];
            }
            return o + V[(i2 * i) / i8];
        }
        int i9 = -i2;
        if (i3 >= 0) {
            if (i9 >= i3) {
                return W - V[(i3 * i) / i9];
            }
            return n + V[(i9 * i) / i3];
        }
        int i10 = -i3;
        if (i9 >= i10) {
            return W + V[(i10 * i) / i9];
        }
        return o - V[(i9 * i) / i10];
    }

    static int d(int i2) {
        if (i2 >= 65536) {
            return i2 >= 16777216 ? i2 >= 268435456 ? i2 >= 1073741824 ? U[i2 >> 24] << 8 : U[i2 >> 22] << 7 : i2 >= 67108864 ? U[i2 >> 20] << 6 : U[i2 >> 18] << 5 : i2 >= 1048576 ? i2 >= 4194304 ? U[i2 >> 16] << 4 : U[i2 >> 14] << 3 : i2 >= 262144 ? U[i2 >> 12] << 2 : U[i2 >> 10] << 1;
        }
        if (i2 >= 256) {
            return i2 >= 4096 ? i2 >= 16384 ? U[i2 >> 8] : U[i2 >> 6] >> 1 : i2 >= 1024 ? U[i2 >> 4] >> 2 : U[i2 >> 2] >> 3;
        }
        if (i2 >= 0) {
            return U[i2] >> 4;
        }
        return 0;
    }

    private static final int b(int i2, int i3, int i4, int i5, int i6, int i7) {
        return (((i2 * i6) + ((i3 * 2) * i5)) + (i4 * i7)) / (1 << 16);
    }

    static void a(int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        int i9 = i8 * i8;
        int i10 = i - i8;
        int i11 = i10 * i10;
        int i12 = i10 * i8;
        k = b(i2, i4, i6, i12, i11, i9);
        l = b(i3, i5, i7, i12, i11, i9);
    }

    private static byte[] l(int i2) throws IOException {
        int iP;
        int iE;
        int iF;
        int i3;
        int iM = m(i2);
        byte[] bArr = null;
        if (al) {
            try {
                byte[] bArr2 = new byte[iM + 1];
                a(aa, bArr2, 0, iM);
                int i4 = 0;
                for (int i5 = 0; i5 < 4; i5++) {
                    i4 += (bArr2[i5 + 5] & 255) << (i5 << 3);
                }
                int i6 = bArr2[0] & 255;
                int i7 = i6 / 45;
                int i8 = i6 % 45;
                int i9 = i8 / 9;
                int i10 = i8 % 9;
                int i11 = 1846 + (768 << (i10 + i9));
                as = new byte[i4];
                au = new short[i11];
                int i12 = i11 << 1;
                int i13 = i4;
                int i14 = 1846 + (768 << (i10 + i9));
                short[] sArr = au;
                int i15 = 0;
                boolean z2 = false;
                int i16 = 0;
                int i17 = 1;
                int i18 = 1;
                int i19 = 1;
                int i20 = 1;
                int i21 = 0;
                int i22 = (1 << i7) - 1;
                int i23 = (1 << i9) - 1;
                if (i12 >= (i14 << 1)) {
                    for (int i24 = 0; i24 < i14; i24++) {
                        sArr[i24] = 1024;
                    }
                    int length = bArr2.length;
                    ao = bArr2;
                    ap = length;
                    at = 13;
                    ar = 0L;
                    aq = 4294967295L;
                    bArr2[ap - 1] = -1;
                    for (int i25 = 0; i25 < 5; i25++) {
                        long j2 = ar << 8;
                        byte[] bArr3 = ao;
                        at = at + 1;
                        ar = j2 | (bArr3[r2] & 255);
                    }
                    while (i21 < i13) {
                        int i26 = i21 & i22;
                        if (p(0 + (i15 << 4) + i26) == 0) {
                            int i27 = 1846 + (768 * (((i21 & i23) << i10) + ((i16 & 255) >> (8 - i10))));
                            i15 = i15 < 4 ? 0 : i15 < 10 ? i15 - 3 : i15 - 6;
                            if (z2) {
                                byte b2 = as[i21 - i17];
                                int iP2 = 1;
                                while (true) {
                                    int i28 = (b2 >> 7) & 1;
                                    b2 = (byte) (b2 << 1);
                                    int iP3 = p(i27 + ((i28 + 1) << 8) + iP2);
                                    iP2 = (iP2 << 1) | iP3;
                                    if (i28 == iP3) {
                                        if (iP2 >= 256) {
                                            break;
                                        }
                                    } else {
                                        while (iP2 < 256) {
                                            iP2 = (iP2 << 1) | p(i27 + iP2);
                                        }
                                    }
                                }
                                i16 = iP2 & 255;
                                z2 = false;
                            } else {
                                int i29 = 1;
                                do {
                                    iP = (i29 << 1) | p(i27 + i29);
                                    i29 = iP;
                                } while (iP < 256);
                                i16 = i29 & 255;
                            }
                            int i30 = i21;
                            i21++;
                            as[i30] = (byte) i16;
                        } else {
                            z2 = true;
                            if (p(i15 + 192) == 1) {
                                if (p(i15 + 204) != 0) {
                                    if (p(i15 + 216) == 0) {
                                        i3 = i18;
                                    } else {
                                        if (p(i15 + 228) == 0) {
                                            i3 = i19;
                                        } else {
                                            i3 = i20;
                                            i20 = i19;
                                        }
                                        i19 = i18;
                                    }
                                    i18 = i17;
                                    i17 = i3;
                                } else if (p(240 + (i15 << 4) + i26) == 0) {
                                    i15 = i15 < 7 ? 9 : 11;
                                    i16 = as[i21 - i17] & 255;
                                    int i31 = i21;
                                    i21++;
                                    as[i31] = (byte) i16;
                                }
                                iE = e(1332, i26);
                                i15 = i15 < 7 ? 8 : 11;
                            } else {
                                i20 = i19;
                                i19 = i18;
                                i18 = i17;
                                i15 = i15 < 7 ? 7 : 10;
                                iE = e(818, i26);
                                int iD = d(432 + ((iE < 4 ? iE : 3) << 6), 6);
                                if (iD >= 4) {
                                    int i32 = (iD >> 1) - 1;
                                    int i33 = (2 | (iD & 1)) << i32;
                                    if (iD < 14) {
                                        iF = i33 + f(((i33 + 688) - iD) - 1, i32);
                                    } else {
                                        long j3 = aq;
                                        long j4 = ar;
                                        int i34 = 0;
                                        for (int i35 = i32 - 4; i35 > 0; i35--) {
                                            j3 >>= 1;
                                            i34 <<= 1;
                                            if (j4 >= j3) {
                                                j4 -= j3;
                                                i34 |= 1;
                                            }
                                            if (j3 < 16777216) {
                                                j3 <<= 8;
                                                byte[] bArr4 = ao;
                                                at = at + 1;
                                                j4 = (j4 << 8) | (bArr4[r3] & 255);
                                            }
                                        }
                                        aq = j3;
                                        ar = j4;
                                        iF = i33 + (i34 << 4) + f(802, 4);
                                    }
                                } else {
                                    iF = iD;
                                }
                                i17 = iF + 1;
                            }
                            int i36 = iE + 2;
                            do {
                                i16 = as[i21 - i17] & 255;
                                int i37 = i21;
                                i21++;
                                as[i37] = (byte) i16;
                                i36--;
                                if (i36 > 0) {
                                }
                            } while (i21 < i13);
                        }
                    }
                }
                au = null;
                ao = null;
                ae += iM;
                bArr = as;
                as = null;
            } catch (Exception unused) {
            }
        } else {
            byte[] bArr5 = new byte[iM];
            bArr = bArr5;
            a(bArr5, 0, bArr.length);
        }
        return bArr;
    }

    private static void l() {
        int i2 = aj == ah - 1 ? af - ai[aj] : ai[aj + 1] - ai[aj];
        ag = new int[i2 + 1];
        for (int i3 = 0; i3 < i2 + 1; i3++) {
            ag[i3] = (n() & 255) | ((n() & 255) << 8) | ((n() & 255) << 16) | ((n() & 255) << 24);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v12, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    static final void a(String str) throws IOException {
        ab = 1;
        if (Z == null || str == null || str.compareTo(Z) != 0) {
            e();
            Z = str;
            ac = null;
            ad = 0;
            aa = b(Z);
            af = (short) o();
            int iO = (short) o();
            ah = iO;
            ai = new short[iO];
            for (int i2 = 0; i2 < ah; i2++) {
                ai[i2] = (short) o();
            }
            aj = 0;
            l();
        }
    }

    private static InputStream b(String str) {
        InputStream resourceAsStream = null;
        if (ab == 3) {
            resourceAsStream = new ByteArrayInputStream(ac, ad, ac.length - ad);
        } else if (ab != 2 && ab == 1) {
            resourceAsStream = "".getClass().getResourceAsStream(str);
        }
        return resourceAsStream;
    }

    static final void e() throws IOException {
        m();
        if (ab == 3) {
            ac = null;
        }
    }

    private static final void m() throws IOException {
        if (aa != null) {
            try {
                aa.close();
            } catch (Exception unused) {
            }
            aa = null;
        }
        ae = 0;
    }

    private static int m(int i2) throws IOException {
        int i3 = ah - 1;
        while (i3 >= 0 && ai[i3] > i2) {
            i3--;
        }
        if (aj != i3) {
            aj = i3;
            e();
            if (aj == 0) {
                String str = Z;
                Z = null;
                a(str);
            } else {
                aa = b(new StringBuffer().append(Z).append(".").append(aj).toString());
                l();
            }
        } else if (aa == null) {
            if (aj == 0) {
                String str2 = Z;
                Z = null;
                a(str2);
            } else {
                aa = b(new StringBuffer().append(Z).append(".").append(aj).toString());
            }
        }
        int i4 = i2 - ai[aj];
        int i5 = ag[i4];
        int i6 = ag[i4 + 1] - ag[i4];
        int i7 = i5;
        if (ae != i7) {
            if (ae > i7) {
                m();
                if (aj == 0) {
                    aa = b(Z);
                } else {
                    aa = b(new StringBuffer().append(Z).append(".").append(aj).toString());
                }
            } else {
                i7 -= ae;
            }
            o(i7);
        }
        al = false;
        if (i6 > 0) {
            n(n() & 255);
            i6--;
        }
        return i6;
    }

    private static void n(int i2) {
        ak = i2;
        if (i2 >= 127) {
            ak -= 127;
            al = true;
        }
    }

    static final byte[] e(int i2) {
        return l(i2);
    }

    private static void o(int i2) throws IOException {
        if (i2 == 0) {
            return;
        }
        if (ab == 3) {
            ae += i2;
            while (i2 > 0) {
                try {
                    i2 = (int) (i2 - aa.skip(i2));
                } catch (Exception unused) {
                    return;
                }
            }
            return;
        }
        if (am == null) {
            am = new byte[256];
        }
        while (i2 > 256) {
            a(am, 0, 256);
            i2 -= 256;
        }
        if (i2 > 0) {
            a(am, 0, i2);
        }
    }

    private static int n() throws IOException {
        int i2 = 0;
        try {
            i2 = aa.read();
        } catch (Exception unused) {
        }
        ae++;
        return i2;
    }

    private static int o() {
        return (n() & 255) | ((n() & 255) << 8);
    }

    private static int a(byte[] bArr, int i2, int i3) throws IOException {
        int i4 = 0;
        int i5 = i3;
        while (i5 > 0) {
            try {
                int i6 = aa.read(bArr, i4, i5);
                i5 -= i6;
                i4 += i6;
            } catch (Exception unused) {
            }
        }
        ae += i3;
        return i3;
    }

    static final Object f(int i2) throws IOException {
        Object objA;
        m(i2);
        av = 0;
        if (al) {
            objA = a(new ByteArrayInputStream(l(i2)));
        } else {
            objA = a(aa);
            ae += av;
        }
        return objA;
    }

    private static int p(int i2) {
        long j2 = (aq >> 11) * au[i2];
        if (ar < j2) {
            aq = j2;
            short[] sArr = au;
            sArr[i2] = (short) (sArr[i2] + ((2048 - au[i2]) >> 5));
            if (aq >= 16777216) {
                return 0;
            }
            long j3 = ar << 8;
            byte[] bArr = ao;
            at = at + 1;
            ar = j3 | (bArr[r2] & 255);
            aq <<= 8;
            return 0;
        }
        aq -= j2;
        ar -= j2;
        short[] sArr2 = au;
        sArr2[i2] = (short) (sArr2[i2] - (au[i2] >> 5));
        if (aq >= 16777216) {
            return 1;
        }
        long j4 = ar << 8;
        byte[] bArr2 = ao;
        at = at + 1;
        ar = j4 | (bArr2[r2] & 255);
        aq <<= 8;
        return 1;
    }

    private static int d(int i2, int i3) {
        int iP = 1;
        for (int i4 = i3; i4 > 0; i4--) {
            iP = (iP << 1) + p(i2 + iP);
        }
        return iP - (1 << i3);
    }

    private static int e(int i2, int i3) {
        return p(i2) == 0 ? d(i2 + 2 + (i3 << 3), 3) : p(i2 + 1) == 0 ? 8 + d(i2 + 130 + (i3 << 3), 3) : 16 + d(i2 + 258, 8);
    }

    private static int f(int i2, int i3) {
        int i4 = 1;
        int i5 = 0;
        for (int i6 = 0; i6 < i3; i6++) {
            int iP = p(i2 + i4);
            i4 = (i4 << 1) + iP;
            i5 |= iP << i6;
        }
        return i5;
    }

    private static void c(int i2, int i3, int i4) {
        a.setColor((-16777216) | (i2 << 16) | (i3 << 8) | i4);
    }

    static final int a(Graphics graphics) {
        return graphics.getClipX();
    }

    static final int b(Graphics graphics) {
        return graphics.getClipY();
    }

    static final int c(Graphics graphics) {
        return graphics.getClipWidth();
    }

    static final int d(Graphics graphics) {
        return graphics.getClipHeight();
    }

    static final void a(Graphics graphics, int i2, int i3, int i4, int i5, boolean z2) {
        if (z2 && graphics != a) {
            z2 = false;
        }
        boolean z3 = z2;
        int i6 = e;
        int i7 = i5;
        int i8 = i4;
        int i9 = i3;
        int i10 = i2;
        if (z3) {
            i10 = (i6 - i9) - i7;
            i9 = i10;
            i8 = i7;
            i7 = i8;
        }
        graphics.setClip(i10, i9, i8, i7);
    }

    static final boolean e(Graphics graphics) {
        return graphics != null;
    }

    static final void a(Graphics graphics, int i2, int i3, int i4, int i5) {
        graphics.drawLine(e - i3, i2, e - i5, i4);
    }

    static final void b(Graphics graphics, int i2, int i3, int i4, int i5) {
        graphics.fillRect((e - i3) - i5, i2, i5, i4);
    }

    static final void c(Graphics graphics, int i2, int i3, int i4, int i5) {
        graphics.drawRect((e - i3) - i5, i2, i5, i4);
    }

    static final void a(Graphics graphics, int i2, int i3, int i4, int i5, int i6, int i7) {
        graphics.drawRoundRect((e - i3) - i5, i2, i5, i4, 10, 10);
    }

    static final void b(Graphics graphics, int i2, int i3, int i4, int i5, int i6, int i7) {
        graphics.fillRoundRect((e - i3) - i5, i2, i5, i4, 10, 10);
    }

    static final void c(Graphics graphics, int i2, int i3, int i4, int i5, int i6, int i7) {
        graphics.drawArc((e - i3) - 10, i2, 10, 10, 90, 450);
    }

    static final void a(int i2, int i3, int i4, int i5, int i6, int i7) {
        a.fillTriangle(e - i3, i2, e - i5, i4, e - i7, i6);
    }

    static final Image a(int[] iArr, int i2, int i3, boolean z2) {
        return Image.createRGBImage(iArr, i2, i3, z2);
    }

    static final void a(Graphics graphics, int[] iArr, int i2, int i3, int i4, int i5, int i6, int i7, boolean z2, boolean z3, int i8, int i9, boolean z4) {
        if (z4) {
            int i10 = i7;
            if ((i8 & 4) != 0) {
                i10 = i6;
                int i11 = i8 & (-5);
                int i12 = (i11 & 2) != 0 ? i11 & (-3) : i11 | 2;
                i8 = (i12 & 1) != 0 ? i12 & (-2) : i12 | 1;
            } else {
                i8 |= 4;
            }
            i4 = (e - i5) - i10;
            i5 = i4;
        }
        if (i8 != 0) {
            if ((i8 & 4) != 0) {
                i6 = i7;
                i7 = i6;
                i3 = i6;
            }
            iArr = b.a(iArr, i6, i7, i8);
        }
        graphics.drawRGB(iArr, i2, i3, i4, i5, i6, i7, z2);
    }

    static int a(byte[] bArr, int i2, byte b2) {
        int i3 = i2 + 1;
        bArr[i2] = b2;
        return i3;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v17, types: [java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r0v19, types: [int[]] */
    /* JADX WARN: Type inference failed for: r0v20, types: [java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r0v24, types: [java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r0v26, types: [short[]] */
    /* JADX WARN: Type inference failed for: r0v27, types: [java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r0v31 */
    /* JADX WARN: Type inference failed for: r0v35, types: [java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r0v37, types: [byte[]] */
    /* JADX WARN: Type inference failed for: r0v38, types: [java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r0v40, types: [int[]] */
    private static Object a(InputStream inputStream) {
        short[] sArr;
        short[] sArr2 = null;
        try {
            int iB = b(inputStream);
            int i2 = iB >> 4;
            int i3 = iB & 7;
            int iC = (iB & 8) != 0 ? c(inputStream) : b(inputStream);
            switch (i3) {
                case 0:
                    byte[] bArr = new byte[iC];
                    for (int i4 = 0; i4 < iC; i4++) {
                        bArr[i4] = (byte) b(inputStream);
                    }
                    sArr2 = bArr;
                    break;
                case 1:
                    short[] sArr3 = new short[iC];
                    if (i2 == 0) {
                        for (int i5 = 0; i5 < iC; i5++) {
                            sArr3[i5] = (byte) b(inputStream);
                        }
                    } else {
                        for (int i6 = 0; i6 < iC; i6++) {
                            sArr3[i6] = (short) c(inputStream);
                        }
                    }
                    sArr2 = sArr3;
                    break;
                case 2:
                    ?? r0 = new int[iC];
                    if (i2 == 0) {
                        for (int i7 = 0; i7 < iC; i7++) {
                            r0[i7] = (byte) b(inputStream);
                        }
                    } else if (i2 == 1) {
                        for (int i8 = 0; i8 < iC; i8++) {
                            r0[i8] = (short) c(inputStream);
                        }
                    } else {
                        for (int i9 = 0; i9 < iC; i9++) {
                            r0[i9] = d(inputStream);
                        }
                    }
                    sArr2 = r0;
                    break;
                default:
                    switch (i3 & 3) {
                        case 0:
                            if (i2 == 2) {
                                sArr = (Object[]) new byte[iC];
                                break;
                            } else {
                                sArr = new byte[iC][];
                                break;
                            }
                        case 1:
                            if (i2 == 2) {
                                sArr = (Object[]) new short[iC];
                                break;
                            } else {
                                sArr = new short[iC][];
                                break;
                            }
                        default:
                            if (i2 == 2) {
                                sArr = (Object[]) new int[iC];
                                break;
                            } else {
                                sArr = new int[iC][];
                                break;
                            }
                    }
                    for (int i10 = 0; i10 < iC; i10++) {
                        sArr[i10] = a(inputStream);
                    }
                    sArr2 = sArr;
                    break;
            }
        } catch (Exception unused) {
        }
        return sArr2;
    }

    private static int b(InputStream inputStream) throws IOException {
        int i2 = inputStream.read();
        if (i2 >= 0) {
            av++;
        }
        return i2;
    }

    private static int c(InputStream inputStream) {
        return (b(inputStream) & 255) | ((b(inputStream) & 255) << 8);
    }

    private static int d(InputStream inputStream) {
        return (b(inputStream) & 255) | ((b(inputStream) & 255) << 8) | ((b(inputStream) & 255) << 16) | ((b(inputStream) & 255) << 24);
    }

    private static int a(InputStream inputStream, byte[] bArr, int i2, int i3) throws IOException {
        int i4 = 0;
        int i5 = i3;
        while (i5 > 0) {
            try {
                int i6 = inputStream.read(bArr, i4, i5);
                i5 -= i6;
                i4 += i6;
            } catch (Exception unused) {
            }
        }
        av += i3;
        return i3;
    }

    private static int e(InputStream inputStream) {
        try {
            int iD = d(inputStream);
            aw = iD;
            az = new int[iD + 1];
            for (int i2 = 1; i2 < aw + 1; i2++) {
                az[i2] = d(inputStream);
            }
            ay = new byte[az[aw]];
            a(inputStream, ay, 0, ay.length);
        } catch (Exception unused) {
        }
        return ay.length + ((aw + 1) << 2);
    }

    static void a(String str, int i2) throws IOException {
        g();
        a(str);
        m(i2);
        if (al) {
            e(new ByteArrayInputStream(l(i2)));
        } else {
            e(aa);
        }
        e();
        p();
    }

    private static String b(byte[] bArr, int i2, int i3) {
        char[] cArr = new char[i3];
        int i4 = 0;
        int i5 = i2;
        int i6 = i2 + i3;
        while (i5 < i6) {
            if ((bArr[i5] & 128) == 0) {
                int i7 = i4;
                i4++;
                int i8 = i5;
                i5++;
                cArr[i7] = (char) bArr[i8];
            } else if ((bArr[i5] & 224) == 224 && i5 + 2 < i6 && (bArr[i5 + 1] & 192) == 128 && (bArr[i5 + 2] & 192) == 128) {
                int i9 = i4;
                i4++;
                cArr[i9] = (char) (((bArr[i5] & 15) << 12) | ((bArr[i5 + 1] & 63) << 6) | (bArr[i5 + 2] & 63));
                i5 += 3;
            } else {
                if ((bArr[i5] & 192) != 192 || i5 + 1 >= i6 || (bArr[i5 + 1] & 192) != 128) {
                    return "";
                }
                int i10 = i4;
                i4++;
                cArr[i10] = (char) (((bArr[i5] & 31) << 6) | (bArr[i5 + 1] & 63));
                i5 += 2;
            }
        }
        return new String(cArr, 0, i4);
    }

    static String g(int i2) {
        if (aA != null) {
            return aA[i2];
        }
        try {
            int i3 = az[i2 + 1] - az[i2];
            if (i3 == 0) {
                return null;
            }
            return !ax.equals("UTF-8") ? new String(ay, az[i2], i3, ax) : b(ay, az[i2], i3);
        } catch (Exception unused) {
            return null;
        }
    }

    static final int f() {
        return aw;
    }

    private static void p() {
        String[] strArr = new String[aw];
        for (int i2 = 0; i2 < aw; i2++) {
            strArr[i2] = g(i2);
        }
        aA = strArr;
        az = null;
        ay = null;
    }

    static void g() {
        if (aA != null) {
            for (int i2 = 0; i2 < aw; i2++) {
                aA[i2] = null;
            }
            aA = null;
        }
        az = null;
        ay = null;
        aw = 0;
    }

    static String c(int i2, int i3) {
        boolean z2 = false;
        if (i2 < 1000) {
            return new StringBuffer().append("").append(i2).toString();
        }
        String str = "";
        switch (z2) {
            case false:
            case true:
            case true:
            case true:
                str = ",";
                break;
            case true:
            case true:
                str = ".";
                break;
            case true:
            case true:
            case true:
            case true:
                if (i2 >= 10000) {
                    str = " ";
                    break;
                }
                break;
            case true:
            case true:
            case true:
                str = " ";
                break;
            case true:
            case true:
                if (i2 >= 10000) {
                    str = ".";
                    break;
                }
                break;
            default:
                return new StringBuffer().append("").append(i2).toString();
        }
        String string = "";
        int i4 = i2 % 1000 < 0 ? -(i2 % 1000) : i2 % 1000;
        int i5 = i2 / 1000;
        while (true) {
            if (i4 == 0 && i5 == 0) {
                return string;
            }
            if (i4 < 10) {
                string = new StringBuffer().append("00").append(i4 < 0 ? -i4 : i4).append(string).toString();
            } else if (i4 < 100) {
                string = new StringBuffer().append("0").append(i4 < 0 ? -i4 : i4).append(string).toString();
            } else {
                string = new StringBuffer().append(i4 < 0 ? -i4 : i4).append(string).toString();
            }
            i4 = i5 % 1000;
            int i6 = i5 / 1000;
            i5 = i6;
            if (i6 != 0) {
                string = new StringBuffer().append(str).append(string).toString();
            } else if (i4 != 0) {
                string = new StringBuffer().append(i4).append(str).append(string).toString();
                i4 = 0;
            }
        }
    }

    public static void h(int i2) {
        if (i2 != aB || aD == null) {
            aB = i2;
            int[] iArr = b.g;
            aC = iArr;
            if (iArr == null) {
                aC = new int[256];
            }
            int i3 = 256;
            while (i3 > 0) {
                i3--;
                aC[i3] = i2;
            }
            aD = a(aC, 16, 16, true);
        }
    }

    public static void d(Graphics graphics, int i2, int i3, int i4, int i5) {
        int i6 = (e - i3) - i5;
        int clipX = graphics.getClipX();
        int clipY = graphics.getClipY();
        int clipWidth = graphics.getClipWidth();
        int clipHeight = graphics.getClipHeight();
        int i7 = i6 > clipX ? i6 : clipX;
        int i8 = i2 > clipY ? i2 : clipY;
        int i9 = (i6 + i5 < clipX + clipWidth ? i6 + i5 : clipX + clipWidth) - i7;
        int i10 = (i2 + i4 < clipY + clipHeight ? i2 + i4 : clipY + clipHeight) - i8;
        if (i9 <= 0 || i10 <= 0) {
            return;
        }
        a(graphics, i7, i8, i9, i10, false);
        int i11 = i9 + i7;
        int i12 = i10 + i8;
        for (int i13 = i7; i13 < i11; i13 += 16) {
            for (int i14 = i8; i14 < i12; i14 += 16) {
                try {
                    graphics.drawImage(aD, i13, i14, 20);
                } catch (Exception unused) {
                }
            }
        }
        a(graphics, clipX, clipY, clipWidth, clipHeight, false);
    }

    /* JADX WARN: Type inference failed for: r0v7, types: [int[], int[][]] */
    static final void h() {
        q = 0;
        aM = 0;
        aI = 0;
        aJ = 0;
        aK = d;
        aL = e;
        r = new int[13];
        t = 0;
        aF = true;
    }

    static final void i(int i2) {
        t &= (1 << i2) ^ (-1);
    }

    static final boolean i() {
        return t != 0;
    }

    static final void a(int i2, boolean z2) {
        q |= 1 << i2;
        if (((1 << i2) & 639) != 0) {
            q &= -640;
        }
        if (((1 << i2) & 31) != 0) {
            t |= 1 << i2;
        }
    }

    static final void b(int i2, boolean z2) {
        q &= (1 << i2) ^ (-1);
        if (((1 << i2) & 31) != 0) {
            i(i2);
        }
    }

    static final void j(int i2) {
        aM += i2;
    }

    static final void a(int i2, int i3, int i4, int i5) {
    }

    static final void a(int i2, int i3, int i4) {
    }

    static void f(Graphics graphics) {
    }

    static final void a(Graphics graphics, int i2) {
    }

    public static void b(int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        int i9 = (i6 >> 16) & 255;
        int i10 = (i6 >> 8) & 255;
        int i11 = i6 & 255;
        int i12 = ((i7 >> 16) & 255) - i9;
        int i13 = ((i7 >> 8) & 255) - i10;
        int i14 = (i7 & 255) - i11;
        int i15 = (i2 + i4) - 1;
        int i16 = (i3 + i5) - 1;
        int i17 = i9 << 16;
        int i18 = i10 << 16;
        int i19 = i11 << 16;
        if (i8 == 4) {
            int i20 = i15 - i2;
            int i21 = (i12 << 16) / i20;
            int i22 = (i13 << 16) / i20;
            int i23 = (i14 << 16) / i20;
            for (int i24 = i15; i24 >= i2; i24--) {
                c(i17 >> 16, i18 >> 16, i19 >> 16);
                a(a, i24, i3, i24, i16);
                i17 += i21;
                i18 += i22;
                i19 += i23;
            }
            return;
        }
        if (i8 == 8) {
            int i25 = i15 - i2;
            int i26 = (i12 << 16) / i25;
            int i27 = (i13 << 16) / i25;
            int i28 = (i14 << 16) / i25;
            for (int i29 = i2; i29 <= i15; i29++) {
                c(i17 >> 16, i18 >> 16, i19 >> 16);
                a(a, i29, i3, i29, i16);
                i17 += i26;
                i18 += i27;
                i19 += i28;
            }
            return;
        }
        if (i8 == 16) {
            int i30 = i16 - i3;
            int i31 = (i12 << 16) / i30;
            int i32 = (i13 << 16) / i30;
            int i33 = (i14 << 16) / i30;
            for (int i34 = i16; i34 >= i3; i34--) {
                c(i17 >> 16, i18 >> 16, i19 >> 16);
                a(a, i2, i34, i15, i34);
                i17 += i31;
                i18 += i32;
                i19 += i33;
            }
            return;
        }
        if (i8 == 32) {
            int i35 = i16 - i3;
            int i36 = (i12 << 16) / i35;
            int i37 = (i13 << 16) / i35;
            int i38 = (i14 << 16) / i35;
            for (int i39 = i3; i39 <= i16; i39++) {
                c(i17 >> 16, i18 >> 16, i19 >> 16);
                a(a, i2, i39, i15, i39);
                i17 += i36;
                i18 += i37;
                i19 += i38;
            }
        }
    }
}
