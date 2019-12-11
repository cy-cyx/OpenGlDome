package android.com.opengldome.camera2.view;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/**
 * create by cy
 * time : 2019/12/11
 * version : 1.0
 * Features : 显示焦点点击的
 */
public class FocusView extends View {

    private FocusViewCallback focusViewCallback;

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
                focusViewCallback.onFocusClick((int) event.getX(), (int) event.getY());
                return true;
        }
        return super.onTouchEvent(event);
    }

    public void setFocusViewCallback(FocusViewCallback focusViewCallback) {
        this.focusViewCallback = focusViewCallback;
    }

    public interface FocusViewCallback {
        public void onFocusClick(int x, int y);
    }
}
