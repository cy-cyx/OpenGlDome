package android.com.opengldome.beauty;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * create by cy
 * time : 2019/11/26
 * version : 1.0
 * Features :
 */
public class BeautyActivity extends AppCompatActivity {

    private GLSurfaceView mGlSurfaceView;
    private BeautyRender beautyRender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout mainLayout = new FrameLayout(this);
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(mainLayout, fl);
        {
            mGlSurfaceView = new GLSurfaceView(this);
            mGlSurfaceView.setEGLContextClientVersion(3);
            mGlSurfaceView.setPreserveEGLContextOnPause(true);
            beautyRender = new BeautyRender();
            mGlSurfaceView.setRenderer(beautyRender);
            fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mainLayout.addView(mGlSurfaceView, fl);

            SeekBar seekBar = new SeekBar(this);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    beautyRender.setCurAlpha(progress / 100f);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekBar.setMax(100);
            fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
            mainLayout.addView(seekBar, fl);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();
        mGlSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGlSurfaceView.onResume();
    }

}
