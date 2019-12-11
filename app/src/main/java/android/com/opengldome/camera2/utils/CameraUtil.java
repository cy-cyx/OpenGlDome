package android.com.opengldome.camera2.utils;

import android.app.Activity;
import android.com.opengldome.Application;
import android.com.opengldome.utils.WHView;
import android.content.Context;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import java.util.ArrayList;

import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_INACTIVE;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_PASSIVE_FOCUSED;
import static android.hardware.camera2.CameraMetadata.CONTROL_AF_STATE_PASSIVE_SCAN;

/**
 * create by cy
 * time : 2019/12/5
 * version : 1.0
 * Features :
 */
public class CameraUtil {

    /**
     * 找出最佳的预览尺寸
     */
    public static Size getOptimalSize(int angle, Size[] sizes, int viewWidth, int viewHeight) {

        // 如果竖屏需要转成横屏
        int targetViewWidth = viewWidth;
        int targetViewHeight = viewHeight;
        if (angle == 90 || angle == 270) {
            targetViewWidth = viewHeight;
            targetViewHeight = viewWidth;
        }

        // 找出宽高满足条件的
        ArrayList<Size> satisfySizeSizes = new ArrayList<>();
        for (Size size : sizes) {
            if (size.getWidth() >= targetViewWidth && size.getHeight() >= targetViewHeight) {
                satisfySizeSizes.add(size);
            }
        }

        // 找出比例相同的或者相近的
        float targerRatio = (float) targetViewWidth / (float) targetViewHeight;

        Size result = sizes[0];
        float diff = Float.MAX_VALUE;

        for (Size size : satisfySizeSizes) {
            float tempDiff = Math.abs(((float) size.getWidth() / (float) size.getHeight()) - targerRatio);
            if (tempDiff < diff) {
                diff = tempDiff;
                result = size;

                // 如果0，直接返回
                if (diff == 0)
                    return result;
            }
        }

        return result;
    }

    /**
     * 获得焦点位置
     */
    public static MeteringRectangle[] focusAeAf(int clickX, int clickY, Size optimalSize, Size array) {
        int preViewRotation = 90;
        int x = 0;
        int y = 0;
        if (preViewRotation == 90) {
            // 先镜像
            clickX = (int) (WHView.getViewWidth() - clickX);
            clickY = clickY;

            // 旋转90
            x = clickY;
            y = (int) (WHView.getViewWidth() - clickX);
        }

        float scac = (float) array.getWidth() / (float) optimalSize.getWidth();

        int shang = 0;
        int viewShang = 0;
        int viewLeft = 0;

        int height = (int) (optimalSize.getWidth() / (float) array.getWidth() * array.getHeight());
        shang = (int) ((array.getHeight() - height) / 2f); // 上面需要加的

        if (WHView.getViewHeight() / WHView.getViewWidth() > optimalSize.getWidth() / optimalSize.getHeight()) {
            float v = (float) WHView.getViewHeight() / (float) optimalSize.getWidth() * optimalSize.getHeight();
            float f = v / WHView.getViewWidth() - 1;
            viewShang = (int) ((optimalSize.getHeight() * f) / 2f * scac);

        } else {
            float v = optimalSize.getHeight() / (float) WHView.getViewHeight() * WHView.getViewWidth();
            viewLeft = (int) ((v - optimalSize.getWidth()) / 2f * scac);
        }

        x = (int) (x * scac + viewLeft);
        y = (int) (y * scac + viewShang + shang);

        MeteringRectangle[] m = new MeteringRectangle[2];
        m[0] = new MeteringRectangle(x - 40, y - 40, 80, 80, 1000);
        m[1] = new MeteringRectangle(x - 80, y - 80, 160, 160, 1000);
        return m;
    }

    private static SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();

    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 90);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 270);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 180);
    }

    /**
     * 获得屏幕角度和预览角度的矫正角度（因为预览数据底层会自动帮我们转正，但是我们不需要矫正，需要转回来）
     */
    public static int getPreViewRotation(Context context) {
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        return DISPLAY_ORIENTATIONS.get(rotation);
    }

    public static void logFocus(int afStatus){
        switch (afStatus){
            case CONTROL_AF_STATE_INACTIVE:
                Log.d("xx", "logFocus: AF已关闭或尚未尝试扫描/尚未被要求扫描。");
                break;
            case CONTROL_AF_STATE_PASSIVE_SCAN:
                Log.d("xx", "logFocus: AF当前正在以连续自动对焦模式执行AF扫描，以启动照相机设备。");
                break;
            case CONTROL_AF_STATE_PASSIVE_FOCUSED:
                Log.d("xx", "logFocus: AF当前认为它已成为焦点，但可能随时重启扫描。");
                break;
            case CONTROL_AF_STATE_ACTIVE_SCAN:
                Log.d("xx", "logFocus: AF正在执行AF扫描，因为它是由AF触发器触发的。");
                break;
            case CONTROL_AF_STATE_FOCUSED_LOCKED:
                Log.d("xx", "logFocus: AF认为对焦正确并锁定了焦点");
                break;
            case CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                Log.d("xx", "logFocus: ");
        }
    }
}
