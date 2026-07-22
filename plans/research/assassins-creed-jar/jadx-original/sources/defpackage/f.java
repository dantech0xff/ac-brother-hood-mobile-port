package defpackage;

import java.io.IOException;
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
    private static String e;
    private static Font f;
    private static int j;
    private static int k;
    private static int l;
    private static int p;
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
    private static int R;
    private static int[] S;
    private static byte[][] T;
    private static int U;
    private static byte[] V;
    private static int W;
    private static Image[] ab;
    private static int ac;
    private static int[] ad;
    private static int ae;
    private static int af;
    private static boolean ag;
    private static String ai;
    private static boolean aj;
    private static boolean ak;
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
    private static String[] bp;
    private static int[] bq;
    private static boolean[] br;
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
    private static String bH;
    private static int bI;
    private static int bJ;
    private static int bK;
    private static int bL;
    private static Command bM;
    private static Command bN;
    private static int bV;
    private static int bW;
    private static int bX;
    private static String a = "2.1";
    private static String b = new StringBuffer().append("IGP-Signature=").append(a).toString();
    private static String c = "";
    private static int d = 0;
    private static int g = 13568256;
    private static int h = 2;
    private static int i = 2;
    private static int m = 0;
    private static boolean[] n = new boolean[1];
    private static int[] o = new int[1];
    private static int q = -1;
    private static int r = 8;
    private static int Q = 14;
    private static int X = 0;
    private static int Y = 1;
    private static int Z = 2;
    private static int aa = 3;
    private static final String[] ah = {"URL-WN", "URL-BS", "URL"};
    private static String[] al = new String[0];
    private static boolean az = false;
    private static CommandListener aA = null;
    private static f aB = null;
    private static boolean aC = false;
    private static String aD = null;
    private static boolean aE = false;
    private static boolean aF = false;
    private static int bo = -1;
    private static int bs = 0;
    private static String bG = "";
    private static int bO = 0;
    private static boolean bP = false;
    private static boolean bQ = false;
    private static int bR = -1;
    private static int bS = -1;
    private static int bT = -1;
    private static int bU = -1;
    private static Hashtable bY = new Hashtable();
    private static boolean bZ = false;
    private static int ca = 0;
    private static short[] cb = new short[50];

    private static boolean c() throws IOException {
        d();
        try {
            InputStream inputStreamA = a("/dataIGP");
            int i2 = inputStreamA.read() & 255;
            R = i2;
            int i3 = i2 + ((inputStreamA.read() & 255) << 8);
            R = i3;
            S = new int[i3];
            for (int i4 = 0; i4 < R; i4++) {
                S[i4] = inputStreamA.read() & 255;
                int[] iArr = S;
                int i5 = i4;
                iArr[i5] = iArr[i5] + ((inputStreamA.read() & 255) << 8);
                int[] iArr2 = S;
                int i6 = i4;
                iArr2[i6] = iArr2[i6] + ((inputStreamA.read() & 255) << 16);
                int[] iArr3 = S;
                int i7 = i4;
                iArr3[i7] = iArr3[i7] + ((inputStreamA.read() & 255) << 24);
            }
            inputStreamA.close();
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    private static void d() {
        S = null;
        T = null;
        R = 0;
        System.gc();
    }

    private static byte[] b(int i2) throws IOException {
        int i3;
        if (i2 < 0 || i2 >= R - 1 || (i3 = S[i2 + 1] - S[i2]) == 0) {
            return null;
        }
        byte[] bArr = null;
        try {
            InputStream inputStreamA = a("/dataIGP");
            inputStreamA.skip(2 + (4 * R) + S[i2]);
            byte[] bArr2 = new byte[i3];
            bArr = bArr2;
            for (int length = bArr2.length; length > 0; length -= inputStreamA.read(bArr)) {
            }
            inputStreamA.close();
        } catch (Exception unused) {
        }
        return bArr;
    }

    private static int a(byte[] bArr) {
        int i2 = U;
        U = i2 + 1;
        int i3 = bArr[i2] & 255;
        int i4 = U;
        U = i4 + 1;
        return i3 + ((bArr[i4] & 255) << 8);
    }

    private static byte[] b(byte[] bArr) {
        int iA = a(bArr);
        byte[] bArr2 = new byte[iA];
        System.arraycopy(bArr, U, bArr2, 0, iA);
        U += iA;
        return bArr2;
    }

    private static Image c(byte[] bArr) {
        byte[] bArrB = b(bArr);
        return Image.createImage(bArrB, 0, bArrB.length);
    }

    private static String c(int i2) {
        return new StringBuffer().append("").append(ar[i2]).toString();
    }

    private static int d(int i2, int i3) {
        if (ca == 0) {
            return V[(i2 << 2) + i3] & 255;
        }
        if (i3 != 0 && i3 != Y) {
            return V[(i2 * 6) + i3] & 255;
        }
        return 0 | (((V[((i2 * 6) + i3) + 1] & 255) & 255) << 8) | (V[(i2 * 6) + i3] & 255 & 255);
    }

    private static void a(int i2, Graphics graphics, int i3, int i4, int i5) {
        a(c(i2), graphics, bI, i3, i4, i5);
    }

    private static void a(String str, Graphics graphics, int i2, int i3, int i4, int i5) {
        a(str, graphics, i3, i4, i5);
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x0063 A[PHI: r18
      0x0063: PHI (r18v13 int) = (r18v10 int), (r18v1 int) binds: [B:17:0x0060, B:13:0x004b] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Removed duplicated region for block: B:59:0x019d A[PHI: r18
      0x019d: PHI (r18v8 int) = (r18v5 int), (r18v3 int) binds: [B:58:0x019a, B:54:0x0185] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Removed duplicated region for block: B:68:0x01fc A[PHI: r20 r23
      0x01fc: PHI (r20v2 boolean) = (r20v1 boolean), (r20v3 boolean) binds: [B:61:0x01b6, B:66:0x01e3] A[DONT_GENERATE, DONT_INLINE]
      0x01fc: PHI (r23v3 int) = (r23v2 int), (r23v5 int) binds: [B:61:0x01b6, B:66:0x01e3] A[DONT_GENERATE, DONT_INLINE]] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static void a(String str, Graphics graphics, int i2, int i3, int i4) {
        ad[0] = 0;
        af = 0;
        int i5 = 0;
        int length = str.length();
        int i6 = 0;
        if (bI <= 176 && bq[aI] == 4) {
            i6 = 0 + 2;
        }
        int i7 = 0;
        while (i7 < length) {
            char cCharAt = str.charAt(i7);
            if (cCharAt != '\n' || i5 >= 10) {
                if (cCharAt == '\\') {
                    i7++;
                    if (str.charAt(i7) == 'N') {
                        int[] iArr = ad;
                        int i8 = i5;
                        iArr[i8] = iArr[i8] - m;
                        if (ad[i5] > af) {
                            af = ad[i5];
                        }
                        i5++;
                        ad[i5] = 0;
                    }
                }
                if (cCharAt != 0 && cCharAt != 1) {
                    int[] iArr2 = ad;
                    int i9 = i5;
                    iArr2[i9] = iArr2[i9] + d(cCharAt, Z) + m;
                }
            }
            i7++;
        }
        int[] iArr3 = ad;
        int i10 = i5;
        iArr3[i10] = iArr3[i10] - m;
        if (ad[i5] > af) {
            af = ad[i5];
        }
        ae = ((i5 + 1) * W) + (i5 * (i6 + 0));
        if (ag) {
            ag = false;
            return;
        }
        int i11 = i3 + (((i6 + 0) * i5) / 2);
        int i12 = 0;
        if ((i4 & 32) != 0) {
            i11 -= ae;
        } else if ((i4 & 2) != 0) {
            i11 -= ae >> 1;
        }
        int iD = i2;
        boolean z2 = true;
        boolean z3 = false;
        a(graphics, 0, 0, bI, bJ);
        int i13 = 0;
        while (i13 < length) {
            char cCharAt2 = str.charAt(i13);
            if (z2) {
                iD = i2;
                if ((i4 & 8) != 0) {
                    iD = i2 - ad[i12];
                } else if ((i4 & 1) != 0) {
                    iD = i2 - (ad[i12] >> 1);
                }
                z2 = false;
            }
            if (cCharAt2 != '\n' || i12 >= 10) {
                if (cCharAt2 == '\\') {
                    i13++;
                    if (str.charAt(i13) == 'N') {
                        i11 += (W + i6) - 1;
                        i12++;
                        z2 = true;
                        z3 = true;
                    }
                }
                if (z3) {
                    z3 = false;
                    char cCharAt3 = str.charAt(i13 - 2);
                    if (cCharAt3 == ' ') {
                        iD -= (d(cCharAt3, Z) + m) >> 1;
                    }
                    if (cCharAt2 == ' ') {
                        iD += (d(cCharAt2, Z) + m) >> 1;
                    } else {
                        a(graphics, iD, i11, d(cCharAt2, Z), d(cCharAt2, aa));
                        graphics.drawRegion(ab[ac], d(cCharAt2, 0), d(cCharAt2, Y), d(cCharAt2, Z), d(cCharAt2, aa), 0, iD, i11, 20);
                        iD += d(cCharAt2, Z) + m;
                    }
                }
            }
            i13++;
        }
        a(graphics, 0, 0, bI, bJ);
        ac = 1;
    }

    public static void a(MIDlet mIDlet, Canvas canvas, int i2, int i3) {
        new StringBuffer().append("initialize(midlet = ").append(mIDlet).append(", game = ").append(canvas).append(", screenWidth = ").append(400).append(", screenHeight = ").append(240).append(", cmdListener = ").append((Object) null).append(")").toString();
        bI = 400;
        bJ = 240;
        bK = bI >> 1;
        bL = bJ >> 1;
        j = (bJ * 5) / 100;
        k = bJ / 2;
        l = (bJ * 93) / 100;
        if (2 > bI) {
            h = 2;
        }
        if (2 > bI) {
            i = 2;
        }
        if (aw != null || canvas == null) {
            return;
        }
        aw = mIDlet;
        ax = canvas;
        e();
        new StringBuffer().append(b).append("").toString();
    }

    private static boolean b(String str, int i2) {
        if (str == null) {
            return (i2 & 1) == 0;
        }
        String strTrim = str.trim();
        if ((i2 & 1) != 0 && strTrim.length() == 0) {
            return false;
        }
        if ((i2 & 2) != 0 && strTrim.toUpperCase().compareTo("DEL") == 0) {
            return false;
        }
        if ((i2 & 4) != 0) {
            return (strTrim.toUpperCase().compareTo("NO") == 0 || strTrim.toUpperCase().compareTo("0") == 0) ? false : true;
        }
        return true;
    }

    /* JADX WARN: Removed duplicated region for block: B:31:0x00d0 A[PHI: r8
      0x00d0: PHI (r8v1 java.lang.String) = 
      (r8v0 java.lang.String)
      (r8v0 java.lang.String)
      (r8v0 java.lang.String)
      (r8v0 java.lang.String)
      (r8v0 java.lang.String)
      (r8v7 java.lang.String)
      (r8v7 java.lang.String)
      (r8v9 java.lang.String)
     binds: [B:4:0x0004, B:6:0x0008, B:8:0x000c, B:10:0x002f, B:12:0x0036, B:24:0x008e, B:26:0x0097, B:30:0x00ce] A[DONT_GENERATE, DONT_INLINE]] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static String a(String str, String str2, String str3) {
        String string = "";
        if (str3 != null && str != null && str2 != null) {
            try {
                int iIndexOf = str.indexOf(new StringBuffer().append(str2).append("=").toString());
                String strTrim = str3.trim();
                if (iIndexOf >= 0 && strTrim.length() > 0) {
                    int length = iIndexOf + str2.length() + 1;
                    int iIndexOf2 = str.indexOf(";", length);
                    int length2 = iIndexOf2;
                    if (iIndexOf2 < 0) {
                        length2 = str.length();
                    }
                    String strTrim2 = str.substring(length, length2).trim();
                    string = strTrim2;
                    if (strTrim2.length() == 0 || string.compareTo("0") == 0 || string.toUpperCase().compareTo("NO") == 0) {
                        string = "";
                    } else if (string.toUpperCase().compareTo("DEL") != 0 && str2.compareTo("OP") != 0) {
                        int iIndexOf3 = strTrim.indexOf("XXXX");
                        if (iIndexOf3 >= 0) {
                            string = new StringBuffer().append(strTrim.substring(0, iIndexOf3)).append(string).append(strTrim.substring(iIndexOf3 + "XXXX".length())).toString();
                        } else {
                            string = strTrim;
                        }
                    }
                }
            } catch (Exception unused) {
                string = "";
            }
        }
        return string;
    }

    private static void a(int i2, String str, int i3, String str2, String str3) {
        try {
            String strA = ak ? a(aw.getAppProperty(str2), str, str3) : aw.getAppProperty(new StringBuffer().append("URL-").append(str).toString());
            if (b(strA, 7)) {
                if (strA.toUpperCase().compareTo("NO") == 0 && strA.toUpperCase().compareTo("0") == 0) {
                    return;
                }
                br[i2] = true;
                bp[i2] = strA;
                if (!br[i2] || i2 == s) {
                    return;
                }
                bs++;
                bq[i2] = 4;
                if (aj) {
                    StringBuffer stringBuffer = new StringBuffer();
                    String[] strArr = bp;
                    strArr[i2] = stringBuffer.append(strArr[i2]).append("&ctg=SC").append(bs < 10 ? "0" : "").append(bs).toString();
                }
            }
        } catch (Exception unused) {
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:37:0x0104 A[Catch: Exception -> 0x01ac, TryCatch #0 {Exception -> 0x01ac, blocks: (B:25:0x009d, B:27:0x00a6, B:29:0x00ac, B:47:0x0173, B:49:0x017d, B:30:0x00c0, B:32:0x00c9, B:36:0x00f6, B:37:0x0104, B:39:0x010a, B:41:0x0116, B:42:0x0126, B:44:0x0132, B:45:0x013a, B:46:0x0149), top: B:57:0x009d }] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static void a(int i2, String[] strArr, int i3, String str) {
        String string;
        int length = strArr.length;
        ba[i2] = new String[length];
        bb[i2] = new int[length];
        bc[i2] = new int[length];
        int i4 = 0;
        if (n[0]) {
            return;
        }
        string = "";
        if (ak) {
            try {
                str = aw.getAppProperty(str);
                string = i2 != 2 ? ai : (bG.length() <= 0 || !aj) ? bG.length() > 0 ? bG : "" : new StringBuffer().append(bG).append("&ctg=XXXX").toString();
            } catch (Exception unused) {
            }
        }
        for (int i5 = 0; i5 < strArr.length; i5++) {
            String strA = "";
            if (i2 != 2) {
                try {
                    if (i5 != length - 1) {
                        strA = ak ? strArr[i5].compareTo("GLDT") == 0 ? a(str, strArr[i5], ai) : strArr[i5].compareTo("CATALOG") == 0 ? bG : a(str, strArr[i5], string) : aw.getAppProperty(new StringBuffer().append(ah[i2]).append("-").append(strArr[i5]).toString());
                    } else if (!ak) {
                        strA = aw.getAppProperty(ah[i2]);
                    } else if (bG.length() > 0) {
                        strA = a(aw.getAppProperty("IGP-CATEGORIES"), strArr[i5], new StringBuffer().append(bG).append(aj ? "&ctg=XXXX" : "").toString());
                    }
                    if (b(strA, 7)) {
                        ba[i2][i4] = strA;
                        int i6 = i4;
                        i4++;
                        bb[i2][i6] = i5;
                        bc[i2][i5] = (i3 - (strArr.length - 1)) + i5;
                    }
                } catch (Exception unused2) {
                }
            }
        }
        if (i4 > 0) {
            br[t + i2] = true;
            bd[i2] = i4;
        }
    }

    private static String[] d(byte[] bArr) {
        String[] strArr = new String[a(bArr)];
        for (int i2 = 0; i2 < strArr.length; i2++) {
            int iA = a(bArr);
            strArr[i2] = new String(bArr, U, iA);
            U += iA;
        }
        return strArr;
    }

    /* JADX WARN: Type inference failed for: r0v81, types: [java.lang.String[], java.lang.String[][]] */
    /* JADX WARN: Type inference failed for: r0v83, types: [int[], int[][]] */
    /* JADX WARN: Type inference failed for: r0v85, types: [int[], int[][]] */
    private static void e() {
        try {
            if (!c()) {
                aE = false;
                return;
            }
            byte[] bArrB = b(0);
            a(bArrB);
            int iA = a(bArrB);
            new String(bArrB, U, iA);
            U += iA;
            al = d(bArrB);
            String[] strArrD = d(bArrB);
            am = d(bArrB);
            an = d(bArrB);
            ao = d(bArrB);
            for (int i2 = 0; i2 < n.length; i2++) {
                n[i2] = a(bArrB) == 1;
            }
            try {
                String str = new String(bArrB, U, a(bArrB));
                c = str;
                if (str.equals("2.1z")) {
                    ca = 2;
                    Y = 2;
                    Z = 4;
                    aa = 5;
                }
                if (!c.startsWith(a)) {
                    new StringBuffer().append("Invalid dataIGP file, dataIGP file IGP Version : ").append(c).toString();
                    new StringBuffer().append("IGP Class version : ").append(a).toString();
                }
            } catch (Exception unused) {
                aE = false;
            }
            d();
            int length = (strArrD.length - 1 > 0 ? strArrD.length - 1 : 0) + 4 + 1;
            x = length;
            int i3 = length + 1;
            y = i3;
            int i4 = i3 + 1;
            z = i4;
            int i5 = i4 + 1;
            A = i5;
            int i6 = i5 + 1;
            B = i6;
            int i7 = i6 + 1;
            C = i7;
            int i8 = i7 + 1;
            D = i8;
            int i9 = i8 + 1;
            E = i9;
            int i10 = i9 + 1;
            F = i10;
            int i11 = i10 + 1;
            G = i11;
            int i12 = i11 + 1;
            H = i12;
            int i13 = i12 + 1;
            I = i13;
            int i14 = i13 + 1;
            J = i14;
            if (am.length > 1) {
                int i15 = i14 + 1;
                K = i15;
                L = i15 + (am.length - 1);
            } else {
                L = i14 + 1;
            }
            int i16 = L;
            if (an.length > 1) {
                int i17 = i16 + 1;
                M = i17;
                N = i17 + (an.length - 1);
            } else {
                N = i16 + 1;
            }
            int i18 = N + 1;
            O = i18;
            P = i18 + 1;
            int length2 = strArrD.length;
            ap = length2;
            aX = new Image[length2];
            aq = ap + 1 + 1 + 1 + 1 + (n[0] ? 1 : 0);
            for (int i19 = 0; i19 < n.length; i19++) {
                if (n[i19]) {
                    int i20 = r + 1;
                    r = i20;
                    o[i19] = i20;
                } else {
                    int i21 = q - 1;
                    q = i21;
                    o[i19] = i21;
                }
            }
            int i22 = r + 1;
            r = i22;
            p = i22;
            bp = new String[aq];
            br = new boolean[aq];
            bq = new int[aq];
            for (int i23 = 0; i23 < br.length; i23++) {
                br[i23] = false;
            }
            int i24 = ap;
            s = i24;
            int i25 = i24 + 1;
            t = i25;
            int i26 = i25 + 1;
            u = i26;
            int i27 = i26 + 1;
            v = i27;
            w = i27 + 1;
            ba = new String[3];
            bb = new int[3];
            bc = new int[3];
            bd = new int[3];
            try {
                String appProperty = aw.getAppProperty("URL-TEMPLATE-GAME");
                ai = appProperty;
                if (appProperty != null) {
                    ai = ai.trim();
                    ak = true;
                    if (ai.indexOf("ingameads.gameloft.com/redir") != -1) {
                        aj = true;
                    }
                }
            } catch (Exception unused2) {
            }
            for (int i28 = 0; i28 < ap; i28++) {
                a(i28, strArrD[i28], 7, "IGP-PROMOS", ai);
            }
            try {
                String strTrim = aw.getAppProperty("URL-OPERATOR").trim();
                if (b(strTrim, 7)) {
                    bG = strTrim;
                }
                bE = aw.getAppProperty("URL-PT");
            } catch (Exception unused3) {
            }
            if (!n[0]) {
                if (!ak) {
                    String appProperty2 = aw.getAppProperty("URL-PROMO");
                    if (appProperty2 != null) {
                        appProperty2.trim();
                        if (b(aw.getAppProperty("URL-PROMO"), 7)) {
                            bp[s] = appProperty2;
                            br[s] = true;
                            bq[s] = 5;
                        }
                    }
                } else if (b(bG, 7)) {
                    a(ap, "PROMO", 7, "IGP-CATEGORIES", new StringBuffer().append(bG).append(aj ? "&ctg=XXXX" : "").toString());
                }
            }
            a(0, am, L, "IGP-WN");
            a(1, an, N, "IGP-BS");
            if (!n[0]) {
                if (ak) {
                    if (b(a(aw.getAppProperty("IGP-CATEGORIES"), "OP", bG), 7)) {
                        if (b(bG, 7)) {
                            br[v] = true;
                        }
                        bp[v] = bG;
                    }
                } else if (b(bG, 7)) {
                    bp[v] = bG;
                    br[v] = true;
                }
                if (br[v] && aj) {
                    StringBuffer stringBuffer = new StringBuffer();
                    String[] strArr = bp;
                    int i29 = v;
                    strArr[i29] = stringBuffer.append(strArr[i29]).append("&ctg=CCTL").toString();
                }
            }
            if (n[0]) {
                try {
                    if (b(bG, 7)) {
                        bp[w] = bG;
                        br[w] = true;
                    }
                } catch (Exception unused4) {
                }
            }
            int iG = g();
            bo = iG;
            if (iG > 0) {
                aE = true;
            }
            new StringBuffer().append("isAvailable = ").append(aE).toString();
        } catch (Exception unused5) {
            aE = false;
        }
    }

    public static int a() {
        return (aE && g() > 0) ? 0 : -1;
    }

    public static void a(String str, int i2) {
        new StringBuffer().append("enterIGP(loadingMsg = ").append(str).append(", appLanguage = ").append(0).append(" (").append(al[0]).append(")").toString();
        aH = 0;
        bO = 0;
        bP = true;
        bQ = false;
        bZ = false;
        if (0 >= 0 && 0 < al.length) {
            bD = 0 <= al.length ? 0 : 0;
            bF = str;
            aJ = -1;
            aG = 0;
            aI = 0;
            bh = 0;
            bi = 0;
            ad = new int[10];
            aC = true;
            f = Font.getFont(0, 0, 8);
            bv = (byte) 0;
            bw = (byte) 0;
            new Thread(new f()).start();
        }
        aK = 4 + aq;
        RecordStore recordStoreOpenRecordStore = null;
        try {
            recordStoreOpenRecordStore = RecordStore.openRecordStore("igp19", false);
        } catch (Exception unused) {
            try {
                recordStoreOpenRecordStore = RecordStore.openRecordStore("igp19", true);
            } catch (Exception unused2) {
            }
        }
        if (recordStoreOpenRecordStore != null) {
            try {
                recordStoreOpenRecordStore.closeRecordStore();
            } catch (Exception unused3) {
            }
        }
        aI = h();
        if (az) {
            ax.setCommandListener(aB);
        }
    }

    public static void b() {
        bZ = true;
        if (f()) {
            aG = 7;
            aC = true;
            new Thread(new f()).start();
            aD = bH;
        }
    }

    private static boolean f() {
        boolean z2 = false;
        for (int i2 = 0; i2 < al.length; i2++) {
            if (al[i2].equals("SP")) {
                z2 = true;
            }
        }
        if (!z2) {
            return false;
        }
        try {
            String appProperty = aw.getAppProperty("IGP-CATEGORIES");
            if (appProperty == null || appProperty.indexOf("ZVIP") == -1) {
                String appProperty2 = aw.getAppProperty("URL-ZVIP");
                bH = appProperty2;
                if (appProperty2 == null) {
                    return false;
                }
                aF = true;
                return true;
            }
            int iIndexOf = appProperty.indexOf("ZVIP") + "ZVIP".length() + 1;
            int length = iIndexOf + "ZVIP".length();
            if (length >= appProperty.length() || !appProperty.substring(iIndexOf, length).equals("ZVIP")) {
                return false;
            }
            bH = bG;
            if (aj) {
                String string = new StringBuffer().append(bH).append("&ctg=XXXX").toString();
                bH = string;
                int iIndexOf2 = string.indexOf("XXXX");
                if (iIndexOf2 >= 0) {
                    bH = new StringBuffer().append(bH.substring(0, iIndexOf2)).append("ZVIP").append(bH.substring(iIndexOf2 + "XXXX".length())).toString();
                }
                if (bH.length() == 0) {
                    return false;
                }
            }
            aF = true;
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    private static int g() {
        int i2 = 0;
        for (int i3 = 0; i3 < br.length; i3++) {
            if (br[i3]) {
                i2++;
            }
        }
        return i2;
    }

    private static int h() {
        for (int i2 = 0; i2 < br.length; i2++) {
            if (br[i2]) {
                return i2;
            }
        }
        return -1;
    }

    private static void i() {
        if (bE == null) {
            return;
        }
        int length = bE.length();
        if (length <= 0) {
            bE = null;
            return;
        }
        boolean z2 = false;
        int length2 = 0;
        int iD = 0;
        int i2 = 0;
        int i3 = 1;
        String string = "";
        bE = bE.toUpperCase();
        int i4 = 0;
        while (true) {
            if (i4 >= length) {
                break;
            }
            char cCharAt = bE.charAt(i4);
            if ((cCharAt < ' ' || cCharAt > 'z') && cCharAt != 130 && cCharAt != '\n') {
                string = null;
                break;
            }
            if (cCharAt == '\n') {
                z2 = true;
            } else if (i4 < length - 1 && cCharAt == '\\' && (bE.charAt(i4 + 1) == 'n' || bE.charAt(i4 + 1) == 'N')) {
                z2 = true;
                i4++;
            }
            if (z2) {
                if (string.length() > 0) {
                    i3++;
                    if (i3 == 3) {
                        string = null;
                        break;
                    } else {
                        string = new StringBuffer().append(string).append('\n').toString();
                        iD = 0;
                    }
                } else {
                    string = new StringBuffer().append(string).append("").toString();
                }
                z2 = false;
            } else {
                string = new StringBuffer().append(string).append(cCharAt).toString();
                iD += d(cCharAt, Z);
                if (cCharAt == ' ') {
                    length2 = string.length() - 1;
                    i2 = iD;
                }
            }
            if (iD >= bI) {
                if (i3 >= 3) {
                    string = null;
                    break;
                }
                if (length2 != i4) {
                    if (length2 == 0) {
                        string = null;
                        break;
                    } else {
                        string = new StringBuffer().append(string.substring(0, length2)).append("\n").append(string.substring(length2 + 1, string.length())).toString();
                        iD -= i2;
                    }
                } else {
                    string = new StringBuffer().append(string).append("\n").toString();
                    iD = 0;
                }
                i3++;
            }
            i4++;
        }
        if (string != null && !b(string, 7)) {
            string = null;
        }
        bE = string;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v143, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r0v146, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r0v149, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r0v156, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r0v159, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r0v167, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v185, types: [javax.microedition.lcdui.Image[], javax.microedition.lcdui.Image[][]] */
    private static void d(int i2) throws Exception {
        U = 0;
        switch (i2) {
            case -1:
                aL = new Image[Q];
                aM = new Image[aq];
                ?? r0 = new Image[3];
                aZ = r0;
                r0[0] = new Image[am.length];
                aZ[1] = new Image[an.length];
                aZ[2] = new Image[ao.length];
                return;
            case 0:
                c();
                return;
            case 1:
                byte[] bArrB = b(i2);
                for (int i3 = 0; i3 < bD; i3++) {
                    U += a(bArrB);
                }
                a(bArrB);
                int iA = a(bArrB);
                ar = new String[iA];
                byte[] bArr = new byte[iA];
                System.arraycopy(bArrB, U, bArr, 0, iA);
                U += iA;
                a(bArrB);
                int i4 = U;
                U = i4 + 1;
                int i5 = bArrB[i4] & 255;
                int i6 = U;
                U = i6 + 1;
                int i7 = i5 | ((bArrB[i6] & 255) << 8);
                as = new short[i7];
                for (int i8 = 0; i8 < i7 - 1; i8++) {
                    int i9 = U;
                    U = i9 + 1;
                    int i10 = bArrB[i9] & 255;
                    int i11 = U;
                    U = i11 + 1;
                    as[i8] = (short) (i10 + ((bArrB[i11] & 255) << 8));
                }
                as[i7 - 1] = (short) iA;
                int i12 = 0;
                while (i12 < i7) {
                    int i13 = i12 == 0 ? 0 : as[i12 - 1] & 65535;
                    int i14 = (as[i12] & 65535) - i13;
                    if (i14 != 0) {
                        try {
                            StringBuffer stringBuffer = new StringBuffer((i14 / 2) + 2);
                            int i15 = i13;
                            while (i15 < i13 + i14) {
                                if ((bArr[i15] & 128) == 0) {
                                    int i16 = i15;
                                    i15++;
                                    stringBuffer.append((char) (bArr[i16] & 255));
                                } else if ((bArr[i15] & 224) == 192) {
                                    if (i15 + 1 >= i13 + i14 || (bArr[i15 + 1] & 192) != 128) {
                                        throw new Exception();
                                    }
                                    int i17 = i15;
                                    int i18 = i15 + 1;
                                    i15 = i18 + 1;
                                    stringBuffer.append((char) (((bArr[i17] & 31) << 6) | (bArr[i18] & 63)));
                                } else {
                                    if ((bArr[i15] & 240) != 224) {
                                        throw new Exception();
                                    }
                                    if (i15 + 2 >= i13 + i14 || (bArr[i15 + 1] & 192) != 128 || (bArr[i15 + 2] & 192) != 128) {
                                        throw new Exception();
                                    }
                                    int i19 = i15;
                                    int i20 = i15 + 1;
                                    int i21 = i20 + 1;
                                    int i22 = ((bArr[i19] & 15) << 12) | ((bArr[i20] & 63) << 6);
                                    i15 = i21 + 1;
                                    stringBuffer.append((char) (i22 | (bArr[i21] & 63)));
                                }
                                ar[i12] = stringBuffer.toString().toUpperCase();
                            }
                        } catch (Exception unused) {
                        }
                    }
                    i12++;
                }
                if (az) {
                    bM = new Command(c(D), 4, 1);
                    bN = new Command(c(E), 2, 1);
                    if (bN != null) {
                        ax.removeCommand(bN);
                    }
                    if (bM != null) {
                        ax.removeCommand(bM);
                    }
                    ax.addCommand(bM);
                    ax.addCommand(bN);
                    return;
                }
                return;
            case 2:
                ab = new Image[3];
                byte[] bArrB2 = b(i2);
                byte[] bArr2 = new byte[bArrB2.length];
                System.arraycopy(bArrB2, 0, bArr2, 0, bArrB2.length);
                int iA2 = a(bArrB2);
                U = 0;
                ab[0] = c(bArrB2);
                ab[1] = a(bArr2, 2, iA2, 1, 46319);
                ab[2] = a(bArr2, 2, iA2, 1, 16711680);
                int iA3 = a(bArrB2);
                V = new byte[(a(bArrB2) + 1) * (4 + ca)];
                int i23 = iA3 / (6 + ca);
                for (int i24 = 0; i24 < i23; i24++) {
                    System.arraycopy(bArrB2, U, V, a(bArrB2) * (4 + ca), 4 + ca);
                    U += 4 + ca;
                }
                byte b2 = V[(32 * (4 + ca)) + aa];
                W = b2;
                m = b2 == 13 ? 0 : -1;
                i();
                return;
            case 3:
                byte[] bArrB3 = b(i2);
                for (int i25 = 0; i25 < Q; i25++) {
                    aL[i25] = c(bArrB3);
                }
                return;
            case 4:
                for (int i26 = 0; i26 < ap; i26++) {
                    aM[i26] = c(b(i2));
                }
                return;
            case 5:
                byte[] bArrB4 = b(i2);
                byte[] bArrB5 = b(bArrB4);
                aM[s] = Image.createImage(bArrB5, 0, bArrB5.length);
                for (int i27 = 0; i27 < bD; i27++) {
                    c(bArrB4);
                }
                aN = c(bArrB4);
                return;
            case 6:
            case 7:
                int length = (i2 == 6 ? am.length : an.length) - 1;
                boolean z2 = i2 != 6;
                byte[] bArrB6 = b(i2);
                int i28 = 0;
                while (i28 < length) {
                    aZ[z2 ? 1 : 0][i28] = c(bArrB6);
                    i28++;
                }
                aZ[z2 ? 1 : 0][i28] = aL[11];
                aM[i2 == 6 ? t : u] = aL[10];
                return;
            case 8:
                byte[] bArrB7 = b(i2);
                aM[v] = c(bArrB7);
                if ((al[bD].equals("JP") || al[bD].equals("DOCOMO")) && aO == null) {
                    aO = c(bArrB7);
                    return;
                }
                return;
            default:
                if (i2 == o[0]) {
                    aM[w] = c(b(i2));
                    return;
                } else {
                    if (i2 == p) {
                        d();
                        return;
                    }
                    return;
                }
        }
    }

    private static void a(boolean z2) {
        for (int i2 = 0; i2 < aZ.length; i2++) {
            if (aZ[i2] != null) {
                for (int i3 = 0; i3 < aZ[i2].length; i3++) {
                    aZ[i2][i3] = null;
                }
            }
        }
        for (int i4 = 0; i4 < aM.length; i4++) {
            aM[i4] = null;
        }
        aN = null;
        aO = null;
        if (z2) {
            d();
            V = null;
            ab = null;
            for (int i5 = 0; i5 < Q; i5++) {
                aL[i5] = null;
            }
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
            for (int i6 = 0; i6 < aX.length; i6++) {
                aX[i6] = null;
            }
        }
        System.gc();
    }

    public static boolean a(int i2) throws Exception {
        if (bZ) {
            return !aF || aG == 4;
        }
        if (!aE) {
            return true;
        }
        if (bP) {
            bP = false;
        } else {
            aH = i2;
        }
        switch (aG) {
            case 0:
                if (aJ >= aK) {
                    k();
                    aG = 1;
                    j();
                } else {
                    d(aJ);
                }
                aJ++;
                break;
            case 1:
                switch (aH) {
                    case 21:
                        if (bx && bi > 0) {
                            int i3 = bi - 1;
                            bi = i3;
                            if (i3 - bh < 0) {
                                bh--;
                                break;
                            }
                        }
                        break;
                    case 23:
                        if (bo > 1) {
                            if (aI == 0) {
                                aI = aq - 1;
                            } else {
                                aI--;
                            }
                            while (!br[aI]) {
                                if (aI == 0) {
                                    aI = aq - 1;
                                } else {
                                    aI--;
                                }
                            }
                            au = true;
                            break;
                        }
                        break;
                    case 25:
                    case 27:
                        aG = 6;
                        break;
                    case 26:
                        aG = 4;
                        break;
                    case 32:
                        if (bx && bi < bf - 1) {
                            int i4 = bi + 1;
                            bi = i4;
                            if (i4 - bh >= bg) {
                                bh++;
                                break;
                            }
                        }
                        break;
                }
                if (bo > 1) {
                    if (!au) {
                        if (aI == aq - 1) {
                            aI = 0;
                        } else {
                            aI++;
                        }
                        while (!br[aI]) {
                            if (aI == aq - 1) {
                                aI = 0;
                            } else {
                                aI++;
                            }
                        }
                        av = true;
                    }
                    bh = 0;
                    bi = 0;
                    j();
                    break;
                }
                break;
            case 2:
                a(false);
                int i5 = aI;
                int i6 = bq[i5] == 4 ? bq[i5] : i5 == s ? 5 : i5 == t ? 6 : i5 == u ? 7 : i5 == v ? 8 : i5 == w ? o[0] : -1;
                aJ = i6;
                d(i6);
                k();
                aG = 1;
                break;
            case 3:
                switch (aH) {
                    case 25:
                        aD = null;
                        break;
                    case 26:
                        aG = 1;
                        ay = null;
                        break;
                }
            case 4:
                a(true);
                if (az) {
                    ax.setCommandListener(aA);
                    if (bN != null) {
                        ax.removeCommand(bN);
                        bN = null;
                    }
                    if (bM != null) {
                        ax.removeCommand(bM);
                        bM = null;
                    }
                }
                aC = false;
                break;
            case 6:
                String string = bp[aI];
                if (bx) {
                    string = ba[be][bi];
                }
                if (string != null && string.length() > 0) {
                    if (aj) {
                        int iIndexOf = string.indexOf("&lg=");
                        string = iIndexOf == -1 ? new StringBuffer().append(string).append("&lg=").append(al[bD]).toString() : new StringBuffer().append(string.substring(0, iIndexOf)).append("&lg=").append(al[bD]).append(string.substring(iIndexOf + "&lg=".length() + 2)).toString();
                    }
                    aD = string;
                    break;
                }
                break;
        }
        return true;
    }

    public static void a(int i2, int i3) {
        int iE = e(i2, i3);
        if (iE != 0) {
            aH = iE;
            bP = true;
        }
        bO = 0;
        bQ = false;
    }

    public static void b(int i2, int i3) {
        int iE = e(i2, i3);
        if (iE != 0) {
            bO = iE;
        }
    }

    public static void c(int i2, int i3) {
        aH = 0;
        bO = 0;
        bQ = false;
        b(i2, i3);
    }

    private static int e(int i2, int i3) {
        if (i2 < 1 || i3 < 1 || aG == 0 || !aC) {
            return 0;
        }
        if (bx) {
            int width = ((bJ * 5) / 100) + 1 + aL[5].getWidth();
            int i4 = bI / 100;
            int width2 = width + aZ[be][0].getWidth() + i4;
            int i5 = i4 * 3;
            boolean z2 = i2 > bk + i5 && i2 < (bk + bm) - i5;
            for (int i6 = bh; i6 < bh + bg; i6++) {
                if (z2 && i3 > bV + (bn * (i6 - bh)) && i3 < bV + (bn * (i6 - bh)) + bn) {
                    if (bi == i6) {
                        return 25;
                    }
                    bi = i6;
                    return 0;
                }
            }
            if (z2) {
                z2 = i2 > ((aL[12].getWidth() * 3) / 2) + 2 && i2 < bI - (((aL[12].getWidth() * 3) / 2) - 2);
            }
            if (bg < bf && z2) {
                if (bi > 0 && i3 > bW - ((width2 - 2) / 2) && i3 < bW + (width2 - 2)) {
                    return 21;
                }
                if (bi < bf - 1 && i3 > bX - ((width2 - 2) / 2) && i3 < bX + (width2 - 2)) {
                    return 32;
                }
            }
        } else if (i2 > bB && i2 < bB + bz && i3 > bC && i3 < bC + bA) {
            return 27;
        }
        int width3 = bI - aL[7].getWidth();
        int height = (bJ >> 1) - aL[7].getHeight();
        int height2 = aL[7].getHeight() << 1;
        int width4 = aL[7].getWidth() << 1;
        if (i3 > height && i3 < height + height2) {
            if (i2 > 0 && i2 < width4 + 0) {
                return 23;
            }
            if (i2 > width3 - (width4 / 2) && i2 < width3 + width4) {
                return 24;
            }
        }
        if (az || i3 < bJ - (aL[13].getHeight() << 1) || i3 > bJ) {
            return 0;
        }
        if (i2 <= h || i2 >= (aL[13].getWidth() << 1) + h) {
            return (i2 <= (bI - i) - (aL[13].getWidth() << 1) || i2 >= bI - i) ? 0 : 26;
        }
        bQ = true;
        return 25;
    }

    private static void j() {
        bt = aI;
        bi = 0;
        bf = 0;
        bh = 0;
        bx = false;
        bu = (bp[aI] == null || bp[aI].length() <= 0 || bp[aI].compareTo("DEL") == 0) ? false : true;
        if (aI == t) {
            be = 0;
            bx = true;
            bu = false;
        }
        if (aI == u) {
            be = 1;
            bx = true;
            bu = false;
        }
        if (aI == v || aI == w) {
            bu = true;
        }
        by = F;
        if (bq[aI] == 4) {
            by = G;
        }
        if (bx) {
            bf = bd[be];
        }
    }

    public static void a(Graphics graphics) {
        if ((aE || bZ) && !bZ) {
            a(graphics, 0, 0, bI, bJ);
            a(graphics, 0, 0, bI, bJ);
            switch (aG) {
                case 0:
                    graphics.setColor(0);
                    graphics.fillRect(0, 0, bI, bJ);
                    int i2 = bL;
                    int i3 = (bI * 3) / 4;
                    int i4 = aJ;
                    int i5 = aK;
                    int i6 = i4;
                    if (i6 > i5) {
                        i6 = i5;
                    }
                    int i7 = (bI - i3) / 2;
                    graphics.setColor(16777215);
                    graphics.drawRect(i7, i2, i3, 6);
                    graphics.setColor(16711680);
                    graphics.fillRect(i7 + 1 + 1, i2 + 1 + 1, ((((i3 - 2) - 2) * i6) / i5) + 1, 3);
                    if (bF != null && !bF.trim().equals("")) {
                        graphics.setColor(16777215);
                        graphics.setFont(f);
                        graphics.drawString(bF, bK, bL - 5, 33);
                        break;
                    }
                    break;
                case 1:
                    b(graphics);
                    if ((System.currentTimeMillis() % 1000 > 500 || (bO == 27 && !bQ)) && bu) {
                        graphics.setColor(g);
                        if (n[0]) {
                            graphics.setColor(16744192);
                        }
                        if (!bQ && bO == 27) {
                            graphics.setColor(14588928);
                            graphics.drawRect(bB - 2, bC - 2, bz + 3, bA + 4);
                            graphics.drawRect(bB - 1, bC - 1, bz + 1, bA + 2);
                            graphics.setColor(5767410);
                        }
                        ac = 0;
                        graphics.fillRect(bB, bC, bz, bA + 1);
                        a(by, graphics, bK, bC + (bA >> 1), 3);
                        break;
                    }
                    break;
                case 2:
                    int height = f.getHeight();
                    graphics.setColor(255);
                    graphics.fillRect(0, (bL - height) - 5, bI, height << 1);
                    int i8 = ac;
                    ac = 0;
                    a(P, graphics, bK, bL - height, 65);
                    ac = i8;
                    break;
                case 3:
                    b(graphics);
                    int i9 = (bJ * 40) / 100;
                    int i10 = bI;
                    int i11 = bJ - (i9 << 1);
                    int[] iArr = new int[i10 * i11];
                    for (int i12 = 0; i12 < iArr.length; i12++) {
                        iArr[i12] = -220209185;
                    }
                    graphics.drawRGB(iArr, 0, i10, 0, i9, i10, i11, true);
                    graphics.setColor(39423);
                    graphics.drawRect(0, i9, i10 - 1, (bJ - (i9 << 1)) - 1);
                    graphics.drawRect(1, i9 + 1, i10 - 3, (bJ - (i9 << 1)) - 3);
                    a(J, graphics, bK, bL, 3);
                    c(graphics);
                    break;
            }
        }
    }

    private static void b(Graphics graphics) {
        int i2;
        int i3;
        ag = true;
        a(by, (Graphics) null, 0, 0, 3);
        int iD = d(32, Z) / 2;
        int iD2 = d(32, aa) / 3;
        bz = af + iD;
        bA = ae + iD2;
        if ((ae + bA) % 2 != 0) {
            bA++;
        }
        bB = bK - (bz / 2);
        if (bq[aI] == 4 || aI == w) {
            bC = k - (bA / 2);
        } else if (aI == s || aI == v) {
            bC = l - (bA / 2);
        }
        if (n[0] && aI == w) {
            bC = (bJ * 80) / 100;
        }
        graphics.setColor(16777215);
        if (n[0]) {
            graphics.setColor(0);
        }
        graphics.fillRect(0, 0, bI, bJ);
        if (bx) {
            int height = j;
            if (bJ > 128) {
                a(graphics, aM[aI], bK, j, 17);
                height += aM[aI].getHeight();
            }
            int i4 = height;
            ac = 2;
            a(bt, graphics, bK, i4, 17);
            int iMax = Math.max(2 * W, aZ[be][bb[be][0]].getHeight());
            ag = true;
            a(bt, (Graphics) null, 0, 0, 0);
            int i5 = i4 + ae;
            int i6 = (bJ * 7) / 100;
            int i7 = ((bJ / 2) - i5) << 1;
            int height2 = ((bJ - aL[12].getHeight()) - 2) - i5;
            bg = bf;
            if (i7 / iMax >= bf) {
                i3 = (i5 + (i7 / 2)) - ((bg * iMax) / 2);
            } else {
                if (height2 / iMax < bf) {
                    bg = (height2 - (i6 * 2)) / iMax;
                }
                i3 = (i5 + (height2 / 2)) - ((bg * iMax) / 2);
            }
            int i8 = i3;
            if (bg < bf) {
                if (bi > 0) {
                    a(graphics, bK, i8 - i6, i6, bO == 21 ? 46319 : 16777215, true, false);
                    bW = i8 - i6;
                }
                if (bi != bf - 1) {
                    a(graphics, bK, i8 + (bg * iMax) + i6, i6, bO == 32 ? 46319 : 16777215, true, true);
                    bX = i8 + (bg * iMax);
                }
            }
            int i9 = i3 + (iMax / 2) + 1;
            bm = bI - (3 * aL[5].getWidth());
            if (bI > bJ) {
                bm -= aL[5].getWidth() / 2;
            }
            bk = (bI >> 1) - (bm >> 1);
            int i10 = bI / 100;
            int i11 = bk + i10;
            int width = i11 + aZ[be][0].getWidth() + i10;
            for (int i12 = bh; i12 < bh + bg; i12++) {
                int i13 = bb[be][i12];
                a(graphics, aZ[be][i13], i11, i9, 6);
                if (be == 2 || i13 == bc[be].length - 1) {
                    ac = 2;
                }
                a(c(bc[be][i13]), graphics, width, i9, 6);
                i9 += iMax;
            }
            bj = bi - bh;
            bl = i3 + (iMax * bj);
            bn = iMax;
            bV = i3;
            graphics.setColor(16545540);
            graphics.drawRect(bk, bl, bm, bn);
        } else if (aI == w) {
            a(graphics, aM[w], bI / 2, (bJ << 3) / 100, 17);
            ag = true;
            a(x, (Graphics) null, 0, 0, 0);
            Math.abs(((bC - aM[aI].getHeight()) - ae) / 3);
            if (n[0]) {
                ac = 0;
            }
            a(c(y), graphics, bI - ((aL[4].getWidth() + 8) << 1), bI / 2, (bJ * 60) / 100, 3);
        } else if (aI == s) {
            boolean z2 = bJ >= 128;
            boolean z3 = bE != null && bE.length() > 0;
            int height3 = j + (((z2 ? aL[10].getHeight() : 0) * 3) / 5);
            if (aM[s].getWidth() > 220) {
                graphics.setColor(13421772);
                graphics.fillRect(bK + 20, height3, 3, 54);
                height3 += 54;
            } else if (aM[s].getWidth() > 140) {
                graphics.setColor(14145495);
                graphics.fillRect(bK + 14, height3, 2, 37);
                height3 += 37;
            } else if (aM[s].getWidth() > 100) {
                graphics.setColor(14465464);
                graphics.fillRect(bK + 11, height3, 1, 26);
                height3 += 26;
            }
            int height4 = height3 + aM[s].getHeight();
            a(graphics, aM[s], bK, height4, 33);
            a(graphics, aN, bK + (aM[s].getWidth() >> 1) + 1, height4, 40);
            if (z2) {
                a(graphics, aL[10], bK, j, 17);
            }
            if (z3) {
                ac = 2;
                int i14 = bC - height4;
                int i15 = 3;
                if ((W << 1) > i14) {
                    i2 = height4 + (W / 3);
                    i15 = 17;
                } else {
                    i2 = height4 + (i14 / 2);
                }
                a(bE, graphics, bI, bK, i2, i15);
            }
        } else if (aI == v) {
            boolean z4 = bJ >= 128;
            int width2 = (aM[aI].getWidth() * 70) / 100;
            int height5 = aM[aI].getHeight() >> 1;
            int i16 = (bI >> 1) - (width2 >> 1);
            int i17 = (bJ >> 1) - (height5 >> 1);
            int i18 = height5;
            int i19 = width2;
            if (i17 + i18 > bJ) {
                i18 = bJ - i17;
            }
            if (i16 + i19 > bI) {
                i19 = bI - i16;
            }
            for (int i20 = i17; i20 < i17 + i18; i20++) {
                graphics.setColor(250 + (((i20 - i17) * (-9)) / i18), 209 + (((i20 - i17) * (-139)) / i18), 45 + (((i20 - i17) * (-24)) / i18));
                graphics.drawLine(i16, i20, i16 + i19, i20);
            }
            int i21 = bJ / 2;
            a(graphics, aM[aI], bK, i21, 3);
            if (z4) {
                a(graphics, aL[10], bK, j, 17);
            }
            if (aO == null) {
                ag = true;
                a(c(bt), null, 0, 0, 0, 0);
                int i22 = i21 - (ae / 2);
                int i23 = ac;
                ac = 0;
                a(c(bt), graphics, aM[aI].getWidth() / 2, bK, i22, 17);
                ac = i23;
            } else {
                a(graphics, aO, bK, bJ / 2, 3);
            }
        } else {
            ag = true;
            a(c(bt), graphics, 0, 0, 0, 0);
            int height6 = aM[aI].getHeight();
            boolean z5 = false;
            int height7 = af > (bI - 4) - (aL[12].getWidth() << 1) ? aL[12].getHeight() : 0;
            int iMax2 = Math.max(0, bJ - ((height6 + ae) + height7));
            int i24 = bI <= 176 ? 2 : 0;
            boolean z6 = z5;
            if (ae > (W << 1) + i24) {
                z6 = z5;
                if (iMax2 / 3 > (W + i24) / 2) {
                    iMax2 = Math.max(0, bJ - ((height6 + (W << 1)) + height7));
                    z6 = true;
                }
            }
            int i25 = iMax2 / 3;
            boolean z7 = z6;
            if (height7 > 0) {
                int height8 = bJ - aL[12].getHeight();
                int iMax3 = Math.max(0, bJ - (height6 + ae));
                boolean z8 = z6;
                if (ae > (W << 1) + i24) {
                    z8 = z6;
                    if (iMax3 / 3 > ae - ((W + i24) << 1)) {
                        iMax3 = Math.max(0, bJ - (height6 + (W << 1)));
                        z8 = true;
                    }
                }
                int i26 = iMax3 / 3;
                z7 = z8;
                if ((i26 * 2) + ae + aM[aI].getHeight() < height8) {
                    i25 = i26;
                    z7 = z8;
                }
            }
            int i27 = i25;
            a(graphics, aM[aI], bK, i27, 17);
            int height9 = i27 + i25 + aM[aI].getHeight();
            if (n[0]) {
                ac = 0;
            }
            a(c(bt), graphics, 0, bK, height9 + (z7 ? -(W / 2) : 0), 17);
        }
        c(graphics);
        if (bo > 1) {
            int iAbs = Math.abs(((int) ((System.currentTimeMillis() / 80) % 8)) - 4);
            boolean z9 = 5;
            boolean z10 = 7;
            if (au || bO == 23) {
                z9 = 4;
                at++;
            }
            if (av || bO == 24) {
                z10 = 6;
                at++;
            }
            int width3 = iAbs + 1 + aL[z9 ? 1 : 0].getWidth();
            aL[z9 ? 1 : 0].getWidth();
            a(graphics, aL[z9 ? 1 : 0], width3, bL, 10);
            a(graphics, aL[z10 ? 1 : 0], bI - width3, bL, 6);
            if (at > 4) {
                au = false;
                av = false;
                at = 0;
            }
        }
    }

    private static void a(Graphics graphics, int i2, int i3, int i4, int i5, boolean z2, boolean z3) {
        int i6 = z3 ? -1 : 1;
        if (i4 % 2 == 0) {
            i4--;
        }
        graphics.setColor(46319);
        a(graphics, i2, i3, i2 - (i4 >> 1), i3 + (i6 * (i4 >> 1)), i2 + (i4 >> 1), i3 + (i6 * (i4 >> 1)));
        graphics.setColor(i5);
        a(graphics, i2, i3 + i6, (i2 - (i4 >> 1)) + 2, (i3 + (i6 * (i4 >> 1))) - i6, (i2 + (i4 >> 1)) - 2, (i3 + (i6 * (i4 >> 1))) - i6);
    }

    private static void c(Graphics graphics) {
        int i2 = bJ - 2;
        int i3 = h;
        int i4 = bI - i;
        if (az) {
            return;
        }
        Image image = aL[12];
        Image image2 = aL[12];
        if (bO == 26) {
            image2 = aL[13];
        } else if (bO == 25 && bQ) {
            image = aL[13];
        }
        bw = (byte) (bw | 1);
        bv = (byte) (bv | 1);
        bw = (byte) (bw | 2);
        bv = (byte) (bv | 2);
        if ((bw & 1) != 0) {
            a(graphics, image, i3, i2, 36);
        }
        if ((bw & 2) != 0) {
            a(graphics, image2, i4, i2, 40);
        }
        int width = i3 + ((aL[12].getWidth() / 2) - (aL[9].getWidth() / 2));
        int width2 = i4 + ((-aL[12].getWidth()) / 2) + (aL[8].getWidth() / 2);
        int height = i2 + ((-aL[12].getHeight()) / 2) + (aL[9].getHeight() / 2);
        byte b2 = (byte) (bv | 1);
        bv = b2;
        byte b3 = (byte) (b2 | 2);
        bv = b3;
        if ((b3 & 1) != 0) {
            a(graphics, aL[9], width, height, 36);
        }
        if ((bv & 2) != 0) {
            a(graphics, aL[8], width2, height, 40);
        }
    }

    /* JADX DEBUG: Move duplicate insns, count: 1 to block B:27:0x00fa */
    private static Image a(byte[] bArr, int i2, int i3, int i4, int i5) {
        int i6 = 0;
        for (int i7 = 2; i6 == 0 && i7 < i3 + 2; i7++) {
            if ((bArr[i7] & 255) == 80 && (bArr[i7 + 1] & 255) == 76 && (bArr[i7 + 2] & 255) == 84 && (bArr[i7 + 3] & 255) == 69) {
                i6 = i7;
            }
        }
        int i8 = ((bArr[i6 - 4] << 24) & (-16777216)) + ((bArr[i6 - 3] << 16) & 16711680) + ((bArr[i6 - 2] << 8) & 65280) + (bArr[i6 - 1] & 255);
        bArr[i6 + 4 + 3] = (byte) ((i5 >> 16) & 255);
        bArr[i6 + 4 + 3 + 1] = (byte) ((i5 >> 8) & 255);
        bArr[i6 + 4 + 3 + 2] = (byte) i5;
        byte[] bArr2 = new byte[i8 + 4];
        System.arraycopy(bArr, i6, bArr2, 0, i8 + 4);
        long[] jArr = new long[256];
        for (int i9 = 0; i9 < 256; i9++) {
            long j2 = i9;
            for (int i10 = 0; i10 < 8; i10++) {
                j2 = (j2 & 1) == 1 ? 3988292384L ^ (j2 >> 1) : j2 >> 1;
            }
            jArr[i9] = j2;
        }
        long j3 = 4294967295L;
        for (byte b2 : bArr2) {
            j3 = jArr[((int) (j3 ^ b2)) & 255] ^ (j3 >> 8);
        }
        long j4 = j3 ^ 4294967295L;
        bArr[i6 + 4 + i8] = (byte) ((j4 & (-16777216)) >> 24);
        bArr[i6 + 4 + i8 + 1] = (byte) ((j4 & 16711680) >> 16);
        bArr[i6 + 4 + i8 + 2] = (byte) ((j4 & 65280) >> 8);
        bArr[i6 + 4 + i8 + 3] = (byte) (j4 & 255);
        System.gc();
        return Image.createImage(bArr, 2, i3);
    }

    private static void a(Graphics graphics, int i2, int i3, int i4, int i5, int i6, int i7) {
        graphics.fillTriangle(i2, i3, i4, i5, i6, i7);
    }

    private static void a(Graphics graphics, Image image, int i2, int i3, int i4) {
        graphics.drawImage(image, i2, i3, i4);
    }

    private static void a(Graphics graphics, int i2, int i3, int i4, int i5) {
        graphics.setClip(Math.max(i2, 0), Math.max(i3, 0), Math.min(i4, bI), Math.min(i5, bJ));
    }

    @Override // java.lang.Runnable
    public final void run() throws InterruptedException {
        while (aC) {
            try {
                if (aD != null) {
                    String str = aD;
                    e = str;
                    if (str != null && e.length() > 0) {
                        String str2 = e;
                        e = null;
                        new StringBuffer().append("urlPlatformRequest = ").append(str2).toString();
                        try {
                            aw.platformRequest(str2);
                            Thread.sleep(200L);
                        } catch (Exception unused) {
                        }
                        if (bZ) {
                            aG = 4;
                        } else {
                            aG = 1;
                        }
                    }
                    aD = null;
                }
                Thread.sleep(1000L);
            } catch (Exception unused2) {
            }
        }
    }

    public final void commandAction(Command command, Displayable displayable) throws Exception {
        if (az) {
            if (command == bM) {
                a(25);
            } else if (command == bN) {
                a(26);
            }
        }
    }

    private static InputStream a(String str) {
        return "a".getClass().getResourceAsStream(str);
    }

    /* JADX WARN: Removed duplicated region for block: B:59:0x016f A[Catch: Exception -> 0x0342, TryCatch #1 {Exception -> 0x0342, blocks: (B:9:0x005c, B:11:0x0064, B:13:0x006c, B:15:0x0073, B:17:0x007a, B:19:0x0081, B:21:0x0088, B:23:0x008f, B:25:0x0096, B:27:0x00a2, B:29:0x00b0, B:31:0x00b7, B:56:0x013c, B:57:0x0166, B:59:0x016f, B:61:0x0179, B:63:0x0183, B:65:0x018d, B:67:0x0197, B:70:0x01d3, B:72:0x01dd, B:77:0x0232, B:73:0x01f6, B:75:0x0200, B:76:0x0219, B:69:0x01a1, B:78:0x0238, B:80:0x0247, B:82:0x0252, B:84:0x025d, B:86:0x0266, B:87:0x029b, B:88:0x02a8, B:92:0x02b5, B:94:0x02c6, B:95:0x02dc, B:97:0x0311, B:99:0x0322, B:100:0x0325, B:101:0x032b, B:33:0x00c3, B:35:0x00cb, B:37:0x00d2, B:39:0x00de, B:42:0x00e9, B:44:0x00f0, B:46:0x00f7, B:49:0x0116, B:51:0x011f, B:53:0x0129, B:54:0x0138, B:48:0x00fe, B:102:0x033b), top: B:108:0x005c }] */
    /* JADX WARN: Removed duplicated region for block: B:90:0x02b0  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private static void k() {
        int width;
        String str;
        String string;
        int i2;
        short[] sArrA;
        int i3;
        int width2 = bI;
        try {
            int width3 = bI - (3 * aL[5].getWidth());
            if (bI > bJ) {
                width3 -= aL[5].getWidth() / 2;
            }
            int i4 = (bI >> 1) - (width3 >> 1);
            int i5 = bI / 100;
            width2 = (i4 + width3) - (((i4 + i5) + aZ[be][0].getWidth()) + i5);
        } catch (Exception unused) {
        }
        for (int i6 = 0; i6 < ar.length; i6++) {
            try {
                if (ar[i6] != null && (i6 == z || i6 == B || i6 == F || i6 == y || i6 < x || (i6 >= K && i6 < M + an.length))) {
                    int i7 = bI;
                    int i8 = 3;
                    if ((am.length > 1 && i6 >= K && i6 < K + am.length) || ((an.length > 1 && i6 >= M && i6 < M + an.length) || i6 == y)) {
                        width = width2;
                    } else if (i6 == z || i6 == B || i6 == F) {
                        i8 = 2;
                        width = (bI - (aL[12].getWidth() << 1)) - 4;
                    } else {
                        if (i6 == x - 1) {
                            width = aM[v] != null ? aM[v].getWidth() >> 1 : bI;
                        }
                        String[] strArr = ar;
                        int i9 = i6;
                        str = ar[i6];
                        int i10 = i7;
                        int i11 = i8;
                        String string2 = "";
                        int i12 = 0;
                        int i13 = 0;
                        string = "";
                        char[] charArray = str.toCharArray();
                        for (i2 = 0; i2 < str.length(); i2++) {
                            string = (charArray[i2] == 65292 || charArray[i2] == 65281 || charArray[i2] == 65311 || charArray[i2] == 65307 || charArray[i2] == 65306) ? new StringBuffer().append(new StringBuffer().append(string).append(charArray[i2]).toString()).append('|').toString() : charArray[i2] == '\b' ? new StringBuffer().append(string).append('|').toString() : charArray[i2] == '\n' ? new StringBuffer().append(string).append(' ').toString() : new StringBuffer().append(string).append(charArray[i2]).toString();
                        }
                        if (al[bD].equals("ZH-TW") && string.indexOf(124) == -1 && string.indexOf(32) == -1 && string.length() > 5) {
                            string = new StringBuffer().append(string.substring(0, string.length() >> 1)).append("|").append(string.substring(string.length() >> 1, string.length())).toString();
                        }
                        String str2 = string;
                        sArrA = a(str2, i10, true);
                        for (i3 = 0; i3 < sArrA[0]; i3++) {
                            if (i12 != 0) {
                                i13++;
                                string2 = string2.trim();
                                if (i13 >= i11) {
                                    break;
                                } else {
                                    string2 = new StringBuffer().append(string2).append("\n").toString();
                                }
                            }
                            string2 = new StringBuffer().append(string2).append(str2.substring(i12, sArrA[(i3 << 1) + 1])).toString();
                            i12 = sArrA[(i3 << 1) + 1];
                            if (str2.length() != i12 && str2.charAt(sArrA[(i3 << 1) + 1]) == '\n') {
                                i12++;
                            }
                        }
                        strArr[i9] = string2;
                        ar[i6] = b(ar[i6]);
                    }
                    i7 = width;
                    String[] strArr2 = ar;
                    int i92 = i6;
                    str = ar[i6];
                    int i102 = i7;
                    int i112 = i8;
                    String string22 = "";
                    int i122 = 0;
                    int i132 = 0;
                    string = "";
                    char[] charArray2 = str.toCharArray();
                    while (i2 < str.length()) {
                    }
                    if (al[bD].equals("ZH-TW")) {
                        string = new StringBuffer().append(string.substring(0, string.length() >> 1)).append("|").append(string.substring(string.length() >> 1, string.length())).toString();
                    }
                    String str22 = string;
                    sArrA = a(str22, i102, true);
                    while (i3 < sArrA[0]) {
                    }
                    strArr2[i92] = string22;
                    ar[i6] = b(ar[i6]);
                }
            } catch (Exception unused2) {
                return;
            }
        }
    }

    private static short[] a(String str, int i2, boolean z2) {
        int length = str.length();
        short sD = 0;
        short s2 = 1;
        short s3 = 0;
        boolean z3 = false;
        short s4 = 0;
        int i3 = 0;
        while (i3 < length) {
            char cCharAt = str.charAt(i3);
            if (cCharAt == ' ') {
                sD = (short) (sD + d(cCharAt, Z));
                short s5 = s3;
                s3 = (short) i3;
                z3 = true;
                s4 = 0;
                if (s3 == length - 2 && sD + d(str.charAt(s3 + 1), Z) > i2) {
                    s3 = s5;
                    short s6 = (short) (s2 + 1);
                    cb[s2] = (short) (s3 + 1);
                    s2 = (short) (s6 + 1);
                    cb[s6] = sD;
                    sD = 0;
                }
                if (sD > i2) {
                    z3 = false;
                    int i4 = s3;
                    while (i4 >= 0 && str.charAt(i4) == ' ') {
                        i4--;
                        sD = (short) (sD - d(32, Z));
                    }
                    int i5 = 0;
                    while (s3 < length && str.charAt(s3) == ' ') {
                        s3 = (short) (s3 + 1);
                        i5++;
                    }
                    if (i5 == 1 && i3 == length - 2) {
                        s3 = s5;
                    } else {
                        short s7 = (short) (s3 - 1);
                        s3 = s7;
                        i3 = s7;
                    }
                    short s8 = (short) (s2 + 1);
                    cb[s2] = (short) (s3 + 1);
                    s2 = (short) (s8 + 1);
                    cb[s8] = sD;
                    sD = 0;
                }
            } else if (cCharAt == '|') {
                if (!z2) {
                    break;
                }
                sD = (short) (sD + d(cCharAt, Z));
                s3 = (short) i3;
                z3 = true;
                s4 = 0;
                if (sD > i2) {
                    z3 = false;
                    int i6 = s3;
                    while (i6 >= 0 && str.charAt(i6) == ' ') {
                        i6--;
                        sD = (short) (sD - d(32, Z));
                    }
                    while (s3 < length && str.charAt(s3) == ' ') {
                        s3 = (short) (s3 + 1);
                    }
                    short s9 = (short) (s3 - 1);
                    s3 = s9;
                    i3 = s9;
                    short s10 = (short) (s2 + 1);
                    cb[s2] = (short) (s3 + 1);
                    s2 = (short) (s10 + 1);
                    cb[s10] = sD;
                    sD = 0;
                }
            } else if (cCharAt == '\n') {
                short s11 = (short) (s2 + 1);
                cb[s2] = (short) i3;
                s2 = (short) (s11 + 1);
                cb[s11] = sD;
                sD = 0;
                s4 = 0;
            } else {
                int iD = d(cCharAt, Z) + m;
                s4 = (short) (s4 + iD);
                short s12 = (short) (sD + iD);
                sD = s12;
                if (s12 > i2 && z3) {
                    z3 = false;
                    int i7 = s3;
                    while (i7 >= 0 && str.charAt(i7) == ' ') {
                        i7--;
                        sD = (short) (sD - d(32, aa));
                    }
                    short s13 = (short) (s2 + 1);
                    cb[s2] = (short) (s3 + 1);
                    s2 = (short) (s13 + 1);
                    cb[s13] = (short) (sD - s4);
                    sD = 0;
                    i3 = s3;
                }
            }
            i3++;
        }
        if (sD != 0) {
            short s14 = (short) (s2 + 1);
            cb[s2] = (short) length;
            s2 = (short) (s14 + 1);
            cb[s14] = sD;
        }
        cb[0] = (short) (s2 / 2);
        return cb;
    }

    private static String b(String str) {
        String string = "";
        char[] charArray = str.toCharArray();
        for (int i2 = 0; i2 < str.length(); i2++) {
            if (charArray[i2] != '|') {
                string = new StringBuffer().append(string).append(charArray[i2]).toString();
            }
        }
        return string;
    }
}
