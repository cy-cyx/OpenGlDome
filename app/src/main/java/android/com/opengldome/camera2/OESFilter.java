package android.com.opengldome.camera2;

import android.com.opengldome.R;
import android.com.opengldome.beauty.AFilter;
import android.content.Context;

/**
 * create by cy
 * time : 2019/11/29
 * version : 1.0
 * Features :
 */
public class OESFilter extends AFilter {

    public OESFilter(Context context) {
        super(context, R.raw.oes_vert, R.raw.oes_frag);
    }

}
