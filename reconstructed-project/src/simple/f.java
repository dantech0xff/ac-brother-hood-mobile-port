package defpackage;

import java.io.InputStream;
import java.util.Hashtable;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStore;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:f.class */
public final class f implements Runnable, CommandListener {
    private static String a;
    private static String b;
    private static String c;
    private static int d;
    private static String e;
    private static Font f;
    private static int g;
    private static int h;
    private static int i;
    private static int j;
    private static int k;
    private static int l;
    private static int m;
    private static boolean[] n;
    private static int[] o;
    private static int p;
    private static int q;
    private static int r;
    private static int s;
    private static int t;
    private static int u;
    private static int v;
    private static int w;
    private static int x;
    private static int y;
    private static int z;
    private static int A;
    private static int B;
    private static int C;
    private static int D;
    private static int E;
    private static int F;
    private static int G;
    private static int H;
    private static int I;
    private static int J;
    private static int K;
    private static int L;
    private static int M;
    private static int N;
    private static int O;
    private static int P;
    private static int Q;
    private static int R;
    private static int[] S;
    private static byte[][] T;
    private static int U;
    private static byte[] V;
    private static int W;
    private static int X;
    private static int Y;
    private static int Z;
    private static int aa;
    private static Image[] ab;
    private static int ac;
    private static int[] ad;
    private static int ae;
    private static int af;
    private static boolean ag;
    private static final String[] ah = null;
    private static String ai;
    private static boolean aj;
    private static boolean ak;
    private static String[] al;
    private static String[] am;
    private static String[] an;
    private static String[] ao;
    private static int ap;
    private static int aq;
    private static String[] ar;
    private static short[] as;
    private static int at;
    private static boolean au;
    private static boolean av;
    private static MIDlet aw;
    private static Canvas ax;
    private static String ay;
    private static boolean az;
    private static CommandListener aA;
    private static f aB;
    private static boolean aC;
    private static String aD;
    private static boolean aE;
    private static boolean aF;
    private static int aG;
    private static int aH;
    private static int aI;
    private static int aJ;
    private static int aK;
    private static Image[] aL;
    private static Image[] aM;
    private static Image aN;
    private static Image aO;
    private static Image aP;
    private static Image aQ;
    private static Image aR;
    private static Image aS;
    private static Image aT;
    private static Image aU;
    private static Image aV;
    private static Image aW;
    private static Image[] aX;
    private static Image aY;
    private static Image[][] aZ;
    private static String[][] ba;
    private static int[][] bb;
    private static int[][] bc;
    private static int[] bd;
    private static int be;
    private static int bf;
    private static int bg;
    private static int bh;
    private static int bi;
    private static int bj;
    private static int bk;
    private static int bl;
    private static int bm;
    private static int bn;
    private static int bo;
    private static String[] bp;
    private static int[] bq;
    private static boolean[] br;
    private static int bs;
    private static int bt;
    private static boolean bu;
    private static byte bv;
    private static byte bw;
    private static boolean bx;
    private static int by;
    private static int bz;
    private static int bA;
    private static int bB;
    private static int bC;
    private static int bD;
    private static String bE;
    private static String bF;
    private static String bG;
    private static String bH;
    private static int bI;
    private static int bJ;
    private static int bK;
    private static int bL;
    private static Command bM;
    private static Command bN;
    private static int bO;
    private static boolean bP;
    private static boolean bQ;
    private static int bR;
    private static int bS;
    private static int bT;
    private static int bU;
    private static int bV;
    private static int bW;
    private static int bX;
    private static Hashtable bY;
    private static boolean bZ;
    private static int ca;
    private static short[] cb;

    public f() {
    }

    private static boolean c() {
        d();
        InputStream r0 = a("/dataIGP");     // Catch: Exception -> L8
        int r02 = r0.read() & 255;     // Catch: Exception -> L8
        R = r02;     // Catch: Exception -> L8
        int r03 = r02 + ((r0.read() & 255) << 8);     // Catch: Exception -> L8
        R = r03;     // Catch: Exception -> L8
        S = new int[r03];     // Catch: Exception -> L8
        int r7 = 0;
    L5:
        if (r7 >= R) goto L7;
        S[r7] = r0.read() & 255;     // Catch: Exception -> L8
        int[] r04 = S;     // Catch: Exception -> L8
        int r1 = r7;
        r04[r1] = r04[r1] + ((r0.read() & 255) << 8);     // Catch: Exception -> L8
        int[] r05 = S;     // Catch: Exception -> L8
        int r12 = r7;
        r05[r12] = r05[r12] + ((r0.read() & 255) << 16);     // Catch: Exception -> L8
        int[] r06 = S;     // Catch: Exception -> L8
        int r13 = r7;
        r06[r13] = r06[r13] + ((r0.read() & 255) << 24);     // Catch: Exception -> L8
        r7 = r7 + 1;     // Catch: Exception -> L8
        goto L5
    L7:
        r0.close();     // Catch: Exception -> L8
        return true;
    L9:
        return false;
    }

    private static void d() {
        S = null;
        T = null;
        R = 0;
        System.gc();
    }

    private static byte[] b(int r5) {
        if (r5 >= 0) goto L5;
        return null;
    L5:
        if (r5 >= (R - 1)) goto L24;
        int r0 = S[r5 + 1] - S[r5];
        if (r0 != 0) goto L12;
        return null;
    L12:
        byte[] r7 = null;
        InputStream r02 = a("/dataIGP");     // Catch: Exception -> L18
        r02.skip((2 + (4 * R)) + S[r5]);     // Catch: Exception -> L18
        byte[] r03 = new byte[r0];     // Catch: Exception -> L18
        r7 = r03;
        int r52 = r03.length;     // Catch: Exception -> L18
    L15:
        if (r52 <= 0) goto L17;
        r52 = r52 - r02.read(r7);     // Catch: Exception -> L18
        goto L15
    L17:
        r02.close();     // Catch: Exception -> L18
    L20:
        return r7;
    L24:
        return null;
    }

    private static int a(byte[] r6) {
        int r1 = U;
        U = r1 + 1;
        int r0 = r6[r1] & 255;
        int r2 = U;
        U = r2 + 1;
        return r0 + ((r6[r2] & 255) << 8);
    }

    private static byte[] b(byte[] r6) {
        int r0 = a(r6);
        byte[] r02 = new byte[r0];
        System.arraycopy(r6, U, r02, 0, r0);
        U += r0;
        return r02;
    }

    private static Image c(byte[] r4) {
        byte[] r0 = b(r4);
        return Image.createImage(r0, 0, r0.length);
    }

    private static String c(int r4) {
        return new StringBuffer().append("").append(ar[r4]).toString();
    }

    private static int d(int r4, int r5) {
        if (ca == 0) goto L13;
        if (r5 != 0) goto L7;
    L8:
        int r0 = V[(r4 * 6) + r5] & 255;
        return (0 | (((V[((r4 * 6) + r5) + 1] & 255) & 255) << 8)) | (r0 & 255);
    L7:
        if (r5 == Y) goto L8;
        return V[(r4 * 6) + r5] & 255;
    L13:
        return V[(r4 << 2) + r5] & 255;
    }

    private static void a(int r7, Graphics r8, int r9, int r10, int r11) {
        a(c(r7), r8, bI, r9, r10, r11);
    }

    private static void a(String r6, Graphics r7, int r8, int r9, int r10, int r11) {
        a(r6, r7, r9, r10, r11);
    }

    private static void a(String r11, Graphics r12, int r13, int r14, int r15) {
        ad[0] = 0;
        af = 0;
        int r16 = 0;
        int r0 = r11.length();
        int r22 = 0;
        if (bI <= 176) goto L5;
    L7:
        int r18 = 0;
    L9:
        if (r18 >= r0) goto L28;
        char r02 = r11.charAt(r18);
        if (r02 != '\n') goto L15;
        if (r16 >= 10) goto L15;
    L18:
        int[] r03 = ad;
        int r1 = r16;
        r03[r1] = r03[r1] - m;
        if (ad[r16] <= af) goto L21;
        af = ad[r16];
    L21:
        r16 = r16 + 1;
        ad[r16] = 0;
    L27:
        r18 = r18 + 1;
    L15:
        if (r02 != '\\') goto L23;
        r18 = r18 + 1;
        if (r11.charAt(r18) == 'N') goto L18;
    L23:
        if (r02 == 0) goto L27;
        if (r02 == 1) goto L27;
        int[] r04 = ad;
        int r17 = r16;
        r04[r17] = r04[r17] + (d(r02, Z) + m);
        goto L27
    L28:
        int[] r05 = ad;
        int r19 = r16;
        r05[r19] = r05[r19] - m;
        if (ad[r16] <= af) goto L31;
        af = ad[r16];
    L31:
        ae = ((r16 + 1) * W) + (r16 * (r22 + 0));
        if (ag == true) goto L72;
        int r142 = r14 + (((r22 + 0) * r16) / 2);
        int r162 = 0;
        if ((r15 & 32) == 0) goto L37;
        r142 = r142 - ae;
    L39:
        int r23 = r13;
        boolean r192 = true;
        boolean r20 = false;
        a(r12, 0, 0, bI, bJ);
        int r182 = 0;
    L41:
        if (r182 >= r0) goto L70;
        char r06 = r11.charAt(r182);
        if (r192 == false) goto L52;
        r23 = r13;
        if ((r15 & 8) == 0) goto L48;
        r23 = r13 - ad[r162];
    L50:
        r192 = false;
        goto L52
    L48:
        if ((r15 & 1) == 0) goto L50;
        r23 = r13 - (ad[r162] >> 1);
    L52:
        if (r06 != '\n') goto L56;
        if (r162 >= 10) goto L56;
    L59:
        r142 = r142 + ((W + r22) - 1);
        r162 = r162 + 1;
        r192 = true;
        r20 = true;
    L69:
        r182 = r182 + 1;
    L56:
        if (r06 != '\\') goto L61;
        r182 = r182 + 1;
        if (r11.charAt(r182) == 'N') goto L59;
    L61:
        if (r20 == false) goto L68;
        r20 = false;
        char r07 = r11.charAt(r182 - 2);
        if (r07 != ' ') goto L66;
        r23 = r23 - ((d(r07, Z) + m) >> 1);
    L66:
        if (r06 != ' ') goto L68;
        r23 = r23 + ((d(r06, Z) + m) >> 1);
    L68:
        a(r12, r23, r142, d(r06, Z), d(r06, aa));
        r12.drawRegion(ab[ac], d(r06, 0), d(r06, Y), d(r06, Z), d(r06, aa), 0, r23, r142, 20);
        r23 = r23 + (d(r06, Z) + m);
        goto L69
    L70:
        a(r12, 0, 0, bI, bJ);
        ac = 1;
        return;
    L37:
        if ((r15 & 2) == 0) goto L39;
        r142 = r142 - (ae >> 1);
        goto L39
    L72:
        ag = false;
        return;
    L5:
        if (bq[aI] != 4) goto L7;
        r22 = 0 + 2;
        goto L7
    }

    public static void a(MIDlet r6, Canvas r7, int r8, int r9) {
        new StringBuffer().append("initialize(midlet = ").append(r6).append(", game = ").append(r7).append(", screenWidth = ").append(400).append(", screenHeight = ").append(240).append(", cmdListener = ").append(null).append(")").toString();
        bI = 400;
        bJ = 240;
        bK = bI >> 1;
        bL = bJ >> 1;
        j = (bJ * 5) / 100;
        k = bJ / 2;
        l = (bJ * 93) / 100;
        if (2 <= bI) goto L6;
        h = 2;
    L6:
        if (2 <= bI) goto L9;
        i = 2;
    L9:
        if (aw == null) goto L11;
        return;
    L11:
        if (r7 == null) goto L15;
        aw = r6;
        ax = r7;
        e();
        String r0 = b;
        new StringBuffer().append(r0).append("").toString();
        return;
    }

    private static boolean b(String r3, int r4) {
        if (r3 == null) goto L5;
        String r0 = r3.trim();
        if ((r4 & 1) == 0) goto L15;
        if (r0.length() != 0) goto L15;
        return false;
    L15:
        if ((r4 & 2) == 0) goto L19;
        if (r0.toUpperCase().compareTo("DEL") != 0) goto L19;
        return false;
    L19:
        if ((r4 & 4) != 0) goto L21;
        return true;
    L21:
        if (r0.toUpperCase().compareTo("NO") != 0) goto L23;
        return false;
    L23:
        if (r0.toUpperCase().compareTo("0") == 0) goto L31;
        return true;
    L31:
        return false;
    L5:
        if ((r4 & 1) != 0) goto L8;
        return true;
    L8:
        return false;
    }

    private static String a(String r5, String r6, String r7) {
        String r8 = "";
        if (r7 == null) goto L34;
        if (r5 == null) goto L34;
        if (r6 == null) goto L34;
        int r0 = r5.indexOf(new StringBuffer().append(r6).append("=").toString());     // Catch: Exception -> L32
        String r02 = r7.trim();     // Catch: Exception -> L32
        if (r0 < 0) goto L34;
        if (r02.length() <= 0) goto L34;
        int r03 = r0 + (r6.length() + 1);     // Catch: Exception -> L32
        int r04 = r5.indexOf(";", r03);     // Catch: Exception -> L32
        int r82 = r04;
        if (r04 >= 0) goto L16;
        r82 = r5.length();     // Catch: Exception -> L32
    L16:
        String r05 = r5.substring(r03, r82).trim();     // Catch: Exception -> L32
        r8 = r05;
        if (r05.length() != 0) goto L19;
    L22:
        r8 = "";
        goto L34
    L19:
        if (r8.compareTo("0") == 0) goto L22;
        if (r8.toUpperCase().compareTo("NO") == 0) goto L22;
        if (r8.toUpperCase().compareTo("DEL") == 0) goto L34;
        if (r6.compareTo("OP") == 0) goto L34;
        int r06 = r02.indexOf("XXXX");     // Catch: Exception -> L32
        if (r06 < 0) goto L30;
        r8 = new StringBuffer().append(r02.substring(0, r06)).append(r8).append(r02.substring(r06 + "XXXX".length())).toString();     // Catch: Exception -> L32
        goto L34
    L30:
        r8 = r02;
    L32:
        r8 = "";
    L34:
        return r8;
    }

    private static void a(int r6, String r7, int r8, String r9, String r10) {
        if (ak == false) goto L5;
        String r72 = a(aw.getAppProperty(r9), r7, r10);     // Catch: Exception -> L24
    L7:
        if (b(r72, 7) == true) goto L9;
        return;
    L9:
        if (r72.toUpperCase().compareTo("NO") == 0) goto L11;
    L12:
        br[r6] = true;     // Catch: Exception -> L24
        bp[r6] = r72;     // Catch: Exception -> L24
        if (br[r6] == true) goto L15;
        return;
    L15:
        if (r6 == s) goto L31;
        bs++;
        bq[r6] = 4;     // Catch: Exception -> L24
        if (aj == false) goto L32;
        StringBuffer r0 = new StringBuffer();     // Catch: Exception -> L24
        String[] r1 = bp;     // Catch: Exception -> L24
        StringBuffer r2 = r0.append(r1[r6]).append("&ctg=SC");     // Catch: Exception -> L24
        if (bs >= 10) goto L21;
        String r3 = "0";
    L22:
        r1[r6] = r2.append(r3).append(bs).toString();     // Catch: Exception -> L24
        return;
    L21:
        r3 = "";
        goto L22
    L32:
        return;
    L31:
        return;
    L11:
        if (r72.toUpperCase().compareTo("0") != 0) goto L12;
        return;
    L5:
        r72 = aw.getAppProperty(new StringBuffer().append("URL-").append(r7).toString());     // Catch: Exception -> L24
    }

    private static void a(int r5, String[] r6, int r7, String r8) {
        int r0 = r6.length;
        ba[r5] = new String[r0];
        bb[r5] = new int[r0];
        bc[r5] = new int[r0];
        int r10 = 0;
        if (n[0] == false) goto L5;
        return;
    L5:
        String r11 = "";
        if (ak == true) goto L59;
    L20:
        int r12 = 0;
    L22:
        if (r12 >= r6.length) goto L54;
        String r13 = "";
        if (r5 == 2) goto L38;
        if (r12 != (r0 - 1)) goto L38;
        if (ak == true) goto L31;
        r13 = aw.getAppProperty(ah[r5]);     // Catch: Exception -> L51
    L48:
        if (b(r13, 7) == false) goto L52;
        ba[r5][r10] = r13;     // Catch: Exception -> L51
        int r1 = r10;
        r10 = r10 + 1;     // Catch: Exception -> L51
        bb[r5][r1] = r12;     // Catch: Exception -> L51
        bc[r5][r12] = (r7 - (r6.length - 1)) + r12;     // Catch: Exception -> L51
        goto L52
    L31:
        if (bG.length() <= 0) goto L48;
        String r02 = aw.getAppProperty("IGP-CATEGORIES");     // Catch: Exception -> L51
        String r14 = r6[r12];     // Catch: Exception -> L51
        StringBuffer r2 = new StringBuffer().append(bG);     // Catch: Exception -> L51
        if (aj == false) goto L35;
        String r3 = "&ctg=XXXX";
    L36:
        r13 = a(r02, r14, r2.append(r3).toString());     // Catch: Exception -> L51
        goto L48
    L35:
        r3 = "";
    L52:
        r12 = r12 + 1;
    L38:
        if (ak == true) goto L40;
        r13 = aw.getAppProperty(new StringBuffer().append(ah[r5]).append("-").append(r6[r12]).toString());     // Catch: Exception -> L51
        goto L48
    L40:
        if (r6[r12].compareTo("GLDT") != 0) goto L43;
        r13 = a(r8, r6[r12], ai);     // Catch: Exception -> L51
        goto L48
    L43:
        if (r6[r12].compareTo("CATALOG") != 0) goto L45;
        r13 = bG;     // Catch: Exception -> L51
        goto L48
    L45:
        r13 = a(r8, r6[r12], r11);     // Catch: Exception -> L51
        goto L48
    L54:
        if (r10 <= 0) goto L63;
        br[t + r5] = true;
        bd[r5] = r10;
        return;
    L63:
        return;
    L59:
        r8 = aw.getAppProperty(r8);     // Catch: Exception -> L19
        if (r5 == 2) goto L11;
        r11 = ai;     // Catch: Exception -> L19
        goto L20
    L11:
        if (bG.length() <= 0) goto L16;
        if (aj == false) goto L16;
        r11 = new StringBuffer().append(bG).append("&ctg=XXXX").toString();     // Catch: Exception -> L19
    L16:
        if (bG.length() <= 0) goto L20;
        r11 = bG;     // Catch: Exception -> L19
        goto L20
    }

    private static String[] d(byte[] r8) {
        String[] r0 = new String[a(r8)];
        int r10 = 0;
    L4:
        if (r10 >= r0.length) goto L7;
        int r02 = a(r8);
        r0[r10] = new String(r8, U, r02);
        U += r02;
        r10 = r10 + 1;
        goto L4
    L7:
        return r0;
    }

    /* JADX WARN: Type inference failed for: r0v81, types: [java.lang.String[], java.lang.String[][]] */
    /* JADX WARN: Type inference failed for: r0v83, types: [int[], int[][]] */
    /* JADX WARN: Type inference failed for: r0v85, types: [int[], int[][]] */
    private static void e() {
    L23:
        aE = false;
        return;
    L3:
        if (c() == true) goto L6;
        aE = false;     // Catch: Exception -> L23
        return;
    L6:
        byte[] r0 = b(0);     // Catch: Exception -> L23
        a(r0);     // Catch: Exception -> L23
        int r02 = a(r0);     // Catch: Exception -> L23
        new String(r0, U, r02);     // Catch: Exception -> L23
        U += r02;
        al = d(r0);     // Catch: Exception -> L23
        String[] r03 = d(r0);     // Catch: Exception -> L23
        am = d(r0);     // Catch: Exception -> L23
        an = d(r0);     // Catch: Exception -> L23
        ao = d(r0);     // Catch: Exception -> L23
        int r9 = 0;
    L8:
        if (r9 >= n.length) goto L121;
        boolean[] r04 = n;     // Catch: Exception -> L23
        int r1 = r9;
        if (a(r0) != 1) goto L12;
        boolean r2 = true;
    L13:
        r04[r1] = r2;     // Catch: Exception -> L23
        r9 = r9 + 1;     // Catch: Exception -> L23
        goto L8
    L12:
        r2 = false;
        goto L13
    L121:
        String r05 = new String(r0, U, a(r0));     // Catch: Exception -> L21 Exception -> L23
        c = r05;     // Catch: Exception -> L21 Exception -> L23
        if (r05.equals("2.1z") == false) goto L18;
        ca = 2;     // Catch: Exception -> L21 Exception -> L23
        Y = 2;     // Catch: Exception -> L21 Exception -> L23
        Z = 4;     // Catch: Exception -> L21 Exception -> L23
        aa = 5;     // Catch: Exception -> L21 Exception -> L23
    L18:
        if (c.startsWith(a) == true) goto L22;
        new StringBuffer().append("Invalid dataIGP file, dataIGP file IGP Version : ").append(c).toString();     // Catch: Exception -> L21 Exception -> L23
        new StringBuffer().append("IGP Class version : ").append(a).toString();     // Catch: Exception -> L21 Exception -> L23
    L22:
        d();     // Catch: Exception -> L23
        if ((r03.length - 1) <= 0) goto L28;
        int r06 = r03.length - 1;
    L29:
        int r8 = (r06 + 4) + 1;
        x = r8;
        int r07 = r8 + 1;
        y = r07;
        int r08 = r07 + 1;
        z = r08;
        int r09 = r08 + 1;
        A = r09;
        int r010 = r09 + 1;
        B = r010;
        int r011 = r010 + 1;
        C = r011;
        int r012 = r011 + 1;
        D = r012;
        int r013 = r012 + 1;
        E = r013;
        int r014 = r013 + 1;
        F = r014;
        int r015 = r014 + 1;
        G = r015;
        int r016 = r015 + 1;
        H = r016;
        int r017 = r016 + 1;
        I = r017;
        int r018 = r017 + 1;
        J = r018;
        if (am.length <= 1) goto L32;
        int r019 = r018 + 1;
        K = r019;
        L = r019 + (am.length - 1);
    L33:
        int r020 = L;
        if (an.length <= 1) goto L36;
        int r021 = r020 + 1;
        M = r021;
        N = r021 + (an.length - 1);
    L37:
        int r022 = N + 1;
        O = r022;
        P = r022 + 1;
        int r023 = r03.length;
        ap = r023;
        aX = new Image[r023];
        int r024 = (((ap + 1) + 1) + 1) + 1;
        if (n[0] == false) goto L40;
        int r12 = 1;
    L41:
        aq = r024 + r12;
        int r82 = 0;
    L43:
        if (r82 >= n.length) goto L49;
        if (n[r82] == false) goto L47;
        int r22 = r + 1;
        r = r22;
        o[r82] = r22;
    L48:
        r82 = r82 + 1;
        goto L43
    L47:
        int r23 = q - 1;
        q = r23;
        o[r82] = r23;
        goto L48
    L49:
        int r025 = r + 1;
        r = r025;
        p = r025;
        bp = new String[aq];
        br = new boolean[aq];
        bq = new int[aq];
        int r83 = 0;
    L51:
        if (r83 >= br.length) goto L53;
        br[r83] = false;
        r83 = r83 + 1;
        goto L51
    L53:
        int r026 = ap;
        s = r026;
        int r027 = r026 + 1;
        t = r027;
        int r028 = r027 + 1;
        u = r028;
        int r029 = r028 + 1;
        v = r029;
        w = r029 + 1;
        ba = new String[3];
        bb = new int[3];
        bc = new int[3];
        bd = new int[3];
        String r030 = aw.getAppProperty("URL-TEMPLATE-GAME");     // Catch: Exception -> L60
        ai = r030;     // Catch: Exception -> L60
        if (r030 == null) goto L61;
        ai = ai.trim();     // Catch: Exception -> L60
        ak = true;     // Catch: Exception -> L60
        if (ai.indexOf("ingameads.gameloft.com/redir") == (-1)) goto L61;
        aj = true;     // Catch: Exception -> L60
    L61:
        int r84 = 0;
    L63:
        if (r84 >= ap) goto L123;
        a(r84, r03[r84], 7, "IGP-PROMOS", ai);
        r84 = r84 + 1;
        goto L63
    L123:
        String r031 = aw.getAppProperty("URL-OPERATOR").trim();     // Catch: Exception -> L69
        if (b(r031, 7) == false) goto L68;
        bG = r031;     // Catch: Exception -> L69
    L68:
        bE = aw.getAppProperty("URL-PT");     // Catch: Exception -> L69
    L71:
        if (n[0] == false) goto L73;
    L86:
        a(0, am, L, "IGP-WN");
        a(1, an, N, "IGP-BS");
        if (n[0] == true) goto L106;
        if (ak == false) goto L98;
        if (b(a(aw.getAppProperty("IGP-CATEGORIES"), "OP", bG), 7) == false) goto L101;
        if (b(bG, 7) == false) goto L95;
        br[v] = true;
    L95:
        bp[v] = bG;
    L101:
        if (br[v] == false) goto L106;
        if (aj == false) goto L106;
        StringBuffer r032 = new StringBuffer();
        String[] r13 = bp;
        int r24 = v;
        r13[r24] = r032.append(r13[r24]).append("&ctg=CCTL").toString();
        goto L106
    L98:
        if (b(bG, 7) == false) goto L101;
        bp[v] = bG;
        br[v] = true;
    L106:
        if (n[0] == true) goto L117;
    L112:
        int r033 = g();
        bo = r033;
        if (r033 <= 0) goto L115;
        aE = true;
    L115:
        new StringBuffer().append("isAvailable = ").append(aE).toString();
        return;
    L117:
        if (b(bG, 7) == false) goto L112;
        bp[w] = bG;     // Catch: Exception -> L111
        br[w] = true;     // Catch: Exception -> L111
        goto L112
    L73:
        if (ak == true) goto L75;
        String r034 = aw.getAppProperty("URL-PROMO");
        if (r034 == null) goto L86;
        r034.trim();
        if (b(aw.getAppProperty("URL-PROMO"), 7) == false) goto L86;
        bp[s] = r034;
        br[s] = true;
        bq[s] = 5;
        goto L86
    L75:
        if (b(bG, 7) == false) goto L86;
        int r035 = ap;
        StringBuffer r4 = new StringBuffer().append(bG);
        if (aj == false) goto L79;
        String r5 = "&ctg=XXXX";
    L80:
        a(r035, "PROMO", 7, "IGP-CATEGORIES", r4.append(r5).toString());
        goto L86
    L79:
        r5 = "";
        goto L80
    L40:
        r12 = 0;
        goto L41
    L36:
        N = r020 + 1;
        goto L37
    L32:
        L = r018 + 1;
        goto L33
    L28:
        r06 = 0;
    L21:
        aE = false;     // Catch: Exception -> L23
        goto L22
    }

    public static int a() {
        if (aE == true) goto L7;
        return -1;
    L7:
        if (g() <= 0) goto L10;
        return 0;
    L10:
        return -1;
    }

    public static void a(String r5, int r6) {
        new StringBuffer().append("enterIGP(loadingMsg = ").append(r5).append(", appLanguage = ").append(0).append(" (").append(al[0]).append(")").toString();
        aH = 0;
        bO = 0;
        bP = true;
        bQ = false;
        bZ = false;
        if (0 >= 0) goto L5;
    L12:
        aK = 4 + aq;
        RecordStore r52 = null;
        r52 = RecordStore.openRecordStore("igp19", false);     // Catch: Exception -> L14
    L18:
        if (r52 == null) goto L22;
        r52.closeRecordStore();     // Catch: Exception -> L21
    L22:
        aI = h();
        if (az == false) goto L32;
        ax.setCommandListener(aB);
        return;
    L32:
        return;
    L28:
        r52 = RecordStore.openRecordStore("igp19", true);     // Catch: Exception -> L16
        goto L18
    L5:
        if (0 >= al.length) goto L12;
        if (0 > al.length) goto L10;
        int r0 = 0;
    L11:
        bD = r0;
        bF = r5;
        aJ = -1;
        aG = 0;
        aI = 0;
        bh = 0;
        bi = 0;
        ad = new int[10];
        aC = true;
        f = Font.getFont(0, 0, 8);
        bv = 0;
        bw = 0;
        new Thread(new f()).start();
        goto L12
    L10:
        r0 = 0;
        goto L11
    }

    public static void b() {
        bZ = true;
        if (f() == true) goto L5;
        return;
    L5:
        aG = 7;
        aC = true;
        new Thread(new f()).start();
        aD = bH;
    }

    private static boolean f() {
        boolean r5 = false;
        int r6 = 0;
    L4:
        if (r6 >= al.length) goto L10;
        if (al[r6].equals("SP") == false) goto L8;
        r5 = true;
    L8:
        r6 = r6 + 1;
        goto L4
    L10:
        if (r5 == true) goto L45;
        return false;
    L45:
        String r0 = aw.getAppProperty("IGP-CATEGORIES");     // Catch: Exception -> L41
        if (r0 != null) goto L16;
    L35:
        String r02 = aw.getAppProperty("URL-ZVIP");     // Catch: Exception -> L41
        bH = r02;     // Catch: Exception -> L41
        if (r02 != null) goto L39;
        return false;
    L39:
        aF = true;     // Catch: Exception -> L41
        return true;
    L16:
        if (r0.indexOf("ZVIP") == (-1)) goto L35;
        int r03 = (r0.indexOf("ZVIP") + "ZVIP".length()) + 1;     // Catch: Exception -> L41
        int r04 = r03 + "ZVIP".length();     // Catch: Exception -> L41
        if (r04 < r0.length()) goto L22;
        return false;
    L22:
        if (r0.substring(r03, r04).equals("ZVIP") == true) goto L25;
        return false;
    L25:
        bH = bG;     // Catch: Exception -> L41
        if (aj == false) goto L43;
        String r05 = new StringBuffer().append(bH).append("&ctg=XXXX").toString();     // Catch: Exception -> L41
        bH = r05;     // Catch: Exception -> L41
        int r06 = r05.indexOf("XXXX");     // Catch: Exception -> L41
        if (r06 < 0) goto L31;
        bH = new StringBuffer().append(bH.substring(0, r06)).append("ZVIP").append(bH.substring(r06 + "XXXX".length())).toString();     // Catch: Exception -> L41
    L31:
        if (bH.length() != 0) goto L43;
        return false;
    L43:
        aF = true;
        return true;
    L42:
        return false;
    }

    private static int g() {
        int r3 = 0;
        int r4 = 0;
    L4:
        if (r4 >= br.length) goto L10;
        if (br[r4] == false) goto L8;
        r3 = r3 + 1;
    L8:
        r4 = r4 + 1;
        goto L4
    L10:
        return r3;
    }

    private static int h() {
        int r3 = 0;
    L4:
        if (r3 >= br.length) goto L10;
        if (br[r3] == true) goto L8;
        r3 = r3 + 1;
        goto L4
    L8:
        return r3;
    L10:
        return -1;
    }

    private static void i() {
        if (bE != null) goto L5;
        return;
    L5:
        int r0 = bE.length();
        if (r0 > 0) goto L9;
        bE = null;
        return;
    L9:
        boolean r6 = false;
        int r7 = 0;
        int r8 = 0;
        int r9 = 0;
        int r10 = 1;
        String r11 = "";
        bE = bE.toUpperCase();
        int r12 = 0;
    L11:
        if (r12 >= r0) goto L61;
        char r02 = bE.charAt(r12);
        if (r02 < ' ') goto L17;
        if (r02 > 'z') goto L17;
    L21:
        if (r02 != '\n') goto L24;
        r6 = true;
    L33:
        if (r6 == true) goto L35;
        r11 = new StringBuffer().append(r11).append(r02).toString();
        r8 = r8 + d(r02, Z);
        if (r02 != ' ') goto L46;
        r7 = r11.length() - 1;
        r9 = r8;
    L46:
        if (r8 < bI) goto L59;
        if (r10 >= 3) goto L57;
        if (r7 != r12) goto L53;
        r11 = new StringBuffer().append(r11).append("\n").toString();
        r8 = 0;
    L56:
        r10 = r10 + 1;
        goto L59
    L53:
        if (r7 == 0) goto L55;
        r11 = new StringBuffer().append(r11.substring(0, r7)).append("\n").append(r11.substring(r7 + 1, r11.length())).toString();
        r8 = r8 - r9;
        goto L56
    L55:
        r11 = null;
        goto L61
    L57:
        r11 = null;
    L59:
        r12 = r12 + 1;
        goto L11
    L35:
        if (r11.length() <= 0) goto L40;
        r10 = r10 + 1;
        if (r10 == 3) goto L38;
        r11 = new StringBuffer().append(r11).append('\n').toString();
        r8 = 0;
    L41:
        r6 = false;
        goto L46
    L38:
        r11 = null;
        goto L61
    L40:
        r11 = new StringBuffer().append(r11).append("").toString();
        goto L41
    L24:
        if (r12 >= (r0 - 1)) goto L33;
        if (r02 != '\\') goto L33;
        if (bE.charAt(r12 + 1) != 'n') goto L30;
    L31:
        r6 = true;
        r12 = r12 + 1;
        goto L33
    L30:
        if (bE.charAt(r12 + 1) != 'N') goto L33;
    L17:
        if (r02 == 130) goto L21;
        if (r02 == '\n') goto L21;
        r11 = null;
    L61:
        if (r11 != null) goto L63;
    L65:
        bE = r11;
        return;
    L63:
        if (b(r11, 7) == true) goto L65;
        r11 = null;
        goto L65
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v185, types: [javax.microedition.lcdui.Image[], javax.microedition.lcdui.Image[][]] */
    private static void d(int r8) {
        U = 0;
        switch(r8) {
            case -1: goto L4;
            case 0: goto L6;
            case 1: goto L8;
            case 2: goto L67;
            case 3: goto L77;
            case 4: goto L82;
            case 5: goto L87;
            case 6: goto L94;
            case 7: goto L94;
            case 8: goto L111;
            default: goto L120;
        };
    L4:
        aL = new Image[Q];
        aM = new Image[aq];
        ?? r0 = new Image[3];
        aZ = r0;
        r0[0] = new Image[am.length];
        aZ[1] = new Image[an.length];
        aZ[2] = new Image[ao.length];
        return;
    L6:
        c();
        return;
    L8:
        byte[] r02 = b(r8);
        int r10 = 0;
    L10:
        if (r10 >= bD) goto L12;
        U += a(r02);
        r10 = r10 + 1;
        goto L10
    L12:
        a(r02);
        int r03 = a(r02);
        ar = new String[r03];
        byte[] r04 = new byte[r03];
        System.arraycopy(r02, U, r04, 0, r03);
        U += r03;
        a(r02);
        int r1 = U;
        U = r1 + 1;
        int r05 = r02[r1] & 255;
        int r2 = U;
        U = r2 + 1;
        int r06 = r05 | ((r02[r2] & 255) << 8);
        as = new short[r06];
        int r12 = 0;
    L14:
        if (r12 >= (r06 - 1)) goto L16;
        int r3 = U;
        U = r3 + 1;
        int r22 = r02[r3] & 255;
        int r4 = U;
        U = r4 + 1;
        as[r12] = (short) (r22 + ((r02[r4] & 255) << 8));
        r12 = r12 + 1;
        goto L14
    L16:
        as[r06 - 1] = (short) r03;
        int r122 = 0;
    L18:
        if (r122 >= r06) goto L58;
        if (r122 != 0) goto L22;
        int r07 = 0;
    L23:
        int r82 = r07;
        int r08 = (as[r122] & 65535) - r82;
        if (r08 == 0) goto L56;
        StringBuffer r09 = new StringBuffer((r08 / 2) + 2);     // Catch: Exception -> L55
        int r15 = r82;
    L27:
        if (r15 >= (r82 + r08)) goto L56;
        if ((r04[r15] & 128) != 0) goto L32;
        int r23 = r15;
        r15 = r15 + 1;     // Catch: Exception -> L55
        r09.append((char) (r04[r23] & 255));     // Catch: Exception -> L55
    L53:
        ar[r122] = r09.toString().toUpperCase();     // Catch: Exception -> L55
        goto L27
    L32:
        if ((r04[r15] & 224) != 192) goto L41;
        if ((r15 + 1) >= (r82 + r08)) goto L38;
        if ((r04[r15 + 1] & 192) != 128) goto L38;
        int r24 = r15;
        int r152 = r15 + 1;     // Catch: Exception -> L55
        r15 = r152 + 1;     // Catch: Exception -> L55
        r09.append((char) (((r04[r24] & 31) << 6) | (r04[r152] & 63)));     // Catch: Exception -> L55
    L38:
        throw new Exception();     // Catch: Exception -> L55
    L41:
        if ((r04[r15] & 240) != 224) goto L52;
        if ((r15 + 2) >= (r82 + r08)) goto L49;
        if ((r04[r15 + 1] & 192) != 128) goto L49;
        if ((r04[r15 + 2] & 192) != 128) goto L49;
        int r25 = r15;
        int r153 = r15 + 1;     // Catch: Exception -> L55
        int r154 = r153 + 1;     // Catch: Exception -> L55
        int r13 = ((r04[r25] & 15) << 12) | ((r04[r153] & 63) << 6);     // Catch: Exception -> L55
        r15 = r154 + 1;     // Catch: Exception -> L55
        r09.append((char) (r13 | (r04[r154] & 63)));     // Catch: Exception -> L55
    L49:
        throw new Exception();     // Catch: Exception -> L55
    L52:
        throw new Exception();     // Catch: Exception -> L55
    L56:
        r122 = r122 + 1;
        goto L18
    L22:
        r07 = as[r122 - 1] & 65535;
        goto L23
    L58:
        if (az == false) goto L149;
        bM = new Command(c(D), 4, 1);
        bN = new Command(c(E), 2, 1);
        if (bN == null) goto L63;
        ax.removeCommand(bN);
    L63:
        if (bM == null) goto L65;
        ax.removeCommand(bM);
    L65:
        ax.addCommand(bM);
        ax.addCommand(bN);
        return;
    L149:
        return;
    L67:
        ab = new Image[3];
        byte[] r010 = b(r8);
        byte[] r011 = new byte[r010.length];
        System.arraycopy(r010, 0, r011, 0, r010.length);
        int r012 = a(r010);
        U = 0;
        ab[0] = c(r010);
        ab[1] = a(r011, 2, r012, 1, 46319);
        ab[2] = a(r011, 2, r012, 1, 16711680);
        int r013 = a(r010);
        V = new byte[(a(r010) + 1) * (4 + ca)];
        int r014 = r013 / (6 + ca);
        int r102 = 0;
    L69:
        if (r102 >= r014) goto L71;
        System.arraycopy(r010, U, V, a(r010) * (4 + ca), 4 + ca);
        U += 4 + ca;
        r102 = r102 + 1;
        goto L69
    L71:
        byte r015 = V[(32 * (4 + ca)) + aa];
        W = r015;
        if (r015 != 13) goto L74;
        int r016 = 0;
    L75:
        m = r016;
        i();
        return;
    L74:
        r016 = -1;
        goto L75
    L77:
        byte[] r017 = b(r8);
        int r103 = 0;
    L79:
        if (r103 >= Q) goto L81;
        aL[r103] = c(r017);
        r103 = r103 + 1;
        goto L79
    L81:
        return;
    L82:
        int r104 = 0;
    L84:
        if (r104 >= ap) goto L86;
        aM[r104] = c(b(r8));
        r104 = r104 + 1;
        goto L84
    L86:
        return;
    L87:
        byte[] r018 = b(r8);
        byte[] r019 = b(r018);
        aM[s] = Image.createImage(r019, 0, r019.length);
        int r105 = 0;
    L89:
        if (r105 >= bD) goto L91;
        c(r018);
        r105 = r105 + 1;
        goto L89
    L91:
        aN = c(r018);
        return;
    L111:
        byte[] r020 = b(r8);
        aM[v] = c(r020);
        if (al[bD].equals("JP") == true) goto L116;
        if (al[bD].equals("DOCOMO") == true) goto L116;
        return;
    L116:
        if (aO != null) goto L151;
        aO = c(r020);
        return;
    L151:
        return;
    L94:
        if (r8 != 6) goto L96;
        int r021 = am.length;
    L97:
        int r022 = r021 - 1;
        if (r8 != 6) goto L100;
        boolean r023 = false;
    L101:
        boolean r11 = r023;
        byte[] r024 = b(r8);
        int r123 = 0;
    L103:
        if (r123 >= r022) goto L105;
        aZ[r11 ? 1 : 0][r123] = c(r024);
        r123 = r123 + 1;
        goto L103
    L105:
        aZ[r11 ? 1 : 0][r123] = aL[11];
        Image[] r025 = aM;
        if (r8 != 6) goto L108;
        int r14 = t;
    L109:
        r025[r14] = aL[10];
        return;
    L108:
        r14 = u;
        goto L109
    L100:
        r023 = true;
        goto L101
    L96:
        r021 = an.length;
        goto L97
    L120:
        if (r8 != o[0]) goto L124;
        aM[w] = c(b(r8));
        return;
    L124:
        if (r8 != p) goto L152;
        d();
        return;
    }

    private static void a(boolean r4) {
        int r5 = 0;
    L4:
        if (r5 >= aZ.length) goto L12;
        if (aZ[r5] == null) goto L11;
        int r6 = 0;
    L9:
        if (r6 >= aZ[r5].length) goto L11;
        aZ[r5][r6] = null;
        r6 = r6 + 1;
    L11:
        r5 = r5 + 1;
        goto L4
    L12:
        int r52 = 0;
    L14:
        if (r52 >= aM.length) goto L16;
        aM[r52] = null;
        r52 = r52 + 1;
        goto L14
    L16:
        aN = null;
        aO = null;
        if (r4 == false) goto L26;
        d();
        V = null;
        ab = null;
        int r53 = 0;
    L20:
        if (r53 >= Q) goto L22;
        aL[r53] = null;
        r53 = r53 + 1;
        goto L20
    L22:
        aL = null;
        as = null;
        ar = null;
        ad = null;
        bF = null;
        aM = null;
        aZ = null;
        aP = null;
        aQ = null;
        aR = null;
        aS = null;
        aT = null;
        aU = null;
        aV = null;
        aW = null;
        aY = null;
        int r54 = 0;
    L24:
        if (r54 >= aX.length) goto L26;
        aX[r54] = null;
        r54 = r54 + 1;
    L26:
        System.gc();
    }

    public static boolean a(int r5) {
        if (bZ == false) goto L15;
        if (aF == true) goto L9;
        return true;
    L9:
        if (aG != 4) goto L12;
        return true;
    L12:
        return false;
    L15:
        if (aE == true) goto L19;
        return true;
    L19:
        if (bP == false) goto L21;
        bP = false;
    L23:
        switch(aG) {
            case 0: goto L25;
            case 1: goto L30;
            case 2: goto L91;
            case 3: goto L113;
            case 4: goto L117;
            case 5: goto L127;
            case 6: goto L77;
            default: goto L127;
        };
    L77:
        String r52 = bp[aI];
        if (bx == false) goto L81;
        r52 = ba[be][bi];
    L81:
        if (r52 != null) goto L83;
        return false;
    L83:
        if (r52.length() > 0) goto L85;
        return false;
    L85:
        if (aj == false) goto L90;
        int r0 = r52.indexOf("&lg=");
        if (r0 != (-1)) goto L89;
        r52 = new StringBuffer().append(r52).append("&lg=").append(al[bD]).toString();
        goto L90
    L89:
        r52 = new StringBuffer().append(r52.substring(0, r0)).append("&lg=").append(al[bD]).append(r52.substring((r0 + "&lg=".length()) + 2)).toString();
    L90:
        aD = r52;
        return false;
    L91:
        a(false);
        int r02 = aI;
        if (bq[r02] != 4) goto L95;
        int r03 = bq[r02];
    L110:
        aJ = r03;
        d(r03);
        k();
        aG = 1;
        return false;
    L95:
        if (r02 != s) goto L98;
        r03 = 5;
        goto L110
    L98:
        if (r02 != t) goto L101;
        r03 = 6;
        goto L110
    L101:
        if (r02 != u) goto L104;
        r03 = 7;
        goto L110
    L104:
        if (r02 != v) goto L107;
        r03 = 8;
        goto L110
    L107:
        if (r02 != w) goto L109;
        r03 = o[0];
        goto L110
    L109:
        r03 = -1;
        goto L110
    L117:
        a(true);
        if (az == false) goto L125;
        ax.setCommandListener(aA);
        if (bN == null) goto L123;
        ax.removeCommand(bN);
        bN = null;
    L123:
        if (bM == null) goto L125;
        ax.removeCommand(bM);
        bM = null;
    L125:
        aC = false;
        return true;
    L127:
        return false;
    L25:
        if (aJ < aK) goto L27;
        k();
        aG = 1;
        j();
    L28:
        aJ++;
        return false;
    L27:
        d(aJ);
        goto L28
    L30:
        switch(aH) {
            case 21: goto L69;
            case 22: goto L154;
            case 23: goto L33;
            case 24: goto L46;
            case 25: goto L75;
            case 26: goto L31;
            case 27: goto L75;
            case 28: goto L154;
            case 29: goto L154;
            case 30: goto L154;
            case 31: goto L154;
            case 32: goto L62;
            default: goto L154;
        };
    L31:
        aG = 4;
        return false;
    L75:
        aG = 6;
        return false;
    L154:
        return false;
    L33:
        if (bo > 1) goto L35;
        return false;
    L35:
        if (aI != 0) goto L37;
        aI = aq - 1;
    L39:
        if (br[aI] == true) goto L44;
        if (aI == 0) goto L42;
        aI--;
        goto L39
    L42:
        aI = aq - 1;
        goto L39
    L44:
        au = true;
        goto L46
    L37:
        aI--;
    L46:
        if (bo > 1) goto L48;
        return false;
    L48:
        if (au == false) goto L50;
    L60:
        bh = 0;
        bi = 0;
        j();
        return false;
    L50:
        if (aI != (aq - 1)) goto L52;
        aI = 0;
    L54:
        if (br[aI] == true) goto L59;
        if (aI == (aq - 1)) goto L57;
        aI++;
        goto L54
    L57:
        aI = 0;
        goto L54
    L59:
        av = true;
        goto L60
    L52:
        aI++;
        goto L54
    L62:
        if (bx == true) goto L64;
        return false;
    L64:
        if (bi >= (bf - 1)) goto L158;
        int r04 = bi + 1;
        bi = r04;
        if ((r04 - bh) < bg) goto L159;
        bh++;
        return false;
    L159:
        return false;
    L158:
        return false;
    L69:
        if (bx == true) goto L71;
        return false;
    L71:
        if (bi <= 0) goto L161;
        int r05 = bi - 1;
        bi = r05;
        if ((r05 - bh) >= 0) goto L162;
        bh--;
        return false;
    L162:
        return false;
    L161:
        return false;
    L113:
        switch(aH) {
            case 25: goto L115;
            case 26: goto L114;
            default: goto L164;
        };
    L114:
        aG = 1;
        ay = null;
        return false;
    L115:
        aD = null;
        return false;
    L164:
        return false;
    L21:
        aH = r5;
        goto L23
    }

    public static void a(int r3, int r4) {
        int r0 = e(r3, r4);
        if (r0 == 0) goto L5;
        aH = r0;
        bP = true;
    L5:
        bO = 0;
        bQ = false;
    }

    public static void b(int r3, int r4) {
        int r0 = e(r3, r4);
        if (r0 == 0) goto L6;
        bO = r0;
        return;
    }

    public static void c(int r3, int r4) {
        aH = 0;
        bO = 0;
        bQ = false;
        b(r3, r4);
    }

    private static int e(int r6, int r7) {
        if (r6 >= 1) goto L5;
        return 0;
    L5:
        if (r7 >= 1) goto L9;
        return 0;
    L9:
        if (aG != 0) goto L11;
        return 0;
    L11:
        if (aC == true) goto L15;
        return 0;
    L15:
        if (bx == false) goto L69;
        int r0 = (((bJ * 5) / 100) + 1) + aL[5].getWidth();
        int r02 = bI / 100;
        int r03 = (r0 + aZ[be][0].getWidth()) + r02;
        int r04 = r02 * 3;
        if (r6 > (bk + r04)) goto L19;
    L21:
        boolean r05 = false;
    L22:
        boolean r11 = r05;
        int r8 = bh;
    L24:
        if (r8 >= (bh + bg)) goto L39;
        if (r11 == false) goto L37;
        if (r7 <= (bV + (bn * (r8 - bh)))) goto L37;
        if (r7 >= ((bV + (bn * (r8 - bh))) + bn)) goto L37;
        if (bi != r8) goto L35;
        return 25;
    L35:
        bi = r8;
        return 0;
    L37:
        r8 = r8 + 1;
        goto L24
    L39:
        if (r11 == false) goto L48;
        if (r6 > (((aL[12].getWidth() * 3) / 2) + 2)) goto L43;
    L45:
        boolean r06 = false;
    L46:
        r11 = r06;
        goto L48
    L43:
        if (r6 >= (bI - (((aL[12].getWidth() * 3) / 2) - 2))) goto L45;
        r06 = true;
    L48:
        if (bg < bf) goto L50;
    L78:
        int r07 = bI - aL[7].getWidth();
        int r08 = (bJ >> 1) - aL[7].getHeight();
        int r09 = aL[7].getHeight() << 1;
        int r010 = aL[7].getWidth() << 1;
        if (r7 <= r08) goto L95;
        if (r7 >= (r08 + r09)) goto L95;
        if (r6 <= 0) goto L89;
        if (r6 >= (r010 + 0)) goto L89;
        return 23;
    L89:
        if (r6 <= (r07 - (r010 / 2))) goto L95;
        if (r6 >= (r07 + r010)) goto L95;
        return 24;
    L95:
        if (az == false) goto L99;
        return 0;
    L99:
        if (r7 >= (bJ - (aL[13].getHeight() << 1))) goto L101;
        return 0;
    L101:
        if (r7 <= bJ) goto L103;
        return 0;
    L103:
        if (r6 <= h) goto L109;
        if (r6 >= ((aL[13].getWidth() << 1) + h)) goto L109;
        bQ = true;
        return 25;
    L109:
        if (r6 > ((bI - i) - (aL[13].getWidth() << 1))) goto L111;
        return 0;
    L111:
        if (r6 >= (bI - i)) goto L125;
        return 26;
    L125:
        return 0;
    L50:
        if (r11 == false) goto L78;
        if (bi <= 0) goto L60;
        if (r7 <= (bW - ((r03 - 2) / 2))) goto L60;
        if (r7 >= (bW + (r03 - 2))) goto L60;
        return 21;
    L60:
        if (bi >= (bf - 1)) goto L78;
        if (r7 <= (bX - ((r03 - 2) / 2))) goto L78;
        if (r7 >= (bX + (r03 - 2))) goto L78;
        return 32;
    L19:
        if (r6 >= ((bk + bm) - r04)) goto L21;
        r05 = true;
        goto L22
    L69:
        if (r6 <= bB) goto L78;
        if (r6 >= (bB + bz)) goto L78;
        if (r7 <= bC) goto L78;
        if (r7 >= (bC + bA)) goto L78;
        return 27;
    }

    private static void j() {
        bt = aI;
        bi = 0;
        bf = 0;
        bh = 0;
        bx = false;
        if (bp[aI] != null) goto L5;
    L9:
        boolean r0 = false;
    L10:
        bu = r0;
        if (aI != t) goto L14;
        be = 0;
        bx = true;
        bu = false;
    L14:
        if (aI != u) goto L17;
        be = 1;
        bx = true;
        bu = false;
    L17:
        if (aI != v) goto L19;
    L20:
        bu = true;
    L21:
        by = F;
        if (bq[aI] != 4) goto L25;
        by = G;
    L25:
        if (bx == false) goto L28;
        bf = bd[be];
        return;
    L28:
        return;
    L19:
        if (aI != w) goto L21;
    L5:
        if (bp[aI].length() <= 0) goto L9;
        if (bp[aI].compareTo("DEL") == 0) goto L9;
        r0 = true;
        goto L10
    }

    public static void a(Graphics r10) {
        if (aE == true) goto L8;
        if (bZ == true) goto L8;
        return;
    L8:
        if (bZ == false) goto L10;
        return;
    L10:
        a(r10, 0, 0, bI, bJ);
        a(r10, 0, 0, bI, bJ);
        switch(aG) {
            case 0: goto L12;
            case 1: goto L21;
            case 2: goto L39;
            case 3: goto L42;
            case 4: goto L50;
            case 5: goto L41;
            default: goto L50;
        };
    L12:
        r10.setColor(0);
        r10.fillRect(0, 0, bI, bJ);
        int r1 = bL;
        int r2 = (bI * 3) / 4;
        int r3 = aJ;
        int r4 = aK;
        int r16 = r3;
        if (r16 <= r4) goto L15;
        r16 = r4;
    L15:
        int r0 = (bI - r2) / 2;
        r10.setColor(16777215);
        r10.drawRect(r0, r1, r2, 6);
        r10.setColor(16711680);
        r10.fillRect((r0 + 1) + 1, (r1 + 1) + 1, ((((r2 - 2) - 2) * r16) / r4) + 1, 3);
        if (bF != null) goto L18;
        return;
    L18:
        if (bF.trim().equals("") == true) goto L52;
        r10.setColor(16777215);
        r10.setFont(f);
        r10.drawString(bF, bK, bL - 5, 33);
        return;
    L52:
        return;
    L21:
        b(r10);
        if ((System.currentTimeMillis() % 1000) > 500) goto L28;
        if (bO == 27) goto L26;
        return;
    L26:
        if (bQ == false) goto L28;
        return;
    L28:
        if (bu == false) goto L49;
        r10.setColor(g);
        if (n[0] == false) goto L33;
        r10.setColor(16744192);
    L33:
        if (bQ == false) goto L35;
    L37:
        ac = 0;
        r10.fillRect(bB, bC, bz, bA + 1);
        a(by, r10, bK, bC + (bA >> 1), 3);
        return;
    L35:
        if (bO != 27) goto L37;
        r10.setColor(14588928);
        r10.drawRect(bB - 2, bC - 2, bz + 3, bA + 4);
        r10.drawRect(bB - 1, bC - 1, bz + 1, bA + 2);
        r10.setColor(5767410);
        goto L37
    L49:
        return;
    L39:
        int r02 = f.getHeight();
        r10.setColor(255);
        r10.fillRect(0, (bL - r02) - 5, bI, r02 << 1);
        int r03 = ac;
        ac = 0;
        a(P, r10, bK, bL - r02, 65);
        ac = r03;
        return;
    L41:
        return;
    L42:
        b(r10);
        int r04 = (bJ * 40) / 100;
        int r05 = bI;
        int r42 = bJ - (r04 << 1);
        int[] r06 = new int[r05 * r42];
        int r14 = 0;
    L44:
        if (r14 >= r06.length) goto L46;
        r06[r14] = -220209185;
        r14 = r14 + 1;
        goto L44
    L46:
        r10.drawRGB(r06, 0, r05, 0, r04, r05, r42, true);
        r10.setColor(39423);
        r10.drawRect(0, r04, r05 - 1, (bJ - (r04 << 1)) - 1);
        r10.drawRect(1, r04 + 1, r05 - 3, (bJ - (r04 << 1)) - 3);
        a(J, r10, bK, bL, 3);
        c(r10);
        return;
    }

    private static void b(Graphics r9) {
        ag = true;
        a(by, null, 0, 0, 3);
        int r0 = d(32, Z) / 2;
        int r02 = d(32, aa) / 3;
        bz = af + r0;
        bA = ae + r02;
        if (((ae + bA) % 2) == 0) goto L5;
        bA++;
    L5:
        bB = bK - (bz / 2);
        if (bq[aI] != 4) goto L8;
    L9:
        bC = k - (bA / 2);
    L16:
        if (n[0] == true) goto L18;
    L20:
        r9.setColor(16777215);
        if (n[0] == false) goto L23;
        r9.setColor(0);
    L23:
        r9.fillRect(0, 0, bI, bJ);
        if (bx == false) goto L63;
        int r12 = j;
        if (bJ <= 128) goto L28;
        a(r9, aM[aI], bK, j, 17);
        r12 = r12 + aM[aI].getHeight();
    L28:
        int r03 = r12;
        ac = 2;
        a(bt, r9, bK, r03, 17);
        int r04 = Math.max(2 * W, aZ[be][bb[be][0]].getHeight());
        ag = true;
        a(bt, null, 0, 0, 0);
        int r05 = r03 + ae;
        int r06 = (bJ * 7) / 100;
        int r07 = ((bJ / 2) - r05) << 1;
        int r08 = ((bJ - aL[12].getHeight()) - 2) - r05;
        bg = bf;
        if ((r07 / r04) < bf) goto L32;
        int r15 = (r05 + (r07 / 2)) - ((bg * r04) / 2);
    L35:
        int r09 = r15;
        if (bg >= bf) goto L49;
        int r16 = 16777215;
        if (bi > 0) goto L40;
    L43:
        int r162 = 16777215;
        if (bi == (bf - 1)) goto L49;
        if (bO != 32) goto L48;
        r162 = 46319;
    L48:
        a(r9, bK, (r09 + (bg * r04)) + r06, r06, r162, true, true);
        bX = r09 + (bg * r04);
        goto L49
    L40:
        if (bO != 21) goto L42;
        r16 = 46319;
    L42:
        a(r9, bK, r09 - r06, r06, r16, true, false);
        bW = r09 - r06;
    L49:
        int r10 = (r15 + (r04 / 2)) + 1;
        bm = bI - (3 * aL[5].getWidth());
        if (bI <= bJ) goto L52;
        bm -= aL[5].getWidth() / 2;
    L52:
        bk = (bI >> 1) - (bm >> 1);
        int r010 = bI / 100;
        int r011 = bk + r010;
        int r012 = (r011 + aZ[be][0].getWidth()) + r010;
        int r163 = bh;
    L54:
        if (r163 >= (bh + bg)) goto L61;
        int r013 = bb[be][r163];
        a(r9, aZ[be][r013], r011, r10, 6);
        if (be != 2) goto L58;
    L59:
        ac = 2;
    L60:
        a(c(bc[be][r013]), r9, r012, r10, 6);
        r10 = r10 + r04;
        r163 = r163 + 1;
        goto L54
    L58:
        if (r013 != (bc[be].length - 1)) goto L60;
    L61:
        bj = bi - bh;
        bl = r15 + (r04 * bj);
        bn = r04;
        bV = r15;
        r9.setColor(16545540);
        r9.drawRect(bk, bl, bm, bn);
    L156:
        c(r9);
        if (bo <= 1) goto L176;
        int r014 = Math.abs(((int) ((System.currentTimeMillis() / 80) % 8)) - 4);
        boolean r11 = 5;
        boolean r122 = 7;
        if (au == false) goto L161;
    L162:
        r11 = 4;
        at++;
    L164:
        if (av == false) goto L166;
    L167:
        r122 = 6;
        at++;
    L168:
        int r015 = (r014 + 1) + aL[r11 ? 1 : 0].getWidth();
        aL[r11 ? 1 : 0].getWidth();
        a(r9, aL[r11 ? 1 : 0], r015, bL, 10);
        a(r9, aL[r122 ? 1 : 0], bI - r015, bL, 6);
        if (at <= 4) goto L177;
        au = false;
        av = false;
        at = 0;
        return;
    L177:
        return;
    L166:
        if (bO != 24) goto L168;
    L161:
        if (bO != 23) goto L164;
    L176:
        return;
    L32:
        if ((r08 / r04) >= bf) goto L34;
        bg = (r08 - (r06 * 2)) / r04;
    L34:
        r15 = (r05 + (r08 / 2)) - ((bg * r04) / 2);
        goto L35
    L63:
        if (aI != w) goto L69;
        a(r9, aM[w], bI / 2, (bJ << 3) / 100, 17);
        ag = true;
        a(x, null, 0, 0, 0);
        Math.abs(((bC - aM[aI].getHeight()) - ae) / 3);
        if (n[0] == false) goto L67;
        ac = 0;
    L67:
        a(c(y), r9, bI - ((aL[4].getWidth() + 8) << 1), bI / 2, (bJ * 60) / 100, 3);
        goto L156
    L69:
        if (aI != s) goto L105;
        if (bJ < 128) goto L73;
        boolean r016 = true;
    L74:
        boolean r102 = r016;
        if (bE != null) goto L77;
    L79:
        boolean r017 = false;
    L80:
        boolean r112 = r017;
        if (r102 == false) goto L83;
        int r018 = aL[10].getHeight();
    L84:
        int r13 = j + ((r018 * 3) / 5);
        if (aM[s].getWidth() <= 220) goto L88;
        r9.setColor(13421772);
        r9.fillRect(bK + 20, r13, 3, 54);
        r13 = r13 + 54;
    L93:
        int r019 = r13 + aM[s].getHeight();
        a(r9, aM[s], bK, r019, 33);
        a(r9, aN, (bK + (aM[s].getWidth() >> 1)) + 1, r019, 40);
        if (r102 == false) goto L97;
        a(r9, aL[10], bK, j, 17);
    L97:
        if (r112 == false) goto L156;
        ac = 2;
        int r020 = bC - r019;
        int r152 = 3;
        if ((W << 1) <= r020) goto L101;
        int r132 = r019 + (W / 3);
        r152 = 17;
    L102:
        a(bE, r9, bI, bK, r132, r152);
        goto L156
    L101:
        r132 = r019 + (r020 / 2);
        goto L102
    L88:
        if (aM[s].getWidth() <= 140) goto L91;
        r9.setColor(14145495);
        r9.fillRect(bK + 14, r13, 2, 37);
        r13 = r13 + 37;
        goto L93
    L91:
        if (aM[s].getWidth() <= 100) goto L93;
        r9.setColor(14465464);
        r9.fillRect(bK + 11, r13, 1, 26);
        r13 = r13 + 26;
        goto L93
    L83:
        r018 = 0;
        goto L84
    L77:
        if (bE.length() <= 0) goto L79;
        r017 = true;
        goto L80
    L73:
        r016 = false;
        goto L74
    L105:
        if (aI == v) goto L107;
        ag = true;
        a(c(bt), r9, 0, 0, 0, 0);
        int r021 = aM[aI].getHeight();
        int r113 = 0;
        int r123 = 0;
        boolean r133 = false;
        if (af <= ((bI - 4) - (aL[12].getWidth() << 1))) goto L130;
        r113 = aL[12].getHeight();
    L130:
        int r164 = Math.max(0, bJ - ((r021 + ae) + r113));
        if (bI > 176) goto L133;
        int r022 = 2;
    L134:
        int r14 = r022;
        boolean r134 = r133;
        if (ae <= ((W << 1) + r14)) goto L139;
        r134 = r133;
        if ((r164 / 3) <= ((W + r14) / 2)) goto L139;
        r164 = Math.max(0, bJ - ((r021 + (W << 1)) + r113));
        r134 = true;
    L139:
        int r17 = r164 / 3;
        boolean r135 = r134;
        if (r113 <= 0) goto L149;
        int r023 = bJ - aL[12].getHeight();
        int r165 = Math.max(0, bJ - (r021 + ae));
        boolean r136 = r134;
        if (ae <= ((W << 1) + r14)) goto L146;
        r136 = r134;
        if ((r165 / 3) <= (ae - ((W + r14) << 1))) goto L146;
        r165 = Math.max(0, bJ - (r021 + (W << 1)));
        r136 = true;
    L146:
        int r024 = r165 / 3;
        r135 = r136;
        if ((((r024 * 2) + ae) + aM[aI].getHeight()) >= r023) goto L149;
        r17 = r024;
        r135 = r136;
    L149:
        int r025 = r17;
        a(r9, aM[aI], bK, r025, 17);
        int r026 = r025 + (r17 + aM[aI].getHeight());
        if (n[0] == false) goto L153;
        ac = 0;
    L153:
        if (r135 == false) goto L155;
        r123 = -(W / 2);
    L155:
        a(c(bt), r9, 0, bK, r026 + r123, 17);
        goto L156
    L133:
        r022 = 0;
        goto L134
    L107:
        if (bJ < 128) goto L109;
        boolean r027 = true;
    L110:
        boolean r103 = r027;
        int r028 = (aM[aI].getWidth() * 70) / 100;
        int r029 = aM[aI].getHeight() >> 1;
        int r030 = (bI >> 1) - (r028 >> 1);
        int r031 = (bJ >> 1) - (r029 >> 1);
        int r153 = r029;
        int r142 = r028;
        if ((r031 + r153) <= bJ) goto L114;
        r153 = bJ - r031;
    L114:
        if ((r030 + r142) <= bI) goto L116;
        r142 = bI - r030;
    L116:
        int r166 = r031;
    L118:
        if (r166 >= (r031 + r153)) goto L120;
        r9.setColor(250 + (((r166 - r031) * (-9)) / r153), 209 + (((r166 - r031) * (-139)) / r153), 45 + (((r166 - r031) * (-24)) / r153));
        r9.drawLine(r030, r166, r030 + r142, r166);
        r166 = r166 + 1;
        goto L118
    L120:
        int r032 = bJ / 2;
        a(r9, aM[aI], bK, r032, 3);
        if (r103 == false) goto L124;
        a(r9, aL[10], bK, j, 17);
    L124:
        if (aO != null) goto L126;
        ag = true;
        a(c(bt), null, 0, 0, 0, 0);
        int r033 = r032 - (ae / 2);
        int r034 = ac;
        ac = 0;
        a(c(bt), r9, aM[aI].getWidth() / 2, bK, r033, 17);
        ac = r034;
        goto L156
    L126:
        a(r9, aO, bK, bJ / 2, 3);
        goto L156
    L109:
        r027 = false;
        goto L110
    L18:
        if (aI != w) goto L20;
        bC = (bJ * 80) / 100;
        goto L20
    L8:
        if (aI == w) goto L9;
        if (aI != s) goto L13;
    L14:
        bC = l - (bA / 2);
        goto L16
    L13:
        if (aI != v) goto L16;
        goto L14
    }

    private static void a(Graphics r11, int r12, int r13, int r14, int r15, boolean r16, boolean r17) {
        if (r17 == false) goto L5;
        int r0 = -1;
    L6:
        int r162 = r0;
        if ((r14 % 2) != 0) goto L9;
        r14 = r14 - 1;
    L9:
        r11.setColor(46319);
        a(r11, r12, r13, r12 - (r14 >> 1), r13 + (r162 * (r14 >> 1)), r12 + (r14 >> 1), r13 + (r162 * (r14 >> 1)));
        r11.setColor(r15);
        a(r11, r12, r13 + r162, (r12 - (r14 >> 1)) + 2, (r13 + (r162 * (r14 >> 1))) - r162, (r12 + (r14 >> 1)) - 2, (r13 + (r162 * (r14 >> 1))) - r162);
        return;
    L5:
        r0 = 1;
        goto L6
    }

    private static void c(Graphics r6) {
        int r0 = bJ - 2;
        int r02 = h;
        int r03 = bI - i;
        if (az == false) goto L5;
        return;
    L5:
        Image r10 = aL[12];
        Image r11 = aL[12];
        if (bO != 26) goto L9;
        r11 = aL[13];
    L13:
        bw = (byte) (bw | 1);
        bv = (byte) (bv | 1);
        bw = (byte) (bw | 2);
        bv = (byte) (bv | 2);
        if ((bw & 1) == 0) goto L17;
        a(r6, r10, r02, r0, 36);
    L17:
        if ((bw & 2) == 0) goto L19;
        a(r6, r11, r03, r0, 40);
    L19:
        int r04 = r02 + ((aL[12].getWidth() / 2) - (aL[9].getWidth() / 2));
        int r05 = r03 + (((-aL[12].getWidth()) / 2) + (aL[8].getWidth() / 2));
        int r06 = r0 + (((-aL[12].getHeight()) / 2) + (aL[9].getHeight() / 2));
        byte r07 = (byte) (bv | 1);
        bv = r07;
        byte r08 = (byte) (r07 | 2);
        bv = r08;
        if ((r08 & 1) == 0) goto L23;
        a(r6, aL[9], r04, r06, 36);
    L23:
        if ((bv & 2) == 0) goto L26;
        a(r6, aL[8], r05, r06, 40);
        return;
    L26:
        return;
    L9:
        if (bO != 25) goto L13;
        if (bQ == false) goto L13;
        r10 = aL[13];
        goto L13
    }

    private static Image a(byte[] r7, int r8, int r9, int r10, int r11) {
        int r82 = 0;
        int r102 = 2;
    L4:
        if (r82 != 0) goto L17;
        if (r102 >= (r9 + 2)) goto L17;
        if ((r7[r102] & 255) != 80) goto L16;
        if ((r7[r102 + 1] & 255) != 76) goto L16;
        if ((r7[r102 + 2] & 255) != 84) goto L16;
        if ((r7[r102 + 3] & 255) != 69) goto L16;
        r82 = r102;
    L16:
        r102 = r102 + 1;
    L17:
        int r0 = ((((r7[r82 - 4] << 24) & (-16777216)) + ((r7[r82 - 3] << 16) & 16711680)) + ((r7[r82 - 2] << 8) & 65280)) + (r7[r82 - 1] & 255);
        r7[(r82 + 4) + 3] = (byte) ((r11 >> 16) & 255);
        r7[((r82 + 4) + 3) + 1] = (byte) ((r11 >> 8) & 255);
        r7[((r82 + 4) + 3) + 2] = (byte) r11;
        byte[] r02 = new byte[r0 + 4];
        System.arraycopy(r7, r82, r02, 0, r0 + 4);
        long[] r03 = new long[256];
        int r13 = 0;
    L19:
        if (r13 >= 256) goto L29;
        long r14 = r13;
        int r16 = 0;
    L22:
        if (r16 >= 8) goto L28;
        if ((r14 & 1) != 1) goto L26;
        long r04 = 3988292384L ^ (r14 >> 1);
    L27:
        r14 = r04;
        r16 = r16 + 1;
        goto L22
    L26:
        r04 = r14 >> 1;
        goto L27
    L28:
        r03[r13] = r14;
        r13 = r13 + 1;
        goto L19
    L29:
        long r142 = 4294967295L;
        int r132 = 0;
    L31:
        if (r132 >= r02.length) goto L33;
        r142 = r03[((int) (r142 ^ r02[r132])) & 255] ^ (r142 >> 8);
        r132 = r132 + 1;
        goto L31
    L33:
        long r05 = r142 ^ 4294967295L;
        r7[(r82 + 4) + r0] = (byte) ((r05 & (-16777216)) >> 24);
        r7[((r82 + 4) + r0) + 1] = (byte) ((r05 & 16711680) >> 16);
        r7[((r82 + 4) + r0) + 2] = (byte) ((r05 & 65280) >> 8);
        r7[((r82 + 4) + r0) + 3] = (byte) (r05 & 255);
        System.gc();
        return Image.createImage(r7, 2, r9);
    }

    private static void a(Graphics r8, int r9, int r10, int r11, int r12, int r13, int r14) {
        r8.fillTriangle(r9, r10, r11, r12, r13, r14);
    }

    private static void a(Graphics r6, Image r7, int r8, int r9, int r10) {
        r6.drawImage(r7, r8, r9, r10);
    }

    private static void a(Graphics r6, int r7, int r8, int r9, int r10) {
        r6.setClip(Math.max(r7, 0), Math.max(r8, 0), Math.min(r9, bI), Math.min(r10, bJ));
    }

    @Override // java.lang.Runnable
    public final void run() {
    L3:
        if (aC == false) goto L20;
        if (aD == null) goto L18;
        String r0 = aD;     // Catch: Exception -> L19
        e = r0;     // Catch: Exception -> L19
        if (r0 != null) goto L9;
    L17:
        aD = null;     // Catch: Exception -> L19
        goto L18
    L9:
        if (e.length() <= 0) goto L17;
        String r02 = e;     // Catch: Exception -> L19
        e = null;     // Catch: Exception -> L19
        new StringBuffer().append("urlPlatformRequest = ").append(r02).toString();     // Catch: Exception -> L19
        aw.platformRequest(r02);     // Catch: Exception -> L12 Exception -> L19
        Thread.sleep(200);     // Catch: Exception -> L12 Exception -> L19
    L14:
        if (bZ == true) goto L16;
        aG = 1;     // Catch: Exception -> L19
        goto L17
    L16:
        aG = 4;     // Catch: Exception -> L19
    L18:
        Thread.sleep(1000);     // Catch: Exception -> L19
        goto L3
    }

    public final void commandAction(Command r4, Displayable r5) {
        if (az == true) goto L5;
        return;
    L5:
        if (r4 != bM) goto L9;
        a(25);
        return;
    L9:
        if (r4 != bN) goto L13;
        a(26);
        return;
    }

    private static InputStream a(String r3) {
        return "a".getClass().getResourceAsStream(r3);
    }

    private static void k() {
        int r9 = bI;
        int r10 = bI - (3 * aL[5].getWidth());     // Catch: Exception -> L7
        if (bI <= bJ) goto L6;
        r10 = r10 - (aL[5].getWidth() / 2);     // Catch: Exception -> L7
    L6:
        int r0 = (bI >> 1) - (r10 >> 1);     // Catch: Exception -> L7
        int r02 = bI / 100;     // Catch: Exception -> L7
        r9 = (r0 + r10) - (((r0 + r02) + aZ[be][0].getWidth()) + r02);     // Catch: Exception -> L7
    L8:
        int r102 = 0;
    L108:
        if (r102 >= ar.length) goto L103;
        if (ar[r102] == null) goto L102;
        if (r102 != z) goto L16;
    L27:
        int r11 = bI;     // Catch: Exception -> L104
        int r12 = 3;
        if (am.length <= 1) goto L34;
        if (r102 < K) goto L34;
        if (r102 >= (K + am.length)) goto L34;
    L41:
        int r03 = r9;
    L55:
        r11 = r03;
    L56:
        String[] r04 = ar;     // Catch: Exception -> L104
        int r1 = r102;
        String r2 = ar[r102];     // Catch: Exception -> L104
        int r3 = r11;
        int r4 = r12;
        String r14 = "";
        int r15 = 0;
        int r16 = 0;
        String r17 = "";
        char[] r22 = r2.toCharArray();     // Catch: Exception -> L104
        int r19 = 0;
    L58:
        if (r19 >= r2.length()) goto L79;
        if (r22[r19] != 65292) goto L62;
    L69:
        r17 = new StringBuffer().append(new StringBuffer().append(r17).append(r22[r19]).toString()).append('|').toString();     // Catch: Exception -> L104
    L77:
        r19 = r19 + 1;     // Catch: Exception -> L104
        goto L58
    L62:
        if (r22[r19] == 65281) goto L69;
        if (r22[r19] == 65311) goto L69;
        if (r22[r19] == 65307) goto L69;
        if (r22[r19] == 65306) goto L69;
        if (r22[r19] != '\b') goto L74;
        r17 = new StringBuffer().append(r17).append('|').toString();     // Catch: Exception -> L104
        goto L77
    L74:
        if (r22[r19] != '\n') goto L76;
        r17 = new StringBuffer().append(r17).append(' ').toString();     // Catch: Exception -> L104
        goto L77
    L76:
        r17 = new StringBuffer().append(r17).append(r22[r19]).toString();     // Catch: Exception -> L104
        goto L77
    L79:
        if (al[bD].equals("ZH-TW") == true) goto L81;
    L87:
        String r23 = r17;
        short[] r24 = a(r23, r3, true);     // Catch: Exception -> L104
        int r172 = 0;
    L89:
        if (r172 >= r24[0]) goto L101;
        if (r15 == 0) goto L95;
        r16 = r16 + 1;     // Catch: Exception -> L104
        r14 = r14.trim();     // Catch: Exception -> L104
        if (r16 >= r4) goto L101;
        r14 = new StringBuffer().append(r14).append("\n").toString();     // Catch: Exception -> L104
    L95:
        r14 = new StringBuffer().append(r14).append(r23.substring(r15, r24[(r172 << 1) + 1])).toString();     // Catch: Exception -> L104
        r15 = r24[(r172 << 1) + 1];     // Catch: Exception -> L104
        if (r23.length() == r15) goto L100;
        if (r23.charAt(r24[(r172 << 1) + 1]) != '\n') goto L100;
        r15 = r15 + 1;     // Catch: Exception -> L104
    L100:
        r172 = r172 + 1;     // Catch: Exception -> L104
    L101:
        r04[r1] = r14;     // Catch: Exception -> L104
        ar[r102] = b(ar[r102]);     // Catch: Exception -> L104
        goto L102
    L81:
        if (r17.indexOf(124) != (-1)) goto L87;
        if (r17.indexOf(32) != (-1)) goto L87;
        if (r17.length() <= 5) goto L87;
        r17 = new StringBuffer().append(r17.substring(0, r17.length() >> 1)).append("|").append(r17.substring(r17.length() >> 1, r17.length())).toString();     // Catch: Exception -> L104
    L34:
        if (an.length <= 1) goto L40;
        if (r102 < M) goto L40;
        if (r102 < (M + an.length)) goto L41;
    L40:
        if (r102 == y) goto L41;
        if (r102 != z) goto L45;
    L48:
        r12 = 2;
        r03 = (bI - (aL[12].getWidth() << 1)) - 4;     // Catch: Exception -> L104
        goto L55
    L45:
        if (r102 == B) goto L48;
        if (r102 == F) goto L48;
        if (r102 != (x - 1)) goto L56;
        if (aM[v] == null) goto L54;
        r03 = aM[v].getWidth() >> 1;     // Catch: Exception -> L104
        goto L55
    L54:
        r03 = bI;     // Catch: Exception -> L104
        goto L55
    L16:
        if (r102 == B) goto L27;
        if (r102 == F) goto L27;
        if (r102 == y) goto L27;
        if (r102 < x) goto L27;
        if (r102 < K) goto L102;
        if (r102 < (M + an.length)) goto L27;
    L102:
        r102 = r102 + 1;     // Catch: Exception -> L104
        goto L108
    L103:
        return;
    }

    private static short[] a(String r5, int r6, boolean r7) {
        int r0 = r5.length();
        short r9 = 0;
        short r10 = 1;
        short r11 = 0;
        boolean r13 = false;
        short r14 = 0;
        int r15 = 0;
    L4:
        if (r15 >= r0) goto L67;
        char r02 = r5.charAt(r15);
        if (r02 != ' ') goto L34;
        r9 = (short) (r9 + d(r02, Z));
        short r03 = r11;
        r11 = (short) r15;
        r13 = true;
        r14 = 0;
        if (r11 != (r0 - 2)) goto L13;
        if ((r9 + d(r5.charAt(r11 + 1), Z)) <= r6) goto L13;
        r11 = r03;
        short r2 = (short) (r10 + 1);
        cb[r10] = (short) (r11 + 1);
        r10 = (short) (r2 + 1);
        cb[r2] = r9;
        r9 = 0;
    L13:
        if (r9 <= r6) goto L65;
        r13 = false;
        int r16 = r11;
    L16:
        if (r16 < 0) goto L20;
        if (r5.charAt(r16) != ' ') goto L20;
        r16 = r16 - 1;
        r9 = (short) (r9 - d(32, Z));
    L20:
        int r162 = 0;
    L22:
        if (r11 >= r0) goto L27;
        if (r5.charAt(r11) != ' ') goto L27;
        r11 = (short) (r11 + 1);
        r162 = r162 + 1;
    L27:
        if (r162 == 1) goto L29;
    L31:
        short r04 = (short) (r11 - 1);
        r11 = r04;
        r15 = r04;
    L32:
        short r22 = (short) (r10 + 1);
        cb[r10] = (short) (r11 + 1);
        r10 = (short) (r22 + 1);
        cb[r22] = r9;
        r9 = 0;
        goto L65
    L29:
        if (r15 != (r0 - 2)) goto L31;
        r11 = r03;
    L65:
        r15 = r15 + 1;
        goto L4
    L34:
        if (r02 != '|') goto L52;
        if (r7 == false) goto L67;
        r9 = (short) (r9 + d(r02, Z));
        r11 = (short) r15;
        r13 = true;
        r14 = 0;
        if (r9 <= r6) goto L65;
        r13 = false;
        int r163 = r11;
    L41:
        if (r163 < 0) goto L46;
        if (r5.charAt(r163) != ' ') goto L46;
        r163 = r163 - 1;
        r9 = (short) (r9 - d(32, Z));
    L46:
        if (r11 >= r0) goto L50;
        if (r5.charAt(r11) != ' ') goto L50;
        r11 = (short) (r11 + 1);
    L50:
        short r05 = (short) (r11 - 1);
        r11 = r05;
        r15 = r05;
        short r23 = (short) (r10 + 1);
        cb[r10] = (short) (r11 + 1);
        r10 = (short) (r23 + 1);
        cb[r23] = r9;
        r9 = 0;
        goto L65
    L52:
        if (r02 != '\n') goto L54;
        short r24 = (short) (r10 + 1);
        cb[r10] = (short) r15;
        r10 = (short) (r24 + 1);
        cb[r24] = r9;
        r9 = 0;
        r14 = 0;
        goto L65
    L54:
        int r06 = d(r02, Z) + m;
        r14 = (short) (r14 + r06);
        short r07 = (short) (r9 + r06);
        r9 = r07;
        if (r07 <= r6) goto L65;
        if (r13 == false) goto L65;
        r13 = false;
        int r164 = r11;
    L60:
        if (r164 < 0) goto L64;
        if (r5.charAt(r164) != ' ') goto L64;
        r164 = r164 - 1;
        r9 = (short) (r9 - d(32, aa));
    L64:
        short r25 = (short) (r10 + 1);
        cb[r10] = (short) (r11 + 1);
        r10 = (short) (r25 + 1);
        cb[r25] = (short) (r9 - r14);
        r9 = 0;
        r15 = r11;
    L67:
        if (r9 == 0) goto L69;
        short r26 = (short) (r10 + 1);
        cb[r10] = (short) r0;
        r10 = (short) (r26 + 1);
        cb[r26] = r9;
    L69:
        cb[0] = (short) (r10 / 2);
        return cb;
    }

    private static String b(String r4) {
        String r5 = "";
        char[] r0 = r4.toCharArray();
        int r7 = 0;
    L4:
        if (r7 >= r4.length()) goto L10;
        if (r0[r7] == '|') goto L8;
        r5 = new StringBuffer().append(r5).append(r0[r7]).toString();
    L8:
        r7 = r7 + 1;
        goto L4
    L10:
        return r5;
    }

    static {
        a = "2.1";
        b = new StringBuffer().append("IGP-Signature=").append(a).toString();
        c = "";
        d = 0;
        g = 13568256;
        h = 2;
        i = 2;
        m = 0;
        n = new boolean[1];
        o = new int[1];
        q = -1;
        r = 8;
        Q = 14;
        X = 0;
        Y = 1;
        Z = 2;
        aa = 3;
        ah = new String[]{"URL-WN", "URL-BS", "URL"};
        al = new String[0];
        az = false;
        aA = null;
        aB = null;
        aC = false;
        aD = null;
        aE = false;
        aF = false;
        bo = -1;
        bs = 0;
        bG = "";
        bO = 0;
        bP = false;
        bQ = false;
        bR = -1;
        bS = -1;
        bT = -1;
        bU = -1;
        bY = new Hashtable();
        bZ = false;
        ca = 0;
        cb = new short[50];
    }
}
