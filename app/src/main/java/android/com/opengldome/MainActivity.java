package android.com.opengldome;

import android.app.ActivityManager;
import android.com.opengldome.blend.BlendActivity;
import android.com.opengldome.fbo.FBOActivity;
import android.com.opengldome.light.LightActivity;
import android.com.opengldome.mvp.MVPActivity;
import android.com.opengldome.obj.ObjActivity;
import android.com.opengldome.particsystem.PSActivity;
import android.com.opengldome.shadows.ShadowsActivity;
import android.com.opengldome.texture.TextureActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private Class[] classes = new Class[]{FBOActivity.class,
            MVPActivity.class, LightActivity.class, BlendActivity.class,
            ShadowsActivity.class, TextureActivity.class, ObjActivity.class,
            PSActivity.class};
    private String[] names = new String[]{"帧缓冲区",
            "MVP矩阵", "冯氏光照模型(平行光)", "混合模式",
            "阴影（深度纹理）", "纹理贴图", "obj3D模型（法线贴图）",
            "粒子系统"};

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecyclerView = new RecyclerView(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(new MyAdapter());
        ViewGroup.LayoutParams vl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(mRecyclerView, vl);

        checkSupportGLES30();
    }

    private void checkSupportGLES30() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportGlES30 = configurationInfo.reqGlEsVersion >= 0x30000;
        if (!supportGlES30) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage("该设备不支持opengles 3.0")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.this.finish();
                        }
                    }).create();
            dialog.show();
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(new Button(MainActivity.this)) {
            };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            ((Button) holder.itemView).setText(names[position]);
            ((Button) holder.itemView).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, classes[position]);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return Math.min(classes.length, names.length);
        }
    }
}
