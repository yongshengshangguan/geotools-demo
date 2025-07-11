package com.shangguan;

import com.shangguan.utils.GeoToolsImageCompressor;
import com.shangguan.utils.TifToPngConverter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.io.File;

public class App {

    public static void main(String[] args) {
//        gdal.AllRegister();

        try {
            String tifUrl = "https://uav-static.nxzhnyyjy.com:9082/uav-static/map/202507040000003/index_map_color/GNDVI_local.tif";  // 替换成真实 URL
//            String localTif = TifToPngConverter.downloadTif(tifUrl);

            String outputPng = "D:\\QgisData\\png\\GNDVI_local1.png"; // 输出路径
            String outputPngyasuo = "D:\\QgisData\\png\\GNDVI_local2_yasuo.png"; // 输出路径2
//            String outputJpg = "D:\\QgisData\\png\\GNDVI_local.jpg"; // 输出路径
//            TifToPngConverter.convertSingleFile(localTif, outputPng);

            // 输出 PNG（尽量压缩，无损）
//            GeoToolsImageCompressor.compressTifToImage(localTif, outputPng, "png", 1.0f);


            File input = new File(outputPng);
            BufferedImage originalImage = ImageIO.read(input);

            // 缩小到原图的 50%
            int width = originalImage.getWidth() / 8;
            int height = originalImage.getHeight() / 8;

            BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, width, height, null);
            g2d.dispose();

            File output = new File(outputPngyasuo);
            ImageIO.write(scaledImage, "png", output);


//            // 输出 JPEG（有损压缩，0.6质量）
//            GeoToolsImageCompressor.compressTifToImage(localTif, outputJpg, "jpg", 0.6f);

//            File input = new File(localTif);      // 替换为你的 GeoTIFF 路径
//            File output = new File(outputPng);     // 输出 PNG 路径
//
//            double minValue = 0;     // 根据你的数据设置（如 DEM 可设为 0）
//            double maxValue = 3000;  // DEM 最大值，或图像像素值上限
//
//            GeoTiffColorRenderer.convertTiffToColorPng(input, output, minValue, maxValue);
            // 转换完成后删除临时文件（如果你想）
//             new File(localTif).delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
