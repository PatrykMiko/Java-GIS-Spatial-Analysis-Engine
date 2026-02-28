# Java GIS & Spatial Analysis Engine

![Java](https://img.shields.io/badge/Java-11%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white) ![GIS](https://img.shields.io/badge/GIS-Spatial_Analysis-1572B6?style=for-the-badge&logo=map&logoColor=white) ![JUnit](https://img.shields.io/badge/JUnit-Tested-25A162?style=for-the-badge&logo=junit5&logoColor=white)

A lightweight, dependency-free Java library designed for processing geographic data, analyzing spatial relationships, and parsing complex CSV datasets. 

This project demonstrates how to build high-performance spatial querying systems and robust file parsers entirely from scratch using pure Java (Standard Library).

## 🚀 Key Features

* **Custom CSV Parser:** A robust, regex-powered parser capable of handling custom delimiters, missing data fields (returning `null` instead of crashing), and edge cases like delimiters nested inside quoted strings (e.g., `"Smith, John"`).
* **Hierarchical Tree Structures:** Automatically reconstructs complex administrative relationships (Country -> Voivodeship -> County -> Municipality) from flat CSV data.
* **Spatial Data Analysis:** * Implements `BoundingBox` geometry for mapping regions.
  * Calculates accurate geographical distances on a sphere using the **Haversine formula**.
* **High-Performance Neighbor Search:** Optimized queries that use bounding box margin intersection (`addMargin`) to pre-filter distant nodes before performing expensive trigonometric calculations.
* **Data Imputation:** Recursively traverse the hierarchical tree to estimate and fill in missing `population` and `density` data based on parent administrative units.
* **Fluent Query API:** A SQL-like Builder pattern (`AdminUnitQuery`) allowing for clean, highly readable data filtering: `selectFrom(...).where(...).and(...).sort(...).limit(...)`.

## 🛠️ Technology Stack
* **Java 17+** (Works with earlier versions, utilizes standard `java.util`, `java.io`, `java.time`)
* **JUnit 4/5** (For unit and performance testing)
* *Zero external parsing or GIS libraries (No Apache Commons, No GeoTools) - everything is written from the ground up.*

## 💻 Code Examples

### 1. Parsing CSV Data
```java
// Handles standard data and complex quotes gracefully
CSVReader reader = new CSVReader("titanic.csv", ",", true);
while (reader.next()) {
    Integer id = reader.getInt("PassengerId");
    String name = reader.get("Name"); // "Braund, Mr. Owen Harris"
    Double fare = reader.getDouble("Fare");
}
