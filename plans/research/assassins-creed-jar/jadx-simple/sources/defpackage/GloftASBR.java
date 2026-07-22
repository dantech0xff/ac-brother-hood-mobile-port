package defpackage;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:GloftASBR.class */
public class GloftASBR extends MIDlet {
    public static k a;
    public static String b;
    public static GloftASBR c;

    public GloftASBR() {
        c = this;
    }

    public void pauseApp() {
        notifyPaused();
    }

    public void startApp() {
        if (a == null) goto L18;
        Display.getDisplay(this).setCurrent(a);
        return;
    L18:
        b = getAppProperty("MIDlet-Version");     // Catch: Exception -> L5
    L16:
    L11:
        k.bK = false;
    L12:
        a = new k(this, Display.getDisplay(this));
        return;
    L7:
        if (getAppProperty("HAS-BLOOD").equals("0") == true) goto L9;
        boolean r0 = true;
    L10:
        k.bK = r0;     // Catch: Exception -> L11
        goto L12
    L9:
        r0 = false;
        goto L10
    }

    public void destroyApp(boolean r4) {
        Display.getDisplay(this).setCurrent(null);
        if (a == null) goto L5;
        a = null;
    L5:
        notifyDestroyed();
        c = null;
    }
}
