package defpackage;

import java.io.ByteArrayInputStream;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:e.class */
public final class e {
    private static ByteArrayInputStream[] a;
    private static Player b;
    private static String[] c;
    private static long d;
    private static int e;

    public e() {
    }

    static void a(String r6) {
        j.a(r6);
        int r62 = 0;
    L4:
        if (r62 >= 34) goto L15;
        byte[] r0 = j.e(r62);
        if (r0.length == 0) goto L14;
        if (r0[0] != 82) goto L11;
        c[r62] = "audio/x-wav";
    L13:
        a[r62] = new ByteArrayInputStream(r0);
        goto L14
    L11:
        if (r0[0] != 77) goto L13;
        c[r62] = "audio/midi";
    L14:
        r62 = r62 + 1;
        goto L4
    L15:
        j.e();
    }

    static boolean a() {
        if (e != (-1)) goto L7;
        return false;
    L7:
        if ((System.currentTimeMillis() - d) >= h.a[e]) goto L10;
        return true;
    L10:
        return false;
    }

    static void a(int r4, boolean r5) {
        if (a[r4] != null) goto L6;
        return;
    L6:
        if (k.bE == true) goto L11;
        if (r4 >= 10) goto L11;
        return;
    L11:
        if (k.bF == true) goto L16;
        if (r4 < 10) goto L16;
        return;
    L16:
        if (k.bF == true) goto L18;
    L31:
        b();     // Catch: Exception -> L33
        a[r4].reset();     // Catch: Exception -> L33
        d = System.currentTimeMillis();     // Catch: Exception -> L33
        Player r0 = Manager.createPlayer(a[r4], c[r4]);     // Catch: Exception -> L33
        b = r0;     // Catch: Exception -> L33
        r0.setLoopCount(1);     // Catch: Exception -> L33
        b.start();     // Catch: Exception -> L33
        e = r4;     // Catch: Exception -> L33
        return;
    L18:
        if (k.bE == false) goto L31;
        if (e == (-1)) goto L31;
        if (a() == false) goto L31;
        if (r4 < 10) goto L26;
        return;
    L26:
        if (e >= 10) goto L29;
        return;
    L29:
        if (a() == false) goto L31;
        return;
    }

    static void b() {
    L6:
        b = null;
        return;
    L3:
        if (b == null) goto L10;
        e = -1;     // Catch: Exception -> L6
        b.stop();     // Catch: Exception -> L6
        b.close();     // Catch: Exception -> L6
        b = null;     // Catch: Exception -> L6
        return;
    }

    static {
        a = new ByteArrayInputStream[34];
        b = null;
        c = new String[34];
        e = -1;
    }
}
