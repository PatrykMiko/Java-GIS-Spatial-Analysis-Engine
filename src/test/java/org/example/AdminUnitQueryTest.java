package org.example;

import org.junit.Test;

import static java.lang.System.out;
import static org.junit.Assert.*;

/**
 * Tests for the Fluent API (Builder pattern) used to construct and execute complex queries.
 */
public class AdminUnitQueryTest {

    /**
     * Tests basic filtering, OR chaining, sorting by property, and limiting results.
     */
    @Test
    public void firstTest() {
        AdminUnitList admin = new AdminUnitList();
        admin.read("admin_units.csv");
        AdminUnitQuery query = new AdminUnitQuery()
                .selectFrom(admin)
                .where(a->a.area != null && a.area > 1000)
                .or(a->a.name.startsWith("Sz"))
                .sort((a,b)->Double.compare(a.area,b.area))
                .limit(100);
        query.execute().list(out);
    }

    /**
     * Tests complex multi-level sorting (first by adminLevel, then by area descending).
     */
    @Test
    public void secondTest() {
        AdminUnitList admin = new AdminUnitList();
        admin.read("admin_units.csv");
        AdminUnitQuery query = new AdminUnitQuery()
                .selectFrom(admin)
                .where(a -> a.adminLevel != null && a.adminLevel > 0)
                .where(a-> a.area != null && a.area > 0)
                .sort((a, b) -> {
                    int result = Integer.compare(a.adminLevel, b.adminLevel);
                    if (result == 0) {
                        return Double.compare(b.area, a.area);
                    }
                    return result;
                })
                .limit(50);
        query.execute().list(out);
    }

    @Test
    public void thirdTest() {
        AdminUnitList admin = new AdminUnitList();
        admin.read("admin_units.csv");
        AdminUnitQuery query = new AdminUnitQuery()
                .selectFrom(admin)
                .where(a -> a.name.endsWith("skie"))
                .or(a -> a.name.contains("Góra"))
                .sort((a, b) -> a.name.compareTo(b.name))
                .limit(20);
        query.execute().list(out);
    }

    @Test
    public void fourthTest() {
        AdminUnitList admin = new AdminUnitList();
        admin.read("admin_units.csv");
        AdminUnitQuery query = new AdminUnitQuery()
                .selectFrom(admin)
                .where(a -> a.population != null && a.population > 500)
                .and(a -> a.area != null && a.area < 100)
                .sort((a, b) -> Double.compare(b.population, a.population))
                .limit(10);
        query.execute().list(out);
    }

}