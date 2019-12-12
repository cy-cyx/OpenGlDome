package android.com.opengldome.camera2;

import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;

/**
 * create by cy
 * time : 2019/12/12
 * version : 1.0
 * Features : 预览请求工厂模式通过{@link CameraConfig}构建响应的请求
 */
public class RequestBuilderFactory {

    public static CaptureRequest.Builder getRequestBuilderBase(CaptureRequest.Builder builder, CameraConfig cameraConfig) {

        // 需要设置为auto control指令才能生效
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        // af
        if (cameraConfig.afRectangles != null)
            builder.set(CaptureRequest.CONTROL_AF_REGIONS, cameraConfig.afRectangles);
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
        builder.set(CaptureRequest.CONTROL_AF_MODE, cameraConfig.controlAfMode);

        // ae
        if (cameraConfig.afRectangles != null)
            builder.set(CaptureRequest.CONTROL_AE_REGIONS, cameraConfig.aeRectangles);
        builder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_IDLE);
        builder.set(CaptureRequest.CONTROL_AE_MODE, cameraConfig.controlAeMode);

        return builder;
    }
}
