package android.com.opengldome.camera2.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;

import java.util.ArrayList;

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

    private static SparseIntArray DISPLAY_ORIENTATIONS = new SparseIntArray();

    static {
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_0, 90);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_90, 0);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_180, 270);
        DISPLAY_ORIENTATIONS.put(Surface.ROTATION_270, 180);
    }

    public static int getPreViewRotation(Context context) {
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        return DISPLAY_ORIENTATIONS.get(rotation);
    }
}
