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
    public static int l;
    public static int m;
    public static int n;
    public static int o;
    private boolean cl;
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
    private int cH;
    private int cI;
    private int cJ;
    private int cK;
    private int cL;
    private int cM;
    public static int I;
    public static int J;
    public int K;
    private int cN;
    public int L;
    public int M;
    public static boolean i = true;
    public static boolean j = false;
    public static int k = -1;
    public static int p = 0;
    public static boolean q = false;
    public static boolean r = false;
    public static boolean s = false;
    public static int t = 0;
    static final int[] u = {5, 10, 15};
    private static int[] cj = {67, 68, 69, 112, 6, 5, 100, 100, 65568, 65568, 65568, 65568};
    private static int[] ck = {112, 113, 114, 115, 9, 5, 5, 100, 65568, 65568, 65568, 65568};
    public static boolean v = false;
    static int[] w = null;
    private static boolean cm = false;
    public static i F = null;
    public static final int[] G = {15, 10, 5};
    public static final int[] H = {15, 10, 5};

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

    g(short[] sArr) {
        super(sArr);
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

    public final void a(int i2) {
        a(43, 32);
        this.al += 10;
        this.ah = i2;
        this.aj = 1536;
        a = null;
        this.ac = null;
    }

    public static boolean a() {
        return a(g);
    }

    public static boolean a(i iVar) {
        if (i.bh != 0 || h()) {
            return false;
        }
        if (g()) {
            return true;
        }
        if (iVar == null || iVar.ax != 61) {
            d(u[k.au]);
            return true;
        }
        int iW = iVar.W();
        if (k.aS.S == 6) {
            iW /= 3;
        }
        d(iW);
        return true;
    }

    private void aj() {
        if (!a(cj, true) && a(ck, false)) {
        }
    }

    private boolean a(int[] iArr, boolean z2) {
        int length = iArr.length / 3;
        for (int i2 = 0; i2 < length - 1; i2++) {
            if (z2 && i2 >= length - 2) {
                return false;
            }
            if (this.R != -1 || (this.S == iArr[i2] && k.v(iArr[i2 + (length << 1)]))) {
                this.cl = false;
                if (this.T >= iArr[i2 + length]) {
                    this.cl = true;
                }
                if (this.R != -1 || this.cl) {
                    return true;
                }
                if (z2 && length >= 3) {
                    int i3 = length - 3;
                }
                this.R = iArr[i2 + 1];
                return true;
            }
        }
        return false;
    }

    private boolean ak() {
        int i2;
        int i3;
        int i4 = (this.W[1] + 10) / 20;
        if (this.av) {
            int i5 = (this.W[0] - 5) / 20;
            i2 = i5;
            i3 = i5 + 1;
        } else {
            int i6 = (this.W[2] + 5) / 20;
            i2 = i6;
            i3 = i6 - 1;
        }
        if (e(i2, i4) < 19 || this.Q == 61 || e(i2, i4 - 1) > 0 || e(i3, i4) > 0 || e(i3, i4 - 1) > 0 || e(i3, i4 + 1) > 0 || e(i3, i4 + 2) > 0) {
            return false;
        }
        this.ak = this.av ? (i2 * 20) + 20 : i2 * 20;
        this.al = (i4 * 20) - 1;
        i(60);
        return true;
    }

    private boolean al() {
        int i2;
        int i3;
        int i4 = (this.W[1] + 10) / 20;
        if (this.av) {
            int i5 = (this.W[0] - 20) / 20;
            i2 = i5;
            i3 = i5 + 1;
        } else {
            int i6 = ((this.W[2] + 20) / 20) + 1;
            i2 = i6;
            i3 = i6 - 1;
        }
        int iE = e(i2, i4);
        if (iE < 19 || e(i2, i4 - 1) > 0 || e(i3, i4) > 0 || e(i3, i4 - 1) > 0 || e(i3, i4 + 1) > 0 || e(i3, i4 + 2) > 0) {
            return false;
        }
        this.ak = this.av ? (i2 * 20) + 20 : i2 * 20;
        this.al = (i4 * 20) - 1;
        if (iE == 21) {
            return true;
        }
        k.v();
        i(61);
        this.aC = 40;
        return true;
    }

    private boolean am() {
        if (this.ag > 0 && this.aV == 19 && this.aR == 0) {
            this.av = true;
            this.ak = (this.ak / 20) * 20;
        } else if (this.ag < 0 && this.aW == 19 && this.aR == 0) {
            this.av = false;
            this.ak = ((this.ak / 20) * 20) + 20;
        } else {
            if (this.ag != 0 || this.aR != 19 || !k.u(33024)) {
                return false;
            }
            if (this.aV == 0) {
                this.av = false;
            }
            if (this.aW == 0) {
                this.av = true;
            }
            this.ak = ((this.ak / 20) * 20) + (this.av ? 20 : 0);
        }
        this.aj = 0;
        this.ah = 0;
        this.ag = 0;
        a(63, 16385);
        return true;
    }

    public static boolean b() {
        if (I != 1 && I != 2) {
            return false;
        }
        switch (k.aS.S) {
            case 67:
            case 68:
            case 69:
            case 81:
            case 112:
            case 113:
            case 114:
            case 115:
            case 183:
            case 184:
            case 216:
            case 217:
            case 286:
            case 287:
                return true;
            default:
                return false;
        }
    }

    public static boolean b(int i2) {
        switch (i2) {
            case 18:
            case 19:
            case 20:
            case 22:
            case 23:
            case 24:
            case 25:
            case 35:
            case 36:
            case 43:
            case 150:
            case 157:
            case 165:
            case 233:
            case 242:
            case 243:
            case 263:
            case 264:
            case 265:
            case 266:
                return true;
            default:
                return false;
        }
    }

    public static boolean c(int i2) {
        switch (i2) {
            case 0:
            case 1:
            case 7:
            case 11:
            case 12:
            case 26:
            case 79:
                return true;
            default:
                return false;
        }
    }

    public final boolean c() {
        return this.S == 112 || this.S == 113 || this.S == 114 || this.S == 115;
    }

    private boolean an() {
        if (this.S <= 43 || this.S == 150) {
            return true;
        }
        if ((this.S >= 67 && this.S <= 69) || this.S == 199 || this.S == 216 || this.S == 217 || this.S == 298) {
            return true;
        }
        return (this.S >= 259 && this.S <= 266) || this.S == 20 || this.S == 49 || this.S == 243;
    }

    /* JADX DEBUG: Possible override for method i.d()I */
    public final void d() {
        b(a);
    }

    /* JADX DEBUG: Possible override for method i.b(Li;)Z */
    public final void b(i iVar) {
        if (iVar == null || iVar.ax != 43) {
            return;
        }
        if (iVar.S == 1 || iVar.S == 4) {
            iVar.t();
            this.ak = (iVar.X[0] + iVar.X[2]) >> 1;
            this.al = (iVar.X[1] + iVar.X[3]) >> 1;
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Code restructure failed: missing block: B:1154:0x2298, code lost:
    
        r8.aF = 0;
        r8.aj = 0;
        r8.ai = 0;
        r8.ah = 0;
        r8.ag = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:1155:0x22b5, code lost:
    
        if (r8.av == false) goto L1157;
     */
    /* JADX WARN: Code restructure failed: missing block: B:1156:0x22b8, code lost:
    
        r8.ak = ((r8.W[0] / 20) * 20) + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:1157:0x22cd, code lost:
    
        r8.ak = (((r8.W[2] / 20) * 20) + 20) - 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:1158:0x22e2, code lost:
    
        a(8, 5, 17, 201);
        defpackage.i.aK.av = r8.av;
        defpackage.i.aK.N = r8.ak << 8;
        defpackage.i.aK.O = r8.al << 8;
        defpackage.i.aK.ak = r8.ak;
        defpackage.i.aK.al = r8.al;
        r0 = defpackage.i.aK;
        r1 = defpackage.i.aK;
        r2 = defpackage.i.aK;
        defpackage.i.aK.aj = 0;
        r2.ai = 0;
        r1.ah = 0;
        r0.ag = 0;
        defpackage.i.aK.t();
        defpackage.i.aK.P = 512;
        defpackage.k.b(defpackage.i.aK);
        i(101);
     */
    /* JADX WARN: Code restructure failed: missing block: B:982:0x1e98, code lost:
    
        if (r8.ba != false) goto L995;
     */
    /* JADX WARN: Code restructure failed: missing block: B:983:0x1e9b, code lost:
    
        r8.aF = 0;
        r8.aj = 0;
        r8.ai = 0;
        r8.ah = 0;
        r8.ag = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:984:0x1ebb, code lost:
    
        if (r8.S != 215) goto L990;
     */
    /* JADX WARN: Code restructure failed: missing block: B:986:0x1ec3, code lost:
    
        if (r8.av != false) goto L988;
     */
    /* JADX WARN: Code restructure failed: missing block: B:987:0x1ec6, code lost:
    
        r1 = true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:988:0x1eca, code lost:
    
        r1 = false;
     */
    /* JADX WARN: Code restructure failed: missing block: B:989:0x1ecb, code lost:
    
        r8.av = r1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:991:0x1ed2, code lost:
    
        if (r8.av == false) goto L993;
     */
    /* JADX WARN: Code restructure failed: missing block: B:992:0x1ed5, code lost:
    
        r8.ak = (((r8.W[0] + 20) / 20) * 20) + 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:993:0x1eed, code lost:
    
        r8.ak = ((((r8.W[2] - 20) / 20) * 20) + 20) - 1;
     */
    /* JADX WARN: Code restructure failed: missing block: B:994:0x1f05, code lost:
    
        a(8, 5, 17, 201);
        defpackage.i.aK.av = r8.av;
        defpackage.i.aK.N = r8.ak << 8;
        defpackage.i.aK.O = r8.al << 8;
        defpackage.i.aK.ak = r8.ak;
        defpackage.i.aK.al = r8.al;
        r0 = defpackage.i.aK;
        r1 = defpackage.i.aK;
        r2 = defpackage.i.aK;
        defpackage.i.aK.aj = 0;
        r2.ai = 0;
        r1.ah = 0;
        r0.ag = 0;
        defpackage.i.aK.t();
        defpackage.i.aK.P = 512;
        defpackage.k.b(defpackage.i.aK);
        i(101);
     */
    /* JADX WARN: Removed duplicated region for block: B:1001:0x1f9f  */
    /* JADX WARN: Removed duplicated region for block: B:1003:0x1fa7  */
    /* JADX WARN: Removed duplicated region for block: B:1004:0x1fad  */
    /* JADX WARN: Removed duplicated region for block: B:1006:0x1fb3  */
    /* JADX WARN: Removed duplicated region for block: B:1008:0x1fbe  */
    /* JADX WARN: Removed duplicated region for block: B:1032:0x2029  */
    /* JADX WARN: Removed duplicated region for block: B:1039:0x204f  */
    /* JADX WARN: Removed duplicated region for block: B:1219:0x24b1  */
    /* JADX WARN: Removed duplicated region for block: B:1230:0x24fd  */
    /* JADX WARN: Removed duplicated region for block: B:1252:0x2585  */
    /* JADX WARN: Removed duplicated region for block: B:1413:0x2973  */
    /* JADX WARN: Removed duplicated region for block: B:1583:0x2da1  */
    /* JADX WARN: Removed duplicated region for block: B:1585:0x2da8  */
    /* JADX WARN: Removed duplicated region for block: B:1588:0x2db4  */
    /* JADX WARN: Removed duplicated region for block: B:1597:0x2dd7  */
    /* JADX WARN: Removed duplicated region for block: B:1754:0x3183  */
    /* JADX WARN: Removed duplicated region for block: B:1927:0x3546  */
    /* JADX WARN: Removed duplicated region for block: B:1934:0x3566  */
    /* JADX WARN: Removed duplicated region for block: B:1938:0x3575  */
    /* JADX WARN: Removed duplicated region for block: B:1944:0x3599  */
    /* JADX WARN: Removed duplicated region for block: B:1946:0x35a2  */
    /* JADX WARN: Removed duplicated region for block: B:1949:0x35b5  */
    /* JADX WARN: Removed duplicated region for block: B:2005:0x3702  */
    /* JADX WARN: Removed duplicated region for block: B:2023:0x3751  */
    /* JADX WARN: Removed duplicated region for block: B:2025:0x3766  */
    /* JADX WARN: Removed duplicated region for block: B:2026:0x376d  */
    /* JADX WARN: Removed duplicated region for block: B:2030:0x3780  */
    /* JADX WARN: Removed duplicated region for block: B:2041:0x37f2  */
    /* JADX WARN: Removed duplicated region for block: B:2049:0x3814  */
    /* JADX WARN: Removed duplicated region for block: B:2052:0x3820  */
    /* JADX WARN: Removed duplicated region for block: B:2055:0x382e  */
    /* JADX WARN: Removed duplicated region for block: B:2056:0x383c  */
    /* JADX WARN: Removed duplicated region for block: B:2065:0x385b  */
    /* JADX WARN: Removed duplicated region for block: B:2067:0x3864  */
    /* JADX WARN: Removed duplicated region for block: B:2070:0x386e  */
    /* JADX WARN: Removed duplicated region for block: B:2072:0x388a  */
    /* JADX WARN: Removed duplicated region for block: B:433:0x115f  */
    /* JADX WARN: Removed duplicated region for block: B:434:0x116f  */
    /* JADX WARN: Removed duplicated region for block: B:440:0x119d  */
    /* JADX WARN: Removed duplicated region for block: B:442:0x11ae  */
    /* JADX WARN: Removed duplicated region for block: B:683:0x17d5  */
    /* JADX WARN: Removed duplicated region for block: B:686:0x17e7  */
    /* JADX WARN: Removed duplicated region for block: B:688:0x17f0  */
    /* JADX WARN: Removed duplicated region for block: B:707:0x1887  */
    /* JADX WARN: Removed duplicated region for block: B:713:0x18aa  */
    /* JADX WARN: Removed duplicated region for block: B:715:0x18b1  */
    /* JADX WARN: Removed duplicated region for block: B:718:0x18c7  */
    /* JADX WARN: Removed duplicated region for block: B:824:0x1b34  */
    /* JADX WARN: Removed duplicated region for block: B:892:0x1cfb  */
    /* JADX WARN: Removed duplicated region for block: B:895:0x1d0f  */
    /* JADX WARN: Removed duplicated region for block: B:900:0x1d29  */
    /* JADX WARN: Removed duplicated region for block: B:906:0x1d42  */
    /* JADX WARN: Removed duplicated region for block: B:912:0x1d5c  */
    /* JADX WARN: Removed duplicated region for block: B:917:0x1d73  */
    /* JADX WARN: Removed duplicated region for block: B:948:0x1dff  */
    /* JADX WARN: Removed duplicated region for block: B:958:0x1e37  */
    /* JADX WARN: Removed duplicated region for block: B:961:0x1e45  */
    /* JADX WARN: Removed duplicated region for block: B:974:0x1e78  */
    /* JADX WARN: Removed duplicated region for block: B:997:0x1f8d  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final void e() {
        boolean z2;
        g gVar;
        int i2;
        g gVar2;
        int i3;
        int i4;
        int i5;
        if (k.C == null || (k.aS.P & 512) != 0) {
            if (v) {
                this.ah = 0;
                this.ag = 0;
                if (k.v(4112) || k.u(4112)) {
                    this.ak -= 20;
                    return;
                }
                if (k.v(8256) || k.u(8256)) {
                    this.ak += 20;
                    return;
                }
                if (k.v(16388) || k.u(16388)) {
                    this.al -= 20;
                    return;
                } else {
                    if (k.v(33024) || k.u(33024)) {
                        this.al += 20;
                        return;
                    }
                    return;
                }
            }
            k.l();
            if (r) {
                return;
            }
            if (k.E != null) {
                k.E.J();
            }
            if ((this.aA & 256) != 0) {
                if (this.Z[1] >= 120) {
                    if (i.bn) {
                        b(k.aY[0].Z[4], 0, 0, -1, -1);
                    }
                    this.aA &= -257;
                    this.aA |= 16;
                }
                if (!i.bn || (k.aS.aA & 8) != 0) {
                    int[] iArr = this.Z;
                    iArr[1] = iArr[1] - 1;
                }
                if (this.Z[1] <= 0) {
                    this.Z[1] = 0;
                }
            }
            if ((this.aA & 16) != 0) {
                if (this.Z[0] <= 0) {
                    this.Z[0] = 3000;
                }
                int[] iArr2 = this.Z;
                iArr2[0] = iArr2[0] - j.f;
                if (this.Z[0] <= 0) {
                    this.aA &= -17;
                    this.aA |= 256;
                }
            }
            if ((i.bq != 0 && this.al > i.bq) || c(this.S)) {
                i.bq = 0;
            }
            if (i.bh > 0) {
                i.bh--;
            }
            if (g()) {
                i(50);
            }
            if (this.ah > 5120) {
                this.ah = 5120;
                cn++;
            } else {
                cn = 0;
            }
            if (k.aA > 0) {
                if (this.aA > 1) {
                    this.aA |= 1;
                }
            } else if (this.aA <= 1) {
                this.aA = 2;
            }
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
            if (k.am && this.S != 183 && this.S != 184 && this.S != 311) {
                k.p();
                i.O();
                if (i.aN != null) {
                    i.aN.aB = 0;
                    i.d(i.aN);
                    i.aN = null;
                }
            }
            if (i.aN != null && i.aN.P()) {
                i.aN = null;
            }
            az();
            if (this.S != 43) {
                i = true;
            }
            switch (this.S) {
                case 0:
                    i.O();
                    if (this.aO <= 12 || this.aR <= 12) {
                        if (this.S == 79) {
                            this.ag = 0;
                            this.ah = 0;
                            if (k.u(33024) || k.v(33024)) {
                                this.al += 20;
                                x();
                                this.al -= 20;
                                int i6 = this.av ? (this.W[0] / 20) - 2 : (this.W[2] / 20) + 2;
                                int i7 = (this.W[3] / 20) + 1;
                                if ((this.aQ == 20 || this.aQ == 5) && this.aR == 0 && e(i6, i7) < 12) {
                                    a(257, 8);
                                }
                            }
                        }
                        if (this.S == 11) {
                            this.ag = (this.ag << 1) / 3;
                        }
                        if (k.u(33024) || !am()) {
                            if (y()) {
                                this.ag = 1;
                                a(true);
                                this.ag = 0;
                            }
                            if (!l()) {
                                this.ab = null;
                                cq = false;
                                a(0);
                            }
                        } else {
                            this.ab = null;
                        }
                    } else {
                        i(79);
                    }
                    if (this.S != 9 && this.ab != null && this.ab.S == 14) {
                        this.ab = null;
                    }
                    if ((this.aR == 2 && this.aO != 2 && !L()) || a != null) {
                        if (this.aO != 6 || this.aR == 6) {
                            a(18, 0, 0, this);
                        }
                        z2 = false;
                        if ((J & 4) != 0 && this.S != 50) {
                            if (i.at != null || (g != null && (g == null || g(g)))) {
                                if (g != null && g.ax == 11 && g.Z[19] == 1 && !g.P() && (this.aZ || k(this.S))) {
                                    z2 = true;
                                    if (!k.v(65568) || (!k.k() && V())) {
                                        this.ag = 0;
                                        this.ah = 0;
                                        this.aj = 0;
                                        if (k.k()) {
                                            U();
                                        } else {
                                            G();
                                        }
                                        c(g);
                                        z = false;
                                    }
                                }
                            } else if ((this.aZ || k(this.S)) && i.at.v()) {
                                boolean z3 = true;
                                if (i.at.ax == 72 && i.at.Z[0] == 1) {
                                    if (k.h(i.at.ak - this.ak, i.at.al - this.W[1]) >= i.at.Z[3]) {
                                        z3 = false;
                                    }
                                } else if (i.at.ax == 72 && i.at.Z[0] == 4) {
                                    if (i.at.Z[4] < 0 || !this.aZ || Math.abs(this.ak - i.at.ak) < ((this.W[2] - this.W[0]) << 1)) {
                                        z3 = false;
                                    }
                                } else if (i.at.ax == 72 && i.at.Z[0] == 3) {
                                    z3 = false;
                                }
                                z2 = z3;
                                if (z3 && (k.v(65568) || (!k.k() && V()))) {
                                    this.ag = 0;
                                    this.ah = 0;
                                    this.aj = 0;
                                    if (i.at.Z[0] != 4) {
                                        if (k.k()) {
                                            G();
                                        } else {
                                            U();
                                        }
                                    }
                                    c(i.at);
                                    z = false;
                                    k.A(30);
                                }
                            }
                        }
                        if (!z2) {
                            if (k.k()) {
                                if (T()) {
                                    U();
                                }
                                a(8, this.ak, this.al - 85);
                                this.ae.ak = this.ak;
                                this.ae.al = this.al - 85;
                            } else {
                                if (!T()) {
                                    G();
                                }
                                c(200 + k.O, 120 + k.P);
                                d(200 + k.O, 120 + k.P);
                            }
                            cm = true;
                        } else if (cm) {
                            cm = false;
                            if (k.k()) {
                                G();
                            } else {
                                U();
                            }
                        }
                        if (o()) {
                            ao();
                        }
                        if (this.aA == 0) {
                            this.aA = 1;
                        }
                        if ((this.aA & 4) == 0) {
                            this.aA &= -5;
                        } else if (z && !i.bn && !E) {
                            ap();
                        }
                        if (this.aO != 7 || this.aO == 9) {
                            cq = false;
                        }
                        if (!A) {
                            E();
                            this.av = B;
                            this.ag = 0;
                            i(148);
                            A = false;
                            break;
                        } else {
                            if (f()) {
                                cq = false;
                            }
                            if (this.ac != null && (this.ac.ax == 51 || this.ac.ax == 66)) {
                                cq = false;
                            }
                            if (cq && !E) {
                                if (k.v(16398)) {
                                    if (k.v(16388) || k.v(2) || k.v(8)) {
                                        if (k.v(2)) {
                                            this.av = true;
                                        } else if (k.v(8)) {
                                            this.av = false;
                                        }
                                        this.ah = 0;
                                        if (this.S != 79 && this.S != 32 && this.S != 199) {
                                            if (a != null && a.ax == 66 && ((a.S == 11 || a.S == 12) && this.ac != null)) {
                                                if (this.av == (this.ac.ak < this.ak)) {
                                                }
                                            }
                                            if (this.aZ || a != null) {
                                                i(233);
                                                if (a != null && a.ax == 51) {
                                                    i.bq = this.al + 20;
                                                }
                                            } else if (k.v(16388)) {
                                                i(233);
                                            } else {
                                                i(22);
                                            }
                                        } else if (e(((this.W[0] + this.W[2]) / 2) / 20, (this.W[1] / 20) - 1) <= 12) {
                                            i(21);
                                        }
                                    }
                                    if (this.S != 233) {
                                        this.ag = 0;
                                    }
                                } else if (((this.av && k.x(8256)) || (!this.av && k.x(4112))) && k.aA > 0) {
                                    i(25);
                                    this.ag = 0;
                                    this.ah = 0;
                                }
                            }
                            if (cp && (((this.aO != 9 && this.aP != 9) || this.aQ != 9) && ct && (ak() || al()))) {
                                this.ag = 0;
                                this.ah = 0;
                                this.aj = 0;
                            }
                            if (cu && k.v(33024)) {
                                a(2560);
                                if (this.ab != null && this.ab.ax == 14) {
                                    H();
                                }
                            }
                            if (cv && (k.u(16388) || k.v(16388) || k.u(8) || k.v(8) || k.u(2) || k.v(2))) {
                                this.aF = 1;
                            }
                            if (cw && this.aO == 5) {
                                i(280);
                                this.aj = 0;
                                this.ai = 0;
                                this.ah = 0;
                                this.ag = 0;
                                this.al = ((this.W[1] / 20) * 20) + 10;
                                break;
                            }
                        }
                    } else {
                        this.ah = 0;
                        this.aj = 0;
                        e(0);
                        i(50);
                        break;
                    }
                    break;
                case 1:
                case 7:
                case 11:
                case 26:
                case 79:
                    if (this.aO <= 12) {
                        if (this.S == 79) {
                        }
                        if (this.S == 11) {
                        }
                        if (k.u(33024)) {
                            if (y()) {
                            }
                            if (!l()) {
                            }
                            break;
                        }
                        break;
                    }
                    if (this.S != 9) {
                        this.ab = null;
                        break;
                    }
                    if (this.aR == 2) {
                        this.ah = 0;
                        this.aj = 0;
                        e(0);
                        i(50);
                        break;
                    } else {
                        this.ah = 0;
                        this.aj = 0;
                        e(0);
                        i(50);
                    }
                    if (this.aO != 6) {
                        a(18, 0, 0, this);
                        break;
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                        if (i.at != null) {
                            if (g != null) {
                                z2 = true;
                                if (!k.v(65568)) {
                                    this.ag = 0;
                                    this.ah = 0;
                                    this.aj = 0;
                                    if (k.k()) {
                                    }
                                    c(g);
                                    z = false;
                                    break;
                                }
                                break;
                            }
                            break;
                        }
                        break;
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                        cq = false;
                        break;
                    }
                    if (!A) {
                    }
                    break;
                case 2:
                case 3:
                case 4:
                case 13:
                case 14:
                case 15:
                case 30:
                case 31:
                case 39:
                case 40:
                case 41:
                case 42:
                case 44:
                case 45:
                case 46:
                case 47:
                case 48:
                case 51:
                case 52:
                case 53:
                case 55:
                case 57:
                case 58:
                case 64:
                case 66:
                case 70:
                case 71:
                case 72:
                case 73:
                case 75:
                case 76:
                case 77:
                case 81:
                case 87:
                case 88:
                case 93:
                case 94:
                case 95:
                case 96:
                case 97:
                case 98:
                case 99:
                case 100:
                case 103:
                case 104:
                case 105:
                case 106:
                case 111:
                case 116:
                case 117:
                case 118:
                case 119:
                case 120:
                case 121:
                case 123:
                case 124:
                case 125:
                case 126:
                case 127:
                case 128:
                case 129:
                case 130:
                case 131:
                case 132:
                case 133:
                case 134:
                case 135:
                case 136:
                case 137:
                case 138:
                case 139:
                case 140:
                case 141:
                case 142:
                case 143:
                case 144:
                case 145:
                case 151:
                case 153:
                case 154:
                case 155:
                case 158:
                case 159:
                case 160:
                case 161:
                case 162:
                case 163:
                case 166:
                case 167:
                case 168:
                case 169:
                case 170:
                case 171:
                case 172:
                case 173:
                case 174:
                case 175:
                case 176:
                case 177:
                case 178:
                case 179:
                case 180:
                case 181:
                case 182:
                case 185:
                case 186:
                case 187:
                case 188:
                case 189:
                case 190:
                case 191:
                case 192:
                case 193:
                case 194:
                case 195:
                case 196:
                case 197:
                case 198:
                case 200:
                case 201:
                case 202:
                case 206:
                case 207:
                case 208:
                case 210:
                case 212:
                case 213:
                case 218:
                case 219:
                case 220:
                case 221:
                case 222:
                case 223:
                case 224:
                case 226:
                case 227:
                case 229:
                case 230:
                case 231:
                case 232:
                case 234:
                case 245:
                case 246:
                case 247:
                case 248:
                case 249:
                case 251:
                case 253:
                case 254:
                case 255:
                case 256:
                case 276:
                case 278:
                case 279:
                case 281:
                case 285:
                case 288:
                case 289:
                case 290:
                case 296:
                case 307:
                case 308:
                case 309:
                case 314:
                case 316:
                case 319:
                case 320:
                case 321:
                case 322:
                case 323:
                case 324:
                case 325:
                case 327:
                case 328:
                case 329:
                case 330:
                case 331:
                case 333:
                case 334:
                case 335:
                case 336:
                case 337:
                case 338:
                case 339:
                case 340:
                case 341:
                case 342:
                case 343:
                case 344:
                case 345:
                case 346:
                case 347:
                case 348:
                case 349:
                case 350:
                case 351:
                case 352:
                case 353:
                case 354:
                case 355:
                case 356:
                case 359:
                case 361:
                case 362:
                case 363:
                case 364:
                case 365:
                case 366:
                case 367:
                case 368:
                case 369:
                case 372:
                case 373:
                default:
                    if (r() && !j && !l() && !j) {
                        a(0);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 5:
                    i.O();
                    this.bM = null;
                    this.ag = 0;
                    this.ah = 0;
                    this.aj = 0;
                    if (k.v(16388) || k.v(2) || k.v(8)) {
                        if (k.v(2)) {
                            this.av = true;
                        } else if (k.v(8)) {
                            this.av = false;
                        }
                        i(21);
                        this.ab = null;
                    }
                    if (k.u(94324)) {
                        this.ab = null;
                        l();
                    } else if (r()) {
                        this.ab = null;
                        i(this.aO > 12 ? 79 : 0);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 6:
                    if (r()) {
                        this.ah = 0;
                        this.ag = 0;
                        this.aj = 0;
                        this.ai = 0;
                        this.P |= 64;
                    }
                    if (!k.u(16388)) {
                        i(0);
                        break;
                    }
                    break;
                case 8:
                    if (this.T == 1 && this.U == 0) {
                        k.A(11);
                    }
                    this.aj = 0;
                    this.ah = 0;
                    this.ag = 0;
                    this.ag = this.av ? 1280 : -1280;
                    if (r()) {
                        this.P |= 64;
                        if (!l()) {
                            aw();
                        }
                    }
                    i.f(this);
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 9:
                case 10:
                    this.ag /= 2;
                    this.ah = 0;
                    this.aj = 0;
                    if (this.ab != null) {
                        this.ab.av = this.av;
                        this.ab.ak = this.ak;
                        this.ab.al = this.al;
                    }
                    if ((this.av ? this.aU : this.aT) >= 19) {
                        this.ag = 0;
                    }
                    if (r()) {
                        this.bl = 0;
                        G();
                        H();
                        i(1);
                        a(false);
                        if (!M() && a == null) {
                            a(0);
                        }
                    }
                    if (this.Q == 0 || this.Q == 54) {
                        this.ah = 1;
                        a(true);
                        this.ah = 0;
                    }
                    i.f(this);
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 12:
                    D = false;
                    co++;
                    if (this.ag != 0 && this.aO == 0) {
                        int i8 = this.av ? this.aX : this.aY;
                        int i9 = this.av ? this.aT : this.aU;
                        if (A()) {
                            this.ai = 0;
                            this.ag = 0;
                            this.al = this.W[1];
                            this.ak += this.av ? -20 : 20;
                            i(74);
                            break;
                        } else if (i9 >= 19 && i9 < 24) {
                            if (i8 == 1) {
                                this.ag = 0;
                                this.ah = 0;
                                i(107);
                            } else if (i8 == 2) {
                                this.ag = 0;
                                this.ah = 0;
                                i(108);
                            } else if (i8 == 3) {
                                this.ag = 0;
                                this.ah = 0;
                                i(109);
                            } else if (co > 2 && z()) {
                                if (ak()) {
                                    this.aj = 0;
                                    this.ah = 0;
                                    this.ag = 0;
                                } else {
                                    i(33);
                                    this.ag = 0;
                                    this.ah = -4096;
                                }
                            }
                            if (this.S != 9) {
                            }
                            if (this.aR == 2) {
                            }
                            if (this.aO != 6) {
                            }
                            z2 = false;
                            if ((J & 4) != 0) {
                            }
                            if (!z2) {
                            }
                            if (o()) {
                            }
                            if (this.aA == 0) {
                            }
                            if ((this.aA & 4) == 0) {
                            }
                            if (this.aO != 7) {
                            }
                            if (!A) {
                            }
                        }
                    }
                    i.O();
                    if (this.aO <= 12) {
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 16:
                case 35:
                case 43:
                case 150:
                case 252:
                    i.f(this);
                    cv = true;
                    cp = true;
                    ct = true;
                    cw = true;
                    if (this.S == 43 && y()) {
                        this.ai = 0;
                        this.ag = 0;
                    }
                    if (I == 4) {
                        z = true;
                    }
                    if (this.S == 43 && r()) {
                        this.P |= 64;
                    }
                    if (this.aQ == 3) {
                        if ((c != null && c.ax == 51 && this.al > c.W[3]) || ((i.bq > 0 && this.al > i.bq && c == null) || (c == null && i.bq == 0))) {
                            i(147);
                            i.bq = 0;
                        }
                    } else if ((this.aR >= 12 && this.aQ >= 12 && this.aQ != 23) || this.aR >= 12 || this.aS >= 12 || this.aR == 5 || this.aS == 5 || this.aR == 4 || this.aS == 4) {
                        d(this.aR == 4 || this.aS == 4);
                    } else {
                        this.aj = 1536;
                        if (k != -1 && ((o != 0 && this.al < o) || o == 0)) {
                            if (k == 0) {
                                a(29, 32);
                            } else if (k == 1) {
                                this.ak = d.W[0];
                                t();
                                i(315);
                                if (d != null) {
                                    this.av = d.av;
                                    if (this.av) {
                                        this.ak = d.W[2];
                                    } else {
                                        this.ak = d.W[0];
                                    }
                                }
                            }
                            this.ag = 0;
                            if (n != 0 && k == 0) {
                                this.ak = n;
                            }
                            this.ah = 1536;
                            this.aj = 0;
                        } else if (y() && (k.u(2) || k.v(2) || k.u(8) || k.v(8) || k.u(16388) || k.v(16388))) {
                            if (cv && this.aF != 0 && !this.ba) {
                                if (!this.av) {
                                    break;
                                } else {
                                    break;
                                }
                            }
                            if (this.ag != 0) {
                                this.ag = this.ag > 0 ? 512 : -512;
                            }
                        }
                        if (this.S != 43 && this.S != 150 && this.S != 35 && this.S != 29 && this.S != 252 && r()) {
                            i(35);
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 17:
                    this.ah = 0;
                    this.ag = 0;
                    if (r()) {
                        i(18);
                        this.ag = this.av ? -2048 : 2048;
                        this.ah = -5120;
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 18:
                case 19:
                case 23:
                case 36:
                    cv = true;
                    cp = true;
                    ct = true;
                    cw = true;
                    if (I == 4) {
                        z = true;
                    }
                    this.aj = 1536;
                    if (this.S == 22 && r()) {
                        this.P |= 64;
                    }
                    if ((this.T != 1 && this.U == 0 && this.S != 19 && this.S != 23) || (this.S == 215 && this.ah == 0)) {
                        if (this.S == 20) {
                            this.ag = this.av ? 2048 : -2048;
                        } else if (this.S == 25) {
                            this.ag = this.av ? 1024 : -1024;
                        } else if (this.S == 36) {
                            this.ag = this.av ? -2048 : 2048;
                        } else {
                            if (this.S == 24) {
                                gVar2 = this;
                                i3 = 0;
                            } else if (this.S == 215) {
                                this.ag = this.av ? 2048 : -2048;
                            } else {
                                gVar2 = this;
                                i3 = this.av ? -2048 : 2048;
                            }
                            gVar2.ag = i3;
                        }
                        if (this.S == 20 || this.S == 36) {
                            this.ah = -5120;
                        } else if (this.S == 215) {
                            this.ah = -6656;
                        } else if (a != null && a.ax == 51) {
                            this.ah = -2560;
                        }
                        if (this.S != 9) {
                        }
                        if (this.aR == 2) {
                        }
                        if (this.aO != 6) {
                        }
                        z2 = false;
                        if ((J & 4) != 0) {
                        }
                        if (!z2) {
                        }
                        if (o()) {
                        }
                        if (this.aA == 0) {
                        }
                        if ((this.aA & 4) == 0) {
                        }
                        if (this.aO != 7) {
                        }
                        if (!A) {
                        }
                    }
                    if (!y()) {
                        av();
                        if (this.ah > 0) {
                            if ((this.aR >= 12 && this.aQ >= 12 && this.aQ != 23) || this.aR >= 12 || this.aS >= 12 || this.aR == 5 || this.aS == 5) {
                                d(false);
                            } else if (this.S == 215) {
                                a(0);
                                this.av = !this.av;
                            }
                        }
                        if (r() && this.S != 215 && this.S != 22) {
                            a(0);
                            a(true);
                        }
                        if (this.S == 22 && this.ah < 0 && this.ah + this.aj >= 0) {
                            i(23);
                        }
                        i.f(this);
                    } else if (cv && this.aF != 0) {
                        if (!this.av) {
                            if (this.S == 215) {
                            }
                            if (this.S == 25) {
                                this.ag = this.ag <= 0 ? 512 : -512;
                                av();
                                if (this.ah > 0) {
                                }
                                if (r()) {
                                }
                                if (this.S == 22) {
                                }
                                i.f(this);
                            }
                            break;
                        } else {
                            if (this.S == 215) {
                            }
                            if (this.S == 25) {
                            }
                            break;
                        }
                    } else {
                        if (this.S == 215) {
                            if (!this.av) {
                                break;
                            } else {
                                break;
                            }
                        }
                        if (this.S == 25 || this.S == 15 || this.S == 19) {
                            this.ag = this.ag <= 0 ? 512 : -512;
                        }
                        av();
                        if (this.ah > 0) {
                        }
                        if (r()) {
                            a(0);
                            a(true);
                        }
                        if (this.S == 22) {
                            i(23);
                        }
                        i.f(this);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 20:
                case 24:
                case 25:
                case 157:
                case 215:
                    cp = true;
                    ct = true;
                    cw = true;
                    if (I == 4) {
                    }
                    this.aj = 1536;
                    if (this.S == 22) {
                        this.P |= 64;
                        break;
                    }
                    if (this.T != 1) {
                        if (this.S == 20) {
                        }
                        if (this.S == 20) {
                        }
                        break;
                    } else {
                        if (this.S == 20) {
                        }
                        if (this.S == 20) {
                            this.ah = -5120;
                            if (!y()) {
                            }
                            break;
                        }
                        break;
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 21:
                case 233:
                    this.ah = 0;
                    this.ag = 0;
                    if (r() || this.S == 233) {
                        i(22);
                        if (a == null || a.ax != 51) {
                            this.ah = -5120;
                        } else {
                            this.ah = -2560;
                        }
                        if (a != null && a.ax == 43 && a.ab()) {
                            this.ag = this.av ? -1024 : 1024;
                        } else {
                            this.ag = this.av ? -2048 : 2048;
                        }
                    }
                    i.f(this);
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 22:
                    if (e != null) {
                        k.aS.aA &= -9;
                        k.aS.az = 100;
                        e = null;
                    }
                    cv = true;
                    cp = true;
                    ct = true;
                    cw = true;
                    if (I == 4) {
                    }
                    this.aj = 1536;
                    if (this.S == 22) {
                    }
                    if (this.T != 1) {
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 27:
                    if (this.ac == null) {
                        a(0);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 28:
                case 318:
                    this.aj = 0;
                    this.ai = 0;
                    this.ah = 0;
                    this.ag = 0;
                    if (k.v(33024)) {
                        a(this.ah);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 29:
                case 315:
                    if (o != 0) {
                        if (this.al > o) {
                            if (this.S == 315) {
                                i(318);
                            } else {
                                i(28);
                            }
                        }
                    } else if (k == -1) {
                        a(this.ah);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 32:
                    i.f(this);
                    if (this.aZ || a != null) {
                        if (this.ag != 0) {
                            a(true);
                        }
                        if (y() && this.ag != 0) {
                            this.ag = 0;
                        }
                        if (r()) {
                            this.ag = 0;
                            i(this.Q == 79 ? 79 : 0);
                            if (this.S == 0) {
                                x();
                                if (this.aO > 12) {
                                    i(79);
                                }
                            }
                        }
                    } else {
                        cq = false;
                        a(0);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 33:
                    cp = true;
                    this.aj = 512;
                    if (A()) {
                        this.aj = 0;
                        this.ah = 0;
                        this.ai = 0;
                        this.ag = 0;
                        this.al = this.W[1];
                        this.ak = this.av ? this.W[0] - 10 : this.W[2] + 10;
                        i(74);
                        break;
                    } else {
                        if (!this.av ? k.u(8256) : k.u(4112)) {
                            x();
                            if (this.aR == 5 || this.aR == 20 || this.aS == 5 || this.aS == 20) {
                                this.ah = 0;
                                this.ag = 0;
                                a(43, 32);
                            } else {
                                ct = false;
                                this.ag = 0;
                                this.ah = 1536;
                                a(34, 36);
                                aA();
                            }
                        } else if (this.ah < 0) {
                            ct = true;
                            if (r()) {
                                boolean z4 = false;
                                int i10 = (this.W[1] + 10) / 20;
                                if (this.av) {
                                    int i11 = (this.W[0] - 5) / 20;
                                    i4 = i11;
                                    i5 = i11 + 1;
                                } else {
                                    int i12 = (this.W[2] + 5) / 20;
                                    i4 = i12;
                                    i5 = i12 - 1;
                                }
                                int i13 = 0;
                                while (true) {
                                    if (i13 < 2) {
                                        i10 -= i13;
                                        if (e(i4, i10) < 19 || e(i4, i10 - 1) > 0 || e(i5, i10) > 0 || e(i5, i10 - 1) > 0) {
                                            i13++;
                                        } else {
                                            z4 = true;
                                        }
                                    }
                                }
                                if (z4 && ak()) {
                                    this.aj = 0;
                                    this.ah = 0;
                                    this.ag = 0;
                                }
                            }
                        }
                        if (this.S != 9) {
                        }
                        if (this.aR == 2) {
                        }
                        if (this.aO != 6) {
                        }
                        z2 = false;
                        if ((J & 4) != 0) {
                        }
                        if (!z2) {
                        }
                        if (o()) {
                        }
                        if (this.aA == 0) {
                        }
                        if ((this.aA & 4) == 0) {
                        }
                        if (this.aO != 7) {
                        }
                        if (!A) {
                        }
                    }
                    break;
                case 34:
                    aA();
                    if ((this.av ? this.aT : this.aU) != 20) {
                        a(0);
                    }
                    if (this.aR >= 19 || this.aR == 5) {
                        l();
                        k.v();
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 37:
                    if (this.ac != null && this.ac.ax == 10 && this.ac.S == 32) {
                        this.ag = 0;
                        if (this.av) {
                            if (this.av) {
                                if (r()) {
                                }
                            } else if (r()) {
                            }
                        } else if (this.av) {
                        }
                    } else if (this.aO != 5) {
                        this.al = this.W[3];
                        i(43);
                    } else {
                        this.al = ((this.W[1] / 20) * 20) + 10;
                        this.ag = 0;
                        if (this.av ? !k.u(8256) : !k.u(4112)) {
                            if (this.av ? k.u(4112) : k.v(8256)) {
                                this.av = !this.av;
                            } else if (r()) {
                                i(38);
                            }
                        } else if (c(this.av)) {
                            i(37);
                            this.ag = this.av ? -1536 : 1536;
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 38:
                    this.ag = 0;
                    if (this.ac != null && this.ac.ax == 10 && this.ac.S == 32) {
                        if (k.u(16388) || (!this.av ? k.u(8) : k.u(2))) {
                            G();
                            this.al -= 20;
                            k.aS.i(23);
                            k.aS.ah = 2560;
                            if (k.u(2) || k.u(8)) {
                                if (this.av) {
                                    k.aS.ag = -4096;
                                } else {
                                    k.aS.ag = 4096;
                                }
                            }
                            cq = true;
                        }
                    } else if (this.aO != 5) {
                        this.al = this.W[3];
                        i(43);
                        if (this.S != 9) {
                        }
                        if (this.aR == 2) {
                        }
                        if (this.aO != 6) {
                        }
                        z2 = false;
                        if ((J & 4) != 0) {
                        }
                        if (!z2) {
                        }
                        if (o()) {
                        }
                        if (this.aA == 0) {
                        }
                        if ((this.aA & 4) == 0) {
                        }
                        if (this.aO != 7) {
                        }
                        if (!A) {
                        }
                    } else {
                        this.al = ((this.W[1] / 20) * 20) + 10;
                        if (k.u(16388)) {
                            G();
                            this.al -= 20;
                            x();
                            this.al += 20;
                            if (this.aO == 0) {
                                a(54, 8);
                            }
                        }
                    }
                    if (k.u(33024)) {
                        this.al += 20;
                        x();
                        this.al -= 20;
                        if (this.aO == 0) {
                            this.al = this.W[3] + 10;
                            i(43);
                        }
                    } else if (!this.av ? !k.u(8256) : !k.u(4112)) {
                        i(37);
                    } else if (!this.av ? k.u(4112) : k.v(8256)) {
                        this.av = !this.av;
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 49:
                    if (this.T > 9) {
                        this.ah = 0;
                        this.ag = 0;
                    }
                    if (r()) {
                        i(0);
                        E();
                        break;
                    }
                    break;
                case 50:
                case 241:
                    if (this.T == 1 && this.U == 0) {
                        d(999);
                        this.ab = null;
                        k.A(18);
                    }
                    this.ag >>= 2;
                    this.ah = 0;
                    this.aj = 0;
                    if (a != null) {
                        if (a.ax == 51) {
                            this.ag += a.ag;
                        } else if (a.ax == 43) {
                            this.al = a.al;
                        }
                        a = null;
                    }
                    if (r()) {
                        k.l(12);
                        break;
                    }
                    break;
                case 54:
                    if (r()) {
                        this.al -= 20;
                        E();
                        i(0);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 56:
                    this.ah = 0;
                    this.ag = 0;
                    if (r()) {
                        if (k.u(127999)) {
                            i(65);
                        } else {
                            i(59);
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 59:
                case 65:
                case 82:
                case 83:
                case 84:
                case 85:
                case 164:
                case 211:
                case 297:
                case 326:
                case 371:
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 60:
                    if (this.Q != 63 || k.u(16388) || ((this.av && k.u(4114)) || (!this.av && k.u(8264)))) {
                        i(62);
                    }
                    cu = true;
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 61:
                case 203:
                    this.ag = 0;
                    this.ah = 0;
                    if (this.S != 203) {
                        int i14 = this.aC - 1;
                        this.aC = i14;
                        if (i14 != 0) {
                            if (e((this.ak + (this.av ? -10 : 10)) / 20, (this.al + 10) / 20) >= 12 || a != null) {
                                if (k.v(33024)) {
                                }
                                if (this.S == 203) {
                                    if (h == null || h.P()) {
                                        G();
                                    } else {
                                        k.c(this.ak, this.al - 85, h.aw);
                                        if (k.v(65568)) {
                                            G();
                                            i(204);
                                            h.ak = this.av ? this.ak + 10 : this.ak - 10;
                                            h = null;
                                        }
                                    }
                                }
                                if (!k.v(16388) || ((this.av && k.v(4114)) || (!this.av && k.v(8264)))) {
                                    H();
                                    G();
                                    i(62);
                                }
                            }
                        }
                        if (a == null || (a != null && a.ax != 43)) {
                            H();
                            G();
                            this.al += this.W[3] - this.W[1];
                            a(0);
                        }
                        if (this.S == 203) {
                        }
                        if (!k.v(16388)) {
                            H();
                            G();
                            i(62);
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 62:
                    if (r()) {
                        this.ak += this.av ? -10 : 10;
                        a(this.aO > 12 ? 79 : 0, 9);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 63:
                    if (r()) {
                        i(60);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 67:
                case 68:
                case 69:
                case 112:
                case 113:
                case 114:
                case 115:
                    ay();
                    if (this.ag != 0 && y()) {
                        this.ag = 0;
                    }
                    if (!this.aZ && a == null) {
                        a(0);
                        break;
                    } else {
                        z = false;
                        if (k.v(65568) && ((this.S == 67 || this.S == 68) && a == null && i.aN != null && i.aN.ax == 11 && i.aN.Z[0] == 2 && i.aN.aB <= i.bw[k.au])) {
                            this.cl = false;
                            if (this.R == -1) {
                                this.R = Math.abs(j.j.nextInt()) % 2;
                                switch (this.R) {
                                    case 0:
                                        this.R = 183;
                                        break;
                                    case 1:
                                        this.R = 184;
                                        break;
                                }
                            }
                        } else {
                            aj();
                            if (k.E != null) {
                                k.E.K();
                            }
                        }
                        if (this.cl || r()) {
                            if (this.R == 112 && a != null && a.ax == 51) {
                                if (r()) {
                                    this.R = -1;
                                }
                            }
                            this.cl = false;
                            if ((i.aN != null && i.aN.aB > 0 && this.R == 183) || this.R == 184 || this.R == 205) {
                                k.o();
                                this.ag = 0;
                                this.ah = 0;
                            } else if (i.aN != null && i.aN.aB <= 0) {
                                this.R = -1;
                            }
                            if (this.R != -1) {
                                i(this.R);
                                this.R = -1;
                            } else {
                                l();
                            }
                            if (this.T == 2) {
                                k.A(10);
                            }
                        } else if (this.T == 2) {
                        }
                        if (this.S != 9) {
                        }
                        if (this.aR == 2) {
                        }
                        if (this.aO != 6) {
                        }
                        z2 = false;
                        if ((J & 4) != 0) {
                        }
                        if (!z2) {
                        }
                        if (o()) {
                        }
                        if (this.aA == 0) {
                        }
                        if ((this.aA & 4) == 0) {
                        }
                        if (this.aO != 7) {
                        }
                        if (!A) {
                        }
                    }
                    break;
                case 74:
                    if (r()) {
                        this.ak += this.av ? -40 : 40;
                        a(0);
                        break;
                    }
                    break;
                case 78:
                case 80:
                    if (r()) {
                        if (this.S == 78) {
                            i(79);
                        } else {
                            i(1);
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 86:
                    if (r()) {
                        i(326);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 89:
                    h(1);
                    this.ah = 0;
                    this.ag = 0;
                    this.aj = 0;
                    this.ai = 0;
                    if (r()) {
                        this.P |= 64;
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 90:
                    this.ae = null;
                    this.ah = 0;
                    G();
                    k.v();
                    D = true;
                    if (p != 0) {
                        if (p == 1) {
                            this.ag = 4096;
                            this.av = false;
                        } else if (p == 2) {
                            this.ag = -4096;
                            this.av = true;
                        }
                        this.ah = -768;
                        i(157);
                        p = 0;
                    } else if (r()) {
                        if (this.aR >= 20 || this.aS >= 20) {
                            i(0);
                            E();
                        } else {
                            a(0);
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 91:
                    this.ah = 0;
                    this.ag = 0;
                    this.aj = 0;
                    this.ai = 0;
                    if (r()) {
                        i(0);
                        break;
                    }
                    break;
                case 92:
                case 101:
                    this.aj = 0;
                    this.ai = 0;
                    this.ah = 0;
                    this.ag = 0;
                    if (r()) {
                        this.av = !this.av;
                        this.ag = this.av ? -2048 : 2048;
                        this.ah = this.aO == 20 ? 0 : -5120;
                        if (this.ah == 0) {
                            a(0);
                        } else {
                            a(36, 36);
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 102:
                    if (this.aZ) {
                        l();
                    } else {
                        int i15 = this.aC;
                        this.aC = i15 - 1;
                        if (i15 <= 0 || this.aR != 4) {
                            a(0);
                            this.al += 21;
                        } else if (k.u(4112)) {
                            this.av = true;
                            i(332);
                        } else if (k.u(8256)) {
                            this.av = false;
                            i(332);
                        } else if (k.v(16388) || ((this.av && k.v(2)) || (!this.av && k.v(8)))) {
                            i(17);
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 107:
                case 108:
                case 109:
                    if (r()) {
                        x();
                        this.al -= ((this.S - 107) + 1) * 20;
                        this.ak += !this.av ? 20 : -20;
                        a(0, 9);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 110:
                    if (r()) {
                        this.P |= 64;
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 122:
                    if (r()) {
                        a(53, 1032);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 146:
                    this.ag = this.av ? -2048 : 2048;
                    if (r() || ((this.av && this.aT != 3) || (!this.av && this.aU != 3))) {
                        this.al += 20;
                        i(147);
                        j = true;
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 147:
                    this.aj = 0;
                    this.ai = 0;
                    this.ah = 0;
                    this.ag = 0;
                    if (r()) {
                        if (this.S == 145) {
                            this.al += 20;
                        }
                        j = true;
                        if (k.x()) {
                            k.bG = k.bH;
                        } else {
                            k.bG = -1;
                        }
                        k.A(18);
                        k.a(true);
                        k.az = k.a(k.bA, 32);
                        break;
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 148:
                    this.ah = -5120;
                    if (r()) {
                        i(149);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 149:
                    if (l != 0) {
                        gVar = this;
                        i2 = l;
                    } else {
                        gVar = this;
                        i2 = this.av ? -1024 : 1024;
                    }
                    gVar.ag = i2;
                    this.aj = 1536;
                    if (r()) {
                        l = 0;
                        this.ag = 0;
                        i(150);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 152:
                    this.ag = this.av ? -1024 : 1024;
                    if (r()) {
                        i(0);
                        break;
                    }
                    break;
                case 156:
                    this.aj = 0;
                    this.ai = 0;
                    this.ah = 0;
                    this.ag = 0;
                    if (r()) {
                        this.ag = l;
                        this.ah = 0;
                        i(157);
                        break;
                    }
                    break;
                case 165:
                    this.aj = 1536;
                    if (r()) {
                        a(0);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 183:
                    if (i.aN != null && i.aN.S != 106 && Math.abs(i.aN.al - this.al) < 20) {
                        i.aN.i(106);
                        k.e(0, this.aw);
                        i.aN.S();
                        if (this.av) {
                            i.aN.ak = this.ak - 30;
                        } else {
                            i.aN.ak = this.ak + 30;
                        }
                        i.aN.al = this.al;
                    }
                    if (this.S == 184 && i.aN != null && i.aN.S != 107 && Math.abs(i.aN.al - this.al) < 20) {
                        i.aN.i(107);
                        k.e(0, this.aw);
                        i.aN.S();
                        if (this.av) {
                            i.aN.ak = this.ak + 35;
                        } else {
                            i.aN.ak = this.ak - 35;
                        }
                        i.aN.al = this.al;
                    }
                    this.ag = 0;
                    this.ah = 0;
                    if (!r() || i.aN == null) {
                        k.p();
                        i.O();
                        i(0);
                        if (i.aN != null) {
                            i.aN.aB = 0;
                            i.d(i.aN);
                            i.aN = null;
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 184:
                case 205:
                    if (this.S == 184) {
                        i.aN.i(107);
                        k.e(0, this.aw);
                        i.aN.S();
                        if (this.av) {
                        }
                        i.aN.al = this.al;
                        break;
                    }
                    this.ag = 0;
                    this.ah = 0;
                    if (!r()) {
                        k.p();
                        i.O();
                        i(0);
                        if (i.aN != null) {
                        }
                        break;
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 199:
                    if (this.aZ || a != null) {
                        if (y() && this.ag != 0) {
                            this.ag = 0;
                            a(this.S, 4);
                        }
                        if (k.u(4112)) {
                            if (this.av) {
                                this.ag = -2560;
                            } else {
                                this.av = true;
                            }
                            i.f(this);
                        } else if (k.u(8256)) {
                            if (this.av) {
                                this.av = false;
                            } else {
                                this.ag = 2560;
                            }
                            i.f(this);
                        } else if (!k.u(12368)) {
                            this.ag = 0;
                            i(79);
                        }
                    } else {
                        cq = false;
                        a(0);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 204:
                    if (r()) {
                        i(203);
                        break;
                    }
                    break;
                case 209:
                    if (a == null) {
                        a(0);
                        break;
                    } else {
                        if (a.ax == 66 && (a.S == 11 || a.S == 12)) {
                            this.al = a.al;
                        } else {
                            this.al = a.W[1] + 1;
                        }
                        if (this.T == this.aa.b(this.S) - 1 && a.ax == 60) {
                            this.ak = a.ak;
                            i(0);
                        }
                        if (r()) {
                            if (a.ax == 66 && (a.S == 11 || a.S == 12)) {
                                this.ak = a.ak;
                            } else {
                                this.ak = (a.W[0] + a.W[2]) >> 1;
                            }
                            if (a == null || a.ax != 66 || a.S != 12) {
                                i(0);
                                break;
                            } else if (a.Z[0] <= 0) {
                                k.aS.i(358);
                                break;
                            } else {
                                k.aS.i(228);
                                break;
                            }
                        }
                    }
                    break;
                case 214:
                    this.ah = -768;
                    if (r()) {
                        this.ah = 0;
                        i(215);
                        break;
                    }
                    break;
                case 216:
                    if (this.T < 2 || this.T > 3) {
                        this.ag = 0;
                    } else {
                        this.ag = this.av ? -5120 : 5120;
                    }
                    if ((this.av && this.aT != 0) || (!this.av && this.aU != 0)) {
                        a(0);
                    }
                    if (r()) {
                        this.ag >>= 1;
                        i(217);
                        this.P |= 64;
                        break;
                    }
                    break;
                case 217:
                    if (this.T == 0) {
                        this.aj = 1536;
                    }
                    if ((this.aR != 2 && this.aO != 2 && !L()) || a != null) {
                        if (this.aZ || this.aR == 5) {
                            this.bd = true;
                            a(true);
                            this.P &= -65;
                            this.ah = 0;
                            this.ag = 0;
                            this.aj = 0;
                            this.ai = 0;
                        }
                        if (r()) {
                            i(0);
                            break;
                        }
                    } else {
                        this.ah = 0;
                        this.aj = 0;
                        e(0);
                        i(50);
                        break;
                    }
                    break;
                case 225:
                    break;
                case 228:
                case 358:
                    this.aj = 0;
                    this.ah = 0;
                    this.ai = 0;
                    this.ag = 0;
                    if (k.u(16388) || ((k.aS.av && k.u(2)) || (!k.aS.av && k.u(8)))) {
                        if (k.aS.ac != null) {
                            if (k.aS.av != (k.aS.ac.ak < k.aS.ak)) {
                                k.aS.i(235);
                                k.aS.ac = null;
                                G();
                            }
                        }
                        if (this.S != 9) {
                        }
                        if (this.aR == 2) {
                        }
                        if (this.aO != 6) {
                        }
                        z2 = false;
                        if ((J & 4) != 0) {
                        }
                        if (!z2) {
                        }
                        if (o()) {
                        }
                        if (this.aA == 0) {
                        }
                        if ((this.aA & 4) == 0) {
                        }
                        if (this.aO != 7) {
                        }
                        if (!A) {
                        }
                    } else if (k.v(4112)) {
                        this.av = true;
                    } else if (k.v(8256)) {
                        this.av = false;
                    }
                    if (r() && this.S == 228) {
                        a(0);
                        i(252);
                    }
                    if (this.S == 228) {
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 235:
                case 238:
                    if (this.T == 1) {
                        if (k.aS.ac != null && (k.aS.ac.ax == 66 || k.aS.ac.ax == 51)) {
                            int i16 = 8;
                            if (k.aS.ac.ax == 51) {
                                i16 = 10;
                            }
                            if ((Math.abs(k.aS.ac.ak - this.ak) < 60 || Math.abs(k.aS.ac.al - this.al) < 60) && k.aS.ac.ax == 51) {
                                i16 >>= 1;
                            }
                            this.ag = 0;
                            this.ah = 0;
                            int i17 = ((this.ac.ak - this.ak) << 8) / i16;
                            int i18 = ((this.ac.al - this.al) << 8) / i16;
                            this.ag = i17;
                            this.ah = i18;
                        } else if (k.aS.ac == null || (k.aS.ac != null && k.aS.ac.ax != 66 && k.aS.ac.ax != 51)) {
                            this.ag = this.av ? -4864 : 4864;
                            this.ah = -6656;
                        }
                    }
                    if (k.aS.ac != null && (k.aS.ac.ax == 66 || k.aS.ac.ax == 51)) {
                        if (r()) {
                            i(this.S == 235 ? 236 : 239);
                            break;
                        }
                    } else {
                        av();
                        if (y()) {
                            this.ai = 0;
                            this.ag = 0;
                            a(0);
                        }
                        if (r()) {
                            a(0);
                            break;
                        }
                    }
                    break;
                case 236:
                case 239:
                    if (k.aS.ac == null || k.aS.ac.ax != 51 || k.aS.ac.S != 8) {
                        this.aj = 512;
                        break;
                    } else if (this.al <= k.aS.ac.al) {
                        a = null;
                        a(0);
                        break;
                    }
                    break;
                case 237:
                case 240:
                    if (r()) {
                        if (a == null || a.ax != 66 || a.S != 12) {
                            if (a != null && a.ax == 51) {
                                if (this.av) {
                                    if (this.W[2] > a.W[2]) {
                                        this.ak -= 20;
                                    }
                                } else if (this.W[0] < a.W[0]) {
                                    this.ak += 20;
                                }
                            }
                            i(0);
                        } else if (a.Z[0] > 0) {
                            k.aS.i(228);
                        } else {
                            k.aS.i(358);
                        }
                        this.ag = 0;
                        this.ah = 0;
                        if (a != null && a.ax != 51) {
                            this.ak = a.ak;
                            break;
                        }
                    }
                    break;
                case 242:
                case 243:
                    this.aj = 1536;
                    if (this.S == 242 && this.ah >= 0) {
                        i(243);
                    }
                    if (this.S == 243 && r()) {
                        a(0);
                    }
                    av();
                    if (y()) {
                        a(true);
                        this.ai = 0;
                        this.ag = 0;
                        break;
                    }
                    break;
                case 244:
                    break;
                case 250:
                    break;
                case 257:
                    if (r()) {
                        this.al += this.aa.c(this.S, this.T) + 10;
                        this.ak += this.av ? -this.aa.b(this.S, this.T) : this.aa.b(this.S, this.T);
                        a(0);
                        break;
                    }
                    break;
                case 258:
                    if (!k.v(16388)) {
                        if (!k.v(2) && !k.v(8) && !k.v(4112) && !k.v(8256)) {
                            if (k.v(33024)) {
                                a(0);
                                this.al += 10;
                                break;
                            }
                        } else {
                            if (k.v(2) || k.v(4112)) {
                                this.av = true;
                            } else if (k.v(8) || k.v(8256)) {
                                this.av = false;
                            }
                            i(261);
                            break;
                        }
                    } else {
                        i(259);
                        break;
                    }
                    break;
                case 259:
                case 261:
                    if (r()) {
                        if (this.S != 259) {
                            if (this.S == 261) {
                                this.ag = this.av ? -3072 : 3072;
                                this.ah = -7680;
                                i(264);
                                break;
                            }
                        } else {
                            this.ag = 0;
                            this.ah = -7680;
                            i(263);
                            break;
                        }
                    }
                    break;
                case 260:
                case 262:
                    if (k.v(16388)) {
                        i(259);
                    } else if (k.v(2) || k.v(8) || k.v(4112) || k.v(8256)) {
                        if (k.v(2) || k.v(4112)) {
                            this.av = true;
                        } else if (k.v(8) || k.v(8256)) {
                            this.av = false;
                        }
                        i(261);
                    }
                    if (r()) {
                        i(258);
                        break;
                    }
                    break;
                case 263:
                case 264:
                    cw = true;
                    this.aj = 2560;
                    if (this.ah >= 0) {
                        if (this.S == 264) {
                            i(266);
                        } else if (this.S == 263) {
                            i(265);
                        }
                    }
                    av();
                    if (y()) {
                        this.ai = 0;
                        this.ag = 0;
                        a(0);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 265:
                case 266:
                    if (r()) {
                        a(0);
                    }
                    av();
                    if (y()) {
                        this.ai = 0;
                        this.ag = 0;
                        a(0);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 267:
                    t = 100;
                    if ((this.af != null && (this.af.ax == 27 || this.af.ax == 10)) || i.ae()) {
                        if (!k.aS.aZ) {
                            k.aS.E();
                        }
                        int i19 = this.af.ax == 10 ? (this.af.W[0] + this.af.W[2]) >> 1 : (this.af.X[0] + this.af.X[2]) >> 1;
                        int i20 = i19 - this.ak;
                        if (Math.abs(i20) > 4) {
                            if (i20 >= 0) {
                                this.av = false;
                                this.ag = 1024;
                                break;
                            } else {
                                this.av = true;
                                this.ag = -1024;
                                break;
                            }
                        } else {
                            this.ak = i19;
                            this.av = false;
                            if (this.af.ax != 10) {
                                i(268);
                                this.af.i(1);
                                break;
                            } else {
                                i(291);
                                break;
                            }
                        }
                    } else {
                        t = 0;
                        i(0);
                        break;
                    }
                case 268:
                    this.ag = 0;
                    this.ah = 0;
                    if ((r() || this.T >= this.aa.b(this.S) - 2) && this.af != null && this.af.ax == 27) {
                        if ((this.ae == null || k.aS.ae.S != 102) && this.af.S != 1) {
                            G();
                            a(102, this.ak, this.al - 85);
                        }
                        if (this.ae != null) {
                            this.ae.ak = this.ak;
                            this.ae.al = this.al - 85;
                        }
                        if (this.af.S != 2 && this.az != -2) {
                            this.af.i(2);
                            this.az = -2;
                            break;
                        } else {
                            this.T = this.aa.b(this.S) - 1;
                            this.U = 0;
                            boolean z5 = false;
                            if (g != null) {
                                boolean z6 = Math.abs(g.ak - this.ak) < 60;
                                z5 = z6;
                                if (z6) {
                                    if (this.ae == null || k.aS.ae.S != 8) {
                                        G();
                                        a(8, this.ak, this.al - 85);
                                    }
                                } else if (this.ae != null && k.aS.ae.S == 8) {
                                    G();
                                }
                            } else if (this.ae != null && k.aS.ae.S == 8) {
                                G();
                            }
                            if (k.u() && (!z5 || g == null)) {
                                this.af.i(1);
                                k.v();
                                G();
                                break;
                            } else if (z5 && k.v(65568) && g != null) {
                                G();
                                this.af.ak = this.ak;
                                this.af.al = this.al;
                                this.af.i(3);
                                i(270);
                                k.A(13);
                                break;
                            }
                        }
                    }
                    break;
                case 269:
                    t = 0;
                    if (r() && this.af != null && this.af.ax == 27) {
                        i(0);
                        E();
                        this.af.i(2);
                        this.af = null;
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 270:
                    t = 0;
                    if (g != null) {
                        if (g.S != 133) {
                            g.i(133);
                        }
                        if (r()) {
                            i(271);
                            g.i(145);
                            this.bl = 5;
                            break;
                        }
                    } else {
                        i(0);
                        if (this.S != 9) {
                        }
                        if (this.aR == 2) {
                        }
                        if (this.aO != 6) {
                        }
                        z2 = false;
                        if ((J & 4) != 0) {
                        }
                        if (!z2) {
                        }
                        if (o()) {
                        }
                        if (this.aA == 0) {
                        }
                        if ((this.aA & 4) == 0) {
                        }
                        if (this.aO != 7) {
                        }
                        if (!A) {
                        }
                    }
                    break;
                case 271:
                    t = 0;
                    if (g != null) {
                        if (g.S != 134) {
                            g.i(134);
                        }
                        if (this.ae == null || k.aS.ae.S != 8) {
                            G();
                            a(8, this.ak, this.al - 85);
                        }
                        this.ae.ak = this.ak;
                        this.ae.al = this.al - 85;
                        if (k.v(65568)) {
                            this.bl += 2;
                        } else if (j.g % 2 == 0) {
                            this.bl--;
                        }
                        if (this.bl >= 0) {
                            if (this.bl >= 10) {
                                this.bl = 0;
                                G();
                                this.P &= -65;
                                i(0);
                                g.aB = 0;
                                g.i(135);
                                k.e(0, this.aw);
                                k.o(3);
                                break;
                            }
                        } else {
                            this.bl = 0;
                            G();
                            i(0);
                            g.i(5);
                            g.aA = 1;
                            g.Q();
                            break;
                        }
                    } else {
                        i(0);
                        if (this.S != 9) {
                        }
                        if (this.aR == 2) {
                        }
                        if (this.aO != 6) {
                        }
                        z2 = false;
                        if ((J & 4) != 0) {
                        }
                        if (!z2) {
                        }
                        if (o()) {
                        }
                        if (this.aA == 0) {
                        }
                        if ((this.aA & 4) == 0) {
                        }
                        if (this.aO != 7) {
                        }
                        if (!A) {
                        }
                    }
                    break;
                case 272:
                case 273:
                case 274:
                case 275:
                case 292:
                    if (i.at != null && i.at.Z[0] == 4 && this.cE <= 0) {
                        au();
                        break;
                    } else {
                        as();
                        break;
                    }
                    break;
                case 277:
                case 293:
                    au();
                    break;
                case 280:
                    if (r()) {
                        i(38);
                        break;
                    }
                    break;
                case 282:
                    if (r()) {
                        i(38);
                        break;
                    }
                    break;
                case 283:
                    if (r()) {
                        i(0);
                        break;
                    }
                    break;
                case 284:
                    if (r()) {
                        this.P |= 64;
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 286:
                case 287:
                    if (!this.aZ && a == null) {
                        a(0);
                        break;
                    } else {
                        if (k.v(65568)) {
                            this.R = -1;
                            if (this.S == 286) {
                                this.R = 287;
                            } else if (this.S == 287) {
                                this.R = 286;
                            }
                        }
                        if (r()) {
                            if (this.R != -1) {
                                i(this.R);
                            } else {
                                i(0);
                            }
                            this.R = -1;
                            break;
                        }
                    }
                    break;
                case 291:
                    this.ag = 0;
                    this.ah = 0;
                    if ((r() || this.T >= this.aa.b(this.S) - 2) && this.af != null && this.af.ax == 10) {
                        this.T = this.aa.b(this.S) - 1;
                        this.U = 0;
                        if (this.ae == null || k.aS.ae.S != 102) {
                            G();
                            a(102, this.ak, this.al - 85);
                        }
                        this.ae.ak = this.ak;
                        this.ae.al = this.al - 85;
                        boolean z7 = false;
                        if (g != null) {
                            boolean z8 = Math.abs(g.ak - this.ak) < 60;
                            z7 = z8;
                            if (z8) {
                                if (this.ae == null || k.aS.ae.S != 8) {
                                    G();
                                    a(8, this.ak, this.al - 85);
                                }
                            } else if (this.ae != null && k.aS.ae.S == 8) {
                                G();
                            }
                        } else if (this.ae != null && k.aS.ae.S == 8) {
                            G();
                        }
                        if (k.u() && (!z7 || g == null)) {
                            i(285);
                            this.af = null;
                            k.v();
                            G();
                        } else if (z7 && k.v(65568) && g != null) {
                            G();
                            this.af = null;
                            i(270);
                            k.A(13);
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 294:
                    break;
                case 295:
                    if ((J & 8) != 0) {
                        h(8);
                    }
                    k.A(19);
                    if (I == 8) {
                        aB();
                        if (k.v(65568) && g != null) {
                            aq();
                        }
                    }
                    if (y()) {
                        a = null;
                        a(0);
                        i = false;
                        k.ae = this;
                        break;
                    }
                    break;
                case 298:
                    if (r()) {
                        this.P |= 64;
                    }
                    if (i.at != null && i.at.S != 168) {
                        as();
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 299:
                case 300:
                case 301:
                case 302:
                    if (r()) {
                        i(303);
                        break;
                    }
                    break;
                case 303:
                    if (!k.u(62430)) {
                        if (r()) {
                            this.P |= 64;
                            aB();
                            if (k.v(65568)) {
                                ar();
                                break;
                            }
                        }
                    } else {
                        i(0);
                        if (this.S != 9) {
                        }
                        if (this.aR == 2) {
                        }
                        if (this.aO != 6) {
                        }
                        z2 = false;
                        if ((J & 4) != 0) {
                        }
                        if (!z2) {
                        }
                        if (o()) {
                        }
                        if (this.aA == 0) {
                        }
                        if ((this.aA & 4) == 0) {
                        }
                        if (this.aO != 7) {
                        }
                        if (!A) {
                        }
                    }
                    break;
                case 304:
                case 305:
                case 306:
                    if (r()) {
                        this.K = 0;
                        this.cN = 0;
                        i(295);
                        break;
                    }
                    break;
                case 310:
                    break;
                case 311:
                case 312:
                    a(true);
                    if (r()) {
                        this.ah = 0;
                        this.ag = 0;
                        l();
                        break;
                    }
                    break;
                case 313:
                    break;
                case 317:
                    this.aj = 128;
                    if (this.ah >= 512) {
                        this.ah = 512;
                    }
                    if (this.av) {
                        this.ag = -2560;
                    } else {
                        this.ag = 2560;
                    }
                    if (k.v(16388)) {
                        this.ah = -2048;
                    }
                    if (y() || this.aR >= 12 || this.aR == 5) {
                        this.ag = 0;
                        if (k.z[30] != null && f == null) {
                            f = i.a(8, 30, 4, this.av, this.av ? this.W[0] : this.W[2], this.al, 300);
                        }
                        if (f != null && f.r()) {
                            k.c(f);
                            f = null;
                            this.aj = 0;
                            this.ah = 0;
                            i(50);
                        }
                    }
                    if (!q) {
                        G();
                        break;
                    } else {
                        a(1, this.ak, this.al - 60);
                        break;
                    }
                    break;
                case 332:
                    this.ag = this.av ? -2560 : 2560;
                    if (this.aT >= 12 && this.ag < 0) {
                        this.ag = 0;
                    }
                    if (this.aU >= 12 && this.ag > 0) {
                        this.ag = 0;
                    }
                    if (this.aZ) {
                        l();
                    } else if (this.aR != 4) {
                        a(0);
                    } else if (k.v(16388) || ((this.av && k.v(2)) || (!this.av && k.v(8)))) {
                        i(17);
                    } else {
                        if (k.u(4112)) {
                            this.av = true;
                            i(332);
                        } else if (k.u(8256)) {
                            this.av = false;
                            i(332);
                        }
                        if (r()) {
                            this.aj = 0;
                            this.ai = 0;
                            this.ah = 0;
                            this.ag = 0;
                            this.aC = 18;
                            i(102);
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 357:
                    this.ag = 3328;
                    if (this.av) {
                        this.ag = -1280;
                    }
                    if (r()) {
                        this.ag = 0;
                        this.K = 4;
                        i(364);
                        ar();
                        break;
                    }
                    break;
                case 360:
                    int i21 = 9;
                    int i22 = 40;
                    if (this.av) {
                        i21 = 106;
                        i22 = -40;
                    }
                    a(i21, this.ak + i22, this.al - 85);
                    if ((!this.av && k.v(8256)) || (this.av && k.v(4112))) {
                        G();
                        i(357);
                    }
                    if (r()) {
                        G();
                        i(0);
                        break;
                    }
                    break;
                case 370:
                    if (r()) {
                        i(371);
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 374:
                    this.ah = 0;
                    this.ag = 0;
                    if (r()) {
                        if (x[1] > 0) {
                            this.ah = 0;
                            this.ag = 0;
                            i(376);
                        } else {
                            k.l(12);
                        }
                    }
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 375:
                    this.ag = 1280;
                    if (!this.av) {
                        this.ag = -1280;
                    }
                    if (r()) {
                        i(376);
                    }
                    i.f(this);
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 376:
                    this.ah = 0;
                    this.ag = 0;
                    if (r()) {
                        i(377);
                    }
                    i.f(this);
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
                case 377:
                    if (r()) {
                        a(0);
                    }
                    i.f(this);
                    if (this.S != 9) {
                    }
                    if (this.aR == 2) {
                    }
                    if (this.aO != 6) {
                    }
                    z2 = false;
                    if ((J & 4) != 0) {
                    }
                    if (!z2) {
                    }
                    if (o()) {
                    }
                    if (this.aA == 0) {
                    }
                    if ((this.aA & 4) == 0) {
                    }
                    if (this.aO != 7) {
                    }
                    if (!A) {
                    }
                    break;
            }
        }
    }

    public final boolean f() {
        return ci != null && g(ci) && Math.abs(ci.ak - this.ak) < 120 && Math.abs(ci.al - this.al) < 20;
    }

    private boolean c(boolean z2) {
        return k.g(z2 ? (this.W[0] / 20) - 1 : (this.W[2] / 20) + 1, this.al / 20) < 12;
    }

    private boolean ao() {
        if ((!k.v(131072) && !k.c(355, 197, 30, 26)) || k.at != 0 || k.as <= 1) {
            return false;
        }
        if (k.C != null && (k.C == null || (this.P & 512) == 0)) {
            return false;
        }
        k.at = 1;
        k.aS.h(k.ar[(k.p(I) + 1) % k.as]);
        k.A(23);
        return true;
    }

    private void ap() {
        if (I == 4 && this.aA < 2) {
            h(1);
        }
        boolean z2 = (this.ac == null || this.ac.ax != 10) && !b();
        if (k.v(65568) && z2) {
            if (I == 1) {
                this.ag = 0;
                this.ah = 0;
                this.aj = 0;
                if (this.S != 79 || a == null || a.ax != 51 || a.aD == 0) {
                    i(this.S == 79 ? 81 : 67);
                    if (k.E != null) {
                        k.E.K();
                        return;
                    }
                    return;
                }
                return;
            }
            if (I == 8) {
                if (this.S != 79) {
                    this.ai = 0;
                    this.ag = 0;
                    this.K = 0;
                    this.cN = 0;
                    i(303);
                    return;
                }
                return;
            }
            if (I != 2 || this.S == 79) {
                return;
            }
            this.ai = 0;
            this.ag = 0;
            i(286);
            k.A(29);
        }
    }

    public static void d(int i2) {
        if (s || t != 0 || k.aS.c() || k.aS.S == 67 || k.aS.S == 183 || k.aS.S == 184 || k.aS.S == 205) {
            return;
        }
        i.bh = 8;
        int[] iArr = x;
        iArr[1] = iArr[1] - i2;
        if (x[1] > 0) {
            t = 10;
            return;
        }
        x[1] = 0;
        if (k.bh[k.aj] != 3) {
            if (!k.aS.aZ && a == null) {
                k.aS.E();
            }
            k.aS.bl = 0;
            k.aS.G();
            k.aS.H();
            a = null;
        }
    }

    public static void e(int i2) {
        x[1] = i2;
    }

    public static void f(int i2) {
        if (x[1] > i2) {
            x[1] = i2;
        }
    }

    public static boolean g() {
        return x[1] <= 0;
    }

    public static boolean h() {
        return s || t != 0;
    }

    private void aq() {
        if (g == null) {
            i(295);
            k.v();
        }
        k.A(16);
        int i2 = (this.W[0] + this.W[2]) >> 1;
        int i3 = (this.W[1] + this.W[3]) >> 1;
        int i4 = (g.W[0] + g.W[2]) >> 1;
        int i5 = (g.W[1] + g.W[3]) >> 1;
        int iAbs = Math.abs(i4 - i2);
        int iAbs2 = Math.abs(i5 - i3) << 8;
        if (iAbs > 0 && iAbs2 > 0) {
            int i6 = iAbs2 / iAbs;
            if (i6 <= 64) {
                i(306);
            } else if (i6 > 64 && i6 <= 256) {
                i(305);
            } else if (i6 > 256 && i6 <= 1024) {
                i(304);
            }
        } else if (iAbs2 == 0) {
            i(306);
        }
        int i7 = i.bu[k.au];
        this.K = 6;
        if (this.K > 3) {
            i.a(8, 5, 14, this.av, this.L, this.M + 30, 300);
            if (g.ax == 4) {
                if (g.S == 30) {
                    g.i(29);
                }
            } else if (g.ax != 58) {
                g.aB -= (i7 * this.K) / 6;
                g.C();
            } else {
                switch (g.S) {
                    case 0:
                    case 5:
                    case 7:
                    case 9:
                    case 11:
                        g.i(this.S + 1);
                        break;
                    case 2:
                        g.i(3);
                        break;
                }
            }
        }
    }

    private void ar() {
        if (g == null) {
            i(0);
            k.v();
        }
        k.A(16);
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        int i2 = this.ak;
        int i3 = (this.Y[1] + this.Y[3]) >> 1;
        int i4 = g.ak;
        int i5 = (g.Y[1] + g.Y[3]) >> 1;
        int iAbs = Math.abs(i4 - i2);
        int iAbs2 = Math.abs(i5 - i3) << 8;
        if (this.S != 364) {
            if (iAbs > 0 && iAbs2 > 0 && i5 - i3 < 20) {
                int i6 = iAbs2 / iAbs;
                if (i6 <= 64) {
                    i(301);
                } else if (i6 > 64 && i6 <= 256) {
                    i(300);
                } else if (i6 > 256 && i6 <= 1024) {
                    i(299);
                }
            } else if (i5 - i3 > 20) {
                i(302);
            } else if (iAbs2 == 0) {
                i(301);
            }
        }
        int i7 = i.bu[k.au] << 1;
        if (this.K > 3) {
            i.a(8, 5, 14, this.av, this.L, this.M, 300);
            if (g.ax == 4) {
                if (g.S == 30) {
                    g.i(29);
                }
            } else if (g.ax != 58) {
                g.aB -= (i7 * this.K) / 6;
                g.C();
            } else {
                switch (g.S) {
                    case 0:
                    case 5:
                    case 7:
                    case 9:
                    case 11:
                        g.i(this.S + 1);
                        break;
                    case 2:
                        g.i(3);
                        break;
                }
            }
        }
    }

    public final void c(i iVar) {
        t();
        i.bq = 0;
        this.aj = 0;
        this.ai = 0;
        this.ah = 0;
        this.ag = 0;
        int i2 = (this.W[0] + this.W[2]) >> 1;
        int i3 = (this.W[1] + this.W[3]) >> 1;
        int i4 = iVar.ak;
        int i5 = iVar.al;
        if (iVar.ax == 11 || iVar.ax == 17) {
            i4 = (iVar.W[0] + iVar.W[2]) >> 1;
            i5 = iVar.W[3] - 45;
        }
        int iAbs = Math.abs(i4 - i2);
        int iAbs2 = Math.abs(i5 - i3) << 8;
        if (b(this.S)) {
            i(292);
            k.A(30);
        } else if (this.S == 298) {
            k.A(30);
        } else if (iAbs > 0 && iAbs2 > 0) {
            int i6 = iAbs2 / iAbs;
            if (i6 <= 64) {
                i(272);
            } else if (i6 > 64 && i6 <= 256) {
                i(273);
            } else if (i6 > 256 && i6 <= 1024) {
                i(274);
            } else if (i6 > 1024) {
                i(275);
            }
            k.A(30);
        } else if (iAbs == 0) {
            i(275);
            k.A(30);
        } else if (iAbs2 == 0) {
            i(272);
            k.A(30);
        }
        this.cF = 5120;
        this.cx = (8 * j.m) / 360;
        this.cG = this.av;
        if (iVar.ax == 72) {
            if (iVar.Z[0] == 0 || iVar.Z[0] == 3 || iVar.Z[0] == 4) {
                if (iVar.Z[1] > 0) {
                    this.cF = iVar.Z[1];
                }
                if (iVar.Z[2] > 0) {
                    this.cx = (iVar.Z[2] * j.m) / 360;
                }
            } else if (iVar.Z[0] == 1 || iVar.Z[0] == 2) {
                this.cM = 0;
            }
        } else if (iVar.ax == 11 || iVar.ax == 17) {
            if (this.S == 298) {
                this.cF = 7680;
                this.cx = 0;
            } else {
                this.cF = 5120;
                this.cx = 0;
            }
        }
        this.cE = 0;
        for (int i7 = 0; i7 < this.aa.b(this.S); i7++) {
            this.cE += this.aa.a(this.S, i7);
        }
        t();
        int i8 = (iVar.W[1] + iVar.W[3]) >> 1;
        this.cz = iVar.ak - this.X[0];
        this.cA = i8 - this.X[1];
        this.cB = k.h(this.cz, this.cA);
        if (this.cz == 0) {
            this.cz = 1;
        }
        this.cy = j.b(-this.cz, this.cA);
        this.cC = this.cz / this.cE;
        int iAbs3 = (Math.abs(this.cA) << 8) / Math.abs(this.cz);
        if (this.cz == 1) {
            this.cD = this.cA / this.cE;
        } else {
            this.cD = (Math.abs(this.cC) * iAbs3) >> 8;
        }
        if (i8 < this.X[1]) {
            this.cD = -this.cD;
        }
        this.cJ = this.X[0];
        this.cK = this.X[1];
        this.cH = this.X[0];
        this.cI = this.X[1];
        F = iVar;
    }

    private void as() {
        i iVarQ;
        if (i.at != null && (g == null || (g != null && !g(g)))) {
            F = i.at;
        } else if (g == null || g.ax != 11 || g.Z[19] != 1 || g.P()) {
            F = null;
        } else {
            F = g;
        }
        if (F == null) {
            a(0);
            return;
        }
        this.cJ = this.X[0];
        this.cK = this.X[1];
        this.cH += this.cC;
        this.cI += this.cD;
        int i2 = this.cE - 1;
        this.cE = i2;
        if (i2 <= 0) {
            this.cH = F.ak;
            this.cI = (F.W[1] + F.W[3]) >> 1;
            if (F.ax != 72 || F.Z[0] != 4) {
                this.ak = this.X[0];
                this.al = this.X[1];
            } else if (F.Z[4] >= 0 && (iVarQ = k.q(F.Z[4])) != null) {
                F.ac = iVarQ;
                iVarQ.aq = F.ak - iVarQ.ak;
                iVarQ.ar = F.al - iVarQ.al;
            }
            if (F.ax == 72) {
                switch (F.Z[0]) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        this.cL = 0;
                        if (F.Z[0] != 4) {
                            i(277);
                            break;
                        }
                        break;
                }
                return;
            }
            if (F.ax == 11 || F.ax == 17) {
                switch (F.ax) {
                    case 11:
                        if (this.S != 298) {
                            this.cL = 0;
                            i(277);
                            F.ag = (this.cF >> 8) * j.b(this.cy);
                            F.ah = (-(this.cF >> 8)) * j.b(j.n - this.cy);
                            if (!F.g(this)) {
                                F.i(180);
                                break;
                            } else {
                                F.i(181);
                                break;
                            }
                        } else {
                            F.i(168);
                            break;
                        }
                    case 17:
                        break;
                }
                return;
            }
            i(0);
        }
    }

    /* JADX DEBUG: Possible override for method i.i()Z */
    public final void i() {
        a(this.cJ, this.cK, this.cH, this.cI, this.cy);
    }

    public static void a(int i2, int i3, int i4, int i5, int i6) {
        int iH = k.h(i4 - i2, i5 - i3) / 6;
        if (iH <= 0 || k.z[61] == null) {
            return;
        }
        int i7 = 0;
        for (int i8 = 0; i8 < iH; i8++) {
            i7 += 6;
            k.z[61].a(i.bg, 0, 0, (i4 + ((i7 * j.b(i6)) >> 8)) - k.O, (i5 - ((i7 * j.b(j.n - i6)) >> 8)) - k.P, 0, 0, 0);
        }
    }

    public final void a(i iVar, i iVar2) {
        if (iVar == null || iVar2 == null) {
            return;
        }
        iVar.t();
        iVar2.t();
        int i2 = iVar.X[0];
        int i3 = iVar.X[1];
        int i4 = iVar2.X[0];
        int i5 = iVar2.X[1];
        int i6 = i4 - i2;
        int i7 = i5 - i3;
        if (i6 == 0) {
            i6 = 1;
        }
        a(i2, i3, i4, i5, j.b(-i6, i7));
    }

    private void at() {
        int iB = (this.cB * j.b(this.cy)) >> 8;
        int iB2 = (this.cB * j.b(j.n - this.cy)) >> 8;
        this.ak = this.cH + iB;
        this.al = this.cI - iB2;
    }

    private void au() {
        if (i.at != null && (g == null || (g != null && !g(g)))) {
            F = i.at;
        } else if (g == null || g.ax != 11 || g.Z[19] != 1 || g.P()) {
            F = null;
        } else {
            F = g;
        }
        if (F == null) {
            a(0);
        }
        if (F.ax == 72 && F.Z[0] == 4) {
            this.cJ = this.X[0];
            this.cK = this.X[1];
        } else {
            this.cJ = this.ak;
            this.cK = this.al;
        }
        this.cH = F.ak;
        this.cI = (F.W[1] + F.W[3]) >> 1;
        if (F.ax != 72) {
            if (F.ax == 11 || F.ax == 17) {
                if (this.S != 293) {
                    this.ag = (-(this.cF >> 8)) * j.b(this.cy);
                    this.ah = (this.cF >> 8) * j.b(j.n - this.cy);
                    return;
                }
                this.cB += 6;
                this.al += 6;
                if (this.cB >= 15) {
                    this.al = this.W[3];
                    a(0);
                }
                this.cK = this.al;
                return;
            }
            return;
        }
        switch (F.Z[0]) {
            case 0:
                this.cB -= this.cF >> 8;
                if (this.cy < j.o && this.cy > j.n) {
                    this.cy += this.cx;
                } else if (this.cy > j.o || this.cy < j.n) {
                    this.cy -= this.cx;
                }
                at();
                if (i.a(this.W, F.W)) {
                    i(22);
                    this.ag = (-(this.cF >> 8)) * j.b(this.cy);
                    this.ah = (this.cF >> 8) * j.b(j.n - this.cy);
                    int i2 = this.ak;
                    this.ak += this.ag >> 8;
                    a(false);
                    if (y()) {
                        this.ag = 0;
                    }
                    this.ak = i2;
                    t();
                    F = null;
                }
                av();
                break;
            case 1:
            case 2:
                if (this.cM == 0) {
                    this.cB -= this.cF >> 8;
                    if (F.Z[0] == 1) {
                        if (this.cB <= 75) {
                            this.cB = 75;
                        }
                    } else if (F.Z[0] == 2 && this.cB <= 75) {
                        this.cB = 75;
                    }
                }
                if (this.cM == 0) {
                    if (F.Z[0] == 2) {
                        this.cx = (15 * j.m) / 360;
                    }
                    if (this.cy < j.o && this.cy > j.n) {
                        this.cy += this.cx;
                    } else if (this.cy > j.o || this.cy < j.n) {
                        this.cy -= this.cx;
                    }
                    if (Math.abs(this.cy - j.o) <= this.cx) {
                        if (F.Z[0] == 2) {
                            this.cL = (35 * j.m) / 360;
                        } else {
                            this.cL = (16 * j.m) / 360;
                        }
                        this.cM = 1;
                        if (F.Z[0] == 2 && this.cB > 75) {
                            this.cB = 75;
                        }
                    }
                } else if (this.cM == 1) {
                    if (this.cy < j.o - this.cL || this.cy > j.o + this.cL) {
                        if (this.cy < j.o - this.cL) {
                            this.cy = j.o - this.cL;
                        } else if (this.cy > j.o + this.cL) {
                            this.cy = j.o + this.cL;
                        }
                        this.cG = !this.cG;
                        if (F.Z[0] == 2) {
                            k.aS.ag = k.aS.av ? -3328 : 3328;
                            k.aS.ah = -6656;
                            k.aS.i(243);
                            break;
                        }
                    }
                    if (F.Z[0] == 1) {
                        this.cL--;
                    }
                    if (F.Z[0] == 2) {
                        this.cy += this.cG ? -10 : 10;
                    } else {
                        this.cy += this.cG ? -5 : 5;
                    }
                    if (this.cL <= 0 && this.S != 293) {
                        i(293);
                        this.cM = 2;
                        this.cy = j.o;
                        at();
                    }
                }
                if (this.S == 293 && F.Z[0] == 1) {
                    this.cB += 6;
                    this.al += 6;
                    if (this.cB >= F.Z[3]) {
                        this.al = this.W[3];
                        a(0);
                    }
                    this.cK = this.al;
                } else {
                    at();
                }
                this.cJ = this.ak;
                this.cK = this.al;
                break;
            case 3:
                this.cB -= this.cF >> 8;
                if (this.cy < j.o && this.cy > j.n) {
                    this.cy += this.cx;
                } else if (this.cy > j.o || this.cy < j.n) {
                    this.cy -= this.cx;
                }
                at();
                if (i.a(this.W, F.W)) {
                    F = null;
                    i.at = null;
                }
                av();
                break;
            case 4:
                if (!k.k()) {
                    d(200 + k.O, 120 + k.P);
                }
                if (k.v(65568) || k.u(65568) || (!k.k() && V())) {
                    this.cB -= (this.cF >> 8) >> 2;
                }
                int iB = (this.cB * j.b(this.cy)) >> 8;
                int iB2 = (this.cB * j.b(j.n - this.cy)) >> 8;
                F.ak = this.cJ - iB;
                F.al = this.cK + iB2;
                if (F.ac != null) {
                    if (F.ac.S == 2 || F.ac.S == 3 || this.cB < 20) {
                        F.ac = null;
                        F.P |= 160;
                        F.Z[4] = -1;
                        F = null;
                        i(0);
                        a(false);
                        if (k.k()) {
                            G();
                        } else {
                            U();
                        }
                    } else {
                        F.ac.ak = F.ak - F.ac.aq;
                        F.ac.al = F.al - F.ac.ar;
                    }
                }
                av();
                break;
        }
    }

    private void d(boolean z2) {
        this.al = ((this.W[3] + 1) / 20) * 20;
        this.al--;
        if (!z2 ? !(this.aR >= 12 || (this.aS < 12 && this.aS != 5)) : !(this.aR == 4 || this.aS != 4)) {
            this.al += 20;
        }
        this.ah = 1;
        boolean z3 = false;
        if (z2) {
            this.aj = 0;
            this.ai = 0;
            this.ah = 0;
            this.ag = 0;
            this.aC = 18;
            i(102);
        } else if (this.S == 16 || this.Q == 16) {
            i(5);
            this.ag = 0;
        } else {
            if (this.S == 150) {
                i(152);
            } else {
                int i2 = (this.al - y) / 20;
                i(5);
                this.ag = 0;
            }
            z3 = true;
        }
        if (I == 4) {
            h(1);
        }
        if (this.S == 16 || this.Q == 16 || z2 || !z3 || (this.al - y) / 20 < 20 || h()) {
            return;
        }
        a(21, 0, 0, this);
    }

    private void av() {
        this.al -= 20;
        x();
        this.al += 20;
        if (this.aO >= 20) {
            a(0);
        }
        if (this.aO < 20 || this.ah >= 0 || i.bq != 0) {
            return;
        }
        t();
        int i2 = this.W[3];
        int i3 = this.W[0] - 1;
        int i4 = this.W[2] + 1;
        if (this.ag != 0) {
            if (this.ag > 0) {
                if (e(i4 / 20, i2 / 20) >= 20) {
                    this.ai = 0;
                    this.ag = 0;
                    this.ak -= 10;
                    return;
                }
                return;
            }
            if (this.ag >= 0 || e(i3 / 20, i2 / 20) < 20) {
                return;
            }
            this.ai = 0;
            this.ag = 0;
            this.ak += 10;
        }
    }

    /* JADX DEBUG: Possible override for method i.j()Z */
    public final void j() {
        if (this.bM.aG == 1 || this.bM.aG == 4) {
            i(23);
            if (this.bM.aG == 4) {
                this.av = this.bM.Z[3] != 0;
            }
            this.ag = this.av ? -2048 : 2048;
            this.ah = -2560;
        } else {
            i(43);
            this.ag = this.av ? -2048 : 2048;
            this.ah = -3840;
            if (y()) {
                this.ag = 0;
            }
        }
        if (this.bM.aG == 2) {
            k.aS.H();
        }
        this.bM.aA = 0;
        this.bM.bM = null;
        this.bM = null;
        this.aA &= -65;
    }

    /* JADX DEBUG: Possible override for method i.k()Z */
    public final void k() {
        if (this.bM != null) {
            if (this.bM == null || this.bM.aG != 1) {
                i iVar = this.bM;
                if (this.bM.aG == 4) {
                    if (iVar.bN - 2 < 0) {
                        j();
                        return;
                    } else {
                        iVar.bN--;
                        i(82);
                        return;
                    }
                }
                if (k.u(65568)) {
                    i(86);
                    return;
                }
                if (k.x(4112)) {
                    this.av = true;
                    return;
                }
                if (k.x(8256)) {
                    this.av = false;
                    return;
                }
                if (k.u(2)) {
                    this.av = true;
                    j();
                    return;
                }
                if (k.u(8)) {
                    this.av = false;
                    j();
                    return;
                }
                if (k.u(8256)) {
                    if (iVar.bP <= 0 && iVar.bO >= 0) {
                        if (iVar.bP > 0 || iVar.bP <= -1280) {
                            iVar.bP += 512;
                        } else {
                            iVar.bP = -1280;
                        }
                        iVar.bO += (20480 + iVar.bP) / 80;
                    }
                    i(!this.av ? 84 : 85);
                    return;
                }
                if (k.u(4112)) {
                    if (iVar.bP >= 0 && iVar.bO <= 0) {
                        if (iVar.bP < 0 || iVar.bP >= 1280) {
                            iVar.bP -= 512;
                        } else {
                            iVar.bP = 1280;
                        }
                        iVar.bO -= (20480 - iVar.bP) / 80;
                    }
                    i(this.av ? 84 : 85);
                    return;
                }
                if (k.u(16388)) {
                    if (iVar.bO != 0 || iVar.bP != 0) {
                        iVar.bP = 0;
                        iVar.bO = 0;
                        return;
                    }
                    if (this.S == 326 || this.S == 83 || (this.S == 82 && this.T == 0)) {
                        if (iVar.bN - 2 < 0) {
                            i(326);
                            return;
                        } else {
                            iVar.bN--;
                            i(82);
                            return;
                        }
                    }
                    if (this.S == 82 || !r()) {
                        return;
                    }
                } else {
                    if (k.u(33024)) {
                        if (iVar.bO != 0 || iVar.bO != 0) {
                            iVar.bP = 0;
                            iVar.bO = 0;
                            return;
                        }
                        if (iVar.bN + 2 <= iVar.Z[1] - 2) {
                            iVar.bN++;
                            i(83);
                            return;
                        }
                        i(43);
                        this.ag = 0;
                        this.ah = 2560;
                        if (this.bM.aG == 2) {
                            k.aS.H();
                        }
                        this.bM.aA = 0;
                        this.bM.bM = null;
                        this.aA &= -65;
                        return;
                    }
                    if (this.S == 326) {
                        return;
                    }
                    if (!r() && this.T != 0) {
                        return;
                    }
                }
                i(326);
            }
        }
    }

    public final boolean l() {
        if (k.aT) {
            return true;
        }
        this.bM = null;
        this.aF = 0;
        cp = true;
        cq = true;
        z = true;
        if (this.S == 79) {
            if (k.u(4112)) {
                if (this.av) {
                    i(i.bn ? 199 : 32);
                    this.ag = (y() || this.S == 199) ? 0 : -2560;
                    i.f(this);
                } else {
                    this.av = true;
                }
            } else if (k.u(8256)) {
                if (this.av) {
                    this.av = false;
                } else {
                    i(i.bn ? 199 : 32);
                    this.ag = (y() || this.S == 199) ? 0 : 2560;
                    i.f(this);
                }
            }
            if (i.bn) {
                cq = false;
                z = false;
                return true;
            }
            if (!k.u(16388)) {
                return this.aZ || a != null;
            }
        }
        if (g != null && g.ax == 73 && g.Z[0] == 3) {
            cq = false;
        }
        if (k.u(4112) || k.x(4112) || k.u(128)) {
            if (this.av) {
                return ax();
            }
            if (this.aA != 0) {
                this.av = true;
            } else if (k.bD > 4 && !k.x(4112)) {
                this.av = true;
            } else if (k.x(4112)) {
                i(10);
                this.ag = -4096;
                this.ag = y() ? 0 : this.ag;
                return true;
            }
        } else if (k.u(8256) || k.x(8256) || k.u(512)) {
            if (!this.av) {
                return ax();
            }
            if (this.aA != 0) {
                this.av = false;
            } else if (k.bD > 4 && !k.x(8256)) {
                this.av = false;
            } else if (k.x(8256)) {
                i(10);
                this.ag = 4096;
                this.ag = y() ? 0 : this.ag;
                return true;
            }
        } else if (k.u(33024)) {
            if (this.ag != 0) {
                this.ag = 0;
                i(32);
                return true;
            }
        } else if (((this.S == 12 && co >= 4) || this.S == 11) && (this.S != 11 || !r())) {
            i(11);
            return true;
        }
        return aw();
    }

    private boolean aw() {
        this.ag = 0;
        this.ah = 0;
        if (!this.aZ && a == null) {
            if (k.aS.af != null || k.aS.S == 284) {
                return true;
            }
            a(0);
            cq = false;
            z = false;
            return true;
        }
        if (this.S == 79 && !i.bn) {
            if (e(((this.W[0] + this.W[2]) / 2) / 20, (this.W[1] / 20) - 1) > 12 || !k.u(16388)) {
                return true;
            }
            i(80);
            k.v();
            return true;
        }
        if (this.S == 2 || !k.u(33024)) {
            if (this.S != 6 && k.u(16388) && f()) {
                i(6);
                return true;
            }
            if (j.g % 100 == 0) {
                i(1);
                return true;
            }
            if (this.S == 1 && !r()) {
                return true;
            }
            i(this.S == 81 ? 79 : 0);
            return true;
        }
        if (this.aS == 9) {
            a(122, 8);
            return true;
        }
        this.al += 20;
        x();
        this.al -= 20;
        int i2 = this.av ? (this.W[0] / 20) - 2 : (this.W[2] / 20) + 2;
        int i3 = (this.W[3] / 20) + 1;
        if ((this.aQ == 20 || this.aQ == 5) && this.aR == 0 && e(i2, i3) < 12) {
            a(257, 8);
            return true;
        }
        i(78);
        return true;
    }

    private boolean ax() {
        this.ah = 0;
        if (!this.aZ && a == null) {
            cq = false;
            z = false;
            return false;
        }
        if (this.aR <= 18 || ((!this.av || this.aV >= 12 || this.bb) && (this.av || this.aW >= 12 || this.bc))) {
            if (this.S != 12) {
                co = 0;
            }
            i(12);
            if (this.av) {
                this.ag = (this.aT < 18 || this.aT > 23) ? -2560 : -512;
            } else {
                this.ag = (this.aU < 18 || this.aU > 23) ? 2560 : 512;
            }
        } else {
            i(26);
            this.ag = this.av ? -1280 : 1280;
        }
        if (this.aR == 14 || this.aR == 15) {
            this.ah = this.ag >> 1;
        } else if (this.aR == 17 || this.aR == 16) {
            this.ah = (-this.ag) >> 1;
        }
        if (this.ah < 0) {
            this.ah = 0;
        }
        i.f(this);
        return true;
    }

    public static void g(int i2) {
        J |= i2;
        k.q();
    }

    /* JADX DEBUG: Possible override for method i.h(I)V */
    public final boolean h(int i2) {
        if (i2 != 0 && (J & i2) == 0) {
            return false;
        }
        I = i2;
        if (k.at != 1) {
            k.at = 1;
        }
        if (i.at != null && i.at.ab != null && i.at.ab.ax == 16) {
            i.at.H();
        }
        if (i2 == 1 || this.S != 38) {
            return true;
        }
        I = 1;
        if (i.at == null || i.at.ab == null || i.at.ab.ax != 16) {
            return false;
        }
        i.at.H();
        return false;
    }

    /* JADX DEBUG: Possible override for method i.m()V */
    public final boolean m() {
        if (this.ax == 0) {
            return (a != null && a.ax == 51) || i.bq != 0;
        }
        return false;
    }

    /* JADX WARN: Removed duplicated region for block: B:29:0x00da  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void ay() {
        boolean z2;
        if (a == null || ((!this.av || a.W[0] >= this.W[0]) && (this.av || a.W[2] <= this.W[2]))) {
            int iE = e((this.W[2] / 20) + 1, this.W[3] / 20);
            int iE2 = e((this.W[0] / 20) - 1, this.W[3] / 20);
            if (i.aN != null) {
                int iE3 = e((this.W[2] / 20) + 2, this.W[3] / 20);
                int iE4 = e((this.W[0] / 20) - 2, this.W[3] / 20);
                z2 = (this.av || iE3 == 20 || iE3 == 5) ? (!this.av || iE4 == 20 || iE4 == 5) ? (this.av || iE == 20 || iE == 5) ? !this.av || iE2 == 20 || iE2 == 5 : false : false : false;
            }
        } else {
            z2 = true;
        }
        if (!z2) {
            this.ai = 0;
            this.ag = 0;
            return;
        }
        switch (this.S) {
            case 67:
                if (this.T == 1 && k.aS.V <= 0) {
                    this.ag = this.av ? -1792 : 1792;
                    break;
                } else {
                    this.ag = 0;
                    break;
                }
                break;
            case 68:
                if ((this.T != 0 && this.T != 1) || k.aS.V > 0) {
                    this.ag = 0;
                    break;
                } else {
                    this.ag = this.av ? -1792 : 1792;
                    break;
                }
            case 69:
                if (this.T == 1 && k.aS.V <= 0) {
                    this.ag = this.av ? -1792 : 1792;
                    break;
                } else {
                    this.ag = 0;
                    break;
                }
                break;
        }
        if (i.aN != null) {
            i.aN.t();
            if (this.ak >= i.aN.ak || this.ag < 0) {
                if (this.ak > i.aN.ak && this.ag <= 0 && ((this.ak + (this.ag >> 8)) + (this.W[0] - this.ak)) - 2 <= i.aN.W[2]) {
                    this.ai = 0;
                    this.ag = 0;
                }
            } else if (this.ak + (this.ag >> 8) + (this.W[2] - this.ak) + 2 >= i.aN.W[0]) {
                this.ai = 0;
                this.ag = 0;
            }
        }
        i.f(this);
    }

    private static boolean k(int i2) {
        switch (i2) {
            case 0:
            case 1:
            case 18:
            case 19:
            case 20:
            case 23:
            case 24:
            case 25:
            case 35:
            case 36:
            case 43:
            case 150:
            case 157:
            case 165:
            case 242:
            case 243:
            case 263:
            case 264:
            case 265:
            case 266:
            case 358:
                return true;
            default:
                return false;
        }
    }

    private boolean i(i iVar) {
        if (((J & 4) != 0 && iVar.ax == 11 && iVar.Z[19] == 1 && (((this.av && iVar.ak - this.ak < 0) || (!this.av && iVar.ak - this.ak > 0)) && Math.abs(iVar.ak - this.ak) <= 200)) || this.S == 268 || this.S == 291 || k.aS.S == 267) {
            return true;
        }
        if ((this.S == 303 && r()) || this.S == 295 || this.S == 357 || this.S == 358) {
            return true;
        }
        return this.S >= 299 && this.S <= 307;
    }

    /* JADX WARN: Removed duplicated region for block: B:278:0x0613 A[PHI: r6
      0x0613: PHI (r6v6 int) = (r6v5 int), (r6v4 int), (r6v2 int) binds: [B:277:0x05f5, B:240:0x0510, B:198:0x03fc] A[DONT_GENERATE, DONT_INLINE]] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private void az() {
        if ((this.aA & 8) != 0 || this.S == 250) {
            g = null;
            i.at = null;
            return;
        }
        if (g == null || !(this.S == 270 || this.S == 271)) {
            if (this.S == 268 || k.aS.S == 267 || this.S == 291) {
                g = null;
            }
            if (g != null && g.ax != 4 && (g.aB <= 0 || k.h(this.ak - g.ak, this.al - g.al) > 440 || Math.abs(this.al - g.al) >= 60)) {
                g = null;
            }
            if (ci != null && (ci.aB <= 0 || !this.av || ci.ak - this.ak >= 0 || this.av || ci.ak - this.ak <= 0)) {
                ci = null;
            }
            if (g != null && g.ax == 11 && g.Z[19] == 1 && ((!this.av && g.ak - this.ak < 0) || (this.av && g.ak - this.ak > 0))) {
                g = null;
            }
            if (i.at != null && (((i.at.ax == 11 && i.at.P()) || k.h(this.ak - i.at.ak, this.al - i.at.al) > 440 || ((this.ak - i.at.ak < 0 && this.av) || ((this.ak - i.at.ak > 0 && !this.av) || this.W[3] < i.at.W[1]))) && this.S != 277 && this.S != 293 && this.S != 298)) {
                i.at = null;
            }
            if (g != null && ((g.ax == 11 && g.P()) || ((g.ax == 17 && g.P()) || ((g.ax == 73 && g.P()) || (g.ax == 9 && g.P()))))) {
                g = null;
            }
            if (g != null && ((this.S == 303 || this.S == 295) && r() && (g.ax != 4 || g.S != 30 || !g(g)))) {
                g = null;
            }
            if (g != null && g.ax == 4 && g.S != 30) {
                g = null;
            }
            if (g != null && g.ax == 58) {
                g = null;
            }
            if ((g == null || ((i.at == null && (J & 4) != 0) || ci == null)) && k.bd != null) {
                int iH = 440;
                for (int i2 = 0; i2 < k.be && k.bd != null; i2++) {
                    if (k.bd[i2] != null && (k.bd[i2].P & 32) == 0 && (k.bd[i2] == null || ((k.bd[i2].ax != 11 || !k.bd[i2].P()) && ((k.bd[i2].ax != 17 || !k.bd[i2].P()) && ((k.bd[i2].ax != 73 || !k.bd[i2].P()) && ((k.bd[i2].ax != 23 || !k.bd[i2].P()) && ((k.bd[i2].ax != 9 || !k.bd[i2].P()) && (k.bd[i2].ax != 4 || k.bd[i2].S == 30)))))))) {
                        if (k.bd[i2].ax == 11 || k.bd[i2].ax == 17 || k.bd[i2].ax == 23 || k.bd[i2].ax == 73 || k.bd[i2].ax == 29 || k.bd[i2].ax == 9 || ((k.bd[i2].ax == 4 && k.bd[i2].S == 30) || k.bd[i2].ax == 58)) {
                            if ((k.bd[i2].aB > 0 || k.bd[i2].ax == 4 || k.bd[i2].ax == 58) && ((I == 8 || (k.bd[i2].ax != 4 && k.bd[i2].ax != 58)) && (((this.av && k.bd[i2].ak - this.ak < 0) || ((!this.av && k.bd[i2].ak - this.ak > 0) || this.S == 268 || this.S == 291)) && k.h(this.ak - k.bd[i2].ak, this.al - k.bd[i2].al) < iH))) {
                                if (this.S == 268 || k.aS.S == 267 || this.S == 291) {
                                    g = k.bd[i2];
                                } else if (e(k.bd[i2])) {
                                    continue;
                                } else {
                                    int i3 = iH;
                                    int iH2 = k.h(this.ak - k.bd[i2].ak, this.al - k.bd[i2].al);
                                    iH = iH2;
                                    if (iH2 < i3) {
                                        g = null;
                                    }
                                    if (g == null || ci == null) {
                                        if (((k.bd[i2].ax != 11 && k.bd[i2].ax != 17 && k.bd[i2].ax != 23 && k.bd[i2].ax != 73) || i(k.bd[i2]) || (k.bd[i2].aA != 0 && k.bd[i2].aA != 2)) && (i(k.bd[i2]) || Math.abs(this.al - k.bd[i2].al) <= 20)) {
                                            if (g == null) {
                                                g = k.bd[i2];
                                            }
                                            if ((k.bd[i2].ax == 11 || k.bd[i2].ax == 17 || k.bd[i2].ax == 23 || k.bd[i2].ax == 73 || k.bd[i2].ax == 9 || k.bd[i2].ax == 29) && ci == null) {
                                                ci = k.bd[i2];
                                            }
                                            iH = k.h(this.ak - k.bd[i2].ak, this.al - k.bd[i2].al);
                                            if (k.bd[i2].ax == 72 && (J & 4) != 0 && k(this.S) && k.bd[i2].v() && k.bd[i2].Z[0] != 3 && (((this.av && k.bd[i2].ak - this.ak < 0) || (!this.av && k.bd[i2].ak - this.ak > 0)) && k.h(this.ak - k.bd[i2].ak, this.al - k.bd[i2].al) < 440 && ((k.bd[i2].Z[0] != 1 || (k.h(this.ak - k.bd[i2].ak, this.al - k.bd[i2].al) < k.bd[i2].Z[3] && this.W[1] >= k.bd[i2].W[3])) && this.W[3] >= k.bd[i2].W[1] && !e(k.bd[i2]) && i.at == null))) {
                                                i.at = k.bd[i2];
                                                return;
                                            }
                                        }
                                    } else if (k.bd[i2].ax == 72) {
                                        continue;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void aA() {
        if (k.u(16388) || ((this.av && k.u(8264)) || (!this.av && k.u(4114)))) {
            i(92);
            a(8, 5, 17, 201);
            i.aK.av = this.av;
            i.aK.P = 512;
            i.aK.N = this.ak << 8;
            i.aK.O = this.al << 8;
            i.aK.ak = this.ak;
            i.aK.al = this.al;
            i iVar = i.aK;
            i iVar2 = i.aK;
            i iVar3 = i.aK;
            i.aK.aj = 0;
            iVar3.ai = 0;
            iVar2.ah = 0;
            iVar.ag = 0;
            i.aK.t();
            k.b(i.aK);
        }
    }

    private void aB() {
        int iA;
        if (g == null || k.z[10] == null || (iA = k.z[10].a(41, this.K)) == 0) {
            return;
        }
        if (iA > this.cN) {
            this.cN++;
            return;
        }
        this.K++;
        this.cN = 0;
        int iB = k.z[10].b(41);
        if (this.S == 295) {
            iB = k.z[10].b(29);
        }
        if (this.K >= iB) {
            this.K = 0;
        }
        this.L = (g.W[0] + g.W[2]) >> 1;
        this.M = ((g.W[1] + g.W[3]) >> 1) - 10;
    }

    private static void aC() {
        if (k.aw == 20) {
            k.aw = 0;
        }
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARN: Removed duplicated region for block: B:142:0x041f  */
    /* JADX WARN: Removed duplicated region for block: B:147:0x043d  */
    /* JADX WARN: Removed duplicated region for block: B:149:0x0443  */
    /* JADX WARN: Removed duplicated region for block: B:151:0x044c  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public final void n() {
        boolean z2 = true;
        boolean z3 = true;
        k.aI++;
        B();
        if (i.be) {
            k.X = 0;
            if (r() || !v()) {
                k.l(12);
                return;
            }
            return;
        }
        if (i.bh > 0) {
            i.bh--;
        }
        if (k.aH < 0) {
            k.aG--;
        }
        if (k.aG <= 0) {
            k.aG = 6;
            k.aE--;
        }
        if (k.aE < 0) {
            k.aE = 0;
        } else if (k.aE <= 25 && k.aE < 25 && k.aF == 0 && k.aH < 0 && (i.aJ == 0 || k.W != 0)) {
            if (k.W != 0) {
                i.aJ = k.W;
                k.W = 0;
            } else {
                i.aJ = k.X;
            }
            k.X = i.aJ >> 1;
        }
        if (k.aE <= 0 && k.aH < 0) {
            if (this.S != 24) {
                i.bB = true;
                i.bC = false;
                i.bD = false;
                i.bF = 90;
                i.bE = 999;
                i.bG = -1;
                i(24);
            }
            x[1] = 0;
        } else if (g()) {
            i(2);
        }
        if (i.bB) {
            this.aq = -1;
            this.ar = -1;
        }
        switch (this.S) {
            case 0:
                if (this.S == 0 && r()) {
                    this.T = this.aa.b(this.S) - 2;
                }
                if (!i.bi || E) {
                    if (k.Q >= 230) {
                        z3 = false;
                        if (i.aH) {
                            this.ah = k.Y * i.aI;
                            break;
                        } else {
                            this.ah = k.Y;
                            break;
                        }
                    }
                } else {
                    aC();
                    if (i.aH && k.Q >= 230) {
                        this.ah = k.Y << 1;
                        z3 = false;
                    }
                    if (!i.bB && k.v(1)) {
                        k.A(28);
                    }
                    if (this.S == 18) {
                        this.av = this.ak > k.O + 200;
                        i(20);
                        i.bk = true;
                    } else if (this.S == 17) {
                        i(4);
                    }
                    boolean z4 = (this.S == 3 || this.S == 0 || this.S == 18 || this.S == 17 || this.S == 20) ? false : true;
                    if (!i.bk && this.Q != 18 && k.aI >= 10 && z4) {
                        k.aI = 0;
                        e(false);
                    }
                    if (k.u(4112)) {
                        if (this.ag > -2048) {
                            this.ag -= 768;
                        }
                        if (this.ag < -2048) {
                            this.ag = -2048;
                        }
                        z2 = false;
                        if (z4) {
                            if (k.bD >= 15) {
                                i(30);
                            } else {
                                i(33);
                            }
                        }
                        this.av = false;
                    }
                    if (k.u(8256)) {
                        if (this.ag < 2048) {
                            this.ag += 768;
                        }
                        if (this.ag > 2048) {
                            this.ag = 2048;
                        }
                        z2 = false;
                        if (z4) {
                            if (k.bD >= 15) {
                                i(31);
                            } else {
                                i(32);
                            }
                        }
                        this.av = false;
                    }
                    if (k.u(16388) && k.Q > 117) {
                        if (this.ah > (-2048) + k.Y) {
                            this.ah -= 768;
                        }
                        if (this.ah < (-2048) + k.Y) {
                            this.ah = (-2048) + k.Y;
                        }
                        z3 = false;
                        this.av = false;
                        if (z4) {
                            i(4);
                        }
                    }
                    if (k.u(33024) && k.Q < 230) {
                        if (this.ah < 2048 + k.Y) {
                            this.ah += 768;
                        }
                        if (this.ah > 2048 + k.Y) {
                            this.ah = 2048 + k.Y;
                        }
                        z3 = false;
                        this.av = false;
                        if (z4) {
                            i(5);
                        }
                    }
                    if (k.bB == 0 && k.bC == 0 && r() && z4) {
                        this.av = false;
                        i(4);
                    }
                    if (this.aq != -1 && this.ar != -1) {
                        this.ah = 0;
                        this.ag = 0;
                        z3 = false;
                        z2 = false;
                        if (this.aq < this.ak && !this.bb) {
                            this.ak -= 10;
                            if (z4) {
                                int i2 = k.bD;
                                k.bD = i2 + 1;
                                if (i2 >= 15) {
                                    i(30);
                                } else {
                                    i(33);
                                }
                            }
                        } else if (this.aq > this.ak && !this.bc) {
                            this.ak += 10;
                            if (z4) {
                                int i3 = k.bD;
                                k.bD = i3 + 1;
                                if (i3 >= 15) {
                                    i(31);
                                } else {
                                    i(32);
                                }
                            }
                        }
                        this.ar += k.X;
                        this.al += k.X;
                        if (this.ar < this.al) {
                            this.al -= 10;
                            if (z4) {
                                i(4);
                            }
                        } else if (this.ar > this.al) {
                            this.al += 10;
                            if (z4) {
                                i(5);
                            }
                        }
                        if ((this.aq < this.ak && this.aT >= 10) || (this.aq > this.ak && this.aU >= 10)) {
                            this.aq = this.ak;
                        }
                        if ((this.ar < this.al && k.Q <= 117) || (this.ar > this.al && k.Q >= 230)) {
                            this.ar = this.al;
                        }
                        if (Math.abs(this.aq - this.ak) <= 10) {
                            this.ak = this.aq;
                        }
                        if (Math.abs(this.ar - this.al) <= 10) {
                            this.al = this.ar;
                        }
                        if (this.ak == this.aq && this.al == this.ar) {
                            this.aq = -1;
                            this.ar = -1;
                            break;
                        }
                    }
                }
                break;
            case 1:
            case 9:
            case 12:
            case 27:
                k.aw = 20;
                if (r()) {
                    if (i.bB) {
                        i.bB = false;
                        i.bG = -1;
                        k.aS.az = 202;
                    }
                    aC();
                    i(4);
                    break;
                }
                break;
            case 2:
            case 24:
                this.ag >>= 1;
                this.ah >>= 1;
                z2 = false;
                z3 = false;
                if (r() || !v()) {
                    k.l(12);
                    break;
                }
                break;
            case 3:
                if (r()) {
                    i(4);
                    break;
                }
                this.av = false;
                if (this.S == 0) {
                    this.T = this.aa.b(this.S) - 2;
                    break;
                }
                if (!i.bi) {
                    if (k.Q >= 230) {
                    }
                    break;
                }
                break;
            case 4:
            case 5:
            case 17:
            case 18:
                if (!i.bi) {
                }
                break;
            case 20:
                if (i.bk) {
                    if (this.T == 5) {
                        for (int i4 = 0; i4 < 5; i4++) {
                            e(true);
                        }
                    }
                    i.bB = true;
                    i.bE = 999;
                    if (this.T <= 10) {
                        i.bC = true;
                        i.bD = true;
                    } else {
                        i.bC = false;
                        i.bD = false;
                        i.bG = 100;
                    }
                }
                if (r()) {
                    i.bB = false;
                    i.bG = -1;
                    i.bk = false;
                    i(4);
                    break;
                }
                break;
            case 21:
                k.aw = 20;
                if (r()) {
                    i.bB = true;
                    i.bC = true;
                    i.bD = true;
                    i.bE = 999;
                    i(22);
                }
                if (k.u(4112)) {
                    if (this.ag > -2048) {
                        this.ag -= 768;
                    }
                    if (this.ag < -2048) {
                        this.ag = -2048;
                    }
                    z2 = false;
                    this.av = false;
                }
                if (k.u(8256)) {
                    if (this.ag < 2048) {
                        this.ag += 768;
                    }
                    if (this.ag > 2048) {
                        this.ag = 2048;
                    }
                    z2 = false;
                    this.av = false;
                    break;
                }
                break;
            case 22:
                k.aw = 20;
                if (r()) {
                    i.bB = true;
                    i.bC = false;
                    i.bD = false;
                    i.bE = 999;
                    i.bG = 100;
                    i(23);
                }
                if (k.u(4112)) {
                    if (this.ag > -2048) {
                        this.ag -= 768;
                    }
                    if (this.ag < -2048) {
                        this.ag = -2048;
                    }
                    z2 = false;
                    this.av = false;
                }
                if (k.u(8256)) {
                    if (this.ag < 2048) {
                        this.ag += 768;
                    }
                    if (this.ag > 2048) {
                        this.ag = 2048;
                    }
                    z2 = false;
                    this.av = false;
                    break;
                }
                break;
            case 23:
                k.aw = 20;
                if (r()) {
                    i.bB = true;
                    i.bC = false;
                    i.bD = false;
                    i.bE = 999;
                    i(25);
                }
                if (k.u(4112)) {
                    if (this.ag > -2048) {
                        this.ag -= 768;
                    }
                    if (this.ag < -2048) {
                        this.ag = -2048;
                    }
                    z2 = false;
                    this.av = false;
                }
                if (k.u(8256)) {
                    if (this.ag < 2048) {
                        this.ag += 768;
                    }
                    if (this.ag > 2048) {
                        this.ag = 2048;
                    }
                    z2 = false;
                    this.av = false;
                    break;
                }
                break;
            case 25:
                k.aw = 20;
                if (r()) {
                    i.bB = false;
                    i.bG = -1;
                    i(4);
                    break;
                }
                break;
            case 26:
                k.aw = 20;
                if (r()) {
                    this.T = this.aa.b(this.S) - 2;
                    break;
                }
                break;
            case 28:
                k.aw = 20;
                if (r()) {
                    i(29);
                    break;
                }
                break;
            case 29:
                k.aw = 20;
                if (r()) {
                    i(26);
                    break;
                }
                break;
            case 30:
            case 31:
            case 32:
            case 33:
                this.av = false;
                if (this.S == 0) {
                }
                if (!i.bi) {
                }
                break;
        }
        if (z2) {
            if (this.ag > 768) {
                this.ag -= 768;
            } else if (this.ag < -768) {
                this.ag += 768;
            } else {
                this.ag = 0;
            }
        }
        if (z3) {
            if (this.ah > 768 + k.Y) {
                this.ah -= 768;
            } else if (this.ah < (-768) + k.Y) {
                this.ah += 768;
            } else {
                this.ah = k.Y;
            }
        }
        if (this.ad != null) {
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
        }
    }

    private void e(boolean z2) {
        if (z2) {
            a(24, 40, 7, this.az - 1);
        } else {
            a(24, 40, 6, this.az - 1);
        }
        i.aK.av = false;
        i.aK.ak = this.ak;
        i.aK.al = this.W[1] - 3;
        i.aK.am = this.ak;
        i.aK.ao = this.ak;
        i.aK.an = i.aK.al;
        if (z2) {
            i.aK.ao = k.O + j.a(70, 330);
            i.aK.ap = k.P + j.a(110, 130);
            i.aK.ag = ((i.aK.ao - i.aK.am) << 8) / 10;
            i.aK.ah = (((i.aK.ap - i.aK.an) << 8) / 10) + k.Y;
            i.aK.aC = 10;
        } else {
            i.aK.ap = i.aK.al - 100;
            i.aK.ag = 0;
            i.aK.ah = (-3840) + k.X;
        }
        i.aK.t();
        i.aK.P |= 16;
        i.aK.af = this;
        i.aK.bR = false;
        k.b(i.aK);
    }

    final boolean o() {
        if (this.aZ) {
            return true;
        }
        if (a == null || a.ax == 43) {
            return false;
        }
        return a.ax == 51 || a.ax == 15 || a.ax == 43;
    }
}
