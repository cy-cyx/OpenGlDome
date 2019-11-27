package android.com.opengldome.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * create by cy
 * time ： 2019.03.15
 * version : 1.0
 * Features : 权限管理类
 */
public class PermissionUtil {

    private static IPermissionCallback sPermissionCallback;
    private static String[] sPermissions;

    public static void requestRuntimePermissions(Context context, String[] permissions, IPermissionCallback permissionCallback) {
        if (context == null || permissions == null) {
            return;
        }
        sPermissionCallback = permissionCallback;
        sPermissions = permissions;
        requestPermissions(context, sPermissions);
    }

    private static void requestPermissions(Context context, String[] permissions) {
        List<String> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission);
            }
        }
        // 申请权限
        if (!permissionList.isEmpty()) {

            // 检查有没有拒接的权限
            List<String> refusePermissionList = new ArrayList<>();
            for (int i = 0; i < permissionList.size(); i++) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permissionList.get(i))) {
                    refusePermissionList.add(permissionList.get(i));
                }
            }

            // 不完整（拒绝权限未处理）


            ActivityCompat.requestPermissions((Activity) context,
                    permissionList.toArray(new String[permissionList.size()]), 4396);

        } else {
            // 拥有权限
            if (sPermissionCallback != null) {
                sPermissionCallback.nextStep();
            }
            sPermissionCallback = null;
            sPermissions = null;
        }
    }

    public static void onRequestPermissionsResult(Context context, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 4396) {
            if (sPermissions != null) {
                requestPermissions(context, sPermissions);
            }
        }
    }

    public static void gotoSetPage(Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getApplicationContext().getPackageName(), null));
        ((Activity) context).startActivityForResult(intent, 77777);
    }

    public static void OnActivityResult(Context context, int requestCode, int resultCode, Intent data) {
        if (resultCode == 77777) {
            if (sPermissions != null) {
                requestPermissions(context, sPermissions);
            }
        }
    }

    public interface IPermissionCallback {
        public void nextStep();

        public void cancel();
    }
}
