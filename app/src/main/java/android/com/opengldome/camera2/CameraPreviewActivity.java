package android.com.opengldome.camera2;

import android.com.opengldome.Application;
import android.com.opengldome.utils.BitmapUtils;
import android.com.opengldome.utils.FileUtils;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 预览Activity
 */
public class CameraPreviewActivity extends AppCompatActivity {

    public static String KEY_DATA = "key_data";

    private ImageView imageView;
    private Button saveButton;

    private String pic;

    private View.OnClickListener onClickListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initListen();
        initView();
        initData();
    }

    private void initListen() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == saveButton) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String path = FileUtils.getDCIMBitmapPath(Application.getInstance());
                            FileUtils.copyFile(pic, path);
                            BitmapUtils.addToMediaStore(CameraPreviewActivity.this, path);
                            CameraPreviewActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(CameraPreviewActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }
            }
        };
    }

    private void initView() {
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(container, fl);
        {
            imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(imageView, fl);

            saveButton = new Button(this);
            saveButton.setText("保存");
            saveButton.setOnClickListener(onClickListener);
            fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            container.addView(saveButton, fl);
        }
    }

    private void initData() {
        pic = getIntent().getStringExtra(KEY_DATA);
        imageView.setImageBitmap(BitmapFactory.decodeFile(pic));
    }
}
