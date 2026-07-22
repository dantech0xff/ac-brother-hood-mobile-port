package defpackage;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:b.class */
public final class b {
    static short[][] b;
    private static int[] A;
    private static int[] B;
    static b[][] c;
    private static int H;
    public static int d;
    public static int e;
    private int J;
    private int K;
    private int L;
    private int N;
    private short[][] Q;
    private short R;
    private static short[] U;
    static int[] g;
    private int ab;
    private short[] ac;
    private short[] ad;
    private short[] ae;
    private short[] af;
    private short[] ag;
    private short[] ah;
    private byte[] ai;
    private short[] aj;
    private byte[] ak;
    private short[] al;
    private byte[] am;
    private short[] an;
    private short[] ao;
    private byte[] ap;
    private byte[] aq;
    private short[] ar;
    private short[] as;
    private byte[] at;
    private byte[] au;
    public short[] h;
    private byte[] av;
    private byte[] aw;
    private short[] ax;
    private short[] ay;
    byte[] i;
    private short[][] az;
    private int aA;
    private byte[] aB;
    private int[] aC;
    private int aD;
    private short[][] aE;
    int[][] j;
    private byte[] aF;
    public int k;
    private int aG;
    private int aH;
    private boolean aI;
    private boolean aJ;
    private int aK;
    private short aL;
    private int aM;
    private int aN;
    private int[][][] aO;
    private Image[][] aP;
    private byte[] aU;
    private byte[] aV;
    private int[] aW;
    private static int aX;
    private static int aY;
    private static int aZ;
    private static int ba;
    private static byte[] n = {-119, 80, 78, 71, 13, 10, 26, 10};
    private static byte[] o = {73, 72, 68, 82};
    private static byte[] p = {80, 76, 84, 69};
    private static byte[] q = {116, 82, 78, 83};
    private static byte[] r = {73, 68, 65, 84};
    private static byte[] s = {8, 3, 0, 0, 0};
    private static byte[] t = {0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
    private static byte[] u = {120, -100, 1};
    private static int[] v = new int[256];
    public static boolean a = false;
    private static boolean w = false;
    private static int x = 0;
    private static int y = 0;
    private static final boolean E = false;
    private static int[] F = null;
    private static int V = -1;
    private static int W = -1;
    private static int X = -1;
    private static int Y = -1;
    private static int Z = -1;
    private static int aa = 0;
    private static int[] aQ = {0, 2, 1, 3, 5, 7, 4, 6};
    private static int[] aR = {1, 0, 3, 2, 6, 7, 4, 5};
    private static int[] aS = {2, 3, 0, 1, 5, 4, 7, 6};
    private static int[] aT = {4, 5, 6, 7, 3, 2, 1, 0};
    private static int[] bb = new int[4];
    private static boolean bc = true;
    static boolean l = false;
    static char m = '|';
    private int z = -1;
    private int C = -1;
    private int D = -1;
    private boolean G = false;
    private boolean I = false;
    private int M = 0;
    private boolean O = false;
    boolean f = false;
    private byte[] P = null;
    private int S = -1;
    private int[] T = new int[4];
    private int bd = -1;

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v520, types: [short[]] */
    /* JADX WARN: Type inference failed for: r0v528, types: [short[]] */
    /* JADX WARN: Type inference failed for: r0v562, types: [short[]] */
    /* JADX WARN: Type inference failed for: r1v255, types: [int[], int[][]] */
    /* JADX WARN: Type inference failed for: r1v50, types: [short[], short[][]] */
    public final void a(byte[] bArr, int i) {
        int i2;
        int i3;
        int i4;
        int i5;
        int i6;
        int i7;
        if (bArr == null) {
            return;
        }
        try {
            if (bc) {
                System.gc();
            }
            int i8 = 0 + 1 + 1;
            int i9 = i8 + 1;
            int i10 = bArr[i8] & 255;
            int i11 = i9 + 1;
            int i12 = i10 + ((bArr[i9] & 255) << 8);
            int i13 = i11 + 1;
            int i14 = i12 + ((bArr[i11] & 255) << 16);
            int i15 = i13 + 1;
            this.aD = i14 + ((bArr[i13] & 255) << 24);
            int i16 = i15 + 1;
            int i17 = i16 + 1;
            this.ab = (short) ((bArr[i15] & 255) + ((bArr[i16] & 255) << 8));
            if (this.ab > 0) {
                if ((this.aD & 32) != 0) {
                    this.ac = new short[this.ab];
                    this.ad = new short[this.ab];
                }
                this.ae = new short[this.ab];
                this.af = new short[this.ab];
                int i18 = 0;
                int i19 = 0;
                short[][] sArr = null;
                this.aU = new byte[this.ab];
                this.aW = new int[this.ab];
                if ((this.aD & 4) != 0) {
                    this.aV = new byte[this.ab];
                }
                boolean z = false;
                boolean z2 = false;
                boolean z3 = false;
                for (int i20 = 0; i20 < this.ab; i20++) {
                    boolean z4 = false;
                    boolean z5 = false;
                    boolean z6 = false;
                    if ((bArr[i17] & 255) == 0) {
                        i17++;
                        this.aU[i20] = 0;
                        z = false;
                        z2 = true;
                        z3 = true;
                        if ((this.aD & 4) != 0) {
                            i17++;
                            this.aV[i20] = bArr[i17];
                        }
                    } else if ((bArr[i17] & 255) == 255) {
                        i17++;
                        this.aU[i20] = 1;
                        z = true;
                        z2 = false;
                        z3 = true;
                    } else if ((bArr[i17] & 255) == 254) {
                        i17++;
                        this.aU[i20] = 2;
                        z = true;
                        z2 = false;
                        z3 = true;
                    } else if ((bArr[i17] & 255) == 253) {
                        i17++;
                        this.aU[i20] = 5;
                        z = false;
                        z2 = false;
                        z3 = true;
                    } else if ((bArr[i17] & 255) == 252) {
                        i17++;
                        this.aU[i20] = 3;
                        z = true;
                        z2 = false;
                        z3 = true;
                        z4 = true;
                    } else if ((bArr[i17] & 255) == 251) {
                        i17++;
                        this.aU[i20] = 4;
                        z = true;
                        z2 = false;
                        z3 = true;
                        z4 = true;
                    } else if ((bArr[i17] & 255) == 250) {
                        i17++;
                        this.aU[i20] = 6;
                        z = true;
                        z2 = false;
                        z3 = false;
                        z5 = true;
                    } else if ((bArr[i17] & 255) == 249) {
                        i17++;
                        this.aU[i20] = 7;
                        z = true;
                        z2 = false;
                        z3 = false;
                        z5 = true;
                    } else if ((bArr[i17] & 255) == 248) {
                        i17++;
                        this.aU[i20] = 8;
                        z = true;
                        z2 = false;
                        z3 = true;
                        z5 = false;
                    } else if ((bArr[i17] & 255) == 247) {
                        i17++;
                        this.aU[i20] = 9;
                        z = true;
                        z6 = true;
                        z2 = false;
                        z3 = true;
                        z5 = false;
                    }
                    if (z) {
                        int i21 = i17;
                        int i22 = i17 + 1;
                        int i23 = i22 + 1;
                        int i24 = (bArr[i21] & 255) + ((bArr[i22] & 255) << 8);
                        int i25 = i23 + 1;
                        int i26 = i24 + ((bArr[i23] & 255) << 16);
                        i17 = i25 + 1;
                        this.aW[i20] = i26 + ((bArr[i25] & 255) << 24);
                    }
                    if (z6) {
                        if (sArr == null) {
                            sArr = new short[this.ab];
                        }
                        int i27 = i17;
                        int i28 = i17 + 1;
                        int i29 = i28 + 1;
                        int i30 = (bArr[i27] & 255) + ((bArr[i28] & 255) << 8);
                        int i31 = i29 + 1;
                        int i32 = i30 + ((bArr[i29] & 255) << 16);
                        int i33 = i32 + ((bArr[i31] & 255) << 24);
                        i17 = i31 + 1 + 1;
                        sArr[i20] = new short[]{(short) i33, (short) (i33 >> 16), bArr[r12]};
                        i18++;
                        i19 += 3;
                    }
                    if (z2 && (this.aD & 32) != 0) {
                        int i34 = i17;
                        int i35 = i17 + 1;
                        int i36 = i35 + 1;
                        this.ac[i20] = (short) ((bArr[i34] & 255) + ((bArr[i35] & 255) << 8));
                        int i37 = i36 + 1;
                        int i38 = bArr[i36] & 255;
                        i17 = i37 + 1;
                        this.ad[i20] = (short) (i38 + ((bArr[i37] & 255) << 8));
                    }
                    if (z3) {
                        if ((this.aD & 16) == 0) {
                            int i39 = i17;
                            int i40 = i17 + 1;
                            this.ae[i20] = (short) (bArr[i39] & 255);
                            i17 = i40 + 1;
                            this.af[i20] = (short) (bArr[i40] & 255);
                        } else {
                            int i41 = i17;
                            int i42 = i17 + 1;
                            int i43 = i42 + 1;
                            this.ae[i20] = (short) ((bArr[i41] & 255) + ((bArr[i42] & 255) << 8));
                            int i44 = i43 + 1;
                            int i45 = bArr[i43] & 255;
                            i17 = i44 + 1;
                            this.af[i20] = (short) (i45 + ((bArr[i44] & 255) << 8));
                        }
                    }
                    if (z4) {
                        if (sArr == null) {
                            sArr = new short[this.ab];
                        }
                        int i46 = i17;
                        int i47 = i17 + 1;
                        int i48 = i47 + 1;
                        int i49 = i48 + 1;
                        int i50 = bArr[i48] & 255;
                        i17 = i49 + 1;
                        sArr[i20] = new short[]{(short) ((bArr[i46] & 255) + ((bArr[i47] & 255) << 8)), (short) (i50 + ((bArr[i49] & 255) << 8))};
                        i18++;
                        i19 += 2;
                    }
                    if (z5) {
                        if (sArr == null) {
                            sArr = new short[this.ab];
                        }
                        int i51 = i17;
                        int i52 = i17 + 1;
                        int i53 = i52 + 1;
                        int i54 = i53 + 1;
                        int i55 = bArr[i53] & 255;
                        int i56 = i54 + 1;
                        int i57 = i56 + 1;
                        int i58 = bArr[i56] & 255;
                        int i59 = i57 + 1;
                        int i60 = i59 + 1;
                        int i61 = bArr[i59] & 255;
                        i17 = i60 + 1;
                        sArr[i20] = new short[]{(short) ((bArr[i51] & 255) + ((bArr[i52] & 255) << 8)), (short) (i55 + ((bArr[i54] & 255) << 8)), (short) (i58 + ((bArr[i57] & 255) << 8)), (short) (i61 + ((bArr[i60] & 255) << 8))};
                        i18++;
                        i19 += 4;
                    }
                }
                if (i18 > 0) {
                    this.ag = new short[i19];
                    this.ah = new short[i18 << 1];
                    int i62 = 0;
                    short s2 = 0;
                    for (short s3 = 0; s3 < this.ab; s3 = (short) (s3 + 1)) {
                        int i63 = (this.aU[s3] == 3 || this.aU[s3] == 4) ? 2 : (this.aU[s3] == 6 || this.aU[s3] == 7) ? 4 : this.aU[s3] == 9 ? 3 : -1;
                        if (i63 > 0) {
                            this.ah[i62 << 1] = s3;
                            this.ah[(i62 << 1) + 1] = s2;
                            for (int i64 = 0; i64 < i63; i64++) {
                                this.ag[s2] = sArr[s3][i64];
                                s2 = (short) (s2 + 1);
                            }
                            sArr[s3] = null;
                            i62++;
                        }
                    }
                }
            }
            int i65 = i17;
            int i66 = i65 + 1;
            int i67 = i66 + 1;
            int i68 = (short) ((bArr[i65] & 255) + ((bArr[i66] & 255) << 8));
            if (i68 > 0) {
                this.ap = new byte[i68];
                this.ar = new short[i68];
                this.as = new short[i68];
                this.aq = new byte[i68];
                if ((this.aD & 16384) != 0) {
                    this.at = new byte[i68];
                }
                for (int i69 = 0; i69 < i68; i69++) {
                    int i70 = i67;
                    int i71 = i67 + 1;
                    this.ap[i69] = bArr[i70];
                    if ((this.aD & 1024) == 0) {
                        this.ar[i69] = bArr[i71];
                        i7 = i71 + 1 + 1;
                        this.as[i69] = bArr[r12];
                    } else {
                        int i72 = i71 + 1;
                        int i73 = bArr[i71] & 255;
                        int i74 = i72 + 1;
                        this.ar[i69] = (short) (i73 + ((bArr[i72] & 255) << 8));
                        int i75 = i74 + 1;
                        int i76 = bArr[i74] & 255;
                        i7 = i75 + 1;
                        this.as[i69] = (short) (i76 + ((bArr[i75] & 255) << 8));
                    }
                    if ((this.aD & 16384) != 0) {
                        int i77 = i7;
                        i7++;
                        this.at[i69] = bArr[i77];
                    }
                    int i78 = i7;
                    i67 = i7 + 1;
                    this.aq[i69] = bArr[i78];
                }
            }
            int iA = i67;
            if ((this.aD & 32768) != 0) {
                int i79 = iA + 1;
                int i80 = bArr[iA] & 255;
                int i81 = i79 + 1;
                short s4 = (short) (i80 + ((bArr[i79] & 255) << 8));
                if ((this.aD & 1024) == 0) {
                    this.am = new byte[s4 << 2];
                    System.arraycopy(bArr, i81, this.am, 0, s4 << 2);
                    iA = i81 + (s4 << 2);
                } else {
                    this.an = new short[s4 << 2];
                    iA = a(bArr, i81, this.an, 0, s4 << 2, false);
                }
            }
            int i82 = iA;
            int i83 = iA + 1;
            int i84 = i83 + 1;
            int i85 = (short) ((bArr[i82] & 255) + ((bArr[i83] & 255) << 8));
            if (i85 > 0) {
                this.ai = new byte[i85];
                this.aj = new short[i85];
                if ((this.aD & 32768) != 0) {
                    this.ao = new short[i85 + 1];
                }
                short s5 = 0;
                for (int i86 = 0; i86 < i85; i86++) {
                    int i87 = i84;
                    int i88 = i84 + 1;
                    this.ai[i86] = bArr[i87];
                    int i89 = i88 + 1;
                    int i90 = bArr[i88] & 255;
                    i84 = i89 + 1;
                    this.aj[i86] = (short) (i90 + ((bArr[i89] & 255) << 8));
                    if ((this.aD & 32768) != 0 && (this.aD & 32768) != 0) {
                        this.ao[i86] = s5;
                        i84++;
                        s5 = (short) (s5 + bArr[i84]);
                    }
                }
                if ((this.aD & 32768) != 0) {
                    this.ao[this.ao.length - 1] = s5;
                }
                if ((this.aD & 4096) == 0) {
                    int i91 = i85 << 2;
                    if ((this.aD & 1024) == 0) {
                        this.ak = new byte[i91];
                        for (int i92 = 0; i92 < i91; i92++) {
                            int i93 = i84;
                            i84++;
                            this.ak[i92] = bArr[i93];
                        }
                    } else {
                        this.al = new short[i91];
                        for (int i94 = 0; i94 < i91; i94++) {
                            int i95 = i84;
                            int i96 = i84 + 1;
                            i84 = i96 + 1;
                            this.al[i94] = (short) ((bArr[i95] & 255) + ((bArr[i96] & 255) << 8));
                        }
                    }
                }
            }
            int i97 = i84;
            int i98 = i97 + 1;
            int i99 = i98 + 1;
            int i100 = (short) ((bArr[i97] & 255) + ((bArr[i98] & 255) << 8));
            if (i100 > 0) {
                this.av = new byte[i100];
                this.aw = new byte[i100];
                this.ax = new short[i100];
                this.ay = new short[i100];
                this.i = new byte[i100];
                for (int i101 = 0; i101 < i100; i101++) {
                    int i102 = i99;
                    int i103 = i99 + 1;
                    this.av[i101] = bArr[i102];
                    int i104 = i103 + 1;
                    this.aw[i101] = bArr[i103];
                    if ((this.aD & 262144) == 0) {
                        this.ax[i101] = bArr[i104];
                        i6 = i104 + 1 + 1;
                        this.ay[i101] = bArr[r12];
                    } else {
                        int i105 = i104 + 1;
                        int i106 = bArr[i104] & 255;
                        int i107 = i105 + 1;
                        this.ax[i101] = (short) (i106 + ((bArr[i105] & 255) << 8));
                        int i108 = i107 + 1;
                        int i109 = bArr[i107] & 255;
                        i6 = i108 + 1;
                        this.ay[i101] = (short) (i109 + ((bArr[i108] & 255) << 8));
                    }
                    int i110 = i6;
                    i99 = i6 + 1;
                    this.i[i101] = bArr[i110];
                }
            }
            int i111 = i99;
            int i112 = i111 + 1;
            int i113 = i112 + 1;
            int i114 = (short) ((bArr[i111] & 255) + ((bArr[i112] & 255) << 8));
            if (i114 > 0) {
                this.au = new byte[i114];
                this.h = new short[i114];
                for (int i115 = 0; i115 < i114; i115++) {
                    int i116 = i113;
                    int i117 = i113 + 1;
                    this.au[i115] = bArr[i116];
                    int i118 = i117 + 1;
                    int i119 = bArr[i117] & 255;
                    i113 = i118 + 1;
                    this.h[i115] = (short) (i119 + ((bArr[i118] & 255) << 8));
                }
            }
            int i120 = i113;
            if (this.ab <= 0) {
                if (bc) {
                    System.gc();
                    return;
                }
                return;
            }
            if ((this.aD & 16777216) != 0) {
                int i121 = i120;
                if ((this.aD & 16777216) != 0 && i121 < bArr.length) {
                    int i122 = i121 + 1;
                    int i123 = bArr[i121] & 255;
                    int i124 = i122 + 1;
                    short s6 = (short) (i123 + ((bArr[i122] & 255) << 8));
                    int i125 = i124 + 1;
                    this.k = bArr[i124] & 255;
                    int i126 = i125 + 1;
                    this.aG = bArr[i125] & 255;
                    if (this.aG == 0) {
                        this.aG = 256;
                    }
                    if (this.j == null) {
                        this.j = new int[16];
                    }
                    for (int i127 = 0; i127 < this.k; i127++) {
                        this.j[i127] = new int[this.aG];
                        if (s6 == -30584) {
                            for (int i128 = 0; i128 < this.aG; i128++) {
                                int i129 = i126;
                                int i130 = i126 + 1;
                                int i131 = i130 + 1;
                                int i132 = (bArr[i129] & 255) + ((bArr[i130] & 255) << 8);
                                int i133 = i131 + 1;
                                int i134 = i132 + ((bArr[i131] & 255) << 16);
                                i126 = i133 + 1;
                                int i135 = i134 + ((bArr[i133] & 255) << 24);
                                if ((i135 & (-16777216)) != -16777216) {
                                    this.aI = true;
                                    if ((i135 & (-16777216)) != 0) {
                                        this.aJ = true;
                                    }
                                }
                                this.j[i127][i128] = i135;
                            }
                        } else if (s6 == 21781) {
                            for (int i136 = 0; i136 < this.aG; i136++) {
                                int i137 = i126;
                                int i138 = i126 + 1;
                                i126 = i138 + 1;
                                short s7 = (short) ((bArr[i137] & 255) + ((bArr[i138] & 255) << 8));
                                int i139 = -16777216;
                                if ((s7 & Short.MIN_VALUE) != 32768) {
                                    i139 = 0;
                                    this.aI = true;
                                }
                                int i140 = i139 | ((s7 & 31744) << 9) | ((s7 & 992) << 6) | ((s7 & 31) << 3);
                                int i141 = i140;
                                if (i140 == 16253176) {
                                    i141 = 16711935;
                                }
                                this.j[i127][i136] = i141;
                            }
                        } else if (s6 == 25861) {
                            for (int i142 = 0; i142 < this.aG; i142++) {
                                int i143 = i126;
                                int i144 = i126 + 1;
                                i126 = i144 + 1;
                                short s8 = (short) ((bArr[i143] & 255) + ((bArr[i144] & 255) << 8));
                                int i145 = -16777216;
                                if ((s8 & 65535) == 63519) {
                                    i145 = 0;
                                    this.aI = true;
                                }
                                int i146 = i145 | ((s8 & 63488) << 8) | ((s8 & 2016) << 5) | ((s8 & 31) << 3);
                                int i147 = i146;
                                if (i146 == 16253176) {
                                    i147 = 16711935;
                                }
                                this.j[i127][i142] = i147;
                            }
                        }
                    }
                    int i148 = i126;
                    int i149 = i126 + 1;
                    i121 = i149 + 1;
                    this.aL = (short) ((bArr[i148] & 255) + ((bArr[i149] & 255) << 8));
                    if (this.aL == -22976) {
                        this.aI = true;
                        this.aJ = true;
                    }
                    if (this.aL == 25840 || this.aL == -22976) {
                        int i150 = this.aG - 1;
                        this.aM = 1;
                        this.aN = 0;
                        while (i150 != 0) {
                            i150 >>= 1;
                            this.aM <<= 1;
                            this.aN++;
                        }
                        this.aM--;
                    }
                    if (this.ab > 0) {
                        this.aC = new int[this.ab];
                        int i151 = 0;
                        for (int i152 = 0; i152 < this.ab; i152++) {
                            if ((this.aD & 128) != 0) {
                                int i153 = i121;
                                int i154 = i121 + 1;
                                int i155 = i154 + 1;
                                int i156 = (bArr[i153] & 255) + ((bArr[i154] & 255) << 8);
                                int i157 = i155 + 1;
                                int i158 = i156 + ((bArr[i155] & 255) << 16);
                                i4 = i157 + 1;
                                i5 = i158 + ((bArr[i157] & 255) << 24);
                            } else {
                                int i159 = i121;
                                int i160 = i121 + 1;
                                i4 = i160 + 1;
                                i5 = (short) ((bArr[i159] & 255) + ((bArr[i160] & 255) << 8));
                            }
                            this.aC[i152] = i151;
                            i151 += i5;
                            i121 = i4 + i5;
                        }
                        i121 = i121;
                        this.aB = new byte[i151];
                        for (int i161 = 0; i161 < this.ab; i161++) {
                            if ((this.aD & 128) != 0) {
                                int i162 = i121;
                                int i163 = i121 + 1;
                                int i164 = i163 + 1;
                                int i165 = (bArr[i162] & 255) + ((bArr[i163] & 255) << 8);
                                int i166 = i164 + 1;
                                int i167 = i165 + ((bArr[i164] & 255) << 16);
                                i2 = i166 + 1;
                                i3 = i167 + ((bArr[i166] & 255) << 24);
                            } else {
                                int i168 = i121;
                                int i169 = i121 + 1;
                                i2 = i169 + 1;
                                i3 = (short) ((bArr[i168] & 255) + ((bArr[i169] & 255) << 8));
                            }
                            System.arraycopy(bArr, i2, this.aB, this.aC[i161], i3);
                            i121 = i2 + i3;
                        }
                    }
                }
            }
            this.az = new short[16];
            this.aA = -1;
            if ((this.aD & 4096) != 0) {
                int iC = c();
                if (iC > 0) {
                    int i170 = 0;
                    if ((this.aD & 1024) == 0 && (this.aK & 4) == 0) {
                        this.ak = new byte[iC << 2];
                        for (int i171 = 0; i171 < iC; i171++) {
                            a(bb, i171, 0, 0, 0, false);
                            int i172 = i170;
                            int i173 = i170 + 1;
                            this.ak[i172] = (byte) bb[0];
                            int i174 = i173 + 1;
                            this.ak[i173] = (byte) bb[1];
                            int i175 = i174 + 1;
                            this.ak[i174] = (byte) (bb[2] - bb[0]);
                            i170 = i175 + 1;
                            this.ak[i175] = (byte) (bb[3] - bb[1]);
                        }
                    } else {
                        this.al = new short[iC << 2];
                        for (int i176 = 0; i176 < iC; i176++) {
                            a(bb, i176, 0, 0, 0, false);
                            int i177 = i170;
                            int i178 = i170 + 1;
                            this.al[i177] = (short) bb[0];
                            int i179 = i178 + 1;
                            this.al[i178] = (short) bb[1];
                            int i180 = i179 + 1;
                            this.al[i179] = (short) (bb[2] - bb[0]);
                            i170 = i180 + 1;
                            this.al[i180] = (short) (bb[3] - bb[1]);
                        }
                    }
                }
            } else if ((this.aK & 4) != 0) {
                this.aK &= -5;
            }
            if (bc) {
                System.gc();
            }
        } catch (Exception unused) {
        }
    }

    final void a(boolean z) {
        this.aF = null;
        this.aE = null;
        this.j = null;
        this.aB = null;
        this.aC = null;
        if (bc) {
            System.gc();
        }
    }

    final void a(int i, byte[] bArr) {
        if (this.az[i] == null) {
            this.az[i] = new short[this.ab];
            for (int i2 = 0; i2 < this.ab; i2++) {
                this.az[i][i2] = (short) i2;
            }
        }
        if (bArr == null) {
            return;
        }
        int i3 = 0;
        while (i3 < bArr.length) {
            int i4 = i3;
            int i5 = i3 + 1;
            int i6 = i5 + 1;
            int i7 = (bArr[i4] & 255) + ((bArr[i5] & 255) << 8);
            int i8 = i6 + 1;
            int i9 = bArr[i6] & 255;
            i3 = i8 + 1;
            this.az[i][i7] = (short) (i9 + ((bArr[i8] & 255) << 8));
        }
    }

    final void a(int i) {
        this.aA = i;
    }

    public final int a(int i, int i2) {
        return this.aw[this.h[i] + i2] & 255;
    }

    public final int b(int i) {
        return this.au[i] & 255;
    }

    private int m(int i) {
        return this.ae[i] & 65535;
    }

    private int n(int i) {
        return this.af[i] & 65535;
    }

    final int c(int i) {
        return this.ax[i];
    }

    final int b(int i, int i2) {
        return this.ax[this.h[i] + i2];
    }

    final int d(int i) {
        return this.ay[i];
    }

    final int c(int i, int i2) {
        return this.ay[this.h[i] + i2];
    }

    final int e(int i) {
        return ((this.aD & 1024) == 0 && (this.aK & 4) == 0) ? this.ak[(i << 2) + 2] & 255 : this.al[(i << 2) + 2] & 65535;
    }

    final int f(int i) {
        return ((this.aD & 1024) == 0 && (this.aK & 4) == 0) ? this.ak[(i << 2) + 3] & 255 : this.al[(i << 2) + 3] & 65535;
    }

    final int g(int i) {
        return ((this.aD & 1024) == 0 && (this.aK & 4) == 0) ? this.ak[i << 2] : this.al[i << 2];
    }

    final int h(int i) {
        return ((this.aD & 1024) == 0 && (this.aK & 4) == 0) ? this.ak[(i << 2) + 1] : this.al[(i << 2) + 1];
    }

    private int i(int i, int i2) {
        return this.ar[this.aj[i]];
    }

    private int j(int i, int i2) {
        return this.as[this.aj[0] + i2];
    }

    final int d(int i, int i2) {
        int i3 = this.h[i] + i2;
        return (this.av[i3] & 255) | ((this.i[i3] & 192) << 2);
    }

    private void a(int[] iArr, int i, int i2, int i3, int i4, boolean z) {
        aX = Integer.MAX_VALUE;
        aY = Integer.MAX_VALUE;
        aZ = Integer.MIN_VALUE;
        ba = Integer.MIN_VALUE;
        aa = 1;
        a(null, i, i2, i3, i4, 0, 0);
        aa = 0;
        if (z) {
            iArr[0] = aY;
            iArr[1] = j.e - aZ;
            iArr[2] = ba;
            iArr[3] = j.e - aX;
        }
    }

    final void a(int[] iArr, int i, int i2, int i3, int i4) {
        iArr[0] = 0;
        iArr[1] = 0;
        iArr[2] = 0;
        iArr[3] = 0;
        if ((i4 & 4) == 0) {
            iArr[2] = iArr[2] + m(i);
            iArr[3] = iArr[3] + n(i);
        } else {
            iArr[2] = iArr[2] + n(i);
            iArr[3] = iArr[3] + m(i);
        }
    }

    final int a() {
        if (this.au == null) {
            return 0;
        }
        return this.au.length;
    }

    private int c() {
        if (this.ai == null) {
            return 0;
        }
        return this.ai.length;
    }

    private boolean o(int i) {
        return (this.aC == null || this.aB == null) ? false : true;
    }

    final void a(int i, int i2, int[] iArr, int i3) {
        if (this.ao == null || iArr == null) {
            return;
        }
        short s2 = this.ao[i];
        int i4 = this.ao[i + 1] - s2;
        if (i4 <= 0 || i2 >= i4) {
            iArr[0] = 0;
            iArr[1] = 0;
            iArr[2] = 0;
            iArr[3] = 0;
            return;
        }
        int i5 = (s2 + i2) << 2;
        if ((this.aD & 1024) != 0) {
            if (this.an != null) {
                iArr[0] = this.an[i5];
                iArr[1] = this.an[i5 + 1];
                iArr[2] = this.an[i5 + 2] & 65535;
                iArr[3] = this.an[i5 + 3] & 65535;
            }
        } else if (this.am != null) {
            iArr[0] = this.am[i5];
            iArr[1] = this.am[i5 + 1];
            iArr[2] = this.am[i5 + 2] & 255;
            iArr[3] = this.am[i5 + 3] & 255;
        }
        if ((i3 & 1) != 0) {
            iArr[0] = (-iArr[0]) - iArr[2];
        }
        if ((i3 & 2) != 0) {
            iArr[1] = (-iArr[1]) - iArr[3];
        }
    }

    final void a(int i, int i2, int i3, int[] iArr, int i4) {
        a(i, i2, i3, iArr, i4, false);
    }

    final void a(int i, int i2, int i3, int[] iArr, int i4, boolean z) {
        int i5 = this.h[i] + i2;
        a((this.av[i5] & 255) | ((this.i[i5] & 192) << 2), i3, iArr, i4 ^ (this.i[i5] & 15));
        if (z) {
            iArr[0] = iArr[0] + this.ax[i5];
            iArr[1] = iArr[1] + this.ay[i5];
        }
    }

    public static int[] a(int[] iArr) {
        if (g == null) {
            g = new int[40960];
        }
        return g;
    }

    final void b(boolean z) {
    }

    public final void a(Graphics graphics, int i, int i2, int i3, int i4, int i5, int i6, int i7) {
        int i8 = this.h[i] + i2;
        int i9 = (this.av[i8] & 255) | ((this.i[i8] & 192) << 2);
        int i10 = (i5 & 1) != 0 ? i6 + this.ax[i8] : i6 - this.ax[i8];
        int i11 = (i5 & 2) != 0 ? i7 + this.ay[i8] : i7 - this.ay[i8];
        a(graphics, i9, i3 - i10, i4 - i11, i5 ^ (this.i[i8] & 15), i10, i11);
    }

    final void a(Graphics graphics, int i, int i2, int i3, int i4, int i5, int i6) {
        int i7 = this.ai[i] & 255;
        if (i7 > 0) {
            for (int i8 = 0; i8 < i7; i8++) {
                int i9 = this.aj[i] + i8;
                int i10 = this.aq[i9] & 255;
                int i11 = (this.ap[i9] & 255) | ((i10 & 192) << 2);
                if ((this.aD & 16384) != 0) {
                    this.aH = this.at[i9] & 255;
                }
                if ((i10 & 16) == 0 && this.aA >= 0) {
                    i11 = this.az[this.aA][i11];
                }
                int iM = 0;
                int iN = 0;
                if ((i10 & 16) == 0) {
                    iM = m(i11);
                    iN = n(i11);
                }
                int i12 = i4 ^ i10;
                if ((i12 & 4) != 0) {
                    int i13 = iM;
                    iM = iN;
                    iN = i13;
                }
                int i14 = (i4 & 1) != 0 ? -(this.ar[i9] + iM) : this.ar[i9];
                int i15 = (i4 & 2) != 0 ? -(this.as[i9] + iN) : this.as[i9];
                int i16 = i2 + i14;
                int i17 = i3 + i15;
                if ((i12 & 16) != 0) {
                    a(graphics, i11, i16, i17, i12 & 15, i5, i6);
                } else {
                    a(graphics, i11, i16, i17, i12 & 15);
                }
            }
        }
    }

    public final void a(int i, int i2, int i3, int i4) {
        int[] iArrR;
        p(i);
        if (this.ab == 0) {
            return;
        }
        int i5 = this.ab - 1;
        if ((this.aD & 16777224) != 0) {
            int i6 = this.aH;
            this.aH = i;
            if (bc) {
                System.gc();
            }
            for (int i7 = 0; i7 <= i5; i7++) {
                if (this.aU[i7] == 0) {
                    int iM = m(i7);
                    int iN = n(i7);
                    if (iM > 0 && iN > 0 && (iArrR = r(i7)) != null) {
                        int i8 = i7;
                        int i9 = iM * iN;
                        boolean z = false;
                        int i10 = 0;
                        while (true) {
                            if (i10 >= i9) {
                                break;
                            }
                            if ((iArrR[i10] & (-16777216)) != -16777216) {
                                z = true;
                                break;
                            }
                            i10++;
                        }
                        this.aP[i][i8] = j.a(a(iArrR, iN, iM, 4), iN, iM, z);
                    }
                }
            }
            if (bc) {
                System.gc();
            }
            this.aH = i6;
        }
        if (bc) {
            System.gc();
        }
    }

    /* JADX WARN: Type inference failed for: r1v4, types: [javax.microedition.lcdui.Image[], javax.microedition.lcdui.Image[][]] */
    private void p(int i) {
        if (this.aP == null) {
            this.aP = new Image[this.k];
        }
        if (this.aP[i] == null) {
            this.aP[i] = new Image[this.ab];
        }
    }

    final void e(int i, int i2) {
        if (this.aP == null || i >= this.aP.length) {
            return;
        }
        if (i2 == -1) {
            this.aP[i] = null;
        } else if (this.aP[i] != null) {
            this.aP[i][i2] = null;
        }
    }

    private int q(int i) {
        if (this.ah == null) {
            return -1;
        }
        for (int i2 = 0; i2 < this.ah.length; i2 += 2) {
            if (this.ah[i2] == i) {
                return this.ah[i2 + 1];
            }
        }
        return -1;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v326, types: [int] */
    /* JADX WARN: Type inference failed for: r0v328, types: [int] */
    /* JADX WARN: Type inference failed for: r0v330, types: [int] */
    /* JADX WARN: Type inference failed for: r0v332, types: [int] */
    /* JADX WARN: Type inference failed for: r1v60, types: [javax.microedition.lcdui.Image[], javax.microedition.lcdui.Image[][]] */
    public final void a(Graphics graphics, int i, int i2, int i3, int i4) {
        if ((i4 & 1) == 0 || (i4 & 2) == 0) {
            if ((i4 & 1) != 0) {
                i4 = (i4 & (-2)) | 2;
            } else if ((i4 & 2) != 0) {
                i4 = (i4 & (-3)) | 1;
            }
        }
        int i5 = j.e - i3;
        int i6 = this.af[i];
        if ((i4 & 7) > 3) {
            i6 = this.ae[i];
        }
        if (i6 < 0) {
            i6 += 255;
        }
        int iAbs = i5 - i6;
        int iAbs2 = i2;
        if (this.aA >= 0) {
            i = this.az[this.aA][i];
        }
        int iN = n(i);
        int iM = m(i);
        if ((i4 & 4) != 0) {
            iN = iM;
            iM = iN;
        }
        if (this.aU[i] == 0 || graphics == null) {
            if (aa == 1) {
                if (this.aU[i] == 5) {
                    iAbs -= iN >> 1;
                    iAbs2 -= iM >> 1;
                }
                if (iAbs < aX) {
                    aX = iAbs;
                }
                if (iAbs2 < aY) {
                    aY = iAbs2;
                }
                if (iAbs + iN > aZ) {
                    aZ = iAbs + iN;
                }
                if (iAbs2 + iM > ba) {
                    ba = iAbs2 + iM;
                    return;
                }
                return;
            }
            if (iN <= 0 || iM <= 0) {
                return;
            }
            if (j.e(graphics)) {
                int iA = j.a(graphics);
                int iB = j.b(graphics);
                int iC = j.c(graphics);
                int iD = j.d(graphics);
                if (iAbs + iN < iA || iAbs2 + iM < iB || iAbs >= iA + iC || iAbs2 >= iB + iD) {
                    return;
                }
            }
            int[] iArr = null;
            Image image = null;
            if ((this.aD & 16777224) != 0) {
                if (this.aP != null && this.aP[this.aH] != null) {
                    image = this.aP[this.aH][i];
                }
                if (this.aO != null && this.aO[this.aH] != null) {
                    iArr = this.aO[this.aH][i];
                }
                boolean z = this.aI;
                if (image != null) {
                    int iM2 = m(i);
                    int iN2 = n(i);
                    a(i, image);
                    Image image2 = image;
                    int i7 = aQ[i4 & 7];
                    if (image2 != null) {
                        if (aQ[i4 & 7] == 0) {
                            graphics.drawImage(image, iAbs, iAbs2, 0);
                            return;
                        } else {
                            graphics.drawRegion(image, 0, 0, iN2, iM2, aQ[i4 & 7], iAbs, iAbs2, 0);
                            return;
                        }
                    }
                    return;
                }
                int[] iArr2 = iArr;
                int[] iArrR = iArr2;
                if (iArr2 == null) {
                    iArrR = r(i);
                }
                if (iArrR == null) {
                    return;
                }
                if (this.z < 0) {
                    if ((this.aK & 8) != 0) {
                        j.a(graphics, iArrR, 0, iM, iAbs2, i3, iM, iN, z, z, i4, -1, false);
                        return;
                    } else {
                        j.a(graphics, iArrR, 0, iM, iAbs2, i3, iM, iN, z, z, i4, -1, true);
                        return;
                    }
                }
                if (this.aP == null) {
                    this.aP = new Image[this.k];
                    for (int i8 = 0; i8 < this.k; i8++) {
                        this.aP[i8] = new Image[this.ab];
                    }
                }
                int[] iArr3 = new int[iN * iM];
                int i9 = iM;
                int i10 = iN;
                if ((i4 & 4) != 0) {
                    i9 = iN;
                    i10 = iM;
                }
                for (int i11 = 0; i11 < i9; i11++) {
                    for (int i12 = 0; i12 < i10; i12++) {
                        iArr3[((i10 - i12) - 1) + (i11 * i10)] = iArrR[i11 + (i9 * i12)];
                    }
                }
                Image imageA = (i4 & 4) != 0 ? j.a(iArr3, iM, iN, this.aI) : j.a(iArr3, iN, iM, this.aI);
                a(i, imageA);
                Image image3 = imageA;
                int i13 = aQ[i4 & 7];
                if (image3 != null) {
                    if (aQ[i4 & 7] == 0) {
                        graphics.drawImage(imageA, iAbs, iAbs2, 0);
                        return;
                    } else if ((i4 & 4) != 0) {
                        graphics.drawRegion(imageA, 0, 0, iM, iN, aQ[i4 & 7], iAbs, iAbs2, 0);
                        return;
                    } else {
                        graphics.drawRegion(imageA, 0, 0, iN, iM, aQ[i4 & 7], iAbs, iAbs2, 0);
                        return;
                    }
                }
                return;
            }
            return;
        }
        graphics.setColor(this.aW[i]);
        switch (this.aU[i]) {
            case 1:
                j.c(j.a, i2, i3, iM - 1, iN - 1);
                break;
            case 2:
                int i14 = iN;
                int i15 = iM;
                if ((this.aW[i] & (-16777216)) != -16777216 && (this.aW[i] & (-16777216)) != 0) {
                    j.h(this.aW[i]);
                    j.d(graphics, iAbs2, i3, i15, i14);
                    break;
                } else {
                    j.b(graphics, iAbs2, i3, i15, i14);
                    break;
                }
            case 3:
            case 4:
                int iQ = q(i);
                if (iQ != -1) {
                    short s2 = this.ag[iQ];
                    short s3 = this.ag[iQ + 1];
                    if ((i4 & 1) != 0) {
                        s2 = 90 - s2;
                    }
                    if ((i4 & 2) != 0) {
                        s2 = -s2;
                        s3 = -s3;
                    }
                    if ((i4 & 4) != 0) {
                        s2 -= 90;
                    }
                    if (this.aU[i] == 3) {
                        graphics.drawArc(iAbs, iAbs2, iN, iM, s2, s3);
                        break;
                    } else {
                        graphics.fillArc(iAbs, iAbs2, iN, iM, s2, s3);
                        break;
                    }
                }
                break;
            case 6:
            case 7:
                int iQ2 = q(i);
                if (iQ2 != -1) {
                    int i16 = this.ag[iQ2];
                    int i17 = this.ag[iQ2 + 1];
                    int i18 = this.ag[iQ2 + 2];
                    int i19 = this.ag[iQ2 + 3];
                    if ((i4 & 1) != 0) {
                        i16 = -i16;
                        i18 = -i18;
                        iAbs += Math.abs(i16) > Math.abs(i18) ? Math.abs(i16) : Math.abs(i18);
                    }
                    if ((i4 & 2) != 0) {
                        i17 = -i17;
                        i19 = -i19;
                        iAbs2 += Math.abs(i17) > Math.abs(i19) ? Math.abs(i17) : Math.abs(i19);
                    }
                    if ((i4 & 4) != 0) {
                        int i20 = iAbs2 - i2;
                        iAbs2 = i2 + (iAbs - iAbs);
                        iAbs = (iAbs - i20) + (Math.abs(i17) > Math.abs(i19) ? Math.abs(i17) : Math.abs(i19));
                        int i21 = i16;
                        i16 = -i17;
                        i17 = i21;
                        int i22 = i18;
                        i18 = -i19;
                        i19 = i22;
                    }
                    if (this.aU[i] == 6) {
                        graphics.drawLine(iAbs, iAbs2, iAbs + i16, iAbs2 + i17);
                        graphics.drawLine(iAbs + i16, iAbs2 + i17, iAbs + i18, iAbs2 + i19);
                        graphics.drawLine(iAbs, iAbs2, iAbs + i18, iAbs2 + i19);
                        break;
                    } else {
                        j.a(i2, i3, i2 + i16, i3 + i17, i2 + i18, i3 + i19);
                        break;
                    }
                }
                break;
            case 8:
                int i23 = iAbs;
                int i24 = iAbs + iN;
                int i25 = iAbs2;
                int i26 = iAbs2 + iM;
                if ((i4 & 1) != 0) {
                    i23 = i24;
                    i24 = i23;
                }
                if ((i4 & 2) != 0) {
                    i25 = i26;
                    i26 = i2;
                }
                if ((i4 & 4) != 0) {
                    int i27 = i23;
                    i23 = i24;
                    i24 = i27;
                }
                j.a(graphics, i23, i25, i24, i26);
                break;
            case 9:
                int iQ3 = q(i);
                if (iQ3 != -1) {
                    int i28 = this.aW[i];
                    int i29 = (this.ag[iQ3] & 65535) | ((this.ag[iQ3 + 1] << 16) & (-65536));
                    short s4 = this.ag[iQ3 + 2];
                    int i30 = iN;
                    int i31 = iM;
                    if ((i4 & 1) != 0 && s4 < 2) {
                        i28 = i29;
                        i29 = i28;
                    }
                    if ((i4 & 2) != 0 && s4 > 1) {
                        int i32 = i28;
                        i28 = i29;
                        i29 = i32;
                    }
                    int i33 = s4 == 0 ? 4 : s4 == 1 ? 8 : s4 == 2 ? 16 : 32;
                    if ((i4 & 4) != 0) {
                        if (i33 == 4) {
                            i33 = 16;
                        } else if (i33 == 8) {
                            i33 = 32;
                        } else if (i33 == 16) {
                            i33 = 8;
                        } else if (i33 == 32) {
                            i33 = 4;
                        }
                    }
                    j.b(iAbs, iAbs2, i30, i31, i28, i29, i33);
                    break;
                }
                break;
        }
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [short[], short[][]] */
    /* JADX WARN: Type inference failed for: r0v3, types: [b[], b[][]] */
    public static void i(int i) {
        b = new short[i];
        c = new b[i];
        A = new int[i];
        B = new int[i];
    }

    public static void f(int i, int i2) {
        B[i] = i2;
        b[i] = new short[i2];
        c[i] = new b[i2];
        for (int i3 = 0; i3 < b[i].length; i3++) {
            b[i][i3] = -1;
        }
    }

    /* JADX WARN: Type inference failed for: r1v3, types: [javax.microedition.lcdui.Image[], javax.microedition.lcdui.Image[][]] */
    public final void j(int i) {
        this.z = i;
        if (this.aP == null) {
            this.aP = new Image[this.k];
            for (int i2 = 0; i2 < this.k; i2++) {
                this.aP[i2] = new Image[this.ab];
            }
        }
    }

    private final void a(int i, Object obj) {
        b bVar;
        if (this.z < 0 || this.aP[this.aH][i] != null) {
            return;
        }
        int i2 = A[this.z];
        short s2 = b[this.z][i2];
        int i3 = s2 >> 10;
        int i4 = s2 & 1023;
        if (s2 >= 0 && (bVar = c[this.z][i2]) != null) {
            bVar.aP[i3][i4] = null;
        }
        b[this.z][i2] = (short) ((i & 1023) + (this.aH << 10));
        c[this.z][i2] = this;
        A[this.z] = (A[this.z] + 1) % B[this.z];
        this.aP[this.aH][i] = (Image) obj;
    }

    private int[] r(int i) {
        if (o(i) && a(this.aB, this.aC[i], m(i), n(i))) {
            return g;
        }
        return null;
    }

    private boolean a(byte[] bArr, int i, int i2, int i3) {
        int[] iArr;
        int i4 = 0;
        int i5 = i2 * i3;
        if (g == null) {
            g = new int[40960];
        }
        if (this.j == null || (iArr = this.j[this.aH]) == null) {
            return false;
        }
        if (this.aL == 25840) {
            while (i4 < i5) {
                int i6 = i;
                i++;
                int i7 = bArr[i6] & 255;
                int i8 = iArr[i7 & this.aM];
                int i9 = i7 >> this.aN;
                while (true) {
                    int i10 = i9;
                    i9 = i10 - 1;
                    if (i10 >= 0) {
                        int i11 = i4;
                        i4++;
                        g[i11] = i8;
                    }
                }
            }
            return true;
        }
        if (this.aL == 22258) {
            int i12 = i - 1;
            int i13 = 0 - 1;
            int i14 = i5 - 1;
            while (i13 < i14) {
                i12++;
                byte b2 = bArr[i12];
                int i15 = b2;
                if (b2 < 0) {
                    int i16 = i15 + 128;
                    do {
                        i13++;
                        i12++;
                        g[i13] = iArr[bArr[i12] & 255];
                        i16--;
                    } while (i16 > 0);
                } else {
                    i12++;
                    int i17 = iArr[bArr[i12] & 255];
                    do {
                        i13++;
                        g[i13] = i17;
                        i15--;
                    } while (i15 > 0);
                }
            }
            return true;
        }
        if (this.aL == 5632) {
            int i18 = (i5 + ((i5 & 1) == 0 ? 0 : 2)) >> 1;
            while (true) {
                i18--;
                if (i18 < 0) {
                    return true;
                }
                int i19 = i;
                i++;
                byte b3 = bArr[i19];
                int i20 = i4;
                int i21 = i4 + 1;
                g[i20] = iArr[(b3 >> 4) & 15];
                i4 = i21 + 1;
                g[i21] = iArr[b3 & 15];
            }
        } else if (this.aL == 1024) {
            int i22 = (i5 + ((i5 & 3) == 0 ? 0 : 4)) >> 2;
            while (true) {
                i22--;
                if (i22 < 0) {
                    return true;
                }
                int i23 = i;
                i++;
                byte b4 = bArr[i23];
                int i24 = i4;
                int i25 = i4 + 1;
                g[i24] = iArr[(b4 >> 6) & 3];
                int i26 = i25 + 1;
                g[i25] = iArr[(b4 >> 4) & 3];
                int i27 = i26 + 1;
                g[i26] = iArr[(b4 >> 2) & 3];
                i4 = i27 + 1;
                g[i27] = iArr[b4 & 3];
            }
        } else if (this.aL == 512) {
            int i28 = (i5 + ((i5 & 7) == 0 ? 0 : 8)) >> 3;
            while (true) {
                i28--;
                if (i28 < 0) {
                    return true;
                }
                int i29 = i;
                i++;
                byte b5 = bArr[i29];
                int i30 = i4;
                int i31 = i4 + 1;
                g[i30] = iArr[(b5 >> 7) & 1];
                int i32 = i31 + 1;
                g[i31] = iArr[(b5 >> 6) & 1];
                int i33 = i32 + 1;
                g[i32] = iArr[(b5 >> 5) & 1];
                int i34 = i33 + 1;
                g[i33] = iArr[(b5 >> 4) & 1];
                int i35 = i34 + 1;
                g[i34] = iArr[(b5 >> 3) & 1];
                int i36 = i35 + 1;
                g[i35] = iArr[(b5 >> 2) & 1];
                int i37 = i36 + 1;
                g[i36] = iArr[(b5 >> 1) & 1];
                i4 = i37 + 1;
                g[i37] = iArr[b5 & 1];
            }
        } else {
            if (this.aL != 22018) {
                if (this.aL != -22976) {
                    return true;
                }
                while (i4 < i5) {
                    int i38 = i;
                    i++;
                    int i39 = bArr[i38] & 255;
                    int i40 = iArr[i39 & this.aM];
                    int i41 = i39 >> this.aN;
                    while (true) {
                        int i42 = i41;
                        i41 = i42 - 1;
                        if (i42 >= 0) {
                            int i43 = i4;
                            i4++;
                            g[i43] = i40;
                        }
                    }
                }
                int i44 = 0;
                while (i44 < i5) {
                    int i45 = i;
                    i++;
                    int i46 = bArr[i45] & 255;
                    if (i46 == 254) {
                        int i47 = i + 1;
                        int i48 = bArr[i] & 255;
                        i = i47 + 1;
                        int i49 = bArr[i47] & 255;
                        while (true) {
                            int i50 = i48;
                            i48 = i50 - 1;
                            if (i50 > 0) {
                                g[i44] = (i49 << 24) | (g[i44] & 16777215);
                                i44++;
                            }
                        }
                    } else {
                        g[i44] = (i46 << 24) | (g[i44] & 16777215);
                        i44++;
                    }
                }
                return true;
            }
            while (true) {
                i5--;
                if (i5 < 0) {
                    return true;
                }
                int i51 = i4;
                i4++;
                int i52 = i;
                i++;
                g[i51] = iArr[bArr[i52] & 255];
            }
        }
    }

    /* JADX WARN: Type inference failed for: r1v4, types: [short[], short[][]] */
    public final void a(short[] sArr) {
        this.R = sArr[0];
        this.Q = new short[this.R];
        int i = 1;
        for (int i2 = 0; i2 < this.R; i2++) {
            this.Q[i2] = new short[2];
            int i3 = i;
            int i4 = i + 1;
            this.Q[i2][0] = sArr[i3];
            i = i4 + 1;
            this.Q[i2][1] = sArr[i4];
        }
        int i5 = i;
        while (i5 < sArr.length) {
            int i6 = i5;
            int i7 = i5 + 1;
            short s2 = sArr[i6];
            i5 = i7 + 1;
            short s3 = sArr[i7];
            short[] sArr2 = new short[(s3 << 1) + 2];
            sArr2[0] = this.Q[s2][0];
            sArr2[1] = this.Q[s2][1];
            for (int i8 = 0; i8 < s3; i8++) {
                int i9 = i5;
                int i10 = i5 + 1;
                sArr2[(i8 << 1) + 2] = sArr[i9];
                i5 = i10 + 1;
                sArr2[(i8 << 1) + 3] = sArr[i10];
            }
            this.Q[s2] = sArr2;
        }
        this.N = -j(0, 0);
        this.J = this.N + j(0, 1);
        this.K = j(0, 2) - j(0, 1);
        this.L = i(s(32), 0);
    }

    private int s(int i) {
        if (this.Q == null) {
            return 0;
        }
        int i2 = i % this.R;
        if (this.Q[i2][0] == i) {
            return this.Q[i2][1];
        }
        int i3 = 2;
        int length = this.Q[i2].length;
        while (i3 < length && this.Q[i2][i3] != i) {
            i3 += 2;
        }
        if (i3 >= length) {
            return 1;
        }
        return this.Q[i2][i3 + 1];
    }

    final int k(int i) {
        return (i * this.J) + ((i - 1) * this.K);
    }

    public final short[] a(String str, int i, boolean z) {
        int iS;
        if (U == null) {
            U = new short[250];
        }
        int length = str.length();
        short s2 = 0;
        short s3 = 1;
        short s4 = 0;
        boolean z2 = this.f;
        boolean z3 = false;
        short s5 = 0;
        boolean z4 = this.f;
        int i2 = 0;
        while (i2 < length) {
            char cCharAt = str.charAt(i2);
            if (cCharAt == ' ') {
                s2 = (short) (s2 + ((short) this.L));
                s4 = (short) i2;
                z2 = z4;
                z3 = true;
                s5 = 0;
                if (s2 > i) {
                    z3 = false;
                    for (int i3 = s4; i3 >= 0 && str.charAt(i3) == ' '; i3--) {
                        s2 = (short) (s2 - ((short) this.L));
                    }
                    while (s4 < length && str.charAt(s4) == ' ') {
                        s4 = (short) (s4 + 1);
                    }
                    short s6 = (short) (s4 - 1);
                    s4 = s6;
                    i2 = s6;
                    z4 = z2;
                    short s7 = (short) (s3 + 1);
                    U[s3] = (short) (s4 + 1);
                    s3 = (short) (s7 + 1);
                    U[s7] = s2;
                    s2 = 0;
                }
            } else if (cCharAt == '\\') {
                i2++;
                if (str.charAt(i2) == '^') {
                    z4 = !z4;
                }
            } else if (cCharAt == '\n') {
                short s8 = (short) (s3 + 1);
                U[s3] = (short) i2;
                s3 = (short) (s8 + 1);
                U[s8] = s2;
                s2 = 0;
                s5 = 0;
            } else {
                if (cCharAt >= ' ') {
                    iS = s(cCharAt);
                } else if (cCharAt == 1) {
                    i2++;
                } else if (cCharAt == 2) {
                    i2++;
                    iS = str.charAt(i2);
                }
                if (iS > c()) {
                    iS = 0;
                }
                int i4 = i(iS, 0);
                if (z4) {
                    i4++;
                }
                s5 = (short) (s5 + ((short) i4));
                short s9 = (short) (s2 + ((short) i4));
                s2 = s9;
                if (s9 > i && z3) {
                    z3 = false;
                    for (int i5 = s4; i5 >= 0 && str.charAt(i5) == ' '; i5--) {
                        s2 = (short) (s2 - ((short) this.L));
                    }
                    short s10 = (short) (s3 + 1);
                    U[s3] = (short) (s4 + 1);
                    s3 = (short) (s10 + 1);
                    U[s10] = (short) (s2 - s5);
                    s2 = 0;
                    i2 = s4;
                    z4 = z2;
                }
            }
            i2++;
        }
        short s11 = (short) (s3 + 1);
        U[s3] = (short) length;
        U[s11] = s2;
        U[0] = (short) (((short) (s11 + 1)) / 2);
        return U;
    }

    final String a(String str, int i) {
        String string = "";
        short s2 = 0;
        short[] sArrA = a(str, 390, false);
        for (int i2 = 0; i2 < sArrA[0]; i2++) {
            if (s2 != 0 && (s2 >= str.length() || str.charAt(s2) != '\n')) {
                string = new StringBuffer().append(string).append("\n").toString();
            }
            string = new StringBuffer().append(string).append(str.substring(s2, sArrA[(i2 << 1) + 1])).toString();
            s2 = sArrA[(i2 << 1) + 1];
        }
        return string;
    }

    public final void a(Graphics graphics, String str, short[] sArr, int i, int i2, int i3, int i4, int i5, int i6) {
        short s2 = sArr[0];
        int i7 = this.J;
        if (i4 == -1) {
            i4 = s2;
        }
        if (i3 + i4 > s2) {
            i4 = s2 - i3;
        }
        int i8 = this.K + i7;
        if ((i5 & 32) != 0) {
            i2 -= i8 * (i4 - 1);
        } else if ((i5 & 2) != 0) {
            i2 -= (i8 * (i4 - 1)) >> 1;
        }
        H = this.aH;
        if (i6 >= 0) {
            short s3 = i3 > 0 ? sArr[((i3 - 1) << 1) + 1] : (short) 0;
            X = s3;
            X = s3 + i6;
        }
        int i9 = i3;
        for (int i10 = 0; i9 < s2 && i10 <= i4 - 1; i10++) {
            V = i9 > 0 ? sArr[((i9 - 1) << 1) + 1] : (short) 0;
            W = sArr[(i9 << 1) + 1];
            if (V < str.length() && str.charAt(V) == '\n') {
                V++;
            }
            int i11 = i;
            int i12 = i2 + (i10 * i8);
            if ((i5 & 43) != 0) {
                if ((i5 & 8) != 0) {
                    i11 = i - sArr[(i9 + 1) << 1];
                } else if ((i5 & 1) != 0) {
                    i11 = i - (sArr[(i9 + 1) << 1] >> 1);
                }
                if ((i5 & 32) != 0) {
                    i12 -= this.J;
                } else if ((i5 & 2) != 0) {
                    i12 -= this.J >> 1;
                }
            }
            a(graphics, str, i11, i12, 0, false);
            i9++;
        }
        V = -1;
        W = -1;
        X = -1;
        this.aH = H;
    }

    final void a(String str, char[] cArr) {
        int length;
        int iCharAt;
        if (str == null && cArr == null) {
            return;
        }
        d = 0;
        e = this.J;
        int i = 0;
        boolean z = str != null;
        int i2 = V >= 0 ? V : 0;
        if (z) {
            length = W >= 0 ? W : str.length();
        } else {
            length = W >= 0 ? W : cArr.length;
        }
        boolean z2 = this.f;
        int i3 = i2;
        while (i3 < length) {
            char cCharAt = z ? str.charAt(i3) : cArr[i3];
            char c2 = cCharAt;
            if (cCharAt == '\\') {
                i3++;
                if ((z ? str.charAt(i3) : cArr[i3]) == '^') {
                    z2 = !z2;
                }
            } else {
                if (c2 > ' ') {
                    iCharAt = s(c2);
                } else if (c2 == ' ') {
                    i += this.L;
                } else if (c2 == ' ') {
                    i += this.L;
                } else if (c2 == '\n') {
                    if (i > d) {
                        d = i;
                    }
                    i = 0;
                    e += this.K + this.J;
                } else if (c2 == 1) {
                    i3++;
                } else if (c2 == 2) {
                    i3++;
                    iCharAt = z ? str.charAt(i3) : cArr[i3];
                }
                i += i(iCharAt, 0);
                if (z2) {
                    i++;
                }
            }
            i3++;
        }
        if (i > d) {
            d = i;
        }
        if (d > 0) {
            d = d;
        }
    }

    public final void a(Graphics graphics, String str, int i, int i2, int i3, int i4) {
    }

    public final void a(Graphics graphics, String str, int i, int i2, int i3) {
        a(graphics, str, i, i2, i3, true);
    }

    private void a(Graphics graphics, String str, int i, int i2, int i3, boolean z) {
        int length;
        int length2;
        char cCharAt;
        char cCharAt2;
        int iCharAt;
        char cCharAt3;
        int i4 = i;
        if (str != null) {
            int i5 = i2 + this.N;
            boolean z2 = str != null;
            a(str, (char[]) null);
            if ((i3 & 43) != 0) {
                if ((i3 & 8) != 0) {
                    i4 -= d;
                } else if ((i3 & 1) != 0) {
                    i4 -= d >> 1;
                }
                if ((i3 & 32) != 0) {
                    i5 -= e;
                } else if ((i3 & 2) != 0) {
                    i5 -= e >> 1;
                }
            }
            int i6 = i4;
            int i7 = i5;
            if (z) {
                H = this.aH;
            }
            int i8 = V >= 0 ? V : 0;
            if (z2) {
                length2 = W >= 0 ? W : str.length();
            } else {
                if (W >= 0) {
                    length = W;
                } else {
                    Object[] objArr = null;
                    length = objArr.length;
                }
                length2 = length;
            }
            if (X >= 0 && length2 > X) {
                length2 = X;
            }
            int i9 = i8;
            while (i9 < length2) {
                if (z2) {
                    cCharAt = str.charAt(i9);
                } else {
                    char[] cArr = null;
                    cCharAt = cArr[i9];
                }
                char c2 = cCharAt;
                if (cCharAt == '\\') {
                    i9++;
                    if (z2) {
                        cCharAt3 = str.charAt(i9);
                    } else {
                        char[] cArr2 = null;
                        cCharAt3 = cArr2[i9];
                    }
                    char c3 = cCharAt3;
                    if (cCharAt3 == '_') {
                        this.O = !this.O;
                    } else if (c3 == '^') {
                        this.f = !this.f;
                    } else {
                        l((c3 & 255) - 48);
                    }
                } else {
                    if (c2 > ' ') {
                        iCharAt = s(c2);
                    } else if (c2 == ' ') {
                        if (this.O) {
                            int iS = s(95);
                            a(graphics, iS, i6 + ((this.L - i(iS, 0)) >> 1), i7, 0, 0, 0);
                        }
                        i6 += this.L;
                    } else if (c2 == '\n') {
                        i6 = i4;
                        i7 += this.K + this.J;
                    } else if (c2 == 1) {
                        i9++;
                        if (z2) {
                            cCharAt2 = str.charAt(i9);
                        } else {
                            char[] cArr3 = null;
                            cCharAt2 = cArr3[i9];
                        }
                        char c4 = cCharAt2;
                        if (cCharAt2 < this.k) {
                            l(c4);
                        }
                        if (c4 == 255) {
                            this.aH = H;
                        }
                    } else if (c2 == 2) {
                        i9++;
                        if (z2) {
                            iCharAt = str.charAt(i9);
                        } else {
                            char[] cArr4 = null;
                            iCharAt = cArr4[i9];
                        }
                    }
                    int i10 = iCharAt;
                    a(graphics, i10, i6, i7, 0, 0, 0);
                    if (this.O) {
                        int iS2 = s(95);
                        a(graphics, iS2, i6 + ((i(i10, 0) - i(iS2, 0)) >> 1), i7, 0, 0, 0);
                    }
                    if (this.f) {
                        i6++;
                        a(graphics, i10, i6, i7, 0, 0, 0);
                    }
                    i6 += i(i10, 0);
                }
                i9++;
            }
            if (z) {
                this.aH = H;
            }
        }
    }

    public final void l(int i) {
        if (i >= this.k || i < 0) {
            return;
        }
        this.aH = i;
    }

    final int b() {
        return this.aH;
    }

    final void g(int i, int i2) {
        boolean z = this.aI;
        boolean z2 = this.aJ;
        int length = this.j[i].length;
        if (z2) {
            for (int i3 = length - 1; i3 >= 0; i3--) {
                this.j[i][i3] = (((((this.j[i][i3] >>> 24) * i2) >> 8) & 255) << 24) | (this.j[i][i3] & 16777215);
            }
            return;
        }
        if (!z) {
            int i4 = (i2 & 255) << 24;
            for (int i5 = length - 1; i5 >= 0; i5--) {
                this.j[i][i5] = i4 | (this.j[i][i5] & 16777215);
            }
            return;
        }
        int i6 = (i2 & 255) << 24;
        for (int i7 = length - 1; i7 >= 0; i7--) {
            if ((this.j[i][i7] & 16777215) != 16711935 && (this.j[i][i7] >> 24) != 0) {
                this.j[i][i7] = i6 | (this.j[i][i7] & 16777215);
            }
        }
    }

    final void h(int i, int i2) {
        this.aI = true;
        this.aJ = true;
        for (int i3 = 0; i3 < this.j[i].length; i3++) {
            if ((this.j[i][i3] & 16777215) != 16711935 && (this.j[i][i3] >> 24) != 0) {
                int[] iArr = this.j[i];
                int i4 = i3;
                iArr[i4] = iArr[i4] & 16777215;
                int[] iArr2 = this.j[i];
                int i5 = i3;
                iArr2[i5] = iArr2[i5] | ((this.j[i2][i3] & 16711680) << 8);
            }
        }
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:13:0x0032 */
    /* JADX DEBUG: Move duplicate insns, count: 1 to block B:9:0x0032 */
    /* JADX DEBUG: Multi-variable search result rejected for r2v2, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r2v5, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r3v3, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v4, types: [int] */
    /* JADX WARN: Type inference failed for: r2v8 */
    private static int a(byte[] bArr, int i, short[] sArr, int i2, int i3, boolean z) {
        int i4;
        for (int i5 = 0; i5 < i3; i5++) {
            int i6 = i5;
            if (z) {
                int i7 = i;
                i++;
                i4 = bArr[i7];
            } else {
                int i8 = i;
                int i9 = i + 1;
                i = i9 + 1;
                i4 = ((bArr[i8] ? 1 : 0) & 255) + (((bArr[i9] ? 1 : 0) & 255) << 8);
            }
            sArr[i6] = (short) i4;
        }
        return i;
    }

    static final int[] a(int[] iArr, int i, int i2, int i3) {
        if ((i3 & 7) == 0) {
            return iArr;
        }
        int[] iArrA = a(iArr, i, i2);
        int i4 = 0;
        int i5 = 0;
        switch (i3 & 7) {
            case 1:
                int i6 = i * i2;
                int i7 = i * (i2 - 1);
                int i8 = i2;
                while (true) {
                    i8--;
                    if (i8 < 0) {
                        break;
                    } else {
                        int i9 = i;
                        while (true) {
                            i9--;
                            if (i9 >= 0) {
                                i6--;
                                int i10 = i7;
                                i7++;
                                iArrA[i6] = iArr[i10];
                            }
                        }
                        i7 -= i << 1;
                    }
                }
                break;
            case 2:
                int i11 = (i2 - 1) * i;
                int i12 = i2;
                while (true) {
                    i12--;
                    if (i12 < 0) {
                        break;
                    } else {
                        System.arraycopy(iArr, i5, iArrA, i11, i);
                        i11 -= i;
                        i5 += i;
                    }
                }
            case 3:
                int i13 = (i * i2) - 1;
                while (i13 >= 0) {
                    int i14 = i4;
                    i4++;
                    int i15 = i13;
                    i13 = i15 - 1;
                    iArrA[i14] = iArr[i15];
                }
                break;
            case 4:
                int i16 = i * i2;
                int i17 = i2;
                while (true) {
                    i17--;
                    if (i17 < 0) {
                        break;
                    } else {
                        int i18 = i17;
                        int i19 = i;
                        while (true) {
                            i19--;
                            if (i19 >= 0) {
                                i16--;
                                iArrA[i16] = iArr[i18];
                                i18 += i2;
                            }
                        }
                    }
                }
            case 5:
                int i20 = i * i2;
                int i21 = i2;
                while (true) {
                    i21--;
                    if (i21 < 0) {
                        break;
                    } else {
                        int i22 = (i2 - 1) - i21;
                        int i23 = i;
                        while (true) {
                            i23--;
                            if (i23 >= 0) {
                                i20--;
                                iArrA[i20] = iArr[i22];
                                i22 += i2;
                            }
                        }
                    }
                }
            case 6:
                int i24 = i * i2;
                int i25 = i24;
                int i26 = i24 - 1;
                int i27 = i2;
                while (true) {
                    i27--;
                    if (i27 < 0) {
                        break;
                    } else {
                        int i28 = i26;
                        i26 = i28 - 1;
                        int i29 = i28;
                        int i30 = i;
                        while (true) {
                            i30--;
                            if (i30 >= 0) {
                                i25--;
                                iArrA[i25] = iArr[i29];
                                i29 -= i2;
                            }
                        }
                    }
                }
            case 7:
                int i31 = i * i2;
                int i32 = i31;
                int i33 = i31 - i2;
                int i34 = i2;
                while (true) {
                    i34--;
                    if (i34 < 0) {
                        break;
                    } else {
                        int i35 = i33;
                        i33++;
                        int i36 = i35;
                        int i37 = i;
                        while (true) {
                            i37--;
                            if (i37 >= 0) {
                                i32--;
                                iArrA[i32] = iArr[i36];
                                i36 -= i2;
                            }
                        }
                    }
                }
        }
        return iArrA;
    }

    static final int[] a(int[] iArr, int i, int i2) {
        int[] iArrA = a((int[]) null);
        int[] iArr2 = iArrA;
        if (iArrA == iArr) {
            iArr2 = new int[i * i2];
        }
        return iArr2;
    }

    static {
        byte[] bArr = {73, 69, 78, 68};
        byte[] bArr2 = {8, 6, 0, 0, 0};
    }
}
