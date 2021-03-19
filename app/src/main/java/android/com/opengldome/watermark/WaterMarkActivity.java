package android.com.opengldome.watermark;

import android.com.opengldome.R;
import android.com.opengldome.utils.SystemAlbumUtil;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * create by caiyx in 2021/3/19
 */
public class WaterMarkActivity extends AppCompatActivity {

    private Button bnSelect;
    private TextView tvFile;
    private Button bnExecute;

    private String path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_water_mark);
        bnSelect = findViewById(R.id.bn_select);
        tvFile = findViewById(R.id.tv_file);
        bnExecute = findViewById(R.id.bn_execute);

        bnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemAlbumUtil.INSTANCE.openSystemAlbum(WaterMarkActivity.this, 1111, SystemAlbumUtil.TYPE_VIDEO);
            }
        });

        bnExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (path != null) {
                    new Thread(new WaterMarkProcess(path)).start();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SystemAlbumUtil.INSTANCE.onActivityResult(WaterMarkActivity.this, 1111, requestCode, resultCode, data, new SystemAlbumUtil.SystemAlbumCallBack() {
            @Override
            public void onResult(@org.jetbrains.annotations.Nullable String uri) {
                if (uri != null) {
                    path = uri;
                    tvFile.setText("路径：" + uri);
                }
            }
        });
    }
}
