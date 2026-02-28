package org.example;

import java.io.PrintStream;
import java.util.Locale;

/**
 * Represents a 2D geographical bounding box.
 * Used for fast spatial queries (intersections, containment) and calculating distances.
 */
public class BoundingBox {
    Double xmin;
    Double ymin;
    Double xmax;
    Double ymax;

    @Override
    public String toString() {
        return "BoundingBox{" +
                "xmin=" + xmin +
                ", ymin=" + ymin +
                ", xmax=" + xmax +
                ", ymax=" + ymax +
                '}';
    }

    /**
     * Expands the bounding box to include the given point.
     */
    void addPoint(Double x, Double y){
        if(x == null || y == null) return;
        if(this.isEmpty()){
            xmax = x; xmin = x;
            ymax = y; ymin = y;
        } else {
            if(x > xmax) xmax = x;
            if(x < xmin) xmin = x;
            if(y > ymax) ymax = y;
            if(y < ymin) ymin = y;
        }
    }

    boolean contains(double x, double y){
        if(this.isEmpty()) return false;
        return (x > xmin && x < xmax && y > ymin && y < ymax);
    }

    boolean contains(BoundingBox bb){
        if(this.isEmpty()) return false;
        if (bb.isEmpty()) return true;
        return (bb.xmin > xmin && bb.xmax < xmax && bb.ymin > ymin && bb.ymax < ymax);
    }

    boolean intersects(BoundingBox bb){
        if (this.isEmpty() || bb.isEmpty()) return false;
        // Check for overlap on both axes
        return ((this.xmin <= bb.xmax && this.xmax >= bb.xmin) &&
                (this.ymin <= bb.ymax && this.ymax >= bb.ymin));
    }

    /**
     * Creates a union of this bounding box and another one.
     */
    BoundingBox add(BoundingBox bb){
        if(bb.isEmpty()) return this;
        if(this.isEmpty()){
            xmax = bb.xmax; xmin = bb.xmin;
            ymax = bb.ymax; ymin = bb.ymin;
            return this;
        } else {
            if(bb.xmax > xmax) xmax = bb.xmax;
            if(bb.xmin < xmin) xmin = bb.xmin;
            if(bb.ymax > ymax) ymax = bb.ymax;
            if(bb.ymin < ymin) ymin = bb.ymin;
            return this;
        }
    }

    boolean isEmpty(){
        return (xmin == null) && (xmax == null) && (ymin == null) && (ymax == null);
    }

    public boolean equals(Object o){
        return (o instanceof BoundingBox && this == o);
    }

    Double getCenterX(){
        if(this.isEmpty()) throw new RuntimeException("The BoundingBox is empty");
        return (xmax + xmin) / 2;
    }

    Double getCenterY(){
        if(this.isEmpty()) throw new RuntimeException("The BoundingBox is empty");
        return (ymax + ymin) / 2;
    }

    /**
     * Calculates the great-circle distance between the centers of two bounding boxes.
     * Uses the Haversine formula to account for the Earth's curvature.
     * @return Distance in kilometers.
     */
    double distanceTo(BoundingBox bbx){
        if (this.isEmpty() || bbx.isEmpty()) {
            throw new RuntimeException("BoundingBox is empty");
        }

        final double EARTH_RADIUS = 6371.0088; // Radius in km

        Double startLat = this.getCenterY();
        Double startLong = this.getCenterX();
        Double endLat = bbx.getCenterY();
        Double endLong = bbx.getCenterX();

        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    /**
     * Outputs the bounding box in WKT (Well-Known Text) format, standard for GIS systems.
     */
    public void getWKT(PrintStream out){
        out.printf(Locale.US,"LINESTRING(%f %f, %f %f)", xmin, ymin, xmax, ymax);
    }

    /**
     * Expands the bounding box by a given distance in degrees.
     * Useful for setting up spatial query areas.
     */
    public BoundingBox addMargin(double distance) {
        BoundingBox newBB = new BoundingBox();
        newBB.addPoint(this.xmin - distance, this.ymin - distance);
        newBB.addPoint(this.xmax + distance, this.ymax + distance);
        return newBB;
    }
}



