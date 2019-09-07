package android.com.opengldome;

import android.content.Context;

public class Application extends android.app.Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getInstance(){
        return mContext;
    }
}
