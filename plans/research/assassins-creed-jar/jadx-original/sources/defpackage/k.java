package defpackage;

import java.io.IOException;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordStore;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:k.class */
final class k extends j implements Runnable {
    public static int u;
    public static int v;
    public static int w;
    private static int bR;
    private static int bS;
    private static int bT;
    private static b bW;
    static b y;
    private static b bX;
    private static b bY;
    private static b bZ;
    private static b ca;
    public static b[] z;
    public static b[] A;
    public static i B;
    private static Graphics cd;
    public static i C;
    public static i D;
    public static i E;
    private static int[] cp;
    public static i N;
    private static Image cv;
    private static Graphics cw;
    private static int cy;
    public static int O;
    public static int P;
    private static int cA;
    private static int cB;
    private static int cC;
    private static int cD;
    private static int cE;
    private static int cF;
    public static int Q;
    public static int R;
    public static int S;
    public static int T;
    public static int U;
    private static int cG;
    private static int cH;
    public static int V;
    public static int W;
    public static int X;
    public static int Y;
    private static int cI;
    public static boolean Z;
    public static boolean aa;
    public static boolean ab;
    public static i ae;
    private static int cN;
    public static i ah;
    private static int cO;
    private static boolean cP;
    private static int cT;
    private static int cW;
    public static int aj;
    private static int da;
    public static int ak;
    public static boolean al;
    private static int dm;
    public static int aw;
    private static int dz;
    private static i dA;
    public static int aA;
    public static String aB;
    public static int aC;
    public static i aD;
    public static int aK;
    public static int aN;
    private static int dF;
    public static Image aQ;
    private static Image dJ;
    private static Graphics dK;
    private static int[][] dL;
    private static boolean dM;
    private static int dN;
    private static int dO;
    private static int dP;
    private static int dQ;
    private static int dU;
    private static int dW;
    private static int dX;
    private static int dY;
    private static int dZ;
    public static g aS;
    public static i aU;
    public static i aV;
    public static int bc;
    public static int be;
    private static byte[] ek;
    public static int bp;
    public static int bq;
    public static int br;
    public static int bs;
    private static byte[] ep;
    private static byte[] eq;
    private static byte[] er;
    private static byte[] es;
    private static byte[] et;
    private static byte[] eu;
    private static byte[] ev;
    public static int bt;
    public static int bu;
    private static int ew;
    public static int bw;
    private static int ex;
    private static int ey;
    public static byte[][][] by;
    private static short[] eH;
    public static int[][] bz;
    static int bB;
    private static int eK;
    private static int eL;
    static int bC;
    static int bD;
    private static int eM;
    private static int eN;
    private static int eO;
    private static int eP;
    private static StringBuffer fa;
    private static String fb;
    private static int fh;
    private static int fi;
    private static int fv;
    private static int fw;
    private static int fx;
    private static String[] bM = new String[15];
    private static int[] bN = new int[15];
    private static int bO = 0;
    private static int bP = 0;
    public static int x = 0;
    private static boolean bQ = true;
    private static String[] bU = null;
    private static char[] bV = {'.', '!', '?', ',', ':'};
    private static boolean cb = false;
    private static int[] cc = new int[3];
    public static i F = null;
    public static int G = -1;
    private static int ce = 60;
    private static int cf = 60;
    private static int cg = 37;
    static int H = -1;
    static int I = -1;
    private static int ch = -1;
    private static int ci = -1;
    static int J = -1;
    static int K = -1;
    private static int cj = -1;
    private static int ck = -1;
    private static boolean cl = false;
    private static int cm = 1;
    private static int cn = 0;
    public static i L = null;
    private static int co = 6;
    public static int[] M = new int[4];
    private static int cq = -1;
    private static int cr = 0;
    private static boolean cs = false;
    private static boolean ct = false;
    private static int cu = 0;
    private static long cx = 0;
    private static boolean cz = false;
    public static int[] ac = new int[4];
    private static int cJ = 1;
    public static int ad = 2;
    private static int cK = 4;
    private static boolean cL = false;
    public static int af = 0;
    public static int ag = 0;
    private static int cM = 200;
    public static boolean ai = false;
    private static int cQ = -1;
    private static int cR = -1;
    private static boolean cS = false;
    private static int cU = 0;
    private static String[] cV = new String[4];
    private static int[] cX = new int[4];
    private static int cY = 1;
    private static int cZ = 0;
    private static boolean db = false;
    private static boolean dc = false;
    public static boolean am = false;
    private static boolean dd = false;
    public static boolean an = false;
    public static boolean ao = false;
    private static boolean de = false;
    private static int df = -1;
    private static long dg = 0;
    public static int[] ap = new int[6];
    public static int aq = 0;
    private static final int[] dh = {100, 200, 300};
    private static final int[] di = {100, 200, 300};
    private static int dj = 0;
    private static int dk = 0;
    private static a dl = null;
    public static int[] ar = new int[5];
    public static int as = 0;
    public static int at = 0;
    private static int[] dn = {10, 12, 9, 11};

    /* renamed from: do, reason: not valid java name */
    private static int[] f0do = {5, 5, 5, 5, 5, 5, 5, 5, 5};
    public static int au = 0;
    private static int[] dp = new int[8];
    private static int[] dq = new int[8];
    private static int[] dr = new int[8];
    private static int ds = -1;
    private static boolean dt = false;
    private static long du = 0;
    private static int dv = 0;
    private static int dw = 0;
    private static boolean dx = false;
    private static String dy = null;
    public static boolean av = false;
    public static byte ax = 30;
    public static byte ay = 30;
    private static byte dB = 30;
    private static byte dC = 30;
    private static int dD = 0;
    public static int az = 0;
    private static int[] dE = {0, 100, 200, 400, 600, 800};
    public static int aE = 0;
    public static int aF = 0;
    public static int aG = 0;
    public static int aH = 0;
    public static int aI = 0;
    public static int aJ = 0;
    public static int aL = -1;
    public static int aM = -1;
    public static int aO = 0;
    public static String aP = null;
    private static boolean dG = false;
    private static int[] dH = {0, 11, -20, 0};
    private static int dI = 0;
    private static int dR = -1;
    public static int aR = -1;
    private static int dS = -1;
    private static int dT = -1;
    private static int[] dV = {16777215, 16711680, 4210752, 255, 16777215, 16777215, 16777215, 65280, 16777215, 16776960, 12632128, 0, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 16777215, 65535, 16777215, 16777215, 40863, 16777215, 16777215, 16777215, 16777215};
    public static boolean aT = false;
    public static int aW = 50;
    public static i[] aX = new i[50];
    public static i[] aY = new i[3];
    public static boolean aZ = false;
    public static int ba = 1000;
    public static i[] bb = new i[1000];
    public static i[] bd = new i[ba];
    private static int[] ea = new int[ba];
    private static int eb = 0;
    public static byte[] bf = new byte[ba * 22];
    public static byte[] bg = new byte[ba];
    private static String[] ec = {"/6", "/7", "/8", "/9", "/10", "/11", "/12", "/13"};
    private static String[] ed = {"/6", "/7", "/8", "/9", "/10", "/11", "/12", "/13"};
    private static int[] ee = {5, 2, 3, 3, 2, 4, 5, 1};
    static final int[] bh = {4, 3, 4, 4, 3, 4, 4, 4, 4};
    private static boolean[] ef = {false, false, false, false, false, false, false, false, false};
    private static boolean[] eg = {true, true, true, true, true, true, true, true, true};
    public static final int[] bi = {0, -1, 1, 2, 3, 1, 4, 60, 5, 47, 6, 7, 8, 61, 9, 25, 10, 7, -1, 11, -1, 13, 14, 7, 40, 16, 15, 48, -1, 52, 36, 44, 36, -1, 42, 62, -1, -1, -1, -1, 45, 30, -1, 31, 32, 33, 29, 7, 13, -1, 7, 28, -1, -1, 19, -1, 19, -1, 20, -1, 21, 71, -1, -1, 22, -1, 23, -1, 26, 38, 43, -1, 51, 7, 54, 55, 56, -1, 63, 0, 57};
    public static final int[] bj = {19, 68};
    public static final int[] bk = {24, 27, 27, 27, 34, 35, 37, 41, 64, 64, 65, 67, 49, 69, 70};
    public static final int[] bl = {29, 0};
    public static final int[] bm = {60, 66};
    public static final int[] bn = {47, 72};
    private static int[] eh = {0};
    private static int[] ei = {4};
    public static final int[][] bo = {new int[]{0, -1}, new int[]{3, 1}, new int[]{5, 2}, new int[]{6, 3}};
    private static byte[] ej = {11, 10, 12, 10, 3, 3, 4, 3, 6, 5, 7, 5, 6, 5, 7, 5, 3, 3, 4, 4, 1, 0, 2, 0, 8, 10, 9, 10, 11, 10, 12, 10};
    private static int[] el = new int[75];
    private static int[] em = {1, 384, 384, 0, 0, 384, 0, 0, 0, 384, 0, 0, 0, 0, 0, 0, 1, 384, 384, 0, 0, 384, 0, 0, 0, 0, 384, 0, 384, 384, 0, 0, 384, 0, 0, 0, 0, 0, 0, 384, 0, 0, 0, 0, 1, 0, 257, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 384, 384, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 384, 0};
    private static int[] en = {39, 1, 90, 0, 44, 0, 80, 1, 69, 0, 75, 1, 42, 0, 55, 0, 46, 0, 80, 1, 50, 0, 90, 1, 37, 0, 55, 0, 58, 0, 25, 1};
    private static int[] eo = {44, 0, 1, 46, 0, 1, 50, 0, 0, 15, 1, 2, 13, 1, 2, 40, 1, 2, 49, 1, 2, 54, 0, 1, 54, 7, 1, 69, 0, 1, 57, 0, 0, 58, 0, 0};
    public static int bv = 0;
    private static int ez = 0;
    private static final int[][] eA = {new int[]{2, 1, 3, 32}, new int[]{11, 12, 4, 6, 0, 8}, new int[]{35, 36, 37}, new int[]{14, 15}, new int[]{83, 84, 123, 97, 5, 113, 7, 87}, new int[]{106, 107, 108, 109}};
    private static int eB = -1;
    private static int eC = -1;
    public static int bx = -1;
    private static int eD = 0;
    private static int eE = 0;
    private static int eF = 0;
    private static int eG = 0;
    private static int[] eI = {6, 2, 4, 2, 2, 4, 9, 2, 4, 8, 4, 9, 6, 4, 4};
    static byte[] bA = new byte[512];
    private static boolean eJ = false;
    private static int eQ = 0;
    private static boolean eR = false;
    private static int eS = 0;
    private static int[][] eT = {new int[]{1024, 2, 4, 8, 16}, new int[]{1024, 4, 4, 8, 16}, new int[]{1024, 8, 4, 8, 16}, new int[]{1024, 16, 4, 8, 16}, new int[]{1024, 32, 4, 8, 16}, new int[]{1024, 64, 4, 8, 16}, new int[]{1024, 128, 4, 8, 16}, new int[]{1024, 256, 4, 8, 16}, new int[]{1024, 512, 4, 8, 16}};
    private static boolean eU = false;
    private static boolean eV = false;
    private static int[] eW = {2, 2, 1, 1, 2, 0, 3, 2, 2};
    private static int[] eX = {51, 52, 53, 54};
    private static int eY = 0;
    private static int eZ = 0;
    private static int fc = 0;
    private static int fd = 0;
    private static int fe = 0;
    private static int[] ff = {21, 20};
    private static int[] fg = {21, 20};
    public static boolean bE = false;
    public static boolean bF = false;
    static int bG = -1;
    static int bH = -1;
    private static int fj = -1;
    public static int bI = 0;
    private static int fk = 4;
    private static int fl = 9;
    private static int fm = (240 / fl) / 2;
    private static int fn = 0;
    public static int bJ = 0;
    private static int fo = 255;
    private static int fp = 120;
    private static int fq = 120;
    private static int fr = 120;
    private static int fs = 80;
    public static boolean bK = false;
    private static int[] ft = new int[4];
    private static int[] fu = new int[4];
    private static boolean fy = false;
    private static int fz = 0;
    private static int fA = 0;
    private static int fB = 0;
    private static int fC = 0;
    private static int fD = 0;
    private static int fE = 0;
    private static int fF = 0;
    private static boolean fG = false;
    private static int fH = 0;
    private static int fI = 0;
    private static a fJ = null;
    private static a fK = null;
    private static a fL = null;
    private static int fM = -1;
    private static int fN = -1;
    private static int fO = 0;
    private static int[] fP = {0, 2, 5, 7};
    private static int fQ = 1;
    public static int bL = 0;
    private static int fR = 0;
    private static int fS = -1;

    public static boolean b(int i, int i2, int i3, int i4) {
        if (i3 == -1) {
            return false;
        }
        u = i;
        if (i == 0 || i == 4 || i == 5 || i == 7) {
            w = a(d(i2, i3), 0, false, i) + 1;
        } else {
            if (i4 < i3) {
                i4 = i3;
            }
            w = (i4 - i3) + 1;
            int i5 = 0;
            while (i5 < w) {
                int iA = a(d(i2, i3 + i5), i5, i != 6, i);
                w += iA - i5;
                i5 = iA + 1;
            }
            D(0);
        }
        bQ = true;
        z();
        return true;
    }

    private static int a(String str, int i, boolean z2, int i2) {
        int i3 = 220;
        int i4 = 0;
        short s = 0;
        if (str == null || str.length() <= 1) {
            return 0;
        }
        if (z2) {
            if (i2 != 9) {
                bN[i] = str.charAt(0) - '0';
            }
            i3 = 300;
        }
        short[] sArrA = a(y, str, i3);
        for (int i5 = sArrA[0]; i5 > 3; i5 -= 3) {
            i4++;
            short s2 = sArrA[(-1) + ((i4 << 1) * 3)];
            bM[(i + i4) - 1] = str.substring(s, s2);
            if (z2 && i4 > 1) {
                bN[(i + i4) - 1] = bN[i];
            }
            s = s2;
        }
        bM[i + i4] = str.substring(s);
        if (z2 && i4 > 0) {
            bN[i + i4] = bN[i];
        }
        return i + i4;
    }

    public static boolean b(int i, int i2, int i3) {
        bO = i3;
        if (i > 0) {
            bN[0] = i;
        } else {
            bN[0] = -1;
        }
        return b(9, 1 + aj, i2, i2);
    }

    private static void i(int i, int i2) {
        A[4].a(cd, 12, 0, 0, i2, 0, 0, 0);
        j.h(Integer.MIN_VALUE);
        j.d(cd, 0, i2, 400, 68);
        if (j.g % 10 < 5 && u == 10) {
            y.a(cd, d(0, 9), 200, 220, 3);
            return;
        }
        if (j.g % 10 < 5 && u == 9) {
            y.a(cd, d(0, 9), 200, 220, 3);
        } else {
            if (j.g % 10 >= 5 || u == 8) {
                return;
            }
            y.a(cd, d(0, 9), 200, 220, 3);
        }
    }

    private static void D(int i) {
        v = Math.min(i, w);
        if (A()) {
            z();
        }
    }

    private static void z() {
        bS = 30;
        bR = 0;
        bT = 0;
    }

    private static boolean A() {
        return bT == -1;
    }

    static String d(int i, int i2) {
        return i == 0 ? bU[i2] : j.g(i2);
    }

    private static boolean a(char c) {
        for (int i = 0; i < bV.length; i++) {
            if (c == bV[i]) {
                return true;
            }
        }
        return false;
    }

    static short[] a(b bVar, String str, int i) {
        String string;
        if (str == null) {
            string = str;
        } else {
            StringBuffer stringBuffer = new StringBuffer(str);
            for (int i2 = 0; i2 < str.length(); i2++) {
                if (str.charAt(i2) == ' ' && i2 + 1 < str.length() && a(str.charAt(i2 + 1))) {
                    stringBuffer.setCharAt(i2, '%');
                }
            }
            string = stringBuffer.toString();
        }
        return bVar.a(string, i, false);
    }

    k(Object obj, Object obj2) {
        super(obj, obj2);
        j.a(System.currentTimeMillis());
        ad();
        b();
    }

    protected final void pointerPressed(int i, int i2) {
        int iJ = j(i2, 240 - i);
        if (iJ != -1) {
            E(2 << iJ);
        }
        cj = i2;
        ck = 240 - i;
        f.b(cj, ck);
    }

    protected final void pointerReleased(int i, int i2) {
        cj = i2;
        ck = 240 - i;
        ch = i2;
        ci = 240 - i;
        cl = true;
        if (eL != 0) {
            eN = eL;
            eL = 0;
        }
        f.a(ch, ci);
    }

    protected final void pointerDragged(int i, int i2) {
        int iJ = j(i2, 240 - i);
        if (iJ != -1 && !v(2 << iJ)) {
            E(2 << iJ);
        }
        cj = i2;
        ck = 240 - i;
        f.c(cj, ck);
    }

    static boolean c(int i, int i2, int i3, int i4) {
        return b(H, I, i, i2, i3, i4);
    }

    static boolean j() {
        if (H == -1 && I == -1) {
            return false;
        }
        if (H < ce || H > 400 - cf || I <= 240 - cg || I >= 240) {
            return I >= 0 && I <= 240 - cg;
        }
        return true;
    }

    static boolean d(int i, int i2, int i3, int i4) {
        return b(J, K, i, i2, i3, i4);
    }

    private static boolean b(int i, int i2, int i3, int i4, int i5, int i6) {
        return !(i == -1 && i2 == -1) && i >= i3 && i <= i3 + i5 && i2 >= i4 && i2 <= i4 + i6;
    }

    private static boolean b(int i, int i2, int i3, int i4, int i5) {
        return a(i, i2, i3 + 35, i4 + 35, 35);
    }

    static boolean a(int i, int i2, int i3, int i4, int i5) {
        return !(i == -1 && i2 == -1) && h(Math.abs(i - i3), Math.abs(i2 - i4)) <= i5;
    }

    public static boolean k() {
        return cm == 1;
    }

    private static void E(int i) {
        v();
        eK |= i;
        if (j.c == 8 && bh[aj] == 3 && !k()) {
            switch (i) {
                case 2:
                    eK = 20;
                    break;
                case 8:
                    eK = 68;
                    break;
                case 128:
                    eK = 272;
                    break;
                case 512:
                    eK = 320;
                    break;
            }
        }
        bC |= eK;
        eL |= eK;
    }

    private static int j(int i, int i2) {
        int iC;
        if (!((j.c == 21 && u == 8) || j.c == 8 || (j.c == 21 && u == 10)) || i == -1 || i2 == -1 || i2 >= 240) {
            return -1;
        }
        if (((i <= ce || i >= 400 - cf) && i2 >= 207) || b(i, i2, 354, 0, 46, 37)) {
            return -1;
        }
        if (k()) {
            if (bh[aj] != 3) {
                if (b(i, i2, 270, 165, 70)) {
                    return 4;
                }
                if (b(i, i2, 320, 110, 70)) {
                    return 1;
                }
            }
            if (!b(i, i2, cn - 10, 124, 116, 116) || (iC = c(i, i2, (cn - 10) + 38, (cn - 10) + 77, 162, 201)) == 4) {
                return -1;
            }
            return iC;
        }
        if (bh[aj] == 3 && b(i, i2, (aS.ak - O) - 10, ((aS.al - P) - 20) - 25, 20, 25)) {
            aS.aq = -1;
            aS.ar = -1;
            return 4;
        }
        if (L != null && cp != null && i >= cp[0] - O && i <= cp[2] - O && i2 >= cp[1] - P && i2 <= cp[3] - P) {
            return co == 1 ? 1 : 4;
        }
        if (k(i, i2) || i.b(i, i2)) {
            return -1;
        }
        if (aS.S == 250 || aS.S == 244) {
            int i3 = (aS.ak - O) - 38;
            int i4 = i3 + 76;
            int i5 = (aS.al - P) - 38;
            return c(i, i2, i3, i4, i5, i5 + 76);
        }
        if (aS.W[1] == aS.W[3]) {
            aS.W[1] = aS.Y[1];
            aS.W[3] = aS.Y[3];
        }
        int i6 = (aS.ak - O) - 25;
        return c(i, i2, i6, i6 + 50, (aS.W[1] - P) - 10, (aS.W[3] - P) + 10);
    }

    private static int c(int i, int i2, int i3, int i4, int i5, int i6) {
        if (i == -1 && i2 == -1) {
            return -1;
        }
        int i7 = i2 < i5 ? 0 : i2 > i6 ? 2 : 1;
        int i8 = i < i3 ? 0 : i > i4 ? 2 : 1;
        if (k() && i2 > i5 && i2 < i6) {
            if (i > i3 && i <= (i3 + i4) / 2) {
                return 3;
            }
            if (i > (i3 + i4) / 2 && i < i4) {
                return 5;
            }
        }
        return (i7 * 3) + i8;
    }

    public static void l() {
        if (M == null) {
            M = new int[4];
        }
        int i = aS.ak;
        if (aS.av) {
            i = aS.ak - 60;
        }
        M[0] = i;
        M[1] = aS.al - 60;
        M[2] = i + 60;
        M[3] = aS.al;
    }

    public static void a(i iVar, int i, int[] iArr) {
        if (L != null && L.aw == iVar.aw) {
            if (L.ax == 51) {
                iArr = iVar.Y;
            }
            a(iArr);
        } else {
            if (iVar == null || i < 0 || i >= 6) {
                return;
            }
            if (i < co || (i == 1 && co == 1)) {
                co = i;
                L = iVar;
                if (iVar.ax == 51) {
                    iArr = iVar.Y;
                }
                a(iArr);
            }
        }
    }

    private static void a(int[] iArr) {
        if (L != null) {
            if (cp == null) {
                cp = new int[4];
            }
            cp[0] = iArr[0] - 10;
            cp[1] = iArr[1] - 10;
            cp[2] = iArr[2] + 10;
            cp[3] = iArr[3] + 10;
        }
    }

    public static void m() {
        co = 6;
        L = null;
        cp = null;
        cp = new int[4];
    }

    public static void c(int i, int i2, int i3) {
        if (N == null) {
            i iVar = new i();
            N = iVar;
            iVar.aa = r(9);
            N.ax = 14;
            N.i(54);
            N.az = 302;
            N.au = 0;
            N.ak = i;
            N.al = i2;
            N.t();
            cq = i3;
        }
        N.ak = i;
        N.al = i2;
    }

    public static void k(int i) {
        if (N != null) {
            if (cq == i || i == -1) {
                N.p();
                N = null;
                cq = -1;
            }
        }
    }

    private static boolean k(int i, int i2) {
        if (N == null) {
            return false;
        }
        return b(i, i2, (N.ak - 25) - O, (N.al - 25) - P, 50, 50);
    }

    /* JADX WARN: Removed duplicated region for block: B:375:0x0e14 A[Catch: Exception -> 0x181d, TryCatch #0 {Exception -> 0x181d, blocks: (B:3:0x000a, B:5:0x0010, B:7:0x0019, B:11:0x0024, B:66:0x0162, B:67:0x0165, B:68:0x01f4, B:70:0x01fa, B:71:0x020d, B:73:0x021a, B:74:0x0226, B:75:0x022f, B:76:0x0235, B:78:0x023d, B:80:0x0243, B:84:0x0263, B:81:0x0254, B:83:0x025b, B:85:0x026b, B:86:0x0271, B:88:0x02b9, B:89:0x02c6, B:91:0x02ce, B:94:0x02d8, B:95:0x02e9, B:97:0x02fa, B:98:0x031a, B:100:0x0322, B:102:0x0328, B:104:0x033b, B:106:0x0366, B:108:0x036d, B:109:0x0371, B:111:0x038b, B:112:0x039f, B:114:0x03a7, B:115:0x03b5, B:117:0x03be, B:118:0x03cd, B:120:0x03d5, B:122:0x03e4, B:123:0x03f3, B:124:0x0418, B:125:0x0424, B:127:0x0450, B:129:0x0458, B:131:0x0484, B:132:0x048e, B:139:0x04dc, B:141:0x04e8, B:146:0x0509, B:143:0x04f0, B:145:0x0502, B:138:0x04c4, B:147:0x0517, B:149:0x0525, B:151:0x052d, B:153:0x0543, B:154:0x054d, B:156:0x0579, B:158:0x0581, B:160:0x05a6, B:161:0x05b0, B:162:0x05de, B:164:0x05e5, B:166:0x062a, B:168:0x063e, B:169:0x064a, B:170:0x0669, B:172:0x0695, B:174:0x069d, B:176:0x06a4, B:178:0x06bc, B:180:0x0709, B:181:0x0716, B:183:0x071e, B:185:0x0724, B:187:0x0732, B:189:0x074b, B:190:0x07b1, B:192:0x07ba, B:195:0x07d0, B:197:0x07f2, B:199:0x080d, B:200:0x0814, B:202:0x081c, B:218:0x08dc, B:220:0x08ff, B:222:0x0908, B:223:0x090e, B:203:0x084b, B:205:0x0853, B:206:0x0873, B:208:0x087b, B:210:0x0884, B:211:0x0894, B:213:0x089c, B:215:0x08a5, B:216:0x08bd, B:217:0x08c5, B:196:0x07eb, B:225:0x0918, B:227:0x0924, B:228:0x093c, B:230:0x094a, B:231:0x094f, B:233:0x0973, B:236:0x097f, B:237:0x0987, B:238:0x0996, B:240:0x09a1, B:241:0x09a5, B:243:0x09ac, B:248:0x0a1c, B:250:0x0a23, B:252:0x0a36, B:251:0x0a2e, B:244:0x09fc, B:246:0x0a03, B:247:0x0a11, B:253:0x0a4d, B:255:0x0a71, B:258:0x0a7d, B:259:0x0a85, B:260:0x0a94, B:261:0x0a9a, B:262:0x0aa0, B:264:0x0aa6, B:266:0x0aae, B:270:0x0abe, B:274:0x0ad2, B:276:0x0ade, B:278:0x0af7, B:279:0x0b0c, B:281:0x0b12, B:282:0x0b17, B:283:0x0b1a, B:284:0x0b54, B:286:0x0b5b, B:289:0x0b6a, B:290:0x0b70, B:292:0x0b7e, B:295:0x0ba5, B:296:0x0bc2, B:298:0x0bca, B:300:0x0bd2, B:305:0x0bed, B:301:0x0bd9, B:303:0x0be0, B:304:0x0be8, B:294:0x0b85, B:288:0x0b62, B:306:0x0bf7, B:308:0x0c02, B:321:0x0c67, B:323:0x0c6f, B:325:0x0c75, B:326:0x0c7d, B:328:0x0c84, B:329:0x0c8a, B:336:0x0cc7, B:338:0x0d16, B:339:0x0d34, B:341:0x0d3c, B:343:0x0d42, B:345:0x0d4a, B:347:0x0d55, B:349:0x0d6f, B:350:0x0d75, B:351:0x0d8a, B:353:0x0d90, B:355:0x0d96, B:357:0x0db7, B:358:0x0dbb, B:360:0x0dc3, B:378:0x0e30, B:380:0x0e39, B:382:0x0e40, B:384:0x0e48, B:385:0x0e56, B:386:0x0e5e, B:388:0x0e65, B:390:0x0e6d, B:391:0x0e75, B:392:0x0e7d, B:394:0x0e85, B:396:0x0e8b, B:397:0x0e91, B:398:0x0e99, B:400:0x0ea1, B:401:0x0ea5, B:362:0x0dca, B:364:0x0dd2, B:366:0x0dda, B:367:0x0df0, B:369:0x0df8, B:375:0x0e14, B:377:0x0e29, B:371:0x0e00, B:373:0x0e08, B:337:0x0cf1, B:333:0x0c9f, B:309:0x0c0b, B:311:0x0c16, B:312:0x0c22, B:314:0x0c2d, B:316:0x0c39, B:317:0x0c4a, B:319:0x0c59, B:320:0x0c62, B:402:0x0eaa, B:404:0x0eb7, B:406:0x0ebd, B:413:0x0ee7, B:415:0x0eed, B:417:0x0efe, B:418:0x0f06, B:420:0x0f10, B:422:0x0f1d, B:424:0x0f2d, B:423:0x0f24, B:425:0x0f45, B:427:0x0f4b, B:429:0x0f51, B:430:0x0f65, B:432:0x0f73, B:434:0x0f9e, B:436:0x0fbb, B:437:0x0fc0, B:439:0x0fc8, B:441:0x0fce, B:442:0x0fd4, B:433:0x0f8a, B:408:0x0ec6, B:410:0x0ece, B:412:0x0ed6, B:268:0x0ab6, B:271:0x0ac4, B:273:0x0acf, B:443:0x0fe0, B:444:0x0fe7, B:446:0x0ff3, B:448:0x0ffb, B:450:0x1001, B:452:0x1050, B:453:0x1053, B:454:0x1065, B:456:0x106b, B:458:0x107a, B:460:0x10a5, B:461:0x10ab, B:463:0x10b1, B:464:0x10b8, B:465:0x111d, B:467:0x1123, B:468:0x112b, B:470:0x1131, B:471:0x1138, B:473:0x11b7, B:476:0x11c6, B:478:0x11cf, B:475:0x11bd, B:480:0x11eb, B:481:0x11f1, B:483:0x1208, B:485:0x120e, B:489:0x123b, B:491:0x128f, B:493:0x1295, B:495:0x12aa, B:496:0x12bc, B:488:0x1232, B:498:0x12c6, B:499:0x12cd, B:500:0x12df, B:502:0x1308, B:505:0x131d, B:507:0x132e, B:504:0x130e, B:509:0x1349, B:513:0x135b, B:515:0x137a, B:518:0x1386, B:519:0x138e, B:520:0x139b, B:521:0x13a1, B:522:0x13a7, B:523:0x13ad, B:525:0x13b2, B:527:0x13b8, B:530:0x13f9, B:532:0x1405, B:533:0x142f, B:534:0x144c, B:536:0x1452, B:538:0x148a, B:539:0x14cb, B:540:0x1506, B:542:0x1518, B:543:0x1524, B:545:0x1544, B:546:0x154c, B:548:0x155a, B:550:0x1566, B:551:0x156a, B:553:0x1572, B:554:0x1576, B:556:0x157c, B:558:0x158e, B:562:0x1599, B:563:0x159c, B:565:0x15ab, B:569:0x15b6, B:570:0x15b9, B:572:0x15c8, B:576:0x15d3, B:580:0x15e1, B:581:0x15e4, B:583:0x15f3, B:587:0x15fe, B:588:0x1601, B:590:0x160f, B:591:0x1613, B:593:0x1619, B:595:0x1697, B:597:0x16a5, B:599:0x16d8, B:601:0x16de, B:603:0x1713, B:605:0x1719, B:607:0x171f, B:609:0x1754, B:611:0x175a, B:613:0x178f, B:612:0x1776, B:608:0x173b, B:602:0x16fa, B:598:0x16c0, B:614:0x17a5, B:616:0x17ab, B:617:0x17b5, B:619:0x17c6, B:620:0x17ca, B:622:0x17d0, B:623:0x17d8, B:625:0x17f8, B:12:0x002a, B:13:0x002e, B:15:0x0036, B:17:0x0045, B:18:0x0054, B:19:0x005a, B:21:0x0062, B:23:0x0068, B:24:0x0070, B:26:0x007c, B:27:0x0095, B:28:0x00c8, B:32:0x00d3, B:33:0x00d9, B:37:0x00e4, B:38:0x00ea, B:42:0x00f5, B:43:0x00fb, B:47:0x0106, B:48:0x010c, B:52:0x0117, B:53:0x011d, B:54:0x0129, B:55:0x0131, B:59:0x013c, B:60:0x0142, B:64:0x014d, B:65:0x0150), top: B:630:0x000a }] */
    @Override // defpackage.j
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final void a() {
        Graphics graphics = j.a;
        cd = graphics;
        i.bg = graphics;
        try {
            if (eR && v(2048)) {
                g.v = !g.v;
            } else {
                int i = eS;
                while (true) {
                    if (i >= eT.length) {
                        break;
                    }
                    if (v(eT[i][eQ])) {
                        eS = i;
                        eQ++;
                        break;
                    }
                    i++;
                }
                if (i == eT.length && t()) {
                    eQ = 0;
                    eS = 0;
                }
                if (eQ == eT[0].length) {
                    new StringBuffer().append("Cheat:").append(eS).toString();
                    switch (eS) {
                        case 0:
                            g.s = !g.s;
                            break;
                        case 1:
                            eR = !eR;
                            break;
                        case 2:
                            dc = !dc;
                            break;
                        case 3:
                            db = !db;
                            break;
                        case 4:
                            eU = !eU;
                            break;
                        case 5:
                            l(15);
                            j.g = 1;
                            break;
                        case 6:
                            l(13);
                            break;
                        case 7:
                            dt = !dt;
                            break;
                        case 8:
                            eV = !eV;
                            break;
                    }
                    eQ = 0;
                    eS = 0;
                    z(15);
                    v();
                }
            }
            switch (j.c) {
                case 0:
                    R();
                    break;
                case 1:
                    f(false);
                    a(y, 0, d(0, 99), 200, 120, 220, 240, 0, 3);
                    if (v(262144) || j()) {
                        l(25);
                        v();
                        z(23);
                    }
                    y.l(1);
                    if (j.g % 10 < 5) {
                        y.a(cd, d(0, 9), 200, 220, 3);
                    }
                    break;
                case 2:
                    f(false);
                    if (A[1] != null) {
                        A[1].a(cd, 3, 0, 200, 4, 0, 0, 0);
                    }
                    int i2 = fD + 3;
                    fD = i2;
                    if (i2 >= 12) {
                        fD = 12;
                    }
                    d(0, 0);
                    d(93, 45, 214);
                    fO = 0;
                    a(d(0, 79), (bv == 0 || bv == 3) ? "" : d(0, 17));
                    L(ey);
                    Q();
                    break;
                case 3:
                    f(false);
                    d(0, 4);
                    d(14, 47, 180);
                    a(d(0, 79), (bv == 0 || bv == 3) ? "" : d(0, 17));
                    L(ey);
                    Q();
                    break;
                case 4:
                    F();
                    break;
                case 5:
                    G();
                    break;
                case 6:
                    cb = true;
                    f(false);
                    d(0, 7);
                    bW.l(1);
                    b(y, 1, d(0, 77), 200, 50, 390, 155, 0, 1);
                    j.a(cd, 0, 0, 400, 240, true);
                    if (!dx) {
                        a("", d(0, 17));
                    }
                    if (v(131072) && !dx) {
                        l(3);
                        z(30);
                    }
                    break;
                case 8:
                case 21:
                    if (!fy) {
                        if ((j.c == 21 && u == 8) || j.c == 8) {
                            I();
                        } else if (bh[aj] == 3) {
                            H();
                        }
                        b(false);
                        if (j.c == 21) {
                            j.a(cd, 0, 0, 400, 240, true);
                            if (u == 7) {
                                cd.setColor(0);
                                j.b(cd, 0, 0, 400, 240);
                            }
                            if (j()) {
                                E(65568);
                            }
                            switch (u) {
                                case 0:
                                case 4:
                                case 5:
                                case 7:
                                    if (u == 4 || u == 5) {
                                        bP = 60;
                                    } else {
                                        bP = 240;
                                    }
                                    i(0, bP);
                                    if (u == 4 || u == 5) {
                                        a(bW, 0, bM[0], 200, bP + 120, 400, 3, bT);
                                    } else {
                                        a(bW, 0, bM[0], 200, bP + 30, 400, 3, bT);
                                    }
                                    if (v(65568)) {
                                        if (u == 7) {
                                            l(2);
                                        } else if (u == 5) {
                                            l(15);
                                        } else {
                                            l(8);
                                        }
                                        z(23);
                                        break;
                                    }
                                    break;
                                case 1:
                                case 2:
                                case 3:
                                case 6:
                                case 8:
                                case 9:
                                case 10:
                                    int i3 = -1;
                                    if (u == 6) {
                                        bP = 137;
                                    } else if (bN[v] == 1) {
                                        bP = 137;
                                        i3 = 1;
                                    } else if (bN[v] > 1 && bN[v] <= 10) {
                                        bP = 50;
                                        i3 = bN[v];
                                    } else if (aS.al - P < 120) {
                                        bP = 137;
                                    } else {
                                        bP = 50;
                                    }
                                    if (u == 9) {
                                        if (bO == 0) {
                                            bP = 50;
                                        } else if (bO == 1) {
                                            bP = 137;
                                        }
                                    }
                                    i(0, bP);
                                    if (u == 6 || i3 == -1) {
                                        a(y, 0, bM[v], 200, bP + 34, 380, 3, bT);
                                    } else {
                                        if (i3 == 1) {
                                            A[4].a(cd, 4 + bL, 0, 378, (bP + 68) - 4, 0, 0, 0);
                                        } else {
                                            z[39].a(cd, i3, 0, 355, (bP + 68) - 2, 0, 0, 0);
                                        }
                                        a(y, 0, bM[v], 10, bP + 4, 300, 20, bT);
                                    }
                                    if (!v(131072) || C == null || u != 9 || !C.cd[2]) {
                                        if (bQ && !A()) {
                                            int i4 = bR + 1;
                                            bR = i4;
                                            int i5 = (i4 * bS) / 16;
                                            bT = i5;
                                            if (i5 > bM[v].length()) {
                                                bT = -1;
                                            }
                                            if (v(65568)) {
                                                bT = -1;
                                            }
                                        } else if (u == 10) {
                                            if (v(65568)) {
                                                D(v + 1);
                                                cz = true;
                                                z(23);
                                            }
                                        } else if (!v(65568) || u == 8) {
                                            if (u == 8) {
                                                int i6 = x;
                                                x = i6 - 1;
                                                if (i6 <= 0) {
                                                    x = 48;
                                                    D(v + 1);
                                                    if (v(65568)) {
                                                        z(23);
                                                    }
                                                }
                                            }
                                        }
                                        if (v == w) {
                                            if (u != 1) {
                                                if (u != 3) {
                                                    if (u != 9) {
                                                        if (u == 8) {
                                                            cz = true;
                                                        }
                                                        l(8);
                                                        break;
                                                    } else {
                                                        if (C != null) {
                                                            C.Z();
                                                        }
                                                        l(8);
                                                        break;
                                                    }
                                                } else if (aj != 7) {
                                                    l(15);
                                                    break;
                                                } else {
                                                    l(24);
                                                    break;
                                                }
                                            } else if (aj != 8) {
                                                l(8);
                                                break;
                                            } else {
                                                aj = 0;
                                                W();
                                                l(2);
                                                break;
                                            }
                                        }
                                    } else {
                                        C.Z();
                                        C.cd[1] = true;
                                        if (bh[aj] != 3) {
                                            m(ad);
                                        }
                                        z(23);
                                        l(8);
                                        v = w;
                                        break;
                                    }
                                    break;
                            }
                        }
                        if ((aS.P & 512) != 0 || ((C == null || !C.ab()) && (j.c != 21 || u != 9))) {
                            dg++;
                            int[] iArr = ap;
                            iArr[2] = iArr[2] + 1;
                        }
                        if (fS >= 0) {
                            String strD = d(0, 111);
                            if (j.g % 2 == 0) {
                                fS++;
                            }
                            if (fS < strD.length()) {
                                strD = strD.substring(0, fS);
                            } else if (fS >= strD.length() + 10) {
                                fS = -1;
                            }
                            y.l(0);
                            y.a(cd, strD, 390, 40, 10);
                        }
                        if (J()) {
                            if (fL == null) {
                                fL = new a(A[2], 377, 19);
                            }
                            if (d(354, 0, 46, 37)) {
                                a(359, 32, 36, true);
                                fL.a(30, 1);
                            } else {
                                a(359, 32, 36, false);
                                fL.a(25, -1);
                            }
                            fL.b(j.f);
                            fL.c();
                            if (c(354, 0, 46, 37)) {
                                E(262144);
                            }
                            if (v(262144)) {
                                if (C != null) {
                                    C.Y();
                                }
                                bw = 0;
                                l(14);
                                break;
                            }
                        }
                    }
                    break;
                case 9:
                    N();
                    if (G(j.g) && (w(65568) || j())) {
                        bG = 0;
                        dl = null;
                        A[5] = null;
                        A[1] = null;
                        a(bA, 16, (short) 0);
                        ax = dB;
                        ay = dC;
                        aN = dF;
                        g.e(ax);
                        C();
                        T();
                        dz = 120;
                        aw = 0;
                        if (ef[aj]) {
                            ab();
                        }
                        l(8);
                        z(23);
                        F(aj);
                    }
                    if (g.w == null) {
                        g.w = new int[4];
                        if (z[12] != null) {
                            z[12].a(8, 0, 0, g.w, 0, true);
                            int[] iArr2 = g.w;
                            iArr2[0] = iArr2[0] + 370;
                            int[] iArr3 = g.w;
                            iArr3[1] = iArr3[1] + 210;
                        }
                    }
                    break;
                case 10:
                    ag();
                    break;
                case 11:
                    j.c = -1;
                    return;
                case 12:
                case 13:
                    if (!j.i()) {
                        b(true);
                        eG = 0;
                        b(93, 67, 214, true, true);
                        bW.l(1);
                        bW.a(cd, d(0, eC), bW.a(d(0, eC), 180, false), 200, 93, 0, 100, 3, -1);
                        L(ey);
                        Q();
                        break;
                    } else {
                        j.t = 0;
                        break;
                    }
                case 14:
                    b(true);
                    if (bv != 1) {
                        eG = 0;
                    }
                    if (bv == 3) {
                        b(93, 67, 214, true, true);
                        bW.l(1);
                        bW.a(cd, d(0, eC), a(bW, d(0, eC), 180), 200, 93, 0, 100, 3, -1);
                    } else if (bv == 4) {
                        b(93, 86, 214, true);
                    } else {
                        b(93, 30, 214, true);
                    }
                    a(bv == 2 ? d(0, 16) : d(0, 79), d(0, 17));
                    L(ey);
                    Q();
                    break;
                case 15:
                    M();
                    break;
                case 17:
                    b(false);
                    break;
                case 18:
                    A[1].a(cd, 1, 0, 0, 0, 0, 0, 0);
                    A[0].a(cd, 0, 0, 0, 0, 0, 0, 0);
                    A[1].a(cd, 2, 0, 0, 0, 0, 0, 0);
                    if (!cS) {
                        bW.l(0);
                        if (j.g % 10 > 5) {
                            y.l(0);
                            y.a(cd, d(0, 9), 200, 205, 17);
                        }
                        if (v(65568) || j()) {
                            cT = 100;
                            cS = true;
                            z(23);
                        }
                        break;
                    } else {
                        cb = true;
                        cT -= 10;
                        try {
                            if (!e.a()) {
                                z(0);
                            }
                        } catch (Exception unused) {
                        }
                        l(2);
                        A[0] = null;
                        cT = 0;
                        break;
                    }
                    break;
                case 19:
                    f(false);
                    ey = da;
                    d(14, 47, 180);
                    a(d(0, 79), d(0, 17));
                    if (v(327712)) {
                        if (bw == -1) {
                            bw = 0;
                        }
                        aj = bw;
                        a(bA, 16, (short) 0);
                        if (!eg[aj]) {
                            break;
                        } else {
                            fF = 19;
                            l(30);
                            z(23);
                            break;
                        }
                    } else if (v(131072)) {
                        l(2);
                        z(30);
                        break;
                    } else if (v(16388)) {
                        L(da);
                        aj = bw;
                        break;
                    } else if (v(33024)) {
                        L(da);
                        aj = bw;
                    }
                case 20:
                    cb = true;
                    f(false);
                    String str = fb;
                    switch (cu) {
                        case 0:
                            cT = 10;
                            cu = 1;
                            break;
                        case 1:
                            z[39].a(cd, 1, 0, 200, 80, 0, 0, 0);
                            int i7 = cT + 10;
                            cT = i7;
                            if (i7 >= 255 || v(262144)) {
                                cu = 2;
                                eY = 200;
                                eZ = 85;
                                fc = 0;
                                fa = new StringBuffer(str.length());
                                str = "";
                                if (v(262144)) {
                                    z(23);
                                    break;
                                }
                            }
                            break;
                        case 2:
                            char[] cArr = new char[1];
                            str.getChars(fc, fc + 1, cArr, 0);
                            fc++;
                            fa.append(cArr);
                            char c = cArr[0];
                            if (c == 1 || c == 2 || c == '\\') {
                                fa.append(str.charAt(fc));
                                fc++;
                            }
                            if (fc >= str.length() - 1 || v(262144)) {
                                cu = 3;
                                eY = 200;
                                if (v(262144)) {
                                    z(23);
                                }
                            }
                            str = new String(fa);
                            break;
                        case 3:
                            int i8 = eY - 4;
                            eY = i8;
                            if (i8 <= 100 || v(262144)) {
                                cu = 4;
                                eY = 100;
                                cT = 10;
                                if (v(262144)) {
                                    z(23);
                                    break;
                                }
                            }
                            break;
                        case 4:
                            z[39].a(cd, 10, 0, 300, 80, 0, 0, 0);
                            int i9 = cT + 10;
                            cT = i9;
                            if (i9 >= 255 || v(262144)) {
                                cu = 5;
                                fd = eZ;
                                a(y, 0, str, 5, 85, 390, 120, 0, 0, false);
                                if (v(262144)) {
                                    z(23);
                                    break;
                                }
                            }
                            break;
                        case 5:
                            z[39].a(cd, 10, 0, 300, 80, 0, 0, 0);
                            a(y, 0, str, 5, 85, 390, 120, 0, 0, false);
                            break;
                    }
                    if (cu >= 2) {
                        j.a(cd, 0, 0, 400, 240, true);
                        z[39].a(cd, 1, 0, eY, 80, 0, 0, 0);
                        j.a(cd, 0, 85, 400, 120, true);
                        if (cu <= 4) {
                            y.a(str, (char[]) null);
                            if (b.e > 120) {
                                eZ = 85 - (b.e - 120);
                            }
                            y.f = false;
                            y.l(0);
                            y.a(cd, str, 5, eZ, 0);
                        }
                    }
                    j.a(cd, 0, 0, 400, 240, true);
                    a(d(0, 16), d(0, 18));
                    if (v(131072) || (v(262144) && cu == 5)) {
                        y.f = false;
                        l(9);
                        z(23);
                    }
                    break;
                case 22:
                    ah();
                    break;
                case 23:
                    if (!v(327712)) {
                        ae();
                        break;
                    } else {
                        if (bw == 0) {
                            bE = true;
                            bF = true;
                            z(0);
                        } else if (bw == 1) {
                            bE = false;
                            bF = false;
                        }
                        l(18);
                        break;
                    }
                case 24:
                    j.a(cd, 0, 0, 400, 240, true);
                    if (dz >= 120) {
                        if (dw >= 160) {
                            int i10 = (dw - 30) - 130;
                            int i11 = i10;
                            if (i10 > 255) {
                                i11 = 255;
                            }
                            cd.setColor((i11 << 24) | (i11 << 16) | (i11 << 8) | i11);
                        } else {
                            cd.setColor(0);
                        }
                        j.b(cd, 0, 0, 400, 240);
                        bW.l(0);
                        if (dw > 0) {
                            if (dw <= 10) {
                                bW.a(cd, d(0, 28), 200, 120, 3, 120 + ((100 * (10 - dw)) / 10));
                                dw++;
                            } else if (dw <= 20) {
                                bW.a(cd, d(0, 28), 200, 120, 3);
                                dw++;
                            } else if (dw < 30) {
                                b(bW, 0, dy, 200, 33, 380, 205, 0, 1);
                            } else if (dw > 415) {
                                dy = null;
                                dz = 0;
                                l(25);
                            } else if (dw >= 30) {
                                if (dw >= 160) {
                                    dw += 20;
                                    fe = 0;
                                    fd++;
                                } else {
                                    dw++;
                                }
                                b(bW, 0, dy, 200, 33, 380, 205, 0, 1);
                            }
                            j.a(cd, 0, 0, 400, 240, true);
                            a((String) null, d(0, 18));
                            if (v(131072)) {
                                if (dw < 160) {
                                    dw = 160;
                                }
                                z(23);
                            }
                            break;
                        } else {
                            dw = 1;
                            break;
                        }
                    } else {
                        dz += 20;
                        cd.setColor(0);
                        j.b(cd, 0, 0, 400, dz);
                        j.b(cd, 0, 240 - dz, 400, dz);
                        dw = 0;
                        fd = 110;
                        dy = null;
                        dy = new StringBuffer().append(d(0, 28)).append("\n\n\n\n\n\n\n\n\n\n\n").append(d(0, 55)).toString();
                        break;
                    }
                case 25:
                    j.a(cd, 0, 0, 400, 240, true);
                    if (!dx) {
                        dx = true;
                        l(6);
                        z(0);
                        break;
                    } else if (!Z()) {
                        l(2);
                        break;
                    } else {
                        int i12 = (dw << 24) | (dw << 16) | (dw << 8) | dw;
                        if (dw <= 0) {
                            i12 = 0;
                        } else {
                            dw -= 20;
                        }
                        cd.setColor(i12);
                        j.b(cd, 0, 0, 400, 240);
                        a(bW, 0, d(0, 66), 200, 80, 400, 220, 0, 3);
                        a(bW, 0, d(0, 9), 200, 160, 400, 260, 0, 3);
                        if (v(65568) || j()) {
                            f.a(d(0, 24), 0);
                            l(27);
                            if (!eJ) {
                                eJ = true;
                                a(bA, 10, 1);
                                e(true);
                            }
                            z(23);
                        }
                        break;
                    }
                    break;
                case 27:
                    if (cv == null) {
                        Image imageCreateImage = Image.createImage(400, 240);
                        cv = imageCreateImage;
                        cw = imageCreateImage.getGraphics();
                    }
                    cd = cw;
                    if (f.a(0)) {
                        l(2);
                        cv = null;
                        cw = null;
                    }
                    f.a(cd);
                    break;
                case 28:
                    ae();
                    break;
                case 29:
                    int i13 = bA[69] != 0 ? 3 : 2;
                    f(false);
                    d(0, 71);
                    d(93, 86, 214);
                    a((String) null, (bv == 0 || bv == 3) ? "" : d(0, 17));
                    L(i13);
                    Q();
                    break;
                case 30:
                    af();
                    break;
                case 31:
                    if (bx >= 0) {
                        if (!j.i()) {
                            b(true);
                            j.h(-856756498);
                            j.d(cd, 93, 67, 214, 126);
                            j.h(-2013265920);
                            j.d(cd, 91, 65, 218, 130);
                            bW.l(1);
                            bW.a(cd, d(0, bx), bW.a(d(0, bx), 180, false), 200, 130, 0, 100, 3, -1);
                            if (v(65568) || j()) {
                                l(13);
                                bx = -1;
                            }
                            if (j.g % 6 == 0) {
                                y.a(cd, d(0, 9), 200, 173, 17);
                            }
                            break;
                        } else {
                            j.t = 0;
                            break;
                        }
                    } else {
                        l(13);
                        break;
                    }
                    break;
            }
            if (eU) {
                j.a(cd, 0, 0, 400, 240, true);
                cd.setColor(0);
                j.b(cd, 0, 0, 80, 20);
                cd.setColor(-1);
                long jCurrentTimeMillis = System.currentTimeMillis() - cx;
                cx = System.currentTimeMillis();
                if (jCurrentTimeMillis <= 0) {
                    jCurrentTimeMillis = 1;
                }
                long j = 1000 / jCurrentTimeMillis;
                if (y != null) {
                    y.l(0);
                    y.a(cd, new StringBuffer().append("FPS=").append(j).toString(), 0, 0, 20);
                } else {
                    cd.drawString(new StringBuffer().append("FPS=").append(j).toString(), 0, 0, 20);
                }
            }
            if (eV) {
                j.a(cd, 0, 0, 400, 240, true);
                cd.setColor(0);
                j.b(cd, 200, 0, 200, 20);
                cd.setColor(-1);
                Runtime runtime = Runtime.getRuntime();
                if (y != null) {
                    y.a(cd, new StringBuffer().append("T=").append(runtime.totalMemory() / 1024).append("K,F=").append(runtime.freeMemory() / 1024).append("K").toString(), 200, 0, 20);
                } else {
                    cd.drawString(new StringBuffer().append("T=").append(runtime.totalMemory() / 1024).append("K,F=").append(runtime.freeMemory() / 1024).append("K").toString(), 240, 0, 20);
                }
            }
            J = cj;
            K = ck;
            if (cl) {
                cj = -1;
                ck = -1;
                cl = false;
            }
            H = ch;
            I = ci;
            ch = -1;
            ci = -1;
            if (c(0, 0, 60, 60)) {
                cr++;
            }
            if (c(0, 0, 400, 240) && !d(0, 0, 60, 60)) {
                cr = 0;
            }
            if (cr >= 20) {
                cs = true;
            }
            if (cs) {
                cr = 0;
                if (c(70, 100, 60, 60)) {
                    g.v = !g.v;
                }
                if (c(140, 100, 60, 60)) {
                    g.s = !g.s;
                }
                if (c(210, 100, 60, 60)) {
                    eU = !eU;
                    eV = !eV;
                }
                if (c(280, 100, 60, 60)) {
                    dt = !dt;
                }
                if (c(340, 0, 60, 60)) {
                    cs = false;
                }
            }
            if (cs) {
                int clipX = cd.getClipX();
                int clipY = cd.getClipY();
                int clipWidth = cd.getClipWidth();
                int clipHeight = cd.getClipHeight();
                j.a(cd, 0, 0, 400, 240, true);
                cd.setColor(65280);
                j.b(cd, 70, 100, 60, 60);
                j.b(cd, 140, 100, 60, 60);
                j.b(cd, 210, 100, 60, 60);
                j.b(cd, 280, 100, 60, 60);
                cd.setColor(0);
                if (y != null) {
                    int iB = y.b();
                    if (g.v) {
                        y.l(3);
                        y.a(cd, "Free fly", 70, 100, 20);
                    } else {
                        y.l(1);
                        y.a(cd, "Free fly", 70, 100, 20);
                    }
                    if (g.s) {
                        y.l(3);
                        y.a(cd, "God Mode", 140, 100, 20);
                    } else {
                        y.l(1);
                        y.a(cd, "God Mode", 140, 100, 20);
                    }
                    if (eU && eV) {
                        y.l(3);
                        y.a(cd, "FPS OPEN", 210, 100, 20);
                    } else {
                        y.l(1);
                        y.a(cd, "FPS close", 210, 100, 20);
                    }
                    if (dt) {
                        y.l(3);
                        y.a(cd, "Open all level!", 280, 100, 20);
                    } else {
                        y.l(1);
                        y.a(cd, "Open all level!", 280, 100, 20);
                    }
                    y.l(iB);
                    j.a(cd, clipX, clipY, clipWidth, clipHeight, true);
                }
            }
            if (bB != 0) {
                eO = bB;
                eP = 0;
            }
            eP++;
            if (bC != eL) {
                bD = -1;
            }
            if (bC != 0) {
                bD++;
            }
            bC = eL;
            bB = eK;
            eM = eN;
            eK = 0;
            eN = 0;
            if (cv != null) {
                j.a.setClip(0, 0, 240, 400);
                j.a.drawRegion(cv, 0, 0, 400, 240, 5, 0, 0, 20);
            }
        } catch (Exception e) {
            new StringBuffer().append("ERROR during paint : ").append(e).toString();
        }
    }

    private static void B() {
        if (aJ == 1) {
            z(9);
        } else if (ee[aj] != -1) {
            z(ee[aj]);
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:17:0x0066, code lost:
    
        defpackage.k.eC = 25;
        K(3);
        defpackage.k.eB = 59;
        z(7);
     */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static void l(int i) {
        boolean z2;
        while (true) {
            eG = 0;
            ex = j.c;
            cZ = 0;
            cb = true;
            cu = 0;
            fd = -1;
            fe = 0;
            if (i == 27) {
                e.b();
            }
            if (i == 9) {
                fO = 0;
                ac();
                ad();
                e.b();
                L();
            } else if (i == 12 || i == 13) {
                b(true);
                aD = null;
                if (i == 13 && bx >= 0) {
                    i = 31;
                }
            } else if (i == 15) {
                j.g = 0;
                eE = 0;
                eF = 37;
                if (ex != 10 && ex != 22) {
                    e.b();
                    z(6);
                }
                if (cc != null && cc.length == 3) {
                    for (int i2 = 0; i2 < 3; i2++) {
                        switch (i2) {
                            case 0:
                                if (ap[0] < 7 || cc[i2] != 0) {
                                    break;
                                } else {
                                    cc[i2] = 1;
                                    break;
                                }
                                break;
                            case 1:
                                if (au != 2 || cc[i2] != 0) {
                                    break;
                                } else {
                                    cc[i2] = 1;
                                    break;
                                }
                                break;
                            case 2:
                                if (ap[0] <= 1 || au != 2 || aj != 1 || ap[0] < 28 || cc[i2] != 0) {
                                    break;
                                } else {
                                    cc[i2] = 1;
                                    break;
                                }
                                break;
                        }
                        j.a(bA, i2 + 130, (byte) cc[i2]);
                    }
                }
                int i3 = 0;
                while (true) {
                    if (i3 >= 3) {
                        z2 = false;
                    } else if (cc[i3] == 1) {
                        z2 = true;
                    } else {
                        i3++;
                    }
                }
                if (z2) {
                    i = 22;
                } else {
                    boolean z3 = false;
                    int i4 = 1;
                    while (true) {
                        if (i4 < fP.length) {
                            if (aj + 1 == fP[i4]) {
                                z3 = true;
                            } else {
                                i4++;
                            }
                        }
                    }
                    boolean z4 = bA[15] == 1;
                    if (z3 && !z4 && ex != 10) {
                        i = 10;
                    }
                }
            } else if ((i == 8 || i == 21) && j.c == 9) {
                B();
            } else if (i == 8 && cy == 17) {
                i = 17;
            } else if (i == 29) {
                K(2);
            } else if (i == 30) {
                K(5);
                fO = 0;
            } else if (i == 4) {
                cU = au;
            } else if (i == 28) {
                K(3);
            } else if (i == 6) {
                if (dx) {
                    fd = 240;
                } else {
                    fd = 60;
                }
            } else if (i == 2) {
                fO = 0;
                E();
                K(0);
                dx = false;
                switch (j.c) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 18:
                    case 19:
                    case 20:
                    case 22:
                    case 28:
                    case 29:
                    case 30:
                        break;
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                    case 14:
                    case 15:
                    case 16:
                    case 17:
                    case 21:
                    case 23:
                    case 24:
                    case 25:
                    case 26:
                    case 27:
                    default:
                        e.b();
                        z(0);
                        break;
                }
            } else if (i == 14) {
                if (j.c == 8 || j.c == 21) {
                    b(true);
                }
                K(1);
                ao = false;
                an = false;
                if (!e.a()) {
                    fi = -1;
                }
                e.b();
            } else if (i == 23) {
                eC = 19;
                K(3);
                eB = 70;
                bw = -1;
            } else if (i == 5) {
                eE = 0;
                for (int i5 = 0; i5 < 4; i5++) {
                    cV[i5] = d(0, i5 + 47);
                    if (i5 == 1) {
                        cW = y.k(a(y, cV[i5], 261)[0]);
                        cV[i5] = new StringBuffer().append("\n\n").append(cV[i5]).append("\n\n\n").append(d(0, 98)).toString();
                    }
                    short s = a(y, cV[i5], 261)[0];
                    int iK = y.k(s);
                    if (iK > eE) {
                        eE = iK;
                    }
                    cZ += ((s + 8) - 1) / 8;
                    cX[i5] = ((s + 8) - 1) / 8;
                }
                eE = y.k(11);
                eF = 37 + eE;
            } else if (i == 20) {
                fb = y.a(d(0, 27), 390);
            }
        }
        al = false;
        if ((i == 21 && u != 8 && u != 9) || i == 13 || i == 12 || i == 17 || i == 16 || i == 31) {
            al = true;
        }
        if (i == 21 && u == 8) {
            cz = true;
        }
        j.g = 0;
        cy = j.c;
        j.c = i;
        if (!cz) {
            v();
        }
        cz = false;
    }

    private static void g(int i, int i2, int i3, int i4) {
        if (cd == null) {
            cd = j.a;
        }
        j.a(cd, i, i2, i3, i4, true);
    }

    private static void C() {
        P = 0;
        O = 0;
        cB = 0;
        cA = 0;
        cD = 0;
        cC = 0;
        Q = 0;
        Z = false;
        ab = false;
        if (bh[aj] != 3) {
            aR = -1;
            ak = 0;
            X = 0;
            n();
            m(ad);
            return;
        }
        dU = 0;
        dR = -1;
        ak = 0;
        aR = -1;
        dS = (-1) - 1;
        dT = (bu - 20) - (20 * aR);
        Q = 230;
        D();
        int i = aS.ak - 200;
        cA = i;
        O = i;
        int i2 = aS.al - 230;
        cB = i2;
        P = i2;
        W = 0;
        X = -7;
        V = -7;
        Y = X << 8;
    }

    public static void m(int i) {
        if (ah == null) {
            n();
        }
        if ((i & ad) != 0) {
            aS.t();
            ae = aS;
            i.aL = null;
            n();
            cF = 0;
            cE = 0;
            cM = 200;
        }
        if (ai) {
            return;
        }
        if (!Z) {
            if (ae == aS) {
                if (aS.c()) {
                    return;
                }
                if (!g.b(aS.S) && aS.S != 101 && aS.S != 92 && aS.S != 11) {
                    if (ae.av && ae.ag < 0) {
                        int i2 = cM + 20;
                        cM = i2;
                        if (i2 >= 266) {
                            cM = 266;
                        }
                    } else if (!ae.av && ae.ag > 0) {
                        int i3 = cM - 20;
                        cM = i3;
                        if (i3 <= 133) {
                            cM = 133;
                        }
                    }
                }
                if (g.g != null && (g.a == null || g.a.ax != 43)) {
                    cA = (aS.ak + ((g.g.ak - aS.ak) >> 1)) - 200;
                } else if (i.at != null && i.at.ax == 72) {
                    cA = (aS.ak + ((i.at.ak - aS.ak) >> 1)) - 200;
                } else if ((g.c == null || g.c.ax != 43) && (g.a == null || g.a.ax != 43)) {
                    if (ae == aS && (ae.S == 60 || ae.S == 61 || ae.S == 62 || ae.S == 148 || ae.S == 149 || ae.S == 150 || ae.S == 210 || ae.S == 59 || ae.S == 65 || ae.S == 258 || ae.S == 259 || ae.S == 260 || ae.S == 261 || ae.S == 262 || ae.S == 263 || ae.S == 264 || ae.S == 265 || ae.S == 266 || g.j || ((g.a != null && g.a.ax == 51) || (ae.S == 38 && ae.ac != null && ae.ac.ax == 22)))) {
                        cA = ae.ak - 200;
                    } else if (ae == aS && ae.S == 317) {
                        if (ae.av && ae.ag < 0) {
                            cM = 300;
                        } else if (!ae.av && ae.ag > 0) {
                            cM = 100;
                        }
                        cA = ae.ak - cM;
                    } else {
                        cA = ae.ak - cM;
                    }
                } else if (g.c != null) {
                    cA = g.c.ak - 200;
                } else if (g.a != null) {
                    cA = g.a.ak - 200;
                }
                if (((ae.aZ || (g.a != null && g.a.ax == 43)) && ae.S != 203 && ae.S != 204 && ae.S != 62) || (i & ad) != 0) {
                    cB = ae.al - 150;
                } else if (ae == aS && (ae.S == 28 || ae.S == 29 || ae.S == 315 || ae.S == 318)) {
                    cB = ae.al + 60;
                } else if ((ae == aS && (ae.S == 148 || ae.S == 149 || ae.S == 150 || ae.S == 210 || ae.S == 59 || ae.S == 65 || ae.S == 258 || ae.S == 259 || ae.S == 260 || ae.S == 261 || ae.S == 262 || ae.S == 263 || ae.S == 264 || ae.S == 265 || ae.S == 266 || g.j || (ae.S == 38 && ae.ac != null && ae.ac.ax == 22))) || ae != aS) {
                    cB = ae.al - 120;
                }
                if (ae.W[1] < cB + 40) {
                    cB = ae.W[1] - 40;
                } else if (ae.W[3] > (cB + 240) - 40) {
                    cB = (ae.W[3] + 40) - 240;
                }
                if (af != 0 || ag != 0) {
                    if (af != 0) {
                        cA = (ae.ak - 200) + af;
                    }
                    if (ag != 0) {
                        cB = (ae.al - 120) + ag;
                    }
                }
            } else if (ae != null) {
                if (ae.ax != 43 || (ae.S != 1 && ae.S != 4)) {
                    cA = ae.ak - 200;
                } else if (ae.av) {
                    if (i.b(aS.Y, ac)) {
                        if (ae.ak - O < 200 || (ae.ak - O < 300 && ae.ag == (ae.Z[1] << 8))) {
                            cC = (ae.Z[1] * 150) / 100;
                        } else if (ae.ak - O >= 300 && ae.ak - O < 350 && ae.ag == (ae.Z[1] << 8)) {
                            cC = ae.Z[1];
                        } else if (ae.ak - O >= 350 && Math.abs(ae.ag) >= (Math.abs(ae.Z[1] << 8) * 50) / 100) {
                            cC = (ae.Z[1] * 50) / 100;
                        }
                    } else if (ae.Y[2] > ac[2]) {
                        cC = (ae.Z[1] * 50) / 100;
                    } else {
                        cC = (ae.Z[1] * 150) / 100;
                    }
                } else if (i.b(aS.Y, ac)) {
                    if (ae.ak - O > 200 || (ae.ak - O > 100 && ae.ag == (ae.Z[1] << 8))) {
                        cC = (ae.Z[1] * 150) / 100;
                    } else if (ae.ak - O > 50 && ae.ak - O <= 100 && ae.ag == (ae.Z[1] << 8)) {
                        cC = ae.Z[1];
                    } else if (ae.ak - O <= 50 && ae.ag <= (ae.Z[1] << 8)) {
                        cC = (ae.Z[1] * 50) / 100;
                    }
                } else if (ae.Y[0] < ac[0]) {
                    cC = (ae.Z[1] * 50) / 100;
                } else {
                    cC = (ae.Z[1] * 150) / 100;
                }
                cB = ae.al - 120;
            }
            if (ah != null && ah.W != null && ah.aF == 1) {
                int i4 = ah.W[0];
                int i5 = ah.W[1];
                int i6 = ah.W[2];
                int i7 = ah.W[3];
                if (cA < i4) {
                    cA = i4;
                } else if (cA + 400 > i6) {
                    cA = i6 - 400;
                }
                if (cB < i5) {
                    cB = i5;
                } else if (cB + 240 > i7) {
                    cB = i7 - 240;
                }
            }
            if (cI == ae.N) {
                X = 20;
            } else if (X < 40) {
                X++;
            }
            cI = ae.N;
            if (R > 0 && cA < R) {
                cA = R;
            } else if (S > 0 && cA > S - 400) {
                cA = S - 400;
            }
            if (T > 0 && cB < T) {
                cB = T;
            } else if (U > 0 && cB > U - 240) {
                cB = U - 240;
            }
            int i8 = 1;
            if (i.aH && i.aI > 0) {
                i8 = i.aI;
            }
            if ((i & ad) != 0) {
                O = cA;
                P = cB;
                cC = 0;
                cD = 0;
                U = 0;
                S = 0;
                T = 0;
                R = 0;
            } else if (ae.ax == 43 && (ae.S == 1 || ae.S == 4)) {
                O += cC / i8;
                cD = l(cB - P, 28);
                P += cD / i8;
            } else {
                cC = l(cA - O, X);
                cD = l(cB - P, 28);
                O += cC / i8;
                P += cD / i8;
            }
            if (ab || ((g.c != null && g.c.ax == 43 && g.c.cd[3]) || (g.a != null && g.a.ax == 43 && g.a.cd != null && g.a.cd[3]))) {
                ab = false;
                O = cA;
            }
        }
        if (g.v) {
            O = ae.ak - 200;
            P = ae.al - 120;
        }
        if (O < 0) {
            O = 0;
        } else if (O > br - 400) {
            O = br - 400;
        }
        if (P < 0) {
            P = 0;
        } else if (P > bs - 240) {
            P = bs - 240;
        }
        if (cO > 0) {
            int i9 = cO;
            if ((cO & 1) != 0) {
                i9 = -i9;
            }
            boolean z2 = cP;
            P += i9;
            cO--;
        }
        if (av && cC == 0 && cD == 0 && i.ai()) {
            r();
        }
        ac[0] = O;
        ac[1] = P;
        ac[2] = O + 400;
        ac[3] = P + 240;
    }

    /* JADX WARN: Removed duplicated region for block: B:58:0x01ab  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static void D() {
        if ((C != null && ((C.cd[0] || C.ab()) && Z)) || j.c == 21) {
            cA = O;
            cB = P;
            ac[0] = O;
            ac[1] = P;
            ac[2] = O + 400;
            ac[3] = P + 240;
            return;
        }
        ae = aS;
        if (W != 0) {
            X = W;
            W = 0;
        }
        Y = X << 8;
        i iVar = ae;
        int i = iVar.W[0] / 20;
        int i2 = iVar.W[2] / 20;
        int i3 = iVar.al / 20;
        int i4 = br / 20;
        if (!ai) {
            int iG = 0;
            int iG2 = 0;
            while (true) {
                if ((i > 0 || iG == 22) && (i2 >= i4 || iG2 == 22)) {
                    break;
                }
                if (i > 0 && iG != 22) {
                    i--;
                    iG = g(i, i3);
                }
                if (i2 < i4 && iG2 != 22) {
                    i2++;
                    iG2 = g(i2, i3);
                }
            }
            R = i * 20;
            S = ((i2 + 1) * 20) - 400;
            if (R > S) {
                R = S;
            }
            cN = iVar.ak;
        } else if (R == -1) {
            R = -1;
            S = -1;
            if (i.bV > 0) {
                R = cG;
                S = cH;
            } else {
                for (int i5 = 0; i5 < i4; i5++) {
                    if (et[i5 + (i3 * bp)] == 22) {
                        if (R == -1) {
                            R = i5 * 20;
                        } else {
                            int i6 = (i5 + 1) * 20;
                            S = i6;
                            if (i6 - 400 < R) {
                                int i7 = R - ((R - (S - 400)) >> 1);
                                R = i7;
                                S = i7 + 400;
                            }
                            cG = R;
                            cH = S;
                        }
                    }
                }
                int iG3 = 0;
                int iG22 = 0;
                while (true) {
                    if (i > 0) {
                    }
                }
                R = i * 20;
                S = ((i2 + 1) * 20) - 400;
                if (R > S) {
                }
                cN = iVar.ak;
            }
        }
        if (ai) {
            cA = ((R + S) >> 1) - 200;
        } else if (!i.be) {
            int i8 = cN - 200;
            cA = i8;
            if (i8 < R) {
                cA = R;
            } else if (cA > S) {
                cA = S;
            }
        }
        cB += X;
        Q = ae.al - cB;
        if (!i.bj) {
            if (Q <= 117) {
                Q = 117;
                if (ae.ah < Y) {
                    ae.ah = Y;
                }
            } else if (Q >= 230) {
                Q = 230;
                if (ae.ah > Y) {
                    ae.ah = Y;
                }
            }
        }
        O += l(cA - O, 4);
        P += l(cB - P, 30);
        ac[0] = O;
        ac[1] = P;
        ac[2] = O + 400;
        ac[3] = P + 240;
    }

    private static int l(int i, int i2) {
        int i3 = i / 2;
        int i4 = i3;
        if (i3 > i2) {
            i4 = i2;
        } else if (i4 < (-i2)) {
            i4 = -i2;
        }
        return i4;
    }

    public static void a(i iVar) {
        ah = iVar;
    }

    public static void n() {
        ah = null;
        R = 0;
        S = 0;
        T = 0;
        U = 0;
    }

    static void n(int i) {
        if (i > 0) {
            cO = i;
            cP = true;
        } else {
            cO = -i;
            cP = false;
        }
    }

    private static void a(int i, int i2, int i3, boolean z2) {
        int i4;
        int i5;
        int i6;
        if (z2) {
            i4 = 41;
            i5 = 42;
        } else {
            i4 = 43;
            i5 = 44;
        }
        if (cQ == -1) {
            cQ = A[2].e(A[2].d(i4, 0));
        }
        if (cR == -1) {
            cR = A[2].e(A[2].d(i5, 0));
        }
        A[2].a(cd, i4, 0, i, i2, 0, 0, 0);
        int i7 = i + cQ;
        do {
            A[2].a(cd, i5, 0, i7, i2, 0, 0, 0);
            i6 = i7 + cR;
            i7 = i6;
        } while (i6 + cR < i + i3);
        A[2].a(cd, i5, 0, ((i + i3) - cQ) - cR, i2, 0, 0, 0);
        A[2].a(cd, i4, 0, i + i3, i2, 1, 0, 0);
    }

    private static void a(String str, String str2) {
        ce = -1;
        cf = -1;
        if (str != null && str != "" && str != d(0, 79)) {
            if (j.c != 21 && j.c != 8) {
                if (str.equals(d(0, 16))) {
                    y.a(str, (char[]) null);
                    ce = b.d + 30;
                } else {
                    ce = 36;
                }
                a(5, 235, ce, d(-5, 198, ce + 20, 47));
                if (str.equals(d(0, 16))) {
                    y.l(0);
                    y.a(cd, str, 5 + (ce >> 1), 222, 3);
                }
            }
            if (c(-5, 198, ce + 20, 47)) {
                E(262144);
            }
        }
        if (str2 == null || str2 == "") {
            return;
        }
        if (str2.equals(d(0, 18))) {
            y.a(str2, (char[]) null);
            cf = b.d + 30;
        } else {
            cf = 36;
        }
        boolean zD = d((395 - cf) - 10, 198, cf + 20, 47);
        a(395 - cf, 235, cf, zD);
        if (str2.equals(d(0, 18))) {
            y.l(0);
            y.a(cd, str2, 395 - (cf >> 1), 222, 3);
        } else {
            A[2].a(cd, zD ? 29 : 24, 0, 395 - (cf >> 1), 222, 0, 0, 0);
        }
        if (c((395 - cf) - 10, 198, cf + 20, 47)) {
            E(131072);
        }
    }

    private static void E() {
        try {
            if (A[1] == null) {
                j.a("/2");
                A[1] = J(1);
                A[1].a(0, 0, -1, -1);
                A[1].a(true);
                j.e();
            }
            U();
            A[1].a(dK, 1, 0, 0, 0, 0, 0, 0);
        } catch (Exception unused) {
        }
    }

    private static void a(int i, String str) {
        f(false);
        A[3].a(cd, 1, 0, 200, i, 0, 0, 0);
        A[3].a(cd, 2, 0, 120, i, 0, 0, 0);
        j.a.setColor(-14274509);
        j.b(j.a, 87, i + 9, 228, 183);
        bW.l(0);
        bW.a(cd, str, 200, i, 3);
    }

    private static void F() {
        cb = true;
        a(30, d(0, 5));
        bW.l(0);
        bW.a(cd, d(0, 35 + cU), 200, 55, 3);
        if (d(110, 15, 50, 80)) {
            A[2].a(cd, 40, 0, 160, 55, 0, 0, 0);
        } else {
            A[2].a(cd, 36, 0, 160, 55, 0, 0, 0);
        }
        if (d(240, 15, 50, 80)) {
            A[2].a(cd, 39, 0, 240, 55, 0, 0, 0);
        } else {
            A[2].a(cd, 35, 0, 240, 55, 0, 0, 0);
        }
        int iA = 0;
        for (int i = 0; i < 8; i++) {
            bW.a(cd, new StringBuffer().append(d(0, 10)).append(" ").append(bw + i + 1).toString(), 107, 75 + (i * 14), 20);
            short sA = a(bA, 81 + (cU << 4) + ((bw + i) << 1));
            if (sA <= 0) {
                bW.a(cd, "-", 293, 75 + (i * 14), 24);
            } else {
                bW.a(cd, j.c(sA, 0), 293, 75 + (i * 14), 24);
            }
        }
        for (int i2 = 0; i2 < 8; i2++) {
            iA += a(bA, 81 + (cU << 4) + (i2 << 1));
        }
        bW.a(cd, d(0, 23), 107, 197, 20);
        if (iA <= 0) {
            bW.a(cd, "-", 293, 197, 24);
        } else {
            bW.a(cd, j.c(iA, 0), 293, 197, 24);
        }
        a("", d(0, 17));
        if (v(16388)) {
            if (bw > 0) {
                bw--;
                z(23);
                return;
            }
            return;
        }
        if (v(33024)) {
            if (bw < 0) {
                bw++;
                z(23);
                return;
            }
            return;
        }
        if (v(8256) || c(240, 15, 50, 80)) {
            int i3 = cU + 1;
            cU = i3;
            cU = i3 % 3;
            z(23);
            return;
        }
        if (v(4112) || c(110, 15, 50, 80)) {
            int i4 = cU - 1;
            cU = i4;
            if (i4 < 0) {
                cU = 2;
            }
            z(23);
            return;
        }
        if (v(131072)) {
            l(3);
            K(4);
            z(30);
        }
    }

    private static void G() {
        cb = true;
        if (cy == 14) {
            b(true);
            j.h(-856756498);
            j.d(cd, 57, 10, 285, eF - (-10));
            j.a.setColor(-16777216);
            j.b(j.a, 57, 18, 285, 18);
            j.h(-2013265920);
            j.d(cd, 57, 8, 285, 2);
            j.d(cd, 57, (10 + eF) - (-10), 285, 2);
            j.d(cd, 55, 8, 2, (eF - (-10)) + 4);
            j.d(cd, 342, 8, 2, (eF - (-10)) + 4);
            bW.l(0);
            bW.a(cd, d(0, 6), 200, 20, 17);
            f(true);
        } else {
            f(false);
            d(0, 6);
        }
        int iK = 47 + ((eE - y.k(1)) / 2);
        j.a(cd, 0, 0, 400, 240, true);
        if (d(45, iK - 15, 50, 30)) {
            A[2].a(cd, 40, 0, 70, iK, 0, 0, 0);
        } else {
            A[2].a(cd, 36, 0, 70, iK, 0, 0, 0);
        }
        if (d(305, iK - 15, 50, 30)) {
            A[2].a(cd, 39, 0, 330, iK, 0, 0, 0);
        } else {
            A[2].a(cd, 35, 0, 330, iK, 0, 0, 0);
        }
        if (c(45, iK - 15, 50, 30)) {
            E(4112);
        }
        if (c(305, iK - 15, 50, 30)) {
            E(8256);
        }
        int iA = a(y, 1, cV[bw], 200, iK, 261, 240, 0, 3);
        if (bw == 1) {
            int iK2 = iK - (y.k(iA) / 2);
            z[11].a(cd, 17, 0, 200, iK2 + 20, 0, 0, 0);
            if (cy == 14) {
                z[54].a(cd, 0, 0, 200, iK2 + y.k(3) + cW, 0, 0, 0);
            } else {
                z[54].a(cd, 0, 0, 200, iK2 + y.k(3) + cW, 0, 0, 0);
            }
        }
        int i = 0;
        for (int i2 = 0; i2 < bw; i2++) {
            i += cX[i2];
        }
        y.a(cd, new StringBuffer().append(i + cY).append("/").append(cZ).toString(), 200, 220, 33);
        a("", d(0, 17));
        if (v(4112)) {
            if (cY > 1) {
                cY--;
            } else {
                bw = ((bw - 1) + 4) % 4;
                cY = cX[bw];
            }
            z(23);
        } else if (v(8256)) {
            if (cX[bw] > cY) {
                cY++;
            } else {
                cY = 1;
                int i3 = bw + 1;
                bw = i3;
                bw = i3 % 4;
            }
            z(23);
        }
        if (v(131072)) {
            cY = 1;
            l(cy);
            z(30);
        }
    }

    private static void d(i iVar) {
        int i = 0;
        while (i < be && bd[i].az < iVar.az) {
            i++;
        }
        while (i < be && bd[i].az == iVar.az && iVar.al > bd[i].al) {
            i++;
        }
        for (int i2 = be; i2 > i; i2--) {
            bd[i2] = bd[i2 - 1];
        }
        bd[i] = iVar;
        be++;
    }

    private static void H() {
        for (int i = 0; i < bc; i++) {
            i iVar = bb[i];
            if (iVar != null && iVar.ax == 24 && (iVar.S == 9 || iVar.S == 10 || iVar.S == 8)) {
                iVar.I();
            }
        }
    }

    private static void I() {
        Runtime.getRuntime().freeMemory();
        fh += j.f;
        if (de || am) {
            j.j(j.f);
        }
        if (bJ > 0) {
            bJ--;
            int i = ((fo << 24) & (-16777216)) | ((((fp * bJ) / 8) << 16) & 16711680) | ((((fq * bJ) / 8) << 8) & 65280) | (((fr * bJ) / 8) & 255);
            de = true;
            df = i;
        }
        i.bf = false;
        if (bh[aj] == 3) {
            int i2 = 0;
            for (int i3 = 0; i3 < bc; i3++) {
                if (bb[i3] != null) {
                    bb[i3].u();
                }
                if (bb[i3] != null && (bb[i3].P & 256) == 0 && ((bb[i3].au < 2 && (bb[i3].P & 32) == 0) || (bb[i3].P & 16) != 0)) {
                    if (ak == 0 && bb[i3].au < 1 && bb[i3].ay > 0) {
                        ak = bb[i3].ay;
                        for (int i4 = 0; i4 < bc; i4++) {
                            if (bb[i4] != null && bb[i4].ay == ak) {
                                dR = bb[i4].aG;
                                bb[i4].v();
                            }
                        }
                    }
                    if (bb[i3].ay == ak) {
                        bb[i3].ay = -1;
                    }
                    if (bb[i3].ay == -1) {
                        i2++;
                        bb[i3].I();
                        if (bb[i3] != null && bb[i3].ac != null && bb[i3].ac.ax != 10) {
                            bb[i3].ac.I();
                        }
                        if (bb[i3] != null && bb[i3].ab != null) {
                            bb[i3].ab.I();
                        }
                    }
                }
            }
            if (i2 == 0 && dU != 0) {
                int i5 = dU * 400;
                int i6 = cB + i5;
                cB = i6;
                P = i6;
                aS.al += i5;
                aS.b(true);
                dT += dU * 20;
                dU = 0;
                dR = -1;
                ak = 0;
            }
        } else {
            for (int i7 = 0; i7 < bc; i7++) {
                if (bb[i7] != null) {
                    bb[i7].u();
                }
                if (bb[i7] != null && (bb[i7].P & 256) == 0 && (((bb[i7].au < 2 && (bb[i7].P & 32) == 0) || (bb[i7].P & 16) != 0) && bb[i7].ax != 71)) {
                    if (bb[i7] != null && bb[i7].ag()) {
                        bb[i7].af();
                    }
                    bb[i7].I();
                    if (bb[i7] != null && bb[i7].ac != null) {
                        bb[i7].ac.I();
                    }
                    if (bb[i7] != null && bb[i7].ab != null) {
                        bb[i7].ab.I();
                    }
                }
            }
        }
        aS.I();
        if (aS.ac != null) {
            aS.ac.I();
        }
        if (aS.ab != null) {
            aS.ab.I();
        }
        if (aS.ad != null && aS.ad.ax == -999) {
            aS.ad.I();
        }
        if (N != null) {
            N.s();
            if (N.S == 54 && (k(H, I) || v(32))) {
                N.i(55);
                N.P &= -65;
                if (k(H, I)) {
                    E(32);
                }
            } else if (N.S == 55 && N.T == N.aa.b(55) - 2) {
                k(-1);
            } else if (k(J, K)) {
                N.P |= 64;
                N.q();
            } else if (N.S != 55) {
                N.i(54);
                N.P &= -65;
            }
        }
        if (bh[aj] == 3) {
            if (i.bW) {
                i.X();
            }
            D();
        } else {
            m(cJ);
        }
        if (aV != null && aV.Z[0] == 1 && j.c != 13 && j.c != 31 && aV.S != 4 && aV.S != 5) {
            switch (aV.w()) {
                case 1:
                    if (j.f % 2 == 0) {
                        z[9].a(cd, 38, 0, 360, 120, 0, 0, 0);
                        break;
                    }
                    break;
                case 2:
                    bx = 56;
                    l(13);
                    bw = 0;
                    break;
            }
        }
        if (C != null && C.cd[2] && C.cd[1]) {
            while (C != null && C.ab()) {
                C.aa();
            }
            if (bh[aj] == 3) {
                cA = O;
                cB = P;
            }
        }
    }

    private static boolean J() {
        return (j.c == 12 || j.c == 13) ? false : true;
    }

    public static void o() {
        am = true;
        dd = false;
    }

    public static void p() {
        if (am) {
            am = false;
            dd = false;
            j.b(0, false);
            j.i(0);
        }
    }

    private static void K() {
        j.a(0, 0, 400, 240);
        j.a(cd, -1, -1, 1, 1, true);
        j.a(4, false);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r11v0, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    private static void b(boolean z2) {
        Graphics graphics;
        int i;
        if (z2 == 0) {
            try {
                if (j.c == 12 || j.c == 13 || j.c == 31) {
                    return;
                }
            } catch (Exception e) {
                z2.printStackTrace();
                return;
            }
        }
        if (am && !dd) {
            dd = true;
            j.a(0, 0, 400, 240);
            j.a(0, 100, 1);
            j.a(cd, -1, -1, 1, 1, true);
            j.a(0, false);
        }
        int i2 = (O * (bt < 21 ? 0 : bt - 21)) / (bp - 21);
        int i3 = (P * (bu - 13)) / (bq - 13);
        if (i3 < 0) {
            i3 -= 20;
        }
        int i4 = i2 / 20;
        int i5 = i4;
        int i6 = i4;
        int i7 = ((i2 + 400) - 1) / 20;
        int i8 = i7;
        int i9 = i3 / 20;
        int i10 = ((i3 + 240) - 1) / 20;
        if (i3 < 0) {
            i3 += 20;
        }
        if (i5 < 0) {
            i5 = 0;
            i6 = 0;
        } else if (i5 > bt - 1) {
            i5 = bt - 1;
        }
        if (i9 < 0 && bh[aj] != 3) {
            i9 = 0;
        } else if (i9 > bu - 1) {
            i9 = bu - 1;
        }
        if (!dM && (i5 != dN || i7 != dP)) {
            if (i7 < dN || i5 > dP) {
                dM = true;
            } else if (i5 < dN) {
                i6 = dN;
                h(i5, i9, dN - 1, i10);
            } else if (i7 > dP) {
                i8 = dP;
                h(dP + 1, i9, i7, i10);
            }
        }
        if (!dM && (i9 != dO || i10 != dQ)) {
            if (i10 < dO || i9 > dQ) {
                dM = true;
            } else if (i9 < dO) {
                h(i6, i9, i8, dO - 1);
            } else if (i10 > dQ) {
                h(i6, dQ + 1, i8, i10);
            }
        }
        if (dM) {
            dM = false;
            h(i5, i9, i7, i10);
        }
        dN = i5;
        dO = i9;
        dP = i7;
        dQ = i10;
        int i11 = i3;
        if (i11 < 0) {
            i11 = (i11 % 260) + 260;
        }
        int i12 = i2 % 420;
        int i13 = i11 % 260;
        int i14 = (i2 + 400) % 420;
        int i15 = (i11 + 240) % 260;
        if (i14 > i12) {
            if (i15 > i13) {
                d(i12, i13, 400, 240, 0, 0);
            } else {
                d(i12, i13, 400, 240 - i15, 0, 0);
                d(i12, 0, 400, i15, 0, 240 - i15);
            }
        } else if (i15 > i13) {
            d(i12, i13, 400 - i14, 240, 0, 0);
            d(0, i13, i14, 240, 400 - i14, 0);
        } else {
            d(i12, i13, 400 - i14, 240 - i15, 0, 0);
            d(i12, 0, 400 - i14, i15, 0, 240 - i15);
            d(0, i13, i14, 240 - i15, 400 - i14, 0);
            d(0, 0, i14, i15, 400 - i14, 240 - i15);
        }
        if (ef[aj]) {
            j.a(cd, 0, 0, 400, 240, true);
            for (int i16 = 0; i16 < 2; i16++) {
                int[] iArr = ft;
                int i17 = i16 << 1;
                iArr[i17] = iArr[i17] - 2;
                int[] iArr2 = fu;
                int i18 = i16 << 1;
                iArr2[i18] = iArr2[i18] - 3;
                if (ft[i16 << 1] <= (-fv)) {
                    if (i16 == 0) {
                        ft[0] = ft[2] + fv;
                    } else if (i16 == 1) {
                        ft[2] = ft[0] + fv;
                    }
                }
                if (fu[i16 << 1] <= (-fw)) {
                    if (i16 == 0) {
                        fu[0] = fu[2] + fw;
                    } else if (i16 == 1) {
                        fu[2] = fu[0] + fw;
                    }
                }
            }
            for (int i19 = 0; i19 < 2; i19++) {
                if (ft[i19 << 1] < 400 && ft[i19 << 1] + fv > 0) {
                    z[58].a(cd, 0, 0, ft[i19 << 1], ft[(i19 << 1) + 1], 0, 0, 0);
                }
            }
            for (int i20 = 0; i20 < 2; i20++) {
                if (fu[i20 << 1] < 400 && fu[i20 << 1] + fw > 0) {
                    z[58].a(cd, 1, 0, fu[i20 << 1], fu[(i20 << 1) + 1], 0, 0, 0);
                }
            }
        }
        if (bh[aj] == 4) {
            e(cd, O, P, 400, 240);
            f(cd, O, P, 400, 240);
        } else if (bh[aj] != 3) {
            e(cd, O, P, 400, 240);
        } else if (P > 0) {
            f(cd, O, P, 400, 240);
        }
        if (am) {
            j.a(cd, 0, 0, 400, 240, true);
            j.f(cd);
        }
        if (dc) {
            int i21 = O;
            int i22 = P;
            int i23 = i21 / 20;
            int i24 = i22 / 20;
            int i25 = ((i21 + 400) - 1) / 20;
            int i26 = ((i22 + 240) - 1) / 20;
            g(0, 0, 400, 240);
            int i27 = (i23 * 20) - i21;
            for (int i28 = i23; i28 <= i25; i28++) {
                int i29 = (i24 * 20) - i22;
                for (int i30 = i24; i30 <= i26; i30++) {
                    if (i28 + (i30 * bp) <= et.length) {
                        byte b = et[i28 + (i30 * bp)];
                        if (b != 0) {
                            cd.setColor(dV[b]);
                            j.c(cd, i27, i29, 19, 19);
                        }
                        i29 += 20;
                    }
                }
                i27 += 20;
            }
        }
        if (i.bQ > 0) {
            if (i.bQ % 4 <= 2) {
                cd.setColor(-1);
            } else {
                cd.setColor(-65536);
            }
            j.b(cd, 0, 0, 400, 240);
            i.bQ--;
        } else if (i.ce) {
            cd.setColor(-1);
            j.b(cd, 0, 0, 400, 240);
        }
        g(0, 0, 400, 240);
        be = 0;
        for (int i31 = 0; i31 < bc; i31++) {
            if (bb[i31] != null && ((bb[i31].P & 128) == 0 || bb[i31].ax == 10 || bb[i31].ax == 51)) {
                i iVar = bb[i31];
                if (iVar.aw == 205 && iVar.S == 34) {
                    d(iVar);
                } else if (iVar.v()) {
                    if (bh[aj] != 3 || bb[i31].ay == -1) {
                        if (iVar.ax == 14 && iVar.S == 38) {
                            iVar.az = 301;
                        }
                        d(iVar);
                        if (iVar.ae != null && (iVar.ae.P & 128) == 0) {
                            d(iVar.ae);
                            iVar.ae.s();
                        }
                    }
                } else if ((iVar.P & 16) != 0) {
                    if (iVar.ax == 15) {
                        if (iVar.S == 9 || iVar.S == 10) {
                            d(iVar);
                        }
                    } else if (iVar.ax == 9) {
                        if (iVar.S == 5) {
                            d(iVar);
                        }
                    } else if (iVar.ax == 14) {
                        if (iVar.S == 74) {
                            d(iVar);
                        }
                    } else if (iVar.ax == 66) {
                        d(iVar);
                    }
                }
            }
        }
        if ((aS.P & 128) == 0) {
            d(aS);
            if (aS.ae != null && (aS.ae.P & 128) == 0) {
                d(aS.ae);
                aS.ae.s();
            }
        }
        for (int i32 = 0; i32 < be; i32++) {
            i iVar2 = bd[i32];
            if (iVar2.ad != null && iVar2.ax != 76 && iVar2.ax != 29) {
                iVar2.ad.F();
            }
            if (iVar2.ax == 21 && iVar2.S == 1 && iVar2.ad != null) {
                if (C == null || u != 9) {
                    iVar2.ad.P &= -65;
                }
                iVar2.ad.s();
            }
            iVar2.F();
            if (iVar2.ad != null && (iVar2.ax == 76 || iVar2.ax == 29)) {
                iVar2.ad.F();
                iVar2.ad.s();
            }
            if (iVar2.ag()) {
                iVar2.ah();
            }
            if (iVar2.ax == 0 && E != null && (E.P & 128) == 0 && (j.c == 8 || (j.c == 21 && u == 8))) {
                E.F();
                E.s();
            }
            if (z2 == 0 && ((iVar2.ax != 11 && iVar2.ax != 17) || iVar2.aB > 0)) {
                iVar2.ad();
            }
            if (iVar2.ab != null && (iVar2.ab.P & 128) == 0 && iVar2.ab.v()) {
                iVar2.ab.F();
            }
            if (C == null || !C.ab() || !C.cd[2] || (iVar2.P & 512) != 0 || iVar2.ax == 0) {
                if (iVar2 != null && (iVar2.P & 32) == 0 && (iVar2.P & 128) == 0) {
                    if (iVar2.bl > 0) {
                        if (iVar2.ax != 73 && iVar2.ax != 11) {
                            cd.setColor(16777215);
                            j.c(cd, iVar2.ak - O, (iVar2.al - P) - 70, 42, 5);
                            cd.setColor(16711680);
                            j.b(cd, (iVar2.ak - O) + 1, (iVar2.al - P) - 70, (iVar2.bl * 40) / 10, 4);
                        }
                    } else if (!(iVar2.ax != 11 || iVar2.S == 24 || iVar2.S == 21 || iVar2.S == 0 || iVar2.S == 139 || iVar2.S == 133 || iVar2.S == 134 || iVar2.S == 145 || iVar2.S == 135 || iVar2.S == 106 || iVar2.S == 107 || !i.h(iVar2)) || ((iVar2.ax == 73 && i.h(iVar2)) || (iVar2.ax == 17 && i.h(iVar2) && iVar2.S != 69))) {
                        cd.setColor(16777215);
                        j.c(cd, (iVar2.ak - 20) - O, (iVar2.al - P) - 80, 41, 5);
                        int i33 = i.bu[au];
                        if (iVar2.ax == 17) {
                            i33 = i.bv[au];
                        }
                        if (iVar2.aB > (i33 >> 1)) {
                            graphics = cd;
                            i = 65280;
                        } else {
                            graphics = cd;
                            i = j.g % 2 == 0 ? 16711680 : 16777215;
                        }
                        graphics.setColor(i);
                        if (iVar2.Z[0] == 2 || iVar2.Z[0] == 1 || iVar2.ax == 73) {
                            j.b(cd, ((iVar2.ak - 20) - O) + 1, (iVar2.al - P) - 80, (iVar2.aB * 20) / i33, 4);
                        } else {
                            j.b(cd, ((iVar2.ak - 20) - O) + 1, (iVar2.al - P) - 80, (iVar2.aB * 40) / i33, 4);
                        }
                    } else if (iVar2.ax == 10 && iVar2.S == 32) {
                        int i34 = (iVar2.W[1] + iVar2.W[3]) >> 1;
                        if (aS.ac == iVar2) {
                            int i35 = aS.ak;
                            int i36 = aS.al;
                            cd.setColor(-3584205);
                            j.a(cd, iVar2.W[0] - O, i34 - P, i35 - O, i36 - P);
                            j.a(cd, iVar2.W[2] - O, i34 - P, i35 - O, i36 - P);
                            cd.setColor(-3584205);
                            j.a(cd, iVar2.W[0] - O, (i34 - P) + 1, i35 - O, (i36 - P) + 1);
                            j.a(cd, iVar2.W[2] - O, (i34 - P) + 1, i35 - O, (i36 - P) + 1);
                        } else {
                            cd.setColor(-3584205);
                            j.a(cd, iVar2.W[0] - O, i34 - P, iVar2.W[2] - O, i34 - P);
                            cd.setColor(-3584205);
                            j.a(cd, iVar2.W[0] - O, (i34 - P) + 1, iVar2.W[2] - O, (i34 - P) + 1);
                        }
                    } else if (iVar2.ax == 0) {
                        if (C != null && C.ab() && C.cd[9]) {
                            if (i.cg != null && i.ch != null) {
                                aS.a(i.cg, i.ch);
                            } else if (C.cd != null && C.cf != null) {
                                g.a(C.cf[0], C.cf[1], C.cf[2], C.cf[3], C.cf[4]);
                            }
                        } else if ((iVar2.S >= 272 && iVar2.S <= 277) || iVar2.S == 293 || iVar2.S == 298) {
                            aS.i();
                        }
                    }
                }
            }
            if (db && iVar2.W != null && iVar2.X != null) {
                int[] iArr3 = iVar2.W;
                int[] iArr4 = iVar2.X;
                int[] iArr5 = iVar2.Y;
                if (iArr3[0] != 0 || iArr3[1] != 0 || iArr3[2] != 0 || iArr3[3] != 0) {
                    cd.setColor(16777215);
                    j.c(cd, iArr3[0] - O, iArr3[1] - P, iArr3[2] - iArr3[0], iArr3[3] - iArr3[1]);
                }
                cd.setColor(0);
                if (bh[aj] == 3 && iVar2.ax == 25) {
                    j.b(cd, iArr3[0] - O, iArr3[1] - P, 30, 20);
                    cd.setColor(16711680);
                    cd.drawString(new StringBuffer().append(iVar2.ak).append("").toString(), (iArr3[0] - O) + 2, iArr3[1] - P, 0);
                    cd.drawString(new StringBuffer().append(iVar2.al).append("").toString(), (iArr3[0] - O) + 2, (iArr3[1] - P) + 10, 0);
                } else {
                    j.b(cd, iArr3[0] - O, iArr3[1] - P, 20, 12);
                    cd.setColor(-1);
                    cd.drawString(new StringBuffer().append("").append(iVar2.aw).toString(), (iArr3[0] - O) + 2, iArr3[1] - P, 0);
                    cd.setColor(-65536);
                    if (iVar2.ac() && iVar2.ca >= 0 && iVar2.ca < eH.length) {
                        cd.drawString(new StringBuffer().append("").append((int) eH[iVar2.ca]).toString(), (iArr3[0] - O) + 20, iArr3[1] - P, 0);
                        cd.drawString(new StringBuffer().append("").append(iVar2.ak).toString(), (iArr3[0] - O) + 2, (iArr3[1] - P) + 10, 0);
                        cd.drawString(new StringBuffer().append("").append(iVar2.al).toString(), (iArr3[0] - O) + 2, (iArr3[1] - P) + 20, 0);
                    }
                }
                if (iVar2.ad != null) {
                    int[] iArr6 = iVar2.ad.W;
                    cd.setColor(16777215);
                    j.c(cd, iArr6[0] - O, iArr6[1] - P, iArr6[2] - iArr6[0], iArr6[3] - iArr6[1]);
                }
                if (iArr4[0] != 0 || iArr4[1] != 0 || iArr4[2] != 0 || iArr4[3] != 0) {
                    cd.setColor(16711680);
                    j.c(cd, iArr4[0] - O, iArr4[1] - P, iArr4[2] - iArr4[0], iArr4[3] - iArr4[1]);
                }
                if (iArr5[0] != 0 || iArr5[1] != 0 || iArr5[2] != 0 || iArr5[3] != 0) {
                    cd.setColor(65280);
                    j.c(cd, iArr5[0] - O, iArr5[1] - P, iArr5[2] - iArr5[0], iArr5[3] - iArr5[1]);
                }
                if (iVar2.ax == 11 && iVar2.Z != null) {
                    cd.setColor(16711935);
                    j.c(cd, iVar2.Z[9] - O, iVar2.Z[11] - P, iVar2.Z[10] - iVar2.Z[9], iVar2.Z[12] - iVar2.Z[11]);
                }
                if ((iVar2.ax == 54 || iVar2.ax == 30) && iVar2.bt != null && iVar2.v()) {
                    for (int i37 = 0; i37 < 4; i37++) {
                        c cVarA = c.a(iVar2.Z[i37 + 1]);
                        if (cVarA == iVar2.bt) {
                            cd.setColor(16711680);
                        } else {
                            cd.setColor(65280);
                        }
                        if (cVarA != null) {
                            int i38 = iVar2.bY - O;
                            int i39 = iVar2.bZ;
                            int i40 = cVarA.a - O;
                            int i41 = cVarA.b;
                            j.a(cd, i38, i39, i40, i41);
                            j.c(cd, i40 - 5, i41 - 5, 10, 10, 0, 360);
                        }
                    }
                }
                if (iVar2.ax == 0 && iVar2.ac != null) {
                    int i42 = iVar2.ak - O;
                    int i43 = iVar2.al - P;
                    int i44 = iVar2.ac.ak - O;
                    int i45 = iVar2.ac.al - P;
                    cd.setColor(65280);
                    j.a(cd, i42, i43, i44, i45);
                }
                if (iVar2.ax == 0 && g.a != null) {
                    int i46 = iVar2.ak - O;
                    int i47 = iVar2.al - P;
                    int i48 = g.a.ak - O;
                    int i49 = g.a.al - P;
                    cd.setColor(255);
                    j.a(cd, i46, i47, i48, i49);
                }
                if (iVar2.ax == 0 && L != null) {
                    int i50 = iVar2.ak - O;
                    int i51 = iVar2.al - P;
                    int i52 = L.ak - O;
                    int i53 = L.al - P;
                    cd.setColor(16711935);
                    j.a(cd, i50, i51, i52, i53);
                }
            }
        }
        if (N != null) {
            N.F();
        }
        if (bJ > 0 && de) {
            j.a(cd, 0, 0, 400, 240, true);
            j.a(cd, df);
        }
        if (C != null && C.ab()) {
            if (C.cb != null && (C.cb[1] == 0 || C.cb[1] == 1 || C.cb[1] == 2 || C.cc != null)) {
                if (C.cb[1] == 0 || C.cb[1] == 1 || C.cb[1] == 2) {
                    a aVar = i.bA[0];
                    aVar.a = 200;
                    aVar.b = 160;
                    i.bA[0].b(j.f);
                    i.bA[0].c();
                } else if (C.cc != null) {
                    int i54 = 0;
                    while (i54 < C.cc[0]) {
                        if (i.bA[i54] != null) {
                            if (C.cc[0] == 3) {
                                a aVar2 = i.bA[i54];
                                aVar2.a = 200 + (50 * (i54 - 1));
                                aVar2.b = 160;
                            } else if (C.cc[0] == 2) {
                                a aVar3 = i.bA[i54];
                                aVar3.a = 200 + (50 * (i54 == 1 ? 1 : -1));
                                aVar3.b = 160;
                            } else {
                                a aVar4 = i.bA[i54];
                                aVar4.a = 200;
                                aVar4.b = 160;
                            }
                            i.bA[i54].b(j.f);
                            i.bA[i54].c();
                        }
                        i54++;
                    }
                }
            }
            if (C.cd[8] && C.cb[3] > 0) {
                bW.l(3);
                int[] iArr7 = C.cb;
                iArr7[3] = iArr7[3] - 1;
                if (C.cb[3] <= 0) {
                    C.cb[3] = 0;
                }
                if (C.cb[3] > 15) {
                    bW.a(cd, d(0, 91), 200, 120, 3, 100 + ((80 * (C.cb[3] - 15)) / 5));
                } else {
                    bW.a(cd, d(0, 91), 200, 120, 3);
                }
            }
        }
        if ((C != null && (!C.cd[6] || !C.ab())) || C == null) {
            c(z2);
            if (!g.g() && aD != null && aD.Z != null && aD.S == 6 && aD.Z[1] > 0) {
                z[12].a(cd, 18, 0, 110, 215, 0, 0, 0);
                j.a(cd, 125, 0, (120 * (aD.Z[1] - aD.Z[2])) / aD.Z[1], 240, true);
                z[12].a(cd, 19, 0, 110, 215, 0, 0, 0);
                j.a(cd, 0, 0, 400, 240, true);
            }
        }
        if (aQ != null) {
            cd.drawImage(aQ, 198 - aQ.getWidth(), 5, 20);
        }
        if (k() && z2 == 0 && j.c != 14 && j.c != 5 && ((j.c != 21 || u != 9) && (C == null || C.cb == null || C.cb[1] >= 0 || (aS.P & 512) != 0))) {
            int i55 = 0;
            if (bh[aj] == 3) {
                cn = 50;
            } else {
                cn = 5;
            }
            int iC = b(J, K, cn - 10, 124, 116, 116) ? c(J, K, (cn - 10) + 38, (cn - 10) + 77, 162, 201) : -1;
            if (iC != -1 && iC != 4) {
                i55 = iC + 1;
                if (iC > 4) {
                    i55--;
                }
            }
            z[74].a(cd, i55, cn, 134, 0, 0, 0);
            if (bh[aj] != 3) {
                z[74].a(cd, b(J, K, 270, 165, 70) ? 10 : 9, 270, 165, 0, 0, 0);
                z[74].a(cd, b(J, K, 320, 110, 70) ? 12 : 11, 320, 110, 0, 0, 0);
            }
        }
        if (z2 == 0 && C != null && ((C.ab() || u == 9) && C.cd[2])) {
            a("", d(0, 18));
        }
        if (an) {
            Graphics graphics2 = cd;
            if (bI < 0) {
                bI = 0;
            }
            if (bI <= 255 - fk) {
                bI += fk;
                aa();
            } else {
                graphics2.setColor(0);
                j.b(graphics2, 0, 0, 400, 240);
                an = false;
            }
        }
        if (ao) {
            Graphics graphics3 = cd;
            if (bI > 255) {
                bI = 255;
            }
            if (bI >= fk) {
                bI -= fk;
                aa();
            } else {
                ao = false;
            }
        }
        if (i.bh > 0 && j.c == 8) {
            int i56 = fs - 10;
            fs = i56;
            if (i56 <= 0) {
                fs = 80;
            }
            j.h((((255 * fs) / 100) << 24) | 16711680);
            j.d(cd, 5, 0, 390, 5);
            j.d(cd, 5, 235, 390, 5);
            j.d(cd, 0, 0, 5, 240);
            j.d(cd, 395, 0, 5, 240);
        }
        if (j.c != 14) {
            if (av) {
                if (dz < 120) {
                    dz += 20;
                }
            } else if (aw != dz) {
                int i57 = aw - dz;
                if (i57 <= -20 || i57 >= 20) {
                    dz += i57 > 0 ? 20 : -20;
                } else {
                    dz = aw;
                }
            }
            j.a(cd, 0, 0, 400, 240, true);
            if (dz > 0) {
                cd.setColor(0);
                j.b(cd, 0, 0, 400, dz);
                j.b(cd, 0, 240 - dz, 400, dz);
            }
        }
        if (i.bJ > 0) {
            if (i.bJ == i.bH) {
                int i58 = i.bL;
                i.bL = i58;
                if (i58 >= 0) {
                    i.bJ = i.bI;
                }
            } else if (i.bJ == i.bI) {
                int i59 = i.bL;
                i.bL = i59;
                if (i59 <= 20) {
                    i.bJ = 0;
                    return;
                }
            }
            y.l(0);
            y.a(cd, null, a(y, (String) null, 320), 200, 50, 0, 4, 17, -1);
        }
        Graphics graphics4 = cd;
        if (aU == null || (aU.P & 32) != 0 || i.by <= 0) {
            return;
        }
        graphics4.setColor(16777215);
        j.c(graphics4, 120, 215, 125, 11);
        int i60 = (125 * aU.aB) / 800;
        if (aU.aB > 300 || j.g % 3 == 0) {
            graphics4.setColor(16711680);
        } else {
            graphics4.setColor(16763904);
        }
        j.b(graphics4, 121, 215, i60 - 1, 10);
    }

    public static void o(int i) {
        if (i == 3 && aj == 7) {
            return;
        }
        int[] iArr = ap;
        iArr[i] = iArr[i] + 1;
    }

    public static void e(int i, int i2) {
        if (i2 <= 0 || aj == 7) {
            return;
        }
        int[] iArr = ap;
        iArr[0] = iArr[0] + 1;
    }

    private static void L() {
        dg = 0L;
        for (int i = 0; i < 6; i++) {
            ap[i] = 0;
        }
    }

    private static void M() throws InterruptedException {
        int i;
        if (j.g == 1) {
            W();
            ac();
            ad();
            E();
            if (ap[5] > a(bA, 52 + (aj << 1))) {
                a(bA, 52 + (aj << 1), (short) ap[5]);
            }
        }
        int i2 = 25 + ((177 - eF) / 2);
        if (cb) {
            a(i2, d(0, 60));
        }
        if (eE < 140) {
            eE += 10;
            eF = 37 + eE;
            if (eE >= 140) {
                eE = 140;
                eF = 37 + eE;
                j.g = 1;
                return;
            }
            return;
        }
        if (cb) {
            bW.l(0);
            for (int i3 = 0; i3 < 5; i3++) {
                if (j.g > i3 * 2) {
                    bW.a(cd, d(0, i3 + 38), 95, 55 + (i3 * 20), 20);
                }
            }
        }
        int i4 = 0;
        int i5 = ap[0];
        if (aj == 7) {
            i4 = 3000;
        } else if (aj >= 8) {
            i4 = 5000;
        }
        if (j.g > 0) {
            if (cb) {
                bW.a(cd, new StringBuffer().append("").append(i5).toString(), 305, 55, 24);
            }
            i4 += i5 * dh[au];
        }
        if (j.g > 2) {
            int i6 = ap[3];
            if (cb) {
                bW.a(cd, new StringBuffer().append("").append(i6).toString(), 305, 75, 24);
            }
            i4 += i6 * di[au];
        }
        if (j.g > 4) {
            int i7 = ap[1];
            if (cb) {
                bW.a(cd, new StringBuffer().append("").append(i7).toString(), 305, 95, 24);
            }
            if (i7 > 4) {
                i7 = 4;
            }
            i4 -= i7 * 300;
        }
        if (j.g > 6) {
            if (bh[aj] == 3) {
                if (ap[4] < 0) {
                    ap[4] = 0;
                }
                i = ap[4];
            } else {
                if (ap[5] < 0) {
                    ap[5] = 0;
                }
                i = ap[5];
            }
            if (cb) {
                bW.a(cd, j.c(i, 0), 305, 115, 24);
            }
            i4 += i * 30;
        }
        if (j.g > 8) {
            int i8 = (int) (dg / 16);
            String string = new StringBuffer().append(i8 / 60).append(":").toString();
            int i9 = i8 % 60;
            int i10 = i9;
            if (i9 < 0) {
                i10 = -i10;
            }
            if (i10 < 10) {
                string = new StringBuffer().append(string).append("0").toString();
            }
            if (cb) {
                bW.a(cd, new StringBuffer().append(string).append(i10).toString(), 306, 135, 24);
            }
            if (i8 > 180) {
                int i11 = (i8 - 180) << 1;
                int i12 = i11;
                if (i11 > 1000) {
                    i12 = 1000;
                }
                i4 -= i12;
            }
        }
        if (i4 < 0) {
            i4 = 0;
        }
        if (cb && j.g > 10) {
            bW.a(cd, d(0, 43), 95, 175, 20);
            bW.a(cd, j.c(i4, 0), 305, 175, 24);
            cb = false;
        }
        if (aj < 7) {
            a(d(0, 16), d(0, 62));
        } else {
            a(d(0, 16), "");
        }
        if (v(458784)) {
            z(23);
            if (j.g <= 10) {
                j.g = 10;
                return;
            }
            if (i4 > a(bA, 81 + (au << 4) + (aj << 1))) {
                a(bA, 81 + (au << 4) + (aj << 1), (short) i4);
            }
            dB = ax;
            dC = ay;
            dF = aN;
            dD = az;
            a(bA, 32, (short) az);
            a(bA, 8, au);
            a(bA, 44, ax);
            a(bA, 46, ay);
            a(bA, 48, (short) aN);
            a(bA, 36, (short) 0);
            if (v(327712)) {
                if (aj < 7) {
                    aj++;
                    if (bA[14] < aj) {
                        a(bA, 14, aj);
                    }
                    if (eg[aj]) {
                        l(30);
                    } else {
                        l(2);
                    }
                } else {
                    aj = 0;
                    a(bA, 14, aj);
                    a(bA, 15, 1);
                    bw = 0;
                    W();
                    dz = 0;
                    l(24);
                }
            } else if (v(131072) && aj < 7) {
                aj++;
                if (bA[14] < aj) {
                    a(bA, 14, aj);
                }
                l(2);
            }
            e(true);
            if (j.c == 2) {
                K(0);
            }
        }
    }

    private static void a(b bVar, String str) {
        while (dk <= 0) {
            StringBuffer stringBuffer = new StringBuffer(str);
            if (dj < str.length()) {
                stringBuffer.insert(dj + 1, "\\0");
                stringBuffer.insert(dj, "\\2");
                bVar.f = true;
                bVar.a(cd, new StringBuffer().append("\\0").append(stringBuffer.toString()).toString(), 200, 40, 17);
                bVar.f = false;
                dj++;
                return;
            }
            dj = 0;
            dk = 15;
            bVar = bVar;
        }
        dk--;
        bVar.f = true;
        bVar.a(cd, new StringBuffer().append("\\0").append(str).toString(), 200, 40, 17);
        bVar.f = false;
    }

    private static void N() throws IOException {
        j.a(cd, 0, 0, 400, 240, true);
        cd.setColor(0);
        j.b(cd, 0, 0, 400, 240);
        if (A[5] == null) {
            j.a("/2");
            try {
                A[5] = J(5);
                A[5].j(fA);
            } catch (Exception unused) {
            }
            j.e();
        }
        if (dl == null) {
            a aVar = new a(A[5], 80, -40);
            dl = aVar;
            aVar.a(0, -1);
        }
        if (j.g >= 165) {
            dm = 165;
            dl.b(j.f);
            dl.a(dl.a() - 3);
            dl.c();
            if (j.g % 10 < 5) {
                bW.a(cd, d(0, 9), 200, 220, 17);
            }
        } else {
            dl.b(j.f);
            dl.c();
            dm = j.g;
            bW.l(0);
            bW.a(cd, d(0, 24), 395, 230, 40);
        }
        j.a.setColor(7644855);
        j.b(j.a, 50, 205, j.a(((dm << 8) / 165) * 300), 10);
        j.a(j.a, 0, 0, j.d, j.e, true);
        if (j.g > 1) {
            a(bW, d(0, 51 + eW[aj]));
            y.a(d(1, 0), (char[]) null);
            a(y, 2, d(1, 0), ((400 - b.d) >> 1) < 20 ? 20 : (400 - b.d) >> 1, 135, 360, 240, 0, 20);
        }
    }

    public static void q() {
        as = 0;
        at = 0;
        for (int i = 0; i < 5; i++) {
            ar[i] = -1;
        }
        int i2 = g.J;
        for (int i3 = 0; i3 < 5; i3++) {
            int i4 = 1 << i3;
            if ((i2 & i4) != 0 && i4 != 4) {
                int i5 = 0;
                while (true) {
                    if (i5 >= 5) {
                        break;
                    }
                    if (ar[i5] == -1) {
                        ar[i5] = i4;
                        break;
                    }
                    i5++;
                }
                i2 &= i4 ^ (-1);
                as++;
            }
        }
    }

    public static int p(int i) {
        for (int i2 = 0; i2 < 5; i2++) {
            if (((i >> i2) & 1) != 0) {
                return i2;
            }
        }
        return 0;
    }

    private static void F(int i) {
        i.bS = 0;
        if (i > 0) {
            i.j(1);
        }
        g.I = 1;
        g.J = 0;
        g.g(f0do[i]);
    }

    private static void O() {
        ds++;
        dp[ds] = j.c;
        dq[ds] = bv;
        dr[ds] = bw;
        bw = -1;
    }

    private static void P() {
        j.c = dp[ds];
        K(dq[ds]);
        bw = -1;
        ds--;
    }

    private static void Q() throws InterruptedException {
        if (v(131072)) {
            if (j.c == 23 || j.c == 13) {
                return;
            }
            cb = true;
            if (bv == 2) {
                l(2);
                z(30);
            }
            if (bv != 3 && bv != 4) {
                if (bv == 1) {
                    if (C != null) {
                        C.Z();
                    }
                    l(8);
                    z(30);
                    return;
                }
                if (bv == 4) {
                    if (fi != 0) {
                        e.b();
                    }
                    P();
                    bw = -1;
                    z(30);
                    return;
                }
                return;
            }
            z(30);
            fF = 0;
            if (ex == 8 || j.c == 14) {
                P();
                bw = -1;
                return;
            }
            if (ex != 3) {
                if (ex == 28) {
                    l(2);
                    return;
                } else {
                    l(2);
                    return;
                }
            }
            fE = 255;
            fO = 3;
            K(4);
            bw = -1;
            l(3);
            return;
        }
        if (v(327712)) {
            cb = true;
            if (bv == 2) {
                au = bw < 0 ? 0 : bw;
                z(23);
                j.a(bA, 16, (byte) au);
                a(bA, 16, (short) 0);
                e(true);
                fF = 20;
                l(30);
                return;
            }
            if (v(327712) && bw == -1) {
                bw = 0;
                return;
            }
            int iM = m(bv, bw);
            if (eA[bv][iM] != 83 && eA[bv][iM] != 84) {
                z(23);
            }
            switch (eA[bv][iM]) {
                case 0:
                    O();
                    eC = 73;
                    K(3);
                    eB = 0;
                    bw = -1;
                    break;
                case 1:
                    if (Y()) {
                        eC = 69;
                        K(3);
                        eB = 87;
                        fG = true;
                        l(28);
                        bw = -1;
                        break;
                    } else {
                        aj = 0;
                        az = 0;
                        dD = 0;
                        a(bA, 32, (short) az);
                        a(bA, 14, aj);
                        a(bA, 15, 0);
                        e(true);
                        dB = (byte) 30;
                        dC = (byte) 30;
                        dF = 0;
                        l(29);
                        break;
                    }
                case 2:
                    if (bA[15] == 1) {
                        bw = 0;
                        da = 8;
                        l(19);
                        break;
                    } else {
                        aj = bA[14];
                        byte bA2 = (byte) a(bA, 44);
                        dB = bA2;
                        if (bA2 == 0) {
                            dB = (byte) 30;
                        }
                        byte bA3 = (byte) a(bA, 46);
                        dC = bA3;
                        if (bA3 == 0) {
                            dC = (byte) 30;
                        }
                        dF = a(bA, 48);
                        au = bA[8];
                        short sA = a(bA, 32);
                        dD = sA;
                        az = sA;
                        l(30);
                        break;
                    }
                case 3:
                    bw = -1;
                    if (dt || bA[69] != 0) {
                        da = 8;
                    } else {
                        da = bA[14] + 1;
                    }
                    l(19);
                    break;
                case 4:
                    if (j.c == 14) {
                        O();
                    } else {
                        l(3);
                    }
                    K(4);
                    break;
                case 5:
                    l(4);
                    bw = 0;
                    break;
                case 6:
                    bw = 0;
                    l(5);
                    break;
                case 7:
                    l(6);
                    break;
                case 8:
                    if (j.c == 14) {
                        O();
                    } else {
                        fG = false;
                        l(28);
                    }
                    eC = 13;
                    K(3);
                    eB = 8;
                    bw = -1;
                    break;
                case 11:
                    if (C != null) {
                        C.Z();
                    }
                    if (cy == 21) {
                        l(21);
                        x = 48;
                    } else {
                        l(8);
                    }
                    if (fi == 1) {
                        if (aj == 8) {
                            z(1);
                        } else {
                            z(1);
                        }
                    } else if (fi == 9) {
                        z(9);
                    } else if (fi != -1) {
                        B();
                    }
                    dM = true;
                    break;
                case 12:
                    O();
                    eC = 25;
                    K(3);
                    eB = 12;
                    bw = -1;
                    break;
                case 14:
                    switch (eC) {
                        case 13:
                            j.c = 11;
                            break;
                        case 25:
                            bG = 0;
                            if (j.c != 12 && j.c != 13) {
                                P();
                                a(false);
                                az = dD;
                                break;
                            } else {
                                bx = -1;
                                a(true);
                                bv = 0;
                                break;
                            }
                            break;
                        case 69:
                            aj = 0;
                            az = 0;
                            dD = 0;
                            ax = (byte) 30;
                            a(bA, 14, aj);
                            for (int i = 0; i < 3; i++) {
                                cc[i] = 0;
                                a(bA, i + 130, 0);
                            }
                            a(bA, 44, ax);
                            a(bA, 28, ax);
                            a(bA, 32, (short) az);
                            a(bA, 14, aj);
                            a(bA, 15, 0);
                            dB = (byte) 30;
                            dC = (byte) 30;
                            dF = 0;
                            if (fG) {
                                fF = 29;
                                l(29);
                            } else {
                                a(bA, 69, 0);
                                au = 1;
                                for (int i2 = 0; i2 < 24; i2++) {
                                    a(bA, 81 + (i2 << 1), (short) 0);
                                }
                                eC = 121;
                            }
                            fG = false;
                            e(true);
                            break;
                        case 73:
                            P();
                            W();
                            l(2);
                            break;
                    }
                case 15:
                    fG = false;
                    if (j.c != 12 && j.c != 13) {
                        if (ex == 2) {
                            l(2);
                            break;
                        } else if (ex != 3 || j.c != 28) {
                            if (dp[ds] != 2 && dp[ds] != 14) {
                                P();
                                j.g = 0;
                                break;
                            } else {
                                P();
                                bw = -1;
                                break;
                            }
                        } else {
                            fE = 255;
                            fO = 3;
                            K(4);
                            bw = -1;
                            l(3);
                            break;
                        }
                    } else {
                        bx = -1;
                        W();
                        l(2);
                        break;
                    }
                    break;
                case 32:
                case 33:
                case 34:
                    f.a(d(0, 24), 0);
                    l(27);
                    if (!eJ) {
                        eJ = true;
                        a(bA, 10, 1);
                        e(true);
                        break;
                    }
                    break;
                case 83:
                    bE = !bE;
                    j.g = 0;
                    if (bE) {
                        if (j.c == 3) {
                            z(0);
                            break;
                        } else {
                            z(6);
                            break;
                        }
                    } else {
                        e.b();
                        fi = -1;
                        break;
                    }
                case 84:
                    boolean z2 = !bF;
                    bF = z2;
                    if (z2) {
                        e.b();
                        z(23);
                        break;
                    }
                    break;
                case 87:
                    eC = 69;
                    K(3);
                    eB = 87;
                    fG = false;
                    l(28);
                    bw = -1;
                    break;
                case 97:
                    int i3 = (au + 1) % 3;
                    au = i3;
                    if (i3 == 2 && bA[69] == 0) {
                        au = 0;
                    }
                    a(bA, 8, au);
                    e(true);
                    break;
                case 103:
                    f.b();
                    l(27);
                    break;
                case 113:
                    j.g = 0;
                    l(22);
                    break;
                case 117:
                    aj = 0;
                    az = 0;
                    dD = 0;
                    au = 1;
                    dB = (byte) 30;
                    dC = (byte) 30;
                    dF = 0;
                    l(9);
                    break;
                case 123:
                    cm = 1 - cm;
                    a(bA, 80, cm);
                    break;
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:61:0x0115 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v125 */
    /* JADX WARN: Type inference failed for: r0v126, types: [java.lang.Throwable] */
    /* JADX WARN: Type inference failed for: r0v136, types: [java.lang.String[]] */
    private static void R() throws IOException {
        switch (cu) {
            case 0:
                az = 0;
                j.a("/1");
                b bVarJ = J(0);
                bX = bVarJ;
                bVarJ.a(0, 0, -1, -1);
                bX.a(true);
                cd.setColor(0);
                j.b(cd, 0, 0, 400, 240);
                bX.a(cd, 0, 200, 120, 0, 0, 0);
                cu++;
                du = System.currentTimeMillis();
                break;
            case 1:
                z = new b[75];
                bW = J(1);
                y = J(3);
                for (int i = 0; i < bW.k; i++) {
                    bW.a(i, 0, -1, -1);
                }
                for (int i2 = 0; i2 < y.k; i2++) {
                    y.a(i2, 0, -1, -1);
                }
                short[] sArr = (short[]) j.f(2);
                bW.a(sArr);
                y.a(sArr);
                j.e();
                j.a("/14", 0);
                bU = new String[j.f()];
                int i3 = 0;
                while (true) {
                    ?? r0 = i3;
                    if (r0 < j.f()) {
                        bU[i3] = j.g(i3);
                        i3++;
                    } else {
                        try {
                            int iIndexOf = bU[77].indexOf("$VVV");
                            int length = bU[77].length();
                            r0 = bU;
                            r0[77] = new StringBuffer().append(bU[77].substring(0, iIndexOf)).append(GloftASBR.b).append(bU[77].substring(iIndexOf + 4, length)).toString();
                        } catch (Exception e) {
                            r0.printStackTrace();
                        }
                        j.g();
                        try {
                            e.a("/17");
                        } catch (Exception unused) {
                        }
                        cu++;
                    }
                }
            case 2:
                cd.setColor(0);
                j.b(cd, 0, 0, 400, 240);
                bX.a(cd, 0, 200, 120, 0, 0, 0);
                if (System.currentTimeMillis() - du >= 3000) {
                    cu++;
                    j.a("/16", 0, 1);
                    j.h();
                    du = System.currentTimeMillis();
                    break;
                }
                break;
            case 3:
                cd.setColor(0);
                j.b(cd, 0, 0, 400, 240);
                bX.a(cd, 1, 200, 120, 0, 0, 0);
                a(y, 2, d(0, 63), 200, 140, 400, 240, 0, 17);
                if (System.currentTimeMillis() - du >= 3000) {
                    cu++;
                    bX = null;
                    du = System.currentTimeMillis();
                    j.a("/3");
                    z[11] = null;
                    z[11] = J(11);
                    z[54] = null;
                    z[54] = J(54);
                    z[54].h(0, 1);
                    j.e();
                    break;
                }
                break;
            case 4:
                cd.setColor(0);
                j.b(cd, 0, 0, 400, 240);
                a(y, 2, d(0, 65), 200, 120, 380, 240, 0, 3);
                cu++;
                for (int i4 = 0; i4 < 512; i4++) {
                    bA[i4] = 0;
                }
                e(false);
                eJ = bA[10] != 0;
                au = bA[8];
                if (RecordStore.openRecordStore("/ASBR", true).getNumRecords() <= 0) {
                    au = 1;
                    j.a(bA, 8, (byte) au);
                }
                int i5 = au % 3;
                au = i5;
                if (i5 == 2 && bA[69] == 0) {
                    au = 0;
                    break;
                }
                break;
            case 5:
                try {
                    j.a("/2");
                    b[] bVarArr = new b[6];
                    A = bVarArr;
                    bVarArr[0] = J(0);
                    A[0].j(fA);
                    A[1] = J(1);
                    A[1].a(0, 0, -1, -1);
                    A[1].a(true);
                    A[2] = J(2);
                    A[2].a(0, 0, -1, -1);
                    A[3] = J(3);
                    A[3].j(fA);
                    A[4] = J(4);
                    A[4].a(0, 0, -1, -1);
                    A[4].a(true);
                    A[4].j(fA);
                    A[5] = J(5);
                    A[5].j(fA);
                    j.e();
                    j.a("/3");
                    z[12] = J(12);
                    z[12].j(fA);
                    z[39] = J(39);
                    z[39] = a(z[39], 1, 90, false);
                    z[39].j(fA);
                    j.e();
                } catch (Exception unused2) {
                }
                cu++;
            case 6:
                S();
                cd.setColor(0);
                j.b(cd, 0, 0, 400, 240);
                a(y, 2, d(0, 65), 200, 120, 380, 240, 0, 3);
                if (System.currentTimeMillis() - du >= 5000) {
                    cu++;
                    du = System.currentTimeMillis();
                    f.a(GloftASBR.c, GloftASBR.a, 400, 240);
                    l(23);
                    break;
                }
                break;
        }
    }

    private static void S() {
        for (int i = 0; i < 3; i++) {
            byte b = bA[i + 130];
            byte b2 = b;
            if (b <= 0) {
                b2 = 0;
            }
            cc[i] = b2;
        }
    }

    private static int a(b bVar, int i, String str, int i2, int i3, int i4, int i5, int i6, int i7) {
        return a(bVar, i, str, i2, i3, i4, i7, -1);
    }

    private static int a(b bVar, int i, String str, int i2, int i3, int i4, int i5, int i6) {
        if (str == null) {
            return 0;
        }
        int iIndexOf = str.toUpperCase().indexOf("EZIO");
        if (bL != 0 && iIndexOf != -1) {
            str = new StringBuffer().append(str.substring(0, iIndexOf)).append(d(0, 106 + bL)).append(str.substring(iIndexOf + 4, str.length())).toString();
        }
        short[] sArrA = a(bVar, str, i4);
        short s = sArrA[0];
        bVar.l(i);
        bVar.a(cd, str, sArrA, i2, i3, 8 * (cY - 1), 8, i5, i6);
        return s;
    }

    public static void r() {
        aw = 0;
        av = false;
        dz = 120;
    }

    public static void s() {
        az++;
        if (ax >= 105) {
            return;
        }
        if (ax < 30) {
            ax = (byte) 30;
        }
        int length = dE.length - 1;
        while (length > 0 && az < dE[length]) {
            length--;
        }
        if (length == 0 || az < dE[length]) {
            return;
        }
        byte b = ax;
        if (ax <= 105) {
            byte b2 = (byte) (30 + (length * 15));
            ax = b2;
            g.f((int) b2);
            if (b < ax) {
                g.e(ax);
            }
        }
    }

    private static void T() {
        dA = null;
        i iVar = new i();
        dA = iVar;
        iVar.aa = z[12];
        dA.i(0);
        dA.ak = 0;
        dA.al = 0;
        aA = 0;
    }

    private static void c(boolean z2) {
        if (ax == 0) {
            ax = (byte) 30;
        }
        g.f((int) ax);
        z[12].a(cd, 2, (ax / 15) - 1, 2, 30, 0, 0, 0);
        A[4].a(cd, 8 + bL, 0, 22, 30, 0, 0, 0);
        j.a(cd, 43, 6, (g.x[1] * 11) / 15, 20, true);
        z[12].a(cd, 6, (ax / 15) - 1, 2, 30, 0, 0, 0);
        int i = 400;
        j.a(cd, 0, 0, 400, 240, true);
        if (bh[aj] == 3) {
            if (i.bT && B != null) {
                cd.setColor(0);
                j.b(cd, 389, 60, 6, 100);
                if (B.aB > i.bU) {
                    B.aB = i.bU;
                }
                cd.setColor(16711680);
                if (B.aB <= 0 || (100 * B.aB) / i.bU != 0) {
                    j.b(cd, 389, 160 - ((100 * B.aB) / i.bU), 6, (100 * B.aB) / i.bU);
                } else {
                    j.b(cd, 389, 159, 6, 1);
                }
            }
            z[54].l(0);
            z[54].a(cd, 0, j.g % z[54].b(0), 300, 8, 0, 0, 0);
            y.l(0);
            if (ap[4] < 0) {
                ap[4] = 0;
            }
            i = 312;
            y.a(cd, new StringBuffer().append(ap[4]).append("/").append(aq).toString(), 312, 5, 20);
            if (aE > 0) {
                if (aH > 30) {
                    aH--;
                } else if (aH >= 0) {
                    int i2 = aH - 1;
                    aH = i2;
                    if (i2 < 0) {
                        aE = aH;
                        aH = 0;
                        return;
                    }
                }
                if (aF > 0) {
                    if (aF < 3) {
                        aE += aF;
                        aF = 0;
                    } else {
                        aE += 3;
                        aF -= 3;
                    }
                }
                int i3 = 0;
                if (aH >= 0 && aH <= 30) {
                    i3 = 30 - aH;
                }
                int i4 = aE;
                if (aE >= 100) {
                    i4 = 100;
                }
                if (aE < 25) {
                    z[12].a(cd, 5, j.g % z[12].b(5), 15 - i3, 165, 0, 0, 0);
                } else {
                    z[12].a(cd, 3, 0, 15 - i3, 165, 0, 0, 0);
                }
                i = 0;
                z[12].a(cd, 4, 0, 10 - i3, 49 + ((116 * (100 - i4)) / 100), 0, 0, 0);
            }
        } else if (bh[aj] != 3 && z[54] != null && aj < 8) {
            if (az < 0) {
                az = 0;
            }
            if (az > 32767) {
                az = 32767;
            }
            y.l(0);
            int length = dE.length - 1;
            while (length > 0 && az < dE[length]) {
                length--;
            }
            y.a(cd, length == dE.length - 1 ? j.c(az - dE[dE.length - 1], 0) : new StringBuffer().append(az - dE[length]).append("/").append(dE[length + 1] - dE[length]).toString(), 200, -1, 17);
            i = 0;
            z[12].a(cd, 7, 0, (200 - (b.d >> 1)) - 10, 13 + ((j.g / 3) % 2), 0, 0, 0);
        }
        if (!g.g() && i.bx != null && i.bx.bl > 0) {
            int i5 = 10;
            if (i.bx.ax == 73 || i.bx.ax == 11) {
                i5 = 80;
            }
            cd.setColor(16777215);
            j.c(cd, i.bx.ak - O, (i.bx.al - P) - 70, 42, 5);
            cd.setColor(16711680);
            Graphics graphics = cd;
            int i6 = (i.bx.ak - O) + 1;
            int i7 = (i.bx.al - P) - 70;
            i = (i.bx.bl * 40) / i5;
            j.b(graphics, i6, i7, i, 4);
        }
        if (!z2 && bh[aj] != 3 && aS.o() && ((C == null || (C != null && (aS.P & 512) != 0)) && (((j.c == 21 && u == 8) || j.c == 8) && z[12] != null))) {
            if (at == 1) {
                at = 0;
            }
            if (d(355, 197, 30, 26)) {
                z[12].a(cd, 22, 0, 370, 210, 0, 0, 0);
            } else {
                z[12].a(cd, 8, 0, 370, 210, 0, 0, 0);
            }
            i = 0;
            z[12].a(cd, dn[p(g.I)], 0, 370, 210, 0, 0, 0);
        }
        int i8 = 0;
        if (aJ == 1) {
            int i9 = aK + 10;
            aK = i9;
            if (i9 > 80) {
                aK = 80;
                aJ = 2;
                aM = 0;
            }
            i8 = aL * 1000;
        } else if (aJ == 3) {
            int i10 = aK - 20;
            aK = i10;
            if (i10 < -40) {
                aK = -40;
                aJ = 0;
                aM = 0;
            }
            i8 = (aL * 1000) - aM;
        } else if (aJ == 2) {
            int i11 = (aL * 1000) - aM;
            i8 = i11;
            if (i11 < 0) {
                i8 = 0;
            }
        }
        if (aJ >= 1) {
            StringBuffer stringBuffer = new StringBuffer(8);
            stringBuffer.append(((i8 / 1000) / 60) % 60).append(':').append(i % 60).append(':').append((i % 1000) / 10);
            String string = stringBuffer.toString();
            String strD = d(0, 78);
            if (aj == 7) {
                strD = d(0, 122);
            }
            y.l(0);
            y.a(cd, strD, aK, 40, 0);
            y.a(cd, string, aK, 60, 0);
        }
        if (aB != null && aC != 0) {
            cd.setColor(0);
            j.b(cd, 0, 200, 400, 40);
            a(bW, 0, aB, 200, 202, 400, 240, 0, 17);
            if (aC > 0) {
                aC--;
            }
            if (aC == 0) {
                aB = null;
            }
        }
        if (aO < 0 || aP == null) {
            aP = null;
        } else {
            y.l(0);
            y.a(cd, aP, 200, 23, 17);
            aO -= j.f;
        }
        if (g.g != null) {
            if ((aS.S == 303 || aS.S == 295) && z[10] != null) {
                if (aS.S == 295) {
                    z[10].a(cd, 29, aS.K, aS.L - O, aS.M - P, 0, 0, 0);
                } else {
                    z[10].a(cd, 41, aS.K, aS.L - O, aS.M - P, 0, 0, 0);
                }
            }
        }
    }

    public static final Image f(int i, int i2) {
        return Image.createImage(i2, i);
    }

    private static void U() {
        if (dJ == null) {
            dJ = f(420, 260);
            if (bh[aj] == 3) {
                dL = new int[21][13];
            }
        }
        if (dK == null) {
            dK = dJ.getGraphics();
        }
        dM = true;
    }

    private static void h(int i, int i2, int i3, int i4) {
        int i5;
        int i6 = i2;
        while (i6 <= i4) {
            int i7 = i6 % 13;
            if (aR < 0) {
                i5 = i6;
            } else {
                if (i6 < dT) {
                    dS = aR;
                    if (dR < 0 || dR > aR) {
                        aR++;
                    } else {
                        dU += (aR - dR) + 1;
                        aR = dR;
                    }
                    dT -= 20;
                }
                i5 = (i6 < dT || i6 >= dT + 20) ? (i6 % 20) + ((((bu / 20) - 1) - dS) * 20) : (i6 % 20) + ((((bu / 20) - 1) - aR) * 20);
                if (i5 < 0) {
                    int i8 = i5 % 20;
                    i5 = i8;
                    if (i8 < 0) {
                        i5 += 20;
                    }
                }
            }
            for (int i9 = i; i9 <= i3; i9++) {
                int i10 = i9 % 21;
                if (bh[aj] == 3) {
                    if (i7 < 0) {
                        i7 += 13;
                    }
                    dL[i10][i7] = i9 + (i5 * bt);
                }
                a(dK, i10 * 20, (i7 * 20) - 20, i9 + (i5 * bt));
            }
            i6++;
        }
    }

    private static void e(Graphics graphics, int i, int i2, int i3, int i4) {
        a(graphics, i, i2, 400, 240, 0);
    }

    public static void a(Graphics graphics, int i, int i2, int i3, int i4, int i5) {
        if (i2 < 0) {
            i2 -= 20;
        }
        int i6 = i / 20;
        int i7 = i2 / 20;
        int i8 = ((i + i3) - 1) / 20;
        int i9 = ((i2 + i4) - 1) / 20;
        j.a(graphics, 0, 0, 400, 240, true);
        if (i2 < 0) {
            i2 += 20;
        }
        int i10 = (i6 * 20) - i;
        for (int i11 = i6; i11 <= i8; i11++) {
            int i12 = (i7 * 20) - i2;
            for (int i13 = i7; i13 <= i9; i13++) {
                if (bh[aj] == 3) {
                    int i14 = i13 % 13;
                    int i15 = i14;
                    if (i14 < 0) {
                        i15 += 13;
                    }
                    b(graphics, i10, i12 + i5, dL[i11 % 21][i15]);
                } else if (bh[aj] != 2) {
                    b(graphics, i10, i12 + i5, i11 + (i13 * bp));
                }
                i12 += 20;
            }
            i10 += 20;
        }
    }

    private static void f(Graphics graphics, int i, int i2, int i3, int i4) {
        b(graphics, i, i2, 400, 240, 0);
    }

    public static void b(Graphics graphics, int i, int i2, int i3, int i4, int i5) {
        if (i2 < 0) {
            i2 -= 20;
        }
        int i6 = i / 20;
        int i7 = i2 / 20;
        int i8 = ((i + i3) - 1) / 20;
        int i9 = ((i2 + i4) - 1) / 20;
        j.a(graphics, 0, 0, 400, 240, true);
        if (i2 < 0) {
            i2 += 20;
        }
        int i10 = (i6 * 20) - i;
        for (int i11 = i6; i11 <= i8; i11++) {
            int i12 = (i7 * 20) - i2;
            for (int i13 = i7; i13 <= i9; i13++) {
                int i14 = i10;
                int i15 = i12 + i5;
                int i16 = i11 + (i13 * ew);
                if (i16 < 0 || i16 >= er.length) {
                    new StringBuffer().append("invalid bottom2 map index : ").append(i16).toString();
                } else if ((er[i16] & 255) != 255) {
                    dW = er[i16] & 255;
                    int i17 = es[i16 >> 2] & 255;
                    dX = i17;
                    dX = (i17 >> ((3 - (i16 & 3)) << 1)) & 3;
                    dY = 0;
                    dZ = 0;
                    if ((dX & 1) != 0) {
                        dY = 20;
                    }
                    if ((dX & 2) != 0) {
                        dZ = 20;
                    }
                    if (ca != null) {
                        ca.a(graphics, dW, i14 + dY, i15 + dZ, dX, 0, 0);
                    } else {
                        bY.a(graphics, dW, i14 + dY, i15 + dZ, dX, 0, 0);
                    }
                }
                i12 += 20;
            }
            i10 += 20;
        }
    }

    private static void d(int i, int i2, int i3, int i4, int i5, int i6) {
        g(i5, i6, i3, i4);
        cd.drawImage(dJ, (240 - (i6 - i2)) - 260, i5 - i, 0);
    }

    public static void a(Graphics graphics, int i, int i2, int i3) {
        if ((eu[i3] & 255) != 255) {
            dW = eu[i3] & 255;
            int i4 = ev[i3 >> 2] & 255;
            dX = i4;
            dX = (i4 >> ((3 - (i3 & 3)) << 1)) & 3;
            dY = 0;
            dZ = 0;
            if ((dX & 1) != 0) {
                dY = 20;
            }
            if ((dX & 2) != 0) {
                dZ = 20;
            }
            bZ.a(graphics, dW, i + dY, i2 + dZ, dX, 0, 0);
        }
    }

    private static void b(Graphics graphics, int i, int i2, int i3) {
        if (i3 < 0 || i3 >= ep.length) {
            new StringBuffer().append("invalid bottom map index : ").append(i3).toString();
            return;
        }
        if ((ep[i3] & 255) != 255) {
            dW = ep[i3] & 255;
            int i4 = eq[i3 >> 2] & 255;
            dX = i4;
            dX = (i4 >> ((3 - (i3 & 3)) << 1)) & 3;
            dY = 0;
            dZ = 0;
            if ((dX & 1) != 0) {
                dY = 20;
            }
            if ((dX & 2) != 0) {
                dZ = 20;
            }
            bY.a(graphics, dW, i + dY, i2 + dZ, dX, 0, 0);
        }
    }

    public static void b(i iVar) {
        iVar.as = -98;
        if (eb > 0) {
            i[] iVarArr = bb;
            int[] iArr = ea;
            int i = eb - 1;
            eb = i;
            iVarArr[iArr[i]] = iVar;
            return;
        }
        if (bc >= ba) {
            return;
        }
        i[] iVarArr2 = bb;
        int i2 = bc;
        bc = i2 + 1;
        iVarArr2[i2] = iVar;
    }

    public static void c(i iVar) {
        if (iVar == null) {
            return;
        }
        if (ah == iVar) {
            n();
        }
        if (F == iVar) {
            F = null;
        }
        for (int i = 0; i < bc; i++) {
            if (bb[i] == iVar) {
                if (iVar.as != -98) {
                    bg[iVar.as] = -99;
                }
                bb[i].p();
                bb[i] = null;
                int[] iArr = ea;
                int i2 = eb;
                eb = i2 + 1;
                iArr[i2] = i;
                return;
            }
        }
    }

    public static i q(int i) {
        if (i == -1) {
            return null;
        }
        if (aS != null && aS.aw == i) {
            return aS;
        }
        for (int i2 = 0; i2 < bc; i2++) {
            if (bb[i2] != null && bb[i2].aw == i) {
                return bb[i2];
            }
        }
        return null;
    }

    public static void a(i iVar, int i) {
        a(bf, i * 22, (short) iVar.S);
        a(bf, (i * 22) + 2, (short) iVar.T);
        a(bf, (i * 22) + 4, (short) iVar.ak);
        a(bf, (i * 22) + 6, (short) iVar.al);
        b(bf, (i * 22) + 8, iVar.aA);
        b(bf, (i * 22) + 12, iVar.P);
        a(bf, (i * 22) + 16, iVar.bz ? 1 : 0);
        b(bf, (i * 22) + 17, iVar.bs);
        a(bf, (i * 22) + 21, iVar.av ? 1 : 0);
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:103:0x0261 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:105:0x0281 A[SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:81:0x025b A[Catch: Exception -> 0x028b, TryCatch #0 {Exception -> 0x028b, blocks: (B:2:0x0000, B:5:0x0011, B:7:0x0019, B:9:0x0029, B:11:0x0031, B:14:0x0043, B:15:0x0066, B:17:0x006c, B:20:0x0081, B:22:0x0089, B:23:0x0090, B:24:0x009a, B:26:0x00a2, B:28:0x00ac, B:30:0x00b6, B:32:0x00c4, B:33:0x00d0, B:35:0x00e0, B:37:0x00ea, B:39:0x00f5, B:41:0x0116, B:43:0x011f, B:44:0x0124, B:48:0x0199, B:52:0x01cb, B:54:0x01d7, B:56:0x01e1, B:58:0x01e8, B:60:0x01f0, B:62:0x01f9, B:64:0x0203, B:65:0x020d, B:67:0x0216, B:68:0x021b, B:70:0x0225, B:72:0x022f, B:74:0x0239, B:76:0x0243, B:77:0x0249, B:79:0x0251, B:84:0x026c, B:81:0x025b, B:83:0x0261, B:19:0x0074, B:86:0x0284), top: B:90:0x0000 }] */
    /* JADX WARN: Type inference failed for: r0v88, types: [i] */
    /* JADX WARN: Type inference failed for: r11v5, types: [int] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static void d(boolean z2) {
        g gVar;
        try {
            short[] sArr = new short[25];
            int i = 0;
            i.D();
            if (!z2) {
                for (int i2 = 0; i2 < ba; i2++) {
                    bg[i2] = 0;
                }
            }
            int i3 = 0;
            while (i < ek.length) {
                int i4 = i;
                i++;
                byte b = ek[i4];
                for (byte b2 = 0; b2 < b; b2++) {
                    int i5 = i;
                    int i6 = i + 1;
                    i = i6 + 1;
                    sArr[b2] = (short) ((ek[i5] & 255) + (ek[i6] << 8));
                }
                if (sArr[0] == 0 || sArr[0] == 25) {
                    gVar = new g(sArr);
                    if (gVar.ax == 0 && gVar.ax != 25 && gVar.ax != 70) {
                        int i7 = i3;
                        i3++;
                        gVar.as = i7;
                        if (!z2) {
                            a(gVar, i3 - 1);
                        } else if (bf[(i3 - 1) * 22] != -99) {
                            if (gVar.ax != 60 || gVar.Z[4] != 2) {
                                g gVar2 = gVar;
                                int i8 = i3 - 1;
                                gVar2.S = a(bf, i8 * 22);
                                if (gVar2.ax == 27 && gVar2.S == 6) {
                                    gVar2.S = 4;
                                }
                                gVar2.T = a(bf, (i8 * 22) + 2);
                                gVar2.ak = a(bf, (i8 * 22) + 4);
                                gVar2.al = a(bf, (i8 * 22) + 6);
                                gVar2.aA = b(bf, (i8 * 22) + 8);
                                gVar2.P = b(bf, (i8 * 22) + 12);
                                gVar2.bz = bf[(i8 * 22) + 16] == 1;
                                gVar2.bs = b(bf, (i8 * 22) + 17);
                                gVar2.av = bf[(i8 * 22) + 21] == 1;
                                if (gVar2.ax == 11 && (gVar2.P & 32) == 0 && gVar2.Z != null && gVar2.S != 2 && (gVar2.Z[5] > 0 || gVar2.Z[6] > 0)) {
                                    gVar2.S = 3;
                                    gVar2.T = 0;
                                }
                                if (gVar2.ax == 21) {
                                    gVar2.aA = 0;
                                }
                                if (gVar.ax == 35 || gVar.ax == 69 || gVar.ax == 11 || gVar.ax == 73) {
                                    gVar.t();
                                }
                            }
                        }
                    }
                    if (gVar.ax == 0 && gVar.ax != 25) {
                        b(gVar);
                        bb[bc - 1].as = i3 - 1;
                    } else if (aS != null) {
                        aS = gVar;
                    }
                } else if (sArr[0] == 55) {
                    c.a(sArr);
                } else {
                    gVar = new i(sArr);
                    if (gVar.ax == 0) {
                    }
                    if (gVar.ax == 0) {
                        if (aS != null) {
                        }
                    }
                }
            }
            v();
            i.R();
        } catch (Exception unused) {
        }
    }

    private static void V() {
        for (int i = 0; i < bc; i++) {
            if (bb[i] != null) {
                bb[i].p();
                bb[i] = null;
            }
        }
        bc = 0;
        eb = 0;
        aS = null;
        n();
        aB = null;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:278:0x04fe */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v177 */
    /* JADX WARN: Type inference failed for: r0v178, types: [java.lang.Throwable] */
    /* JADX WARN: Type inference failed for: r0v202, types: [short] */
    /* JADX WARN: Type inference failed for: r0v229, types: [byte[], java.lang.Object] */
    /* JADX WARN: Type inference failed for: r0v234 */
    /* JADX WARN: Type inference failed for: r0v236, types: [byte[][], byte[][][]] */
    /* JADX WARN: Type inference failed for: r0v240, types: [int[], int[][]] */
    /* JADX WARN: Type inference failed for: r0v247, types: [int] */
    /* JADX WARN: Type inference failed for: r0v248, types: [byte[][][]] */
    /* JADX WARN: Type inference failed for: r0v254 */
    /* JADX WARN: Type inference failed for: r0v257 */
    /* JADX WARN: Type inference failed for: r0v278 */
    /* JADX WARN: Type inference failed for: r0v282 */
    /* JADX WARN: Type inference failed for: r16v3, types: [int] */
    /* JADX WARN: Type inference failed for: r1v213 */
    /* JADX WARN: Type inference failed for: r2v79 */
    /* JADX WARN: Type inference failed for: r3v18 */
    private static boolean G(int i) {
        short s;
        try {
            A[1] = null;
            if (i == 1) {
                dx = false;
                j.a("/14", 1 + aj);
                dD = a(bA, 32);
                byte bA2 = (byte) a(bA, 44);
                dB = bA2;
                if (bA2 <= 0) {
                    dB = (byte) 30;
                }
                az = dD;
                ax = dB;
                return false;
            }
            if (i == 2) {
                U();
                if (bh[aj] != 3 || dL != null) {
                    return false;
                }
                dL = new int[21][13];
                return false;
            }
            if (i == 3) {
                I(aj);
                return false;
            }
            if (i < 8) {
                int i2 = i - 4;
                if (bh[aj] == 2) {
                    if (i2 == 0) {
                        j.a("/15");
                        bY = J(ej[aj << 2]);
                        bZ = J(ej[(aj << 2) + 1]);
                        dM = true;
                        j.e();
                        bY.l(0);
                        bY.a(0, 0, -1, -1);
                        bZ.l(0);
                        bZ.a(0, 0, -1, -1);
                        bZ.a(true);
                        return false;
                    }
                } else if (i2 == 0) {
                    j.a("/15");
                    if (bh[aj] == 3) {
                        ca = J(ej[(aj << 2) + 2]);
                        return false;
                    }
                    bY = J(ej[aj << 2]);
                    if (bh[aj] == 4) {
                        if (ej[aj << 2] != ej[(aj << 2) + 2]) {
                            ca = J(ej[(aj << 2) + 2]);
                            return false;
                        }
                        ca = null;
                        return false;
                    }
                } else {
                    if (i2 == 1) {
                        bZ = J(ej[(aj << 2) + 1]);
                        dM = true;
                        j.e();
                        return false;
                    }
                    if (i2 == 2) {
                        if (bh[aj] != 3 || ca == null) {
                            bY.l(0);
                            bY.a(0, 0, -1, -1);
                            bY.a(true);
                        } else {
                            ca.l(0);
                            ca.a(0, 0, -1, -1);
                            ca.a(true);
                        }
                        if (bh[aj] == 4 && ca != null) {
                            ca.l(0);
                            ca.a(0, 0, -1, -1);
                            ca.a(true);
                        }
                        return false;
                    }
                    if (i2 == 3) {
                        bZ.l(0);
                        bZ.a(0, 0, -1, -1);
                        bZ.a(true);
                    }
                }
                return false;
            }
            if (i == 8) {
                K();
                j.a(ec[aj]);
                byte[] bArrE = j.e(0);
                ?? E2 = j.e(7);
                j.e();
                ek = bArrE;
                int iT = 0 + 1;
                int i3 = E2[0] == true ? (short) 1 : (short) 0;
                by = new byte[i3][];
                eH = new short[i3];
                bz = new int[i3];
                for (int i4 = 0; i4 < i3; i4++) {
                    eH[i4] = (short) (((E2[iT] == true ? 1 : 0) & 255) + (((E2[iT + 1] == true ? 1 : 0) & 255) << 8));
                    int i5 = iT + 2;
                    int i6 = E2[i5];
                    iT = i5 + 1 + 2;
                    by[i4] = new byte[i6];
                    bz[i4] = new int[i6];
                    for (int i7 = 0; i7 < i6; i7++) {
                        int i8 = iT;
                        int i9 = iT;
                        int i10 = iT + 1 + 1;
                        switch (E2[i9] == true ? 1 : 0) {
                            case true:
                                i10 += 2;
                                break;
                            case true:
                                i10 += 2;
                                break;
                        }
                        short s2 = (short) (((E2[i10] == true ? 1 : 0) & 255) + (((E2[i10 + 1] == true ? 1 : 0) & 255) << 8));
                        iT = i10 + 2;
                        for (short s3 = 0; s3 < s2; s3++) {
                            int i11 = iT + 2;
                            iT = i11 + 1;
                            ?? r0 = E2[i11];
                            for (int i12 = 0; i12 < r0; i12++) {
                                int i13 = iT;
                                iT++;
                                ?? r02 = E2[i13];
                                if (r02 < 100) {
                                    switch (r02 == true ? 1 : 0) {
                                        case true:
                                        case true:
                                        case true:
                                        case true:
                                        case true:
                                        case true:
                                            iT = iT + 2 + 2;
                                            break;
                                        case true:
                                            iT = iT + 1 + 2 + 2;
                                            break;
                                        case true:
                                        case true:
                                        case true:
                                            iT += 2;
                                            break;
                                        case true:
                                        case true:
                                        case true:
                                        case true:
                                            iT += 4;
                                            break;
                                        case true:
                                        case true:
                                        case true:
                                            iT += (((((r02 == true ? 1 : 0) - 34) % 3) + 1) << 1) + 2;
                                            break;
                                        case true:
                                        case true:
                                        case true:
                                            iT += ((((r02 == true ? 1 : 0) - 34) % 3) + 1) << 1;
                                            break;
                                    }
                                } else {
                                    iT += t(r02 == true ? 1 : 0);
                                }
                            }
                        }
                        int i14 = iT - i8;
                        by[i4][i7] = new byte[i14 + 2];
                        bz[i4][i7] = iT - i8;
                        System.arraycopy(E2, i8, by[i4][i7], 0, i14);
                        by[i4][i7][i14] = (byte) bz[i4][i7];
                        by[i4][i7][i14 + 1] = (byte) (bz[i4][i7] >> 8);
                    }
                }
                z[54] = null;
                return false;
            }
            if (i == 9) {
                for (int i15 = 0; i15 < 75; i15++) {
                    el[i15] = 0;
                }
                short[] sArr = new short[25];
                int i16 = 0;
                dv = 0;
                while (i16 < ek.length) {
                    int i17 = i16;
                    i16++;
                    byte b = ek[i17];
                    int i18 = 0;
                    while (true) {
                        s = i18;
                        if (s < b) {
                            int i19 = i16;
                            int i20 = i16 + 1;
                            i16 = i20 + 1;
                            sArr[i18] = (short) ((ek[i19] & 255) + (ek[i20] << 8));
                            i18++;
                        } else {
                            try {
                                break;
                            } catch (Exception e) {
                                s.printStackTrace();
                            }
                        }
                    }
                    s = sArr[0];
                    if (s == 67) {
                        el[bk[sArr[7]]] = 1;
                    } else if (sArr[0] == 46) {
                        el[bl[sArr[10]]] = 1;
                    } else if (sArr[0] == 7) {
                        el[bm[sArr[8]]] = 1;
                    } else if (sArr[0] == 56) {
                        el[bj[sArr[7]]] = 1;
                    } else if (sArr[0] == 9) {
                        el[bn[sArr[8]]] = 1;
                    } else if (sArr[0] < bi.length && bi[sArr[0]] != -1) {
                        el[bi[sArr[0]]] = 1;
                    }
                    if (sArr[0] == 11 || sArr[0] == 73 || sArr[0] == 17 || sArr[0] == 23 || sArr[0] == 47 || sArr[0] == 50 || sArr[0] == 54 || sArr[0] == 64 || sArr[0] == 56 || sArr[0] == 30) {
                        dv++;
                    }
                }
                el[59] = 1;
                el[61] = 1;
                if (el[52] == 1) {
                    el[71] = 1;
                }
                el[39] = 1;
                el[74] = 1;
                el[54] = 1;
                if (ef[aj]) {
                    el[58] = 1;
                }
                el[42] = 1;
                el[46] = 1;
                el[50] = 1;
                el[43] = 0;
                if (bh[aj] != 3) {
                    el[5] = 1;
                    el[10] = 1;
                }
                el[9] = 1;
                if (bh[aj] == 3) {
                    el[15] = 1;
                    el[26] = 1;
                    el[40] = 1;
                    el[17] = 1;
                }
                el[2] = 1;
                j.a("/3");
                return false;
            }
            if (i >= 10 && i < 85) {
                int i21 = i - 10;
                if (el[i21] == 1 && z[i21] == null) {
                    z[i21] = J(i21);
                } else if (el[i21] == 1 && z[i21] != null) {
                    el[i21] = 2;
                }
                if (z[i21] != null) {
                    z[i21].j(fA);
                }
                return false;
            }
            if (i == 85) {
                j.e();
                for (int i22 = 0; i22 < eo.length / 3; i22++) {
                    int i23 = eo[i22 * 3];
                    int i24 = eo[(i22 * 3) + 1];
                    int i25 = eo[(i22 * 3) + 2];
                    if (el[i23] == 1 && z[i23] != null) {
                        z[i23].h(i24, i25);
                    }
                }
                for (int i26 = 0; i26 < en.length / 4; i26++) {
                    int i27 = en[i26 << 2];
                    int i28 = en[(i26 << 2) + 1];
                    int i29 = en[(i26 << 2) + 2];
                    boolean z2 = en[(i26 << 2) + 3] != 0;
                    if (el[i27] == 1 && z[i27] != null) {
                        a(z[i27], i28, i29, z2);
                    }
                }
                return false;
            }
            if (i < 87 || i >= 162) {
                if (i != 163) {
                    if (i != 164) {
                        return i > 164;
                    }
                    d(false);
                    return false;
                }
                if (z[52] != null) {
                    j.a("/5");
                    z[52].a(0, j.e(0));
                    j.e();
                }
                j.a("/4");
                for (int i30 = 0; i30 < eh.length; i30++) {
                    for (int i31 = 0; i31 < ei[i30]; i31++) {
                        if (z[eh[i30]] != null) {
                            z[eh[i30]].a(i31, j.e(i31));
                        }
                    }
                }
                j.e();
                return false;
            }
            int i32 = i - 87;
            if (z[i32] != null && el[i32] == 1) {
                if (i32 == 23) {
                    int i33 = aj == 5 ? 1 : 0;
                    if (em[i32] != 0) {
                        z[i32].a(i33, 0, -1, -1);
                        z[i32].a(true);
                    }
                    z[i32].l(i33);
                }
                if ((em[i32] & 512) != 0) {
                    z[i32].b(true);
                    return false;
                }
                if ((em[i32] & 128) != 0) {
                    for (int i34 = 0; i34 < z[i32].k; i34++) {
                        z[i32].a(i34, 0, -1, -1);
                    }
                } else if ((em[i32] & 127) != 0) {
                    for (int i35 = 0; i35 < 7 && i35 < z[i32].k; i35++) {
                        if ((em[i32] & (1 << i35)) != 0) {
                            z[i32].a(i35, 0, -1, -1);
                        }
                    }
                }
                if ((em[i32] & 256) != 0) {
                    z[i32].a(true);
                }
            }
            return false;
        } catch (Exception unused) {
            return false;
        }
    }

    private static void W() throws InterruptedException {
        V();
        ek = null;
        aU = null;
        dJ = null;
        dK = null;
        i.bV = 0;
        i.bW = false;
        i.bX = 0;
        i.D();
        by = null;
        eH = null;
        bz = null;
        X();
        aO = 0;
        aP = null;
        bY = null;
        bZ = null;
        ca = null;
        if (dA != null) {
            dA.aa = null;
            dA = null;
        }
        for (int i = 0; i < 75; i++) {
            if (z[i] != null && i != 12 && i != 11 && i != 54 && i != 39) {
                z[i] = null;
            }
        }
        for (int i2 = 0; i2 < ba; i2++) {
            if (bd[i2] != null) {
                bd[i2].aa = null;
                bd[i2] = null;
            }
        }
        if (ae != null) {
            ae.aa = null;
        }
        ae = null;
        de = false;
        j.b(4, false);
        try {
            Thread.sleep(20L);
        } catch (Exception unused) {
        }
    }

    public static void a(boolean z2) {
        i iVarQ;
        int i = 0;
        if (z2) {
            i = aS.aA;
        } else {
            i.bV = 0;
        }
        i.bW = false;
        i.bX = 0;
        e.b();
        e.b();
        V();
        d(z2);
        if (z2 && ((i & 256) != 0 || (i & 16) != 0)) {
            aS.aA |= 256;
        }
        g.b = null;
        g.a = null;
        g.h = null;
        g.c = null;
        g.d = null;
        g.g = null;
        g.j = false;
        C = null;
        D = null;
        aD = null;
        if (!z2) {
            X();
            try {
                I(aj);
            } catch (Exception unused) {
            }
        }
        if (z2) {
            K();
            o(1);
            if (a(bA, 16) != 0) {
                if (G > 0 && (iVarQ = q(G)) != null && iVarQ.ax == 5) {
                    iVarQ.P |= 16;
                    iVarQ.N();
                }
                aS.ak = a(bA, 18);
                aS.al = a(bA, 20);
                aS.av = a(bA, 22) == 1;
                g.J = a(bA, 24);
                g.I = a(bA, 26);
                q();
                ap[0] = a(bA, 36);
                ap[3] = a(bA, 38);
                ap[2] = a(bA, 40) << 4;
                ap[4] = a(bA, 42);
                ap[5] = a(bA, 52 + (aj << 1));
                ax = (byte) a(bA, 28);
                ay = (byte) a(bA, 30);
                az = a(bA, 32);
                aN = (byte) a(bA, 34);
                aL = a(bA, 50);
                aZ = bA[68] != 0;
                i.bn = bA[79] != 0;
                for (int i2 = 0; i2 < i.br.length; i2++) {
                    i.br[i2] = bA[i2 + 76] != 0;
                }
                if (aL != -1) {
                    aJ = 1;
                    aK = -40;
                }
            } else {
                L();
                F(aj);
                az = 0;
                ax = dB;
                az = dD;
                ay = dC;
                aN = dF;
            }
        } else {
            L();
            F(aj);
            a(bA, 16, (short) 0);
            az = 0;
            ax = dB;
            ay = dC;
            az = dD;
            aN = dF;
        }
        g.e(ax);
        C();
        T();
        l(8);
        if (bG >= 0) {
            B();
        }
    }

    private static byte[] H(int i) {
        byte[] bArrE = j.e(i);
        byte[] bArrE2 = j.e(i + 1);
        bp = (bArrE2[0] & 255) + ((bArrE2[1] & 255) << 8);
        bq = (bArrE2[2] & 255) + ((bArrE2[3] & 255) << 8);
        new StringBuffer().append("map width  : ").append(bp).toString();
        new StringBuffer().append("map height : ").append(bq).toString();
        return bArrE;
    }

    private static void I(int i) throws IOException {
        j.a(ed[i]);
        if (bh[i] != 2) {
            eu = H(8);
            ev = j.e(10);
        }
        bt = bp;
        bu = bq;
        j.e();
        j.a(ec[i]);
        et = H(1);
        if (bh[aj] != 3) {
            ep = H(4);
            eq = j.e(6);
        }
        if (bh[i] == 4 || bh[aj] == 3) {
            er = H(11);
            es = j.e(13);
            ew = bp;
        }
        br = bp * 20;
        bs = bq * 20;
        j.e();
        for (int i2 = 0; i2 < et.length; i2++) {
            if (et[i2] == -1) {
                et[i2] = 0;
            }
        }
    }

    private static void X() {
        ep = null;
        eq = null;
        er = null;
        es = null;
        et = null;
        eu = null;
        ev = null;
    }

    public static int g(int i, int i2) {
        if (i < 0 || i >= bp || i2 >= bq) {
            return 20;
        }
        if (bh[aj] == 3) {
            if (i2 < 0) {
                return 0;
            }
        } else if (i2 < 0) {
            return 20;
        }
        if (bh[aj] != 3) {
            return et[i + (i2 * bp)];
        }
        return et[dL[i % 21][i2 % 13]];
    }

    public static int h(int i, int i2) {
        if (i == 0 && i2 == 0) {
            return 0;
        }
        if (i < 0) {
            i = -i;
        }
        if (i2 < 0) {
            i2 = -i2;
        }
        int i3 = i > i2 ? i2 : i;
        return (((i + i2) - (i3 >> 1)) - (i3 >> 2)) + (i3 >> 3);
    }

    public static int e(int i, int i2, int i3, int i4) {
        int i5 = -i;
        int iD = (1 != 0 && (i2 * i2) - ((1 * 4) * i5) >= 0) ? (j.d((i2 * i2) - ((1 * 4) * i5)) - i2) / (1 * 2) : -1;
        int i6 = iD;
        int i7 = -i;
        int i8 = (1 != 0 && (i2 * i2) - ((1 * 4) * i7) >= 0) ? ((-j.d((i2 * i2) - ((1 * 4) * i7))) - i2) / (1 * 2) : -1;
        int i9 = i8;
        if (i6 > 0 || i9 > 0) {
            return (i4 / (i6 > i9 ? i6 : i9)) << 8;
        }
        return -1;
    }

    private static b J(int i) {
        b bVar = new b();
        byte[] bArrE = j.e(i);
        if (bArrE.length == 0) {
            return null;
        }
        bVar.a(bArrE, 0);
        return bVar;
    }

    public static b r(int i) {
        if (i < 75 && i >= 0) {
            return z[i];
        }
        new StringBuffer().append("ERROR : sprite index [").append(i).append("] is out of range").toString();
        return null;
    }

    private static int a(byte[] bArr, int i, int i2) {
        int i3 = i + 1;
        bArr[i] = (byte) i2;
        return i3;
    }

    static int a(byte[] bArr, int i, short s) {
        int i2 = i + 1;
        bArr[i] = (byte) s;
        int i3 = i2 + 1;
        bArr[i2] = (byte) (s >>> 8);
        return i3;
    }

    private static int b(byte[] bArr, int i, int i2) {
        int i3 = i + 1;
        bArr[i] = (byte) i2;
        int i4 = i3 + 1;
        bArr[i3] = (byte) (i2 >>> 8);
        int i5 = i4 + 1;
        bArr[i4] = (byte) (i2 >>> 16);
        int i6 = i5 + 1;
        bArr[i5] = (byte) (i2 >>> 24);
        return i6;
    }

    static short a(byte[] bArr, int i) {
        return (short) ((bArr[i] & 255) | ((bArr[i + 1] & 255) << 8));
    }

    private static int b(byte[] bArr, int i) {
        int i2 = i + 1;
        int i3 = bArr[i] & 255;
        int i4 = i2 + 1;
        return i3 | ((bArr[i2] & 255) << 8) | ((bArr[i4] & 255) << 16) | ((bArr[i4 + 1] & 255) << 24);
    }

    private static void K(int i) {
        bw = -1;
        bv = i;
        ey = eA[i].length;
        eD = 0;
        switch (i) {
            case 0:
                eB = 0;
                if (Y()) {
                    eA[0][0] = 2;
                } else {
                    eA[0][0] = 117;
                }
                if (!Z()) {
                    ey--;
                    break;
                }
                break;
            case 1:
                eB = 72;
                break;
            case 2:
                eB = 71;
                bw = -1;
                if (bA[69] == 0) {
                    ey--;
                    break;
                }
                break;
            case 3:
                eB = -1;
                if (eC != -1) {
                    eD = y.k(a(y, d(0, eC), 206)[0]);
                }
                if (bx != -1) {
                    eD += y.k(a(y, d(0, bx), 206)[0]);
                    break;
                }
                break;
            case 4:
                eB = 4;
                if (j.c != 14) {
                    if (!Y()) {
                        ey--;
                        break;
                    }
                } else {
                    ey -= 5;
                    break;
                }
                break;
        }
        if (bv == 0) {
            eE = 312;
        } else if (bv == 1) {
            eE = (ey * 28) + 4 + 2;
        } else if (bv == 3) {
            eE = (ey * 36) + 4 + 2;
        } else if (bv == 1) {
            eE = (((ey * 28) + 4) + 2) - 2;
        } else if (bv == 0 || bv == 2) {
            eE = (ey * 36) + 4 + 10;
        } else {
            eE = (((ey * 30) + 4) + 2) - 4;
        }
        eE += eD;
        eF = 37 + eE;
    }

    private static boolean Y() {
        return bA[15] == 1 || bA[14] > 0;
    }

    private static boolean Z() {
        switch (f.a()) {
            case 0:
                eA[0][3] = 32;
                return true;
            case 1:
                eA[0][3] = 33;
                return true;
            case 2:
                eA[0][3] = 34;
                return true;
            default:
                return false;
        }
    }

    private static int m(int i, int i2) {
        if (i == 0 && i2 >= 3 && !Z()) {
            i2++;
        }
        if (i2 > eA[i].length - 1) {
            i2 = eA[i].length - 1;
        }
        return i2;
    }

    public static void f(int i, int i2, int i3, int i4) {
        cd.setColor(-1);
        j.b(cd, i, i2, 120, i4);
        cd.setColor(0);
        j.c(cd, i, i2, 120, i4);
    }

    private static void L(int i) {
        if (i <= 0) {
            return;
        }
        if (v(16388)) {
            int i2 = bw - 1;
            bw = i2;
            if (i2 < 0) {
                bw = 0;
            } else {
                fH = 0;
                fI = 1;
                fK.a(21, 1);
            }
            if (e.a()) {
                return;
            }
            z(23);
            return;
        }
        if (v(33024)) {
            int i3 = bw + 1;
            bw = i3;
            if (i3 >= i) {
                bw = i - 1;
            } else {
                fH = 0;
                fI = 1;
                fK.a(21, 1);
            }
            if (e.a()) {
                return;
            }
            z(23);
        }
    }

    private static b a(b bVar, int i, int i2, boolean z2) {
        if (i < 0 || i > bVar.k) {
            return bVar;
        }
        for (int i3 = 0; i3 < bVar.j[i].length; i3++) {
            if ((bVar.j[i][i3] & 16777215) != 16711935) {
                int i4 = z2 ? bVar.j[i][i3] >> 24 : 255;
                int[] iArr = bVar.j[i];
                int i5 = i3;
                iArr[i5] = iArr[i5] & 16777215;
                int[] iArr2 = bVar.j[i];
                int i6 = i3;
                iArr2[i6] = iArr2[i6] | (((((i4 & 255) * i2) / 100) & 255) << 24);
            }
        }
        return bVar;
    }

    public static int s(int i) {
        for (int i2 = 0; i2 < eH.length; i2++) {
            if (eH[i2] == i) {
                return i2;
            }
        }
        return -1;
    }

    static int t(int i) {
        return eI[i - 100];
    }

    private static void e(boolean z2) {
        try {
            RecordStore recordStoreOpenRecordStore = RecordStore.openRecordStore("/ASBR", true);
            if (recordStoreOpenRecordStore.getNumRecords() <= 0) {
                if (!z2) {
                    recordStoreOpenRecordStore.closeRecordStore();
                    return;
                }
                recordStoreOpenRecordStore.addRecord(bA, 0, 512);
            } else if (z2) {
                recordStoreOpenRecordStore.setRecord(1, bA, 0, 512);
            } else {
                recordStoreOpenRecordStore.getRecord(1, bA, 0);
            }
            recordStoreOpenRecordStore.closeRecordStore();
        } catch (Exception unused) {
            new StringBuffer().append("ERROR during RecordStore ").append(z2 ? "save" : "load").toString();
        }
    }

    @Override // defpackage.j
    protected final void keyPressed(int i) {
    }

    @Override // defpackage.j
    protected final void keyReleased(int i) {
    }

    public static boolean u(int i) {
        return (bC & i) != 0;
    }

    public static boolean v(int i) {
        return (bB & i) != 0;
    }

    public static boolean w(int i) {
        return (eM & i) != 0;
    }

    public static boolean x(int i) {
        return (bB & i) != 0 && bB == eO && eP < 5;
    }

    public static boolean t() {
        return j() || bB != 0;
    }

    public static boolean u() {
        return bB != 0 && (bB & 131072) == 0 && (bB & 262144) == 0;
    }

    public static void v() {
        eL = 0;
        bC = 0;
        bB = 0;
        eM = 0;
        eK = 0;
        eN = 0;
    }

    public static void y(int i) {
        bC &= i ^ (-1);
        bB &= i ^ (-1);
    }

    private static void b(b bVar, int i, String str, int i2, int i3, int i4, int i5, int i6, int i7) {
        a(bVar, i, str, 200, i3, i4, i5, 0, 1, true);
    }

    private static void a(b bVar, int i, String str, int i2, int i3, int i4, int i5, int i6, int i7, boolean z2) {
        int iK;
        if (v(33024)) {
            int i8 = fe - 1;
            fe = i8;
            if (i8 < -5) {
                fe = -5;
            }
        } else if (v(16388)) {
            int i9 = fe + 1;
            fe = i9;
            if (i9 == 0) {
                fe++;
            }
            if (dx || j.c == 24) {
                if (fe > -1) {
                    fe = -1;
                }
            } else if (fe > 2) {
                fe = 2;
            }
        }
        if (fe == 0) {
            fe = -1;
        }
        if (!z2 && fe >= 0) {
            fe = -1;
        }
        short[] sArr = null;
        if (z2) {
            short[] sArrA = a(bVar, str, i4);
            sArr = sArrA;
            iK = bVar.k(sArrA[0]);
        } else {
            bVar.a(str, (char[]) null);
            iK = b.e;
        }
        if (j.c == 24) {
            if (dw < 30 && fd < (-iK) + 160) {
                dw = 30;
            }
        } else if (fd < i3 - iK) {
            if (dx) {
                if (bA[69] != 0) {
                    l(25);
                } else {
                    a(bA, 69, 1);
                    e(true);
                    l(1);
                }
                dw = 255;
                return;
            }
            fd = 240;
        } else if (fe > 0 && fd >= i5) {
            fe = -1;
        }
        if (j.c != 24 || fd >= (-iK) + 160) {
            fd += fe;
        }
        j.a(cd, 0, i3, 400, i5, true);
        bVar.l(i);
        if (!z2 || sArr == null) {
            bVar.a(cd, str, i2, fd, i7);
        } else {
            bVar.a(cd, str, sArr, i2, fd, i6, 200, i7, -1);
        }
    }

    public static void z(int i) {
        if (i < 0 || i >= 34) {
            return;
        }
        e.a(i, false);
    }

    public static void A(int i) {
        z(i);
    }

    public static void w() {
        e.b();
    }

    static boolean x() {
        return e.a();
    }

    private static void aa() {
        j.a.setColor(0);
        if (!an) {
            int i = fn - 1;
            fn = i;
            if (i >= 0) {
                j.b(j.a, 0, 0, 400, 120 - ((fl - fn) * fm));
                j.b(j.a, 0, 120 + (((fl - fn) - 1) * fm), 400, 120 - (((fl - fn) - 1) * fm));
                return;
            }
            return;
        }
        int i2 = fn + 1;
        fn = i2;
        if (i2 > fl) {
            fn = fl;
        }
        if (fn <= fl) {
            j.b(j.a, 0, 0, 400, fm * fn);
            j.b(j.a, 0, 240 - (fm * fn), 400, fm * fn);
        }
    }

    public static void B(int i) {
        an = true;
        ao = false;
        bI = 0;
        fk = 26;
    }

    public static void C(int i) {
        ao = true;
        an = false;
        bI = 255;
        fk = 26;
    }

    private static void ab() {
        if (z[58] == null) {
            return;
        }
        fv = z[58].e(z[58].d(0, 0));
        fw = z[58].e(z[58].d(1, 0));
        fx = z[58].f(z[58].d(0, 0));
        for (int i = 0; i < 2; i++) {
            ft[i << 1] = 0 + (fv * i);
            ft[(i << 1) + 1] = 255;
            fu[i << 1] = 0 + (fw * i);
            fu[(i << 1) + 1] = 255 + fx;
        }
    }

    @Override // defpackage.j
    protected final void d() {
        if (fy) {
            cb = true;
            try {
                fy = false;
                if (bv == 3) {
                    switch (eC) {
                        case 13:
                        case 19:
                        case 69:
                        case 73:
                            bw = -1;
                            break;
                        case 25:
                            if (j.c == 12 || j.c == 13) {
                                bw = 0;
                            } else {
                                bw = 1;
                            }
                            bw = -1;
                            break;
                    }
                } else if (j.c == 8 || j.c == 21) {
                    if (J()) {
                        l(14);
                    } else if (C != null && C.cd != null && C.cd[6]) {
                        C.Z();
                    }
                } else if (j.c == 14) {
                    bw = 0;
                } else if (j.c == 21 && u == 8) {
                    l(14);
                }
                if (j.c != 14) {
                    if (bG == 1 || bG != -1) {
                        z(bG);
                    }
                } else if (bG != -1) {
                    fi = bG;
                }
                cb = true;
            } catch (Exception unused) {
            }
            fy = false;
            v();
        }
        super.d();
    }

    @Override // defpackage.j
    protected final void c() {
        if (!fy) {
            fy = true;
            v();
            if (j.c == 8 && C != null && C.cd != null && C.cd[6]) {
                C.Y();
            }
            if (!e.a() || fj >= 10) {
                bG = -1;
            } else {
                bG = bH;
            }
            try {
                e.b();
            } catch (Exception unused) {
            }
        }
        super.c();
    }

    private static void ac() {
        for (int i = 0; i < fB; i++) {
            for (int i2 = 0; i2 < b.b[i].length; i2++) {
                short s = b.b[i][i2];
                b.b[i][i2] = -1;
                if (b.c[i][i2] != null) {
                    b.c[i][i2].e(s >> 10, s & 1023);
                    b.c[i][i2] = null;
                }
            }
        }
    }

    private static void ad() {
        fB = 0;
        fz = 0;
        int i = fB + 1;
        fB = i;
        fA = i;
        int i2 = fB + 1;
        fB = i2;
        b.i(i2);
        b.f(fz, 100);
        b.f(fA, 300);
    }

    private static void d(int i, int i2, int i3) {
        b(i, i2, i3, false, false);
    }

    private static void b(int i, int i2, int i3, boolean z2) {
        b(93, i2, 214, true, false);
    }

    private static void a(int i, int i2, int i3, boolean z2, boolean z3) {
        int i4;
        int i5;
        int i6;
        if (z3) {
            if (z2) {
                i4 = 16;
                i5 = 17;
            } else {
                i4 = 14;
                i5 = 15;
            }
        } else if (z2) {
            i4 = 12;
            i5 = 13;
        } else {
            i4 = 10;
            i5 = 11;
        }
        fM = A[2].e(A[2].d(i4, 0));
        fN = A[2].e(A[2].d(i5, 0));
        A[2].a(cd, i4, 0, i, i2, 0, 0, 0);
        int i7 = i + fM;
        do {
            A[2].a(cd, i5, 0, i7, i2, 0, 0, 0);
            i6 = i7 + fN;
            i7 = i6;
        } while (i6 + fN < i + i3);
        A[2].a(cd, i4, 0, i + i3, i2, 1, 0, 0);
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:101:0x04ce  */
    /* JADX WARN: Removed duplicated region for block: B:102:0x04f9  */
    /* JADX WARN: Removed duplicated region for block: B:135:0x0687  */
    /* JADX WARN: Removed duplicated region for block: B:138:0x0690  */
    /* JADX WARN: Removed duplicated region for block: B:145:0x0736  */
    /* JADX WARN: Removed duplicated region for block: B:148:0x075a  */
    /* JADX WARN: Removed duplicated region for block: B:46:0x01bb  */
    /* JADX WARN: Removed duplicated region for block: B:80:0x03c2  */
    /* JADX WARN: Removed duplicated region for block: B:98:0x04af  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static void b(int i, int i2, int i3, boolean z2, boolean z3) {
        int i4;
        int i5;
        boolean zD;
        String strD;
        StringBuffer stringBufferAppend;
        int i6;
        int i7;
        int i8;
        if (fJ == null) {
            a aVar = new a(A[2], 0, 0);
            fJ = aVar;
            aVar.a(18, -1);
        }
        if (fK == null) {
            a aVar2 = new a(A[2], 0, 0);
            fK = aVar2;
            aVar2.a(21, 1);
        }
        int i9 = i2 + 10;
        int i10 = 8;
        if (8 > ey) {
            i10 = ey;
        }
        int i11 = 0;
        if (z3) {
            i11 = 40;
        }
        if (z2) {
            j.h(-856756498);
            j.d(j.a, i, i2, i3, (i10 * 33) + 20 + i11);
            j.h(-2013265920);
            j.d(j.a, i - 2, i2 - 2, 2, (i10 * 33) + 20 + 4 + i11);
            j.d(j.a, i + i3, i2 - 2, 2, (i10 * 33) + 20 + 4 + i11);
            j.d(j.a, i, i2 - 2, 95, 2);
            j.d(j.a, i, i2 + (i10 * 33) + 20 + i11, 95, 2);
            j.d(j.a, i + 108, i2 - 2, i3 - 108, 2);
            j.d(j.a, i + 108, i2 + (i10 * 33) + 20 + i11, i3 - 108, 2);
        }
        j.h(805306368);
        j.d(j.a, i, i2, i3, 10);
        if (z2) {
            j.h(-2013265920);
            j.d(j.a, i + 95, i2 - 2, 13, 2);
        }
        if (z3) {
            j.h(805306368);
            j.d(j.a, i, i9, i3, 40);
            i9 += 40;
        }
        int i12 = i9;
        int i13 = 0;
        while (i13 < i10) {
            int iM = m(bv, i13);
            if (i13 == 0 && j.c == 2) {
                i4 = 35;
            } else {
                i4 = 30;
                if ((bv == 4 && j.c != 14) || j.c == 19) {
                    i5 = 135;
                }
                if (i13 == 1 && j.c == 2) {
                    i9 += 13;
                }
                zD = d(i, i9, i3, i4);
                if (zD) {
                    j.h(1879048192);
                    j.d(j.a, i, i9, i3, i4);
                    a(i + ((i3 - i5) >> 1), i9 + (i4 >> 1), i5, false, i13 == 0 && j.c == 2);
                    if (j.c == 30) {
                        A[2].a(cd, i13 + 5, 0, i + 40, i9 + (i4 >> 1), 0, 0, 0);
                    } else if (i13 == 0 && j.c == 2) {
                        A[2].a(cd, 9, 0, i + 40, i9 + (i4 >> 1), 0, 0, 0);
                    } else {
                        A[2].a(cd, 5, 0, i + 40, i9 + (i4 >> 1), 0, 0, 0);
                    }
                    bW.l(0);
                } else {
                    if (i13 == 0 && j.c == 2) {
                        if (fJ.e != 19) {
                            fJ.a(19, -1);
                        }
                    } else if (fJ.e != 18) {
                        fJ.a(18, -1);
                    }
                    a aVar3 = fJ;
                    aVar3.a = (i + i3) - ((i3 - i5) >> 1);
                    aVar3.b = i9;
                    fJ.b(j.f);
                    if (fI > 0) {
                        j.h(1879048192);
                        j.d(j.a, i, i9, i3, i4);
                        j.a(cd, 0, i9 + ((i4 - fI) >> 1), 400, fI, true);
                        fI += fH;
                        fH += 8;
                        if (fI >= i4) {
                            fI = 0;
                        }
                    }
                    fJ.c();
                    j.a.setColor(-16777216);
                    j.b(j.a, i, i9, (i3 + i5) >> 1, i4);
                    a((i + ((i3 - i5) >> 1)) - 2, i9 + (i4 >> 1), i5 + 4, true, i13 == 0 && j.c == 2);
                    if (j.c == 30) {
                        A[2].a(cd, i13 + 0, 0, i + 40, i9 + (i4 >> 1), 0, 0, 0);
                    } else if (i13 == 0 && j.c == 2) {
                        A[2].a(cd, 4, 0, i + 40, i9 + (i4 >> 1), 0, 0, 0);
                    } else {
                        A[2].a(cd, 0, 0, i + 40, i9 + (i4 >> 1), 0, 0, 0);
                    }
                    fK.b(j.f);
                    if (fK.b()) {
                        fK.a(20, -1);
                    }
                    a aVar4 = fK;
                    aVar4.a = i;
                    aVar4.b = i9 + (i4 >> 1);
                    fK.c();
                    bW.l(1);
                }
                int i14 = i + (i3 >> 1);
                if (j.c == 19) {
                    i14 = i + (((((i3 - i5) >> 1) + 25) + 145) >> 1);
                }
                if (j.c == 19) {
                    if (i13 == 0 && j.c == 2) {
                        iM = 0;
                    }
                    strD = d(0, eA[bv][iM]);
                    switch (eA[bv][iM]) {
                        case 32:
                        case 33:
                        case 34:
                            if (!eJ && j.g % 10 > 5 && j.g % 10 > 5) {
                                z[12].a(cd, 1, 0, i14 + 86, (i9 + (i4 >> 1)) - 7, 0, 0, 0);
                            }
                            bW.l(3);
                            break;
                        case 83:
                            strD = new StringBuffer().append(strD).append(": ").append(d(0, bE ? ff[1] : ff[0])).toString();
                            break;
                        case 84:
                            strD = new StringBuffer().append(strD).append(": ").append(d(0, bF ? fg[1] : fg[0])).toString();
                            break;
                        case 97:
                            stringBufferAppend = new StringBuffer().append(strD).append(": ");
                            i6 = 0;
                            i7 = 35;
                            i8 = au;
                            strD = stringBufferAppend.append(d(i6, i7 + i8)).toString();
                            break;
                        case 103:
                            bW.l(3);
                            break;
                        case 123:
                            stringBufferAppend = new StringBuffer().append(strD).append(": ");
                            i6 = 0;
                            i7 = 124;
                            i8 = k() ? 0 : 1;
                            strD = stringBufferAppend.append(d(i6, i7 + i8)).toString();
                            break;
                    }
                } else {
                    strD = new StringBuffer().append(d(0, 10)).append(" ").append(Integer.toString(i13 + 1)).toString();
                }
                String strA = a(strD, zD, i5 - 50);
                int i15 = 0;
                if (j.c == 19) {
                    i15 = -3;
                }
                if (zD) {
                    bW.a(cd, strA, i14, i9 + (i4 >> 1) + i15, 3);
                } else {
                    if (j.c == 30) {
                        bL = i13;
                    }
                    if (j.c == 19) {
                        y.l(1);
                        y.a(cd, d(0, eX[eW[i13]]), i14 - ez, i9 + (i4 >> 1) + 10 + i15, 3);
                    }
                    j.a(cd, (i14 - (i5 >> 1)) + 25, i9, i5 - 50, 240, true);
                    bW.a(cd, strA, i14 - ez, i9 + (i4 >> 1) + i15, 3);
                    j.a(cd, 0, 0, 400, 240, true);
                }
                if (c(i, i9, i3, i4)) {
                    bw = i13;
                    if (j.c == 30) {
                        bL = bw;
                    }
                    E(32);
                }
                if ((bv != 4 && j.c != 14) || j.c == 19) {
                    int i16 = i10 / 2;
                    if (i10 % 2 == 0) {
                        i16--;
                    }
                    if (i13 == i16 && i13 < i10 - 1) {
                        j.h(805306368);
                        j.d(j.a, i, i9 + i4, i3, 10);
                        i = 206;
                        i9 = i12 - (i4 + 3);
                        j.h(805306368);
                        j.d(j.a, 206, ((i9 + i4) + 3) - 10, i3, 10);
                    }
                }
                i9 += i4 + 3;
                j.a(cd, 0, 0, 400, 240, true);
                i13++;
            }
            i5 = 170;
            if (i13 == 1) {
                i9 += 13;
            }
            zD = d(i, i9, i3, i4);
            if (zD) {
            }
            int i142 = i + (i3 >> 1);
            if (j.c == 19) {
            }
            if (j.c == 19) {
            }
            String strA2 = a(strD, zD, i5 - 50);
            int i152 = 0;
            if (j.c == 19) {
            }
            if (zD) {
            }
            if (c(i, i9, i3, i4)) {
            }
            if (bv != 4) {
            }
            i9 += i4 + 3;
            j.a(cd, 0, 0, 400, 240, true);
            i13++;
        }
        j.h(805306368);
        j.d(j.a, i, i9 - 3, i3, 10);
        if (z2) {
            j.h(-2013265920);
            j.d(j.a, i + 95, i2 + (i10 * 33) + 20 + i11, 13, 2);
        }
        if (j.c == 2) {
            if (c(10, 167, 36, 27)) {
                bw = 0;
                l(5);
            }
            if (d(10, 167, 36, 27)) {
                a(10, 193, 36, true);
                A[2].a(cd, 27, 0, 28, 180, 0, 0, 0);
            } else {
                a(10, 193, 36, false);
                A[2].a(cd, 22, 0, 28, 180, 0, 0, 0);
            }
            if (c(10, 204, 36, 27)) {
                l(3);
                K(4);
            }
            if (d(10, 204, 36, 27)) {
                a(10, 230, 36, true);
                A[2].a(cd, 28, 0, 28, 217, 0, 0, 0);
            } else {
                a(10, 230, 36, false);
                A[2].a(cd, 23, 0, 28, 217, 0, 0, 0);
            }
            if (c(354, 204, 36, 27)) {
                fG = false;
                l(28);
                eC = 13;
                K(3);
                eB = 8;
            }
            if (d(354, 204, 36, 27)) {
                a(354, 230, 36, true);
                A[2].a(cd, 31, 0, 372, 217, 0, 0, 0);
            } else {
                a(354, 230, 36, false);
                A[2].a(cd, 26, 0, 372, 217, 0, 0, 0);
            }
        }
    }

    private static void f(boolean z2) {
        if (z2 || A[1] == null) {
            return;
        }
        A[1].a(cd, 1, 0, 0, 0, 0, 0, 0);
    }

    private static void ae() throws InterruptedException {
        f(false);
        if (eC == 121) {
            bW.a(cd, d(0, eC), a(bW, d(0, eC), 200), 200, 120, 0, 100, 3, -1);
            if (v(131072)) {
                fE = 255;
                fO = 3;
                K(4);
                bw = -1;
                l(3);
                z(30);
            }
            a("", d(0, 17));
            return;
        }
        if (eB > 0 && j.c != 23) {
            d(0, eB);
        }
        d(93, 120, 214);
        bW.l(1);
        bW.a(cd, d(0, eC), a(bW, d(0, eC), 200), 200, 80, 0, 100, 3, -1);
        a(d(0, 79), (bv == 0 || j.c == 23 || j.c == 13) ? "" : d(0, 17));
        L(ey);
        Q();
    }

    private static void af() {
        f(false);
        switch (fO) {
            case 0:
                fC = 20;
                fE = 0;
                fQ = 0;
                if (dt || bA[69] != 0) {
                    da = 8;
                } else {
                    da = bA[14] + 1;
                }
                for (int i = 0; i < 4; i++) {
                    if (da > fP[i]) {
                        fQ++;
                    }
                }
                ey = fQ;
                bL = 0;
                fR = -1;
                fO = 1;
                break;
        }
        d(93, 46, 214);
        if (fC > 0 && fC != 255) {
            int i2 = fC + 20;
            fC = i2;
            if (i2 >= 255) {
                fC = 255;
            }
        }
        if (fR != -1) {
            if (fE > 20) {
                fE -= 20;
            } else {
                fR = -1;
            }
        }
        a(d(0, 79), d(0, 17));
        if (v(327712)) {
            if (fF == 20) {
                l(20);
            } else {
                l(9);
            }
            fF = 0;
            z(23);
            return;
        }
        if (v(131072)) {
            if (fF == 19) {
                fO = 3;
                l(19);
            } else {
                l(2);
            }
            fF = 0;
            z(30);
            return;
        }
        if (fQ > 1) {
            if (v(16388)) {
                fR = bL;
                int i3 = bL - 1;
                bL = i3;
                if (i3 < 0) {
                    bL = 0;
                } else {
                    fC = 20;
                    fE = 255;
                    bw = bL;
                    fH = 0;
                    fI = 1;
                    fK.a(21, 1);
                }
                if (e.a()) {
                    return;
                }
                z(23);
                return;
            }
            if (v(33024)) {
                fC = 20;
                fE = 255;
                fR = bL;
                int i4 = bL + 1;
                bL = i4;
                if (i4 >= fQ) {
                    bL = fQ - 1;
                } else {
                    fC = 20;
                    fE = 255;
                    bw = bL;
                    fH = 0;
                    fI = 1;
                    fK.a(21, 1);
                }
                if (e.a()) {
                    return;
                }
                z(23);
            }
        }
    }

    private static String a(String str, boolean z2, int i) {
        bW.a(str, (char[]) null);
        int i2 = b.d;
        if (!z2) {
            int length = str.length() - 3;
            while (length > 0 && i2 > i) {
                length--;
                str = new StringBuffer().append(str.substring(0, length)).append("...").toString();
                bW.a(str, (char[]) null);
                i2 = b.d;
            }
        } else if (i2 > i) {
            int i3 = ez + 2;
            ez = i3;
            if (i3 > i2) {
                ez = -i;
            }
        } else {
            ez = 0;
        }
        return str;
    }

    private static void ag() throws IOException {
        if (A[1] == null) {
            j.a("/2");
            try {
                A[1] = J(1);
                A[1].a(0, 0, -1, -1);
                A[1].a(true);
            } catch (Exception unused) {
            }
            j.e();
        }
        f(false);
        int i = 1;
        while (i < fP.length && aj + 1 != fP[i]) {
            i++;
        }
        u = 8;
        i(0, 120);
        A[4].a(cd, i + 4, 0, 200, 119, 0, 0, 0);
        a(y, 0, d(0, 110), 200, 150, 380, 240, 0, 3);
        if (v(327712) || j()) {
            l(15);
            z(23);
        }
        y.l(1);
        if (j.g % 10 < 5) {
            y.a(cd, d(0, 9), 200, 220, 3);
        }
    }

    public static void y() {
        fS = 0;
    }

    private static void ah() throws IOException {
        if (A[1] == null) {
            j.a("/2");
            try {
                A[1] = J(1);
                A[1].a(0, 0, -1, -1);
                A[1].a(true);
            } catch (Exception unused) {
            }
            j.e();
        }
        f(false);
        if (z[73] == null) {
            j.a("/3");
            try {
                z[73] = J(73);
            } catch (Exception unused2) {
            }
            j.e();
        }
        A[3].a(cd, 0, 0, 200, 50, 0, 0, 0);
        A[3].a(cd, 2, 0, 140, 50, 0, 0, 0);
        bW.l(0);
        bW.a(cd, d(0, 113), 210, 43, 17);
        cd.setColor(-15196640);
        j.b(cd, 114, 59, 172, 155);
        cd.setColor(0);
        for (int i = 0; i < 3; i++) {
            j.b(cd, 114, 70 + (i * 45), 172, 40);
        }
        int i2 = 0;
        if (ex == 3) {
            for (int i3 = 0; i3 < 3; i3++) {
                int i4 = i3 * 45;
                if (cc[i3] == 2) {
                    z[73].a(cd, i3 + 0, 0, 140, i4 + 91, 0, 0, 0);
                    y.l(2);
                    y.a(cd, d(0, i3 + 114), 164, i4 + 91, 6);
                } else {
                    z[73].a(cd, 3, 0, 140, i4 + 91, 0, 0, 0);
                    y.l(4);
                    y.a(cd, d(0, i3 + 114), 164, i4 + 91, 6);
                }
            }
        } else {
            for (int i5 = 0; i5 < 3; i5++) {
                if (cc[i5] == 1) {
                    int i6 = i2 * 45;
                    z[73].a(cd, i5 + 0, 0, 140, i6 + 91, 0, 0, 0);
                    y.l(2);
                    y.a(cd, d(0, i5 + 114), 164, i6 + 91, 6);
                    i2++;
                }
            }
        }
        if (ex == 3) {
            if (v(131072)) {
                l(3);
                K(4);
                z(30);
            }
            a("", d(0, 17));
            return;
        }
        if (j.g < 10) {
            j.h(((10 - j.g) * 25) << 24);
            j.d(cd, 0, 0, 400, 240);
        }
        if (j.g >= 10 && (v(327712) || j())) {
            for (int i7 = 0; i7 < 3; i7++) {
                if (cc[i7] == 1) {
                    cc[i7] = 2;
                }
                j.a(bA, i7 + 130, (byte) cc[i7]);
            }
            boolean z2 = false;
            int i8 = 1;
            while (true) {
                if (i8 >= fP.length) {
                    break;
                }
                if (aj + 1 == fP[i8]) {
                    z2 = true;
                    break;
                }
                i8++;
            }
            boolean z3 = bA[15] == 1;
            if (!z2 || z3) {
                l(15);
            } else {
                l(10);
            }
            z(23);
        }
        y.l(1);
        if (j.g % 10 < 5) {
            y.a(cd, d(0, 9), 200, 220, 3);
        }
    }

    /* JADX WARN: Type inference failed for: r0v184, types: [int[], int[][]] */
    /* JADX WARN: Type inference failed for: r0v198, types: [int[], int[][]] */
    /* JADX WARN: Type inference failed for: r0v215, types: [int[], int[][]] */
    static {
        int[] iArr = {15, 17, 14, 16};
        boolean[] zArr = {true, false, false, false, false, false, false, false, false};
        boolean[] zArr2 = {false, false, false, false, false, false, true, true, true};
    }
}
