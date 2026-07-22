package defpackage;

/* loaded from: assassins_creed_-_br_320x240_136711.jar:c.class */
public final class c {
    int a;
    int b;
    private short k;
    short c;
    short d;
    byte e;
    byte f;
    short g;
    short h;
    short i;
    private static short l = 0;
    private static c[] m = new c[400];
    static short j = 10000;

    static void a(short[] sArr) {
        c cVar = new c();
        cVar.k = sArr[1];
        cVar.a = sArr[2];
        cVar.b = sArr[3];
        cVar.c = sArr[4];
        cVar.d = sArr[5];
        cVar.e = (byte) sArr[6];
        cVar.f = (byte) sArr[7];
        cVar.g = sArr[8];
        m[l] = cVar;
        l = (short) (l + 1);
    }

    static void a(c cVar, i iVar) {
        c cVar2 = new c();
        cVar2.k = j;
        j = (short) (j + 1);
        cVar2.a = cVar.a + iVar.ak;
        cVar2.b = cVar.b;
        cVar2.c = cVar.c;
        cVar2.d = cVar.d;
        cVar2.e = cVar.e;
        cVar2.f = cVar.f;
        cVar2.g = cVar.g;
        m[l] = cVar2;
        l = (short) (l + 1);
    }

    static void a() {
        for (int i = 0; i < m.length; i++) {
            m[i] = null;
        }
        l = (short) 0;
        j = (short) 10000;
    }

    static c a(int i) {
        if (i < 0) {
            return null;
        }
        for (int i2 = 0; i2 < l; i2++) {
            if (m[i2] != null && m[i2].k == i) {
                return m[i2];
            }
        }
        return null;
    }
}
