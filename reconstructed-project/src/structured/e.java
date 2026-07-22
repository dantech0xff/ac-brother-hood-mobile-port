package defpackage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:e.class */
public final class e {
    private static long d;
    private static ByteArrayInputStream[] a = new ByteArrayInputStream[34];
    private static Player b = null;
    private static String[] c = new String[34];
    private static int e = -1;

    static void a(String str) throws IOException {
        j.a(str);
        for (int i = 0; i < 34; i++) {
            byte[] bArrE = j.e(i);
            if (bArrE.length != 0) {
                if (bArrE[0] == 82) {
                    c[i] = "audio/x-wav";
                } else if (bArrE[0] == 77) {
                    c[i] = "audio/midi";
                }
                a[i] = new ByteArrayInputStream(bArrE);
            }
        }
        j.e();
    }

    static boolean a() {
        return e != -1 && System.currentTimeMillis() - d < ((long) h.a[e]);
    }

    static void a(int i, boolean z) {
        try {
            if (a[i] == null) {
                return;
            }
            if (k.bE || i >= 10) {
                if (k.bF || i < 10) {
                    if (k.bF && k.bE && e != -1 && a() && (i >= 10 || e < 10 || a())) {
                        return;
                    }
                    b();
                    a[i].reset();
                    d = System.currentTimeMillis();
                    Player playerCreatePlayer = Manager.createPlayer(a[i], c[i]);
                    b = playerCreatePlayer;
                    playerCreatePlayer.setLoopCount(1);
                    b.start();
                    e = i;
                }
            }
        } catch (Exception unused) {
        }
    }

    static void b() {
        try {
            if (b != null) {
                e = -1;
                b.stop();
                b.close();
                b = null;
            }
        } catch (Exception unused) {
            b = null;
        }
    }
}
