package defpackage;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
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
        if (a != null) {
            Display.getDisplay(this).setCurrent(a);
            return;
        }
        try {
            b = getAppProperty("MIDlet-Version");
        } catch (Exception unused) {
        }
        try {
            k.bK = !getAppProperty("HAS-BLOOD").equals("0");
        } catch (Exception unused2) {
            k.bK = false;
        }
        a = new k(this, Display.getDisplay(this));
    }

    public void destroyApp(boolean z) {
        Display.getDisplay(this).setCurrent((Displayable) null);
        if (a != null) {
            a = null;
        }
        notifyDestroyed();
        c = null;
    }
}
