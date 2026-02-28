package org.example;


import java.io.PrintStream;
import java.text.Collator;
import java.util.*;
import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Core class managing the collection of administrative units.
 * Handles loading, cleaning (imputation), querying, and spatial analysis.
 */

public class AdminUnitList {
    List<AdminUnit> units = new ArrayList<>();

    /**
     * Reads CSV data and reconstructs the hierarchical Tree structure
     * using parent IDs to link objects together.
     */
    public void read(String filename) {
        CSVReader reader = new CSVReader(filename, ",", true);

        // Temporary maps to build graph edges efficiently in O(N) time
        Map<Long, AdminUnit> idToAdminUnit = new HashMap<>();
        Map<AdminUnit, Long> adminUnitToParentId = new HashMap<>();

        Map<Long, List<AdminUnit>> parentid2child = new HashMap<>();

        while (reader.next()) {
            AdminUnit adminUnit = new AdminUnit();

            adminUnit.name= reader.get("name");
            adminUnit.adminLevel= reader.getInt("admin_level");
            adminUnit.population= reader.getDouble("population");
            adminUnit.area= reader.getDouble("area");
            adminUnit.density= reader.getDouble("density");

            Long id = reader.getLong("id");
            Long parentId = reader.getLong("parent");

            if (id != null) {
                idToAdminUnit.put(id, adminUnit);
            }

            if (parentId != null) {
                adminUnitToParentId.put(adminUnit, parentId);
                if (!parentid2child.containsKey(parentId)) {
                    parentid2child.put(parentId, new ArrayList<>());
                }
                parentid2child.get(parentId).add(adminUnit);
            }

            // Parse geometric data into a BoundingBox
            Double x1= reader.getDouble("x1");
            Double y1= reader.getDouble("y1");
            Double x2= reader.getDouble("x2");
            Double y2= reader.getDouble("y2");
            Double x3= reader.getDouble("x3");
            Double y3= reader.getDouble("y3");
            Double x4= reader.getDouble("x4");
            Double y4= reader.getDouble("y4");

            BoundingBox boundingBox = new BoundingBox();
            boundingBox.addPoint(x1, y1);
            boundingBox.addPoint(x2, y2);
            boundingBox.addPoint(x3, y3);
            boundingBox.addPoint(x4, y4);
            adminUnit.bbox = boundingBox;

            units.add(adminUnit);

        }
        // Post-processing: Resolve references to reconstruct the tree structure
        for (AdminUnit unit : units) {
            if (adminUnitToParentId.containsKey(unit)) {
                Long parentId = adminUnitToParentId.get(unit);
                AdminUnit parent = idToAdminUnit.get(parentId);
                unit.parent = parent;
            } else {
                unit.parent = null;
            }
        }

        parentid2child.forEach((parentId, children) -> {
            AdminUnit parent = idToAdminUnit.get(parentId);
            if (parent != null) {
                parent.children = children;
            }
        });


    }


    void list(PrintStream out){
        for(AdminUnit adminUnit : units) {
            out.println(adminUnit.toString()+"\n");
        }

    }

    void list(PrintStream out,int offset, int limit ){
        for(int j=0;j<limit;j++){
            out.println(units.get(offset+j)+"\n");
        }
    }

    AdminUnitList selectByName(String pattern, boolean regex){
        AdminUnitList ret = new AdminUnitList();
        for(AdminUnit adminUnit : units){
            if(regex && adminUnit.name.matches(pattern)){
                ret.units.add(adminUnit);
            }
            if(!regex && adminUnit.name.contains(pattern)){
                ret.units.add(adminUnit);
            }
        }
        return ret;
    }

    /**
     * Recursively imputes missing density and population data by borrowing
     * demographic statistics from parent administrative units.
     */

    public void fixMissingValues(){
        for(AdminUnit adminUnit : units){
            fixMissingValues(adminUnit);
        }
    }

    public void fixMissingValues(AdminUnit au){
        if(au.density != null){
            if(au.population == null){
                au.population = au.area * au.density;
            }
            return;
        }

        if(au.parent == null){
            return;
        }

        if(au.parent.density == null){
            fixMissingValues(au.parent);
        }

        if (au.parent.density != null) {
            au.density = au.parent.density;
            if(au.area != null){
                au.population = au.area * au.density;
            }
        }

    }

    /**
     * Removes units that match a pattern, effectively pruning the tree.
     */
    public void removeRoot(String pattern, boolean regex) {

        List<AdminUnit> toRemove = new ArrayList<>();
        Deque<AdminUnit> stack = new ArrayDeque<>();

        for (AdminUnit unit : units) {
            if (unit.parent == null) {
                boolean match = false;
                if (regex) {
                    if (unit.name.matches(pattern)) match = true;
                } else {
                    if (unit.name.contains(pattern)) match = true;
                }

                if (match) {
                    toRemove.add(unit);
                    stack.push(unit);
                }
            }
        }

        while (!stack.isEmpty()) {
            AdminUnit current = stack.pop();

            if (current.children != null && !current.children.isEmpty()) {
                for (AdminUnit child : current.children) {
                    toRemove.add(child);
                    stack.push(child);
                }
            }
        }
        units.removeAll(toRemove);
    }


    AdminUnitList getNeighbors(AdminUnit unit, double maxdistance){
        if(unit == null || unit.adminLevel == null){
            throw new RuntimeException("The AdminUnit does not exist");
        }

        AdminUnitList neighbours = new AdminUnitList();

        for(AdminUnit candidate : units) {
            if (candidate == unit) {
                continue;
            }
            if(candidate.bbox.isEmpty()){
                continue;
            }
            if(candidate.adminLevel == null){
                continue;
            }
            if (!candidate.adminLevel.equals(unit.adminLevel)) {
                continue;
            }
            if(unit.adminLevel == 8){
                if (unit.bbox.distanceTo(candidate.bbox) <= maxdistance) {
                    neighbours.units.add(candidate);
                }
            }
            else if (unit.bbox.intersects(candidate.bbox)) {
                neighbours.units.add(candidate);
            }
        }
        return neighbours;
    }

    /**
     * Highly optimized neighbor search.
     * Uses bounding box pre-filtering to avoid calculating expensive Haversine distances
     * for nodes that are visibly too far away geographically.
     */
    AdminUnitList getNeighborsFast(AdminUnit unit, double maxdistance){
        if(unit == null || unit.adminLevel == null){
            throw new RuntimeException("The AdminUnit does not exist");
        }

        AdminUnitList neighbours = new AdminUnitList();
        boolean isLocality = (unit.adminLevel == 8);

        BoundingBox queryBox;
        if (isLocality) {
            // Rough conversion of km to degrees for BBox expansion (1 degree ≈ 111 km)
            double kmToDegrees = maxdistance / 111.0;
            queryBox = unit.bbox.addMargin(kmToDegrees);
        } else {
            queryBox = unit.bbox;
        }

        // Search recursively starting from roots (units without a parent)
        for (AdminUnit candidate : units) {
            if (candidate.parent == null) {
                searchRecursive(candidate, unit, queryBox, neighbours, maxdistance, isLocality);
            }
        }
        return neighbours;
    }

    private void searchRecursive(AdminUnit current, AdminUnit source, BoundingBox queryBox, AdminUnitList neighbours, double maxDist, boolean isLocality) {

        if (current.bbox.isEmpty() || !current.bbox.intersects(queryBox)) {
            return;
        }

        if (current.adminLevel != null && current.adminLevel.equals(source.adminLevel)) {
            if (current != source) {
                if (isLocality) {
                    if (source.bbox.distanceTo(current.bbox) <= maxDist) {
                        neighbours.units.add(current);
                    }
                } else {
                    neighbours.units.add(current);
                }
            }
            return;
        }

        if (current.children != null && !current.children.isEmpty()) {
            if (current.adminLevel < source.adminLevel) {
                for (AdminUnit child : current.children) {
                    searchRecursive(child, source, queryBox, neighbours, maxDist, isLocality);
                }
            }
        }
    }

    // --- Stream-like collection operations utilizing functional interfaces ---

    public AdminUnitList sortInplaceByName() {
        class NameComparator implements Comparator<AdminUnit> {
            @Override
            public int compare(AdminUnit t, AdminUnit t1) {
                if (t.name == null && t1.name == null) return 0;
                if (t.name == null) return 1;
                if (t1.name == null) return -1;
                return t.name.compareTo(t1.name);
            }
        }
        NameComparator comparator = new NameComparator();
        this.units.sort(comparator);
        return this;
    }

    public AdminUnitList sortInplaceByArea() {
        this.units.sort(new Comparator<AdminUnit>() {
            @Override
            public int compare(AdminUnit t, AdminUnit t1) {
                if (t.area == null && t1.area == null) return 0;
                if (t.area == null) return 1;
                if (t1.area == null) return -1;
                return Double.compare(t.area, t1.area);
            }
        });
        return this;
    }

    public AdminUnitList sortInplaceByPopulation() {
        this.units.sort(Comparator.comparing(t -> t.population, Comparator.nullsLast(Double::compare)));
        return this;
    }

    public AdminUnitList sortInplace(Comparator<AdminUnit> cmp) {
        this.units.sort(cmp);
        return this;
    }

    public AdminUnitList sort(Comparator<AdminUnit> cmp) {
        AdminUnitList result = new AdminUnitList();
        result.units.addAll(this.units);
        result.sortInplace(cmp);
        return result;
    }

    public AdminUnitList filter(Predicate<AdminUnit> pred) {
        AdminUnitList result = new AdminUnitList();
        for (AdminUnit unit : this.units) {
            if (pred.test(unit)) {
                result.units.add(unit);
            }
        }
        return result;
    }

    AdminUnitList filter(Predicate<AdminUnit> pred, int limit) {
        AdminUnitList result = new AdminUnitList();
        if (limit <= 0) return result;
        int count = 0;
        for (AdminUnit unit : this.units) {
            if (pred.test(unit)) {
                result.units.add(unit);
                count++;
                if (count >= limit) {
                    break;
                }
            }
        }
        return result;
    }

    AdminUnitList filter(Predicate<AdminUnit> pred, int offset, int limit) {
        AdminUnitList result = new AdminUnitList();
        if (limit <= 0) return result;
        if (offset < 0) offset = 0;

        int skip = 0;
        int count= 0;

        for (AdminUnit unit : this.units) {
            if (pred.test(unit)) {
                if (skip < offset) {
                    skip++;
                    continue;
                }
                result.units.add(unit);
                count++;
                if (count >= limit) {
                    break;
                }
            }
        }
        return result;
    }

}


