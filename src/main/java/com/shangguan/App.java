package com.shangguan;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.data.simple.SimpleFeatureIterator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class App {
    public static void main(String[] args) throws Exception {
        // 请将shapefile的路径替换为你自己的文件路径
        String shapefilePath = "data/your-shapefile.shp";

        File file = new File(shapefilePath);
        if (!file.exists()) {
            System.out.println("Shapefile 文件不存在: " + file.getAbsolutePath());
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(params);
        if (dataStore == null) {
            System.out.println("无法加载Shapefile数据源！");
            return;
        }

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        System.out.println("要素类型: " + typeName);
        System.out.println("要素总数: " + featureSource.getCount(null));

        try (SimpleFeatureIterator features = featureSource.getFeatures().features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                System.out.println(feature.getID() + ": " + feature.getDefaultGeometry());
            }
        }

        dataStore.dispose();
    }
}
