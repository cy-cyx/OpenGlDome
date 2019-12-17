package android.com.opengldome.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * create by cy
 * time : 2019/11/26
 * version : 1.0
 * Features :
 */
public class BitmapUtils {

    public static Bitmap getBitmapByAsset(Context context, String resName) {
        return BitmapFactory.decodeStream(getAssetsStream(context, resName));
    }

    /**
     * 打开Assets流
     */
    private static InputStream getAssetsStream(Context context, String resName) {
        if (resName == null || resName.trim().equals("")) {
            return null;
        }
        AssetManager asset = context.getAssets();
        InputStream is = null;
        try {
            is = asset.open(resName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }

    public static void saveBitmap(Context context, String path, Bitmap bitmap,
                                  boolean addToMediaStore) {
        final File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 添加到媒体库
        if (addToMediaStore) {
            addToMediaStore(context, path);
        }
    }

    public static void addToMediaStore(Context context, String path) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, path);
        File file = new File(path);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
        context.getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
