package android.com.opengldome.camera2.view;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public class ControlLayout extends FrameLayout {

    private FrameLayout.LayoutParams fl;

    private TopLayout topLayout;
    private FocusView focusView;

    private TopLayout.TopLayoutCallback topLayoutCallback;
    private ControlLayoutCallback controlLayoutCallback;
    private FocusView.FocusViewCallback focusViewCallback;

    public ControlLayout(@NonNull Context context) {
        super(context);
        initListen();
        initView();
    }

    private void initListen() {
        topLayoutCallback = new TopLayout.TopLayoutCallback() {
            @Override
            public void onSwitch() {
                controlLayoutCallback.onSwitch();
            }
        };
        focusViewCallback = new FocusView.FocusViewCallback() {
            @Override
            public void onFocusClick(int x, int y) {
                controlLayoutCallback.onFocusClick(x, y);
            }
        };
    }

    private void initView() {
        focusView = new FocusView(getContext());
        focusView.setFocusViewCallback(focusViewCallback);
        fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(focusView, fl);

        topLayout = new TopLayout(getContext());
        topLayout.setTopLayoutCallback(topLayoutCallback);
        fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        addView(topLayout, fl);
    }

    public void setControlLayoutCallback(ControlLayoutCallback callback) {
        this.controlLayoutCallback = callback;
    }

    public interface ControlLayoutCallback {
        public void onSwitch();

        public void onFocusClick(int x, int y);
    }
}