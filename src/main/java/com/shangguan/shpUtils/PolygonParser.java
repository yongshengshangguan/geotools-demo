package com.shangguan.shpUtils;
import org.locationtech.jts.geom.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class PolygonParser {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    public static List<Polygon> parsePolygonListFromString(String json) {
        List<Polygon> polygonList = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            // 三维数组：[ [ [lon, lat], [lon, lat], ... ] ]
            double[][][] coords = mapper.readValue(json, double[][][].class);

            for (double[][] ring : coords) {
                Coordinate[] coordinates = new Coordinate[ring.length];
                for (int i = 0; i < ring.length; i++) {
                    double lon = ring[i][0];
                    double lat = ring[i][1];
                    coordinates[i] = new Coordinate(lon, lat);
                }
                // 保证闭合：首尾点相同
                if (!coordinates[0].equals2D(coordinates[coordinates.length - 1])) {
                    Coordinate[] closed = new Coordinate[coordinates.length + 1];
                    System.arraycopy(coordinates, 0, closed, 0, coordinates.length);
                    closed[coordinates.length] = coordinates[0];
                    coordinates = closed;
                }
                polygonList.add(geometryFactory.createPolygon(coordinates));
            }
        } catch (Exception e) {
            throw new RuntimeException("无法解析地理坐标字符串", e);
        }
        return polygonList;
    }

    // 示例调用
    public static void main(String[] args) {
        String json = "[[[105.862604473,27.013138109],[105.862661693,27.01300221],[105.862618778,27.012880617],[105.86256871,27.012737565],[105.862246845,27.012794786],[105.862499061,27.013036913],[105.862604473,27.013138109]]]";
        List<Polygon> polygons = parsePolygonListFromString(json);
        System.out.println("共解析出 Polygon 个数: " + polygons.size());
    }
}
