package com.shangguan.shpUtils;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKBReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author: shangguan
 * @CreateTime: 2025-07-14 (Updated)
 * @Description: 强大的Shapefile工具类，支持创建、读取点/线/面Shapefile，自定义属性，WKT/WKB解析，并打包为zip文件。
 */
public class ShapefileUtils2 {

    private static final Logger logger = LoggerFactory.getLogger(ShapefileUtils2.class);
    private static final GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory(null);

    // 默认的坐标参考系
    private static final CoordinateReferenceSystem DEFAULT_CRS;

    static {
        CoordinateReferenceSystem crs = null;
        try {
            // 尝试使用EPSG:4326 (WGS84)作为默认CRS
            crs = CRS.decode("EPSG:4326");
        } catch (Exception e) {
            logger.error("无法解码默认CRS EPSG:4326，ShapefileUtils2可能无法正常工作。", e);
        }
        DEFAULT_CRS = crs;
    }

    /**
     * 定义一个属性字段的结构
     */
    public static class AttributeDescriptor {
        public String name;
        public Class<?> type;

        public AttributeDescriptor(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }
    }

    /**
     * 创建点图层Shapefile
     *
     * @param file        输出的shp文件路径
     * @param points      点几何对象列表
     * @param attributesList 每个点的属性列表，列表中每个Map对应一个点的属性，key为属性名，value为属性值
     * @param crs         坐标参考系，如果为null则使用默认WGS84
     * @throws IOException 如果写入失败
     */
    public static void createPointShapefile(File file, List<Point> points, List<Map<String, Object>> attributesList, CoordinateReferenceSystem crs) throws IOException {
        List<AttributeDescriptor> defaultAttrs = Arrays.asList(new AttributeDescriptor("name", String.class));
        SimpleFeatureType featureType = createFeatureType("PointLayer", Point.class, attributesList, defaultAttrs, crs);
        createShapefile(file, featureType, points, attributesList);
    }

    /**
     * 创建线图层Shapefile
     *
     * @param file        输出的shp文件路径
     * @param lines       线几何对象列表
     * @param attributesList 每条线的属性列表
     * @param crs         坐标参考系，如果为null则使用默认WGS84
     * @throws IOException 如果写入失败
     */
    public static void createLineShapefile(File file, List<LineString> lines, List<Map<String, Object>> attributesList, CoordinateReferenceSystem crs) throws IOException {
        List<AttributeDescriptor> defaultAttrs = Arrays.asList(new AttributeDescriptor("name", String.class));
        SimpleFeatureType featureType = createFeatureType("LineLayer", LineString.class, attributesList, defaultAttrs, crs);
        createShapefile(file, featureType, lines, attributesList);
    }

    /**
     * 创建面图层Shapefile
     *
     * @param file        输出的shp文件路径
     * @param polygons    面几何对象列表
     * @param attributesList 每个面的属性列表
     * @param crs         坐标参考系，如果为null则使用默认WGS84
     * @throws IOException 如果写入失败
     */
    public static void createPolygonShapefile(File file, List<Polygon> polygons, List<Map<String, Object>> attributesList, CoordinateReferenceSystem crs) throws IOException {
        List<AttributeDescriptor> defaultAttrs = Arrays.asList(new AttributeDescriptor("name", String.class));
        SimpleFeatureType featureType = createFeatureType("PolygonLayer", Polygon.class, attributesList, defaultAttrs, crs);
        createShapefile(file, featureType, polygons, attributesList);
    }

    /**
     * 创建多点图层Shapefile
     *
     * @param file         输出的shp文件路径
     * @param multiPoints  多点几何对象列表
     * @param attributesList 每个多点的属性列表
     * @param crs          坐标参考系，如果为null则使用默认WGS84
     * @throws IOException 如果写入失败
     */
    public static void createMultiPointShapefile(File file, List<MultiPoint> multiPoints, List<Map<String, Object>> attributesList, CoordinateReferenceSystem crs) throws IOException {
        List<AttributeDescriptor> defaultAttrs = Arrays.asList(new AttributeDescriptor("name", String.class));
        SimpleFeatureType featureType = createFeatureType("MultiPointLayer", MultiPoint.class, attributesList, defaultAttrs, crs);
        createShapefile(file, featureType, multiPoints, attributesList);
    }

    /**
     * 创建多线图层Shapefile
     *
     * @param file          输出的shp文件路径
     * @param multiLines    多线几何对象列表
     * @param attributesList 每个多线的属性列表
     * @param crs           坐标参考系，如果为null则使用默认WGS84
     * @throws IOException 如果写入失败
     */
    public static void createMultiLineShapefile(File file, List<MultiLineString> multiLines, List<Map<String, Object>> attributesList, CoordinateReferenceSystem crs) throws IOException {
        List<AttributeDescriptor> defaultAttrs = Arrays.asList(new AttributeDescriptor("name", String.class));
        SimpleFeatureType featureType = createFeatureType("MultiLineLayer", MultiLineString.class, attributesList, defaultAttrs, crs);
        createShapefile(file, featureType, multiLines, attributesList);
    }

    /**
     * 创建多面图层Shapefile
     *
     * @param file          输出的shp文件路径
     * @param multiPolygons 多面几何对象列表
     * @param attributesList 每个多面的属性列表
     * @param crs           坐标参考系，如果为null则使用默认WGS84
     * @throws IOException 如果写入失败
     */
    public static void createMultiPolygonShapefile(File file, List<MultiPolygon> multiPolygons, List<Map<String, Object>> attributesList, CoordinateReferenceSystem crs) throws IOException {
        List<AttributeDescriptor> defaultAttrs = Arrays.asList(new AttributeDescriptor("name", String.class));
        SimpleFeatureType featureType = createFeatureType("MultiPolygonLayer", MultiPolygon.class, attributesList, defaultAttrs, crs);
        createShapefile(file, featureType, multiPolygons, attributesList);
    }

    /**
     * 核心创建SimpleFeatureType方法
     *
     * @param typeName         图层名称
     * @param geomClass        几何类型class
     * @param attributesList   所有要素的属性列表，用于推断所有可能出现的属性字段
     * @param defaultAttributes 如果attributesList为空，则使用的默认属性列表
     * @param crs              坐标参考系，如果为null则使用默认WGS84
     * @return SimpleFeatureType
     * @throws IOException 如果CRS解码失败
     */
    private static SimpleFeatureType createFeatureType(String typeName, Class<? extends Geometry> geomClass,
                                                       List<Map<String, Object>> attributesList,
                                                       List<AttributeDescriptor> defaultAttributes,
                                                       CoordinateReferenceSystem crs) throws IOException {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(typeName);
        builder.setCRS(crs != null ? crs : DEFAULT_CRS); // 使用传入的CRS或默认WGS84

        // 添加几何属性
        builder.add("the_geom", geomClass);

        // 收集所有属性名和类型
        Set<String> fieldNames = new HashSet<>();
        Map<String, Class<?>> fieldTypes = new HashMap<>();

        if (attributesList != null) {
            for (Map<String, Object> attrs : attributesList) {
                if (attrs != null) {
                    for (Map.Entry<String, Object> entry : attrs.entrySet()) {
                        String attrName = entry.getKey();
                        Object attrValue = entry.getValue();
                        if (attrName != null && !fieldNames.contains(attrName)) {
                            fieldNames.add(attrName);
                            // 尝试确定属性类型，如果value为null，则默认为String.class
                            fieldTypes.put(attrName, attrValue != null ? attrValue.getClass() : String.class);
                        }
                    }
                }
            }
        }

        // 如果没有提供属性列表，使用默认属性
        if (fieldNames.isEmpty() && defaultAttributes != null) {
            for (AttributeDescriptor desc : defaultAttributes) {
                builder.add(desc.name, desc.type);
            }
        } else {
            // 添加推断出的所有属性
            for (String fieldName : fieldNames) {
                builder.add(fieldName, fieldTypes.get(fieldName));
            }
        }

        logger.info("Created FeatureType '{}' with geometry '{}' and attributes: {}", typeName, geomClass.getSimpleName(), fieldNames.isEmpty() ? "Default" : fieldNames);
        return builder.buildFeatureType();
    }


    /**
     * 核心创建Shapefile方法，适用点、线、面及复合几何类型
     *
     * @param file           输出的shp文件路径
     * @param featureType    要素类型定义
     * @param geometries     几何对象列表
     * @param attributesList 对应每个几何对象的属性列表，顺序应与geometries一致
     * @throws IOException 如果写入失败
     */
    private static <T extends Geometry> void createShapefile(File file, SimpleFeatureType featureType, List<T> geometries, List<Map<String, Object>> attributesList) throws IOException {
        // 创建文件父目录（如果不存在）
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                logger.error("无法创建Shapefile的输出目录: {}", parentDir.getAbsolutePath());
                throw new IOException("无法创建Shapefile的输出目录: " + parentDir.getAbsolutePath());
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("url", file.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE); // 创建空间索引
        params.put("charset", StandardCharsets.UTF_8); // 设置字符编码

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        DataStore dataStore = null;
        try {
            dataStore = dataStoreFactory.createNewDataStore(params);
            dataStore.createSchema(featureType); // 创建Schema

            logger.info("开始写入Shapefile: {}", file.getAbsolutePath());
            try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer = dataStore.getFeatureWriterAppend(dataStore.getTypeNames()[0], Transaction.AUTO_COMMIT)) {
                for (int i = 0; i < geometries.size(); i++) {
                    T geom = geometries.get(i);
                    Map<String, Object> currentAttrs = (attributesList != null && i < attributesList.size()) ? attributesList.get(i) : null;

                    SimpleFeature feature = writer.next();
                    feature.setAttribute("the_geom", geom); // 设置几何属性

                    // 设置其他自定义属性
                    if (currentAttrs != null) {
                        for (Map.Entry<String, Object> entry : currentAttrs.entrySet()) {
                            try {
                                feature.setAttribute(entry.getKey(), entry.getValue());
                            } catch (IllegalArgumentException e) {
                                // 如果属性类型不匹配或属性不存在Schema中，会抛出此异常
                                logger.warn("跳过属性 '{}'，因为其不符合Schema或类型不匹配: {}", entry.getKey(), e.getMessage());
                            }
                        }
                    } else {
                        // 如果没有提供属性列表，尝试设置一个默认的name属性
                        try {
                            feature.setAttribute("name", geom.getGeometryType() + "_" + i);
                        } catch (IllegalArgumentException e) {
                            // 如果Schema中没有'name'属性，则忽略
                            logger.debug("Schema中没有'name'属性，无法设置默认名称。");
                        }
                    }
                    writer.write();
                }
                logger.info("Shapefile写入完成，共写入 {} 个要素。", geometries.size());
            }
        } catch (IOException e) {
            logger.error("创建或写入Shapefile失败: {}", file.getAbsolutePath(), e);
            throw e;
        } finally {
            if (dataStore != null) {
                dataStore.dispose(); // 释放资源
                logger.debug("DataStore资源已释放。");
            }
        }
    }

    /**
     * 将同名的shapefile相关文件打包为zip
     *
     * @param baseName 文件基础名（如"polygon1"，不带后缀），例如：/path/to/my_shapefile
     * @param zipName  要生成的压缩包名称（如"my_shapefile.zip"），例如：/path/to/my_shapefile.zip
     * @throws IOException IO异常
     */
    public static void zipShapefile(String baseName, String zipName) throws IOException {
        // shapefile 相关扩展名
        String[] extensions = {".shp", ".shx", ".dbf", ".prj", ".cpg", ".qix", ".fix", ".sbn", ".sbx"};
        File zipFile = new File(zipName);
        // 确保zip文件目录存在
        File parentDir = zipFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                logger.error("无法创建ZIP文件的输出目录: {}", parentDir.getAbsolutePath());
                throw new IOException("无法创建ZIP文件的输出目录: " + parentDir.getAbsolutePath());
            }
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (String ext : extensions) {
                File f = new File(baseName + ext);
                if (f.exists()) {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        zos.putNextEntry(new ZipEntry(f.getName())); // 只保留文件名，不带路径
                        byte[] buffer = new byte[4096]; // 增加缓冲区大小
                        int len;
                        while ((len = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeEntry();
                        logger.debug("已将文件 '{}' 添加到ZIP。", f.getName());
                    }
                } else {
                    logger.debug("文件 '{}' 不存在，跳过压缩。", f.getName());
                }
            }
            logger.info("压缩完成，生成文件：{}", zipFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("压缩Shapefile失败: {}", zipName, e);
            throw e;
        }
    }

    /**
     * 从WKT（Well-Known Text）字符串解析几何对象
     *
     * @param wkt WKT字符串
     * @return 几何对象
     * @throws com.vividsolutions.jts.io.ParseException 如果WKT字符串格式不正确
     */
    public static Geometry parseGeometryFromWKT(String wkt) throws org.locationtech.jts.io.ParseException {
        WKTReader reader = new WKTReader(GEOMETRY_FACTORY);
        return reader.read(wkt);
    }

    /**
     * 从WKB（Well-Known Binary）字节数组解析几何对象
     *
     * @param wkbBytes WKB字节数组
     * @return 几何对象
     * @throws com.vividsolutions.jts.io.ParseException 如果WKB字节数组格式不正确
     */
    public static Geometry parseGeometryFromWKB(byte[] wkbBytes) throws org.locationtech.jts.io.ParseException {
        WKBReader reader = new WKBReader(GEOMETRY_FACTORY);
        return reader.read(wkbBytes);
    }

    /**
     * 读取Shapefile中的所有要素
     *
     * @param shpFile Shapefile文件对象
     * @return 包含所有SimpleFeature的列表
     * @throws IOException 如果读取失败
     */
    public static List<SimpleFeature> readShapefile(File shpFile) throws IOException {
        List<SimpleFeature> features = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();
        params.put("url", shpFile.toURI().toURL());
        params.put("charset", StandardCharsets.UTF_8);

        DataStore dataStore = null;
        try {
            dataStore = DataStoreFinder.getDataStore(params);
            if (dataStore == null) {
                throw new IOException("无法打开Shapefile数据存储: " + shpFile.getAbsolutePath());
            }
            String typeName = dataStore.getTypeNames()[0];
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures(Filter.INCLUDE);

            logger.info("开始读取Shapefile: {}", shpFile.getAbsolutePath());
            try (FeatureIterator<SimpleFeature> iterator = collection.features()) {
                while (iterator.hasNext()) {
                    features.add(iterator.next());
                }
            }
            logger.info("Shapefile读取完成，共读取 {} 个要素。", features.size());
        } catch (IOException e) {
            logger.error("读取Shapefile失败: {}", shpFile.getAbsolutePath(), e);
            throw e;
        } finally {
            if (dataStore != null) {
                dataStore.dispose();
                logger.debug("DataStore资源已释放。");
            }
        }
        return features;
    }

    /**
     * 测试用例
     */
    public static void main(String[] args) throws Exception {
        // 配置SLF4J简单日志输出，方便测试观察
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");

        GeometryFactory gf = GEOMETRY_FACTORY; // 使用静态的GeometryFactory

        // 1. 创建带自定义属性的点Shapefile
        List<Point> points = Arrays.asList(
                gf.createPoint(new Coordinate(116.397, 39.907)), // 北京
                gf.createPoint(new Coordinate(121.4737, 31.2304))  // 上海
        );
        List<Map<String, Object>> pointAttrs = new ArrayList<>();
        Map<String, Object> attr1 = new HashMap<>();
        attr1.put("City", "Beijing");
        attr1.put("Population", 21000000);
        attr1.put("IsCapital", true);
        pointAttrs.add(attr1);
        Map<String, Object> attr2 = new HashMap<>();
        attr2.put("City", "Shanghai");
        attr2.put("Population", 24000000);
        attr2.put("IsCapital", false);
        pointAttrs.add(attr2);

        File pointShpFile = new File("output/points_with_attrs.shp");
        createPointShapefile(pointShpFile, points, pointAttrs, null); // null表示使用默认WGS84 CRS
        zipShapefile("output/points_with_attrs", "output/points_with_attrs.zip");

        // 2. 创建线Shapefile（从WKT解析）
        String lineWKT = "LINESTRING (0 0, 1 1, 2 0, 3 1, 4 0)";
        LineString line = (LineString) parseGeometryFromWKT(lineWKT);
        List<LineString> lines = Collections.singletonList(line);
        List<Map<String, Object>> lineAttrs = new ArrayList<>();
        Map<String, Object> lineAttr1 = new HashMap<>();
        lineAttr1.put("RouteName", "Test Route 1");
        lineAttrs.add(lineAttr1);
        File lineShpFile = new File("output/line_from_wkt.shp");
        createLineShapefile(lineShpFile, lines, lineAttrs, null);
        zipShapefile("output/line_from_wkt", "output/line_from_wkt.zip");

        // 3. 创建面Shapefile（带有自定义CRS和复杂属性）
        String polygonWKT = "POLYGON ((10 10, 10 20, 20 20, 20 10, 10 10))";
        Polygon polygon = (Polygon) parseGeometryFromWKT(polygonWKT);
        List<Polygon> polygons = Collections.singletonList(polygon);

        List<Map<String, Object>> polygonAttrs = new ArrayList<>();
        Map<String, Object> polyAttr1 = new HashMap<>();
        polyAttr1.put("AreaID", 1001);
        polyAttr1.put("Description", "This is a test polygon.");
        polyAttr1.put("Type", "Building");
        polygonAttrs.add(polyAttr1);

        // 示例：使用UTM投影坐标系 (例如，WGS 84 / UTM zone 31N)
        // 注意：实际使用时请确保您的几何坐标与CRS匹配
        CoordinateReferenceSystem utmCrs = CRS.decode("EPSG:32631"); // UTM zone 31N
        File polygonShpFile = new File("output/polygon_utm.shp");
        // 注意：此处如果polygonWKT是经纬度，而CRS是UTM，则数据会有问题，需要进行坐标转换
        // 实际应用中，你可能需要 GeoTools 的 transform 方法来转换几何对象的CRS
        createPolygonShapefile(polygonShpFile, polygons, polygonAttrs, utmCrs);
        zipShapefile("output/polygon_utm", "output/polygon_utm.zip");


        // 4. 创建MultiPolygon Shapefile
        Polygon poly1 = gf.createPolygon(new Coordinate[]{new Coordinate(100, 0), new Coordinate(100, 10), new Coordinate(110, 10), new Coordinate(110, 0), new Coordinate(100, 0)});
        Polygon poly2 = gf.createPolygon(new Coordinate[]{new Coordinate(120, 20), new Coordinate(120, 30), new Coordinate(130, 30), new Coordinate(130, 20), new Coordinate(120, 20)});
        MultiPolygon multiPolygon = gf.createMultiPolygon(new Polygon[]{poly1, poly2});
        List<MultiPolygon> multiPolygons = Collections.singletonList(multiPolygon);
        List<Map<String, Object>> multiPolyAttrs = new ArrayList<>();
        Map<String, Object> multiPolyAttr1 = new HashMap<>();
        multiPolyAttr1.put("ComplexId", "MP_001");
        multiPolyAttrs.add(multiPolyAttr1);
        File multiPolygonShpFile = new File("output/multi_polygon.shp");
        createMultiPolygonShapefile(multiPolygonShpFile, multiPolygons, multiPolyAttrs, null);
        zipShapefile("output/multi_polygon", "output/multi_polygon.zip");

        // 5. 读取Shapefile示例
        System.out.println("\n--- 读取Shapefile示例 ---");
        List<SimpleFeature> readFeatures = readShapefile(pointShpFile);
        for (SimpleFeature feature : readFeatures) {
            // 您可以通过feature.getAttribute("属性名")来获取具体属性值
            System.out.println("  City: " + feature.getAttribute("City"));
            System.out.println("  Population: " + feature.getAttribute("Population"));
        }
    }
}