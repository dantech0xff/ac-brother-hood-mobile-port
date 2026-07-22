package defpackage;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:e.class */
public final class e {
    private static java.io.ByteArrayInputStream[] a;
    private static javax.microedition.media.Player b;
    private static java.lang.String[] c;
    private static long d;
    private static int e;

    public e() {
            r2 = this;
            r0 = r2
            r0.<init>()
            return
    }

    static void a(java.lang.String r6) {
            r0 = r6
            defpackage.j.a(r0)
            r0 = 0
            r6 = r0
        L6:
            r0 = r6
            r1 = 34
            if (r0 >= r1) goto L4a
            r0 = r6
            byte[] r0 = defpackage.j.e(r0)
            r1 = r0
            r7 = r1
            int r0 = r0.length
            if (r0 == 0) goto L44
            r0 = r7
            r1 = 0
            r0 = r0[r1]
            r1 = 82
            if (r0 != r1) goto L28
            java.lang.String[] r0 = defpackage.e.c
            r1 = r6
            java.lang.String r2 = "audio/x-wav"
            r0[r1] = r2
            goto L37
        L28:
            r0 = r7
            r1 = 0
            r0 = r0[r1]
            r1 = 77
            if (r0 != r1) goto L37
            java.lang.String[] r0 = defpackage.e.c
            r1 = r6
            java.lang.String r2 = "audio/midi"
            r0[r1] = r2
        L37:
            java.io.ByteArrayInputStream[] r0 = defpackage.e.a
            r1 = r6
            java.io.ByteArrayInputStream r2 = new java.io.ByteArrayInputStream
            r3 = r2
            r4 = r7
            r3.<init>(r4)
            r0[r1] = r2
        L44:
            int r6 = r6 + 1
            goto L6
        L4a:
            defpackage.j.e()
            return
    }

    static boolean a() {
            int r0 = defpackage.e.e
            r1 = -1
            if (r0 != r1) goto L9
            r0 = 0
            return r0
        L9:
            long r0 = java.lang.System.currentTimeMillis()
            r1 = r0; r0 = r0; 
            r5 = r1
            long r1 = defpackage.e.d
            long r0 = r0 - r1
            int[] r1 = defpackage.h.a
            int r2 = defpackage.e.e
            r1 = r1[r2]
            long r1 = (long) r1
            int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r0 >= 0) goto L20
            r0 = 1
            return r0
        L20:
            r0 = 0
            return r0
    }

    static void a(int r4, boolean r5) {
            java.io.ByteArrayInputStream[] r0 = defpackage.e.a     // Catch: java.lang.Exception -> L87
            r1 = r4
            r0 = r0[r1]     // Catch: java.lang.Exception -> L87
            if (r0 != 0) goto L9
            return
        L9:
            boolean r0 = defpackage.k.bE     // Catch: java.lang.Exception -> L87
            if (r0 != 0) goto L16
            r0 = r4
            r1 = 10
            if (r0 >= r1) goto L16
            return
        L16:
            boolean r0 = defpackage.k.bF     // Catch: java.lang.Exception -> L87
            if (r0 != 0) goto L23
            r0 = r4
            r1 = 10
            if (r0 < r1) goto L23
            return
        L23:
            boolean r0 = defpackage.k.bF     // Catch: java.lang.Exception -> L87
            if (r0 == 0) goto L52
            boolean r0 = defpackage.k.bE     // Catch: java.lang.Exception -> L87
            if (r0 == 0) goto L52
            int r0 = defpackage.e.e     // Catch: java.lang.Exception -> L87
            r1 = -1
            if (r0 == r1) goto L52
            boolean r0 = a()     // Catch: java.lang.Exception -> L87
            if (r0 == 0) goto L52
            r0 = r4
            r1 = 10
            if (r0 >= r1) goto L4a
            int r0 = defpackage.e.e     // Catch: java.lang.Exception -> L87
            r1 = 10
            if (r0 >= r1) goto L4b
        L4a:
            return
        L4b:
            boolean r0 = a()     // Catch: java.lang.Exception -> L87
            if (r0 == 0) goto L52
            return
        L52:
            b()     // Catch: java.lang.Exception -> L87
            java.io.ByteArrayInputStream[] r0 = defpackage.e.a     // Catch: java.lang.Exception -> L87
            r1 = r4
            r0 = r0[r1]     // Catch: java.lang.Exception -> L87
            r0.reset()     // Catch: java.lang.Exception -> L87
            long r0 = java.lang.System.currentTimeMillis()     // Catch: java.lang.Exception -> L87
            defpackage.e.d = r0     // Catch: java.lang.Exception -> L87
            java.io.ByteArrayInputStream[] r0 = defpackage.e.a     // Catch: java.lang.Exception -> L87
            r1 = r4
            r0 = r0[r1]     // Catch: java.lang.Exception -> L87
            java.lang.String[] r1 = defpackage.e.c     // Catch: java.lang.Exception -> L87
            r2 = r4
            r1 = r1[r2]     // Catch: java.lang.Exception -> L87
            javax.microedition.media.Player r0 = javax.microedition.media.Manager.createPlayer(r0, r1)     // Catch: java.lang.Exception -> L87
            r1 = r0
            defpackage.e.b = r1     // Catch: java.lang.Exception -> L87
            r1 = 1
            r0.setLoopCount(r1)     // Catch: java.lang.Exception -> L87
            javax.microedition.media.Player r0 = defpackage.e.b     // Catch: java.lang.Exception -> L87
            r0.start()     // Catch: java.lang.Exception -> L87
            r0 = r4
            defpackage.e.e = r0     // Catch: java.lang.Exception -> L87
            return
        L87:
            return
    }

    static void b() {
            javax.microedition.media.Player r0 = defpackage.e.b     // Catch: java.lang.Exception -> L1f
            if (r0 == 0) goto L1e
            r0 = -1
            defpackage.e.e = r0     // Catch: java.lang.Exception -> L1f
            javax.microedition.media.Player r0 = defpackage.e.b     // Catch: java.lang.Exception -> L1f
            r0.stop()     // Catch: java.lang.Exception -> L1f
            javax.microedition.media.Player r0 = defpackage.e.b     // Catch: java.lang.Exception -> L1f
            r0.close()     // Catch: java.lang.Exception -> L1f
            r0 = 0
            defpackage.e.b = r0     // Catch: java.lang.Exception -> L1f
        L1e:
            return
        L1f:
            r0 = 0
            defpackage.e.b = r0
            return
    }

    static {
            r0 = 34
            java.io.ByteArrayInputStream[] r0 = new java.io.ByteArrayInputStream[r0]
            defpackage.e.a = r0
            r0 = 0
            defpackage.e.b = r0
            r0 = 34
            java.lang.String[] r0 = new java.lang.String[r0]
            defpackage.e.c = r0
            r0 = -1
            defpackage.e.e = r0
            return
    }
}
