package android.com.opengldome.camera2;

import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
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
    public String cameraId = "1";

    /**
     * 角度
     */
    public int rotation = 0;

    /**
     * 输出最佳尺寸
     */
    public Size optimalSize;

    /**
     * 对焦模式
     * {@link CaptureRequest#CONTROL_AF_MODE}
     * <p>
     * {@link android.hardware.camera2.CameraMetadata#CONTROL_AF_MODE_AUTO} 最基本的对焦方法，手动对焦时需要把他设置为该参数
     * {@link android.hardware.camera2.CameraMetadata#CONTROL_AF_MODE_CONTINUOUS_PICTURE} 自动对焦根据算法确定一个持续对焦的图片
     */
    public int controlAfMode = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE;

    /**
     * 对焦区域
     */
    public MeteringRectangle[] afRectangles = null;

    /**
     * 闪光灯
     * {@link CaptureRequest#CONTROL_AE_MODE}
     *
     * {@link android.hardware.camera2.CameraMetadata#CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE} 红颜
     * {@link android.hardware.camera2.CameraMetadata#CONTROL_AE_MODE_ON_AUTO_FLASH} 自动开启闪光灯
     */
    public int controlAeMode = CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH;

    /**
     * 测光区域
     */
    public MeteringRectangle[] aeRectangles = null;

    /**
     * 切换镜头方向
     */
    public String switchCamera() {
        return cameraId = cameraId.equals("0") ? "1" : "0";
    }

    /**
     * 重置为自动曝光和聚焦
     */
    public void resetAeAfMode() {
        controlAfMode = CaptureResult.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
        afRectangles = null;
        aeRectangles = null;
    }
}
