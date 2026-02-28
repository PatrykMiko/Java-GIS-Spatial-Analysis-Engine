package org.example;

import java.util.List;
import java.util.Locale;

/**
 * POJO representing an Administrative Unit.
 * Acts as a node in a hierarchical Tree structure mapping a country's administration
 * (e.g., Country -> Voivodeship -> County -> Municipality).
 */
public class AdminUnit {
    String name;
    Integer adminLevel;
    Double population;
    Double area;
    Double density;

    // Self-referential attributes for Graph/Tree structure
    AdminUnit parent;
    List<AdminUnit> children;

    // Spatial data
    BoundingBox bbox = new BoundingBox();

    @Override
    public String toString() {
        return "AdminUnit{" +
                "name='" + name + '\'' +
                ", adminLevel=" + adminLevel +
                ", population=" + population +
                ", area=" + area +
                ", density=" + density +
                ", bbox=" + bbox.toString() +
                '}';
    }
}
