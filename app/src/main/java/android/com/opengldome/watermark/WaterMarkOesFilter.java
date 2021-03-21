package android.com.opengldome.watermark;

import android.com.opengldome.R;
import android.com.opengldome.beauty.AFilter;
import android.content.Context;

/**
 * 用于获得视频帧数据画基础帧数据
 */
class WaterMarkOesFilter extends AFilter {

    WaterMarkOesFilter(Context context) {
        super(context, R.raw.watermark_base_vert, R.raw.watermark_base_frag);
    }

}
