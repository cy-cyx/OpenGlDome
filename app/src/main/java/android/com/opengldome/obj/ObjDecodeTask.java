package android.com.opengldome.obj;

import android.com.opengldome.Application;
import android.com.opengldome.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 解obj文件线程
 */
public class ObjDecodeTask implements Runnable {

    float maxUnit = 0; // 找piont点最大值，正负中的最大，用于归一

    @Override
    public void run() {
        InputStream is = null;
        ArrayList<Float> points = new ArrayList<>();
        ArrayList<Float> realPoints = new ArrayList<>();  // 点
        ArrayList<Float> realNormal = new ArrayList<>();  // 法线
        ArrayList<Float> realCoord = new ArrayList<>();    // 纹理

        ArrayList<String> flats = new ArrayList<>(); // 面信息

        ArrayList<Float> result = new ArrayList<>(); // 放点1/1/1 纹理3/3 法线2/2/2

        try {
            is = Application.getInstance().getResources().openRawResource(R.raw.dog);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String temps = null;
            while ((temps = br.readLine()) != null) {
                String[] split = temps.split("[ ]+");
                if (split[0].trim().equals("v")) {
                    points.add(findMax(Float.valueOf(split[1])));
                    points.add(findMax(Float.valueOf(split[2])));
                    points.add(findMax(Float.valueOf(split[3])));
                } else if (split[0].trim().equals("vn")) {
                    realNormal.add(Float.valueOf(split[1]));
                    realNormal.add(Float.valueOf(split[2]));
                    realNormal.add(Float.valueOf(split[3]));
                } else if (split[0].trim().equals("vt")) {
                    realCoord.add(Float.valueOf(split[1]));
                    realCoord.add(Float.valueOf(split[2]));
                    realCoord.add(Float.valueOf(split[3]));
                } else if (split[0].trim().equals("f")) {
                    flats.add(temps);
                }
            }

            // 归一
            for (Float f : points) {
                realPoints.add(f / maxUnit);
            }

            // 将点信息整理在一个数值里 注意obj中f(面)不止只有3个点
            for (String flat : flats) {
                String[] split = flat.split("[ ]+");

                int point = split.length - 1;

                // 例如4点构成 0123 就把它拆成两三角形 012 023
                for (int i = 0; i < point - 2; i++) {
                    // 第一个点  永远是第1个(去掉f)
                    String[] part = split[1].split("/");
                    // 点  纹理 法线
                    int pot = Integer.valueOf(part[0]) - 1; //由于不是从0开始的
                    result.add(realPoints.get(pot * 3));
                    result.add(realPoints.get(pot * 3 + 1));
                    result.add(realPoints.get(pot * 3 + 2));

                    int coord = Integer.valueOf(part[1]) - 1;
                    result.add(realCoord.get(coord * 3));
                    result.add(realCoord.get(coord * 3 + 1));

                    int normal = Integer.valueOf(part[2]) - 1;
                    result.add(realNormal.get(normal * 3));
                    result.add(realNormal.get(normal * 3 + 1));
                    result.add(realNormal.get(normal * 3 + 2));

                    // 第二个点
                    part = split[2 + i].split("/");
                    // 点  纹理 法线
                    pot = Integer.valueOf(part[0]) - 1; //由于不是从0开始的
                    result.add(realPoints.get(pot * 3));
                    result.add(realPoints.get(pot * 3 + 1));
                    result.add(realPoints.get(pot * 3 + 2));

                    coord = Integer.valueOf(part[1]) - 1;
                    result.add(realCoord.get(coord * 3));
                    result.add(realCoord.get(coord * 3 + 1));

                    normal = Integer.valueOf(part[2]) - 1;
                    result.add(realNormal.get(normal * 3));
                    result.add(realNormal.get(normal * 3 + 1));
                    result.add(realNormal.get(normal * 3 + 2));

                    // 第三个点
                    part = split[3 + i].split("/");
                    // 点  纹理 法线
                    pot = Integer.valueOf(part[0]) - 1; //由于不是从0开始的
                    result.add(realPoints.get(pot * 3));
                    result.add(realPoints.get(pot * 3 + 1));
                    result.add(realPoints.get(pot * 3 + 2));

                    coord = Integer.valueOf(part[1]) - 1;
                    result.add(realCoord.get(coord * 3));
                    result.add(realCoord.get(coord * 3 + 1));

                    normal = Integer.valueOf(part[2]) - 1;
                    result.add(realNormal.get(normal * 3));
                    result.add(realNormal.get(normal * 3 + 1));
                    result.add(realNormal.get(normal * 3 + 2));
                }
            }

            // 转成数组

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 点不一定是以1为标准的
     */
    private float findMax(float point) {
        float abs = Math.abs(point);
        if (abs > maxUnit) {
            maxUnit = abs;
        }
        return point;
    }
}
