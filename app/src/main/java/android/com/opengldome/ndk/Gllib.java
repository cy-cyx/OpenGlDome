package android.com.opengldome.ndk;

/**
 * create by cy
 * time : 2019/12/18
 * version : 1.0
 * Features :
 */
public class Gllib {

    static {
        System.loadLibrary("GlLib");
    }

    public static native int creatBaseProgram();
}
