package android.com.opengldome.watermark;

import android.com.opengldome.Application;

class WaterMarkRender {

    private WaterMarkOesFilter oesFilter;
    private WaterMarkFilter waterMarkFilter;

    void onSurfaceCreate() {
        oesFilter = new WaterMarkOesFilter(Application.getInstance());
        waterMarkFilter = new WaterMarkFilter(Application.getInstance());
    }

    public void onSurfaceChange(int width, int height, int rotation) {
        waterMarkFilter.upDataMatrix(width, height, rotation);
    }

    void onDraw(int src, int width, int height) {
        oesFilter.onDraw(src, width, height);
        waterMarkFilter.onDraw(width, height);
    }
}
