package com.shangguan.utils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.gce.geotiff.GeoTiffFormat;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;


public class GeoToolsImageCompressor {

    /**
     * 从 GeoTIFF 读取图像，并压缩保存为 JPG 或 PNG
     * @param inputTif 输入 GeoTIFF 文件路径
     * @param outputImage 输出图片路径（支持 .jpg / .png）
     * @param formatName 格式名称："jpg" 或 "png"
     * @param quality 图像质量（0.0 ~ 1.0，JPG 有效）
     */
    public static void compressTifToImage(String inputTif, String outputImage, String formatName, float quality) {
        try {
            File tifFile = new File(inputTif);
            AbstractGridFormat format = new GeoTiffFormat();
            GridCoverage2DReader reader = format.getReader(tifFile);
            GridCoverage2D coverage = reader.read(null);
            RenderedImage image = coverage.getRenderedImage();

            File outFile = new File(outputImage);
            try (FileOutputStream fos = new FileOutputStream(outFile);
                 ImageOutputStream ios = ImageIO.createImageOutputStream(fos)) {

                Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(formatName);
                if (!writers.hasNext()) {
                    throw new IllegalStateException("No writers found for format: " + formatName);
                }

                ImageWriter writer = writers.next();
                writer.setOutput(ios);

                ImageWriteParam param = writer.getDefaultWriteParam();
                if ("jpg".equalsIgnoreCase(formatName) || "jpeg".equalsIgnoreCase(formatName)) {
                    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    param.setCompressionQuality(quality);  // 0.0 = max compression, 1.0 = best quality
                }

                writer.write(null, new IIOImage(image, null, null), param);
                writer.dispose();
                System.out.println("压缩成功: " + outputImage);
            }
        } catch (Exception e) {
            System.err.println("压缩失败: " + inputTif);
            e.printStackTrace();
        }
    }

}
