package defpackage;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:g.class */
public final class g extends i {
    public static i a;
    public static i b;
    public static i c;
    public static i d;
    public static i e;
    public static i f;
    public static i g;
    private static i ci;
    public static i h;
    public static boolean i;
    public static boolean j;
    public static int k;
    public static int l;
    public static int m;
    public static int n;
    public static int o;
    public static int p;
    public static boolean q;
    public static boolean r;
    public static boolean s;
    public static int t;
    static final int[] u = null;
    private static int[] cj;
    private static int[] ck;
    private boolean cl;
    public static boolean v;
    static int[] w;
    private static boolean cm;
    public static int[] x;
    public static int y;
    private static int cn;
    private static int co;
    private static boolean cp;
    private static boolean cq;
    private static boolean cr;
    private static boolean cs;
    public static boolean z;
    private static boolean ct;
    private static boolean cu;
    private static boolean cv;
    private static boolean cw;
    public static boolean A;
    public static boolean B;
    public static boolean C;
    public static boolean D;
    public static boolean E;
    private int cx;
    private int cy;
    private int cz;
    private int cA;
    private int cB;
    private int cC;
    private int cD;
    private int cE;
    private int cF;
    private boolean cG;
    public static i F;
    private int cH;
    private int cI;
    private int cJ;
    private int cK;
    private int cL;
    private int cM;
    public static final int[] G = null;
    public static final int[] H = null;
    public static int I;
    public static int J;
    public int K;
    private int cN;
    public int L;
    public int M;

    g() {
        this.cl = false;
        this.cx = (8 * j.m) / 360;
        this.cy = 0;
        this.cz = 0;
        this.cA = 0;
        this.cB = 0;
        this.cC = 0;
        this.cD = 0;
        this.cE = 0;
        this.cF = 5120;
        this.cG = this.av;
        this.cH = 0;
        this.cI = 0;
        this.cJ = 0;
        this.cK = 0;
        this.cL = 0;
        this.cM = 0;
        this.K = 0;
        this.cN = 0;
        this.L = 0;
        this.M = 0;
    }

    g(short[] r5) {
        super(r5);
        this.cl = false;
        this.cx = (8 * j.m) / 360;
        this.cy = 0;
        this.cz = 0;
        this.cA = 0;
        this.cB = 0;
        this.cC = 0;
        this.cD = 0;
        this.cE = 0;
        this.cF = 5120;
        this.cG = this.av;
        this.cH = 0;
        this.cI = 0;
        this.cJ = 0;
        this.cK = 0;
        this.cL = 0;
        this.cM = 0;
        this.K = 0;
        this.cN = 0;
        this.L = 0;
        this.M = 0;
    }

    public final void a(int r5) {
        a(43, 32);
        this.al += 10;
        this.ah = r5;
        this.aj = 1536;
        a = null;
        this.ac = null;
    }

    public static boolean a() {
        return a(g);
    }

    public static boolean a(i r3) {
        if (i.bh == 0) goto L5;
        return false;
    L5:
        if (h() == false) goto L7;
        return false;
    L7:
        if (g() == false) goto L9;
        return true;
    L9:
        if (r3 != null) goto L11;
    L16:
        d(u[k.au]);
        return true;
    L11:
        if (r3.ax != 61) goto L16;
        int r32 = r3.W();
        if (k.aS.S != 6) goto L15;
        r32 = r32 / 3;
    L15:
        d(r32);
        return true;
    }

    private void aj() {
        if (a(cj, true) == false) goto L6;
        return;
    L6:
        if (a(ck, false) == false) goto L8;
        return;
    }

    private boolean a(int[] r6, boolean r7) {
        int r0 = r6.length / 3;
        int r9 = 0;
    L4:
        if (r9 >= (r0 - 1)) goto L33;
        if (r7 == false) goto L12;
        if (r9 < (r0 - 2)) goto L12;
        return false;
    L12:
        if (this.R != (-1)) goto L17;
        if (this.S != r6[r9]) goto L32;
        if (k.v(r6[r9 + (r0 << 1)]) == true) goto L17;
    L32:
        r9 = r9 + 1;
    L17:
        this.cl = false;
        if (this.T < r6[r9 + r0]) goto L21;
        this.cl = true;
    L21:
        if (this.R == (-1)) goto L23;
        return true;
    L23:
        if (this.cl == false) goto L25;
        return true;
    L25:
        if (r7 == true) goto L27;
    L29:
        this.R = r6[r9 + 1];
        return true;
    L27:
        if (r0 < 3) goto L29;
        int r1 = r0 - 3;
        goto L29
    L33:
        return false;
    }

    private boolean ak() {
        int r0 = (this.W[1] + 10) / 20;
        if (this.av == false) goto L5;
        int r02 = (this.W[0] - 5) / 20;
        int r6 = r02;
        int r8 = r02 + 1;
    L7:
        if (e(r6, r0) >= 19) goto L9;
        return false;
    L9:
        if (this.Q != 61) goto L11;
        return false;
    L11:
        if (e(r6, r0 - 1) <= 0) goto L13;
        return false;
    L13:
        if (e(r8, r0) <= 0) goto L15;
        return false;
    L15:
        if (e(r8, r0 - 1) <= 0) goto L17;
        return false;
    L17:
        if (e(r8, r0 + 1) <= 0) goto L19;
        return false;
    L19:
        if (e(r8, r0 + 2) <= 0) goto L21;
        return false;
    L21:
        if (this.av == false) goto L23;
        int r1 = (r6 * 20) + 20;
    L24:
        this.ak = r1;
        this.al = (r0 * 20) - 1;
        i(60);
        return true;
    L23:
        r1 = r6 * 20;
        goto L24
    L5:
        int r03 = (this.W[2] + 5) / 20;
        r6 = r03;
        r8 = r03 - 1;
        goto L7
    }

    private boolean al() {
        int r0 = (this.W[1] + 10) / 20;
        if (this.av == false) goto L5;
        int r02 = (this.W[0] - 20) / 20;
        int r6 = r02;
        int r8 = r02 + 1;
    L6:
        int r03 = e(r6, r0);
        if (r03 >= 19) goto L9;
        return false;
    L9:
        if (e(r6, r0 - 1) <= 0) goto L11;
        return false;
    L11:
        if (e(r8, r0) <= 0) goto L13;
        return false;
    L13:
        if (e(r8, r0 - 1) <= 0) goto L15;
        return false;
    L15:
        if (e(r8, r0 + 1) <= 0) goto L17;
        return false;
    L17:
        if (e(r8, r0 + 2) <= 0) goto L19;
        return false;
    L19:
        if (this.av == false) goto L21;
        int r1 = (r6 * 20) + 20;
    L22:
        this.ak = r1;
        this.al = (r0 * 20) - 1;
        if (r03 == 21) goto L29;
        k.v();
        i(61);
        this.aC = 40;
        return true;
    L29:
        return true;
    L21:
        r1 = r6 * 20;
        goto L22
    L5:
        int r04 = ((this.W[2] + 20) / 20) + 1;
        r6 = r04;
        r8 = r04 - 1;
        goto L6
    }

    private boolean am() {
        if (this.ag <= 0) goto L10;
        if (this.aV != 19) goto L10;
        if (this.aR != 0) goto L10;
        this.av = true;
        this.ak = (this.ak / 20) * 20;
    L35:
        this.aj = 0;
        this.ah = 0;
        this.ag = 0;
        a(63, 16385);
        return true;
    L10:
        if (this.ag >= 0) goto L17;
        if (this.aW != 19) goto L17;
        if (this.aR != 0) goto L17;
        this.av = false;
        this.ak = ((this.ak / 20) * 20) + 20;
    L17:
        if (this.ag == 0) goto L19;
        return false;
    L19:
        if (this.aR == 19) goto L21;
        return false;
    L21:
        if (k.u(33024) == true) goto L23;
        return false;
    L23:
        if (this.aV != 0) goto L26;
        this.av = false;
    L26:
        if (this.aW != 0) goto L28;
        this.av = true;
    L28:
        int r1 = (this.ak / 20) * 20;
        if (this.av == false) goto L31;
        int r2 = 20;
    L32:
        this.ak = r1 + r2;
        goto L35
    L31:
        r2 = 0;
        goto L32
    }

    public static boolean b() {
        if (I == 1) goto L9;
        if (I == 2) goto L9;
        return false;
    L9:
        switch(k.aS.S) {
            case 67: goto L10;
            case 68: goto L10;
            case 69: goto L10;
            case 81: goto L10;
            case 112: goto L10;
            case 113: goto L10;
            case 114: goto L10;
            case 115: goto L10;
            case 183: goto L10;
            case 184: goto L10;
            case 216: goto L10;
            case 217: goto L10;
            case 286: goto L10;
            case 287: goto L10;
            default: goto L12;
        };
    L10:
        return true;
    L12:
        return false;
    }

    public static boolean b(int r2) {
        switch(r2) {
            case 18: goto L4;
            case 19: goto L4;
            case 20: goto L4;
            case 22: goto L4;
            case 23: goto L4;
            case 24: goto L4;
            case 25: goto L4;
            case 35: goto L4;
            case 36: goto L4;
            case 43: goto L4;
            case 150: goto L4;
            case 157: goto L4;
            case 165: goto L4;
            case 233: goto L4;
            case 242: goto L4;
            case 243: goto L4;
            case 263: goto L4;
            case 264: goto L4;
            case 265: goto L4;
            case 266: goto L4;
            default: goto L6;
        };
    L4:
        return true;
    L6:
        return false;
    }

    public static boolean c(int r2) {
        switch(r2) {
            case 0: goto L4;
            case 1: goto L4;
            case 7: goto L4;
            case 11: goto L4;
            case 12: goto L4;
            case 26: goto L4;
            case 79: goto L4;
            default: goto L6;
        };
    L4:
        return true;
    L6:
        return false;
    }

    public final boolean c() {
        if (this.S != 112) goto L5;
        return true;
    L5:
        if (this.S != 113) goto L7;
        return true;
    L7:
        if (this.S != 114) goto L9;
        return true;
    L9:
        if (this.S == 115) goto L16;
        return false;
    L16:
        return true;
    }

    private boolean an() {
        if (this.S > 43) goto L5;
        return true;
    L5:
        if (this.S != 150) goto L7;
        return true;
    L7:
        if (this.S < 67) goto L11;
        if (this.S > 69) goto L11;
        return true;
    L11:
        if (this.S != 199) goto L13;
        return true;
    L13:
        if (this.S != 216) goto L15;
        return true;
    L15:
        if (this.S != 217) goto L17;
        return true;
    L17:
        if (this.S != 298) goto L19;
        return true;
    L19:
        if (this.S < 259) goto L23;
        if (this.S > 266) goto L23;
        return true;
    L23:
        if (this.S != 20) goto L25;
        return true;
    L25:
        if (this.S != 49) goto L27;
        return true;
    L27:
        if (this.S == 243) goto L41;
        return false;
    L41:
        return true;
    }

    public final void d() {
        b(a);
    }

    public final void b(i r6) {
        if (r6 != null) goto L5;
        return;
    L5:
        if (r6.ax == 43) goto L7;
        return;
    L7:
        if (r6.S != 1) goto L9;
    L10:
        r6.t();
        this.ak = (r6.X[0] + r6.X[2]) >> 1;
        this.al = (r6.X[1] + r6.X[3]) >> 1;
        return;
    L9:
        if (r6.S == 4) goto L10;
    }

    public final void e() {
        if (k.C == null) goto L8;
        if ((k.aS.P & 512) != 0) goto L8;
        return;
    L8:
        if (v == false) goto L33;
        this.ah = 0;
        this.ag = 0;
        if (k.v(4112) == false) goto L12;
    L13:
        this.ak -= 20;
        return;
    L12:
        if (k.u(4112) == true) goto L13;
        if (k.v(8256) == false) goto L18;
    L19:
        this.ak += 20;
        return;
    L18:
        if (k.u(8256) == true) goto L19;
        if (k.v(16388) == false) goto L24;
    L25:
        this.al -= 20;
        return;
    L24:
        if (k.u(16388) == true) goto L25;
        if (k.v(33024) == false) goto L30;
    L31:
        this.al += 20;
        return;
    L30:
        if (k.u(33024) == true) goto L31;
        return;
    L33:
        k.l();
        if (r == false) goto L37;
        return;
    L37:
        if (k.E == null) goto L40;
        k.E.J();
    L40:
        if ((this.aA & 256) == 0) goto L56;
        if (this.Z[1] < 120) goto L48;
        if (i.bn == false) goto L46;
        b(k.aY[0].Z[4], 0, 0, -1, -1);
    L46:
        this.aA &= -257;
        this.aA |= 16;
    L48:
        if (i.bn == true) goto L50;
    L51:
        int[] r0 = this.Z;
        r0[1] = r0[1] - 1;
    L53:
        if (this.Z[1] > 0) goto L56;
        this.Z[1] = 0;
        goto L56
    L50:
        if ((k.aS.aA & 8) == 0) goto L53;
    L56:
        if ((this.aA & 16) == 0) goto L64;
        if (this.Z[0] > 0) goto L60;
        this.Z[0] = 3000;
    L60:
        int[] r02 = this.Z;
        r02[0] = r02[0] - j.f;
        if (this.Z[0] > 0) goto L64;
        this.aA &= -17;
        this.aA |= 256;
    L64:
        if (i.bq == 0) goto L68;
        if (this.al <= i.bq) goto L68;
    L69:
        i.bq = 0;
    L71:
        if (i.bh <= 0) goto L74;
        i.bh--;
    L74:
        if (g() == false) goto L77;
        i(50);
    L77:
        if (this.ah <= 5120) goto L79;
        this.ah = 5120;
        cn++;
    L81:
        if (k.aA <= 0) goto L86;
        if (this.aA <= 1) goto L88;
        this.aA |= 1;
    L88:
        a(an());
        cp = false;
        cq = false;
        cr = false;
        cs = false;
        z = false;
        ct = false;
        cu = false;
        cv = false;
        cw = false;
        if (k.am == false) goto L100;
        if (this.S == 183) goto L100;
        if (this.S == 184) goto L100;
        if (this.S == 311) goto L100;
        k.p();
        i.O();
        if (i.aN == null) goto L100;
        i.aN.aB = 0;
        i.d(i.aN);
        i.aN = null;
    L100:
        if (i.aN != null) goto L102;
    L104:
        az();
        if (this.S == 43) goto L108;
        i = true;
    L108:
        switch(this.S) {
            case 0: goto L680;
            case 1: goto L682;
            case 2: goto L1917;
            case 3: goto L1917;
            case 4: goto L1917;
            case 5: goto L464;
            case 6: goto L139;
            case 7: goto L682;
            case 8: goto L1301;
            case 9: goto L1315;
            case 10: goto L1315;
            case 11: goto L682;
            case 12: goto L639;
            case 13: goto L1917;
            case 14: goto L1917;
            case 15: goto L1917;
            case 16: goto L1045;
            case 17: goto L1418;
            case 18: goto L889;
            case 19: goto L889;
            case 20: goto L890;
            case 21: goto L859;
            case 22: goto L887;
            case 23: goto L889;
            case 24: goto L890;
            case 25: goto L890;
            case 26: goto L682;
            case 27: goto L1339;
            case 28: goto L1190;
            case 29: goto L1180;
            case 30: goto L1917;
            case 31: goto L1917;
            case 32: goto L746;
            case 33: goto L806;
            case 34: goto L776;
            case 35: goto L1045;
            case 36: goto L889;
            case 37: goto L1560;
            case 38: goto L1502;
            case 39: goto L1917;
            case 40: goto L1917;
            case 41: goto L1917;
            case 42: goto L1917;
            case 43: goto L1045;
            case 44: goto L1917;
            case 45: goto L1917;
            case 46: goto L1917;
            case 47: goto L1917;
            case 48: goto L1917;
            case 49: goto L1707;
            case 50: goto L447;
            case 51: goto L1917;
            case 52: goto L1917;
            case 53: goto L1917;
            case 54: goto L410;
            case 55: goto L1917;
            case 56: goto L1490;
            case 57: goto L1917;
            case 58: goto L1917;
            case 59: goto L1926;
            case 60: goto L1194;
            case 61: goto L1207;
            case 62: goto L1261;
            case 63: goto L1258;
            case 64: goto L1917;
            case 65: goto L1926;
            case 66: goto L1917;
            case 67: goto L1341;
            case 68: goto L1341;
            case 69: goto L1341;
            case 70: goto L1917;
            case 71: goto L1917;
            case 72: goto L1917;
            case 73: goto L1917;
            case 74: goto L632;
            case 75: goto L1917;
            case 76: goto L1917;
            case 77: goto L1917;
            case 78: goto L771;
            case 79: goto L682;
            case 80: goto L771;
            case 81: goto L1917;
            case 82: goto L1926;
            case 83: goto L1926;
            case 84: goto L1926;
            case 85: goto L1926;
            case 86: goto L1415;
            case 87: goto L1917;
            case 88: goto L1917;
            case 89: goto L1271;
            case 90: goto L1274;
            case 91: goto L1717;
            case 92: goto L788;
            case 93: goto L1917;
            case 94: goto L1917;
            case 95: goto L1917;
            case 96: goto L1917;
            case 97: goto L1917;
            case 98: goto L1917;
            case 99: goto L1917;
            case 100: goto L1917;
            case 101: goto L788;
            case 102: goto L1426;
            case 103: goto L1917;
            case 104: goto L1917;
            case 105: goto L1917;
            case 106: goto L1917;
            case 107: goto L488;
            case 108: goto L488;
            case 109: goto L488;
            case 110: goto L1599;
            case 111: goto L1917;
            case 112: goto L1341;
            case 113: goto L1341;
            case 114: goto L1341;
            case 115: goto L1341;
            case 116: goto L1917;
            case 117: goto L1917;
            case 118: goto L1917;
            case 119: goto L1917;
            case 120: goto L1917;
            case 121: goto L1917;
            case 122: goto L444;
            case 123: goto L1917;
            case 124: goto L1917;
            case 125: goto L1917;
            case 126: goto L1917;
            case 127: goto L1917;
            case 128: goto L1917;
            case 129: goto L1917;
            case 130: goto L1917;
            case 131: goto L1917;
            case 132: goto L1917;
            case 133: goto L1917;
            case 134: goto L1917;
            case 135: goto L1917;
            case 136: goto L1917;
            case 137: goto L1917;
            case 138: goto L1917;
            case 139: goto L1917;
            case 140: goto L1917;
            case 141: goto L1917;
            case 142: goto L1917;
            case 143: goto L1917;
            case 144: goto L1917;
            case 145: goto L1917;
            case 146: goto L1616;
            case 147: goto L1604;
            case 148: goto L1630;
            case 149: goto L1634;
            case 150: goto L1045;
            case 151: goto L1917;
            case 152: goto L1648;
            case 153: goto L1917;
            case 154: goto L1917;
            case 155: goto L1917;
            case 156: goto L1643;
            case 157: goto L890;
            case 158: goto L1917;
            case 159: goto L1917;
            case 160: goto L1917;
            case 161: goto L1917;
            case 162: goto L1917;
            case 163: goto L1917;
            case 164: goto L1926;
            case 165: goto L1601;
            case 166: goto L1917;
            case 167: goto L1917;
            case 168: goto L1917;
            case 169: goto L1917;
            case 170: goto L1917;
            case 171: goto L1917;
            case 172: goto L1917;
            case 173: goto L1917;
            case 174: goto L1917;
            case 175: goto L1917;
            case 176: goto L1917;
            case 177: goto L1917;
            case 178: goto L1917;
            case 179: goto L1917;
            case 180: goto L1917;
            case 181: goto L1917;
            case 182: goto L1917;
            case 183: goto L413;
            case 184: goto L424;
            case 185: goto L1917;
            case 186: goto L1917;
            case 187: goto L1917;
            case 188: goto L1917;
            case 189: goto L1917;
            case 190: goto L1917;
            case 191: goto L1917;
            case 192: goto L1917;
            case 193: goto L1917;
            case 194: goto L1917;
            case 195: goto L1917;
            case 196: goto L1917;
            case 197: goto L1917;
            case 198: goto L1917;
            case 199: goto L720;
            case 200: goto L1917;
            case 201: goto L1917;
            case 202: goto L1917;
            case 203: goto L1207;
            case 204: goto L1254;
            case 205: goto L424;
            case 206: goto L1917;
            case 207: goto L1917;
            case 208: goto L1917;
            case 209: goto L495;
            case 210: goto L1917;
            case 211: goto L1926;
            case 212: goto L1917;
            case 213: goto L1917;
            case 214: goto L1702;
            case 215: goto L890;
            case 216: goto L1658;
            case 217: goto L1681;
            case 218: goto L1917;
            case 219: goto L1917;
            case 220: goto L1917;
            case 221: goto L1917;
            case 222: goto L1917;
            case 223: goto L1917;
            case 224: goto L1917;
            case 225: goto L1656;
            case 226: goto L1917;
            case 227: goto L1917;
            case 228: goto L1737;
            case 229: goto L1917;
            case 230: goto L1917;
            case 231: goto L1917;
            case 232: goto L1917;
            case 233: goto L859;
            case 234: goto L1917;
            case 235: goto L536;
            case 236: goto L620;
            case 237: goto L589;
            case 238: goto L536;
            case 239: goto L620;
            case 240: goto L589;
            case 241: goto L447;
            case 242: goto L1721;
            case 243: goto L1721;
            case 244: goto L1736;
            case 245: goto L1917;
            case 246: goto L1917;
            case 247: goto L1917;
            case 248: goto L1917;
            case 249: goto L1917;
            case 250: goto L1735;
            case 251: goto L1917;
            case 252: goto L1045;
            case 253: goto L1917;
            case 254: goto L1917;
            case 255: goto L1917;
            case 256: goto L1917;
            case 257: goto L1770;
            case 258: goto L1804;
            case 259: goto L1832;
            case 260: goto L1778;
            case 261: goto L1832;
            case 262: goto L1778;
            case 263: goto L1845;
            case 264: goto L1845;
            case 265: goto L1857;
            case 266: goto L1857;
            case 267: goto L276;
            case 268: goto L304;
            case 269: goto L367;
            case 270: goto L374;
            case 271: goto L384;
            case 272: goto L1863;
            case 273: goto L1863;
            case 274: goto L1863;
            case 275: goto L1863;
            case 276: goto L1917;
            case 277: goto L1872;
            case 278: goto L1917;
            case 279: goto L1917;
            case 280: goto L273;
            case 281: goto L1917;
            case 282: goto L1499;
            case 283: goto L1714;
            case 284: goto L135;
            case 285: goto L1917;
            case 286: goto L182;
            case 287: goto L182;
            case 288: goto L1917;
            case 289: goto L1917;
            case 290: goto L1917;
            case 291: goto L221;
            case 292: goto L1863;
            case 293: goto L1872;
            case 294: goto L1874;
            case 295: goto L204;
            case 296: goto L1917;
            case 297: goto L1926;
            case 298: goto L1293;
            case 299: goto L1885;
            case 300: goto L1885;
            case 301: goto L1885;
            case 302: goto L1885;
            case 303: goto L1876;
            case 304: goto L218;
            case 305: goto L218;
            case 306: goto L218;
            case 307: goto L1917;
            case 308: goto L1917;
            case 309: goto L1917;
            case 310: goto L1888;
            case 311: goto L1889;
            case 312: goto L1889;
            case 313: goto L137;
            case 314: goto L1917;
            case 315: goto L1180;
            case 316: goto L1917;
            case 317: goto L145;
            case 318: goto L1190;
            case 319: goto L1917;
            case 320: goto L1917;
            case 321: goto L1917;
            case 322: goto L1917;
            case 323: goto L1917;
            case 324: goto L1917;
            case 325: goto L1917;
            case 326: goto L1926;
            case 327: goto L1917;
            case 328: goto L1917;
            case 329: goto L1917;
            case 330: goto L1917;
            case 331: goto L1917;
            case 332: goto L1451;
            case 333: goto L1917;
            case 334: goto L1917;
            case 335: goto L1917;
            case 336: goto L1917;
            case 337: goto L1917;
            case 338: goto L1917;
            case 339: goto L1917;
            case 340: goto L1917;
            case 341: goto L1917;
            case 342: goto L1917;
            case 343: goto L1917;
            case 344: goto L1917;
            case 345: goto L1917;
            case 346: goto L1917;
            case 347: goto L1917;
            case 348: goto L1917;
            case 349: goto L1917;
            case 350: goto L1917;
            case 351: goto L1917;
            case 352: goto L1917;
            case 353: goto L1917;
            case 354: goto L1917;
            case 355: goto L1917;
            case 356: goto L1917;
            case 357: goto L1909;
            case 358: goto L1737;
            case 359: goto L1917;
            case 360: goto L1893;
            case 361: goto L1917;
            case 362: goto L1917;
            case 363: goto L1917;
            case 364: goto L1917;
            case 365: goto L1917;
            case 366: goto L1917;
            case 367: goto L1917;
            case 368: goto L1917;
            case 369: goto L1917;
            case 370: goto L117;
            case 371: goto L1926;
            case 372: goto L1917;
            case 373: goto L1917;
            case 374: goto L128;
            case 375: goto L109;
            case 376: goto L120;
            case 377: goto L125;
            default: goto L1917;
        };
    L109:
        this.ag = 1280;
        if (this.av == true) goto L113;
        this.ag = -1280;
    L113:
        if (r() == false) goto L115;
        i(376);
    L115:
        i.f(this);
        goto L1926
    L120:
        this.ah = 0;
        this.ag = 0;
        if (r() == false) goto L123;
        i(377);
    L123:
        i.f(this);
        goto L1926
    L128:
        this.ah = 0;
        this.ag = 0;
        if (r() == false) goto L1926;
        if (x[1] <= 0) goto L133;
        this.ah = 0;
        this.ag = 0;
        i(376);
        goto L1926
    L133:
        k.l(12);
        goto L1926
    L137:
        return;
    L145:
        this.aj = 128;
        if (this.ah < 512) goto L149;
        this.ah = 512;
    L149:
        if (this.av == false) goto L151;
        this.ag = -2560;
    L153:
        if (k.v(16388) == false) goto L156;
        this.ah = -2048;
    L156:
        if (y() == false) goto L158;
    L161:
        this.ag = 0;
        if (k.z[30] == null) goto L171;
        if (f != null) goto L171;
        boolean r3 = this.av;
        if (this.av == false) goto L168;
        int r4 = this.W[0];
    L169:
        f = i.a(8, 30, 4, r3, r4, this.al, 300);
        goto L171
    L168:
        r4 = this.W[2];
    L171:
        if (f != null) goto L173;
    L176:
        if (q == false) goto L179;
        a(1, this.ak, this.al - 60);
        return;
    L179:
        G();
        return;
    L173:
        if (f.r() == false) goto L176;
        k.c(f);
        f = null;
        this.aj = 0;
        this.ah = 0;
        i(50);
        goto L176
    L158:
        if (this.aR >= 12) goto L161;
        if (this.aR != 5) goto L176;
    L151:
        this.ag = 2560;
        goto L153
    L221:
        this.ag = 0;
        this.ah = 0;
        if (r() == true) goto L226;
        if (this.T < (this.aa.b(this.S) - 2)) goto L1926;
    L226:
        if (this.af == null) goto L1926;
        if (this.af.ax != 10) goto L1926;
        this.T = this.aa.b(this.S) - 1;
        this.U = 0;
        if (this.ae != null) goto L232;
    L233:
        G();
        a(102, this.ak, this.al - 85);
    L234:
        this.ae.ak = this.ak;
        this.ae.al = this.al - 85;
        boolean r9 = false;
        if (g == null) goto L253;
        if (Math.abs(g.ak - this.ak) >= 60) goto L239;
        boolean r03 = true;
    L240:
        r9 = r03;
        if (r03 == false) goto L248;
        if (this.ae != null) goto L245;
    L246:
        G();
        a(8, this.ak, this.al - 85);
    L258:
        if (k.u() == false) goto L265;
        if (r9 == true) goto L262;
    L263:
        i(285);
        this.af = null;
        k.v();
        G();
        goto L1926
    L262:
        if (g == null) goto L263;
    L265:
        if (r9 == false) goto L1926;
        if (k.v(65568) == false) goto L1926;
        if (g == null) goto L1926;
        G();
        this.af = null;
        i(270);
        k.A(13);
        goto L1926
    L245:
        if (k.aS.ae.S == 8) goto L258;
    L248:
        if (this.ae == null) goto L258;
        if (k.aS.ae.S != 8) goto L258;
        G();
        goto L258
    L239:
        r03 = false;
        goto L240
    L253:
        if (this.ae == null) goto L258;
        if (k.aS.ae.S != 8) goto L258;
        G();
        goto L258
    L232:
        if (k.aS.ae.S == 102) goto L234;
    L276:
        t = 100;
        if (this.af == null) goto L283;
        if (this.af.ax == 27) goto L285;
        if (this.af.ax != 10) goto L283;
    L285:
        if (k.aS.aZ == true) goto L288;
        k.aS.E();
    L288:
        if (this.af.ax != 10) goto L290;
        int r92 = (this.af.W[0] + this.af.W[2]) >> 1;
    L291:
        int r04 = r92 - this.ak;
        if (Math.abs(r04) > 4) goto L298;
        this.ak = r92;
        this.av = false;
        if (this.af.ax != 10) goto L296;
        i(291);
        return;
    L296:
        i(268);
        this.af.i(1);
        return;
    L298:
        if (r04 >= 0) goto L300;
        this.av = true;
        this.ag = -1024;
        return;
    L300:
        this.av = false;
        this.ag = 1024;
        return;
    L290:
        r92 = (this.af.X[0] + this.af.X[2]) >> 1;
    L283:
        if (i.ae() == true) goto L285;
        t = 0;
        i(0);
        return;
    L304:
        this.ag = 0;
        this.ah = 0;
        if (r() == true) goto L309;
        if (this.T >= (this.aa.b(this.S) - 2)) goto L309;
        return;
    L309:
        if (this.af != null) goto L311;
        return;
    L311:
        if (this.af.ax == 27) goto L313;
        return;
    L313:
        if (this.ae == null) goto L317;
        if (k.aS.ae.S != 102) goto L317;
    L320:
        if (this.ae == null) goto L323;
        this.ae.ak = this.ak;
        this.ae.al = this.al - 85;
    L323:
        if (this.af.S != 2) goto L325;
    L328:
        this.T = this.aa.b(this.S) - 1;
        this.U = 0;
        boolean r93 = false;
        if (g == null) goto L347;
        if (Math.abs(g.ak - this.ak) >= 60) goto L333;
        boolean r05 = true;
    L334:
        r93 = r05;
        if (r05 == false) goto L342;
        if (this.ae != null) goto L339;
    L340:
        G();
        a(8, this.ak, this.al - 85);
    L352:
        if (k.u() == false) goto L360;
        if (r93 == true) goto L356;
    L357:
        this.af.i(1);
        k.v();
        G();
        return;
    L356:
        if (g == null) goto L357;
    L360:
        if (r93 == true) goto L362;
        return;
    L362:
        if (k.v(65568) == true) goto L364;
        return;
    L364:
        if (g == null) goto L2217;
        G();
        this.af.ak = this.ak;
        this.af.al = this.al;
        this.af.i(3);
        i(270);
        k.A(13);
        return;
    L2217:
        return;
    L339:
        if (k.aS.ae.S == 8) goto L352;
    L342:
        if (this.ae == null) goto L352;
        if (k.aS.ae.S != 8) goto L352;
        G();
        goto L352
    L333:
        r05 = false;
        goto L334
    L347:
        if (this.ae == null) goto L352;
        if (k.aS.ae.S != 8) goto L352;
        G();
        goto L352
    L325:
        if (this.az == (-2)) goto L328;
        this.af.i(2);
        this.az = -2;
        return;
    L317:
        if (this.af.S == 1) goto L320;
        G();
        a(102, this.ak, this.al - 85);
        goto L320
    L367:
        t = 0;
        if (r() == false) goto L1926;
        if (this.af == null) goto L1926;
        if (this.af.ax != 27) goto L1926;
        i(0);
        E();
        this.af.i(2);
        this.af = null;
        goto L1926
    L374:
        t = 0;
        if (g != null) goto L378;
        i(0);
        goto L1926
    L378:
        if (g.S == 133) goto L381;
        g.i(133);
    L381:
        if (r() == false) goto L2218;
        i(271);
        g.i(145);
        this.bl = 5;
        return;
    L2218:
        return;
    L384:
        t = 0;
        if (g != null) goto L388;
        i(0);
        goto L1926
    L388:
        if (g.S == 134) goto L391;
        g.i(134);
    L391:
        if (this.ae != null) goto L393;
    L394:
        G();
        a(8, this.ak, this.al - 85);
    L395:
        this.ae.ak = this.ak;
        this.ae.al = this.al - 85;
        if (k.v(65568) == false) goto L399;
        this.bl += 2;
    L402:
        if (this.bl >= 0) goto L406;
        this.bl = 0;
        G();
        i(0);
        g.i(5);
        g.aA = 1;
        g.Q();
        return;
    L406:
        if (this.bl < 10) goto L2219;
        this.bl = 0;
        G();
        this.P &= -65;
        i(0);
        g.aB = 0;
        g.i(135);
        k.e(0, this.aw);
        k.o(3);
        return;
    L2219:
        return;
    L399:
        if ((j.g % 2) != 0) goto L402;
        this.bl--;
        goto L402
    L393:
        if (k.aS.ae.S == 8) goto L395;
    L464:
        i.O();
        this.bM = null;
        this.ag = 0;
        this.ah = 0;
        this.aj = 0;
        if (k.v(16388) == true) goto L471;
        if (k.v(2) == true) goto L471;
        if (k.v(8) == true) goto L471;
    L478:
        if (k.u(94324) == false) goto L481;
        this.ab = null;
        l();
        goto L1926
    L481:
        if (r() == false) goto L1926;
        this.ab = null;
        if (this.aO <= 12) goto L485;
        int r1 = 79;
    L486:
        i(r1);
        goto L1926
    L485:
        r1 = 0;
    L471:
        if (k.v(2) == false) goto L474;
        this.av = true;
    L476:
        i(21);
        this.ab = null;
        goto L478
    L474:
        if (k.v(8) == false) goto L476;
        this.av = false;
        goto L476
    L639:
        D = false;
        co++;
        if (this.ag == 0) goto L680;
        if (this.aO != 0) goto L680;
        if (this.av == false) goto L646;
        int r06 = this.aX;
    L647:
        int r94 = r06;
        if (this.av == false) goto L650;
        int r07 = this.aT;
    L651:
        int r10 = r07;
        if (A() == false) goto L660;
        this.ai = 0;
        this.ag = 0;
        this.al = this.W[1];
        int r12 = this.ak;
        if (this.av == false) goto L656;
        int r2 = -20;
    L657:
        this.ak = r12 + r2;
        i(74);
        return;
    L656:
        r2 = 20;
        goto L657
    L660:
        if (r10 < 19) goto L680;
        if (r10 >= 24) goto L680;
        if (r94 != 1) goto L667;
        this.ag = 0;
        this.ah = 0;
        i(107);
        goto L1926
    L667:
        if (r94 != 2) goto L670;
        this.ag = 0;
        this.ah = 0;
        i(108);
        goto L1926
    L670:
        if (r94 != 3) goto L673;
        this.ag = 0;
        this.ah = 0;
        i(109);
        goto L1926
    L673:
        if (co <= 2) goto L680;
        if (z() == false) goto L680;
        if (ak() == false) goto L679;
        this.aj = 0;
        this.ah = 0;
        this.ag = 0;
        goto L1926
    L679:
        i(33);
        this.ag = 0;
        this.ah = -4096;
        goto L1926
    L650:
        r07 = this.aU;
        goto L651
    L646:
        r06 = this.aY;
    L680:
        i.O();
        goto L682
    L746:
        i.f(this);
        if (this.aZ == true) goto L752;
        if (a != null) goto L752;
        cq = false;
        a(0);
    L752:
        if (this.ag == 0) goto L755;
        a(true);
    L755:
        if (y() == false) goto L760;
        if (this.ag == 0) goto L760;
        this.ag = 0;
    L760:
        if (r() == false) goto L1926;
        this.ag = 0;
        if (this.Q != 79) goto L764;
        int r13 = 79;
    L765:
        i(r13);
        if (this.S != 0) goto L1926;
        x();
        if (this.aO <= 12) goto L1926;
        i(79);
        goto L1926
    L764:
        r13 = 0;
        goto L765
    L776:
        aA();
        if (this.av == false) goto L779;
        int r08 = this.aT;
    L781:
        if (r08 == 20) goto L784;
        a(0);
    L784:
        if (this.aR < 19) goto L786;
    L787:
        l();
        k.v();
        goto L1926
    L786:
        if (this.aR != 5) goto L1926;
    L779:
        r08 = this.aU;
        goto L781
    L788:
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        if (r() == false) goto L1926;
        if (this.av == true) goto L793;
        boolean r14 = true;
    L794:
        this.av = r14;
        if (this.av == false) goto L797;
        int r15 = -2048;
    L798:
        this.ag = r15;
        if (this.aO != 20) goto L801;
        int r16 = 0;
    L802:
        this.ah = r16;
        if (this.ah != 0) goto L805;
        a(0);
        goto L1926
    L805:
        a(36, 36);
        goto L1926
    L801:
        r16 = -5120;
        goto L802
    L797:
        r15 = 2048;
        goto L798
    L793:
        r14 = false;
        goto L794
    L806:
        cp = true;
        this.aj = 512;
        if (A() == false) goto L815;
        this.aj = 0;
        this.ah = 0;
        this.ai = 0;
        this.ag = 0;
        this.al = this.W[1];
        if (this.av == false) goto L811;
        int r17 = this.W[0] - 10;
    L812:
        this.ak = r17;
        i(74);
        return;
    L811:
        r17 = this.W[2] + 10;
        goto L812
    L815:
        if (this.av == false) goto L820;
        if (k.u(4112) == true) goto L822;
    L824:
        x();
        if (this.aR != 5) goto L827;
    L832:
        this.ah = 0;
        this.ag = 0;
        a(43, 32);
        goto L1926
    L827:
        if (this.aR == 20) goto L832;
        if (this.aS == 5) goto L832;
        if (this.aS == 20) goto L832;
        ct = false;
        this.ag = 0;
        this.ah = 1536;
        a(34, 36);
        aA();
    L822:
        if (this.ah >= 0) goto L824;
        ct = true;
        if (r() == false) goto L1926;
        boolean r95 = false;
        int r11 = (this.W[1] + 10) / 20;
        if (this.av == false) goto L839;
        int r09 = (this.W[0] - 5) / 20;
        int r102 = r09;
        int r122 = r09 + 1;
    L840:
        int r132 = 0;
    L842:
        if (r132 >= 2) goto L854;
        r11 = r11 - r132;
        if (e(r102, r11) < 19) goto L852;
        if (e(r102, r11 - 1) > 0) goto L852;
        if (e(r122, r11) > 0) goto L852;
        if (e(r122, r11 - 1) > 0) goto L852;
        r95 = true;
    L852:
        r132 = r132 + 1;
    L854:
        if (r95 == false) goto L1926;
        if (ak() == false) goto L1926;
        this.aj = 0;
        this.ah = 0;
        this.ag = 0;
        goto L1926
    L839:
        int r010 = (this.W[2] + 5) / 20;
        r102 = r010;
        r122 = r010 - 1;
        goto L840
    L820:
        if (k.u(8256) == false) goto L824;
    L859:
        this.ah = 0;
        this.ag = 0;
        if (r() == false) goto L862;
    L863:
        i(22);
        if (a != null) goto L866;
    L868:
        this.ah = -5120;
    L870:
        if (a == null) goto L881;
        if (a.ax != 43) goto L881;
        if (a.ab() == false) goto L881;
        if (this.av == false) goto L878;
        int r18 = -1024;
    L879:
        this.ag = r18;
    L885:
        i.f(this);
        goto L1926
    L878:
        r18 = 1024;
    L881:
        if (this.av == false) goto L883;
        int r19 = -2048;
    L884:
        this.ag = r19;
        goto L885
    L883:
        r19 = 2048;
        goto L884
    L866:
        if (a.ax != 51) goto L868;
        this.ah = -2560;
        goto L870
    L862:
        if (this.S != 233) goto L885;
    L889:
        cv = true;
    L890:
        cp = true;
        ct = true;
        cw = true;
        if (I != 4) goto L893;
        z = true;
    L893:
        this.aj = 1536;
        if (this.S != 22) goto L899;
        if (r() == false) goto L899;
        this.P |= 64;
    L899:
        if (this.T != 1) goto L907;
        if (this.U != 0) goto L907;
        if (this.S == 19) goto L907;
        if (this.S == 23) goto L907;
    L911:
        if (this.S != 20) goto L918;
        if (this.av == false) goto L915;
        int r110 = 2048;
    L916:
        this.ag = r110;
    L947:
        if (this.S != 20) goto L949;
    L958:
        this.ah = -5120;
    L960:
        if (y() == true) goto L962;
    L1006:
        av();
        if (this.ah <= 0) goto L1031;
        if (this.aR < 12) goto L1015;
        if (this.aQ < 12) goto L1015;
        if (this.aQ == 23) goto L1015;
    L1022:
        d(false);
    L1015:
        if (this.aR >= 12) goto L1022;
        if (this.aS >= 12) goto L1022;
        if (this.aR == 5) goto L1022;
        if (this.aS == 5) goto L1022;
        if (this.S != 215) goto L1031;
        a(0);
        if (this.av == true) goto L1028;
        boolean r111 = true;
    L1029:
        this.av = r111;
        goto L1031
    L1028:
        r111 = false;
    L1031:
        if (r() == false) goto L1038;
        if (this.S == 215) goto L1038;
        if (this.S == 22) goto L1038;
        a(0);
        a(true);
    L1038:
        if (this.S == 22) goto L1040;
    L1044:
        i.f(this);
        goto L1926
    L1040:
        if (this.ah >= 0) goto L1044;
        if ((this.ah + this.aj) < 0) goto L1044;
        i(23);
        goto L1044
    L962:
        if (cv == false) goto L973;
        if (this.aF == 0) goto L973;
        if (this.av == true) goto L971;
        if (this.aU != 20) goto L973;
    L982:
        if (this.ba == true) goto L996;
        this.aF = 0;
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        if (this.S != 215) goto L991;
        if (this.av == true) goto L988;
        boolean r112 = true;
    L989:
        this.av = r112;
        goto L991
    L988:
        r112 = false;
    L991:
        if (this.av == false) goto L993;
        this.ak = (((this.W[0] + 20) / 20) * 20) + 1;
    L994:
        a(8, 5, 17, 201);
        i.aK.av = this.av;
        i.aK.N = this.ak << 8;
        i.aK.O = this.al << 8;
        i.aK.ak = this.ak;
        i.aK.al = this.al;
        i r011 = i.aK;
        i r113 = i.aK;
        i r22 = i.aK;
        i.aK.aj = 0;
        r22.ai = 0;
        r113.ah = 0;
        r011.ag = 0;
        i.aK.t();
        i.aK.P = 512;
        k.b(i.aK);
        i(101);
        goto L1926
    L993:
        this.ak = ((((this.W[2] - 20) / 20) * 20) + 20) - 1;
    L996:
        if (this.S == 25) goto L1002;
        if (this.S == 15) goto L1002;
        if (this.S != 19) goto L1006;
    L1002:
        if (this.ag <= 0) goto L1004;
        int r114 = 512;
    L1005:
        this.ag = r114;
        goto L1006
    L1004:
        r114 = -512;
        goto L1005
    L971:
        if (this.aT == 20) goto L982;
    L973:
        if (this.S != 215) goto L996;
        if (this.av == false) goto L980;
        if (this.aU != 20) goto L996;
    L980:
        if (this.aT != 20) goto L996;
    L949:
        if (this.S == 36) goto L958;
        if (this.S != 215) goto L954;
        this.ah = -6656;
        goto L960
    L954:
        if (a == null) goto L958;
        if (a.ax != 51) goto L958;
        this.ah = -2560;
        goto L960
    L915:
        r110 = -2048;
        goto L916
    L918:
        if (this.S != 25) goto L925;
        if (this.av == false) goto L922;
        int r115 = 1024;
    L923:
        this.ag = r115;
        goto L947
    L922:
        r115 = -1024;
        goto L923
    L925:
        if (this.S != 36) goto L932;
        if (this.av == false) goto L929;
        int r116 = -2048;
    L930:
        this.ag = r116;
        goto L947
    L929:
        r116 = 2048;
        goto L930
    L932:
        if (this.S != 24) goto L935;
        g r012 = this;
        int r117 = 0;
    L945:
        r012.ag = r117;
        goto L947
    L935:
        if (this.S == 215) goto L937;
        r012 = this;
        if (this.av == false) goto L944;
        r117 = -2048;
        goto L945
    L944:
        r117 = 2048;
        goto L945
    L937:
        if (this.av == false) goto L939;
        int r118 = 2048;
    L940:
        this.ag = r118;
        goto L947
    L939:
        r118 = -2048;
    L907:
        if (this.S != 215) goto L960;
        if (this.ah != 0) goto L960;
    L1045:
        i.f(this);
        cv = true;
        cp = true;
        ct = true;
        cw = true;
        if (this.S != 43) goto L1051;
        if (y() == false) goto L1051;
        this.ai = 0;
        this.ag = 0;
    L1051:
        if (I != 4) goto L1054;
        z = true;
    L1054:
        if (this.S != 43) goto L1059;
        if (r() == false) goto L1059;
        this.P |= 64;
    L1059:
        if (this.aQ != 3) goto L1078;
        if (c == null) goto L1067;
        if (c.ax != 51) goto L1067;
        if (this.al <= c.W[3]) goto L1067;
    L1076:
        i(147);
        i.bq = 0;
    L1067:
        if (i.bq <= 0) goto L1073;
        if (this.al <= i.bq) goto L1073;
        if (c == null) goto L1076;
    L1073:
        if (c != null) goto L1926;
        if (i.bq != 0) goto L1926;
    L1078:
        if (this.aR < 12) goto L1084;
        if (this.aQ < 12) goto L1084;
        if (this.aQ == 23) goto L1084;
    L1096:
        if (this.aR != 4) goto L1098;
    L1099:
        boolean r119 = true;
    L1101:
        d(r119);
        goto L1926
    L1098:
        if (this.aS == 4) goto L1099;
        r119 = false;
    L1084:
        if (this.aR >= 12) goto L1096;
        if (this.aS >= 12) goto L1096;
        if (this.aR == 5) goto L1096;
        if (this.aS == 5) goto L1096;
        if (this.aR == 4) goto L1096;
        if (this.aS == 4) goto L1096;
        this.aj = 1536;
        if (k == (-1)) goto L1128;
        if (o == 0) goto L1109;
        if (this.al >= o) goto L1109;
    L1111:
        if (k != 0) goto L1114;
        a(29, 32);
    L1121:
        this.ag = 0;
        if (n != 0) goto L1124;
    L1126:
        this.ah = 1536;
        this.aj = 0;
    L1167:
        if (this.S == 43) goto L1926;
        if (this.S == 150) goto L1926;
        if (this.S == 35) goto L1926;
        if (this.S == 29) goto L1926;
        if (this.S == 252) goto L1926;
        if (r() == false) goto L1926;
        i(35);
        goto L1926
    L1124:
        if (k != 0) goto L1126;
        this.ak = n;
        goto L1126
    L1114:
        if (k != 1) goto L1121;
        this.ak = d.W[0];
        t();
        i(315);
        if (d == null) goto L1121;
        this.av = d.av;
        if (this.av == false) goto L1120;
        this.ak = d.W[2];
        goto L1121
    L1120:
        this.ak = d.W[0];
    L1109:
        if (o == 0) goto L1111;
    L1128:
        if (y() == false) goto L1167;
        if (k.u(2) == true) goto L1142;
        if (k.v(2) == true) goto L1142;
        if (k.u(8) == true) goto L1142;
        if (k.v(8) == true) goto L1142;
        if (k.u(16388) == true) goto L1142;
        if (k.v(16388) == false) goto L1167;
    L1142:
        if (cv == false) goto L1160;
        if (this.aF == 0) goto L1160;
        if (this.ba == true) goto L1160;
        if (this.av == true) goto L1153;
        if (this.aU != 20) goto L1160;
    L1154:
        this.aF = 0;
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        if (this.av == false) goto L1157;
        this.ak = ((this.W[0] / 20) * 20) + 1;
    L1158:
        a(8, 5, 17, 201);
        i.aK.av = this.av;
        i.aK.N = this.ak << 8;
        i.aK.O = this.al << 8;
        i.aK.ak = this.ak;
        i.aK.al = this.al;
        i r013 = i.aK;
        i r120 = i.aK;
        i r23 = i.aK;
        i.aK.aj = 0;
        r23.ai = 0;
        r120.ah = 0;
        r013.ag = 0;
        i.aK.t();
        i.aK.P = 512;
        k.b(i.aK);
        i(101);
        goto L1926
    L1157:
        this.ak = (((this.W[2] / 20) * 20) + 20) - 1;
        goto L1158
    L1153:
        if (this.aT == 20) goto L1154;
    L1160:
        if (this.ag == 0) goto L1167;
        if (this.ag <= 0) goto L1164;
        int r121 = 512;
    L1165:
        this.ag = r121;
        goto L1167
    L1164:
        r121 = -512;
        goto L1165
    L1190:
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        if (k.v(33024) == false) goto L1926;
        a(this.ah);
        goto L1926
    L1207:
        this.ag = 0;
        this.ah = 0;
        if (this.S == 203) goto L1220;
        int r123 = this.aC - 1;
        this.aC = r123;
        if (r123 == 0) goto L1222;
        int r124 = this.ak;
        if (this.av == false) goto L1214;
        int r24 = -10;
    L1216:
        if (e((r124 + r24) / 20, (this.al + 10) / 20) >= 12) goto L1220;
        if (a == null) goto L1222;
    L1214:
        r24 = 10;
    L1222:
        if (a != null) goto L1224;
    L1227:
        H();
        G();
        this.al += this.W[3] - this.W[1];
        a(0);
    L1229:
        if (this.S != 203) goto L1243;
        if (h != null) goto L1233;
    L1241:
        G();
        goto L1243
    L1233:
        if (h.P() == true) goto L1241;
        k.c(this.ak, this.al - 85, h.aw);
        if (k.v(65568) == false) goto L1243;
        G();
        i(204);
        i r014 = h;
        if (this.av == false) goto L1239;
        int r125 = this.ak + 10;
    L1240:
        r014.ak = r125;
        h = null;
        goto L1243
    L1239:
        r125 = this.ak - 10;
    L1243:
        if (k.v(16388) == false) goto L1245;
    L1252:
        H();
        G();
        i(62);
        goto L1926
    L1245:
        if (this.av == false) goto L1249;
        if (k.v(4114) == true) goto L1252;
    L1249:
        if (this.av == true) goto L1926;
        if (k.v(8264) == false) goto L1926;
    L1224:
        if (a == null) goto L1229;
        if (a.ax == 43) goto L1229;
    L1220:
        if (k.v(33024) == false) goto L1229;
    L1271:
        h(1);
        this.ah = 0;
        this.ag = 0;
        this.aj = 0;
        this.ai = 0;
        if (r() == false) goto L1926;
        this.P |= 64;
        goto L1926
    L1274:
        this.ae = null;
        this.ah = 0;
        G();
        k.v();
        D = true;
        if (p == 0) goto L1284;
        if (p != 1) goto L1280;
        this.ag = 4096;
        this.av = false;
    L1282:
        this.ah = -768;
        i(157);
        p = 0;
        goto L1926
    L1280:
        if (p != 2) goto L1282;
        this.ag = -4096;
        this.av = true;
        goto L1282
    L1284:
        if (r() == false) goto L1926;
        if (this.aR < 20) goto L1288;
    L1290:
        i(0);
        E();
        goto L1926
    L1288:
        if (this.aS >= 20) goto L1290;
        a(0);
        goto L1926
    L1315:
        this.ag /= 2;
        this.ah = 0;
        this.aj = 0;
        if (this.ab == null) goto L1319;
        this.ab.av = this.av;
        this.ab.ak = this.ak;
        this.ab.al = this.al;
    L1319:
        if (this.av == false) goto L1321;
        int r015 = this.aU;
    L1323:
        if (r015 < 19) goto L1326;
        this.ag = 0;
    L1326:
        if (r() == false) goto L1333;
        this.bl = 0;
        G();
        H();
        i(1);
        a(false);
        if (M() == true) goto L1333;
        if (a != null) goto L1333;
        a(0);
    L1333:
        if (this.Q != 0) goto L1335;
    L1336:
        this.ah = 1;
        a(true);
        this.ah = 0;
    L1337:
        i.f(this);
        goto L1926
    L1335:
        if (this.Q != 54) goto L1337;
    L1321:
        r015 = this.aT;
        goto L1323
    L1341:
        ay();
        if (this.ag == 0) goto L1347;
        if (y() == false) goto L1347;
        this.ag = 0;
    L1347:
        if (this.aZ == false) goto L1349;
    L1352:
        z = false;
        if (k.v(65568) == true) goto L1355;
    L1375:
        aj();
        if (k.E == null) goto L1379;
        k.E.K();
    L1379:
        if (this.cl == true) goto L1383;
        if (r() == true) goto L1383;
    L1412:
        if (this.T != 2) goto L1926;
        k.A(10);
    L1383:
        if (this.R == 112) goto L1385;
    L1391:
        this.cl = false;
        if (i.aN == null) goto L1398;
        if (i.aN.aB <= 0) goto L1398;
        if (this.R != 183) goto L1398;
    L1401:
        k.o();
        this.ag = 0;
        this.ah = 0;
    L1408:
        if (this.R == (-1)) goto L1410;
        i(this.R);
        this.R = -1;
        goto L1412
    L1410:
        l();
    L1398:
        if (this.R == 184) goto L1401;
        if (this.R == 205) goto L1401;
        if (i.aN == null) goto L1408;
        if (i.aN.aB > 0) goto L1408;
        this.R = -1;
        goto L1408
    L1385:
        if (a == null) goto L1391;
        if (a.ax != 51) goto L1391;
        if (r() == false) goto L1926;
        this.R = -1;
        goto L1391
    L1355:
        if (this.S == 67) goto L1359;
        if (this.S != 68) goto L1375;
    L1359:
        if (a != null) goto L1375;
        if (i.aN == null) goto L1375;
        if (i.aN.ax != 11) goto L1375;
        if (i.aN.Z[0] != 2) goto L1375;
        if (i.aN.aB > i.bw[k.au]) goto L1375;
        this.cl = false;
        if (this.R != (-1)) goto L1379;
        this.R = Math.abs(j.j.nextInt()) % 2;
        switch(this.R) {
            case 0: goto L1372;
            case 1: goto L1373;
            default: goto L1379;
        };
    L1372:
        this.R = 183;
        goto L1379
    L1373:
        this.R = 184;
        goto L1379
    L1349:
        if (a != null) goto L1352;
        a(0);
        return;
    L1418:
        this.ah = 0;
        this.ag = 0;
        if (r() == false) goto L1926;
        i(18);
        if (this.av == false) goto L1423;
        int r126 = -2048;
    L1424:
        this.ag = r126;
        this.ah = -5120;
        goto L1926
    L1423:
        r126 = 2048;
        goto L1424
    L1490:
        this.ah = 0;
        this.ag = 0;
        if (r() == false) goto L1926;
        if (k.u(127999) == false) goto L1495;
        i(65);
        goto L1926
    L1495:
        i(59);
        goto L1926
    L1502:
        this.ag = 0;
        if (this.ac == null) goto L1527;
        if (this.ac.ax != 10) goto L1527;
        if (this.ac.S != 32) goto L1527;
        if (k.u(16388) == false) goto L1511;
    L1517:
        G();
        this.al -= 20;
        k.aS.i(23);
        k.aS.ah = 2560;
        if (k.u(2) == true) goto L1522;
        if (k.u(8) == true) goto L1522;
    L1525:
        cq = true;
    L1535:
        if (k.u(33024) == false) goto L1540;
        this.al += 20;
        x();
        this.al -= 20;
        if (this.aO != 0) goto L1926;
        this.al = this.W[3] + 10;
        i(43);
        goto L1926
    L1540:
        if (this.av == false) goto L1545;
        if (k.u(4112) == false) goto L1548;
    L1546:
        i(37);
    L1548:
        if (this.av == false) goto L1553;
        if (k.v(8256) == false) goto L1926;
    L1555:
        if (this.av == true) goto L1557;
        boolean r127 = true;
    L1558:
        this.av = r127;
        goto L1926
    L1557:
        r127 = false;
        goto L1558
    L1553:
        if (k.u(4112) == false) goto L1926;
    L1545:
        if (k.u(8256) == false) goto L1548;
    L1522:
        if (this.av == false) goto L1524;
        k.aS.ag = -4096;
        goto L1525
    L1524:
        k.aS.ag = 4096;
        goto L1525
    L1511:
        if (this.av == false) goto L1516;
        if (k.u(2) == false) goto L1535;
    L1516:
        if (k.u(8) == false) goto L1535;
    L1527:
        if (this.aO == 5) goto L1529;
        this.al = this.W[3];
        i(43);
        goto L1926
    L1529:
        this.al = ((this.W[1] / 20) * 20) + 10;
        if (k.u(16388) == false) goto L1535;
        G();
        this.al -= 20;
        x();
        this.al += 20;
        if (this.aO != 0) goto L1535;
        a(54, 8);
        goto L1535
    L1601:
        this.aj = 1536;
        if (r() == false) goto L1926;
        a(0);
        goto L1926
    L1604:
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        if (r() == false) goto L1926;
        if (this.S != 145) goto L1609;
        this.al += 20;
    L1609:
        j = true;
        if (k.x() == false) goto L1612;
        k.bG = k.bH;
    L1613:
        k.A(18);
        k.a(true);
        k.az = k.a(k.bA, 32);
        return;
    L1612:
        k.bG = -1;
        goto L1613
    L1630:
        this.ah = -5120;
        if (r() == false) goto L1926;
        i(149);
        goto L1926
    L1643:
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        if (r() == false) goto L2231;
        this.ag = l;
        this.ah = 0;
        i(157);
        return;
    L2231:
        return;
    L1656:
        return;
    L1702:
        this.ah = -768;
        if (r() == false) goto L2235;
        this.ah = 0;
        i(215);
        return;
    L2235:
        return;
    L1717:
        this.ah = 0;
        this.ag = 0;
        this.aj = 0;
        this.ai = 0;
        if (r() == false) goto L2238;
        i(0);
        return;
    L2238:
        return;
    L1721:
        this.aj = 1536;
        if (this.S != 242) goto L1727;
        if (this.ah < 0) goto L1727;
        i(243);
    L1727:
        if (this.S == 243) goto L1729;
    L1731:
        av();
        if (y() == false) goto L2239;
        a(true);
        this.ai = 0;
        this.ag = 0;
        return;
    L2239:
        return;
    L1729:
        if (r() == false) goto L1731;
        a(0);
        goto L1731
    L1735:
        return;
    L1736:
        return;
    L1737:
        this.aj = 0;
        this.ah = 0;
        this.ai = 0;
        this.ag = 0;
        if (k.u(16388) == true) goto L1748;
        if (k.aS.av == false) goto L1744;
        if (k.u(2) == true) goto L1748;
    L1744:
        if (k.aS.av == true) goto L1756;
        if (k.u(8) == true) goto L1748;
    L1756:
        if (k.v(4112) == false) goto L1759;
        this.av = true;
    L1762:
        if (r() == false) goto L1767;
        if (this.S != 228) goto L1767;
        a(0);
        i(252);
    L1767:
        if (this.S != 228) goto L1926;
        return;
    L1759:
        if (k.v(8256) == false) goto L1762;
        this.av = false;
    L1748:
        if (k.aS.ac == null) goto L1754;
        boolean r016 = k.aS.av;
        if (k.aS.ac.ak >= k.aS.ak) goto L1752;
        boolean r128 = true;
    L1753:
        if (r016 == r128) goto L1762;
    L1752:
        r128 = false;
    L1754:
        k.aS.i(235);
        k.aS.ac = null;
        G();
        goto L1762
    L1845:
        cw = true;
        this.aj = 2560;
        if (this.ah >= 0) goto L1848;
    L1853:
        av();
        if (y() == false) goto L1926;
        this.ai = 0;
        this.ag = 0;
        a(0);
        goto L1926
    L1848:
        if (this.S != 264) goto L1851;
        i(266);
        goto L1853
    L1851:
        if (this.S != 263) goto L1853;
        i(265);
        goto L1853
    L1872:
        au();
        return;
    L1874:
        return;
    L1888:
        return;
    L1889:
        a(true);
        if (r() == false) goto L2248;
        this.ah = 0;
        this.ag = 0;
        l();
        return;
    L2248:
        return;
    L1893:
        int r103 = 9;
        int r1110 = 40;
        if (this.av == false) goto L1896;
        r103 = 106;
        r1110 = -40;
    L1896:
        a(r103, this.ak + r1110, this.al - 85);
        if (this.av == true) goto L1901;
        if (k.v(8256) == false) goto L1901;
    L1904:
        G();
        i(357);
    L1906:
        if (r() == false) goto L2249;
        G();
        i(0);
        return;
    L2249:
        return;
    L1901:
        if (this.av == false) goto L1906;
        if (k.v(4112) == false) goto L1906;
    L1909:
        this.ag = 3328;
        if (this.av == false) goto L1913;
        this.ag = -1280;
    L1913:
        if (r() == false) goto L2250;
        this.ag = 0;
        this.K = 4;
        i(364);
        ar();
        return;
    L2250:
        return;
    L117:
        if (r() == false) goto L1926;
        i(371);
        goto L1926
    L125:
        if (r() == false) goto L127;
        a(0);
    L127:
        i.f(this);
        goto L1926
    L135:
        if (r() == false) goto L1926;
        this.P |= 64;
        goto L1926
    L139:
        if (r() == false) goto L142;
        this.ah = 0;
        this.ag = 0;
        this.aj = 0;
        this.ai = 0;
        this.P |= 64;
    L142:
        if (k.u(16388) == true) goto L2204;
        i(0);
        return;
    L2204:
        return;
    L182:
        if (this.aZ == true) goto L188;
        if (a != null) goto L188;
        a(0);
        return;
    L188:
        if (k.v(65568) == false) goto L196;
        this.R = -1;
        if (this.S != 286) goto L193;
        this.R = 287;
        goto L196
    L193:
        if (this.S != 287) goto L196;
        this.R = 286;
    L196:
        if (r() == true) goto L198;
        return;
    L198:
        if (this.R == (-1)) goto L200;
        i(this.R);
    L201:
        this.R = -1;
        return;
    L200:
        i(0);
        goto L201
    L204:
        if ((J & 8) == 0) goto L206;
        h(8);
    L206:
        k.A(19);
        if (I != 8) goto L214;
        aB();
        if (k.v(65568) == false) goto L214;
        if (g == null) goto L214;
        aq();
    L214:
        if (y() == false) goto L2206;
        a = null;
        a(0);
        i = false;
        k.ae = this;
        return;
    L2206:
        return;
    L218:
        if (r() == false) goto L2207;
        this.K = 0;
        this.cN = 0;
        i(295);
        return;
    L2207:
        return;
    L273:
        if (r() == false) goto L2208;
        i(38);
        return;
    L2208:
        return;
    L410:
        if (r() == false) goto L1926;
        this.al -= 20;
        E();
        i(0);
        goto L1926
    L413:
        if (i.aN == null) goto L424;
        if (i.aN.S == 106) goto L424;
        if (Math.abs(i.aN.al - this.al) >= 20) goto L424;
        i.aN.i(106);
        k.e(0, this.aw);
        i.aN.S();
        if (this.av == false) goto L421;
        i.aN.ak = this.ak - 30;
    L422:
        i.aN.al = this.al;
        goto L424
    L421:
        i.aN.ak = this.ak + 30;
    L424:
        if (this.S == 184) goto L426;
    L436:
        this.ag = 0;
        this.ah = 0;
        if (r() == false) goto L439;
    L440:
        k.p();
        i.O();
        i(0);
        if (i.aN == null) goto L1926;
        i.aN.aB = 0;
        i.d(i.aN);
        i.aN = null;
        goto L1926
    L439:
        if (i.aN != null) goto L1926;
    L426:
        if (i.aN == null) goto L436;
        if (i.aN.S == 107) goto L436;
        if (Math.abs(i.aN.al - this.al) >= 20) goto L436;
        i.aN.i(107);
        k.e(0, this.aw);
        i.aN.S();
        if (this.av == false) goto L434;
        i.aN.ak = this.ak - 35;
    L435:
        i.aN.al = this.al;
        goto L436
    L434:
        i.aN.ak = this.ak + 35;
        goto L435
    L444:
        if (r() == false) goto L1926;
        a(53, 1032);
        goto L1926
    L447:
        if (this.T == 1) goto L449;
    L451:
        this.ag >>= 2;
        this.ah = 0;
        this.aj = 0;
        if (a == null) goto L461;
        if (a.ax != 51) goto L457;
        this.ag += a.ag;
    L459:
        a = null;
        goto L461
    L457:
        if (a.ax != 43) goto L459;
        this.al = a.al;
    L461:
        if (r() == false) goto L2220;
        k.l(12);
        return;
    L2220:
        return;
    L449:
        if (this.U != 0) goto L451;
        d(999);
        this.ab = null;
        k.A(18);
        goto L451
    L488:
        if (r() == false) goto L1926;
        x();
        this.al -= ((this.S - 107) + 1) * 20;
        int r129 = this.ak;
        if (this.av == true) goto L492;
        int r25 = 20;
    L493:
        this.ak = r129 + r25;
        a(0, 9);
        goto L1926
    L492:
        r25 = -20;
        goto L493
    L495:
        if (a != null) goto L497;
        a(0);
        return;
    L497:
        if (a.ax == 66) goto L499;
    L503:
        this.al = a.W[1] + 1;
    L505:
        if (this.T != (this.aa.b(this.S) - 1)) goto L510;
        if (a.ax != 60) goto L510;
        this.ak = a.ak;
        i(0);
    L510:
        if (r() == true) goto L512;
        return;
    L512:
        if (a.ax == 66) goto L514;
    L518:
        this.ak = (a.W[0] + a.W[2]) >> 1;
    L520:
        if (a != null) goto L522;
    L531:
        i(0);
        return;
    L522:
        if (a.ax != 66) goto L531;
        if (a.S != 12) goto L531;
        if (a.Z[0] <= 0) goto L529;
        k.aS.i(228);
        return;
    L529:
        k.aS.i(358);
        return;
    L514:
        if (a.S != 11) goto L516;
    L517:
        this.ak = a.ak;
        goto L520
    L516:
        if (a.S != 12) goto L518;
    L499:
        if (a.S != 11) goto L501;
    L502:
        this.al = a.al;
        goto L505
    L501:
        if (a.S != 12) goto L503;
    L536:
        if (this.T != 1) goto L568;
        if (k.aS.ac == null) goto L555;
        if (k.aS.ac.ax != 66) goto L542;
    L543:
        int r96 = 8;
        if (k.aS.ac.ax != 51) goto L547;
        r96 = 10;
    L547:
        if (Math.abs(k.aS.ac.ak - this.ak) < 60) goto L551;
        if (Math.abs(k.aS.ac.al - this.al) < 60) goto L551;
    L553:
        this.ag = 0;
        this.ah = 0;
        int r017 = ((this.ac.ak - this.ak) << 8) / r96;
        int r018 = ((this.ac.al - this.al) << 8) / r96;
        this.ag = r017;
        this.ah = r018;
    L551:
        if (k.aS.ac.ax != 51) goto L553;
        r96 = r96 >> 1;
        goto L553
    L542:
        if (k.aS.ac.ax == 51) goto L543;
    L555:
        if (k.aS.ac == null) goto L563;
        if (k.aS.ac == null) goto L568;
        if (k.aS.ac.ax == 66) goto L568;
        if (k.aS.ac.ax == 51) goto L568;
    L563:
        if (this.av == false) goto L565;
        int r130 = -4864;
    L566:
        this.ag = r130;
        this.ah = -6656;
        goto L568
    L565:
        r130 = 4864;
    L568:
        if (k.aS.ac != null) goto L570;
    L581:
        av();
        if (y() == false) goto L585;
        this.ai = 0;
        this.ag = 0;
        a(0);
    L585:
        if (r() == false) goto L2223;
        a(0);
        return;
    L2223:
        return;
    L570:
        if (k.aS.ac.ax == 66) goto L574;
        if (k.aS.ac.ax != 51) goto L581;
    L574:
        if (r() == true) goto L576;
        return;
    L576:
        if (this.S != 235) goto L578;
        int r131 = 236;
    L579:
        i(r131);
        return;
    L578:
        r131 = 239;
        goto L579
    L589:
        if (r() == true) goto L591;
        return;
    L591:
        if (a == null) goto L601;
        if (a.ax != 66) goto L601;
        if (a.S != 12) goto L601;
        if (a.Z[0] <= 0) goto L599;
        k.aS.i(228);
    L613:
        this.ag = 0;
        this.ah = 0;
        if (a != null) goto L616;
        return;
    L616:
        if (a.ax == 51) goto L2226;
        this.ak = a.ak;
        return;
    L2226:
        return;
    L599:
        k.aS.i(358);
    L601:
        if (a != null) goto L603;
    L612:
        i(0);
        goto L613
    L603:
        if (a.ax != 51) goto L612;
        if (this.av == true) goto L610;
        if (this.W[0] >= a.W[0]) goto L612;
        this.ak += 20;
        goto L612
    L610:
        if (this.W[2] <= a.W[2]) goto L612;
        this.ak -= 20;
        goto L612
    L620:
        if (k.aS.ac != null) goto L622;
    L629:
        this.aj = 512;
        return;
    L622:
        if (k.aS.ac.ax != 51) goto L629;
        if (k.aS.ac.S != 8) goto L629;
        if (this.al > k.aS.ac.al) goto L2227;
        a = null;
        a(0);
        return;
    L2227:
        return;
    L632:
        if (r() == false) goto L2228;
        int r133 = this.ak;
        if (this.av == false) goto L636;
        int r26 = -40;
    L637:
        this.ak = r133 + r26;
        a(0);
        return;
    L636:
        r26 = 40;
        goto L637
    L2228:
        return;
    L682:
        if (this.aO <= 12) goto L687;
        if (this.aR <= 12) goto L687;
        i(79);
    L687:
        if (this.S != 79) goto L706;
        this.ag = 0;
        this.ah = 0;
        if (k.u(33024) == false) goto L691;
    L692:
        this.al += 20;
        x();
        this.al -= 20;
        if (this.av == false) goto L695;
        int r019 = (this.W[0] / 20) - 2;
    L696:
        int r97 = r019;
        int r020 = (this.W[3] / 20) + 1;
        if (this.aQ == 20) goto L701;
        if (this.aQ != 5) goto L706;
    L701:
        if (this.aR != 0) goto L706;
        if (e(r97, r020) >= 12) goto L706;
        a(257, 8);
        goto L706
    L695:
        r019 = (this.W[2] / 20) + 2;
        goto L696
    L691:
        if (k.v(33024) == true) goto L692;
    L706:
        if (this.S != 11) goto L709;
        this.ag = (this.ag << 1) / 3;
    L709:
        if (k.u(33024) == false) goto L714;
        if (am() == false) goto L714;
        this.ab = null;
    L714:
        if (y() == false) goto L717;
        this.ag = 1;
        a(true);
        this.ag = 0;
    L717:
        if (l() == true) goto L1926;
        this.ab = null;
        cq = false;
        a(0);
        goto L1926
    L720:
        if (this.aZ == true) goto L725;
        if (a != null) goto L725;
        cq = false;
        a(0);
    L725:
        if (y() == false) goto L730;
        if (this.ag == 0) goto L730;
        this.ag = 0;
        a(this.S, 4);
    L730:
        if (k.u(4112) == false) goto L737;
        if (this.av == false) goto L734;
        this.ag = -2560;
    L735:
        i.f(this);
        goto L1926
    L734:
        this.av = true;
        goto L735
    L737:
        if (k.u(8256) == false) goto L744;
        if (this.av == true) goto L741;
        this.ag = 2560;
    L742:
        i.f(this);
        goto L1926
    L741:
        this.av = false;
        goto L742
    L744:
        if (k.u(12368) == true) goto L1926;
        this.ag = 0;
        i(79);
        goto L1926
    L771:
        if (r() == false) goto L1926;
        if (this.S != 78) goto L775;
        i(79);
        goto L1926
    L775:
        i(1);
        goto L1926
    L887:
        if (e == null) goto L889;
        k.aS.aA &= -9;
        k.aS.az = 100;
        e = null;
        goto L889
    L1180:
        if (o == 0) goto L1188;
        if (this.al <= o) goto L1926;
        if (this.S != 315) goto L1186;
        i(318);
        goto L1926
    L1186:
        i(28);
        goto L1926
    L1188:
        if (k != (-1)) goto L1926;
        a(this.ah);
        goto L1926
    L1194:
        if (this.Q == 63) goto L1196;
    L1205:
        i(62);
    L1206:
        cu = true;
        goto L1926
    L1196:
        if (k.u(16388) == true) goto L1205;
        if (this.av == false) goto L1202;
        if (k.u(4114) == true) goto L1205;
    L1202:
        if (this.av == true) goto L1206;
        if (k.u(8264) == false) goto L1206;
    L1254:
        if (r() == false) goto L2229;
        i(203);
        return;
    L2229:
        return;
    L1258:
        if (r() == false) goto L1926;
        i(60);
        goto L1926
    L1261:
        if (r() == false) goto L1926;
        int r134 = this.ak;
        if (this.av == false) goto L1265;
        int r27 = -10;
    L1266:
        this.ak = r134 + r27;
        if (this.aO <= 12) goto L1269;
        int r135 = 79;
    L1270:
        a(r135, 9);
        goto L1926
    L1269:
        r135 = 0;
        goto L1270
    L1265:
        r27 = 10;
        goto L1266
    L1293:
        if (r() == false) goto L1296;
        this.P |= 64;
    L1296:
        if (i.at == null) goto L1926;
        if (i.at.S == 168) goto L1926;
        as();
        goto L1926
    L1301:
        if (this.T == 1) goto L1303;
    L1305:
        this.aj = 0;
        this.ah = 0;
        this.ag = 0;
        if (this.av == false) goto L1308;
        int r136 = 1280;
    L1309:
        this.ag = r136;
        if (r() == false) goto L1314;
        this.P |= 64;
        if (l() == true) goto L1314;
        aw();
    L1314:
        i.f(this);
        goto L1926
    L1308:
        r136 = -1280;
        goto L1309
    L1303:
        if (this.U != 0) goto L1305;
        k.A(11);
        goto L1305
    L1339:
        if (this.ac != null) goto L1926;
        a(0);
        goto L1926
    L1415:
        if (r() == false) goto L1926;
        i(326);
        goto L1926
    L1426:
        if (this.aZ == false) goto L1428;
        l();
        goto L1926
    L1428:
        int r137 = this.aC;
        this.aC = r137 - 1;
        if (r137 > 0) goto L1431;
    L1432:
        a(0);
        this.al += 21;
        goto L1926
    L1431:
        if (this.aR != 4) goto L1432;
        if (k.u(4112) == false) goto L1437;
        this.av = true;
        i(332);
        goto L1926
    L1437:
        if (k.u(8256) == false) goto L1440;
        this.av = false;
        i(332);
        goto L1926
    L1440:
        if (k.v(16388) == false) goto L1442;
    L1449:
        i(17);
        goto L1926
    L1442:
        if (this.av == false) goto L1446;
        if (k.v(2) == true) goto L1449;
    L1446:
        if (this.av == true) goto L1926;
        if (k.v(8) == false) goto L1926;
    L1451:
        if (this.av == false) goto L1453;
        int r138 = -2560;
    L1454:
        this.ag = r138;
        if (this.aT < 12) goto L1460;
        if (this.ag >= 0) goto L1460;
        this.ag = 0;
    L1460:
        if (this.aU < 12) goto L1465;
        if (this.ag <= 0) goto L1465;
        this.ag = 0;
    L1465:
        if (this.aZ == false) goto L1468;
        l();
        goto L1926
    L1468:
        if (this.aR == 4) goto L1471;
        a(0);
        goto L1926
    L1471:
        if (k.v(16388) == false) goto L1473;
    L1480:
        i(17);
        goto L1926
    L1473:
        if (this.av == false) goto L1477;
        if (k.v(2) == true) goto L1480;
    L1477:
        if (this.av == true) goto L1482;
        if (k.v(8) == true) goto L1480;
    L1482:
        if (k.u(4112) == false) goto L1485;
        this.av = true;
        i(332);
    L1488:
        if (r() == false) goto L1926;
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        this.aC = 18;
        i(102);
        goto L1926
    L1485:
        if (k.u(8256) == false) goto L1488;
        this.av = false;
        i(332);
        goto L1488
    L1453:
        r138 = 2560;
        goto L1454
    L1499:
        if (r() == false) goto L2230;
        i(38);
        return;
    L2230:
        return;
    L1560:
        if (this.ac == null) goto L1566;
        if (this.ac.ax != 10) goto L1566;
        if (this.ac.S != 32) goto L1566;
    L1569:
        this.ag = 0;
        if (this.av == false) goto L1575;
        if (k.u(4112) == true) goto L1577;
    L1584:
        if (this.av == false) goto L1589;
        if (k.v(8256) == true) goto L1591;
    L1596:
        if (r() == false) goto L1926;
        i(38);
    L1591:
        if (this.av == true) goto L1593;
        boolean r139 = true;
    L1594:
        this.av = r139;
        goto L1926
    L1593:
        r139 = false;
        goto L1594
    L1589:
        if (k.u(4112) == false) goto L1596;
    L1577:
        if (c(this.av) == false) goto L1584;
        i(37);
        if (this.av == false) goto L1581;
        int r140 = -1536;
    L1582:
        this.ag = r140;
        goto L1926
    L1581:
        r140 = 1536;
        goto L1582
    L1575:
        if (k.u(8256) == false) goto L1584;
    L1566:
        if (this.aO == 5) goto L1568;
        this.al = this.W[3];
        i(43);
        goto L1926
    L1568:
        this.al = ((this.W[1] / 20) * 20) + 10;
        goto L1569
    L1599:
        if (r() == false) goto L1926;
        this.P |= 64;
        goto L1926
    L1616:
        if (this.av == false) goto L1618;
        int r141 = -2048;
    L1619:
        this.ag = r141;
        if (r() == false) goto L1622;
    L1629:
        this.al += 20;
        i(147);
        j = true;
        goto L1926
    L1622:
        if (this.av == false) goto L1626;
        if (this.aT != 3) goto L1629;
    L1626:
        if (this.av == true) goto L1926;
        if (this.aU == 3) goto L1926;
    L1618:
        r141 = 2048;
        goto L1619
    L1634:
        if (l == 0) goto L1636;
        g r021 = this;
        int r142 = l;
    L1640:
        r021.ag = r142;
        this.aj = 1536;
        if (r() == false) goto L1926;
        l = 0;
        this.ag = 0;
        i(150);
        goto L1926
    L1636:
        r021 = this;
        if (this.av == false) goto L1639;
        r142 = -1024;
        goto L1640
    L1639:
        r142 = 1024;
        goto L1640
    L1648:
        if (this.av == false) goto L1650;
        int r143 = -1024;
    L1651:
        this.ag = r143;
        if (r() == false) goto L2232;
        i(0);
        return;
    L2232:
        return;
    L1650:
        r143 = 1024;
        goto L1651
    L1658:
        if (this.T >= 2) goto L1660;
    L1666:
        this.ag = 0;
    L1668:
        if (this.av == false) goto L1672;
        if (this.aT == 0) goto L1672;
    L1675:
        a(0);
    L1677:
        if (r() == false) goto L2233;
        this.ag >>= 1;
        i(217);
        this.P |= 64;
        return;
    L2233:
        return;
    L1672:
        if (this.av == true) goto L1677;
        if (this.aU == 0) goto L1677;
    L1660:
        if (this.T > 3) goto L1666;
        if (this.av == false) goto L1664;
        int r144 = -5120;
    L1665:
        this.ag = r144;
        goto L1668
    L1664:
        r144 = 5120;
        goto L1665
    L1681:
        if (this.T != 0) goto L1684;
        this.aj = 1536;
    L1684:
        if (this.aR == 2) goto L1690;
        if (this.aO == 2) goto L1690;
        if (L() == true) goto L1690;
    L1694:
        if (this.aZ == false) goto L1696;
    L1697:
        this.bd = true;
        a(true);
        this.P &= -65;
        this.ah = 0;
        this.ag = 0;
        this.aj = 0;
        this.ai = 0;
    L1699:
        if (r() == false) goto L2234;
        i(0);
        return;
    L2234:
        return;
    L1696:
        if (this.aR != 5) goto L1699;
    L1690:
        if (a != null) goto L1694;
        this.ah = 0;
        this.aj = 0;
        e(0);
        i(50);
        return;
    L1707:
        if (this.T <= 9) goto L1710;
        this.ah = 0;
        this.ag = 0;
    L1710:
        if (r() == false) goto L2236;
        i(0);
        E();
        return;
    L2236:
        return;
    L1714:
        if (r() == false) goto L2237;
        i(0);
        return;
    L2237:
        return;
    L1770:
        if (r() == false) goto L2240;
        this.al += this.aa.c(this.S, this.T) + 10;
        int r145 = this.ak;
        if (this.av == false) goto L1774;
        int r28 = -this.aa.b(this.S, this.T);
    L1775:
        this.ak = r145 + r28;
        a(0);
        return;
    L1774:
        r28 = this.aa.b(this.S, this.T);
        goto L1775
    L2240:
        return;
    L1778:
        if (k.v(16388) == false) goto L1781;
        i(259);
    L1800:
        if (r() == false) goto L2241;
        i(258);
        return;
    L2241:
        return;
    L1781:
        if (k.v(2) == true) goto L1789;
        if (k.v(8) == true) goto L1789;
        if (k.v(4112) == true) goto L1789;
        if (k.v(8256) == false) goto L1800;
    L1789:
        if (k.v(2) == false) goto L1791;
    L1792:
        this.av = true;
    L1798:
        i(261);
        goto L1800
    L1791:
        if (k.v(4112) == true) goto L1792;
        if (k.v(8) == false) goto L1796;
    L1797:
        this.av = false;
        goto L1798
    L1796:
        if (k.v(8256) == false) goto L1798;
    L1804:
        if (k.v(16388) == false) goto L1808;
        i(259);
        return;
    L1808:
        if (k.v(2) == true) goto L1816;
        if (k.v(8) == true) goto L1816;
        if (k.v(4112) == true) goto L1816;
        if (k.v(8256) == true) goto L1816;
        if (k.v(33024) == false) goto L2242;
        a(0);
        this.al += 10;
        return;
    L2242:
        return;
    L1816:
        if (k.v(2) == false) goto L1818;
    L1819:
        this.av = true;
    L1825:
        i(261);
        return;
    L1818:
        if (k.v(4112) == true) goto L1819;
        if (k.v(8) == false) goto L1823;
    L1824:
        this.av = false;
        goto L1825
    L1823:
        if (k.v(8256) == false) goto L1825;
    L1832:
        if (r() == true) goto L1834;
        return;
    L1834:
        if (this.S != 259) goto L1838;
        this.ag = 0;
        this.ah = -7680;
        i(263);
        return;
    L1838:
        if (this.S == 261) goto L1840;
        return;
    L1840:
        if (this.av == false) goto L1842;
        int r146 = -3072;
    L1843:
        this.ag = r146;
        this.ah = -7680;
        i(264);
        return;
    L1842:
        r146 = 3072;
        goto L1843
    L1857:
        if (r() == false) goto L1859;
        a(0);
    L1859:
        av();
        if (y() == false) goto L1926;
        this.ai = 0;
        this.ag = 0;
        a(0);
        goto L1926
    L1863:
        if (i.at != null) goto L1865;
    L1870:
        as();
        return;
    L1865:
        if (i.at.Z[0] != 4) goto L1870;
        if (this.cE > 0) goto L1870;
        au();
        return;
    L1876:
        if (k.u(62430) == false) goto L1879;
        i(0);
        goto L1926
    L1879:
        if (r() == false) goto L2245;
        this.P |= 64;
        aB();
        if (k.v(65568) == false) goto L2246;
        ar();
        return;
    L2246:
        return;
    L2245:
        return;
    L1885:
        if (r() == false) goto L2247;
        i(303);
        return;
    L2247:
        return;
    L1917:
        if (r() == false) goto L1926;
        if (j == true) goto L1926;
        if (l() == true) goto L1926;
        if (j == true) goto L1926;
        a(0);
    L1926:
        if (this.S == 9) goto L1933;
        if (this.ab == null) goto L1933;
        if (this.ab.S != 14) goto L1933;
        this.ab = null;
    L1933:
        if (this.aR == 2) goto L1939;
        if (this.aO == 2) goto L1939;
        if (L() == true) goto L1939;
    L1943:
        if (this.aO != 6) goto L1945;
    L1946:
        a(18, 0, 0, this);
    L1947:
        boolean r98 = false;
        if ((J & 4) == 0) goto L2029;
        if (this.S == 50) goto L2029;
        if (i.at == null) goto L2006;
        if (g == null) goto L1960;
        if (g == null) goto L2006;
        if (g(g) == true) goto L2006;
    L1960:
        if (this.aZ == true) goto L1964;
        if (k(this.S) == false) goto L2029;
    L1964:
        if (i.at.v() == false) goto L2029;
        boolean r104 = true;
        if (i.at.ax != 72) goto L1974;
        if (i.at.Z[0] != 1) goto L1974;
        if (k.h(i.at.ak - this.ak, i.at.al - this.W[1]) < i.at.Z[3]) goto L1989;
        r104 = false;
    L1989:
        r98 = r104;
        if (r104 == false) goto L2029;
        if (k.v(65568) == false) goto L1994;
    L1997:
        this.ag = 0;
        this.ah = 0;
        this.aj = 0;
        if (i.at.Z[0] != 4) goto L2000;
    L2003:
        c(i.at);
        z = false;
        k.A(30);
        goto L2029
    L2000:
        if (k.k() == true) goto L2002;
        U();
        goto L2003
    L2002:
        G();
        goto L2003
    L1994:
        if (k.k() == true) goto L2029;
        if (V() == false) goto L2029;
    L1974:
        if (i.at.ax != 72) goto L1985;
        if (i.at.Z[0] != 4) goto L1985;
        if (i.at.Z[4] >= 0) goto L1980;
    L1983:
        r104 = false;
        goto L1989
    L1980:
        if (this.aZ == false) goto L1983;
        if (Math.abs(this.ak - i.at.ak) >= ((this.W[2] - this.W[0]) << 1)) goto L1989;
    L1985:
        if (i.at.ax != 72) goto L1989;
        if (i.at.Z[0] != 3) goto L1989;
        r104 = false;
    L2006:
        if (g == null) goto L2029;
        if (g.ax != 11) goto L2029;
        if (g.Z[19] != 1) goto L2029;
        if (g.P() == true) goto L2029;
        if (this.aZ == false) goto L2016;
    L2017:
        r98 = true;
        if (k.v(65568) == false) goto L2020;
    L2023:
        this.ag = 0;
        this.ah = 0;
        this.aj = 0;
        if (k.k() == true) goto L2026;
        U();
    L2027:
        c(g);
        z = false;
        goto L2029
    L2026:
        G();
        goto L2027
    L2020:
        if (k.k() == true) goto L2029;
        if (V() == false) goto L2029;
    L2016:
        if (k(this.S) == true) goto L2017;
    L2029:
        if (r98 == false) goto L2042;
        if (k.k() == true) goto L2037;
        if (T() == true) goto L2035;
        G();
    L2035:
        c(200 + k.O, 120 + k.P);
        d(200 + k.O, 120 + k.P);
    L2040:
        cm = true;
    L2048:
        if (o() == false) goto L2051;
        ao();
    L2051:
        if (this.aA != 0) goto L2054;
        this.aA = 1;
    L2054:
        if ((this.aA & 4) == 0) goto L2057;
        this.aA &= -5;
    L2064:
        if (this.aO != 7) goto L2066;
    L2067:
        cq = false;
    L2069:
        if (A == false) goto L2073;
        E();
        this.av = B;
        this.ag = 0;
        i(148);
        A = false;
        return;
    L2073:
        if (f() == false) goto L2076;
        cq = false;
    L2076:
        if (this.ac == null) goto L2083;
        if (this.ac.ax != 51) goto L2080;
    L2081:
        cq = false;
        goto L2083
    L2080:
        if (this.ac.ax == 66) goto L2081;
    L2083:
        if (cq == false) goto L2153;
        if (E == true) goto L2153;
        if (k.v(16398) == false) goto L2142;
        if (k.v(16388) == true) goto L2095;
        if (k.v(2) == true) goto L2095;
        if (k.v(8) == true) goto L2095;
    L2139:
        if (this.S == 233) goto L2153;
        this.ag = 0;
    L2095:
        if (k.v(2) == false) goto L2098;
        this.av = true;
    L2100:
        this.ah = 0;
        if (this.S == 79) goto L2107;
        if (this.S == 32) goto L2107;
        if (this.S == 199) goto L2107;
        if (a == null) goto L2126;
        if (a.ax != 66) goto L2126;
        if (a.S == 11) goto L2118;
        if (a.S != 12) goto L2126;
    L2118:
        if (this.ac == null) goto L2126;
        boolean r022 = this.av;
        if (this.ac.ak >= this.ak) goto L2122;
        boolean r147 = true;
    L2123:
        if (r022 != r147) goto L2126;
        return;
    L2122:
        r147 = false;
    L2126:
        if (this.aZ == false) goto L2128;
    L2129:
        i(233);
        if (a == null) goto L2139;
        if (a.ax != 51) goto L2139;
        i.bq = this.al + 20;
        goto L2139
    L2128:
        if (a != null) goto L2129;
        if (k.v(16388) == false) goto L2137;
        i(233);
        goto L2139
    L2137:
        i(22);
    L2107:
        if (e(((this.W[0] + this.W[2]) / 2) / 20, (this.W[1] / 20) - 1) > 12) goto L2139;
        i(21);
        goto L2139
    L2098:
        if (k.v(8) == false) goto L2100;
        this.av = false;
        goto L2100
    L2142:
        if (this.av == false) goto L2146;
        if (k.x(8256) == false) goto L2146;
    L2150:
        if (k.aA <= 0) goto L2153;
        i(25);
        this.ag = 0;
        this.ah = 0;
    L2146:
        if (this.av == true) goto L2153;
        if (k.x(4112) == true) goto L2150;
    L2153:
        if (cp == false) goto L2168;
        if (this.aO == 9) goto L2159;
        if (this.aP == 9) goto L2159;
    L2161:
        if (ct == false) goto L2168;
        if (ak() == false) goto L2165;
    L2166:
        this.ag = 0;
        this.ah = 0;
        this.aj = 0;
        goto L2168
    L2165:
        if (al() == false) goto L2168;
    L2159:
        if (this.aQ != 9) goto L2161;
    L2168:
        if (cu == false) goto L2177;
        if (k.v(33024) == false) goto L2177;
        a(2560);
        if (this.ab == null) goto L2177;
        if (this.ab.ax != 14) goto L2177;
        H();
    L2177:
        if (cv == false) goto L2192;
        if (k.u(16388) == false) goto L2181;
    L2190:
        this.aF = 1;
        goto L2192
    L2181:
        if (k.v(16388) == true) goto L2190;
        if (k.u(8) == true) goto L2190;
        if (k.v(8) == true) goto L2190;
        if (k.u(2) == true) goto L2190;
        if (k.v(2) == true) goto L2190;
    L2192:
        if (cw == true) goto L2194;
        return;
    L2194:
        if (this.aO != 5) goto L2252;
        i(280);
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        this.al = ((this.W[1] / 20) * 20) + 10;
        return;
    L2252:
        return;
    L2066:
        if (this.aO != 9) goto L2069;
    L2057:
        if (z == false) goto L2064;
        if (i.bn == true) goto L2064;
        if (E == true) goto L2064;
        ap();
        goto L2064
    L2037:
        if (T() == false) goto L2039;
        U();
    L2039:
        a(8, this.ak, this.al - 85);
        this.ae.ak = this.ak;
        this.ae.al = this.al - 85;
        goto L2040
    L2042:
        if (cm == false) goto L2048;
        cm = false;
        if (k.k() == true) goto L2046;
        U();
        goto L2048
    L2046:
        G();
        goto L2048
    L1945:
        if (this.aR != 6) goto L1947;
    L1939:
        if (a != null) goto L1943;
        this.ah = 0;
        this.aj = 0;
        e(0);
        i(50);
        return;
    L102:
        if (i.aN.P() == false) goto L104;
        i.aN = null;
        goto L104
    L86:
        if (this.aA > 1) goto L88;
        this.aA = 2;
        goto L88
    L79:
        cn = 0;
    L68:
        if (c(this.S) == false) goto L71;
        goto L69
    }

    public final boolean f() {
        if (ci != null) goto L5;
        return false;
    L5:
        if (g(ci) == true) goto L7;
        return false;
    L7:
        if (Math.abs(ci.ak - this.ak) < 120) goto L9;
        return false;
    L9:
        if (Math.abs(ci.al - this.al) >= 20) goto L16;
        return true;
    L16:
        return false;
    }

    private boolean c(boolean r4) {
        if (r4 == false) goto L5;
        int r0 = (this.W[0] / 20) - 1;
    L7:
        if (k.g(r0, this.al / 20) < 12) goto L10;
        return false;
    L10:
        return true;
    L5:
        r0 = (this.W[2] / 20) + 1;
        goto L7
    }

    private boolean ao() {
        if (k.v(131072) == true) goto L7;
        if (k.c(355, 197, 30, 26) == true) goto L7;
        return false;
    L7:
        if (k.at == 0) goto L9;
        return false;
    L9:
        if (k.as > 1) goto L11;
        return false;
    L11:
        if (k.C != null) goto L13;
    L16:
        k.at = 1;
        k.aS.h(k.ar[(k.p(I) + 1) % k.as]);
        k.A(23);
        return true;
    L13:
        if (k.C != null) goto L15;
        return false;
    L15:
        if ((this.P & 512) != 0) goto L16;
        return false;
    }

    private void ap() {
        if (I != 4) goto L8;
        if (this.aA >= 2) goto L8;
        h(1);
    L8:
        if (this.ac == null) goto L12;
        if (this.ac.ax != 10) goto L12;
    L14:
        boolean r0 = false;
    L15:
        boolean r6 = r0;
        if (k.v(65568) == true) goto L18;
        return;
    L18:
        if (r6 == true) goto L20;
        return;
    L20:
        if (I != 1) goto L39;
        this.ag = 0;
        this.ah = 0;
        this.aj = 0;
        if (this.S != 79) goto L31;
        if (a == null) goto L31;
        if (a.ax != 51) goto L31;
        if (a.aD == 0) goto L31;
        return;
    L31:
        if (this.S != 79) goto L33;
        int r1 = 81;
    L34:
        i(r1);
        if (k.E == null) goto L52;
        k.E.K();
        return;
    L52:
        return;
    L33:
        r1 = 67;
        goto L34
    L39:
        if (I != 8) goto L45;
        if (this.S == 79) goto L53;
        this.ai = 0;
        this.ag = 0;
        this.K = 0;
        this.cN = 0;
        i(303);
        return;
    L53:
        return;
    L45:
        if (I == 2) goto L47;
        return;
    L47:
        if (this.S == 79) goto L55;
        this.ai = 0;
        this.ag = 0;
        i(286);
        k.A(29);
        return;
    L55:
        return;
    L12:
        if (b() == true) goto L14;
        r0 = true;
        goto L15
    }

    public static void d(int r5) {
        if (s == false) goto L5;
        return;
    L5:
        if (t == 0) goto L8;
        return;
    L8:
        if (k.aS.c() == false) goto L10;
        return;
    L10:
        if (k.aS.S != 67) goto L12;
        return;
    L12:
        if (k.aS.S != 183) goto L14;
        return;
    L14:
        if (k.aS.S != 184) goto L16;
        return;
    L16:
        if (k.aS.S == 205) goto L35;
        i.bh = 8;
        int[] r0 = x;
        r0[1] = r0[1] - r5;
        if (x[1] > 0) goto L29;
        x[1] = 0;
        if (k.bh[k.aj] != 3) goto L23;
        return;
    L23:
        if (k.aS.aZ == false) goto L25;
    L27:
        k.aS.bl = 0;
        k.aS.G();
        k.aS.H();
        a = null;
        return;
    L25:
        if (a != null) goto L27;
        k.aS.E();
        goto L27
    L29:
        t = 10;
        return;
    }

    public static void e(int r4) {
        x[1] = r4;
    }

    public static void f(int r4) {
        if (x[1] <= r4) goto L6;
        x[1] = r4;
        return;
    }

    public static boolean g() {
        if (x[1] > 0) goto L6;
        return true;
    L6:
        return false;
    }

    public static boolean h() {
        if (s == false) goto L5;
        return true;
    L5:
        if (t != 0) goto L10;
        return false;
    L10:
        return true;
    }

    private void aq() {
        if (g != null) goto L6;
        i(295);
        k.v();
        return;
    L6:
        k.A(16);
        int r0 = (this.W[0] + this.W[2]) >> 1;
        int r02 = (this.W[1] + this.W[3]) >> 1;
        int r03 = (g.W[0] + g.W[2]) >> 1;
        int r04 = (g.W[1] + g.W[3]) >> 1;
        int r05 = Math.abs(r03 - r0);
        int r06 = Math.abs(r04 - r02) << 8;
        if (r05 <= 0) goto L25;
        if (r06 <= 0) goto L25;
        int r07 = r06 / r05;
        if (r07 > 64) goto L14;
        i(306);
    L27:
        int r08 = i.bu[k.au];
        this.K = 6;
        if (this.K <= 3) goto L45;
        i.a(8, 5, 14, this.av, this.L, this.M + 30, 300);
        if (g.ax != 4) goto L36;
        if (g.S != 30) goto L46;
        g.i(29);
        return;
    L46:
        return;
    L36:
        if (g.ax == 58) goto L38;
        g.aB -= (r08 * this.K) / 6;
        g.C();
        return;
    L38:
        switch(g.S) {
            case 0: goto L39;
            case 1: goto L44;
            case 2: goto L40;
            case 3: goto L44;
            case 4: goto L44;
            case 5: goto L39;
            case 6: goto L44;
            case 7: goto L39;
            case 8: goto L44;
            case 9: goto L39;
            case 10: goto L44;
            case 11: goto L39;
            default: goto L44;
        };
    L39:
        g.i(this.S + 1);
        return;
    L40:
        g.i(3);
        return;
    L44:
        return;
    L45:
        return;
    L14:
        if (r07 <= 64) goto L19;
        if (r07 > 256) goto L19;
        i(305);
    L19:
        if (r07 <= 256) goto L27;
        if (r07 > 1024) goto L27;
        i(304);
    L25:
        if (r06 != 0) goto L27;
        i(306);
        goto L27
    }

    private void ar() {
        if (g != null) goto L6;
        i(0);
        k.v();
        return;
    L6:
        k.A(16);
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        int r0 = this.ak;
        int r02 = (this.Y[1] + this.Y[3]) >> 1;
        int r03 = g.ak;
        int r04 = (g.Y[1] + g.Y[3]) >> 1;
        int r05 = Math.abs(r03 - r0);
        int r06 = Math.abs(r04 - r02) << 8;
        if (this.S != 364) goto L9;
    L34:
        int r07 = i.bu[k.au] << 1;
        if (this.K <= 3) goto L52;
        i.a(8, 5, 14, this.av, this.L, this.M, 300);
        if (g.ax != 4) goto L43;
        if (g.S != 30) goto L53;
        g.i(29);
        return;
    L53:
        return;
    L43:
        if (g.ax == 58) goto L45;
        g.aB -= (r07 * this.K) / 6;
        g.C();
        return;
    L45:
        switch(g.S) {
            case 0: goto L46;
            case 1: goto L51;
            case 2: goto L47;
            case 3: goto L51;
            case 4: goto L51;
            case 5: goto L46;
            case 6: goto L51;
            case 7: goto L46;
            case 8: goto L51;
            case 9: goto L46;
            case 10: goto L51;
            case 11: goto L46;
            default: goto L51;
        };
    L46:
        g.i(this.S + 1);
        return;
    L47:
        g.i(3);
        return;
    L51:
        return;
    L52:
        return;
    L9:
        if (r05 <= 0) goto L29;
        if (r06 <= 0) goto L29;
        if ((r04 - r02) >= 20) goto L29;
        int r08 = r06 / r05;
        if (r08 > 64) goto L18;
        i(301);
        goto L34
    L18:
        if (r08 <= 64) goto L23;
        if (r08 > 256) goto L23;
        i(300);
    L23:
        if (r08 <= 256) goto L34;
        if (r08 > 1024) goto L34;
        i(299);
    L29:
        if ((r04 - r02) <= 20) goto L32;
        i(302);
        goto L34
    L32:
        if (r06 != 0) goto L34;
        i(301);
        goto L34
    }

    public final void c(i r8) {
        t();
        i.bq = 0;
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        int r0 = (this.W[0] + this.W[2]) >> 1;
        int r02 = (this.W[1] + this.W[3]) >> 1;
        int r11 = r8.ak;
        int r12 = r8.al;
        if (r8.ax != 11) goto L5;
    L6:
        r11 = (r8.W[0] + r8.W[2]) >> 1;
        r12 = r8.W[3] - 45;
    L7:
        int r03 = Math.abs(r11 - r0);
        int r04 = Math.abs(r12 - r02) << 8;
        if (b(this.S) == false) goto L11;
        i(292);
        k.A(30);
    L40:
        this.cF = 5120;
        this.cx = (8 * j.m) / 360;
        this.cG = this.av;
        if (r8.ax != 72) goto L60;
        if (r8.Z[0] == 0) goto L49;
        if (r8.Z[0] == 3) goto L49;
        if (r8.Z[0] == 4) goto L49;
        if (r8.Z[0] != 1) goto L57;
    L58:
        this.cM = 0;
    L67:
        this.cE = 0;
        int r9 = 0;
    L69:
        if (r9 >= this.aa.b(this.S)) goto L71;
        this.cE += this.aa.a(this.S, r9);
        r9 = r9 + 1;
        goto L69
    L71:
        t();
        int r05 = (r8.W[1] + r8.W[3]) >> 1;
        this.cz = r8.ak - this.X[0];
        this.cA = r05 - this.X[1];
        this.cB = k.h(this.cz, this.cA);
        if (this.cz != 0) goto L74;
        this.cz = 1;
    L74:
        this.cy = j.b(-this.cz, this.cA);
        this.cC = this.cz / this.cE;
        int r06 = (Math.abs(this.cA) << 8) / Math.abs(this.cz);
        if (this.cz != 1) goto L77;
        this.cD = this.cA / this.cE;
    L79:
        if (r05 >= this.X[1]) goto L81;
        this.cD = -this.cD;
    L81:
        this.cJ = this.X[0];
        this.cK = this.X[1];
        this.cH = this.X[0];
        this.cI = this.X[1];
        F = r8;
        return;
    L77:
        this.cD = (Math.abs(this.cC) * r06) >> 8;
        goto L79
    L57:
        if (r8.Z[0] != 2) goto L67;
    L49:
        if (r8.Z[1] <= 0) goto L52;
        this.cF = r8.Z[1];
    L52:
        if (r8.Z[2] <= 0) goto L67;
        this.cx = (r8.Z[2] * j.m) / 360;
        goto L67
    L60:
        if (r8.ax == 11) goto L64;
        if (r8.ax != 17) goto L67;
    L64:
        if (this.S != 298) goto L66;
        this.cF = 7680;
        this.cx = 0;
        goto L67
    L66:
        this.cF = 5120;
        this.cx = 0;
        goto L67
    L11:
        if (this.S != 298) goto L14;
        k.A(30);
        goto L40
    L14:
        if (r03 <= 0) goto L35;
        if (r04 <= 0) goto L35;
        int r07 = r04 / r03;
        if (r07 > 64) goto L21;
        i(272);
    L33:
        k.A(30);
        goto L40
    L21:
        if (r07 <= 64) goto L26;
        if (r07 > 256) goto L26;
        i(273);
    L26:
        if (r07 <= 256) goto L31;
        if (r07 > 1024) goto L31;
        i(274);
    L31:
        if (r07 <= 1024) goto L33;
        i(275);
    L35:
        if (r03 != 0) goto L38;
        i(275);
        k.A(30);
        goto L40
    L38:
        if (r04 != 0) goto L40;
        i(272);
        k.A(30);
        goto L40
    L5:
        if (r8.ax != 17) goto L7;
        goto L6
    }

    private void as() {
        if (i.at == null) goto L12;
        if (g != null) goto L7;
    L10:
        F = i.at;
    L22:
        if (F != null) goto L25;
        a(0);
        return;
    L25:
        this.cJ = this.X[0];
        this.cK = this.X[1];
        this.cH += this.cC;
        this.cI += this.cD;
        int r1 = this.cE - 1;
        this.cE = r1;
        if (r1 > 0) goto L67;
        this.cH = F.ak;
        this.cI = (F.W[1] + F.W[3]) >> 1;
        if (F.ax == 72) goto L30;
    L37:
        this.ak = this.X[0];
        this.al = this.X[1];
    L39:
        if (F.ax != 72) goto L48;
        switch(F.Z[0]) {
            case 0: goto L42;
            case 1: goto L42;
            case 2: goto L42;
            case 3: goto L42;
            case 4: goto L42;
            default: goto L64;
        };
    L42:
        this.cL = 0;
        if (F.Z[0] == 4) goto L66;
        i(277);
        return;
    L66:
        return;
    L64:
        i(0);
        return;
    L48:
        if (F.ax == 11) goto L52;
        if (F.ax != 17) goto L64;
    L52:
        switch(F.ax) {
            case 11: goto L54;
            case 17: goto L63;
            default: goto L64;
        };
    L63:
        return;
    L54:
        if (this.S != 298) goto L57;
        F.i(168);
        return;
    L57:
        this.cL = 0;
        i(277);
        F.ag = (this.cF >> 8) * j.b(this.cy);
        F.ah = (-(this.cF >> 8)) * j.b(j.n - this.cy);
        if (F.g(this) == false) goto L61;
        F.i(181);
        return;
    L61:
        F.i(180);
        return;
    L30:
        if (F.Z[0] != 4) goto L37;
        if (F.Z[4] < 0) goto L39;
        i r0 = k.q(F.Z[4]);
        if (r0 == null) goto L39;
        F.ac = r0;
        r0.aq = F.ak - r0.ak;
        r0.ar = F.al - r0.al;
        goto L39
    L67:
        return;
    L7:
        if (g == null) goto L12;
        if (g(g) == false) goto L10;
    L12:
        if (g != null) goto L14;
    L20:
        F = null;
        goto L22
    L14:
        if (g.ax != 11) goto L20;
        if (g.Z[19] != 1) goto L20;
        if (g.P() == true) goto L20;
        F = g;
        goto L22
    }

    public final void i() {
        a(this.cJ, this.cK, this.cH, this.cI, this.cy);
    }

    public static void a(int r10, int r11, int r12, int r13, int r14) {
        int r0 = k.h(r12 - r10, r13 - r11) / 6;
        if (r0 > 0) goto L5;
        return;
    L5:
        if (k.z[61] == null) goto L12;
        int r112 = 0;
        int r15 = 0;
    L8:
        if (r15 >= r0) goto L13;
        r112 = r112 + 6;
        int r02 = (r112 * j.b(r14)) >> 8;
        k.z[61].a(i.bg, 0, 0, (r12 + r02) - k.O, (r13 - ((r112 * j.b(j.n - r14)) >> 8)) - k.P, 0, 0, 0);
        r15 = r15 + 1;
        goto L8
    L13:
        return;
    }

    public final void a(i r7, i r8) {
        if (r7 != null) goto L5;
        return;
    L5:
        if (r8 == null) goto L12;
        r7.t();
        r8.t();
        int r0 = r7.X[0];
        int r02 = r7.X[1];
        int r03 = r8.X[0];
        int r04 = r8.X[1];
        int r10 = r03 - r0;
        int r05 = r04 - r02;
        if (r10 != 0) goto L10;
        r10 = 1;
    L10:
        a(r0, r02, r03, r04, j.b(-r10, r05));
        return;
    }

    private void at() {
        int r0 = (this.cB * j.b(this.cy)) >> 8;
        int r02 = this.cB;
        int r1 = this.cy;
        int r03 = (r02 * j.b(j.n - r1)) >> 8;
        this.ak = this.cH + r0;
        this.al = this.cI - r03;
    }

    private void au() {
        if (i.at == null) goto L12;
        if (g != null) goto L7;
    L10:
        F = i.at;
    L22:
        if (F != null) goto L26;
        a(0);
        return;
    L26:
        if (F.ax == 72) goto L28;
    L30:
        this.cJ = this.ak;
        this.cK = this.al;
    L31:
        this.cH = F.ak;
        this.cI = (F.W[1] + F.W[3]) >> 1;
        if (F.ax != 72) goto L190;
        switch(F.Z[0]) {
            case 0: goto L35;
            case 1: goto L69;
            case 2: goto L69;
            case 3: goto L53;
            case 4: goto L162;
            default: goto L188;
        };
    L35:
        this.cB -= this.cF >> 8;
        if (this.cy >= j.o) goto L41;
        if (this.cy <= j.n) goto L41;
        this.cy += this.cx;
    L45:
        at();
        if (i.a(this.W, F.W) == false) goto L51;
        i(22);
        this.ag = (-(this.cF >> 8)) * j.b(this.cy);
        this.ah = (this.cF >> 8) * j.b(j.n - this.cy);
        int r0 = this.ak;
        this.ak += this.ag >> 8;
        a(false);
        if (y() == false) goto L50;
        this.ag = 0;
    L50:
        this.ak = r0;
        t();
        F = null;
    L51:
        av();
        return;
    L41:
        if (this.cy <= j.o) goto L43;
    L44:
        this.cy -= this.cx;
        goto L45
    L43:
        if (this.cy >= j.n) goto L45;
    L53:
        this.cB -= this.cF >> 8;
        if (this.cy >= j.o) goto L59;
        if (this.cy <= j.n) goto L59;
        this.cy += this.cx;
    L63:
        at();
        if (i.a(this.W, F.W) == false) goto L66;
        F = null;
        i.at = null;
    L66:
        av();
        return;
    L59:
        if (this.cy <= j.o) goto L61;
    L62:
        this.cy -= this.cx;
        goto L63
    L61:
        if (this.cy >= j.n) goto L63;
    L188:
        return;
    L69:
        if (this.cM != 0) goto L81;
        this.cB -= this.cF >> 8;
        if (F.Z[0] != 1) goto L76;
        if (this.cB > 75) goto L81;
        this.cB = 75;
        goto L81
    L76:
        if (F.Z[0] != 2) goto L81;
        if (this.cB > 75) goto L81;
        this.cB = 75;
    L81:
        if (this.cM != 0) goto L107;
        if (F.Z[0] != 2) goto L86;
        this.cx = (15 * j.m) / 360;
    L86:
        if (this.cy >= j.o) goto L91;
        if (this.cy <= j.n) goto L91;
        this.cy += this.cx;
    L96:
        if (Math.abs(this.cy - j.o) > this.cx) goto L151;
        if (F.Z[0] != 2) goto L100;
        this.cL = (35 * j.m) / 360;
    L101:
        this.cM = 1;
        if (F.Z[0] != 2) goto L151;
        if (this.cB <= 75) goto L151;
        this.cB = 75;
        goto L151
    L100:
        this.cL = (16 * j.m) / 360;
    L151:
        if (this.S == 293) goto L153;
    L158:
        at();
    L159:
        this.cJ = this.ak;
        this.cK = this.al;
        return;
    L153:
        if (F.Z[0] != 1) goto L158;
        this.cB += 6;
        this.al += 6;
        if (this.cB < F.Z[3]) goto L157;
        this.al = this.W[3];
        a(0);
    L157:
        this.cK = this.al;
    L91:
        if (this.cy <= j.o) goto L93;
    L94:
        this.cy -= this.cx;
        goto L96
    L93:
        if (this.cy >= j.n) goto L96;
    L107:
        if (this.cM != 1) goto L151;
        if (this.cy < (j.o - this.cL)) goto L113;
        if (this.cy > (j.o + this.cL)) goto L113;
    L131:
        if (F.Z[0] != 1) goto L134;
        this.cL--;
    L134:
        if (F.Z[0] != 2) goto L140;
        int r1 = this.cy;
        if (this.cG == false) goto L138;
        int r2 = -10;
    L139:
        this.cy = r1 + r2;
    L146:
        if (this.cL > 0) goto L151;
        if (this.S == 293) goto L151;
        i(293);
        this.cM = 2;
        this.cy = j.o;
        at();
        goto L151
    L138:
        r2 = 10;
        goto L139
    L140:
        int r12 = this.cy;
        if (this.cG == false) goto L143;
        int r22 = -5;
    L144:
        this.cy = r12 + r22;
        goto L146
    L143:
        r22 = 5;
    L113:
        if (this.cy >= (j.o - this.cL)) goto L116;
        this.cy = j.o - this.cL;
    L119:
        if (this.cG == true) goto L121;
        boolean r13 = true;
    L122:
        this.cG = r13;
        if (F.Z[0] != 2) goto L131;
        g r02 = k.aS;
        if (k.aS.av == false) goto L127;
        int r14 = -3328;
    L128:
        r02.ag = r14;
        k.aS.ah = -6656;
        k.aS.i(243);
        return;
    L127:
        r14 = 3328;
        goto L128
    L121:
        r13 = false;
        goto L122
    L116:
        if (this.cy <= (j.o + this.cL)) goto L119;
        this.cy = j.o + this.cL;
        goto L119
    L162:
        if (k.k() == true) goto L165;
        d(200 + k.O, 120 + k.P);
    L165:
        if (k.v(65568) == false) goto L167;
    L172:
        this.cB -= (this.cF >> 8) >> 2;
    L173:
        int r03 = (this.cB * j.b(this.cy)) >> 8;
        int r04 = (this.cB * j.b(j.n - this.cy)) >> 8;
        F.ak = this.cJ - r03;
        F.al = this.cK + r04;
        if (F.ac != null) goto L176;
    L186:
        av();
        return;
    L176:
        if (F.ac.S != 2) goto L178;
    L181:
        F.ac = null;
        F.P |= 160;
        F.Z[4] = -1;
        F = null;
        i(0);
        a(false);
        if (k.k() == true) goto L184;
        U();
        goto L186
    L184:
        G();
        goto L186
    L178:
        if (F.ac.S == 3) goto L181;
        if (this.cB < 20) goto L181;
        F.ac.ak = F.ak - F.ac.aq;
        F.ac.al = F.al - F.ac.ar;
        goto L186
    L167:
        if (k.u(65568) == true) goto L172;
        if (k.k() == true) goto L173;
        if (V() == false) goto L173;
    L190:
        if (F.ax == 11) goto L194;
        if (F.ax == 17) goto L194;
        return;
    L194:
        if (this.S != 293) goto L200;
        this.cB += 6;
        this.al += 6;
        if (this.cB < 15) goto L198;
        this.al = this.W[3];
        a(0);
    L198:
        this.cK = this.al;
        return;
    L200:
        this.ag = (-(this.cF >> 8)) * j.b(this.cy);
        this.ah = (this.cF >> 8) * j.b(j.n - this.cy);
        return;
    L28:
        if (F.Z[0] != 4) goto L30;
        this.cJ = this.X[0];
        this.cK = this.X[1];
        goto L31
    L7:
        if (g == null) goto L12;
        if (g(g) == false) goto L10;
    L12:
        if (g != null) goto L14;
    L20:
        F = null;
        goto L22
    L14:
        if (g.ax != 11) goto L20;
        if (g.Z[19] != 1) goto L20;
        if (g.P() == true) goto L20;
        F = g;
        goto L22
    }

    private void d(boolean r8) {
        this.al = ((this.W[3] + 1) / 20) * 20;
        this.al--;
        if (r8 == false) goto L10;
        if (this.aR != 4) goto L7;
    L16:
        this.ah = 1;
        boolean r9 = false;
        if (r8 == false) goto L20;
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        this.aC = 18;
        i(102);
    L30:
        if (I != 4) goto L33;
        h(1);
    L33:
        if (this.S != 16) goto L35;
        return;
    L35:
        if (this.Q != 16) goto L37;
        return;
    L37:
        if (r8 == false) goto L39;
        return;
    L39:
        if (r9 == true) goto L41;
        return;
    L41:
        if (((this.al - y) / 20) >= 20) goto L43;
        return;
    L43:
        if (h() == true) goto L51;
        a(21, 0, 0, this);
        return;
    L51:
        return;
    L20:
        if (this.S != 16) goto L22;
    L23:
        i(5);
        this.ag = 0;
        goto L30
    L22:
        if (this.Q == 16) goto L23;
        if (this.S != 150) goto L27;
        i(152);
    L28:
        r9 = true;
        goto L30
    L27:
        int r0 = (this.al - y) / 20;
        i(5);
        this.ag = 0;
        goto L28
    L7:
        if (this.aS != 4) goto L16;
    L15:
        this.al += 20;
        goto L16
    L10:
        if (this.aR >= 12) goto L16;
        if (this.aS >= 12) goto L15;
        if (this.aS != 5) goto L16;
        goto L15
    }

    private void av() {
        this.al -= 20;
        x();
        this.al += 20;
        if (this.aO < 20) goto L6;
        a(0);
    L6:
        if (this.aO >= 20) goto L8;
        return;
    L8:
        if (this.ah < 0) goto L10;
        return;
    L10:
        if (i.bq != 0) goto L27;
        t();
        int r0 = this.W[3];
        int r02 = this.W[0] - 1;
        int r03 = this.W[2] + 1;
        if (this.ag != 0) goto L14;
        return;
    L14:
        if (this.ag <= 0) goto L20;
        if (e(r03 / 20, r0 / 20) < 20) goto L29;
        this.ai = 0;
        this.ag = 0;
        this.ak -= 10;
        return;
    L29:
        return;
    L20:
        if (this.ag < 0) goto L22;
        return;
    L22:
        if (e(r02 / 20, r0 / 20) < 20) goto L31;
        this.ai = 0;
        this.ag = 0;
        this.ak += 10;
        return;
    L31:
        return;
    }

    public final void j() {
        if (this.bM.aG != 1) goto L5;
    L6:
        i(23);
        if (this.bM.aG != 4) goto L14;
        if (this.bM.Z[3] == 0) goto L11;
        boolean r1 = true;
    L12:
        this.av = r1;
        goto L14
    L11:
        r1 = false;
    L14:
        if (this.av == false) goto L16;
        int r12 = -2048;
    L17:
        this.ag = r12;
        this.ah = -2560;
    L26:
        if (this.bM.aG != 2) goto L28;
        k.aS.H();
    L28:
        this.bM.aA = 0;
        this.bM.bM = null;
        this.bM = null;
        this.aA &= -65;
        return;
    L16:
        r12 = 2048;
        goto L17
    L5:
        if (this.bM.aG == 4) goto L6;
        i(43);
        if (this.av == false) goto L21;
        int r13 = -2048;
    L22:
        this.ag = r13;
        this.ah = -3840;
        if (y() == false) goto L26;
        this.ag = 0;
        goto L26
    L21:
        r13 = 2048;
        goto L22
    }

    public final void k() {
        if (this.bM != null) goto L5;
        return;
    L5:
        if (this.bM != null) goto L7;
    L9:
        i r0 = this.bM;
        if (this.bM.aG != 4) goto L18;
        if ((r0.bN - 2) < 0) goto L15;
        r0.bN--;
        i(82);
        return;
    L15:
        j();
        return;
    L18:
        if (k.u(65568) == false) goto L22;
        i(86);
        return;
    L22:
        if (k.x(4112) == false) goto L26;
        this.av = true;
        return;
    L26:
        if (k.x(8256) == false) goto L30;
        this.av = false;
        return;
    L30:
        if (k.u(2) == false) goto L34;
        this.av = true;
        j();
        return;
    L34:
        if (k.u(8) == false) goto L38;
        this.av = false;
        j();
        return;
    L38:
        if (k.u(8256) == false) goto L57;
        if (r0.bP > 0) goto L51;
        if (r0.bO < 0) goto L51;
        if (r0.bP <= 0) goto L46;
    L48:
        r0.bP += 512;
    L49:
        r0.bO += (20480 + r0.bP) / 80;
        goto L51
    L46:
        if (r0.bP <= (-1280)) goto L48;
        r0.bP = -1280;
    L51:
        if (this.av == true) goto L53;
        int r1 = 84;
    L54:
        i(r1);
        return;
    L53:
        r1 = 85;
        goto L54
    L57:
        if (k.u(4112) == false) goto L76;
        if (r0.bP < 0) goto L70;
        if (r0.bO > 0) goto L70;
        if (r0.bP >= 0) goto L65;
    L67:
        r0.bP -= 512;
    L68:
        r0.bO -= (20480 - r0.bP) / 80;
        goto L70
    L65:
        if (r0.bP >= 1280) goto L67;
        r0.bP = 1280;
    L70:
        if (this.av == false) goto L72;
        int r12 = 84;
    L73:
        i(r12);
        return;
    L72:
        r12 = 85;
        goto L73
    L76:
        if (k.u(16388) == false) goto L103;
        if (r0.bO == 0) goto L80;
    L81:
        r0.bP = 0;
        r0.bO = 0;
        return;
    L80:
        if (r0.bP != 0) goto L81;
        if (this.S == 326) goto L92;
        if (this.S == 83) goto L92;
        if (this.S != 82) goto L98;
        if (this.T == 0) goto L92;
    L98:
        if (this.S != 82) goto L100;
        return;
    L100:
        if (r() == false) goto L128;
    L124:
        i(326);
        return;
    L128:
        return;
    L92:
        if ((r0.bN - 2) < 0) goto L95;
        r0.bN--;
        i(82);
        return;
    L95:
        i(326);
        return;
    L103:
        if (k.u(33024) == false) goto L119;
        if (r0.bO == 0) goto L107;
    L108:
        r0.bP = 0;
        r0.bO = 0;
        return;
    L107:
        if (r0.bO != 0) goto L108;
        if ((r0.bN + 2) > (r0.Z[1] - 2)) goto L113;
        r0.bN++;
        i(83);
        return;
    L113:
        i(43);
        this.ag = 0;
        this.ah = 2560;
        if (this.bM.aG != 2) goto L116;
        k.aS.H();
    L116:
        this.bM.aA = 0;
        this.bM.bM = null;
        this.aA &= -65;
        return;
    L119:
        if (this.S != 326) goto L121;
        return;
    L121:
        if (r() == true) goto L124;
        if (this.T == 0) goto L124;
        return;
    L7:
        if (this.bM.aG != 1) goto L9;
    }

    public final boolean l() {
        if (k.aT == false) goto L6;
        return true;
    L6:
        this.bM = null;
        this.aF = 0;
        cp = true;
        cq = true;
        z = true;
        if (this.S != 79) goto L55;
        if (k.u(4112) == false) goto L25;
        if (this.av == true) goto L13;
        this.av = true;
    L41:
        if (i.bn == false) goto L45;
        cq = false;
        z = false;
        return true;
    L45:
        if (k.u(16388) == true) goto L55;
        if (this.aZ == false) goto L49;
        return true;
    L49:
        if (a != null) goto L133;
        return false;
    L133:
        return true;
    L13:
        if (i.bn == false) goto L15;
        int r1 = 199;
    L16:
        i(r1);
        if (y() == false) goto L19;
    L20:
        int r12 = 0;
    L22:
        this.ag = r12;
        i.f(this);
        goto L41
    L19:
        if (this.S == 199) goto L20;
        r12 = -2560;
        goto L22
    L15:
        r1 = 32;
        goto L16
    L25:
        if (k.u(8256) == false) goto L41;
        if (this.av == false) goto L29;
        this.av = false;
        goto L41
    L29:
        if (i.bn == false) goto L31;
        int r13 = 199;
    L32:
        i(r13);
        if (y() == false) goto L35;
    L36:
        int r14 = 0;
    L38:
        this.ag = r14;
        i.f(this);
        goto L41
    L35:
        if (this.S == 199) goto L36;
        r14 = 2560;
        goto L38
    L31:
        r13 = 32;
    L55:
        if (g == null) goto L62;
        if (g.ax != 73) goto L62;
        if (g.Z[0] != 3) goto L62;
        cq = false;
    L62:
        if (k.u(4112) == true) goto L68;
        if (k.x(4112) == true) goto L68;
        if (k.u(128) == true) goto L68;
        if (k.u(8256) == true) goto L94;
        if (k.x(8256) == true) goto L94;
        if (k.u(512) == true) goto L94;
        if (k.u(33024) == false) goto L120;
        if (this.ag == 0) goto L132;
        this.ag = 0;
        i(32);
        return true;
    L132:
        return aw();
    L120:
        if (this.S != 12) goto L124;
        if (co < 4) goto L124;
    L126:
        if (this.S == 11) goto L128;
    L129:
        i(11);
        return true;
    L128:
        if (r() == true) goto L132;
    L124:
        if (this.S != 11) goto L132;
    L94:
        if (this.av == true) goto L98;
        return ax();
    L98:
        if (this.aA == 0) goto L100;
        this.av = false;
        goto L132
    L100:
        if (k.bD <= 4) goto L105;
        if (k.x(8256) == true) goto L105;
        this.av = false;
    L105:
        if (k.x(8256) == false) goto L132;
        i(10);
        this.ag = 4096;
        if (y() == false) goto L109;
        int r15 = 0;
    L110:
        this.ag = r15;
        return true;
    L109:
        r15 = this.ag;
    L68:
        if (this.av == false) goto L72;
        return ax();
    L72:
        if (this.aA == 0) goto L74;
        this.av = true;
        goto L132
    L74:
        if (k.bD <= 4) goto L79;
        if (k.x(4112) == true) goto L79;
        this.av = true;
    L79:
        if (k.x(4112) == false) goto L132;
        i(10);
        this.ag = -4096;
        if (y() == false) goto L83;
        int r16 = 0;
    L84:
        this.ag = r16;
        return true;
    L83:
        r16 = this.ag;
        goto L84
    }

    private boolean aw() {
        this.ag = 0;
        this.ah = 0;
        if (this.aZ == true) goto L7;
        if (a != null) goto L7;
        if (k.aS.af == null) goto L58;
        return true;
    L58:
        if (k.aS.S == 284) goto L73;
        a(0);
        cq = false;
        z = false;
        return true;
    L73:
        return true;
    L7:
        if (this.S != 79) goto L16;
        if (i.bn == true) goto L16;
        if (e(((this.W[0] + this.W[2]) / 2) / 20, (this.W[1] / 20) - 1) <= 12) goto L13;
        return true;
    L13:
        if (k.u(16388) == false) goto L63;
        i(80);
        k.v();
        return true;
    L63:
        return true;
    L16:
        if (this.S == 2) goto L37;
        if (k.u(33024) == false) goto L37;
        if (this.aS != 9) goto L22;
        a(122, 8);
        return true;
    L22:
        this.al += 20;
        x();
        this.al -= 20;
        if (this.av == false) goto L25;
        int r0 = (this.W[0] / 20) - 2;
    L26:
        int r6 = r0;
        int r02 = (this.W[3] / 20) + 1;
        if (this.aQ == 20) goto L31;
        if (this.aQ == 5) goto L31;
    L35:
        i(78);
        return true;
    L31:
        if (this.aR != 0) goto L35;
        if (e(r6, r02) >= 12) goto L35;
        a(257, 8);
        return true;
    L25:
        r0 = (this.W[2] / 20) + 2;
    L37:
        if (this.S == 6) goto L44;
        if (k.u(16388) == false) goto L44;
        if (f() == false) goto L44;
        i(6);
        return true;
    L44:
        if ((j.g % 100) != 0) goto L47;
        i(1);
        return true;
    L47:
        if (this.S != 1) goto L51;
        if (r() == true) goto L51;
        return true;
    L51:
        if (this.S != 81) goto L53;
        int r1 = 79;
    L54:
        i(r1);
        return true;
    L53:
        r1 = 0;
        goto L54
    }

    private boolean ax() {
        this.ah = 0;
        if (this.aZ == true) goto L7;
        if (a != null) goto L7;
        cq = false;
        z = false;
        return false;
    L7:
        if (this.aR <= 18) goto L26;
        if (this.av == false) goto L15;
        if (this.aV >= 12) goto L15;
        if (this.bb == true) goto L15;
    L20:
        i(26);
        if (this.av == false) goto L23;
        int r1 = -1280;
    L24:
        this.ag = r1;
    L45:
        if (this.aR != 14) goto L47;
    L48:
        this.ah = this.ag >> 1;
    L55:
        if (this.ah >= 0) goto L59;
        this.ah = 0;
    L59:
        i.f(this);
        return true;
    L47:
        if (this.aR == 15) goto L48;
        if (this.aR != 17) goto L52;
    L53:
        this.ah = (-this.ag) >> 1;
        goto L55
    L52:
        if (this.aR != 16) goto L55;
    L23:
        r1 = 1280;
    L15:
        if (this.av == true) goto L26;
        if (this.aW >= 12) goto L26;
        if (this.bc == false) goto L20;
    L26:
        if (this.S == 12) goto L28;
        co = 0;
    L28:
        i(12);
        if (this.av == false) goto L38;
        if (this.aT >= 18) goto L33;
    L35:
        int r12 = -2560;
    L36:
        this.ag = r12;
        goto L45
    L33:
        if (this.aT > 23) goto L35;
        r12 = -512;
        goto L36
    L38:
        if (this.aU >= 18) goto L40;
    L42:
        int r13 = 2560;
    L43:
        this.ag = r13;
        goto L45
    L40:
        if (this.aU > 23) goto L42;
        r13 = 512;
        goto L43
    }

    public static void g(int r3) {
        J |= r3;
        k.q();
    }

    public final boolean h(int r4) {
        if (r4 != 0) goto L5;
    L6:
        I = r4;
        if (k.at == 1) goto L10;
        k.at = 1;
    L10:
        if (i.at == null) goto L17;
        if (i.at.ab == null) goto L17;
        if (i.at.ab.ax != 16) goto L17;
        i.at.H();
    L17:
        if (r4 != 1) goto L19;
        return true;
    L19:
        if (this.S != 38) goto L36;
        I = 1;
        if (i.at != null) goto L23;
        return false;
    L23:
        if (i.at.ab != null) goto L25;
        return false;
    L25:
        if (i.at.ab.ax != 16) goto L35;
        i.at.H();
        return false;
    L35:
        return false;
    L36:
        return true;
    L5:
        if ((J & r4) != 0) goto L6;
        return false;
    }

    public final boolean m() {
        if (this.ax == 0) goto L5;
        return false;
    L5:
        if (a == null) goto L9;
        if (a.ax != 51) goto L9;
        return true;
    L9:
        if (i.bq == 0) goto L15;
        return true;
    L15:
        return false;
    }

    private void ay() {
        if (a != null) goto L5;
    L13:
        int r0 = e((this.W[2] / 20) + 1, this.W[3] / 20);
        int r02 = e((this.W[0] / 20) - 1, this.W[3] / 20);
        if (i.aN == null) goto L30;
        int r03 = e((this.W[2] / 20) + 2, this.W[3] / 20);
        int r04 = e((this.W[0] / 20) - 2, this.W[3] / 20);
        if (this.av == true) goto L23;
        if (r03 == 20) goto L23;
        if (r03 == 5) goto L23;
        boolean r05 = false;
    L44:
        if (r05 == true) goto L48;
        this.ai = 0;
        this.ag = 0;
        return;
    L48:
        switch(this.S) {
            case 67: goto L50;
            case 68: goto L60;
            case 69: goto L72;
            default: goto L82;
        };
    L50:
        if (this.T == 1) goto L52;
    L58:
        this.ag = 0;
        goto L82
    L52:
        if (k.aS.V > 0) goto L58;
        if (this.av == false) goto L56;
        int r1 = -1792;
    L57:
        this.ag = r1;
        goto L82
    L56:
        r1 = 1792;
        goto L57
    L60:
        if (this.T == 0) goto L64;
        if (this.T == 1) goto L64;
    L70:
        this.ag = 0;
    L64:
        if (k.aS.V > 0) goto L70;
        if (this.av == false) goto L68;
        int r12 = -1792;
    L69:
        this.ag = r12;
        goto L82
    L68:
        r12 = 1792;
        goto L69
    L72:
        if (this.T == 1) goto L74;
    L80:
        this.ag = 0;
        goto L82
    L74:
        if (k.aS.V > 0) goto L80;
        if (this.av == false) goto L78;
        int r13 = -1792;
    L79:
        this.ag = r13;
        goto L82
    L78:
        r13 = 1792;
    L82:
        if (i.aN == null) goto L97;
        i.aN.t();
        if (this.ak >= i.aN.ak) goto L91;
        if (this.ag < 0) goto L91;
        if ((((this.ak + (this.ag >> 8)) + (this.W[2] - this.ak)) + 2) < i.aN.W[0]) goto L97;
        this.ai = 0;
        this.ag = 0;
    L91:
        if (this.ak <= i.aN.ak) goto L97;
        if (this.ag > 0) goto L97;
        if ((((this.ak + (this.ag >> 8)) + (this.W[0] - this.ak)) - 2) > i.aN.W[2]) goto L97;
        this.ai = 0;
        this.ag = 0;
    L97:
        i.f(this);
        return;
    L23:
        if (this.av == false) goto L30;
        if (r04 == 20) goto L30;
        if (r04 == 5) goto L30;
        r05 = false;
    L30:
        if (this.av == true) goto L37;
        if (r0 == 20) goto L37;
        if (r0 == 5) goto L37;
        r05 = false;
    L37:
        if (this.av == true) goto L39;
    L43:
        r05 = true;
        goto L44
    L39:
        if (r02 == 20) goto L43;
        if (r02 == 5) goto L43;
        r05 = false;
        goto L44
    L5:
        if (this.av == false) goto L9;
        if (a.W[0] >= this.W[0]) goto L9;
    L12:
        r05 = true;
    L9:
        if (this.av == true) goto L13;
        if (a.W[2] <= this.W[2]) goto L13;
        goto L12
    }

    private static boolean k(int r2) {
        switch(r2) {
            case 0: goto L4;
            case 1: goto L4;
            case 18: goto L4;
            case 19: goto L4;
            case 20: goto L4;
            case 23: goto L4;
            case 24: goto L4;
            case 25: goto L4;
            case 35: goto L4;
            case 36: goto L4;
            case 43: goto L4;
            case 150: goto L4;
            case 157: goto L4;
            case 165: goto L4;
            case 242: goto L4;
            case 243: goto L4;
            case 263: goto L4;
            case 264: goto L4;
            case 265: goto L4;
            case 266: goto L4;
            case 358: goto L4;
            default: goto L6;
        };
    L4:
        return true;
    L6:
        return false;
    }

    private boolean i(i r4) {
        if ((J & 4) == 0) goto L21;
        if (r4.ax != 11) goto L21;
        if (r4.Z[19] != 1) goto L21;
        if (this.av == false) goto L13;
        if ((r4.ak - this.ak) >= 0) goto L13;
    L17:
        if (Math.abs(r4.ak - this.ak) > 200) goto L21;
        return true;
    L13:
        if (this.av == true) goto L21;
        if ((r4.ak - this.ak) > 0) goto L17;
    L21:
        if (this.S != 268) goto L23;
        return true;
    L23:
        if (this.S != 291) goto L25;
        return true;
    L25:
        if (k.aS.S != 267) goto L27;
        return true;
    L27:
        if (this.S != 303) goto L31;
        if (r() == false) goto L31;
        return true;
    L31:
        if (this.S != 295) goto L33;
        return true;
    L33:
        if (this.S != 357) goto L35;
        return true;
    L35:
        if (this.S != 358) goto L37;
        return true;
    L37:
        if (this.S >= 299) goto L39;
        return false;
    L39:
        if (this.S > 307) goto L51;
        return true;
    L51:
        return false;
    }

    private void az() {
        if ((this.aA & 8) == 0) goto L5;
    L6:
        boolean r0 = true;
    L8:
        if (r0 == false) goto L12;
        g = null;
        i.at = null;
        return;
    L12:
        if (g == null) goto L19;
        if (this.S != 270) goto L16;
        return;
    L16:
        if (this.S != 271) goto L19;
        return;
    L19:
        if (this.S != 268) goto L21;
    L24:
        g = null;
    L26:
        if (g == null) goto L37;
        if (g.ax == 4) goto L37;
        if (g.aB > 0) goto L32;
    L35:
        g = null;
        goto L37
    L32:
        if (k.h(this.ak - g.ak, this.al - g.al) > 440) goto L35;
        if (Math.abs(this.al - g.al) >= 60) goto L35;
    L37:
        if (ci == null) goto L50;
        if (ci.aB > 0) goto L41;
    L48:
        ci = null;
        goto L50
    L41:
        if (this.av == false) goto L48;
        if ((ci.ak - this.ak) >= 0) goto L48;
        if (this.av == true) goto L48;
        if ((ci.ak - this.ak) <= 0) goto L48;
    L50:
        if (g == null) goto L65;
        if (g.ax != 11) goto L65;
        if (g.Z[19] != 1) goto L65;
        if (this.av == true) goto L60;
        if ((g.ak - this.ak) >= 0) goto L60;
    L63:
        g = null;
    L60:
        if (this.av == false) goto L65;
        if ((g.ak - this.ak) > 0) goto L63;
    L65:
        if (i.at == null) goto L90;
        if (i.at.ax != 11) goto L71;
        if (i.at.P() == false) goto L71;
    L83:
        if (this.S == 277) goto L90;
        if (this.S == 293) goto L90;
        if (this.S == 298) goto L90;
        i.at = null;
    L71:
        if (k.h(this.ak - i.at.ak, this.al - i.at.al) > 440) goto L83;
        if ((this.ak - i.at.ak) >= 0) goto L77;
        if (this.av == true) goto L83;
    L77:
        if ((this.ak - i.at.ak) <= 0) goto L81;
        if (this.av == false) goto L83;
    L81:
        if (this.W[3] < i.at.W[1]) goto L83;
    L90:
        if (g == null) goto L109;
        if (g.ax != 11) goto L96;
        if (g.P() == false) goto L96;
    L107:
        g = null;
    L96:
        if (g.ax != 17) goto L100;
        if (g.P() == true) goto L107;
    L100:
        if (g.ax != 73) goto L104;
        if (g.P() == true) goto L107;
    L104:
        if (g.ax != 9) goto L109;
        if (g.P() == true) goto L107;
    L109:
        if (g == null) goto L124;
        if (this.S == 303) goto L115;
        if (this.S != 295) goto L124;
    L115:
        if (r() == false) goto L124;
        if (g.ax == 4) goto L119;
    L122:
        g = null;
        goto L124
    L119:
        if (g.S != 30) goto L122;
        if (g(g) == false) goto L122;
    L124:
        if (g == null) goto L131;
        if (g.ax != 4) goto L131;
        if (g.S == 30) goto L131;
        g = null;
    L131:
        if (g == null) goto L136;
        if (g.ax != 58) goto L136;
        g = null;
    L136:
        if (g == null) goto L144;
        if (i.at != null) goto L142;
        if ((J & 4) != 0) goto L144;
    L142:
        if (ci == null) goto L144;
        return;
    L144:
        if (k.bd == null) goto L349;
        int r6 = 440;
        int r7 = 0;
    L147:
        if (r7 >= k.be) goto L350;
        if (k.bd == null) goto L150;
        if (k.bd[r7] == null) goto L312;
        if ((k.bd[r7].P & 32) != 0) goto L312;
        if (k.bd[r7] == null) goto L182;
        if (k.bd[r7].ax != 11) goto L162;
        if (k.bd[r7].P() == true) goto L312;
    L162:
        if (k.bd[r7].ax != 17) goto L166;
        if (k.bd[r7].P() == true) goto L312;
    L166:
        if (k.bd[r7].ax != 73) goto L170;
        if (k.bd[r7].P() == true) goto L312;
    L170:
        if (k.bd[r7].ax != 23) goto L174;
        if (k.bd[r7].P() == true) goto L312;
    L174:
        if (k.bd[r7].ax != 9) goto L178;
        if (k.bd[r7].P() == true) goto L312;
    L178:
        if (k.bd[r7].ax != 4) goto L182;
        if (k.bd[r7].S != 30) goto L312;
    L182:
        if (k.bd[r7].ax == 11) goto L200;
        if (k.bd[r7].ax == 17) goto L200;
        if (k.bd[r7].ax == 23) goto L200;
        if (k.bd[r7].ax == 73) goto L200;
        if (k.bd[r7].ax == 29) goto L200;
        if (k.bd[r7].ax == 9) goto L200;
        if (k.bd[r7].ax != 4) goto L198;
        if (k.bd[r7].S == 30) goto L200;
    L198:
        if (k.bd[r7].ax == 58) goto L200;
    L279:
        if (k.bd[r7].ax != 72) goto L312;
        if ((J & 4) == 0) goto L312;
        if (k(this.S) == false) goto L312;
        if (k.bd[r7].v() == false) goto L312;
        if (k.bd[r7].Z[0] == 3) goto L312;
        if (this.av == false) goto L293;
        if ((k.bd[r7].ak - this.ak) >= 0) goto L293;
    L297:
        if (k.h(this.ak - k.bd[r7].ak, this.al - k.bd[r7].al) >= 440) goto L312;
        if (k.bd[r7].Z[0] != 1) goto L305;
        if (k.h(this.ak - k.bd[r7].ak, this.al - k.bd[r7].al) >= k.bd[r7].Z[3]) goto L312;
        if (this.W[1] < k.bd[r7].W[3]) goto L312;
    L305:
        if (this.W[3] < k.bd[r7].W[1]) goto L312;
        if (e(k.bd[r7]) == true) goto L312;
        if (i.at != null) goto L312;
        i.at = k.bd[r7];
        return;
    L293:
        if (this.av == true) goto L312;
        if ((k.bd[r7].ak - this.ak) <= 0) goto L312;
    L200:
        if (k.bd[r7].aB > 0) goto L206;
        if (k.bd[r7].ax == 4) goto L206;
        if (k.bd[r7].ax != 58) goto L312;
    L206:
        if (I == 8) goto L212;
        if (k.bd[r7].ax == 4) goto L312;
        if (k.bd[r7].ax == 58) goto L312;
    L212:
        if (this.av == false) goto L216;
        if ((k.bd[r7].ak - this.ak) >= 0) goto L216;
    L224:
        if (k.h(this.ak - k.bd[r7].ak, this.al - k.bd[r7].al) >= r6) goto L312;
        if (this.S != 268) goto L228;
    L231:
        g = k.bd[r7];
        goto L312
    L228:
        if (k.aS.S == 267) goto L231;
        if (this.S == 291) goto L231;
        if (e(k.bd[r7]) == true) goto L312;
        int r02 = r6;
        int r03 = k.h(this.ak - k.bd[r7].ak, this.al - k.bd[r7].al);
        r6 = r03;
        if (r03 >= r02) goto L238;
        g = null;
    L238:
        if (g == null) goto L242;
        if (ci != null) goto L279;
    L242:
        if (k.bd[r7].ax == 11) goto L250;
        if (k.bd[r7].ax == 17) goto L250;
        if (k.bd[r7].ax == 23) goto L250;
        if (k.bd[r7].ax == 73) goto L250;
    L256:
        if (i(k.bd[r7]) == true) goto L260;
        if (Math.abs(this.al - k.bd[r7].al) > 20) goto L312;
    L260:
        if (g != null) goto L263;
        g = k.bd[r7];
    L263:
        if (k.bd[r7].ax == 11) goto L275;
        if (k.bd[r7].ax == 17) goto L275;
        if (k.bd[r7].ax == 23) goto L275;
        if (k.bd[r7].ax == 73) goto L275;
        if (k.bd[r7].ax == 9) goto L275;
        if (k.bd[r7].ax == 29) goto L275;
    L277:
        r6 = k.h(this.ak - k.bd[r7].ak, this.al - k.bd[r7].al);
    L275:
        if (ci != null) goto L277;
        ci = k.bd[r7];
    L250:
        if (i(k.bd[r7]) == true) goto L256;
        if (k.bd[r7].aA == 0) goto L312;
        if (k.bd[r7].aA == 2) goto L312;
    L216:
        if (this.av == true) goto L220;
        if ((k.bd[r7].ak - this.ak) > 0) goto L224;
    L220:
        if (this.S == 268) goto L224;
        if (this.S == 291) goto L224;
    L312:
        r7 = r7 + 1;
        goto L147
    L150:
        return;
    L350:
        return;
    L349:
        return;
    L21:
        if (k.aS.S == 267) goto L24;
        if (this.S != 291) goto L26;
    L5:
        if (this.S == 250) goto L6;
        r0 = false;
        goto L8
    }

    private void aA() {
        if (k.u(16388) == false) goto L5;
    L12:
        i(92);
        a(8, 5, 17, 201);
        i r0 = i.aK;
        r0.av = this.av;
        i.aK.P = 512;
        i r02 = i.aK;
        r02.N = this.ak << 8;
        i r03 = i.aK;
        r03.O = this.al << 8;
        i r04 = i.aK;
        r04.ak = this.ak;
        i r05 = i.aK;
        r05.al = this.al;
        i r06 = i.aK;
        i r1 = i.aK;
        i r2 = i.aK;
        i.aK.aj = 0;
        r2.ai = 0;
        r1.ah = 0;
        r06.ag = 0;
        i.aK.t();
        k.b(i.aK);
        return;
    L5:
        if (this.av == false) goto L9;
        if (k.u(8264) == true) goto L12;
    L9:
        if (this.av == false) goto L11;
        return;
    L11:
        if (k.u(4114) == true) goto L12;
    }

    private void aB() {
        if (g != null) goto L5;
        return;
    L5:
        if (k.z[10] == null) goto L22;
        int r0 = k.z[10].a(41, this.K);
        if (r0 != 0) goto L10;
        return;
    L10:
        if (r0 <= this.cN) goto L13;
        this.cN++;
        return;
    L13:
        this.K++;
        this.cN = 0;
        int r6 = k.z[10].b(41);
        if (this.S != 295) goto L17;
        r6 = k.z[10].b(29);
    L17:
        if (this.K < r6) goto L19;
        this.K = 0;
    L19:
        this.L = (g.W[0] + g.W[2]) >> 1;
        this.M = ((g.W[1] + g.W[3]) >> 1) - 10;
        return;
    }

    private static void aC() {
        if (k.aw != 20) goto L6;
        k.aw = 0;
        return;
    }

    public final void n() {
        boolean r6 = true;
        boolean r7 = true;
        k.aI++;
        B();
        if (i.be == false) goto L11;
        k.X = 0;
        if (r() == false) goto L7;
    L8:
        k.l(12);
        return;
    L7:
        if (v() == false) goto L8;
        return;
    L11:
        if (i.bh <= 0) goto L14;
        i.bh--;
    L14:
        if (k.aH >= 0) goto L17;
        k.aG--;
    L17:
        if (k.aG > 0) goto L20;
        k.aG = 6;
        k.aE--;
    L20:
        if (k.aE >= 0) goto L23;
        k.aE = 0;
    L40:
        if (k.aE > 0) goto L48;
        if (k.aH >= 0) goto L48;
        if (this.S == 24) goto L46;
        i.bB = true;
        i.bC = false;
        i.bD = false;
        i.bF = 90;
        i.bE = 999;
        i.bG = -1;
        i(24);
    L46:
        x[1] = 0;
    L51:
        if (i.bB == false) goto L54;
        this.aq = -1;
        this.ar = -1;
    L54:
        switch(this.S) {
            case 0: goto L141;
            case 1: goto L130;
            case 2: goto L325;
            case 3: goto L137;
            case 4: goto L146;
            case 5: goto L146;
            case 6: goto L346;
            case 7: goto L346;
            case 8: goto L346;
            case 9: goto L130;
            case 10: goto L346;
            case 11: goto L346;
            case 12: goto L130;
            case 13: goto L346;
            case 14: goto L346;
            case 15: goto L346;
            case 16: goto L346;
            case 17: goto L146;
            case 18: goto L146;
            case 19: goto L346;
            case 20: goto L331;
            case 21: goto L55;
            case 22: goto L76;
            case 23: goto L97;
            case 24: goto L325;
            case 25: goto L118;
            case 26: goto L121;
            case 27: goto L130;
            case 28: goto L124;
            case 29: goto L127;
            case 30: goto L139;
            case 31: goto L139;
            case 32: goto L139;
            case 33: goto L139;
            default: goto L346;
        };
    L55:
        k.aw = 20;
        if (r() == false) goto L59;
        i.bB = true;
        i.bC = true;
        i.bD = true;
        i.bE = 999;
        i(22);
    L59:
        if (k.u(4112) == false) goto L68;
        if (this.ag <= (-2048)) goto L64;
        this.ag -= 768;
    L64:
        if (this.ag >= (-2048)) goto L66;
        this.ag = -2048;
    L66:
        r6 = false;
        this.av = false;
    L68:
        if (k.u(8256) == false) goto L346;
        if (this.ag >= 2048) goto L73;
        this.ag += 768;
    L73:
        if (this.ag <= 2048) goto L75;
        this.ag = 2048;
    L75:
        r6 = false;
        this.av = false;
        goto L346
    L76:
        k.aw = 20;
        if (r() == false) goto L80;
        i.bB = true;
        i.bC = false;
        i.bD = false;
        i.bE = 999;
        i.bG = 100;
        i(23);
    L80:
        if (k.u(4112) == false) goto L89;
        if (this.ag <= (-2048)) goto L85;
        this.ag -= 768;
    L85:
        if (this.ag >= (-2048)) goto L87;
        this.ag = -2048;
    L87:
        r6 = false;
        this.av = false;
    L89:
        if (k.u(8256) == false) goto L346;
        if (this.ag >= 2048) goto L94;
        this.ag += 768;
    L94:
        if (this.ag <= 2048) goto L96;
        this.ag = 2048;
    L96:
        r6 = false;
        this.av = false;
        goto L346
    L97:
        k.aw = 20;
        if (r() == false) goto L101;
        i.bB = true;
        i.bC = false;
        i.bD = false;
        i.bE = 999;
        i(25);
    L101:
        if (k.u(4112) == false) goto L110;
        if (this.ag <= (-2048)) goto L106;
        this.ag -= 768;
    L106:
        if (this.ag >= (-2048)) goto L108;
        this.ag = -2048;
    L108:
        r6 = false;
        this.av = false;
    L110:
        if (k.u(8256) == false) goto L346;
        if (this.ag >= 2048) goto L115;
        this.ag += 768;
    L115:
        if (this.ag <= 2048) goto L117;
        this.ag = 2048;
    L117:
        r6 = false;
        this.av = false;
        goto L346
    L118:
        k.aw = 20;
        if (r() == false) goto L346;
        i.bB = false;
        i.bG = -1;
        i(4);
        goto L346
    L121:
        k.aw = 20;
        if (r() == false) goto L346;
        this.T = this.aa.b(this.S) - 2;
        goto L346
    L124:
        k.aw = 20;
        if (r() == false) goto L346;
        i(29);
        goto L346
    L127:
        k.aw = 20;
        if (r() == false) goto L346;
        i(26);
        goto L346
    L130:
        k.aw = 20;
        if (r() == false) goto L346;
        if (i.bB == false) goto L135;
        i.bB = false;
        i.bG = -1;
        k.aS.az = 202;
    L135:
        aC();
        i(4);
    L139:
        this.av = false;
        goto L141
    L325:
        this.ag >>= 1;
        this.ah >>= 1;
        r6 = false;
        r7 = false;
        if (r() == false) goto L328;
    L329:
        k.l(12);
        goto L346
    L328:
        if (v() == true) goto L346;
    L137:
        if (r() == false) goto L139;
        i(4);
    L141:
        if (this.S != 0) goto L146;
        if (r() == false) goto L146;
        this.T = this.aa.b(this.S) - 2;
    L146:
        if (i.bi == true) goto L150;
        if (E == true) goto L150;
        aC();
        if (i.aH == false) goto L161;
        if (k.Q < 230) goto L161;
        this.ah = k.Y << 1;
        r7 = false;
    L161:
        if (i.bB == true) goto L166;
        if (k.v(1) == false) goto L166;
        k.A(28);
    L166:
        if (this.S != 18) goto L173;
        if (this.ak <= (k.O + 200)) goto L170;
        boolean r1 = true;
    L171:
        this.av = r1;
        i(20);
        i.bk = true;
    L176:
        if (this.S != 3) goto L178;
    L186:
        boolean r0 = false;
    L187:
        boolean r8 = r0;
        if (i.bk == true) goto L197;
        if (this.Q == 18) goto L197;
        if (k.aI < 10) goto L197;
        if (r8 == false) goto L197;
        k.aI = 0;
        e(false);
    L197:
        if (k.u(4112) == false) goto L212;
        if (this.ag <= (-2048)) goto L202;
        this.ag -= 768;
    L202:
        if (this.ag >= (-2048)) goto L204;
        this.ag = -2048;
    L204:
        r6 = false;
        if (r8 == true) goto L207;
    L210:
        this.av = false;
        goto L212
    L207:
        if (k.bD < 15) goto L209;
        i(30);
        goto L210
    L209:
        i(33);
    L212:
        if (k.u(8256) == false) goto L227;
        if (this.ag >= 2048) goto L217;
        this.ag += 768;
    L217:
        if (this.ag <= 2048) goto L219;
        this.ag = 2048;
    L219:
        r6 = false;
        if (r8 == true) goto L222;
    L225:
        this.av = false;
        goto L227
    L222:
        if (k.bD < 15) goto L224;
        i(31);
        goto L225
    L224:
        i(32);
    L227:
        if (k.u(16388) == false) goto L240;
        if (k.Q <= 117) goto L240;
        if (this.ah <= ((-2048) + k.Y)) goto L234;
        this.ah -= 768;
    L234:
        if (this.ah >= ((-2048) + k.Y)) goto L236;
        this.ah = (-2048) + k.Y;
    L236:
        r7 = false;
        this.av = false;
        if (r8 == false) goto L240;
        i(4);
    L240:
        if (k.u(33024) == false) goto L253;
        if (k.Q >= 230) goto L253;
        if (this.ah >= (2048 + k.Y)) goto L247;
        this.ah += 768;
    L247:
        if (this.ah <= (2048 + k.Y)) goto L249;
        this.ah = 2048 + k.Y;
    L249:
        r7 = false;
        this.av = false;
        if (r8 == false) goto L253;
        i(5);
    L253:
        if (k.bB != 0) goto L262;
        if (k.bC != 0) goto L262;
        if (r() == false) goto L262;
        if (r8 == false) goto L262;
        this.av = false;
        i(4);
    L262:
        if (this.aq == (-1)) goto L346;
        if (this.ar == (-1)) goto L346;
        this.ah = 0;
        this.ag = 0;
        r7 = false;
        r6 = false;
        if (this.aq >= this.ak) goto L276;
        if (this.bb == true) goto L276;
        this.ak -= 10;
        if (r8 == false) goto L285;
        int r02 = k.bD;
        k.bD = r02 + 1;
        if (r02 < 15) goto L274;
        i(30);
        goto L285
    L274:
        i(33);
    L285:
        this.ar += k.X;
        this.al += k.X;
        if (this.ar >= this.al) goto L291;
        this.al -= 10;
        if (r8 == false) goto L296;
        i(4);
    L296:
        if (this.aq >= this.ak) goto L300;
        if (this.aT < 10) goto L300;
    L303:
        this.aq = this.ak;
    L305:
        if (this.ar >= this.al) goto L309;
        if (k.Q > 117) goto L309;
    L312:
        this.ar = this.al;
    L314:
        if (Math.abs(this.aq - this.ak) > 10) goto L317;
        this.ak = this.aq;
    L317:
        if (Math.abs(this.ar - this.al) > 10) goto L320;
        this.al = this.ar;
    L320:
        if (this.ak != this.aq) goto L346;
        if (this.al != this.ar) goto L346;
        this.aq = -1;
        this.ar = -1;
    L309:
        if (this.ar <= this.al) goto L314;
        if (k.Q < 230) goto L314;
    L300:
        if (this.aq <= this.ak) goto L305;
        if (this.aU < 10) goto L305;
    L291:
        if (this.ar <= this.al) goto L296;
        this.al += 10;
        if (r8 == false) goto L296;
        i(5);
    L276:
        if (this.aq <= this.ak) goto L285;
        if (this.bc == true) goto L285;
        this.ak += 10;
        if (r8 == false) goto L285;
        int r03 = k.bD;
        k.bD = r03 + 1;
        if (r03 < 15) goto L284;
        i(31);
        goto L285
    L284:
        i(32);
        goto L285
    L178:
        if (this.S == 0) goto L186;
        if (this.S == 18) goto L186;
        if (this.S == 17) goto L186;
        if (this.S == 20) goto L186;
        r0 = true;
        goto L187
    L170:
        r1 = false;
        goto L171
    L173:
        if (this.S != 17) goto L176;
        i(4);
    L150:
        if (k.Q < 230) goto L346;
        r7 = false;
        if (i.aH == false) goto L154;
        this.ah = k.Y * i.aI;
        goto L346
    L154:
        this.ah = k.Y;
        goto L346
    L331:
        if (i.bk == false) goto L343;
        if (this.T != 5) goto L338;
        int r82 = 0;
    L336:
        if (r82 >= 5) goto L338;
        e(true);
        r82 = r82 + 1;
    L338:
        i.bB = true;
        i.bE = 999;
        if (this.T > 10) goto L341;
        i.bC = true;
        i.bD = true;
        goto L343
    L341:
        i.bC = false;
        i.bD = false;
        i.bG = 100;
    L343:
        if (r() == false) goto L346;
        i.bB = false;
        i.bG = -1;
        i.bk = false;
        i(4);
    L346:
        if (r6 == false) goto L355;
        if (this.ag <= 768) goto L351;
        this.ag -= 768;
        goto L355
    L351:
        if (this.ag >= (-768)) goto L353;
        this.ag += 768;
        goto L355
    L353:
        this.ag = 0;
    L355:
        if (r7 == false) goto L364;
        if (this.ah <= (768 + k.Y)) goto L360;
        this.ah -= 768;
        goto L364
    L360:
        if (this.ah >= ((-768) + k.Y)) goto L362;
        this.ah += 768;
        goto L364
    L362:
        this.ah = k.Y;
    L364:
        if (this.ad == null) goto L369;
        this.ad.av = this.av;
        this.ad.P = this.P;
        this.ad.ak = this.ak + 20;
        this.ad.S = this.S;
        this.ad.T = this.T;
        this.ad.al = this.al;
        this.ad.ag = this.ag;
        this.ad.ah = this.ah;
        this.ad.ai = this.ai;
        this.ad.aj = this.aj;
        return;
    L369:
        return;
    L48:
        if (g() == false) goto L51;
        i(2);
        goto L51
    L23:
        if (k.aE > 25) goto L40;
        if (k.aE >= 25) goto L40;
        if (k.aF != 0) goto L40;
        if (k.aH >= 0) goto L40;
        if (i.aJ == 0) goto L35;
        if (k.W == 0) goto L40;
    L35:
        if (k.W == 0) goto L37;
        i.aJ = k.W;
        k.W = 0;
    L38:
        k.X = i.aJ >> 1;
        goto L40
    L37:
        i.aJ = k.X;
        goto L38
    }

    private void e(boolean r8) {
        if (r8 == false) goto L5;
        a(24, 40, 7, this.az - 1);
    L6:
        i.aK.av = false;
        i.aK.ak = this.ak;
        i.aK.al = this.W[1] - 3;
        i.aK.am = this.ak;
        i.aK.ao = this.ak;
        i.aK.an = i.aK.al;
        if (r8 == false) goto L9;
        i.aK.ao = k.O + j.a(70, 330);
        i.aK.ap = k.P + j.a(110, 130);
        i.aK.ag = ((i.aK.ao - i.aK.am) << 8) / 10;
        i.aK.ah = (((i.aK.ap - i.aK.an) << 8) / 10) + k.Y;
        i.aK.aC = 10;
    L10:
        i.aK.t();
        i.aK.P |= 16;
        i.aK.af = this;
        i.aK.bR = false;
        k.b(i.aK);
        return;
    L9:
        i.aK.ap = i.aK.al - 100;
        i.aK.ag = 0;
        i.aK.ah = (-3840) + k.X;
        goto L10
    L5:
        a(24, 40, 6, this.az - 1);
        goto L6
    }

    final boolean o() {
        if (this.aZ == false) goto L5;
        return true;
    L5:
        if (a != null) goto L7;
        return false;
    L7:
        if (a.ax != 43) goto L9;
        return false;
    L9:
        if (a.ax != 51) goto L11;
        return true;
    L11:
        if (a.ax != 15) goto L13;
        return true;
    L13:
        if (a.ax != 43) goto L22;
        return true;
    L22:
        return false;
    }

    static {
        i = true;
        j = false;
        k = -1;
        p = 0;
        q = false;
        r = false;
        s = false;
        t = 0;
        u = new int[]{5, 10, 15};
        cj = new int[]{67, 68, 69, 112, 6, 5, 100, 100, 65568, 65568, 65568, 65568};
        ck = new int[]{112, 113, 114, 115, 9, 5, 5, 100, 65568, 65568, 65568, 65568};
        v = false;
        w = null;
        cm = false;
        F = null;
        G = new int[]{15, 10, 5};
        H = new int[]{15, 10, 5};
    }
}
