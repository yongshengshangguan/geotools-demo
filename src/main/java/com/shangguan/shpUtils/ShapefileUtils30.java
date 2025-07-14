package com.shangguan.shpUtils;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ShapefileUtils30 {

    /**
     * 创建点图层Shapefile
     */
    public static void createPointShapefile(File file, List<Point> points) throws Exception {
        SimpleFeatureType TYPE = createFeatureType("PointLayer", Point.class);
        createShapefile(file, TYPE, points);
    }

    /**
     * 创建线图层Shapefile
     */
    public static void createLineShapefile(File file, List<LineString> lines) throws Exception {
        SimpleFeatureType TYPE = createFeatureType("LineLayer", LineString.class);
        createShapefile(file, TYPE, lines);
    }

    /**
     * 创建面图层Shapefile
     */
    public static void createPolygonShapefile(File file, List<Polygon> polygons) throws Exception {
        SimpleFeatureType TYPE = createFeatureType("PolygonLayer", Polygon.class);
        createShapefile(file, TYPE, polygons);
    }

    /**
     * 创建SimpleFeatureType
     * @param typeName 图层名称
     * @param geomClass 几何类型class
     */
    private static SimpleFeatureType createFeatureType(String typeName, Class<? extends Geometry> geomClass) throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(typeName);
        builder.setCRS(DefaultGeographicCRS.WGS84); // 设置坐标参考系WGS84
        builder.add("the_geom", geomClass);
        builder.add("name", String.class); // 添加一个属性字段
        return builder.buildFeatureType();
    }

    /**
     * 核心创建Shapefile方法，适用点、线、面
     */
    private static <T extends Geometry> void createShapefile(File file, SimpleFeatureType featureType, List<T> geometries) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("url", file.toURI().toURL());
        params.put("charset", StandardCharsets.UTF_8);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        DataStore dataStore = dataStoreFactory.createNewDataStore(params);
        dataStore.createSchema(featureType);

        try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer = dataStore.getFeatureWriterAppend(dataStore.getTypeNames()[0], Transaction.AUTO_COMMIT)) {
            int fid = 0;
            for (Geometry geom : geometries) {
                SimpleFeature feature = writer.next();
                feature.setAttribute("the_geom", geom);
                feature.setAttribute("name", geom.getGeometryType() + "_" + fid);
                writer.write();
                fid++;
            }
        }
        dataStore.dispose();
    }

    // 测试用例
    public static void main(String[] args) throws Exception {
        GeometryFactory gf = new GeometryFactory();
//
//        // 点数据
//        List<Point> points = Arrays.asList(
//                gf.createPoint(new Coordinate(116.397, 39.907)),
//                gf.createPoint(new Coordinate(121.4737, 31.2304))
//        );
//        createPointShapefile(new File("points.shp"), points);
//
//        // 线数据
//        List<LineString> lines = Arrays.asList(
//                gf.createLineString(new Coordinate[]{
//                        new Coordinate(0, 0),
//                        new Coordinate(10, 10),
//                        new Coordinate(20, 20)
//                })
//        );
//        createLineShapefile(new File("lines.shp"), lines);

        String geoStr = "[[[105.862561206,27.013139676],[105.862196425,27.0127892],[105.862539748,27.012703369],[105.862453917,27.012603234],[105.862253645,27.012538861],[105.862081984,27.012438725],[105.861989001,27.012560318],[105.861810188,27.012503098],[105.861931781,27.012360047],[105.861838798,27.012281368],[105.861574153,27.012674759],[105.86150978,27.012624691],[105.861423949,27.012717675],[105.861359576,27.0127892],[105.861431101,27.012875031],[105.861459712,27.012989472],[105.861516932,27.013132523],[105.861695746,27.013003777],[105.861795882,27.012896488],[105.861910323,27.012939404],[105.861981848,27.013032387],[105.862010459,27.013103913],[105.862074832,27.013168286],[105.862110594,27.013261269],[105.862124899,27.013354252],[105.862196425,27.013332795],[105.862353782,27.013289879],[105.862446764,27.013268422],[105.862561206,27.013139676]]]";
        List<Polygon> polygons = PolygonParser.parsePolygonListFromString(geoStr);
//        // 面数据
//        List<Polygon> polygons = Arrays.asList(
//                gf.createPolygon(new Coordinate[]{
//                        new Coordinate(0,0),
//                        new Coordinate(0,10),
//                        new Coordinate(10,10),
//                        new Coordinate(10,0),
//                        new Coordinate(0,0)
//                })
//        );
        createPolygonShapefile(new File("polygons1.shp"), polygons);

        System.out.println("三种类型Shapefile均已生成！");
    }
}
