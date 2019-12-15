package android.com.opengldome.camera2.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.view.MotionEvent;
import android.view.View;

public class ShutButton extends View {

    private Paint paint;
    private PorterDuffXfermode porterClear;

    private float circleSize = 200;//圆环的大小
    private float circleWidth = 20;//圆环的宽

    private float lastChangeFloat;//按下最后变化的大小

    private float tempSize;//每一次变化是大小

    private int color = 0xfff1ffff;//圆环的颜色

    private final long duration = 100;

    private ValueAnimator downAnimator;
    private ValueAnimator upAnimator;

    private long runTime;//按下动画时间

    private ShutButtonListen shutButtonListen;

    public ShutButton(Context context) {
        super(context);
        init();
    }

    protected void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        porterClear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension((int) circleSize, (int) circleSize);
    }

    public void setViewSize(int size) {
        this.circleSize = size;
        setMeasuredDimension(size, size);
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downAnimator = ValueAnimator.ofFloat(0, 1f);
                downAnimator.setDuration(duration);
                downAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float animatedValue = (float) valueAnimator.getAnimatedValue();
                        tempSize = animatedValue * (circleSize / 10);
                        invalidate();
                    }
                });
                downAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        shutButtonListen.ShutDown();
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        lastChangeFloat = (float) ((ValueAnimator) animator).getAnimatedValue();
                        runTime = (long) (lastChangeFloat * duration);
                        postInvalidate();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                downAnimator.start();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_UP:
                if (downAnimator.isRunning()) {
                    downAnimator.cancel();
                }
                upAnimator = ValueAnimator.ofFloat(lastChangeFloat, 0);
                upAnimator.setDuration(runTime);
                upAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        float animatedValue = (float) valueAnimator.getAnimatedValue();
                        tempSize = animatedValue * (circleSize / 10);
                        postInvalidate();
                    }
                });
                upAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        shutButtonListen.ShutClick();
                        shutButtonListen.ShutUp();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                upAnimator.start();
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setXfermode(null);
        paint.setColor(color);
        canvas.drawCircle(circleSize / 2, circleSize / 2,
                (circleSize - tempSize) / 2, paint);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setXfermode(porterClear);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(circleSize / 2, circleSize / 2,
                (circleSize - tempSize - circleWidth) / 2, paint);
    }

    public void setShutListen(ShutButtonListen shutListen) {
        this.shutButtonListen = shutListen;
    }

    public interface ShutButtonListen {
        public void ShutDown();

        public void ShutUp();

        public void ShutClick();
    }
}
