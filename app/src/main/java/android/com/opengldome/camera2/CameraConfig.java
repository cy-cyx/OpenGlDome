package android.com.opengldome.camera2;

import android.hardware.camera2.CaptureRequest;
import android.util.Size;

/**
 * create by cy
 * time : 2019/11/29
 * version : 1.0
 * Features : 设置预览的参数配置（先建个大概）
 */
public class CameraConfig {

    /**
     * "1"为前摄像头 "0"为后摄像头
     */
    public String cameraId = "0";

    /**
     * 输出尺寸
     */
    public Size optimalSize;

    /**
     * 对焦模式
     * {@link CaptureRequest#CONTROL_AF_MODE}
     */
    public int controlAfMode = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE;

    /**
     * 闪光灯
     * {@link CaptureRequest#CONTROL_AF_MODE}
     */
    public int controlAeMode = CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH;

    public String switchCamera() {
        return cameraId = cameraId.equals("0") ? "1" : "0";
    }
}
