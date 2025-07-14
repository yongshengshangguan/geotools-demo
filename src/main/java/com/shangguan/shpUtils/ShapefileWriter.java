package com.shangguan.shpUtils;

import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ShapefileWriter {

    public enum GeometryType {
        POINT, LINESTRING, POLYGON
    }

    public static File writeShapefileWithZip(
            String baseFileName,
            GeometryType geometryType,
            List<Map<String, Object>> data,
            Map<String, Class<?>> attributeTypes,
            String geometryFieldName
    ) throws Exception {

        File tempDir = Files.createTempDirectory("shp_layer").toFile();
        File shpFile = new File(tempDir, baseFileName + ".shp");

        // 1. 定义图层结构
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName(baseFileName);
        typeBuilder.setCRS(DefaultGeographicCRS.WGS84);

        // 添加几何字段
        switch (geometryType) {
            case POINT:
                typeBuilder.add(geometryFieldName, Point.class);
                break;
            case LINESTRING:
                typeBuilder.add(geometryFieldName, LineString.class);
                break;
            case POLYGON:
                typeBuilder.add(geometryFieldName, Polygon.class);
                break;
        }

        for (Map.Entry<String, Class<?>> entry : attributeTypes.entrySet()) {
            if (!entry.getKey().equals(geometryFieldName)) {
                typeBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        SimpleFeatureType featureType = typeBuilder.buildFeatureType();

        // 2. 构建 Feature
        List<SimpleFeature> features = new ArrayList<>();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        int fid = 0;
        for (Map<String, Object> row : data) {
            for (String key : row.keySet()) {
                featureBuilder.set(key, row.get(key));
            }
            features.add(featureBuilder.buildFeature("fid." + (fid++)));
        }

        // 3. 写入 SHP
        Map<String, Object> params = new HashMap<>();
        params.put("url", shpFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
        ShapefileDataStore dataStore = (ShapefileDataStore) factory.createNewDataStore(params);
        dataStore.setCharset(StandardCharsets.UTF_8);
        dataStore.createSchema(featureType);

        Transaction transaction = new DefaultTransaction("create");
        try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                     dataStore.getFeatureWriterAppend(dataStore.getTypeNames()[0], transaction)) {
            for (SimpleFeature feature : features) {
                SimpleFeature toWrite = writer.next();
                for (int i = 0; i < feature.getAttributeCount(); i++) {
                    toWrite.setAttribute(i, feature.getAttribute(i));
                }
                toWrite.setDefaultGeometry(feature.getDefaultGeometry());
                writer.write();
            }
            transaction.commit();
        } catch (Exception e) {
            transaction.rollback();
            throw e;
        } finally {
            transaction.close();
            dataStore.dispose();
        }

        // 4. 打包 ZIP
        File zipFile = new File(tempDir, baseFileName + ".zip");
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (String suffix : Arrays.asList(".shp", ".shx", ".dbf", ".prj", ".cpg")) {
                File f = new File(tempDir, baseFileName + suffix);
                if (f.exists()) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        zos.putNextEntry(new ZipEntry(f.getName()));
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) >= 0) {
                            zos.write(buffer, 0, length);
                        }
                        zos.closeEntry();
                    }
                }
            }
        }

        return zipFile; // 可用于上传 MinIO
    }

    // 示例: 写入一个点图层并上传
    public static void main(String[] args) throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();
        GeometryFactory factory = new GeometryFactory();

        Map<String, Object> row1 = new HashMap<>();
        row1.put("geom", factory.createPoint(new Coordinate(105, 30)));
        row1.put("name", "点A");
        data.add(row1);

        Map<String, Object> row2 = new HashMap<>();
        row2.put("geom", factory.createPoint(new Coordinate(106, 31)));
        row2.put("name", "点B");
        data.add(row2);

        Map<String, Class<?>> attrTypes = new LinkedHashMap<>();
        attrTypes.put("geom", Point.class);
        attrTypes.put("name", String.class);

        File zip = writeShapefileWithZip("point_layer", GeometryType.POINT, data, attrTypes, "geom");
        System.out.println("✅ ZIP路径：" + zip.getAbsolutePath());
        // 可使用 minioTools.uploadFile(zip, "shapefile.zip", "shp/zip/");
    }
}
