package defpackage;

import java.io.ByteArrayInputStream;
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
    public static Graphics a;
    private static Graphics u;
    private static Graphics v;
    static boolean b;
    static int c;
    private static long w;
    private static long x;
    static int d;
    static int e;
    private static Display y;
    private static boolean z;
    private static MIDlet A;
    private static int B;
    private long C;
    private static int D;
    private static Image E;
    private static Graphics F;
    private static boolean G;
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
    private static int O;
    private static int P;
    private static Hashtable Q;
    private static Hashtable R;
    private static int S;
    static final int i = 0;
    static Random j;
    static int k;
    static int l;
    private static int[] T;
    private static int[] U;
    private static int[] V;
    static final int m = 0;
    static final int n = 0;
    private static int W;
    static final int o = 0;
    private static int X;
    private static int Y;
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
    private static int av;
    private static int aw;
    private static String ax;
    private static byte[] ay;
    private static int[] az;
    private static String[] aA;
    private static int aB;
    private static int[] aC;
    private static Image aD;
    private static boolean aE;
    static final boolean p = false;
    private static boolean aF;
    static int q;
    static int[][] r;
    private static Image aG;
    static Graphics s;
    static int t;
    private static int aH;
    private static int aI;
    private static int aJ;
    private static int aK;
    private static int aL;
    private static int aM;

    abstract void a();

    public j(Object r7, Object r8) {
        h = this;
        c = -1;
        z = true;
        A = (MIDlet) r7;
        y = (Display) r8;
        j();
        R = new Hashtable();
        Hashtable r0 = new Hashtable();
        Q = r0;
        r0.put(new Integer(48), new Integer(6));
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
        if (c < 0) goto L5;
        return;
    L5:
        d = 400;
        e = 240;
        a(System.currentTimeMillis());
        c = 0;
        new Thread(this).start();
    }

    protected void c() {
        if (b == true) goto L6;
        b = true;
        a.d();
        return;
    }

    protected void d() {
        if (b == false) goto L9;
        long r0 = System.currentTimeMillis();
        w = r0;
        H = r0;
        this.C = r0;
        b = false;
        j();
        if (z == true) goto L7;
        repaint();
    L7:
        k();
        return;
    }

    public void hideNotify() {
        c();
    }

    public void showNotify() {
        d();
    }

    public void sizeChanged(int r2, int r3) {
    }

    private void j() {
        setFullScreenMode(true);
        if (y != null) goto L5;
        return;
    L5:
        if (y.getCurrent() == this) goto L9;
        y.setCurrent(this);
        return;
    }

    @Override // java.lang.Runnable
    public void run() {
        j();     // Catch: Exception -> L10
        z = false;     // Catch: Exception -> L10
    L3:
        if (c < 0) goto L11;
        if (b == false) goto L7;
        this.C = Math.min(this.C, System.currentTimeMillis());     // Catch: Exception -> L10
        Thread.sleep(1);     // Catch: Exception -> L10
        goto L3
    L7:
        repaint();     // Catch: Exception -> L10
        serviceRepaints();     // Catch: Exception -> L10
        long r0 = System.currentTimeMillis();     // Catch: Exception -> L10
        this.C = Math.min(this.C, r0);     // Catch: Exception -> L10
        Thread.sleep(Math.max(1, B - (r0 - this.C)));     // Catch: Exception -> L10
        this.C = System.currentTimeMillis();     // Catch: Exception -> L10
    L11:
        an = null;
        A.notifyDestroyed();
        return;
    L10:
        c = -1;
        goto L11
    }

    public void paint(Graphics r6) {
        v = r6;
        long r0 = System.currentTimeMillis() - x;
        x = System.currentTimeMillis();
        if (r0 <= 3000) goto L8;
        if (x == 0) goto L8;
        c();
        d();
    L8:
        if (b == false) goto L10;
        return;
    L10:
        if (z == true) goto L31;
        z = true;
        J = M;
        K = L;
        M = 0;
        N = 0;
        if (P > 0) goto L15;
    L18:
        long r02 = System.currentTimeMillis();
        w = r02;
        int r03 = (int) (r02 - H);
        f = r03;
        if (r03 >= 0) goto L22;
        f = 0;
    L22:
        if (f <= 1000) goto L24;
        f = 1000;
    L24:
        H = w;
        I += f;
        g++;
        u = r6;     // Catch: Exception -> L26
        a = r6;     // Catch: Exception -> L26
        a();     // Catch: Exception -> L26
    L27:
        z = false;
        v = null;
        return;
    L26:
        c = -1;
        goto L27
    L15:
        if (P == Integer.MAX_VALUE) goto L17;
        P -= f;
    L17:
        k();
        goto L18
    }

    protected void keyPressed(int r4) {
        int r0 = 1 << k(r4);
        M |= r0;
        L |= r0;
    }

    protected void keyReleased(int r5) {
        int r0 = 1 << k(r5);
        N |= r0;
        L &= r0 ^ (-1);
    }

    private static byte k(int r4) {
        Integer r0 = new Integer(r4);
        if (Q != null) goto L6;
        return 0;
    L6:
        Integer r02 = (Integer) Q.get(r0);
        if (r02 != null) goto L9;
        Integer r03 = (Integer) R.get(r0);
        if (r03 != null) goto L13;
        return 0;
    L13:
        return r03.byteValue();
    L9:
        return r02.byteValue();
    }

    private static void k() {
        J = 0;
        K = 0;
        L = 0;
        M = 0;
        N = 0;
    }

    static void a(String r2, int r3, int r4) {
        a(r2);
        T = (int[]) f(0);
        U = (int[]) f(1);
        e();
    }

    static final int a(int r4) {
        return (r4 + (i >> 1)) >> 8;
    }

    static final void a(long r5) {
        if (j != null) goto L6;
        j = new Random(r5);
        return;
    L6:
        j.setSeed(r5);
    }

    static int a(int r5, int r6) {
        if (r6 == r5) goto L10;
        int r0 = j.nextInt();
        int r7 = r0;
        if (r0 >= 0) goto L8;
        r7 = -r7;
    L8:
        return r5 + (r7 % (r6 - r5));
    L10:
        return r6;
    }

    static int b(int r4) {
        if (r4 >= 0) goto L5;
        r4 = -r4;
    L5:
        int r0 = r4 & (X - 1);
        if (r0 > n) goto L10;
        return T[r0];
    L10:
        if (r0 >= W) goto L14;
        int r02 = W - r0;
        return -T[r02];
    L14:
        if (r0 > o) goto L17;
        int r03 = r0 - W;
        return -T[r03];
    L17:
        int r04 = X - r0;
        return T[r04];
    }

    static int c(int r3) {
        int r0 = b(r3);
        if (r0 != 0) goto L7;
        return Integer.MAX_VALUE;
    L7:
        return (b(n - r3) << 8) / r0;
    }

    private static int b(int r3, int r4, int r5) {
    L3:
        if ((r3 + 1) >= r4) goto L9;
        int r0 = (r3 + r4) >> 1;
        if (r5 > c(r0)) goto L6;
        r4 = r0;
        goto L3
    L6:
        r3 = r0;
        goto L3
    L9:
        if (r5 < c(r4)) goto L13;
        return r4;
    L13:
        return r3;
    }

    static int b(int r7, int r8) {
        if (V != null) goto L34;
        V = new int[i + 1];
        int r9 = 0;
    L6:
        if (r9 >= (i + 1)) goto L34;
        int[] r0 = V;
        int r1 = r9;
        int r2 = i;
        int r3 = r9;
        if (r2 <= 0) goto L17;
        if (r3 <= 0) goto L13;
        int r22 = b(0, n, (r3 * i) / r2);
    L32:
        r0[r1] = r22;
        r9 = r9 + 1;
        goto L6
    L13:
        if (r3 != 0) goto L15;
        r22 = 0;
        goto L32
    L15:
        r22 = b(o, X, (r3 * i) / r2);
        goto L32
    L17:
        if (r2 != 0) goto L26;
        if (r3 <= 0) goto L22;
        r22 = n;
        goto L32
    L22:
        if (r3 != 0) goto L24;
        r22 = 0;
        goto L32
    L24:
        r22 = o;
        goto L32
    L26:
        if (r3 <= 0) goto L29;
        r22 = b(n, W, (r3 * i) / r2);
        goto L32
    L29:
        if (r3 != 0) goto L31;
        r22 = W;
        goto L32
    L31:
        r22 = b(W, o, (r3 * i) / r2);
    L34:
        if (r7 != 0) goto L46;
        if (r8 <= 0) goto L40;
        return n;
    L40:
        if (r8 != 0) goto L44;
        return 0;
    L44:
        return o;
    L46:
        if (r7 > 0) goto L48;
        int r02 = -r7;
        if (r8 >= 0) goto L64;
        int r03 = -r8;
        if (r02 < r03) goto L74;
        return W + V[(r03 * i) / r02];
    L74:
        return o - V[(r02 * i) / r03];
    L64:
        if (r02 < r8) goto L68;
        return W - V[(r8 * i) / r02];
    L68:
        return n + V[(r02 * i) / r8];
    L48:
        if (r8 >= 0) goto L50;
        int r04 = -r8;
        if (r7 < r04) goto L60;
        return X - V[(r04 * i) / r7];
    L60:
        return o + V[(r7 * i) / r04];
    L50:
        if (r7 < r8) goto L54;
        return V[(r8 * i) / r7];
    L54:
        return n - V[(r7 * i) / r8];
    }

    static int d(int r4) {
        if (r4 < 65536) goto L35;
        if (r4 < 16777216) goto L21;
        if (r4 < 268435456) goto L15;
        if (r4 < 1073741824) goto L13;
        return U[r4 >> 24] << 8;
    L13:
        return U[r4 >> 22] << 7;
    L15:
        if (r4 < 67108864) goto L19;
        return U[r4 >> 20] << 6;
    L19:
        return U[r4 >> 18] << 5;
    L21:
        if (r4 < 1048576) goto L29;
        if (r4 < 4194304) goto L27;
        return U[r4 >> 16] << 4;
    L27:
        return U[r4 >> 14] << 3;
    L29:
        if (r4 < 262144) goto L33;
        return U[r4 >> 12] << 2;
    L33:
        return U[r4 >> 10] << 1;
    L35:
        if (r4 < 256) goto L51;
        if (r4 < 4096) goto L45;
        if (r4 < 16384) goto L43;
        return U[r4 >> 8];
    L43:
        return U[r4 >> 6] >> 1;
    L45:
        if (r4 < 1024) goto L49;
        return U[r4 >> 4] >> 2;
    L49:
        return U[r4 >> 2] >> 3;
    L51:
        if (r4 >= 0) goto L53;
        return 0;
    L53:
        return U[r4] >> 4;
    }

    private static final int b(int r4, int r5, int r6, int r7, int r8, int r9) {
        return (((r4 * r8) + ((r5 * 2) * r7)) + (r6 * r9)) / (1 << 16);
    }

    static void a(int r7, int r8, int r9, int r10, int r11, int r12, int r13) {
        int r0 = r13 * r13;
        int r02 = i - r13;
        int r03 = r02 * r02;
        int r04 = r02 * r13;
        k = b(r7, r9, r11, r04, r03, r0);
        l = b(r8, r10, r12, r04, r03, r0);
    }

    private static byte[] l(int r8) {
        int r0 = m(r8);
        byte[] r9 = null;
        if (al == true) goto L107;
        byte[] r02 = new byte[r0];
        r9 = r02;
        a(r02, 0, r9.length);
    L106:
        return r9;
    L107:
        byte[] r03 = new byte[r0 + 1];     // Catch: Exception -> L103
        a(aa, r03, 0, r0);     // Catch: Exception -> L103
        int r10 = 0;
        int r11 = 0;
    L6:
        if (r11 >= 4) goto L8;
        r10 = r10 + ((r03[r11 + 5] & 255) << (r11 << 3));     // Catch: Exception -> L103
        r11 = r11 + 1;     // Catch: Exception -> L103
        goto L6
    L8:
        int r04 = r03[0] & 255;     // Catch: Exception -> L103
        int r05 = r04 / 45;     // Catch: Exception -> L103
        int r06 = r04 % 45;     // Catch: Exception -> L103
        int r07 = r06 / 9;     // Catch: Exception -> L103
        int r08 = r06 % 9;     // Catch: Exception -> L103
        int r09 = 1846 + (768 << (r08 + r07));     // Catch: Exception -> L103
        as = new byte[r10];     // Catch: Exception -> L103
        au = new short[r09];     // Catch: Exception -> L103
        int r010 = r09 << 1;     // Catch: Exception -> L103
        int r5 = r10;
        int r011 = 1846 + (768 << (r08 + r07));     // Catch: Exception -> L103
        short[] r012 = au;     // Catch: Exception -> L103
        int r18 = 0;
        boolean r19 = false;
        int r20 = 0;
        int r21 = 1;
        int r22 = 1;
        int r23 = 1;
        int r24 = 1;
        int r25 = 0;
        int r013 = (1 << r05) - 1;     // Catch: Exception -> L103
        int r014 = (1 << r07) - 1;     // Catch: Exception -> L103
        if (r010 < (r011 << 1)) goto L102;
        int r102 = 0;
    L12:
        if (r102 >= r011) goto L14;
        r012[r102] = 1024;     // Catch: Exception -> L103
        r102 = r102 + 1;     // Catch: Exception -> L103
        goto L12
    L14:
        int r1 = r03.length;     // Catch: Exception -> L103
        ao = r03;     // Catch: Exception -> L103
        ap = r1;     // Catch: Exception -> L103
        at = 13;     // Catch: Exception -> L103
        ar = 0;     // Catch: Exception -> L103
        aq = 4294967295L;     // Catch: Exception -> L103
        r03[ap - 1] = -1;     // Catch: Exception -> L103
        int r44 = 0;
    L16:
        if (r44 >= 5) goto L19;
        long r015 = ar << 8;     // Catch: Exception -> L103
        byte[] r12 = ao;     // Catch: Exception -> L103
        at = at + 1;     // Catch: Exception -> L103
        ar = r015 | (r12[r2] & 255);     // Catch: Exception -> L103
        r44 = r44 + 1;     // Catch: Exception -> L103
    L19:
        if (r25 >= r5) goto L102;
        int r016 = r25 & r013;     // Catch: Exception -> L103
        if (p((0 + (r18 << 4)) + r016) == 0) goto L22;
        r19 = true;
        if (p(r18 + 192) != 1) goto L70;
        if (p(r18 + 204) != 0) goto L57;
        if (p((240 + (r18 << 4)) + r016) == 0) goto L52;
    L65:
        int r103 = e(1332, r016);     // Catch: Exception -> L103
        if (r18 >= 7) goto L68;
        int r017 = 8;
    L69:
        r18 = r017;
    L96:
        int r104 = r103 + 2;     // Catch: Exception -> L103
    L97:
        r20 = as[r25 - r21] & 255;     // Catch: Exception -> L103
        int r13 = r25;
        r25 = r25 + 1;     // Catch: Exception -> L103
        as[r13] = (byte) r20;     // Catch: Exception -> L103
        r104 = r104 - 1;
        if (r104 <= 0) goto L19;
        if (r25 < r5) goto L97;
    L68:
        r017 = 11;
        goto L69
    L52:
        if (r18 >= 7) goto L54;
        int r018 = 9;
    L55:
        r18 = r018;
        r20 = as[r25 - r21] & 255;     // Catch: Exception -> L103
        int r14 = r25;
        r25 = r25 + 1;     // Catch: Exception -> L103
        as[r14] = (byte) r20;     // Catch: Exception -> L103
        goto L19
    L54:
        r018 = 11;
        goto L55
    L57:
        if (p(r18 + 216) != 0) goto L60;
        int r142 = r22;
    L64:
        r22 = r21;
        r21 = r142;
        goto L65
    L60:
        if (p(r18 + 228) != 0) goto L62;
        r142 = r23;
    L63:
        r23 = r22;
        goto L64
    L62:
        r142 = r24;
        r24 = r23;
        goto L63
    L70:
        r24 = r23;
        r23 = r22;
        r22 = r21;
        if (r18 >= 7) goto L73;
        int r019 = 7;
    L74:
        r18 = r019;
        r103 = e(818, r016);     // Catch: Exception -> L103
        if (r103 >= 4) goto L77;
        int r15 = r103;
    L78:
        int r020 = d(432 + (r15 << 6), 6);     // Catch: Exception -> L103
        if (r020 < 4) goto L94;
        int r021 = (r020 >> 1) - 1;     // Catch: Exception -> L103
        int r022 = (2 | (r020 & 1)) << r021;     // Catch: Exception -> L103
        if (r020 >= 14) goto L83;
        int r212 = r022 + f(((r022 + 688) - r020) - 1, r021);     // Catch: Exception -> L103
    L95:
        r21 = r212 + 1;     // Catch: Exception -> L103
        goto L96
    L83:
        long r43 = aq;     // Catch: Exception -> L103
        long r45 = ar;     // Catch: Exception -> L103
        int r16 = 0;
        int r143 = r021 - 4;     // Catch: Exception -> L103
    L85:
        if (r143 <= 0) goto L93;
        r43 = r43 >> 1;     // Catch: Exception -> L103
        r16 = r16 << 1;     // Catch: Exception -> L103
        if (r45 < r43) goto L90;
        r45 = r45 - r43;     // Catch: Exception -> L103
        r16 = r16 | 1;     // Catch: Exception -> L103
    L90:
        if (r43 >= 16777216) goto L92;
        r43 = r43 << 8;     // Catch: Exception -> L103
        byte[] r2 = ao;     // Catch: Exception -> L103
        at = at + 1;     // Catch: Exception -> L103
        r45 = (r45 << 8) | (r2[r3] & 255);     // Catch: Exception -> L103
    L92:
        r143 = r143 - 1;
        goto L85
    L93:
        aq = r43;     // Catch: Exception -> L103
        ar = r45;     // Catch: Exception -> L103
        r212 = (r022 + (r16 << 4)) + f(802, 4);     // Catch: Exception -> L103
        goto L95
    L94:
        r212 = r020;
        goto L95
    L77:
        r15 = 3;
        goto L78
    L73:
        r019 = 10;
        goto L74
    L22:
        int r023 = 1846 + (768 * (((r25 & r014) << r08) + ((r20 & 255) >> (8 - r08))));     // Catch: Exception -> L103
        if (r18 >= 4) goto L26;
        r18 = 0;
    L30:
        if (r19 == false) goto L40;
        byte r432 = as[r25 - r21];     // Catch: Exception -> L103
        int r442 = 1;
    L32:
        int r024 = (r432 >> 7) & 1;     // Catch: Exception -> L103
        r432 = (byte) (r432 << 1);     // Catch: Exception -> L103
        int r025 = p((r023 + ((r024 + 1) << 8)) + r442);     // Catch: Exception -> L103
        r442 = (r442 << 1) | r025;     // Catch: Exception -> L103
        if (r024 != r025) goto L35;
        if (r442 < 256) goto L32;
    L39:
        r20 = r442 & 255;     // Catch: Exception -> L103
        r19 = false;
    L44:
        int r17 = r25;
        r25 = r25 + 1;     // Catch: Exception -> L103
        as[r17] = (byte) r20;     // Catch: Exception -> L103
    L35:
        if (r442 >= 256) goto L39;
        r442 = (r442 << 1) | p(r023 + r442);     // Catch: Exception -> L103
        goto L35
    L40:
        int r433 = 1;
    L41:
        int r026 = (r433 << 1) | p(r023 + r433);     // Catch: Exception -> L103
        r433 = r026;
        if (r026 < 256) goto L41;
        r20 = r433 & 255;     // Catch: Exception -> L103
        goto L44
    L26:
        if (r18 >= 10) goto L28;
        r18 = r18 - 3;
        goto L30
    L28:
        r18 = r18 - 6;
    L102:
        au = null;     // Catch: Exception -> L103
        ao = null;     // Catch: Exception -> L103
        ae += r0;
        r9 = as;     // Catch: Exception -> L103
        as = null;     // Catch: Exception -> L103
        goto L106
    }

    private static void l() {
        if (aj != (ah - 1)) goto L5;
        int r7 = af - ai[aj];
    L6:
        ag = new int[r7 + 1];
        int r8 = 0;
    L8:
        if (r8 >= (r7 + 1)) goto L10;
        ag[r8] = ((n() & 255) | ((n() & 255) << 8)) | (((n() & 255) << 16) | ((n() & 255) << 24));
        r8 = r8 + 1;
        goto L8
    L10:
        return;
    L5:
        r7 = ai[aj + 1] - ai[aj];
        goto L6
    }

    /* JADX WARN: Multi-variable type inference failed */
    static final void a(String r4) {
        ab = 1;
        if (Z != null) goto L5;
    L8:
        e();
        Z = r4;
        ac = null;
        ad = 0;
        aa = b(Z);
        af = (short) o();
        int r0 = (short) o();
        ah = r0;
        ai = new short[r0];
        int r42 = 0;
    L10:
        if (r42 >= ah) goto L12;
        ai[r42] = (short) o();
        r42 = r42 + 1;
        goto L10
    L12:
        aj = 0;
        l();
        return;
    L5:
        if (r4 == null) goto L8;
        if (r4.compareTo(Z) != 0) goto L8;
    }

    private static InputStream b(String r7) {
        InputStream r8 = null;
        if (ab != 3) goto L6;
        r8 = new ByteArrayInputStream(ac, ad, ac.length - ad);
    L11:
        return r8;
    L6:
        if (ab == 2) goto L11;
        if (ab != 1) goto L11;
        r8 = "".getClass().getResourceAsStream(r7);
        goto L11
    }

    static final void e() {
        m();
        if (ab != 3) goto L6;
        ac = null;
        return;
    }

    private static final void m() {
        if (aa != null) goto L9;
    L7:
        ae = 0;
        return;
    L9:
        aa.close();     // Catch: Exception -> L5
    L6:
        aa = null;
        goto L7
    }

    private static int m(int r4) {
        int r5 = ah - 1;
    L4:
        if (r5 < 0) goto L9;
        if (ai[r5] <= r4) goto L9;
        r5 = r5 - 1;
    L9:
        if (aj == r5) goto L15;
        aj = r5;
        e();
        if (aj != 0) goto L13;
        String r0 = Z;
        Z = null;
        a(r0);
    L20:
        int r02 = r4 - ai[aj];
        int r03 = ag[r02];
        int r42 = ag[r02 + 1] - ag[r02];
        int r52 = r03;
        if (ae != r52) goto L23;
    L30:
        al = false;
        if (r42 <= 0) goto L34;
        n(n() & 255);
        r42 = r42 - 1;
    L34:
        return r42;
    L23:
        if (ae <= r52) goto L28;
        m();
        if (aj != 0) goto L27;
        aa = b(Z);
    L29:
        o(r52);
        goto L30
    L27:
        aa = b(new StringBuffer().append(Z).append(".").append(aj).toString());
        goto L29
    L28:
        r52 = r52 - ae;
        goto L29
    L13:
        aa = b(new StringBuffer().append(Z).append(".").append(aj).toString());
        l();
        goto L20
    L15:
        if (aa != null) goto L20;
        if (aj != 0) goto L19;
        String r04 = Z;
        Z = null;
        a(r04);
        goto L20
    L19:
        aa = b(new StringBuffer().append(Z).append(".").append(aj).toString());
        goto L20
    }

    private static void n(int r3) {
        ak = r3;
        if (r3 < 127) goto L6;
        ak -= 127;
        al = true;
        return;
    }

    static final byte[] e(int r2) {
        return l(r2);
    }

    private static void o(int r6) {
        if (r6 != 0) goto L6;
        return;
    L6:
        if (ab != 3) goto L15;
        ae += r6;
    L9:
        if (r6 <= 0) goto L11;
        r6 = (int) (r6 - aa.skip(r6));     // Catch: Exception -> L12
    L13:
        return;
    L11:
        return;
    L15:
        if (am != null) goto L18;
        am = new byte[256];
    L18:
        if (r6 <= 256) goto L21;
        a(am, 0, 256);
        r6 = r6 - 256;
        goto L18
    L21:
        if (r6 <= 0) goto L30;
        a(am, 0, r6);
        return;
    }

    private static int n() {
        int r3 = 0;
        r3 = aa.read();     // Catch: Exception -> L4
    L5:
        ae++;
        return r3;
    }

    private static int o() {
        return (n() & 255) | ((n() & 255) << 8);
    }

    private static int a(byte[] r5, int r6, int r7) {
        int r62 = 0;
        int r8 = r7;
    L4:
        if (r8 <= 0) goto L8;
        int r0 = aa.read(r5, r62, r8);     // Catch: Exception -> L7
        r8 = r8 - r0;     // Catch: Exception -> L7
        r62 = r62 + r0;     // Catch: Exception -> L7
    L8:
        ae += r7;
        return r7;
    }

    static final Object f(int r4) {
        m(r4);
        av = 0;
        if (al == false) goto L5;
        Object r42 = a(new ByteArrayInputStream(l(r4)));
    L7:
        return r42;
    L5:
        r42 = a(aa);
        ae += av;
        goto L7
    }

    private static int p(int r7) {
        long r0 = (aq >> 11) * au[r7];
        if (ar >= r0) goto L9;
        aq = r0;
        short[] r02 = au;
        r02[r7] = (short) (r02[r7] + ((2048 - au[r7]) >> 5));
        if (aq >= 16777216) goto L14;
        long r03 = ar << 8;
        byte[] r1 = ao;
        at = at + 1;
        ar = r03 | (r1[r2] & 255);
        aq <<= 8;
        return 0;
    L14:
        return 0;
    L9:
        aq -= r0;
        ar -= r0;
        short[] r04 = au;
        r04[r7] = (short) (r04[r7] - (au[r7] >> 5));
        if (aq >= 16777216) goto L15;
        long r05 = ar << 8;
        byte[] r12 = ao;
        at = at + 1;
        ar = r05 | (r12[r2] & 255);
        aq <<= 8;
        return 1;
    L15:
        return 1;
    }

    private static int d(int r4, int r5) {
        int r6 = 1;
        int r7 = r5;
    L4:
        if (r7 <= 0) goto L7;
        r6 = (r6 << 1) + p(r4 + r6);
        r7 = r7 - 1;
        goto L4
    L7:
        return r6 - (1 << r5);
    }

    private static int e(int r5, int r6) {
        if (p(r5) != 0) goto L7;
        return d((r5 + 2) + (r6 << 3), 3);
    L7:
        if (p(r5 + 1) != 0) goto L11;
        return 8 + d((r5 + 130) + (r6 << 3), 3);
    L11:
        return 16 + d(r5 + 258, 8);
    }

    private static int f(int r4, int r5) {
        int r6 = 1;
        int r7 = 0;
        int r9 = 0;
    L4:
        if (r9 >= r5) goto L7;
        int r0 = p(r4 + r6);
        r6 = (r6 << 1) + r0;
        r7 = r7 | (r0 << r9);
        r9 = r9 + 1;
        goto L4
    L7:
        return r7;
    }

    private static void c(int r5, int r6, int r7) {
        a.setColor((((-16777216) | (r5 << 16)) | (r6 << 8)) | r7);
    }

    static final int a(Graphics r2) {
        return r2.getClipX();
    }

    static final int b(Graphics r2) {
        return r2.getClipY();
    }

    static final int c(Graphics r2) {
        return r2.getClipWidth();
    }

    static final int d(Graphics r2) {
        return r2.getClipHeight();
    }

    static final void a(Graphics r8, int r9, int r10, int r11, int r12, boolean r13) {
        if (r13 == true) goto L5;
    L7:
        boolean r5 = r13;
        int r6 = e;
        int r122 = r12;
        int r112 = r11;
        int r102 = r10;
        int r92 = r9;
        if (r5 == false) goto L10;
        r92 = (r6 - r102) - r122;
        r102 = r92;
        r112 = r122;
        r122 = r112;
    L10:
        r8.setClip(r92, r102, r112, r122);
        return;
    L5:
        if (r8 == a) goto L7;
        r13 = false;
        goto L7
    }

    static final boolean e(Graphics r2) {
        if (r2 == null) goto L6;
        return true;
    L6:
        return false;
    }

    static final void a(Graphics r6, int r7, int r8, int r9, int r10) {
        r6.drawLine(e - r8, r7, e - r10, r9);
    }

    static final void b(Graphics r6, int r7, int r8, int r9, int r10) {
        r6.fillRect((e - r8) - r10, r7, r10, r9);
    }

    static final void c(Graphics r6, int r7, int r8, int r9, int r10) {
        r6.drawRect((e - r8) - r10, r7, r10, r9);
    }

    static final void a(Graphics r8, int r9, int r10, int r11, int r12, int r13, int r14) {
        r8.drawRoundRect((e - r10) - r12, r9, r12, r11, 10, 10);
    }

    static final void b(Graphics r8, int r9, int r10, int r11, int r12, int r13, int r14) {
        r8.fillRoundRect((e - r10) - r12, r9, r12, r11, 10, 10);
    }

    static final void c(Graphics r8, int r9, int r10, int r11, int r12, int r13, int r14) {
        r8.drawArc((e - r10) - 10, r9, 10, 10, 90, 450);
    }

    static final void a(int r8, int r9, int r10, int r11, int r12, int r13) {
        int r0 = e - r9;
        int r02 = e - r11;
        int r03 = e - r13;
        a.fillTriangle(r0, r8, r02, r10, r03, r12);
    }

    static final Image a(int[] r5, int r6, int r7, boolean r8) {
        return Image.createRGBImage(r5, r6, r7, r8);
    }

    static final void a(Graphics r10, int[] r11, int r12, int r13, int r14, int r15, int r16, int r17, boolean r18, boolean r19, int r20, int r21, boolean r22) {
        if (r22 == false) goto L17;
        int r192 = r17;
        if ((r20 & 4) == 0) goto L14;
        r192 = r16;
        int r0 = r20 & (-5);
        if ((r0 & 2) == 0) goto L9;
        int r202 = r0 & (-3);
    L11:
        if ((r202 & 1) == 0) goto L13;
        r20 = r202 & (-2);
    L15:
        r14 = (e - r15) - r192;
        r15 = r14;
        goto L17
    L13:
        r20 = r202 | 1;
        goto L15
    L9:
        r202 = r0 | 2;
        goto L11
    L14:
        r20 = r20 | 4;
    L17:
        if (r20 != 0) goto L19;
    L22:
        r10.drawRGB(r11, r12, r13, r14, r15, r16, r17, r18);
        return;
    L19:
        if ((r20 & 4) == 0) goto L21;
        r16 = r17;
        r17 = r16;
        r13 = r16;
    L21:
        r11 = b.a(r11, r16, r17, r20);
        goto L22
    }

    static int a(byte[] r4, int r5, byte r6) {
        int r52 = r5 + 1;
        r4[r5] = r6;
        return r52;
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
    private static Object a(InputStream r4) {
        short[] r5 = null;
        int r0 = b(r4);     // Catch: Exception -> L61
        int r02 = r0 >> 4;     // Catch: Exception -> L61
        int r03 = r0 & 7;     // Catch: Exception -> L61
        if ((r0 & 8) == 0) goto L6;
        int r6 = c(r4);     // Catch: Exception -> L61
    L8:
        switch(r03) {
            case 0: goto L9;
            case 1: goto L14;
            case 2: goto L25;
            default: goto L43;
        };     // Catch: Exception -> L61
    L9:
        byte[] r04 = new byte[r6];     // Catch: Exception -> L61
        int r7 = 0;
    L11:
        if (r7 >= r6) goto L13;
        r04[r7] = (byte) b(r4);     // Catch: Exception -> L61
        r7 = r7 + 1;     // Catch: Exception -> L61
        goto L11
    L13:
        r5 = r04;
        goto L63
    L14:
        short[] r05 = new short[r6];     // Catch: Exception -> L61
        if (r02 != 0) goto L20;
        int r72 = 0;
    L18:
        if (r72 >= r6) goto L24;
        r05[r72] = (byte) b(r4);     // Catch: Exception -> L61
        r72 = r72 + 1;     // Catch: Exception -> L61
    L24:
        r5 = r05;
        goto L63
    L20:
        int r73 = 0;
    L22:
        if (r73 >= r6) goto L24;
        r05[r73] = (short) c(r4);     // Catch: Exception -> L61
        r73 = r73 + 1;     // Catch: Exception -> L61
        goto L22
    L25:
        ?? r06 = new int[r6];     // Catch: Exception -> L61
        if (r02 != 0) goto L32;
        int r74 = 0;
    L29:
        if (r74 >= r6) goto L41;
        r06[r74] = (byte) b(r4);     // Catch: Exception -> L61
        r74 = r74 + 1;     // Catch: Exception -> L61
    L41:
        r5 = r06;
        goto L63
    L32:
        if (r02 != 1) goto L37;
        int r75 = 0;
    L35:
        if (r75 >= r6) goto L41;
        r06[r75] = (short) c(r4);     // Catch: Exception -> L61
        r75 = r75 + 1;     // Catch: Exception -> L61
        goto L35
    L37:
        int r76 = 0;
    L39:
        if (r76 >= r6) goto L41;
        r06[r76] = d(r4);     // Catch: Exception -> L61
        r76 = r76 + 1;     // Catch: Exception -> L61
        goto L39
    L43:
        switch((r03 & 3)) {
            case 0: goto L45;
            case 1: goto L49;
            default: goto L53;
        };     // Catch: Exception -> L61
    L45:
        if (r02 != 2) goto L47;
        short[] r8 = (Object[]) new byte[r6];     // Catch: Exception -> L61
    L56:
        int r77 = 0;
    L58:
        if (r77 >= r6) goto L60;
        r8[r77] = a(r4);     // Catch: Exception -> L61
        r77 = r77 + 1;     // Catch: Exception -> L61
        goto L58
    L60:
        r5 = r8;
        goto L63
    L47:
        r8 = new byte[r6][];     // Catch: Exception -> L61
        goto L56
    L49:
        if (r02 != 2) goto L51;
        r8 = (Object[]) new short[r6];     // Catch: Exception -> L61
        goto L56
    L51:
        r8 = new short[r6][];     // Catch: Exception -> L61
        goto L56
    L53:
        if (r02 != 2) goto L55;
        r8 = (Object[]) new int[r6];     // Catch: Exception -> L61
        goto L56
    L55:
        r8 = new int[r6][];     // Catch: Exception -> L61
        goto L56
    L6:
        r6 = b(r4);     // Catch: Exception -> L61
    L63:
        return r5;
    }

    private static int b(InputStream r3) {
        int r0 = r3.read();
        if (r0 < 0) goto L6;
        av++;
    L6:
        return r0;
    }

    private static int c(InputStream r4) {
        return (b(r4) & 255) | ((b(r4) & 255) << 8);
    }

    private static int d(InputStream r5) {
        return ((b(r5) & 255) | ((b(r5) & 255) << 8)) | (((b(r5) & 255) << 16) | ((b(r5) & 255) << 24));
    }

    private static int a(InputStream r5, byte[] r6, int r7, int r8) {
        int r72 = 0;
        int r9 = r8;
    L4:
        if (r9 <= 0) goto L8;
        int r0 = r5.read(r6, r72, r9);     // Catch: Exception -> L7
        r9 = r9 - r0;     // Catch: Exception -> L7
        r72 = r72 + r0;     // Catch: Exception -> L7
    L8:
        av += r8;
        return r8;
    }

    private static int e(InputStream r5) {
        int r0 = d(r5);     // Catch: Exception -> L7
        aw = r0;     // Catch: Exception -> L7
        az = new int[r0 + 1];     // Catch: Exception -> L7
        int r6 = 1;
    L4:
        if (r6 >= (aw + 1)) goto L6;
        az[r6] = d(r5);     // Catch: Exception -> L7
        r6 = r6 + 1;     // Catch: Exception -> L7
        goto L4
    L6:
        ay = new byte[az[aw]];     // Catch: Exception -> L7
        a(r5, ay, 0, ay.length);     // Catch: Exception -> L7
    L9:
        return ay.length + ((aw + 1) << 2);
    }

    static void a(String r4, int r5) {
        g();
        a(r4);
        m(r5);
        if (al == false) goto L5;
        e(new ByteArrayInputStream(l(r5)));
    L6:
        e();
        p();
        return;
    L5:
        e(aa);
        goto L6
    }

    private static String b(byte[] r7, int r8, int r9) {
        char[] r0 = new char[r9];
        int r11 = 0;
        int r12 = r8;
        int r02 = r8 + r9;
    L4:
        if (r12 >= r02) goto L27;
        if ((r7[r12] & 128) == 0) goto L7;
        if ((r7[r12] & 224) != 224) goto L18;
        if ((r12 + 2) >= r02) goto L18;
        if ((r7[r12 + 1] & 192) != 128) goto L18;
        if ((r7[r12 + 2] & 192) != 128) goto L18;
        int r1 = r11;
        r11 = r11 + 1;
        r0[r1] = (char) ((((r7[r12] & 15) << 12) | ((r7[r12 + 1] & 63) << 6)) | (r7[r12 + 2] & 63));
        r12 = r12 + 3;
    L18:
        if ((r7[r12] & 192) != 192) goto L24;
        if ((r12 + 1) >= r02) goto L43;
        if ((r7[r12 + 1] & 192) != 128) goto L44;
        int r13 = r11;
        r11 = r11 + 1;
        r0[r13] = (char) (((r7[r12] & 31) << 6) | (r7[r12 + 1] & 63));
        r12 = r12 + 2;
        goto L4
    L44:
        return "";
    L43:
        return "";
    L24:
        return "";
    L7:
        int r14 = r11;
        r11 = r11 + 1;
        int r3 = r12;
        r12 = r12 + 1;
        r0[r14] = (char) r7[r3];
        goto L4
    L27:
        return new String(r0, 0, r11);
    }

    static String g(int r7) {
        if (aA != null) goto L5;
        int r0 = az[r7 + 1] - az[r7];     // Catch: Exception -> L16
        if (r0 != 0) goto L11;
        return null;
    L11:
        if (ax.equals("UTF-8") == true) goto L15;
        return new String(ay, az[r7], r0, ax);
    L15:
        return b(ay, az[r7], r0);
    L17:
        return null;
    L5:
        return aA[r7];
    }

    static final int f() {
        return aw;
    }

    private static void p() {
        String[] r0 = new String[aw];
        int r5 = 0;
    L4:
        if (r5 >= aw) goto L6;
        r0[r5] = g(r5);
        r5 = r5 + 1;
        goto L4
    L6:
        aA = r0;
        az = null;
        ay = null;
    }

    static void g() {
        if (aA == null) goto L9;
        int r4 = 0;
    L6:
        if (r4 >= aw) goto L8;
        aA[r4] = null;
        r4 = r4 + 1;
        goto L6
    L8:
        aA = null;
    L9:
        az = null;
        ay = null;
        aw = 0;
    }

    static String c(int r4, int r5) {
        boolean r1 = false;
        if (r4 < 1000) goto L5;
        String r7 = "";
        switch(r1) {
            case 0: goto L8;
            case 1: goto L9;
            case 2: goto L12;
            case 3: goto L10;
            case 4: goto L12;
            case 5: goto L9;
            case 6: goto L12;
            case 7: goto L8;
            case 8: goto L8;
            case 9: goto L8;
            case 10: goto L12;
            case 11: goto L15;
            case 12: goto L10;
            case 13: goto L10;
            case 14: goto L15;
            default: goto L18;
        };
    L8:
        r7 = ",";
    L19:
        String r52 = "";
        if ((r4 % 1000) >= 0) goto L22;
        int r0 = -(r4 % 1000);
    L23:
        int r6 = r0;
        int r42 = r4 / 1000;
    L25:
        if (r6 != 0) goto L29;
        if (r42 != 0) goto L29;
        return r52;
    L29:
        if (r6 >= 10) goto L36;
        StringBuffer r02 = new StringBuffer().append("00");
        if (r6 >= 0) goto L33;
        int r12 = -r6;
    L34:
        r52 = r02.append(r12).append(r52).toString();
    L47:
        r6 = r42 % 1000;
        int r03 = r42 / 1000;
        r42 = r03;
        if (r03 != 0) goto L49;
        if (r6 == 0) goto L25;
        r52 = new StringBuffer().append(r6).append(r7).append(r52).toString();
        r6 = 0;
        goto L25
    L49:
        r52 = new StringBuffer().append(r7).append(r52).toString();
        goto L25
    L33:
        r12 = r6;
        goto L34
    L36:
        if (r6 >= 100) goto L42;
        StringBuffer r04 = new StringBuffer().append("0");
        if (r6 >= 0) goto L40;
        int r13 = -r6;
    L41:
        r52 = r04.append(r13).append(r52).toString();
        goto L47
    L40:
        r13 = r6;
        goto L41
    L42:
        StringBuffer r05 = new StringBuffer();
        if (r6 >= 0) goto L45;
        int r14 = -r6;
    L46:
        r52 = r05.append(r14).append(r52).toString();
        goto L47
    L45:
        r14 = r6;
        goto L46
    L22:
        r0 = r4 % 1000;
        goto L23
    L9:
        r7 = ".";
        goto L19
    L10:
        r7 = " ";
        goto L19
    L12:
        if (r4 < 10000) goto L19;
        r7 = " ";
        goto L19
    L15:
        if (r4 < 10000) goto L19;
        r7 = ".";
        goto L19
    L18:
        return new StringBuffer().append("").append(r4).toString();
    L5:
        return new StringBuffer().append("").append(r4).toString();
    }

    public static void h(int r5) {
        if (r5 == aB) goto L5;
    L6:
        aB = r5;
        int[] r0 = b.g;
        aC = r0;
        if (r0 != null) goto L9;
        aC = new int[256];
    L9:
        int r6 = 256;
    L11:
        if (r6 <= 0) goto L13;
        r6 = r6 - 1;
        aC[r6] = r5;
        goto L11
    L13:
        aD = a(aC, 16, 16, true);
        return;
    L5:
        if (aD == null) goto L6;
    }

    public static void d(Graphics r7, int r8, int r9, int r10, int r11) {
        int r0 = (e - r9) - r11;
        int r02 = r7.getClipX();
        int r03 = r7.getClipY();
        int r04 = r7.getClipWidth();
        int r05 = r7.getClipHeight();
        if (r0 <= r02) goto L5;
        int r06 = r0;
    L6:
        int r12 = r06;
        if (r8 <= r03) goto L9;
        int r07 = r8;
    L10:
        int r17 = r07;
        if ((r0 + r11) >= (r02 + r04)) goto L13;
        int r08 = r0 + r11;
    L14:
        int r09 = r08 - r12;
        if ((r8 + r10) >= (r03 + r05)) goto L17;
        int r010 = r8 + r10;
    L18:
        int r011 = r010 - r17;
        if (r09 > 0) goto L21;
        return;
    L21:
        if (r011 <= 0) goto L42;
        a(r7, r12, r17, r09, r011, false);
        int r012 = r09 + r12;
        int r013 = r011 + r17;
        int r122 = r12;
    L25:
        if (r122 >= r012) goto L34;
        int r82 = r17;
    L28:
        if (r82 >= r013) goto L33;
        r7.drawImage(aD, r122, r82, 20);     // Catch: Exception -> L31
    L32:
        r82 = r82 + 16;
        goto L28
    L33:
        r122 = r122 + 16;
        goto L25
    L34:
        a(r7, r02, r03, r04, r05, false);
        return;
    L42:
        return;
    L17:
        r010 = r03 + r05;
        goto L18
    L13:
        r08 = r02 + r04;
        goto L14
    L9:
        r07 = r03;
        goto L10
    L5:
        r06 = r02;
        goto L6
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

    static final void i(int r4) {
        t &= (1 << r4) ^ (-1);
    }

    static final boolean i() {
        if (t == 0) goto L6;
        return true;
    L6:
        return false;
    }

    static final void a(int r4, boolean r5) {
        q |= 1 << r4;
        if (((1 << r4) & 639) == 0) goto L6;
        q &= -640;
    L6:
        if (((1 << r4) & 31) == 0) goto L9;
        t |= 1 << r4;
        return;
    }

    static final void b(int r4, boolean r5) {
        q &= (1 << r4) ^ (-1);
        if (((1 << r4) & 31) == 0) goto L6;
        i(r4);
        return;
    }

    static final void j(int r3) {
        aM += r3;
    }

    static final void a(int r1, int r2, int r3, int r4) {
    }

    static final void a(int r1, int r2, int r3) {
    }

    static void f(Graphics r1) {
    }

    static final void a(Graphics r1, int r2) {
    }

    public static void b(int r6, int r7, int r8, int r9, int r10, int r11, int r12) {
        int r0 = (r10 >> 16) & 255;
        int r02 = (r10 >> 8) & 255;
        int r03 = r10 & 255;
        int r04 = ((r11 >> 16) & 255) - r0;
        int r05 = ((r11 >> 8) & 255) - r02;
        int r06 = (r11 & 255) - r03;
        int r07 = (r6 + r8) - 1;
        int r08 = (r7 + r9) - 1;
        int r13 = r0 << 16;
        int r14 = r02 << 16;
        int r102 = r03 << 16;
        if (r12 != 4) goto L9;
        int r09 = r07 - r6;
        int r010 = (r04 << 16) / r09;
        int r011 = (r05 << 16) / r09;
        int r012 = (r06 << 16) / r09;
        int r122 = r07;
    L6:
        if (r122 < r6) goto L31;
        c(r13 >> 16, r14 >> 16, r102 >> 16);
        a(a, r122, r7, r122, r08);
        r13 = r13 + r010;
        r14 = r14 + r011;
        r102 = r102 + r012;
        r122 = r122 - 1;
        goto L6
    L31:
        return;
    L9:
        if (r12 != 8) goto L15;
        int r013 = r07 - r6;
        int r014 = (r04 << 16) / r013;
        int r015 = (r05 << 16) / r013;
        int r016 = (r06 << 16) / r013;
        int r123 = r6;
    L12:
        if (r123 > r07) goto L32;
        c(r13 >> 16, r14 >> 16, r102 >> 16);
        a(a, r123, r7, r123, r08);
        r13 = r13 + r014;
        r14 = r14 + r015;
        r102 = r102 + r016;
        r123 = r123 + 1;
        goto L12
    L32:
        return;
    L15:
        if (r12 != 16) goto L21;
        int r017 = r08 - r7;
        int r018 = (r04 << 16) / r017;
        int r019 = (r05 << 16) / r017;
        int r020 = (r06 << 16) / r017;
        int r124 = r08;
    L18:
        if (r124 < r7) goto L33;
        c(r13 >> 16, r14 >> 16, r102 >> 16);
        a(a, r6, r124, r07, r124);
        r13 = r13 + r018;
        r14 = r14 + r019;
        r102 = r102 + r020;
        r124 = r124 - 1;
        goto L18
    L33:
        return;
    L21:
        if (r12 != 32) goto L26;
        int r021 = r08 - r7;
        int r022 = (r04 << 16) / r021;
        int r023 = (r05 << 16) / r021;
        int r024 = (r06 << 16) / r021;
        int r125 = r7;
    L24:
        if (r125 > r08) goto L34;
        c(r13 >> 16, r14 >> 16, r102 >> 16);
        a(a, r6, r125, r07, r125);
        r13 = r13 + r022;
        r14 = r14 + r023;
        r102 = r102 + r024;
        r125 = r125 + 1;
        goto L24
    L34:
        return;
    }

    static {
        a = null;
        u = null;
        v = null;
        d = 400;
        e = 240;
        B = 62;
        D = -1;
        E = null;
        F = null;
        G = true;
        O = -9999;
        S = 24;
        i = 256;
        m = 256;
        n = (90 * m) / 360;
        W = (180 * m) / 360;
        o = (270 * m) / 360;
        X = (360 * m) / 360;
        Y = 804;
        av = 0;
        ax = "UTF-8";
        aE = false;
        p = false;
        aF = false;
        q = 0;
        r = null;
        aG = null;
        s = null;
        t = 0;
        aH = 0;
    }
}
