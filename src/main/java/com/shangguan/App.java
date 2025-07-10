package com.shangguan;

import com.shangguan.utils.TifToPngConverter;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdal.Dataset;
import org.gdal.gdalconst.gdalconst;

import java.io.File;

public class App {

    public static void main(String[] args) {
        gdal.AllRegister();

        try {
            String tifUrl = "https://uav-static.nxzhnyyjy.com:9082/uav-static/map/202507040000003/index_map/GNDVI.tif";  // 替换成真实 URL
            String localTif = TifToPngConverter.downloadTif(tifUrl);

            String outputPng = "C:\\data\\png\\ONLINE_GNDVI.png"; // 输出路径
            TifToPngConverter.convertSingleFile(localTif, outputPng);

            // 转换完成后删除临时文件（如果你想）
            // new File(localTif).delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
