package android.com.opengldome.camera2.view;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/**
 * create by cy
 * time : 2019/12/11
 * version : 1.0
 * Features : 显示焦点点击的动画控件
 */
public class FocusView extends View {

    private FocusViewCallback focusViewCallback;

    private int width;
    private int height;

    public FocusView(Context context) {
        super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_UP:
                focusViewCallback.onFocusClick((int) event.getX(), (int) event.getY(), width, height);
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
    }

    public void setFocusViewCallback(FocusViewCallback focusViewCallback) {
        this.focusViewCallback = focusViewCallback;
    }

    public interface FocusViewCallback {
        public void onFocusClick(int x, int y, int viewWidth, int viewHeight);
    }
}
