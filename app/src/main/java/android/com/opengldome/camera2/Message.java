package android.com.opengldome.camera2;

/**
 * create by cy
 * time : 2019/12/11
 * version : 1.0
 * Features :
 */
public class Message {

    public static final int MSG_SURFACE_CREATE = 0; // 预览GlSurfaceView的OES成功创建（正常只会调用一次）
    public static final int MSG_PAUSE = 1;
    public static final int MSG_RESUME = 2;
    public static final int MSG_RELEASE = 3;
    public static final int MSG_SWITCH = 4; // 切换镜头
    public static final int MSG_FOCUS = 5; // 切换镜头
}
