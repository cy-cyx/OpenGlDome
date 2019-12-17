package android.com.opengldome.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;

/**
 * create by cy
 * time : 2019/12/16
 * version : 1.0
 * Features : 管理输出与缓存路径
 */
public class FileUtils {

    /**
     * 获得图片缓存路径
     */
    public static String getCacheBitmapPath(Context context) {
        return getCachePath(context) + File.separator + ".temp" + File.separator
                + System.currentTimeMillis() + ".jpg";
    }

    private static String getCachePath(Context context) {
        String out;

        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir != null) {
            out = externalCacheDir.getAbsolutePath();
        } else {
            out = context.getCacheDir().getAbsolutePath();
        }

        return out;
    }

    public static String getDCIMBitmapPath(Context context) {
        return getPhotoSavePath() + File.separator + "DCIM" +
                File.separator + System.currentTimeMillis() + ".jpg";
    }

    private static String getPhotoSavePath() {
        String out;

        File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (dcim != null) {
            out = dcim.getAbsolutePath() + File.separator + "Camera";
        } else {
            out = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM" + File.separator + "Camera";
        }

        //魅族的默认相册路径不同，原来的路径图库不显示
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer != null) {
            manufacturer = manufacturer.toLowerCase(Locale.getDefault());
            if (manufacturer.contains("meizu")) {
                out = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Camera";
            }
        }

        // vivo路径特殊
        if ("vivo".equalsIgnoreCase(Build.MANUFACTURER)) {
            out = Environment.getExternalStorageDirectory().toString() + File.separator + "相机";
        }

        makeFolder(out);

        return out;
    }

    private static void makeFolder(String path) {
        try {
            if (path != null) {
                File file = new File(path);
                if (!(file.exists() && file.isDirectory())) {
                    file.mkdirs();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            makeFolder(newPath);

            FileInputStream fileInputStream = new FileInputStream(oldPath);
            FileOutputStream fileOutputStream = new FileOutputStream(newPath);
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = fileInputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            fileInputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
