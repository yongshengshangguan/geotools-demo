package com.shangguan.utils;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URL;

import java.io.File;

public class TifToPngConverter {

    static {
        gdal.AllRegister();
    }

    /**
     * 转换单个 TIF 文件为 PNG
     * @param inputPath 输入文件路径（本地）
     * @param outputPath 输出 PNG 文件路径
     */
    public static void convertSingleFile(String inputPath, String outputPath) {
        Dataset inDs = gdal.Open(inputPath);
        if (inDs == null) {
            System.err.println("无法打开文件：" + inputPath);
            return;
        }

        int width = inDs.getRasterXSize();
        int height = inDs.getRasterYSize();
        int bandCount = inDs.getRasterCount();

        if (bandCount < 3) {
            System.out.println("只有 " + bandCount + " 个波段，继续灰度模式");
            // 回退灰度转换逻辑...
            inDs.delete();
            return;
        }

        // 读取 RGB 波段数据（前3个波段）
        float[][] rgbFloat = new float[3][width * height];
        byte[][] rgbByte = new byte[3][width * height];
        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;

        for (int b = 0; b < 3; b++) {
            inDs.GetRasterBand(b + 1).ReadRaster(0, 0, width, height, rgbFloat[b]);
            for (float v : rgbFloat[b]) {
                if (!Float.isNaN(v)) {
                    if (v < min) min = v;
                    if (v > max) max = v;
                }
            }
        }

        float range = (max - min == 0) ? 1 : (max - min);

        for (int b = 0; b < 3; b++) {
            for (int i = 0; i < rgbFloat[b].length; i++) {
                float val = rgbFloat[b][i];
                if (Float.isNaN(val)) val = min;
                rgbByte[b][i] = (byte) Math.max(0, Math.min(255, ((val - min) / range) * 255));
            }
        }

        Driver memDriver = gdal.GetDriverByName("MEM");
        Driver pngDriver = gdal.GetDriverByName("PNG");

        Dataset memDs = memDriver.Create("", width, height, 3, gdalconst.GDT_Byte);
        for (int b = 0; b < 3; b++) {
            memDs.GetRasterBand(b + 1).WriteRaster(0, 0, width, height, rgbByte[b]);
        }

        Dataset outDs = pngDriver.CreateCopy(outputPath, memDs);
        if (outDs != null) {
            System.out.println("彩色转换成功：" + outputPath);
            outDs.delete();
        } else {
            System.err.println("PNG 转换失败");
        }

        inDs.delete();
        memDs.delete();
    }

    /**
     * 批量转换目录中所有 tif 文件
     * @param inputDir 输入目录
     * @param outputDir 输出目录
     */
    public static void convertBatch(String inputDir, String outputDir) {
        new File(outputDir).mkdirs();
        File[] tifFiles = new File(inputDir).listFiles((dir, name) -> name.toLowerCase().endsWith(".tif"));
        if (tifFiles == null || tifFiles.length == 0) {
            System.out.println("未找到任何 .tif 文件");
            return;
        }
        for (File tifFile : tifFiles) {
            String outputPath = outputDir + File.separator + removeExtension(tifFile.getName()) + ".png";
            convertSingleFile(tifFile.getAbsolutePath(), outputPath);
        }
        System.out.println("批量转换完成！");
    }

    private static String removeExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        return (i > 0) ? fileName.substring(0, i) : fileName;
    }

    /**
     * 下载远程 tif 文件到本地临时目录，返回本地路径
     */
    public static String downloadTif(String urlString) throws Exception {
        URL url = new URL(urlString);
        String tempFileName = "temp_" + System.currentTimeMillis() + ".tif";
        File tempFile = new File(System.getProperty("java.io.tmpdir"), tempFileName);

        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
             FileOutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        System.out.println("下载完成：" + tempFile.getAbsolutePath());
        return tempFile.getAbsolutePath();
    }

//    public static void main(String[] args) {
//        try {
//            String tifUrl = "http://example.com/your-image.tif";  // 替换成真实 URL
//            String localTif = downloadTif(tifUrl);
//
//            String outputPng = "C:\\data\\png\\converted.png"; // 输出路径
//            TifToPngConverter.convertSingleFile(localTif, outputPng);
//
//            // 转换完成后删除临时文件（如果你想）
//            // new File(localTif).delete();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
