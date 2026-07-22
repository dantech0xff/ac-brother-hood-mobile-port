package defpackage;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:GloftASBR.class */
public class GloftASBR extends javax.microedition.midlet.MIDlet {
    public static defpackage.k a;
    public static java.lang.String b;
    public static defpackage.GloftASBR c;

    public GloftASBR() {
            r2 = this;
            r0 = r2
            r0.<init>()
            r0 = r2
            defpackage.GloftASBR.c = r0
            return
    }

    public void pauseApp() {
            r2 = this;
            r0 = r2
            r0.notifyPaused()
            return
    }

    public void startApp() {
            r5 = this;
            k r0 = defpackage.GloftASBR.a
            if (r0 != 0) goto L41
            r0 = r5
            java.lang.String r1 = "MIDlet-Version"
            java.lang.String r0 = r0.getAppProperty(r1)     // Catch: java.lang.Exception -> L12
            defpackage.GloftASBR.b = r0     // Catch: java.lang.Exception -> L12
            goto L13
        L12:
        L13:
            r0 = r5
            java.lang.String r1 = "HAS-BLOOD"
            java.lang.String r0 = r0.getAppProperty(r1)     // Catch: java.lang.Exception -> L2c
            java.lang.String r1 = "0"
            boolean r0 = r0.equals(r1)     // Catch: java.lang.Exception -> L2c
            if (r0 != 0) goto L25
            r0 = 1
            goto L26
        L25:
            r0 = 0
        L26:
            defpackage.k.bK = r0     // Catch: java.lang.Exception -> L2c
            goto L31
        L2c:
            r0 = 0
            defpackage.k.bK = r0
        L31:
            k r0 = new k
            r1 = r0
            r2 = r5
            r3 = r5
            javax.microedition.lcdui.Display r3 = javax.microedition.lcdui.Display.getDisplay(r3)
            r1.<init>(r2, r3)
            defpackage.GloftASBR.a = r0
            return
        L41:
            r0 = r5
            javax.microedition.lcdui.Display r0 = javax.microedition.lcdui.Display.getDisplay(r0)
            k r1 = defpackage.GloftASBR.a
            r0.setCurrent(r1)
            return
    }

    public void destroyApp(boolean r4) {
            r3 = this;
            r0 = r3
            javax.microedition.lcdui.Display r0 = javax.microedition.lcdui.Display.getDisplay(r0)
            r1 = 0
            r0.setCurrent(r1)
            k r0 = defpackage.GloftASBR.a
            if (r0 == 0) goto L12
            r0 = 0
            defpackage.GloftASBR.a = r0
        L12:
            r0 = r3
            r0.notifyDestroyed()
            r0 = 0
            defpackage.GloftASBR.c = r0
            return
    }
}
