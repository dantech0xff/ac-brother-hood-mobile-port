package defpackage;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:b.class */
public final class b {
    private static byte[] n;
    private static byte[] o;
    private static byte[] p;
    private static byte[] q;
    private static byte[] r;
    private static byte[] s;
    private static byte[] t;
    private static byte[] u;
    private static int[] v;
    public static boolean a;
    private static boolean w;
    private static int x;
    private static int y;
    private int z;
    static short[][] b;
    private static int[] A;
    private static int[] B;
    static b[][] c;
    private int C;
    private int D;
    private static final boolean E = false;
    private static int[] F;
    private boolean G;
    private static int H;
    public static int d;
    public static int e;
    private boolean I;
    private int J;
    private int K;
    private int L;
    private int M;
    private int N;
    private boolean O;
    boolean f;
    private byte[] P;
    private short[][] Q;
    private short R;
    private int S;
    private int[] T;
    private static short[] U;
    private static int V;
    private static int W;
    private static int X;
    private static int Y;
    private static int Z;
    private static int aa;
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
    private static int[] aQ;
    private static int[] aR;
    private static int[] aS;
    private static int[] aT;
    private byte[] aU;
    private byte[] aV;
    private int[] aW;
    private static int aX;
    private static int aY;
    private static int aZ;
    private static int ba;
    private static int[] bb;
    private static boolean bc;
    static boolean l;
    static char m;
    private int bd;

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v520, types: [short[]] */
    /* JADX WARN: Type inference failed for: r0v528, types: [short[]] */
    /* JADX WARN: Type inference failed for: r0v562, types: [short[]] */
    /* JADX WARN: Type inference failed for: r1v255, types: [int[], int[][]] */
    /* JADX WARN: Type inference failed for: r1v50, types: [short[], short[][]] */
    public final void a(byte[] r9, int r10) {
        if (r9 != null) goto L291;
        return;
    L291:
        if (bc == false) goto L8;
        System.gc();     // Catch: Exception -> L289
    L8:
        int r102 = (0 + 1) + 1;     // Catch: Exception -> L289
        int r103 = r102 + 1;     // Catch: Exception -> L289
        int r1 = r9[r102] & 255;     // Catch: Exception -> L289
        int r104 = r103 + 1;     // Catch: Exception -> L289
        int r12 = r1 + ((r9[r103] & 255) << 8);     // Catch: Exception -> L289
        int r105 = r104 + 1;     // Catch: Exception -> L289
        int r13 = r12 + ((r9[r104] & 255) << 16);     // Catch: Exception -> L289
        int r106 = r105 + 1;     // Catch: Exception -> L289
        this.aD = r13 + ((r9[r105] & 255) << 24);     // Catch: Exception -> L289
        int r122 = r106 + 1;     // Catch: Exception -> L289
        int r123 = r122 + 1;     // Catch: Exception -> L289
        this.ab = (short) ((r9[r106] & 255) + ((r9[r122] & 255) << 8));     // Catch: Exception -> L289
        if (this.ab > 0) goto L11;
    L111:
        int r0 = r123;
        int r124 = r0 + 1;     // Catch: Exception -> L289
        int r125 = r124 + 1;     // Catch: Exception -> L289
        int r02 = (short) ((r9[r0] & 255) + ((r9[r124] & 255) << 8));     // Catch: Exception -> L289
        if (r02 <= 0) goto L127;
        this.ap = new byte[r02];     // Catch: Exception -> L289
        this.ar = new short[r02];     // Catch: Exception -> L289
        this.as = new short[r02];     // Catch: Exception -> L289
        this.aq = new byte[r02];     // Catch: Exception -> L289
        if ((this.aD & 16384) == 0) goto L116;
        this.at = new byte[r02];     // Catch: Exception -> L289
    L116:
        int r15 = 0;
    L118:
        if (r15 >= r02) goto L127;
        int r3 = r125;
        int r126 = r125 + 1;     // Catch: Exception -> L289
        this.ap[r15] = r9[r3];     // Catch: Exception -> L289
        if ((this.aD & 1024) != 0) goto L122;
        this.ar[r15] = r9[r126];     // Catch: Exception -> L289
        int r127 = (r126 + 1) + 1;     // Catch: Exception -> L289
        this.as[r15] = r9[r12];     // Catch: Exception -> L289
    L124:
        if ((this.aD & 16384) == 0) goto L126;
        int r32 = r127;
        r127 = r127 + 1;     // Catch: Exception -> L289
        this.at[r15] = r9[r32];     // Catch: Exception -> L289
    L126:
        int r33 = r127;
        r125 = r127 + 1;     // Catch: Exception -> L289
        this.aq[r15] = r9[r33];     // Catch: Exception -> L289
        r15 = r15 + 1;     // Catch: Exception -> L289
        goto L118
    L122:
        int r128 = r126 + 1;     // Catch: Exception -> L289
        int r2 = r9[r126] & 255;     // Catch: Exception -> L289
        int r129 = r128 + 1;     // Catch: Exception -> L289
        this.ar[r15] = (short) (r2 + ((r9[r128] & 255) << 8));     // Catch: Exception -> L289
        int r1210 = r129 + 1;     // Catch: Exception -> L289
        int r22 = r9[r129] & 255;     // Catch: Exception -> L289
        r127 = r1210 + 1;     // Catch: Exception -> L289
        this.as[r15] = (short) (r22 + ((r9[r1210] & 255) << 8));     // Catch: Exception -> L289
    L127:
        int r1211 = r125;
        if ((this.aD & 32768) == 0) goto L133;
        int r1212 = r1211 + 1;     // Catch: Exception -> L289
        int r03 = r9[r1211] & 255;     // Catch: Exception -> L289
        int r1213 = r1212 + 1;     // Catch: Exception -> L289
        short r04 = (short) (r03 + ((r9[r1212] & 255) << 8));     // Catch: Exception -> L289
        if ((this.aD & 1024) != 0) goto L132;
        this.am = new byte[r04 << 2];     // Catch: Exception -> L289
        System.arraycopy(r9, r1213, this.am, 0, r04 << 2);     // Catch: Exception -> L289
        r1211 = r1213 + (r04 << 2);     // Catch: Exception -> L289
        goto L133
    L132:
        this.an = new short[r04 << 2];     // Catch: Exception -> L289
        r1211 = a(r9, r1213, this.an, 0, r04 << 2, false);     // Catch: Exception -> L289
    L133:
        int r14 = r1211;
        int r1214 = r1211 + 1;     // Catch: Exception -> L289
        int r1215 = r1214 + 1;     // Catch: Exception -> L289
        int r05 = (short) ((r9[r14] & 255) + ((r9[r1214] & 255) << 8));     // Catch: Exception -> L289
        if (r05 <= 0) goto L162;
        this.ai = new byte[r05];     // Catch: Exception -> L289
        this.aj = new short[r05];     // Catch: Exception -> L289
        if ((this.aD & 32768) == 0) goto L138;
        this.ao = new short[r05 + 1];     // Catch: Exception -> L289
    L138:
        short r16 = 0;
        int r17 = 0;
    L140:
        if (r17 >= r05) goto L148;
        int r34 = r1215;
        int r1216 = r1215 + 1;     // Catch: Exception -> L289
        this.ai[r17] = r9[r34];     // Catch: Exception -> L289
        int r1217 = r1216 + 1;     // Catch: Exception -> L289
        int r23 = r9[r1216] & 255;     // Catch: Exception -> L289
        r1215 = r1217 + 1;     // Catch: Exception -> L289
        this.aj[r17] = (short) (r23 + ((r9[r1217] & 255) << 8));     // Catch: Exception -> L289
        if ((this.aD & 32768) == 0) goto L146;
        if ((this.aD & 32768) == 0) goto L146;
        this.ao[r17] = r16;     // Catch: Exception -> L289
        r1215 = r1215 + 1;     // Catch: Exception -> L289
        r16 = (short) (r16 + r9[r1215]);     // Catch: Exception -> L289
    L146:
        r17 = r17 + 1;     // Catch: Exception -> L289
        goto L140
    L148:
        if ((this.aD & 32768) == 0) goto L151;
        this.ao[this.ao.length - 1] = r16;     // Catch: Exception -> L289
    L151:
        if ((this.aD & 4096) != 0) goto L162;
        int r06 = r05 << 2;     // Catch: Exception -> L289
        if ((this.aD & 1024) != 0) goto L158;
        this.ak = new byte[r06];     // Catch: Exception -> L289
        int r18 = 0;
    L156:
        if (r18 >= r06) goto L162;
        int r35 = r1215;
        r1215 = r1215 + 1;     // Catch: Exception -> L289
        this.ak[r18] = r9[r35];     // Catch: Exception -> L289
        r18 = r18 + 1;     // Catch: Exception -> L289
        goto L156
    L158:
        this.al = new short[r06];     // Catch: Exception -> L289
        int r182 = 0;
    L160:
        if (r182 >= r06) goto L162;
        int r36 = r1215;
        int r1218 = r1215 + 1;     // Catch: Exception -> L289
        r1215 = r1218 + 1;     // Catch: Exception -> L289
        this.al[r182] = (short) ((r9[r36] & 255) + ((r9[r1218] & 255) << 8));     // Catch: Exception -> L289
        r182 = r182 + 1;     // Catch: Exception -> L289
    L162:
        int r07 = r1215;
        int r1219 = r07 + 1;     // Catch: Exception -> L289
        int r1220 = r1219 + 1;     // Catch: Exception -> L289
        int r08 = (short) ((r9[r07] & 255) + ((r9[r1219] & 255) << 8));     // Catch: Exception -> L289
        if (r08 <= 0) goto L172;
        this.av = new byte[r08];     // Catch: Exception -> L289
        this.aw = new byte[r08];     // Catch: Exception -> L289
        this.ax = new short[r08];     // Catch: Exception -> L289
        this.ay = new short[r08];     // Catch: Exception -> L289
        this.i = new byte[r08];     // Catch: Exception -> L289
        int r152 = 0;
    L166:
        if (r152 >= r08) goto L172;
        int r37 = r1220;
        int r1221 = r1220 + 1;     // Catch: Exception -> L289
        this.av[r152] = r9[r37];     // Catch: Exception -> L289
        int r1222 = r1221 + 1;     // Catch: Exception -> L289
        this.aw[r152] = r9[r1221];     // Catch: Exception -> L289
        if ((this.aD & 262144) != 0) goto L170;
        this.ax[r152] = r9[r1222];     // Catch: Exception -> L289
        int r1223 = (r1222 + 1) + 1;     // Catch: Exception -> L289
        this.ay[r152] = r9[r12];     // Catch: Exception -> L289
    L171:
        int r38 = r1223;
        r1220 = r1223 + 1;     // Catch: Exception -> L289
        this.i[r152] = r9[r38];     // Catch: Exception -> L289
        r152 = r152 + 1;     // Catch: Exception -> L289
        goto L166
    L170:
        int r1224 = r1222 + 1;     // Catch: Exception -> L289
        int r24 = r9[r1222] & 255;     // Catch: Exception -> L289
        int r1225 = r1224 + 1;     // Catch: Exception -> L289
        this.ax[r152] = (short) (r24 + ((r9[r1224] & 255) << 8));     // Catch: Exception -> L289
        int r1226 = r1225 + 1;     // Catch: Exception -> L289
        int r25 = r9[r1225] & 255;     // Catch: Exception -> L289
        r1223 = r1226 + 1;     // Catch: Exception -> L289
        this.ay[r152] = (short) (r25 + ((r9[r1226] & 255) << 8));     // Catch: Exception -> L289
    L172:
        int r09 = r1220;
        int r1227 = r09 + 1;     // Catch: Exception -> L289
        int r1228 = r1227 + 1;     // Catch: Exception -> L289
        int r010 = (short) ((r9[r09] & 255) + ((r9[r1227] & 255) << 8));     // Catch: Exception -> L289
        if (r010 <= 0) goto L178;
        this.au = new byte[r010];     // Catch: Exception -> L289
        this.h = new short[r010];     // Catch: Exception -> L289
        int r162 = 0;
    L176:
        if (r162 >= r010) goto L178;
        int r39 = r1228;
        int r1229 = r1228 + 1;     // Catch: Exception -> L289
        this.au[r162] = r9[r39];     // Catch: Exception -> L289
        int r1230 = r1229 + 1;     // Catch: Exception -> L289
        int r26 = r9[r1229] & 255;     // Catch: Exception -> L289
        r1228 = r1230 + 1;     // Catch: Exception -> L289
        this.h[r162] = (short) (r26 + ((r9[r1230] & 255) << 8));     // Catch: Exception -> L289
        r162 = r162 + 1;     // Catch: Exception -> L289
    L178:
        int r011 = r1228;
        if (this.ab > 0) goto L185;
        if (bc == false) goto L333;
        System.gc();     // Catch: Exception -> L289
        return;
    L333:
        return;
    L185:
        if ((this.aD & 16777216) == 0) goto L266;
        int r1231 = r011;
        if ((this.aD & 16777216) == 0) goto L266;
        if (r1231 >= r9.length) goto L266;
        int r1232 = r1231 + 1;     // Catch: Exception -> L289
        int r012 = r9[r1231] & 255;     // Catch: Exception -> L289
        int r1233 = r1232 + 1;     // Catch: Exception -> L289
        short r013 = (short) (r012 + ((r9[r1232] & 255) << 8));     // Catch: Exception -> L289
        int r1234 = r1233 + 1;     // Catch: Exception -> L289
        this.k = r9[r1233] & 255;     // Catch: Exception -> L289
        int r1235 = r1234 + 1;     // Catch: Exception -> L289
        this.aG = r9[r1234] & 255;     // Catch: Exception -> L289
        if (this.aG != 0) goto L194;
        this.aG = 256;     // Catch: Exception -> L289
    L194:
        if (this.j != null) goto L196;
        this.j = new int[16];     // Catch: Exception -> L289
    L196:
        int r172 = 0;
    L198:
        if (r172 >= this.k) goto L235;
        this.j[r172] = new int[this.aG];     // Catch: Exception -> L289
        if (r013 != (-30584)) goto L211;
        int r183 = 0;
    L203:
        if (r183 >= this.aG) goto L234;
        int r19 = r1235;
        int r1236 = r1235 + 1;     // Catch: Exception -> L289
        int r1237 = r1236 + 1;     // Catch: Exception -> L289
        int r014 = (r9[r19] & 255) + ((r9[r1236] & 255) << 8);     // Catch: Exception -> L289
        int r1238 = r1237 + 1;     // Catch: Exception -> L289
        int r015 = r014 + ((r9[r1237] & 255) << 16);     // Catch: Exception -> L289
        r1235 = r1238 + 1;     // Catch: Exception -> L289
        int r016 = r015 + ((r9[r1238] & 255) << 24);     // Catch: Exception -> L289
        if ((r016 & (-16777216)) == (-16777216)) goto L209;
        this.aI = true;     // Catch: Exception -> L289
        if ((r016 & (-16777216)) == 0) goto L209;
        this.aJ = true;     // Catch: Exception -> L289
    L209:
        this.j[r172][r183] = r016;     // Catch: Exception -> L289
        r183 = r183 + 1;     // Catch: Exception -> L289
    L234:
        r172 = r172 + 1;     // Catch: Exception -> L289
        goto L198
    L211:
        if (r013 != 21781) goto L223;
        int r184 = 0;
    L214:
        if (r184 >= this.aG) goto L234;
        int r110 = r1235;
        int r1239 = r1235 + 1;     // Catch: Exception -> L289
        r1235 = r1239 + 1;     // Catch: Exception -> L289
        short r017 = (short) ((r9[r110] & 255) + ((r9[r1239] & 255) << 8));     // Catch: Exception -> L289
        int r20 = -16777216;
        if ((r017 & Short.MIN_VALUE) == 32768) goto L218;
        r20 = 0;
        this.aI = true;     // Catch: Exception -> L289
    L218:
        int r018 = ((r20 | ((r017 & 31744) << 9)) | ((r017 & 992) << 6)) | ((r017 & 31) << 3);     // Catch: Exception -> L289
        int r21 = r018;
        if (r018 != 16253176) goto L221;
        r21 = 16711935;
    L221:
        this.j[r172][r184] = r21;     // Catch: Exception -> L289
        r184 = r184 + 1;     // Catch: Exception -> L289
        goto L214
    L223:
        if (r013 != 25861) goto L234;
        int r185 = 0;
    L226:
        if (r185 >= this.aG) goto L234;
        int r111 = r1235;
        int r1240 = r1235 + 1;     // Catch: Exception -> L289
        r1235 = r1240 + 1;     // Catch: Exception -> L289
        short r019 = (short) ((r9[r111] & 255) + ((r9[r1240] & 255) << 8));     // Catch: Exception -> L289
        int r202 = -16777216;
        if ((r019 & 65535) != 63519) goto L230;
        r202 = 0;
        this.aI = true;     // Catch: Exception -> L289
    L230:
        int r020 = ((r202 | ((r019 & 63488) << 8)) | ((r019 & 2016) << 5)) | ((r019 & 31) << 3);     // Catch: Exception -> L289
        int r212 = r020;
        if (r020 != 16253176) goto L233;
        r212 = 16711935;
    L233:
        this.j[r172][r185] = r212;     // Catch: Exception -> L289
        r185 = r185 + 1;     // Catch: Exception -> L289
        goto L226
    L235:
        int r27 = r1235;
        int r1241 = r1235 + 1;     // Catch: Exception -> L289
        r1231 = r1241 + 1;     // Catch: Exception -> L289
        this.aL = (short) ((r9[r27] & 255) + ((r9[r1241] & 255) << 8));     // Catch: Exception -> L289
        if (this.aL != (-22976)) goto L239;
        this.aI = true;     // Catch: Exception -> L289
        this.aJ = true;     // Catch: Exception -> L289
    L239:
        if (this.aL != 25840) goto L241;
    L242:
        int r173 = this.aG - 1;     // Catch: Exception -> L289
        this.aM = 1;     // Catch: Exception -> L289
        this.aN = 0;     // Catch: Exception -> L289
    L244:
        if (r173 == 0) goto L246;
        r173 = r173 >> 1;     // Catch: Exception -> L289
        this.aM <<= 1;
        this.aN++;
        goto L244
    L246:
        this.aM--;
    L248:
        if (this.ab <= 0) goto L266;
        this.aC = new int[this.ab];     // Catch: Exception -> L289
        int r174 = 0;
        int r192 = 0;
    L251:
        if (r192 >= this.ab) goto L257;
        if ((this.aD & 128) == 0) goto L255;
        int r112 = r1231;
        int r1242 = r1231 + 1;     // Catch: Exception -> L289
        int r1243 = r1242 + 1;     // Catch: Exception -> L289
        int r021 = (r9[r112] & 255) + ((r9[r1242] & 255) << 8);     // Catch: Exception -> L289
        int r1244 = r1243 + 1;     // Catch: Exception -> L289
        int r022 = r021 + ((r9[r1243] & 255) << 16);     // Catch: Exception -> L289
        int r1245 = r1244 + 1;     // Catch: Exception -> L289
        int r203 = r022 + ((r9[r1244] & 255) << 24);     // Catch: Exception -> L289
    L256:
        this.aC[r192] = r174;     // Catch: Exception -> L289
        r174 = r174 + r203;     // Catch: Exception -> L289
        r1231 = r1245 + r203;     // Catch: Exception -> L289
        r192 = r192 + 1;     // Catch: Exception -> L289
        goto L251
    L255:
        int r113 = r1231;
        int r1246 = r1231 + 1;     // Catch: Exception -> L289
        r1245 = r1246 + 1;     // Catch: Exception -> L289
        r203 = (short) ((r9[r113] & 255) + ((r9[r1246] & 255) << 8));     // Catch: Exception -> L289
        goto L256
    L257:
        r1231 = r1231;
        this.aB = new byte[r174];     // Catch: Exception -> L289
        int r175 = 0;
    L259:
        if (r175 >= this.ab) goto L266;
        if ((this.aD & 128) == 0) goto L263;
        int r114 = r1231;
        int r1247 = r1231 + 1;     // Catch: Exception -> L289
        int r1248 = r1247 + 1;     // Catch: Exception -> L289
        int r023 = (r9[r114] & 255) + ((r9[r1247] & 255) << 8);     // Catch: Exception -> L289
        int r1249 = r1248 + 1;     // Catch: Exception -> L289
        int r024 = r023 + ((r9[r1248] & 255) << 16);     // Catch: Exception -> L289
        int r1250 = r1249 + 1;     // Catch: Exception -> L289
        int r186 = r024 + ((r9[r1249] & 255) << 24);     // Catch: Exception -> L289
    L264:
        System.arraycopy(r9, r1250, this.aB, this.aC[r175], r186);     // Catch: Exception -> L289
        r1231 = r1250 + r186;     // Catch: Exception -> L289
        r175 = r175 + 1;     // Catch: Exception -> L289
        goto L259
    L263:
        int r115 = r1231;
        int r1251 = r1231 + 1;     // Catch: Exception -> L289
        r1250 = r1251 + 1;     // Catch: Exception -> L289
        r186 = (short) ((r9[r115] & 255) + ((r9[r1251] & 255) << 8));     // Catch: Exception -> L289
        goto L264
    L241:
        if (this.aL != (-22976)) goto L248;
    L266:
        this.az = new short[16];     // Catch: Exception -> L289
        this.aA = -1;     // Catch: Exception -> L289
        if ((this.aD & 4096) == 0) goto L283;
        int r025 = c();     // Catch: Exception -> L289
        if (r025 <= 0) goto L286;
        int r107 = 0;
        if ((this.aD & 1024) == 0) goto L273;
    L278:
        this.al = new short[r025 << 2];     // Catch: Exception -> L289
        int r11 = 0;
    L280:
        if (r11 >= r025) goto L286;
        a(bb, r11, 0, 0, 0, false);     // Catch: Exception -> L289
        int r116 = r107;
        int r108 = r107 + 1;     // Catch: Exception -> L289
        this.al[r116] = (short) bb[0];     // Catch: Exception -> L289
        int r109 = r108 + 1;     // Catch: Exception -> L289
        this.al[r108] = (short) bb[1];     // Catch: Exception -> L289
        int r1010 = r109 + 1;     // Catch: Exception -> L289
        this.al[r109] = (short) (bb[2] - bb[0]);     // Catch: Exception -> L289
        r107 = r1010 + 1;     // Catch: Exception -> L289
        this.al[r1010] = (short) (bb[3] - bb[1]);     // Catch: Exception -> L289
        r11 = r11 + 1;     // Catch: Exception -> L289
        goto L280
    L273:
        if ((this.aK & 4) != 0) goto L278;
        this.ak = new byte[r025 << 2];     // Catch: Exception -> L289
        int r117 = 0;
    L276:
        if (r117 >= r025) goto L286;
        a(bb, r117, 0, 0, 0, false);     // Catch: Exception -> L289
        int r118 = r107;
        int r1011 = r107 + 1;     // Catch: Exception -> L289
        this.ak[r118] = (byte) bb[0];     // Catch: Exception -> L289
        int r1012 = r1011 + 1;     // Catch: Exception -> L289
        this.ak[r1011] = (byte) bb[1];     // Catch: Exception -> L289
        int r1013 = r1012 + 1;     // Catch: Exception -> L289
        this.ak[r1012] = (byte) (bb[2] - bb[0]);     // Catch: Exception -> L289
        r107 = r1013 + 1;     // Catch: Exception -> L289
        this.ak[r1013] = (byte) (bb[3] - bb[1]);     // Catch: Exception -> L289
        r117 = r117 + 1;     // Catch: Exception -> L289
    L286:
        if (bc == false) goto L334;
        System.gc();     // Catch: Exception -> L289
        return;
    L334:
        return;
    L283:
        if ((this.aK & 4) == 0) goto L286;
        this.aK &= -5;
        goto L286
    L11:
        if ((this.aD & 32) == 0) goto L13;
        this.ac = new short[this.ab];     // Catch: Exception -> L289
        this.ad = new short[this.ab];     // Catch: Exception -> L289
    L13:
        this.ae = new short[this.ab];     // Catch: Exception -> L289
        this.af = new short[this.ab];     // Catch: Exception -> L289
        int r142 = 0;
        int r153 = 0;
        short[][] r163 = null;
        this.aU = new byte[this.ab];     // Catch: Exception -> L289
        this.aW = new int[this.ab];     // Catch: Exception -> L289
        if ((this.aD & 4) == 0) goto L16;
        this.aV = new byte[this.ab];     // Catch: Exception -> L289
    L16:
        boolean r176 = false;
        boolean r187 = false;
        boolean r193 = false;
        int r232 = 0;
    L18:
        if (r232 >= this.ab) goto L85;
        boolean r204 = false;
        boolean r213 = false;
        boolean r222 = false;
        if ((r9[r123] & 255) != 0) goto L25;
        r123 = r123 + 1;     // Catch: Exception -> L289
        this.aU[r232] = 0;     // Catch: Exception -> L289
        r176 = false;
        r187 = true;
        r193 = true;
        if ((this.aD & 4) == 0) goto L52;
        r123 = r123 + 1;     // Catch: Exception -> L289
        this.aV[r232] = r9[r123];     // Catch: Exception -> L289
    L52:
        if (r176 == false) goto L55;
        int r310 = r123;
        int r1252 = r123 + 1;     // Catch: Exception -> L289
        int r1253 = r1252 + 1;     // Catch: Exception -> L289
        int r28 = (r9[r310] & 255) + ((r9[r1252] & 255) << 8);     // Catch: Exception -> L289
        int r1254 = r1253 + 1;     // Catch: Exception -> L289
        int r29 = r28 + ((r9[r1253] & 255) << 16);     // Catch: Exception -> L289
        r123 = r1254 + 1;     // Catch: Exception -> L289
        this.aW[r232] = r29 + ((r9[r1254] & 255) << 24);     // Catch: Exception -> L289
    L55:
        if (r222 == false) goto L61;
        if (r163 != null) goto L59;
        r163 = new short[this.ab];     // Catch: Exception -> L289
    L59:
        int r119 = r123;
        int r1255 = r123 + 1;     // Catch: Exception -> L289
        int r1256 = r1255 + 1;     // Catch: Exception -> L289
        int r026 = (r9[r119] & 255) + ((r9[r1255] & 255) << 8);     // Catch: Exception -> L289
        int r1257 = r1256 + 1;     // Catch: Exception -> L289
        int r027 = r026 + ((r9[r1256] & 255) << 16);     // Catch: Exception -> L289
        int r028 = r027 + ((r9[r1257] & 255) << 24);     // Catch: Exception -> L289
        r123 = (r1257 + 1) + 1;     // Catch: Exception -> L289
        r163[r232] = new short[]{(short) r028, (short) (r028 >> 16), r9[r12]};     // Catch: Exception -> L289
        r142 = r142 + 1;     // Catch: Exception -> L289
        r153 = r153 + 3;     // Catch: Exception -> L289
    L61:
        if (r187 == false) goto L66;
        if ((this.aD & 32) == 0) goto L66;
        int r311 = r123;
        int r1258 = r123 + 1;     // Catch: Exception -> L289
        int r1259 = r1258 + 1;     // Catch: Exception -> L289
        this.ac[r232] = (short) ((r9[r311] & 255) + ((r9[r1258] & 255) << 8));     // Catch: Exception -> L289
        int r1260 = r1259 + 1;     // Catch: Exception -> L289
        int r210 = r9[r1259] & 255;     // Catch: Exception -> L289
        r123 = r1260 + 1;     // Catch: Exception -> L289
        this.ad[r232] = (short) (r210 + ((r9[r1260] & 255) << 8));     // Catch: Exception -> L289
    L66:
        if (r193 == false) goto L72;
        if ((this.aD & 16) != 0) goto L70;
        int r312 = r123;
        int r1261 = r123 + 1;     // Catch: Exception -> L289
        this.ae[r232] = (short) (r9[r312] & 255);     // Catch: Exception -> L289
        r123 = r1261 + 1;     // Catch: Exception -> L289
        this.af[r232] = (short) (r9[r1261] & 255);     // Catch: Exception -> L289
        goto L72
    L70:
        int r313 = r123;
        int r1262 = r123 + 1;     // Catch: Exception -> L289
        int r1263 = r1262 + 1;     // Catch: Exception -> L289
        this.ae[r232] = (short) ((r9[r313] & 255) + ((r9[r1262] & 255) << 8));     // Catch: Exception -> L289
        int r1264 = r1263 + 1;     // Catch: Exception -> L289
        int r211 = r9[r1263] & 255;     // Catch: Exception -> L289
        r123 = r1264 + 1;     // Catch: Exception -> L289
        this.af[r232] = (short) (r211 + ((r9[r1264] & 255) << 8));     // Catch: Exception -> L289
    L72:
        if (r204 == false) goto L78;
        if (r163 != null) goto L76;
        r163 = new short[this.ab];     // Catch: Exception -> L289
    L76:
        int r314 = r123;
        int r1265 = r123 + 1;     // Catch: Exception -> L289
        int r1266 = r1265 + 1;     // Catch: Exception -> L289
        int r1267 = r1266 + 1;     // Catch: Exception -> L289
        int r214 = r9[r1266] & 255;     // Catch: Exception -> L289
        r123 = r1267 + 1;     // Catch: Exception -> L289
        r163[r232] = new short[]{(short) ((r9[r314] & 255) + ((r9[r1265] & 255) << 8)), (short) (r214 + ((r9[r1267] & 255) << 8))};     // Catch: Exception -> L289
        r142 = r142 + 1;     // Catch: Exception -> L289
        r153 = r153 + 2;     // Catch: Exception -> L289
    L78:
        if (r213 == false) goto L83;
        if (r163 != null) goto L82;
        r163 = new short[this.ab];     // Catch: Exception -> L289
    L82:
        int r315 = r123;
        int r1268 = r123 + 1;     // Catch: Exception -> L289
        int r1269 = r1268 + 1;     // Catch: Exception -> L289
        int r1270 = r1269 + 1;     // Catch: Exception -> L289
        int r215 = r9[r1269] & 255;     // Catch: Exception -> L289
        int r1271 = r1270 + 1;     // Catch: Exception -> L289
        int r1272 = r1271 + 1;     // Catch: Exception -> L289
        int r216 = r9[r1271] & 255;     // Catch: Exception -> L289
        int r1273 = r1272 + 1;     // Catch: Exception -> L289
        int r1274 = r1273 + 1;     // Catch: Exception -> L289
        int r217 = r9[r1273] & 255;     // Catch: Exception -> L289
        r123 = r1274 + 1;     // Catch: Exception -> L289
        r163[r232] = new short[]{(short) ((r9[r315] & 255) + ((r9[r1268] & 255) << 8)), (short) (r215 + ((r9[r1270] & 255) << 8)), (short) (r216 + ((r9[r1272] & 255) << 8)), (short) (r217 + ((r9[r1274] & 255) << 8))};     // Catch: Exception -> L289
        r142 = r142 + 1;     // Catch: Exception -> L289
        r153 = r153 + 4;     // Catch: Exception -> L289
    L83:
        r232 = r232 + 1;     // Catch: Exception -> L289
        goto L18
    L25:
        if ((r9[r123] & 255) != 255) goto L28;
        r123 = r123 + 1;     // Catch: Exception -> L289
        this.aU[r232] = 1;     // Catch: Exception -> L289
        r176 = true;
        r187 = false;
        r193 = true;
        goto L52
    L28:
        if ((r9[r123] & 255) != 254) goto L31;
        r123 = r123 + 1;     // Catch: Exception -> L289
        this.aU[r232] = 2;     // Catch: Exception -> L289
        r176 = true;
        r187 = false;
        r193 = true;
        goto L52
    L31:
        if ((r9[r123] & 255) != 253) goto L34;
        r123 = r123 + 1;     // Catch: Exception -> L289
        this.aU[r232] = 5;     // Catch: Exception -> L289
        r176 = false;
        r187 = false;
        r193 = true;
        goto L52
    L34:
        if ((r9[r123] & 255) != 252) goto L37;
        r123 = r123 + 1;     // Catch: Exception -> L289
        this.aU[r232] = 3;     // Catch: Exception -> L289
        r176 = true;
        r187 = false;
        r193 = true;
        r204 = true;
        goto L52
    L37:
        if ((r9[r123] & 255) != 251) goto L40;
        r123 = r123 + 1;     // Catch: Exception -> L289
        this.aU[r232] = 4;     // Catch: Exception -> L289
        r176 = true;
        r187 = false;
        r193 = true;
        r204 = true;
        goto L52
    L40:
        if ((r9[r123] & 255) != 250) goto L43;
        r123 = r123 + 1;     // Catch: Exception -> L289
        this.aU[r232] = 6;     // Catch: Exception -> L289
        r176 = true;
        r187 = false;
        r193 = false;
        r213 = true;
        goto L52
    L43:
        if ((r9[r123] & 255) != 249) goto L46;
        r123 = r123 + 1;     // Catch: Exception -> L289
        this.aU[r232] = 7;     // Catch: Exception -> L289
        r176 = true;
        r187 = false;
        r193 = false;
        r213 = true;
        goto L52
    L46:
        if ((r9[r123] & 255) != 248) goto L49;
        r123 = r123 + 1;     // Catch: Exception -> L289
        this.aU[r232] = 8;     // Catch: Exception -> L289
        r176 = true;
        r187 = false;
        r193 = true;
        r213 = false;
        goto L52
    L49:
        if ((r9[r123] & 255) != 247) goto L52;
        r123 = r123 + 1;     // Catch: Exception -> L289
        this.aU[r232] = 9;     // Catch: Exception -> L289
        r176 = true;
        r222 = true;
        r187 = false;
        r193 = true;
        r213 = false;
        goto L52
    L85:
        if (r142 <= 0) goto L111;
        this.ag = new short[r153];     // Catch: Exception -> L289
        this.ah = new short[r142 << 1];     // Catch: Exception -> L289
        int r233 = 0;
        short r223 = 0;
        short r132 = 0;
    L88:
        if (r132 >= this.ab) goto L111;
        if (this.aU[r132] != 3) goto L92;
    L93:
        int r242 = 2;
    L104:
        if (r242 <= 0) goto L110;
        this.ah[r233 << 1] = r132;     // Catch: Exception -> L289
        this.ah[(r233 << 1) + 1] = r223;     // Catch: Exception -> L289
        int r143 = 0;
    L107:
        if (r143 >= r242) goto L109;
        this.ag[r223] = r163[r132][r143];     // Catch: Exception -> L289
        r223 = (short) (r223 + 1);     // Catch: Exception -> L289
        r143 = r143 + 1;     // Catch: Exception -> L289
        goto L107
    L109:
        r163[r132] = null;     // Catch: Exception -> L289
        r233 = r233 + 1;     // Catch: Exception -> L289
    L110:
        r132 = (short) (r132 + 1);     // Catch: Exception -> L289
        goto L88
    L92:
        if (this.aU[r132] == 4) goto L93;
        if (this.aU[r132] != 6) goto L97;
    L98:
        r242 = 4;
        goto L104
    L97:
        if (this.aU[r132] == 7) goto L98;
        if (this.aU[r132] != 9) goto L102;
        r242 = 3;
        goto L104
    L102:
        r242 = -1;
        goto L104
    }

    final void a(boolean r4) {
        this.aF = null;
        this.aE = null;
        this.j = null;
        this.aB = null;
        this.aC = null;
        if (bc == false) goto L6;
        System.gc();
        return;
    }

    final void a(int r5, byte[] r6) {
        if (this.az[r5] != null) goto L9;
        this.az[r5] = new short[this.ab];
        int r7 = 0;
    L6:
        if (r7 >= this.ab) goto L9;
        this.az[r5][r7] = (short) r7;
        r7 = r7 + 1;
    L9:
        if (r6 != null) goto L11;
        return;
    L11:
        int r72 = 0;
    L13:
        if (r72 >= r6.length) goto L15;
        int r1 = r72;
        int r73 = r72 + 1;
        int r74 = r73 + 1;
        int r0 = (r6[r1] & 255) + ((r6[r73] & 255) << 8);
        int r75 = r74 + 1;
        int r02 = r6[r74] & 255;
        r72 = r75 + 1;
        this.az[r5][r0] = (short) (r02 + ((r6[r75] & 255) << 8));
        goto L13
    }

    final void a(int r4) {
        this.aA = r4;
    }

    public final int a(int r5, int r6) {
        return this.aw[this.h[r5] + r6] & 255;
    }

    public final int b(int r4) {
        return this.au[r4] & 255;
    }

    private int m(int r4) {
        return this.ae[r4] & 65535;
    }

    private int n(int r4) {
        return this.af[r4] & 65535;
    }

    final int c(int r4) {
        return this.ax[r4];
    }

    final int b(int r4, int r5) {
        int r0 = this.h[r4] + r5;
        return this.ax[r0];
    }

    final int d(int r4) {
        return this.ay[r4];
    }

    final int c(int r4, int r5) {
        int r0 = this.h[r4] + r5;
        return this.ay[r0];
    }

    final int e(int r5) {
        if ((this.aD & 1024) != 0) goto L9;
        if ((this.aK & 4) != 0) goto L9;
        return this.ak[(r5 << 2) + 2] & 255;
    L9:
        return this.al[(r5 << 2) + 2] & 65535;
    }

    final int f(int r5) {
        if ((this.aD & 1024) != 0) goto L9;
        if ((this.aK & 4) != 0) goto L9;
        return this.ak[(r5 << 2) + 3] & 255;
    L9:
        return this.al[(r5 << 2) + 3] & 65535;
    }

    final int g(int r5) {
        if ((this.aD & 1024) != 0) goto L9;
        if ((this.aK & 4) != 0) goto L9;
        return this.ak[r5 << 2];
    L9:
        return this.al[r5 << 2];
    }

    final int h(int r5) {
        if ((this.aD & 1024) != 0) goto L9;
        if ((this.aK & 4) != 0) goto L9;
        return this.ak[(r5 << 2) + 1];
    L9:
        return this.al[(r5 << 2) + 1];
    }

    private int i(int r5, int r6) {
        short r1 = this.aj[r5];
        return this.ar[r1];
    }

    private int j(int r5, int r6) {
        int r1 = this.aj[0] + r6;
        return this.as[r1];
    }

    final int d(int r5, int r6) {
        int r0 = this.h[r5] + r6;
        return (this.av[r0] & 255) | ((this.i[r0] & 192) << 2);
    }

    private void a(int[] r10, int r11, int r12, int r13, int r14, boolean r15) {
        aX = Integer.MAX_VALUE;
        aY = Integer.MAX_VALUE;
        aZ = Integer.MIN_VALUE;
        ba = Integer.MIN_VALUE;
        aa = 1;
        a(null, r11, r12, r13, r14, 0, 0);
        aa = 0;
        if (r15 == false) goto L6;
        r10[0] = aY;
        r10[1] = j.e - aZ;
        r10[2] = ba;
        r10[3] = j.e - aX;
        return;
    }

    final void a(int[] r7, int r8, int r9, int r10, int r11) {
        r7[0] = 0;
        r7[1] = 0;
        r7[2] = 0;
        r7[3] = 0;
        if ((r11 & 4) != 0) goto L6;
        r7[2] = r7[2] + m(r8);
        r7[3] = r7[3] + n(r8);
        return;
    L6:
        r7[2] = r7[2] + n(r8);
        r7[3] = r7[3] + m(r8);
    }

    final int a() {
        if (this.au != null) goto L7;
        return 0;
    L7:
        return this.au.length;
    }

    private int c() {
        if (this.ai != null) goto L7;
        return 0;
    L7:
        return this.ai.length;
    }

    private boolean o(int r3) {
        if (this.aC != null) goto L7;
        return false;
    L7:
        if (this.aB != null) goto L10;
        return false;
    L10:
        return true;
    }

    final void a(int r7, int r8, int[] r9, int r10) {
        if (this.ao != null) goto L5;
        return;
    L5:
        if (r9 == null) goto L28;
        short r0 = this.ao[r7];
        int r02 = this.ao[r7 + 1] - r0;
        if (r02 > 0) goto L9;
    L25:
        r9[0] = 0;
        r9[1] = 0;
        r9[2] = 0;
        r9[3] = 0;
        return;
    L9:
        if (r8 >= r02) goto L25;
        int r03 = (r0 + r8) << 2;
        if ((this.aD & 1024) == 0) goto L16;
        if (this.an == null) goto L19;
        r9[0] = this.an[r03];
        r9[1] = this.an[r03 + 1];
        r9[2] = this.an[r03 + 2] & 65535;
        r9[3] = this.an[r03 + 3] & 65535;
    L19:
        if ((r10 & 1) == 0) goto L22;
        r9[0] = (-r9[0]) - r9[2];
    L22:
        if ((r10 & 2) == 0) goto L29;
        r9[1] = (-r9[1]) - r9[3];
        return;
    L29:
        return;
    L16:
        if (this.am == null) goto L19;
        r9[0] = this.am[r03];
        r9[1] = this.am[r03 + 1];
        r9[2] = this.am[r03 + 2] & 255;
        r9[3] = this.am[r03 + 3] & 255;
        goto L19
    }

    final void a(int r9, int r10, int r11, int[] r12, int r13) {
        a(r9, r10, r11, r12, r13, false);
    }

    final void a(int r9, int r10, int r11, int[] r12, int r13, boolean r14) {
        int r0 = this.h[r9] + r10;
        a((this.av[r0] & 255) | ((this.i[r0] & 192) << 2), r11, r12, r13 ^ (this.i[r0] & 15));
        if (r14 == false) goto L6;
        r12[0] = r12[0] + this.ax[r0];
        r12[1] = r12[1] + this.ay[r0];
        return;
    }

    public static int[] a(int[] r2) {
        if (g != null) goto L6;
        g = new int[40960];
    L6:
        return g;
    }

    final void b(boolean r2) {
    }

    public final void a(Graphics r10, int r11, int r12, int r13, int r14, int r15, int r16, int r17) {
        int r0 = this.h[r11] + r12;
        int r02 = (this.av[r0] & 255) | ((this.i[r0] & 192) << 2);
        if ((r15 & 1) == 0) goto L5;
        int r162 = r16 + this.ax[r0];
    L7:
        if ((r15 & 2) == 0) goto L9;
        int r172 = r17 + this.ay[r0];
    L10:
        a(r10, r02, r13 - r162, r14 - r172, r15 ^ (this.i[r0] & 15), r162, r172);
        return;
    L9:
        r172 = r17 - this.ay[r0];
        goto L10
    L5:
        r162 = r16 - this.ax[r0];
        goto L7
    }

    final void a(Graphics r11, int r12, int r13, int r14, int r15, int r16, int r17) {
        int r0 = this.ai[r12] & 255;
        if (r0 <= 0) goto L34;
        int r19 = 0;
    L6:
        if (r19 >= r0) goto L38;
        int r02 = this.aj[r12] + r19;
        int r03 = this.aq[r02] & 255;
        int r29 = (this.ap[r02] & 255) | ((r03 & 192) << 2);
        if ((this.aD & 16384) == 0) goto L11;
        this.aH = this.at[r02] & 255;
    L11:
        if ((r03 & 16) == 0) goto L13;
    L15:
        int r32 = 0;
        int r33 = 0;
        if ((r03 & 16) != 0) goto L18;
        r32 = m(r29);
        r33 = n(r29);
    L18:
        int r04 = r15 ^ r03;
        if ((r04 & 4) == 0) goto L22;
        int r05 = r32;
        r32 = r33;
        r33 = r05;
    L22:
        if ((r15 & 1) == 0) goto L24;
        int r31 = -(this.ar[r02] + r32);
    L26:
        if ((r15 & 2) == 0) goto L28;
        int r22 = -(this.as[r02] + r33);
    L29:
        int r06 = r13 + r31;
        int r07 = r14 + r22;
        if ((r04 & 16) == 0) goto L32;
        a(r11, r29, r06, r07, r04 & 15, r16, r17);
    L33:
        r19 = r19 + 1;
        goto L6
    L32:
        a(r11, r29, r06, r07, r04 & 15);
        goto L33
    L28:
        r22 = this.as[r02];
        goto L29
    L24:
        r31 = this.ar[r02];
        goto L26
    L13:
        if (this.aA < 0) goto L15;
        r29 = this.az[this.aA][r29];
        goto L15
    L38:
        return;
    }

    public final void a(int r8, int r9, int r10, int r11) {
        p(r8);
        if (this.ab != 0) goto L5;
        return;
    L5:
        int r0 = this.ab - 1;
        if ((this.aD & 16777224) == 0) goto L35;
        int r02 = this.aH;
        this.aH = r8;
        if (bc == false) goto L10;
        System.gc();
    L10:
        int r112 = 0;
    L12:
        if (r112 > r0) goto L31;
        if (this.aU[r112] != 0) goto L29;
        int r03 = m(r112);
        int r04 = n(r112);
        if (r03 <= 0) goto L29;
        if (r04 <= 0) goto L29;
        int[] r05 = r(r112);
        if (r05 == null) goto L29;
        int r5 = r112;
        int r06 = r03 * r04;
        boolean r19 = false;
        int r20 = 0;
    L23:
        if (r20 >= r06) goto L28;
        if ((r05[r20] & (-16777216)) != (-16777216)) goto L26;
        r20 = r20 + 1;
        goto L23
    L26:
        r19 = true;
    L28:
        this.aP[r8][r5] = j.a(a(r05, r04, r03, 4), r04, r03, r19);
    L29:
        r112 = r112 + 1;
        goto L12
    L31:
        if (bc == false) goto L33;
        System.gc();
    L33:
        this.aH = r02;
    L35:
        if (bc == false) goto L46;
        System.gc();
        return;
    }

    /* JADX WARN: Type inference failed for: r1v4, types: [javax.microedition.lcdui.Image[], javax.microedition.lcdui.Image[][]] */
    private void p(int r5) {
        if (this.aP != null) goto L6;
        this.aP = new Image[this.k];
    L6:
        if (this.aP[r5] != null) goto L9;
        this.aP[r5] = new Image[this.ab];
        return;
    }

    final void e(int r5, int r6) {
        if (this.aP != null) goto L5;
        return;
    L5:
        if (r5 < this.aP.length) goto L7;
        return;
    L7:
        if (r6 != (-1)) goto L11;
        this.aP[r5] = null;
        return;
    L11:
        if (this.aP[r5] == null) goto L16;
        this.aP[r5][r6] = null;
        return;
    }

    private int q(int r5) {
        if (this.ah == null) goto L12;
        int r6 = 0;
    L6:
        if (r6 >= this.ah.length) goto L16;
        if (this.ah[r6] == r5) goto L10;
        r6 = r6 + 2;
        goto L6
    L10:
        return this.ah[r6 + 1];
    L16:
        return -1;
    L12:
        return -1;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v326, types: [int] */
    /* JADX WARN: Type inference failed for: r0v328, types: [int] */
    /* JADX WARN: Type inference failed for: r0v330, types: [int] */
    /* JADX WARN: Type inference failed for: r0v332, types: [int] */
    /* JADX WARN: Type inference failed for: r1v60, types: [javax.microedition.lcdui.Image[], javax.microedition.lcdui.Image[][]] */
    public final void a(Graphics r15, int r16, int r17, int r18, int r19) {
        if ((r19 & 1) == 0) goto L7;
        if ((r19 & 2) == 0) goto L7;
    L12:
        int r0 = j.e - r18;
        int r24 = this.af[r16];
        if ((r19 & 7) <= 3) goto L16;
        r24 = this.ae[r16];
    L16:
        if (r24 >= 0) goto L18;
        r24 = r24 + 255;
    L18:
        int r172 = r0 - r24;
        int r182 = r17;
        if (this.aA < 0) goto L21;
        r16 = this.az[this.aA][r16];
    L21:
        int r23 = n(r16);
        int r242 = m(r16);
        if ((r19 & 4) == 0) goto L25;
        r23 = r242;
        r242 = r23;
    L25:
        if (this.aU[r16] == 0) goto L136;
        if (r15 == null) goto L136;
        r15.setColor(this.aW[r16]);
        switch(this.aU[r16]) {
            case 1: goto L38;
            case 2: goto L30;
            case 3: goto L40;
            case 4: goto L40;
            case 5: goto L247;
            case 6: goto L57;
            case 7: goto L57;
            case 8: goto L86;
            case 9: goto L97;
            default: goto L247;
        };
    L30:
        int r02 = r23;
        int r03 = r242;
        if ((this.aW[r16] & (-16777216)) != (-16777216)) goto L33;
    L34:
        j.b(r15, r182, r18, r03, r02);
        return;
    L33:
        if ((this.aW[r16] & (-16777216)) == 0) goto L34;
        j.h(this.aW[r16]);
        j.d(r15, r182, r18, r03, r02);
        return;
    L38:
        int r04 = r23;
        j.c(j.a, r17, r18, r242 - 1, r04 - 1);
        return;
    L40:
        int r05 = q(r16);
        if (r05 == (-1)) goto L248;
        short r28 = this.ag[r05];
        short r29 = this.ag[r05 + 1];
        if ((r19 & 1) == 0) goto L46;
        r28 = 90 - r28;
    L46:
        if ((r19 & 2) == 0) goto L49;
        r28 = -r28;
        r29 = -r29;
    L49:
        if ((r19 & 4) == 0) goto L52;
        r28 = r28 - 90;
    L52:
        if (this.aU[r16] != 3) goto L55;
        r15.drawArc(r172, r182, r23, r242, r28, r29);
        return;
    L55:
        r15.fillArc(r172, r182, r23, r242, r28, r29);
        return;
    L248:
        return;
    L57:
        int r06 = q(r16);
        if (r06 == (-1)) goto L249;
        int r282 = this.ag[r06];
        int r292 = this.ag[r06 + 1];
        int r30 = this.ag[r06 + 2];
        int r31 = this.ag[r06 + 3];
        if ((r19 & 1) == 0) goto L67;
        r282 = -r282;
        r30 = -r30;
        if (Math.abs(r282) <= Math.abs(r30)) goto L64;
        int r1 = Math.abs(r282);
    L65:
        r172 = r172 + r1;
        goto L67
    L64:
        r1 = Math.abs(r30);
    L67:
        if ((r19 & 2) == 0) goto L74;
        r292 = -r292;
        r31 = -r31;
        if (Math.abs(r292) <= Math.abs(r31)) goto L71;
        int r12 = Math.abs(r292);
    L72:
        r182 = r182 + r12;
        goto L74
    L71:
        r12 = Math.abs(r31);
    L74:
        if ((r19 & 4) == 0) goto L81;
        if (Math.abs(r292) <= Math.abs(r31)) goto L78;
        int r07 = Math.abs(r292);
    L79:
        int r25 = r07;
        int r08 = r182 - r17;
        r182 = r17 + (r172 - r172);
        r172 = (r172 - r08) + r25;
        int r09 = r282;
        r282 = -r292;
        r292 = r09;
        int r010 = r30;
        r30 = -r31;
        r31 = r010;
        goto L81
    L78:
        r07 = Math.abs(r31);
    L81:
        if (this.aU[r16] != 6) goto L84;
        r15.drawLine(r172, r182, r172 + r282, r182 + r292);
        r15.drawLine(r172 + r282, r182 + r292, r172 + r30, r182 + r31);
        r15.drawLine(r172, r182, r172 + r30, r182 + r31);
        return;
    L84:
        j.a(r17, r18, r17 + r282, r18 + r292, r17 + r30, r18 + r31);
        return;
    L249:
        return;
    L86:
        int r27 = r172;
        int r283 = r172 + r23;
        int r293 = r182;
        int r302 = r182 + r242;
        if ((r19 & 1) == 0) goto L90;
        r27 = r283;
        r283 = r27;
    L90:
        if ((r19 & 2) == 0) goto L93;
        r293 = r302;
        r302 = r17;
    L93:
        if ((r19 & 4) == 0) goto L95;
        int r011 = r27;
        r27 = r283;
        r283 = r011;
    L95:
        j.a(r15, r27, r293, r283, r302);
        return;
    L97:
        int r012 = q(r16);
        if (r012 == (-1)) goto L250;
        int r284 = this.aW[r16];
        int r294 = (this.ag[r012] & 65535) | ((this.ag[r012 + 1] << 16) & (-65536));
        short r013 = this.ag[r012 + 2];
        int r014 = r23;
        int r015 = r242;
        if ((r19 & 1) == 0) goto L105;
        if (r013 >= 2) goto L105;
        r284 = r294;
        r294 = r284;
    L105:
        if ((r19 & 2) == 0) goto L110;
        if (r013 <= 1) goto L110;
        int r016 = r284;
        r284 = r294;
        r294 = r016;
    L110:
        if (r013 != 0) goto L113;
        int r303 = 4;
    L120:
        if ((r19 & 4) != 0) goto L122;
    L133:
        j.b(r172, r182, r014, r015, r284, r294, r303);
        return;
    L122:
        if (r303 != 4) goto L125;
        r303 = 16;
        goto L133
    L125:
        if (r303 != 8) goto L128;
        r303 = 32;
        goto L133
    L128:
        if (r303 != 16) goto L131;
        r303 = 8;
        goto L133
    L131:
        if (r303 != 32) goto L133;
        r303 = 4;
        goto L133
    L113:
        if (r013 != 1) goto L116;
        r303 = 8;
        goto L120
    L116:
        if (r013 != 2) goto L118;
        r303 = 16;
        goto L120
    L118:
        r303 = 32;
        goto L120
    L250:
        return;
    L247:
        return;
    L136:
        if (aa != 1) goto L154;
        if (this.aU[r16] != 5) goto L141;
        r172 = r172 - (r23 >> 1);
        r182 = r182 - (r242 >> 1);
    L141:
        if (r172 >= aX) goto L144;
        aX = r172;
    L144:
        if (r182 >= aY) goto L147;
        aY = r182;
    L147:
        if ((r172 + r23) <= aZ) goto L150;
        aZ = r172 + r23;
    L150:
        if ((r182 + r242) <= ba) goto L251;
        ba = r182 + r242;
        return;
    L251:
        return;
    L154:
        if (r23 > 0) goto L156;
        return;
    L156:
        if (r242 > 0) goto L159;
        return;
    L159:
        if (j.e(r15) == false) goto L169;
        int r017 = j.a(r15);
        int r018 = j.b(r15);
        int r019 = j.c(r15);
        int r020 = j.d(r15);
        if ((r172 + r23) >= r017) goto L163;
        return;
    L163:
        if ((r182 + r242) >= r018) goto L165;
        return;
    L165:
        if (r172 < (r017 + r019)) goto L167;
        return;
    L167:
        if (r182 < (r018 + r020)) goto L169;
        return;
    L169:
        int[] r272 = null;
        Image r285 = null;
        if ((this.aD & 16777224) != 0) goto L172;
        return;
    L172:
        if (this.aP == null) goto L177;
        if (this.aP[this.aH] == null) goto L177;
        r285 = this.aP[this.aH][r16];
    L177:
        if (this.aO != null) goto L179;
    L181:
        boolean r021 = this.aI;
        if (r285 != null) goto L233;
        int[] r022 = r272;
        int[] r312 = r022;
        if (r022 != null) goto L187;
        r312 = r(r16);
    L187:
        if (r312 != null) goto L190;
        return;
    L190:
        if (this.z < 0) goto L228;
        if (this.aP != null) goto L197;
        this.aP = new Image[this.k];
        int r22 = 0;
    L195:
        if (r22 >= this.k) goto L197;
        this.aP[r22] = new Image[this.ab];
        r22 = r22 + 1;
    L197:
        int[] r023 = new int[r23 * r242];
        int r20 = r242;
        int r252 = r23;
        if ((r19 & 4) == 0) goto L200;
        r20 = r23;
        r252 = r242;
    L200:
        int r26 = 0;
    L202:
        if (r26 >= r20) goto L209;
        int r273 = 0;
    L205:
        if (r273 >= r252) goto L207;
        r023[((r252 - r273) - 1) + (r26 * r252)] = r312[r26 + (r20 * r273)];
        r273 = r273 + 1;
        goto L205
    L207:
        r26 = r26 + 1;
        goto L202
    L209:
        if ((r19 & 4) == 0) goto L211;
        Image r286 = j.a(r023, r242, r23, this.aI);
    L212:
        a(r16, r286);
        Image r13 = r286;
        int r6 = aQ[r19 & 7];
        if (r13 != null) goto L215;
        boolean r024 = false;
    L216:
        if (r024 == true) goto L218;
        return;
    L218:
        if (aQ[r19 & 7] != 0) goto L222;
        r15.drawImage(r286, r172, r182, 0);
        return;
    L222:
        if ((r19 & 4) == 0) goto L225;
        r15.drawRegion(r286, 0, 0, r242, r23, aQ[r19 & 7], r172, r182, 0);
        return;
    L225:
        r15.drawRegion(r286, 0, 0, r23, r242, aQ[r19 & 7], r172, r182, 0);
        return;
    L215:
        r024 = true;
        goto L216
    L211:
        r286 = j.a(r023, r23, r242, this.aI);
        goto L212
    L228:
        if ((this.aK & 8) == 0) goto L231;
        j.a(r15, r312, 0, r242, r182, r18, r242, r23, r021, r021, r19, -1, false);
        return;
    L231:
        j.a(r15, r312, 0, r242, r182, r18, r242, r23, r021, r021, r19, -1, true);
        return;
    L233:
        int r025 = m(r16);
        int r026 = n(r16);
        a(r16, r285);
        Image r14 = r285;
        int r62 = aQ[r19 & 7];
        if (r14 != null) goto L236;
        boolean r027 = false;
    L237:
        if (r027 == true) goto L239;
        return;
    L239:
        if (aQ[r19 & 7] != 0) goto L242;
        r15.drawImage(r285, r172, r182, 0);
        return;
    L242:
        r15.drawRegion(r285, 0, 0, r026, r025, aQ[r19 & 7], r172, r182, 0);
        return;
    L236:
        r027 = true;
        goto L237
    L179:
        if (this.aO[this.aH] == null) goto L181;
        r272 = this.aO[this.aH][r16];
    L7:
        if ((r19 & 1) == 0) goto L10;
        r19 = (r19 & (-2)) | 2;
        goto L12
    L10:
        if ((r19 & 2) == 0) goto L12;
        r19 = (r19 & (-3)) | 1;
        goto L12
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [short[], short[][]] */
    /* JADX WARN: Type inference failed for: r0v3, types: [b[], b[][]] */
    public static void i(int r2) {
        b = new short[r2];
        c = new b[r2];
        A = new int[r2];
        B = new int[r2];
    }

    public static void f(int r4, int r5) {
        B[r4] = r5;
        b[r4] = new short[r5];
        c[r4] = new b[r5];
        int r52 = 0;
    L4:
        if (r52 >= b[r4].length) goto L6;
        b[r4][r52] = -1;
        r52 = r52 + 1;
        goto L4
    }

    /* JADX WARN: Type inference failed for: r1v3, types: [javax.microedition.lcdui.Image[], javax.microedition.lcdui.Image[][]] */
    public final void j(int r5) {
        this.z = r5;
        if (this.aP != null) goto L8;
        this.aP = new Image[this.k];
        int r52 = 0;
    L6:
        if (r52 >= this.k) goto L10;
        this.aP[r52] = new Image[this.ab];
        r52 = r52 + 1;
        goto L6
    L10:
        return;
    }

    private final void a(int r7, Object r8) {
        if (this.z >= 0) goto L5;
        return;
    L5:
        if (this.aP[this.aH][r7] != null) goto L14;
        int r0 = A[this.z];
        short r02 = b[this.z][r0];
        int r03 = r02 >> 10;
        int r04 = r02 & 1023;
        if (r02 < 0) goto L11;
        b r05 = c[this.z][r0];
        if (r05 == null) goto L11;
        r05.aP[r03][r04] = null;
    L11:
        short r06 = (short) ((r7 & 1023) + (this.aH << 10));
        b[this.z][r0] = r06;
        c[this.z][r0] = this;
        A[this.z] = (A[this.z] + 1) % B[this.z];
        this.aP[this.aH][r7] = (Image) r8;
        return;
    }

    private int[] r(int r8) {
        if (o(r8) == true) goto L5;
        return null;
    L5:
        if (a(this.aB, this.aC[r8], m(r8), n(r8)) == true) goto L7;
        return null;
    L7:
        return g;
    }

    private boolean a(byte[] r7, int r8, int r9, int r10) {
        int r11 = 0;
        int r92 = r9 * r10;
        if (g != null) goto L6;
        g = new int[40960];
    L6:
        if (this.j != null) goto L9;
        return false;
    L9:
        int[] r0 = this.j[this.aH];
        if (r0 != null) goto L14;
        return false;
    L14:
        if (this.aL != 25840) goto L22;
    L16:
        if (r11 >= r92) goto L113;
        int r1 = r8;
        r8 = r8 + 1;
        int r02 = r7[r1] & 255;
        int r03 = r0[r02 & this.aM];
        int r13 = r02 >> this.aN;
    L18:
        int r04 = r13;
        r13 = r04 - 1;
        if (r04 < 0) goto L16;
        int r12 = r11;
        r11 = r11 + 1;
        g[r12] = r03;
        goto L18
    L113:
        return true;
    L22:
        if (this.aL != 22258) goto L37;
        int r82 = r8 - 1;
        int r112 = 0 - 1;
        int r93 = r92 - 1;
    L25:
        if (r112 >= r93) goto L114;
        r82 = r82 + 1;
        byte r05 = r7[r82];
        int r132 = r05;
        if (r05 < 0) goto L28;
        r82 = r82 + 1;
        int r06 = r0[r7[r82] & 255];
    L33:
        r112 = r112 + 1;
        g[r112] = r06;
        r132 = r132 - 1;
        if (r132 > 0) goto L33;
    L28:
        int r133 = r132 + 128;
    L29:
        r112 = r112 + 1;
        r82 = r82 + 1;
        g[r112] = r0[r7[r82] & 255];
        r133 = r133 - 1;
        if (r133 > 0) goto L29;
    L114:
        return true;
    L37:
        if (this.aL != 5632) goto L47;
        if ((r92 & 1) != 0) goto L41;
        int r14 = 0;
    L42:
        int r94 = (r92 + r14) >> 1;
    L43:
        r94 = r94 - 1;
        if (r94 < 0) goto L115;
        int r15 = r8;
        r8 = r8 + 1;
        byte r07 = r7[r15];
        int r16 = r11;
        int r113 = r11 + 1;
        g[r16] = r0[(r07 >> 4) & 15];
        r11 = r113 + 1;
        g[r113] = r0[r07 & 15];
        goto L43
    L115:
        return true;
    L41:
        r14 = 2;
        goto L42
    L47:
        if (this.aL != 1024) goto L57;
        if ((r92 & 3) != 0) goto L51;
        int r17 = 0;
    L52:
        int r95 = (r92 + r17) >> 2;
    L53:
        r95 = r95 - 1;
        if (r95 < 0) goto L116;
        int r18 = r8;
        r8 = r8 + 1;
        byte r08 = r7[r18];
        int r19 = r11;
        int r114 = r11 + 1;
        g[r19] = r0[(r08 >> 6) & 3];
        int r115 = r114 + 1;
        g[r114] = r0[(r08 >> 4) & 3];
        int r116 = r115 + 1;
        g[r115] = r0[(r08 >> 2) & 3];
        r11 = r116 + 1;
        g[r116] = r0[r08 & 3];
        goto L53
    L116:
        return true;
    L51:
        r17 = 4;
        goto L52
    L57:
        if (this.aL != 512) goto L67;
        if ((r92 & 7) != 0) goto L61;
        int r110 = 0;
    L62:
        int r96 = (r92 + r110) >> 3;
    L63:
        r96 = r96 - 1;
        if (r96 < 0) goto L117;
        int r111 = r8;
        r8 = r8 + 1;
        byte r09 = r7[r111];
        int r117 = r11;
        int r118 = r11 + 1;
        g[r117] = r0[(r09 >> 7) & 1];
        int r119 = r118 + 1;
        g[r118] = r0[(r09 >> 6) & 1];
        int r1110 = r119 + 1;
        g[r119] = r0[(r09 >> 5) & 1];
        int r1111 = r1110 + 1;
        g[r1110] = r0[(r09 >> 4) & 1];
        int r1112 = r1111 + 1;
        g[r1111] = r0[(r09 >> 3) & 1];
        int r1113 = r1112 + 1;
        g[r1112] = r0[(r09 >> 2) & 1];
        int r1114 = r1113 + 1;
        g[r1113] = r0[(r09 >> 1) & 1];
        r11 = r1114 + 1;
        g[r1114] = r0[r09 & 1];
        goto L63
    L117:
        return true;
    L61:
        r110 = 8;
        goto L62
    L67:
        if (this.aL != 22018) goto L72;
    L68:
        r92 = r92 - 1;
        if (r92 < 0) goto L118;
        int r120 = r11;
        r11 = r11 + 1;
        int r4 = r8;
        r8 = r8 + 1;
        g[r120] = r0[r7[r4] & 255];
        goto L68
    L118:
        return true;
    L72:
        if (this.aL == (-22976)) goto L74;
        return true;
    L74:
        if (r11 >= r92) goto L79;
        int r121 = r8;
        r8 = r8 + 1;
        int r010 = r7[r121] & 255;
        int r011 = r0[r010 & this.aM];
        int r134 = r010 >> this.aN;
    L76:
        int r012 = r134;
        r134 = r012 - 1;
        if (r012 < 0) goto L74;
        int r122 = r11;
        r11 = r11 + 1;
        g[r122] = r011;
        goto L76
    L79:
        int r1115 = 0;
    L81:
        if (r1115 >= r92) goto L119;
        int r123 = r8;
        r8 = r8 + 1;
        int r013 = r7[r123] & 255;
        if (r013 == 254) goto L84;
        g[r1115] = (r013 << 24) | (g[r1115] & 16777215);
        r1115 = r1115 + 1;
        goto L81
    L84:
        int r83 = r8 + 1;
        int r6 = r7[r8] & 255;
        r8 = r83 + 1;
        int r014 = r7[r83] & 255;
    L85:
        int r015 = r6;
        r6 = r015 - 1;
        if (r015 <= 0) goto L81;
        g[r1115] = (r014 << 24) | (g[r1115] & 16777215);
        r1115 = r1115 + 1;
        goto L85
    L119:
        return true;
    }

    /* JADX WARN: Type inference failed for: r1v4, types: [short[], short[][]] */
    public final void a(short[] r7) {
        this.R = r7[0];
        this.Q = new short[this.R];
        int r8 = 1;
        int r9 = 0;
    L4:
        if (r9 >= this.R) goto L6;
        this.Q[r9] = new short[2];
        int r3 = r8;
        int r82 = r8 + 1;
        this.Q[r9][0] = r7[r3];
        r8 = r82 + 1;
        this.Q[r9][1] = r7[r82];
        r9 = r9 + 1;
        goto L4
    L6:
        int r92 = r8;
    L8:
        if (r92 >= r7.length) goto L14;
        int r1 = r92;
        int r93 = r92 + 1;
        short r0 = r7[r1];
        r92 = r93 + 1;
        short r02 = r7[r93];
        short[] r03 = new short[(r02 << 1) + 2];
        r03[0] = this.Q[r0][0];
        r03[1] = this.Q[r0][1];
        int r12 = 0;
    L11:
        if (r12 >= r02) goto L13;
        int r32 = r92;
        int r94 = r92 + 1;
        r03[(r12 << 1) + 2] = r7[r32];
        r92 = r94 + 1;
        r03[(r12 << 1) + 3] = r7[r94];
        r12 = r12 + 1;
        goto L11
    L13:
        this.Q[r0] = r03;
        goto L8
    L14:
        this.N = -j(0, 0);
        this.J = this.N + j(0, 1);
        this.K = j(0, 2) - j(0, 1);
        this.L = i(s(32), 0);
    }

    private int s(int r5) {
        if (this.Q != null) goto L6;
        return 0;
    L6:
        int r0 = r5 % this.R;
        if (this.Q[r0][0] == r5) goto L9;
        int r7 = 2;
        int r02 = this.Q[r0].length;
    L12:
        if (r7 >= r02) goto L17;
        if (this.Q[r0][r7] == r5) goto L17;
        r7 = r7 + 2;
    L17:
        if (r7 < r02) goto L21;
        return 1;
    L21:
        return this.Q[r0][r7 + 1];
    L9:
        return this.Q[r0][1];
    }

    final int k(int r6) {
        return (r6 * this.J) + ((r6 - 1) * this.K);
    }

    public final short[] a(String r6, int r7, boolean r8) {
        if (U != null) goto L5;
        U = new short[250];
    L5:
        int r0 = r6.length();
        short r9 = 0;
        short r10 = 1;
        short r11 = 0;
        boolean r12 = this.f;
        boolean r13 = false;
        short r14 = 0;
        boolean r15 = this.f;
        int r16 = 0;
    L7:
        if (r16 >= r0) goto L64;
        char r02 = r6.charAt(r16);
        if (r02 != ' ') goto L25;
        r9 = (short) (r9 + ((short) this.L));
        r11 = (short) r16;
        r12 = r15;
        r13 = true;
        r14 = 0;
        if (r9 <= r7) goto L63;
        r13 = false;
        int r17 = r11;
    L14:
        if (r17 < 0) goto L19;
        if (r6.charAt(r17) != ' ') goto L19;
        r9 = (short) (r9 - ((short) this.L));
        r17 = r17 - 1;
    L19:
        if (r11 >= r0) goto L23;
        if (r6.charAt(r11) != ' ') goto L23;
        r11 = (short) (r11 + 1);
    L23:
        short r03 = (short) (r11 - 1);
        r11 = r03;
        r16 = r03;
        r15 = r12;
        short r2 = (short) (r10 + 1);
        U[r10] = (short) (r11 + 1);
        r10 = (short) (r2 + 1);
        U[r2] = r9;
        r9 = 0;
    L63:
        r16 = r16 + 1;
        goto L7
    L25:
        if (r02 != '\\') goto L35;
        r16 = r16 + 1;
        if (r6.charAt(r16) != '^') goto L63;
        if (r15 == true) goto L31;
        boolean r04 = true;
    L32:
        r15 = r04;
        goto L63
    L31:
        r04 = false;
        goto L32
    L35:
        if (r02 != '\n') goto L38;
        short r22 = (short) (r10 + 1);
        U[r10] = (short) r16;
        r10 = (short) (r22 + 1);
        U[r22] = r9;
        r9 = 0;
        r14 = 0;
        goto L63
    L38:
        if (r02 < ' ') goto L40;
        int r172 = s(r02);
    L47:
        if (r172 <= c()) goto L49;
        r172 = 0;
    L49:
        int r173 = i(r172, 0);
        if (r15 == false) goto L52;
        r173 = r173 + 1;
    L52:
        r14 = (short) (r14 + ((short) r173));
        short r05 = (short) (r9 + ((short) r173));
        r9 = r05;
        if (r05 <= r7) goto L63;
        if (r13 == false) goto L63;
        r13 = false;
        int r152 = r11;
    L58:
        if (r152 < 0) goto L62;
        if (r6.charAt(r152) != ' ') goto L62;
        r9 = (short) (r9 - ((short) this.L));
        r152 = r152 - 1;
    L62:
        short r23 = (short) (r10 + 1);
        U[r10] = (short) (r11 + 1);
        r10 = (short) (r23 + 1);
        U[r23] = (short) (r9 - r14);
        r9 = 0;
        r16 = r11;
        r15 = r12;
        goto L63
    L40:
        if (r02 != 1) goto L43;
        r16 = r16 + 1;
        goto L63
    L43:
        if (r02 != 2) goto L63;
        r16 = r16 + 1;
        r172 = r6.charAt(r16);
        goto L47
    L64:
        short r24 = (short) (r10 + 1);
        U[r10] = (short) r0;
        U[r24] = r9;
        U[0] = (short) (((short) (r24 + 1)) / 2);
        return U;
    }

    final String a(String r8, int r9) {
        String r92 = "";
        short r10 = 0;
        short[] r0 = a(r8, 390, false);
        int r11 = 0;
    L4:
        if (r11 >= r0[0]) goto L14;
        if (r10 == 0) goto L12;
        if (r10 < r8.length()) goto L10;
    L11:
        r92 = new StringBuffer().append(r92).append("\n").toString();
        goto L12
    L10:
        if (r8.charAt(r10) != '\n') goto L11;
    L12:
        r92 = new StringBuffer().append(r92).append(r8.substring(r10, r0[(r11 << 1) + 1])).toString();
        r10 = r0[(r11 << 1) + 1];
        r11 = r11 + 1;
        goto L4
    L14:
        return r92;
    }

    public final void a(Graphics r9, String r10, short[] r11, int r12, int r13, int r14, int r15, int r16, int r17) {
        short r0 = r11[0];
        int r02 = this.J;
        if (r15 != (-1)) goto L6;
        r15 = r0;
    L6:
        if ((r14 + r15) <= r0) goto L8;
        r15 = r0 - r14;
    L8:
        int r03 = this.K + r02;
        if ((r16 & 32) == 0) goto L12;
        r13 = r13 - (r03 * (r15 - 1));
    L14:
        H = this.aH;
        if (r17 >= 0) goto L17;
    L21:
        int r172 = 0;
        int r142 = r14;
    L23:
        if (r142 >= r0) goto L50;
        if (r172 > (r15 - 1)) goto L50;
        if (r142 <= 0) goto L29;
        short r04 = r11[((r142 - 1) << 1) + 1];
    L30:
        V = r04;
        W = r11[(r142 << 1) + 1];
        if (V < r10.length()) goto L33;
    L35:
        int r20 = r12;
        int r21 = r13 + (r172 * r03);
        if ((r16 & 43) == 0) goto L49;
        if ((r16 & 8) == 0) goto L41;
        r20 = r12 - r11[(r142 + 1) << 1];
    L44:
        if ((r16 & 32) == 0) goto L47;
        r21 = r21 - this.J;
        goto L49
    L47:
        if ((r16 & 2) == 0) goto L49;
        r21 = r21 - (this.J >> 1);
        goto L49
    L41:
        if ((r16 & 1) == 0) goto L44;
        r20 = r12 - (r11[(r142 + 1) << 1] >> 1);
    L49:
        a(r9, r10, r20, r21, 0, false);
        r142 = r142 + 1;
        r172 = r172 + 1;
        goto L23
    L33:
        if (r10.charAt(V) != '\n') goto L35;
        V++;
        goto L35
    L29:
        r04 = 0;
    L50:
        V = -1;
        W = -1;
        X = -1;
        this.aH = H;
        return;
    L17:
        if (r14 <= 0) goto L19;
        short r05 = r11[((r14 - 1) << 1) + 1];
    L20:
        X = r05;
        X = r05 + r17;
        goto L21
    L19:
        r05 = 0;
        goto L20
    L12:
        if ((r16 & 2) == 0) goto L14;
        r13 = r13 - ((r03 * (r15 - 1)) >> 1);
        goto L14
    }

    final void a(String r6, char[] r7) {
        if (r6 == null) goto L5;
    L7:
        d = 0;
        e = this.J;
        int r8 = 0;
        if (r6 == null) goto L10;
        boolean r0 = true;
    L11:
        boolean r9 = r0;
        if (V < 0) goto L14;
        int r02 = V;
    L15:
        int r10 = r02;
        if (r9 == false) goto L23;
        if (W < 0) goto L20;
        int r03 = W;
    L21:
        int r11 = r03;
    L27:
        boolean r12 = this.f;
        int r102 = r10;
    L29:
        if (r102 >= r11) goto L76;
        if (r9 == false) goto L33;
        char r04 = r6.charAt(r102);
    L34:
        char r1 = r04;
        if (r04 != '\\') goto L48;
        r102 = r102 + 1;
        if (r9 == false) goto L39;
        char r05 = r6.charAt(r102);
    L41:
        if (r05 != '^') goto L74;
        if (r12 == true) goto L45;
        boolean r06 = true;
    L46:
        r12 = r06;
        goto L74
    L45:
        r06 = false;
    L74:
        r102 = r102 + 1;
        goto L29
    L39:
        r05 = r7[r102];
        goto L41
    L48:
        if (r1 <= ' ') goto L51;
        int r07 = s(r1);
    L71:
        r8 = r8 + i(r07, 0);
        if (r12 == false) goto L74;
        r8 = r8 + 1;
        goto L74
    L51:
        if (r1 != ' ') goto L54;
        r8 = r8 + this.L;
        goto L74
    L54:
        if (r1 != ' ') goto L57;
        r8 = r8 + this.L;
        goto L74
    L57:
        if (r1 != '\n') goto L63;
        if (r8 <= d) goto L61;
        d = r8;
    L61:
        r8 = 0;
        e += this.K + this.J;
        goto L74
    L63:
        if (r1 != 1) goto L66;
        r102 = r102 + 1;
        goto L74
    L66:
        if (r1 != 2) goto L74;
        r102 = r102 + 1;
        if (r9 == false) goto L70;
        r07 = r6.charAt(r102);
        goto L71
    L70:
        r07 = r7[r102];
        goto L71
    L33:
        r04 = r7[r102];
        goto L34
    L76:
        if (r8 <= d) goto L79;
        d = r8;
    L79:
        if (d <= 0) goto L92;
        d = d;
        return;
    L92:
        return;
    L20:
        r03 = r6.length();
        goto L21
    L23:
        if (W < 0) goto L25;
        int r08 = W;
    L26:
        r11 = r08;
        goto L27
    L25:
        r08 = r7.length;
        goto L26
    L14:
        r02 = 0;
        goto L15
    L10:
        r0 = false;
        goto L11
    L5:
        if (r7 != null) goto L7;
    }

    public final void a(Graphics r2, String r3, int r4, int r5, int r6, int r7) {
    }

    public final void a(Graphics r9, String r10, int r11, int r12, int r13) {
        a(r9, r10, r11, r12, r13, true);
    }

    private void a(Graphics r11, String r12, int r13, int r14, int r15, boolean r16) {
        int r132 = r13;
        if (r12 == null) goto L126;
        int r142 = r14 + this.N;
        if (r12 == null) goto L7;
        boolean r0 = true;
    L8:
        boolean r17 = r0;
        a(r12, null);
        if ((r15 & 43) != 0) goto L11;
    L22:
        int r152 = r132;
        int r143 = r142;
        if (r16 == false) goto L26;
        H = this.aH;
    L26:
        if (V < 0) goto L28;
        int r02 = V;
    L29:
        int r18 = r02;
        if (r17 == false) goto L37;
        if (W < 0) goto L34;
        int r03 = W;
    L35:
        int r20 = r03;
    L42:
        if (X >= 0) goto L44;
    L46:
        int r21 = r18;
    L48:
        if (r21 >= r20) goto L113;
        if (r17 == false) goto L52;
        char r04 = r12.charAt(r21);
    L53:
        char r1 = r04;
        if (r04 != '\\') goto L75;
        r21 = r21 + 1;
        if (r17 == false) goto L58;
        char r05 = r12.charAt(r21);
    L59:
        char r19 = r05;
        if (r05 != '_') goto L67;
        if (this.O == true) goto L64;
        boolean r110 = true;
    L65:
        this.O = r110;
    L111:
        r21 = r21 + 1;
        goto L48
    L64:
        r110 = false;
        goto L65
    L67:
        if (r19 == '^') goto L69;
        l((r19 & 255) - 48);
        goto L111
    L69:
        if (this.f == true) goto L71;
        boolean r111 = true;
    L72:
        this.f = r111;
        goto L111
    L71:
        r111 = false;
        goto L72
    L58:
        char[] r06 = null;
        r05 = r06[r21];
        goto L59
    L75:
        if (r1 <= ' ') goto L78;
        int r07 = s(r1);
    L104:
        int r22 = r07;
        a(r11, r22, r152, r143, 0, 0, 0);
        if (this.O == false) goto L108;
        int r08 = s(95);
        a(r11, r08, r152 + ((i(r22, 0) - i(r08, 0)) >> 1), r143, 0, 0, 0);
    L108:
        if (this.f == false) goto L110;
        r152 = r152 + 1;
        a(r11, r22, r152, r143, 0, 0, 0);
    L110:
        r152 = r152 + i(r22, 0);
        goto L111
    L78:
        if (r1 != ' ') goto L84;
        if (this.O == false) goto L82;
        int r09 = s(95);
        a(r11, r09, r152 + ((this.L - i(r09, 0)) >> 1), r143, 0, 0, 0);
    L82:
        r152 = r152 + this.L;
        goto L111
    L84:
        if (r1 != '\n') goto L87;
        r152 = r132;
        r143 = r143 + (this.K + this.J);
        goto L111
    L87:
        if (r1 != 1) goto L99;
        r21 = r21 + 1;
        if (r17 == false) goto L91;
        char r010 = r12.charAt(r21);
    L92:
        char r112 = r010;
        if (r010 >= this.k) goto L96;
        l(r112);
    L96:
        if (r112 != 255) goto L111;
        this.aH = H;
        goto L111
    L91:
        char[] r011 = null;
        r010 = r011[r21];
        goto L92
    L99:
        if (r1 != 2) goto L111;
        r21 = r21 + 1;
        if (r17 == false) goto L103;
        r07 = r12.charAt(r21);
        goto L104
    L103:
        char[] r012 = null;
        r07 = r012[r21];
        goto L104
    L52:
        char[] r013 = null;
        r04 = r013[r21];
        goto L53
    L113:
        if (r16 == false) goto L127;
        this.aH = H;
        return;
    L127:
        return;
    L44:
        if (r20 <= X) goto L46;
        r20 = X;
        goto L46
    L34:
        r03 = r12.length();
        goto L35
    L37:
        if (W < 0) goto L39;
        int r014 = W;
    L40:
        r20 = r014;
        goto L42
    L39:
        Object[] r015 = null;
        r014 = r015.length;
        goto L40
    L28:
        r02 = 0;
        goto L29
    L11:
        if ((r15 & 8) == 0) goto L14;
        r132 = r132 - d;
    L17:
        if ((r15 & 32) == 0) goto L20;
        r142 = r142 - e;
        goto L22
    L20:
        if ((r15 & 2) == 0) goto L22;
        r142 = r142 - (e >> 1);
        goto L22
    L14:
        if ((r15 & 1) == 0) goto L17;
        r132 = r132 - (d >> 1);
        goto L17
    L7:
        r0 = false;
        goto L8
    }

    public final void l(int r4) {
        if (r4 < this.k) goto L5;
        return;
    L5:
        if (r4 < 0) goto L9;
        this.aH = r4;
        return;
    }

    final int b() {
        return this.aH;
    }

    final void g(int r7, int r8) {
        boolean r3 = this.aI;
        boolean r4 = this.aJ;
        int r0 = this.j[r7].length;
        if (r4 == false) goto L9;
        int r9 = r0 - 1;
    L6:
        if (r9 < 0) goto L23;
        this.j[r7][r9] = (((((this.j[r7][r9] >>> 24) * r8) >> 8) & 255) << 24) | (this.j[r7][r9] & 16777215);
        r9 = r9 - 1;
        goto L6
    L23:
        return;
    L9:
        if (r3 == false) goto L19;
        int r02 = (r8 & 255) << 24;
        int r92 = r0 - 1;
    L12:
        if (r92 < 0) goto L30;
        if ((this.j[r7][r92] & 16777215) == 16711935) goto L18;
        if ((this.j[r7][r92] >> 24) == 0) goto L18;
        this.j[r7][r92] = r02 | (this.j[r7][r92] & 16777215);
    L18:
        r92 = r92 - 1;
        goto L12
    L30:
        return;
    L19:
        int r03 = (r8 & 255) << 24;
        int r93 = r0 - 1;
    L21:
        if (r93 < 0) goto L31;
        this.j[r7][r93] = r03 | (this.j[r7][r93] & 16777215);
        r93 = r93 - 1;
        goto L21
    }

    final void h(int r7, int r8) {
        this.aI = true;
        this.aJ = true;
        int r9 = 0;
    L4:
        if (r9 >= this.j[r7].length) goto L11;
        if ((this.j[r7][r9] & 16777215) == 16711935) goto L10;
        if ((this.j[r7][r9] >> 24) == 0) goto L10;
        int[] r0 = this.j[r7];
        int r1 = r9;
        r0[r1] = r0[r1] & 16777215;
        int[] r02 = this.j[r7];
        int r12 = r9;
        r02[r12] = r02[r12] | ((this.j[r8][r9] & 16711680) << 8);
    L10:
        r9 = r9 + 1;
        goto L4
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v4, types: [int] */
    /* JADX WARN: Type inference failed for: r2v8 */
    private static int a(byte[] r6, int r7, short[] r8, int r9, int r10, boolean r11) {
        int r92 = 0;
    L4:
        if (r92 >= r10) goto L11;
        int r1 = r92;
        if (r11 == false) goto L8;
        int r3 = r7;
        r7 = r7 + 1;
        int r2 = r6[r3];
    L9:
        r8[r1] = (short) r2;
        r92 = r92 + 1;
        goto L4
    L8:
        int r32 = r7;
        int r72 = r7 + 1;
        r7 = r72 + 1;
        r2 = ((r6[r32] ? 1 : 0) & 255) + (((r6[r72] ? 1 : 0) & 255) << 8);
        goto L9
    L11:
        return r7;
    }

    static final int[] a(int[] r7, int r8, int r9, int r10) {
        if ((r10 & 7) == 0) goto L5;
        int[] r0 = a(r7, r8, r9);
        int r12 = 0;
        int r13 = 0;
        switch((r10 & 7)) {
            case 1: goto L8;
            case 2: goto L16;
            case 3: goto L20;
            case 4: goto L24;
            case 5: goto L31;
            case 6: goto L38;
            case 7: goto L45;
            default: goto L53;
        };
    L8:
        int r122 = r8 * r9;
        int r132 = r8 * (r9 - 1);
        int r14 = r9;
    L9:
        r14 = r14 - 1;
        if (r14 < 0) goto L53;
        int r102 = r8;
    L12:
        r102 = r102 - 1;
        if (r102 < 0) goto L15;
        r122 = r122 - 1;
        int r3 = r132;
        r132 = r132 + 1;
        r0[r122] = r7[r3];
        goto L12
    L15:
        r132 = r132 - (r8 << 1);
        goto L9
    L16:
        int r123 = (r9 - 1) * r8;
        int r142 = r9;
    L17:
        r142 = r142 - 1;
        if (r142 < 0) goto L53;
        System.arraycopy(r7, r13, r0, r123, r8);
        r123 = r123 - r8;
        r13 = r13 + r8;
        goto L17
    L20:
        int r133 = (r8 * r9) - 1;
    L22:
        if (r133 < 0) goto L53;
        int r1 = r12;
        r12 = r12 + 1;
        int r32 = r133;
        r133 = r32 - 1;
        r0[r1] = r7[r32];
        goto L22
    L24:
        int r124 = r8 * r9;
        int r143 = r9;
    L25:
        r143 = r143 - 1;
        if (r143 < 0) goto L53;
        int r134 = r143;
        int r103 = r8;
    L28:
        r103 = r103 - 1;
        if (r103 < 0) goto L25;
        r124 = r124 - 1;
        r0[r124] = r7[r134];
        r134 = r134 + r9;
        goto L28
    L31:
        int r125 = r8 * r9;
        int r144 = r9;
    L32:
        r144 = r144 - 1;
        if (r144 < 0) goto L53;
        int r135 = (r9 - 1) - r144;
        int r104 = r8;
    L35:
        r104 = r104 - 1;
        if (r104 < 0) goto L32;
        r125 = r125 - 1;
        r0[r125] = r7[r135];
        r135 = r135 + r9;
        goto L35
    L38:
        int r02 = r8 * r9;
        int r126 = r02;
        int r15 = r02 - 1;
        int r145 = r9;
    L39:
        r145 = r145 - 1;
        if (r145 < 0) goto L53;
        int r03 = r15;
        r15 = r03 - 1;
        int r136 = r03;
        int r105 = r8;
    L42:
        r105 = r105 - 1;
        if (r105 < 0) goto L39;
        r126 = r126 - 1;
        r0[r126] = r7[r136];
        r136 = r136 - r9;
        goto L42
    L45:
        int r04 = r8 * r9;
        int r127 = r04;
        int r152 = r04 - r9;
        int r146 = r9;
    L46:
        r146 = r146 - 1;
        if (r146 < 0) goto L53;
        int r05 = r152;
        r152 = r152 + 1;
        int r137 = r05;
        int r106 = r8;
    L49:
        r106 = r106 - 1;
        if (r106 < 0) goto L46;
        r127 = r127 - 1;
        r0[r127] = r7[r137];
        r137 = r137 - r9;
    L53:
        return r0;
    L5:
        return r7;
    }

    static final int[] a(int[] r3, int r4, int r5) {
        int[] r0 = a(null);
        int[] r6 = r0;
        if (r0 != r3) goto L6;
        r6 = new int[r4 * r5];
    L6:
        return r6;
    }

    public b() {
        this.z = -1;
        this.C = -1;
        this.D = -1;
        this.G = false;
        this.I = false;
        this.M = 0;
        this.O = false;
        this.f = false;
        this.P = null;
        this.S = -1;
        this.T = new int[4];
        this.bd = -1;
    }

    static {
        n = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};
        o = new byte[]{73, 72, 68, 82};
        p = new byte[]{80, 76, 84, 69};
        q = new byte[]{116, 82, 78, 83};
        r = new byte[]{73, 68, 65, 84};
        byte[] r0 = {73, 69, 78, 68};
        byte[] r02 = {8, 6, 0, 0, 0};
        s = new byte[]{8, 3, 0, 0, 0};
        t = new byte[]{0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126};
        u = new byte[]{120, -100, 1};
        v = new int[256];
        a = false;
        w = false;
        x = 0;
        y = 0;
        E = false;
        F = null;
        V = -1;
        W = -1;
        X = -1;
        Y = -1;
        Z = -1;
        aa = 0;
        aQ = new int[]{0, 2, 1, 3, 5, 7, 4, 6};
        aR = new int[]{1, 0, 3, 2, 6, 7, 4, 5};
        aS = new int[]{2, 3, 0, 1, 5, 4, 7, 6};
        aT = new int[]{4, 5, 6, 7, 3, 2, 1, 0};
        bb = new int[4];
        bc = true;
        l = false;
        m = '|';
    }
}
