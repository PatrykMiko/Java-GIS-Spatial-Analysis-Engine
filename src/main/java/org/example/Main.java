package org.example;

import java.io.*;
import java.util.Locale;
import static java.lang.System.out;

/**
 * Entry point to demonstrate the spatial and data processing capabilities.
 */
public class Main {
    public static void main(String[] args) {
        AdminUnitList admin = new AdminUnitList();

        // 1. Data loading and tree building
        admin.read("admin_units.csv");
        admin.list(out, 5, 10);

        // 2. Searching with Regex
        AdminUnitList regex = admin.selectByName(".*skie", true);
        regex.list(out, 5, 10);

        // 3. Data imputation (filling missing fields dynamically)
        admin.fixMissingValues();
        out.println("Liczba jednostek przed usunięciem: " + admin.units.size());

        // 4. Tree manipulation (pruning nodes and their children)
        admin.removeRoot("województwo", false);
        out.println("Liczba jednostek po usunięciu: " + admin.units.size());
    }
}