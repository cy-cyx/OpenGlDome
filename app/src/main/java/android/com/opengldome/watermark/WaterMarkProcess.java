package android.com.opengldome.watermark;

/**
 * create by caiyx in 2021/3/19
 * <p>
 * 添加视频水印的主线程
 */
public class WaterMarkProcess implements Runnable {

    private String videoPath;

    WaterMarkProcess(String path) {
        videoPath = path;
    }

    @Override
    public void run() {

    }
}
