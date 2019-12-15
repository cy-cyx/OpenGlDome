package android.com.opengldome.camera2.view;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

/**
 * 底部的布局包含了：
 * 1、拍摄按钮
 */
public class ButtonLayout extends FrameLayout {

    private FrameLayout containerFl;
    private ShutButton shutButton;

    private ShutButton.ShutButtonListen shutButtonListen;
    private ButtonLayoutCallback buttonLayoutCallback;

    public ButtonLayout(@NonNull Context context) {
        super(context);
        initListen();
        initView();
    }

    private void initListen() {
        shutButtonListen = new ShutButton.ShutButtonListen() {
            @Override
            public void ShutDown() {

            }

            @Override
            public void ShutUp() {

            }

            @Override
            public void ShutClick() {
                buttonLayoutCallback.onShutCLick();
            }
        };
    }

    private void initView() {
        FrameLayout.LayoutParams fl;
        containerFl = new FrameLayout(getContext());
        fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400);
        fl.gravity = Gravity.BOTTOM;
        addView(containerFl, fl);

        shutButton = new ShutButton(getContext());
        shutButton.setShutListen(shutButtonListen);
        fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.CENTER;
        containerFl.addView(shutButton, fl);
    }

    public void setButtonLayoutCallback(ButtonLayoutCallback callback) {
        this.buttonLayoutCallback = callback;
    }

    public interface ButtonLayoutCallback {
        public void onShutCLick();
    }
}
