package android.com.opengldome.camera2.view;

import android.com.opengldome.R;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 顶部的布局包含了：
 * 1、反转
 * 2、
 */
public class TopLayout extends FrameLayout {

    private LayoutParams fl;

    private FrameLayout containerFl;
    private ImageView switchIv;

    private TopLayoutCallback topLayoutCallback;
    private OnClickListener onClickListener;

    public TopLayout(Context context) {
        super(context);
        initListen();
        initView();
    }

    private void initListen() {
        onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == switchIv) {
                    topLayoutCallback.onSwitch();
                }
            }
        };
    }

    private void initView() {
        containerFl = new FrameLayout(getContext());
        fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
        addView(containerFl, fl);

        switchIv = new ImageView(getContext());
        switchIv.setBackgroundResource(R.drawable.ic_switch_camera);
        switchIv.setOnClickListener(onClickListener);
        fl = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fl.gravity = Gravity.CENTER;
        containerFl.addView(switchIv, fl);
    }

    public void setTopLayoutCallback(TopLayoutCallback callback) {
        this.topLayoutCallback = callback;
    }

    public interface TopLayoutCallback {
        public void onSwitch();
    }
}
