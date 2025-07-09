# GeoTools Demo

这是一个使用 [GeoTools](https://geotools.org/) 的简单 Java 示例项目，演示如何读取 Shapefile 格式的地理数据。

## 使用方法

1. 克隆仓库，导入到你的 IDE。
2. 将你的 Shapefile 放到 `data/your-shapefile.shp`（或修改代码中的路径）。
3. 使用 Maven 构建项目：
   ```bash
   mvn compile
   mvn exec:java -Dexec.mainClass="org.example.App"
   ```
4. 你将看到 shapefile 的要素和几何信息输出。

## 依赖

- GeoTools 30.0
- SLF4J simple 日志

如需支持更多格式或GDAL扩展，可进一步添加相关依赖。
