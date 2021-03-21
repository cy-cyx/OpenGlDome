package android.com.opengldome.watermark;

import android.com.opengldome.Application;

class WaterMarkRender {

    private WaterMarkOesFilter oesFilter;

    void onSurfaceCreate() {
        oesFilter = new WaterMarkOesFilter(Application.getInstance());
    }

    void onDraw(int src, int width, int height) {
        oesFilter.onDraw(src, width, height);
    }
}
