package android.com.opengldome.camera2.utils;

import android.app.Activity;
import android.com.opengldome.utils.WHView;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Log;
import android.util.Size;
import android.util.SizeF;
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
     *
     * @param clickX      在View坐标的X
     * @param clickY      在View坐标的Y
     * @param optimalSize 预览尺寸
     * @param pixel       最大素材尺寸
     * @param rotation    方向（大部分机型默认为90）
     * @return 返回Af和Ae的参照框（Ae要比Af大效果更好）
     */
    public static MeteringRectangle[] focusAeAf(int clickX, int clickY, Size optimalSize, Size pixel, int rotation) {
        int AF = 100 / 2;
        int AE = 120 / 2;

        SizeF size = viewCoord2PreviewCoord(clickX, clickY, optimalSize, rotation);

        int optimalWidth = optimalSize.getWidth();
        int optimalHeight = optimalSize.getHeight();

        int pixelWidth = pixel.getWidth();
        int pixelHeight = pixel.getHeight();

        float resultX = size.getWidth();
        float resuleY = size.getHeight();

        if (pixelWidth / pixelHeight > optimalWidth / optimalHeight) {
            // 宽有富余

            float zoom = (float) pixelHeight / (float) optimalHeight;

            float surplusWidth = ((pixelWidth - (float) pixelHeight * (float) optimalWidth / (float) optimalHeight) / 2.f);

            resultX = (int) (resultX * zoom + surplusWidth);
            resuleY = (int) (resuleY * zoom);
        } else {
            float zoom = (float) pixelWidth / (float) optimalWidth;

            float surplusHeight = ((pixelHeight - (float) pixelWidth * (float) optimalHeight / (float) optimalWidth) / 2.f);
            resultX = (int) (resultX * zoom);
            resuleY = (int) (resuleY * zoom + surplusHeight);
        }


        MeteringRectangle af = new MeteringRectangle(
                new Rect(clamp(resultX - AF, 0, pixelWidth),
                        clamp(resuleY - AF, 0, pixelHeight),
                        clamp(resultX + AF, 0, pixelWidth),
                        clamp(resuleY + AF, 0, pixelHeight)),
                MeteringRectangle.METERING_WEIGHT_MAX);
        MeteringRectangle ae = new MeteringRectangle(
                new Rect(clamp(resultX - AE, 0, pixelWidth),
                        clamp(resuleY - AE, 0, pixelHeight),
                        clamp(resultX + AE, 0, pixelWidth),
                        clamp(resuleY + AE, 0, pixelHeight)),
                MeteringRectangle.METERING_WEIGHT_MAX);

        return new MeteringRectangle[]{af, ae};
    }

    private static int clamp(float obj, int min, int max) {
        if (obj < min)
            return min;
        if (obj > max)
            return max;
        return (int) obj;
    }

    /**
     * 将在View上的点击位置转换成在预览尺寸的点击位置
     */
    private static SizeF viewCoord2PreviewCoord(int clickX, int clickY, Size optimalSize, int rotation) {

        // 点击的wiew是全显示
        int viewWidth = (int) WHView.getViewWidth();
        int viewHeight = (int) WHView.getViewHeight();

        int optimalWidth = optimalSize.getWidth();
        int optimalHeight = optimalSize.getHeight();

        float resultX = clickX;
        float resultY = clickY;

        // 基本的机型都是90
        if (rotation == 90) {

            // 90旋转
            float tempX = resultX;
            resultX = resultY;
            resultY = viewWidth - tempX;

            // view的宽高也旋转90
            int tempWidth = viewWidth;
            viewWidth = viewHeight;
            viewHeight = tempWidth;

            // 判断显示区域的宽居中还高居中
            if (optimalWidth / (float) optimalHeight > viewWidth / (float) viewHeight) {
                // 预览宽大于高 等宽后 高居中

                float zoom = (float) optimalHeight / (float) viewHeight;

                // 多出的宽
                int surplusWidth = (int) ((optimalWidth - (float) optimalHeight * (float) viewWidth / (float) viewHeight) / 2.f);

                resultX = (int) (resultX * zoom + surplusWidth);
                resultY = (int) (resultY * zoom);
                return new SizeF(resultX, resultY);
            } else {
                float zoom = (float) optimalWidth / (float) viewWidth;

                // 多出的高
                float surplusHeight = ((optimalHeight - (float) optimalWidth * (float) viewHeight / (float) viewWidth) / 2.f);

                resultX = resultX * zoom;
                resultY = resultY * zoom + surplusHeight;
                return new SizeF(resultX, resultY);
            }
        }
        return null;
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

    public static void logFocus(int afStatus) {
        switch (afStatus) {
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
                Log.d("xx", "logFocus: 焦点失败");
        }
    }
}
