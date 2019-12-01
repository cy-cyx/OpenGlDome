package android.com.opengldome.camera2.view;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public class ControlLayout extends FrameLayout {

    private FrameLayout.LayoutParams fl;

    private TopLayout topLayout;

    private TopLayout.TopLayoutCallback topLayoutCallback;
    private ControlLayoutCallback controlLayoutCallback;

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
    }

    private void initView() {
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
    }
}
