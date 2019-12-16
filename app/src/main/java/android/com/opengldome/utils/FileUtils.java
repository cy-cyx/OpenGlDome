package android.com.opengldome.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * create by cy
 * time : 2019/12/16
 * version : 1.0
 * Features :
 */
public class FileUtils {


    /**
     * 获得图片缓存路径
     */
    public static String getCacheBitmapPath(Context context) {
        return context.getCacheDir().getAbsolutePath() + File.separator + ".temp" + File.separator
                + System.currentTimeMillis() + ".jpg";
    }

    public static String getDCIMBitmapPath(Context context) {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "DCIM" +
                File.separator + System.currentTimeMillis() + ".jpg";
    }

    public static boolean copyFile(String oldPath, String newPath) {
        try {
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
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
