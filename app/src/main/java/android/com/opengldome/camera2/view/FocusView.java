package android.com.opengldome.camera2.view;

import android.com.opengldome.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * create by cy
 * time : 2019/12/11
 * version : 1.0
 * Features : 显示焦点点击的动画控件
 */
public class FocusView extends View {

    private boolean showFocusUI = false;
    private int focusX = 0;
    private int focusY = 0;
    private DisappearThread disappearThread;
    private Paint paint;

    private FocusViewCallback focusViewCallback;

    private int width;
    private int height;
    private Bitmap focusBmp;

    public FocusView(Context context) {
        super(context);
        focusBmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_focus);
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_UP:
                focusViewCallback.onFocusClick((int) event.getX(), (int) event.getY(), width, height);
                focusX = (int) event.getX();
                focusY = (int) event.getY();
                clickFocus();
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 更新聚焦点
     */
    private void clickFocus() {
        if (disappearThread != null) {
            disappearThread.interrupt();
            disappearThread = null;
        }
        showFocusUI = true;
        invalidate();
        disappearThread = new DisappearThread();
        disappearThread.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showFocusUI) {
            int left = (int) (focusX - focusBmp.getWidth() / 2.f);
            int top = (int) (focusY - focusBmp.getHeight() / 2.f);
            canvas.drawBitmap(focusBmp, left, top, paint);
        }
    }

    public void setFocusViewCallback(FocusViewCallback focusViewCallback) {
        this.focusViewCallback = focusViewCallback;
    }

    public interface FocusViewCallback {
        public void onFocusClick(int x, int y, int viewWidth, int viewHeight);
    }

    /**
     * 管理对焦后的消失动画
     */
    private class DisappearThread extends Thread {

        boolean esc = false;

        @Override
        public void run() {
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                esc = true;
            }
            if (!esc) {
                showFocusUI = false;
                postInvalidate();
            }
        }
    }
}
