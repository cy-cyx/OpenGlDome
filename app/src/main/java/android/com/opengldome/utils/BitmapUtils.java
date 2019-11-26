package android.com.opengldome.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
}
