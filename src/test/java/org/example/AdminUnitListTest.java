package org.example;

import org.junit.Test;

import java.util.Locale;
import java.util.function.Predicate;

import static java.lang.System.out;
import static org.junit.Assert.*;

/**
 * Tests for the core AdminUnitList functionalities.
 * Focuses on performance benchmarking (spatial queries) and collection operations (filter, sort).
 */
public class AdminUnitListTest {

    /**
     * Benchmark test: Tests the brute-force spatial neighbor search.
     * Calculates distance using the Haversine formula for all units.
     */
    @Test
    public void firstTest() {
        System.out.println("\n WOLNE \n");
        AdminUnitList admin = new AdminUnitList();
        admin.read("admin_units.csv");
        System.out.println("PIERWSZY PRZYKŁAD NIE DLA MIEJSCOWOŚCI");
        AdminUnit adminUnit = admin.units.get(5);
        double t1 = System.nanoTime()/1e6;
        AdminUnitList neighbors = admin.getNeighbors(adminUnit, 15);
        double t2 = System.nanoTime()/1e6;
        System.out.printf(Locale.US,"t2-t1=%f\n",t2-t1);
        neighbors.list(System.out);
        System.out.println("DRUGI PRZYKŁAD DLA MIEJSCOWOŚCI");
        AdminUnit adminUnit2 = admin.units.get(9);
        double t12 = System.nanoTime()/1e6;
        AdminUnitList neighbors2 = admin.getNeighbors(adminUnit2, 15);
        double t22 = System.nanoTime()/1e6;
        System.out.printf(Locale.US,"t2-t1=%f\n",t22-t12);
        neighbors2.list(System.out);
    }

    /**
     * Benchmark test: Tests the optimized spatial neighbor search.
     * Uses Bounding Box intersection to pre-filter candidates before calculating exact distances.
     * Should be significantly faster than firstTest().
     */
    @Test
    public void secondTest() {
        System.out.println("\n SZYBKIE \n");
        AdminUnitList admin = new AdminUnitList();
        admin.read("admin_units.csv");
        System.out.println("PIERWSZY PRZYKŁAD NIE DLA MIEJSCOWOŚCI");
        AdminUnit adminUnit = admin.units.get(5);
        double t1 = System.nanoTime()/1e6;
        AdminUnitList neighbors = admin.getNeighborsFast(adminUnit, 15);
        double t2 = System.nanoTime()/1e6;
        System.out.printf(Locale.US,"t2-t1=%f\n",t2-t1);
        neighbors.list(System.out);
        System.out.println("DRUGI PRZYKŁAD DLA MIEJSCOWOŚCI");
        AdminUnit adminUnit2 = admin.units.get(9);
        double t12 = System.nanoTime()/1e6;
        AdminUnitList neighbors2 = admin.getNeighborsFast(adminUnit2, 15);
        double t22 = System.nanoTime()/1e6;
        System.out.printf(Locale.US,"t2-t1=%f\n",t22-t12);
        neighbors2.list(System.out);
    }

    // --- Functional filtering and sorting tests ---

    @Test
    public void thirdTest() {
        AdminUnitList admin = new AdminUnitList();
        admin.read("admin_units.csv");
        admin.filter(a->a.name.startsWith("Ż")).sortInplaceByArea().list(out);
    }

    @Test
    public void fourthTest() {
        AdminUnitList admin = new AdminUnitList();
        admin.read("admin_units.csv");
        admin.filter(a->a.name.startsWith("K")).sortInplaceByArea().list(out);
    }


    @Test
    public void fifthTest() {
        AdminUnitList admin = new AdminUnitList();
        admin.read("admin_units.csv");
        // Test hierarchical relationship traversal (finding units within a specific parent)
        admin.filter(a -> a.adminLevel != null && a.adminLevel == 6 && a.parent != null && "województwo małopolskie".equals(a.parent.name)).list(System.out);

    }

    @Test
    public void sixthTest() {
        AdminUnitList admin = new AdminUnitList();
        admin.read("admin_units.csv");
        admin.filter(a -> a.name.length() > 15).sortInplaceByName().list(System.out);

    }

    @Test
    public void seventhTest() {
        AdminUnitList admin = new AdminUnitList();
        admin.read("admin_units.csv");
        // Test complex predicate composition (NOT StartsWithP OR StartsWithM AND BigArea)
        Predicate<AdminUnit> startsWithP = a -> a.name.startsWith("P");
        Predicate<AdminUnit> startsWithM = a -> a.name.startsWith("M");
        Predicate<AdminUnit> isBigArea = a -> a.area != null && a.area > 5000;
        admin.filter(startsWithP.negate().or(startsWithM).and(isBigArea)).sortInplaceByPopulation().list(System.out);

    }



}