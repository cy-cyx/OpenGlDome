package android.com.opengldome.obj;

import android.app.ProgressDialog;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

public class ObjActivity extends AppCompatActivity {

    private GLSurfaceView mGlSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setTitle("解obj数据中");
        progressDialog.show();

        // 开始解码线程
        ObjDecodeTask objDecodeTask = new ObjDecodeTask();
        objDecodeTask.setObjDecodeTaskListen(new ObjDecodeTask.IObjDecodeTaskListen() {
            @Override
            public void onCallBack(final float[] result, final boolean success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            initGlSurfaceView(result);
                        } else {
                            Toast.makeText(ObjActivity.this, "解obj数据错误", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
            }
        });
        new Thread(objDecodeTask).start();
    }

    private void initGlSurfaceView(float[] data) {
        mGlSurfaceView = new GLSurfaceView(this);
        mGlSurfaceView.setEGLContextClientVersion(3);
        ObjRender objRender = new ObjRender();
        objRender.setData(data);
        mGlSurfaceView.setRenderer(objRender);
        mGlSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
        setContentView(mGlSurfaceView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGlSurfaceView != null)
            mGlSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGlSurfaceView != null)
            mGlSurfaceView.onResume();
    }
}
