package com.shangguan.utils;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.gce.geotiff.GeoTiffFormat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;

public class GeoTiffColorRenderer {

    public static void convertTiffToColorPng(File tifFile, File outputPngFile, double minVal, double maxVal) throws Exception {
        AbstractGridFormat format = new GeoTiffFormat();
        GridCoverage2DReader reader = format.getReader(tifFile);
        GridCoverage2D coverage = reader.read(null);

        Raster raster = coverage.getRenderedImage().getData();
        int width = raster.getWidth();
        int height = raster.getHeight();

        BufferedImage colorImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double value = raster.getSampleDouble(x, y, 0);
                Color color = ColorMapper.map(value, minVal, maxVal);
                colorImage.setRGB(x, y, color.getRGB());
            }
        }

        ImageIO.write(colorImage, "png", outputPngFile);
        System.out.println("✅ 生成成功：" + outputPngFile.getAbsolutePath());
    }
}
