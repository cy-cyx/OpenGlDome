package android.com.opengldome.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * create by cy
 * time : 2019/8/23
 * version : 1.0
 * Features : 这个View用于拿屏幕的真正的宽高
 * 限制：需要找一个没有虚拟键的activity add进去初始化一下
 */
public class WHView extends View {

    public static float sViewHeight = 0;
    public static float sViewWidth = 0;

    public WHView(Context context) {
        super(context);
        setBackgroundColor(0x00000000);
    }

    public void init(ViewGroup view) {
        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.addView(this, vl);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        // 沉浸有几秒的变换
        if (width > sViewWidth) {
            sViewWidth = width;
        }
        if (height > sViewHeight) {
            sViewHeight = height;
        }
    }

    /**
     * 屏幕高度（包含虚拟键和刘海）
     *
     * @return
     */
    public static float getViewHeight() {
        return sViewHeight;
    }

    /**
     * 屏幕宽度
     *
     * @return
     */
    public static float getViewWidth() {
        return sViewWidth;
    }
}
