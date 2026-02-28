package org.example;

import org.junit.Test;

import java.io.StringReader;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Tests the robustness of the custom CSVReader.
 * Ensures data integrity, proper type conversion, and handling of tricky CSV formats.
 */
public class CSVReaderTest {

    /**
     * Tests standard parsing by column name with normal data.
     */
    @Test
    public void firstTest() {
        CSVReader reader = new CSVReader("with-header.csv", ";", true);
        int count = 0;
        while (reader.next()) {
            String id = reader.get("id");
            String imie = reader.get("imie");
            String nazwisko = reader.get("nazwisko");
            String ulica = reader.get("ulica");
            String nrdomu = reader.get("nrdomu");
            String nrmieszkania = reader.get("nrmieszkania");

            if (count == 0) {
                assertEquals("1", id);
                assertEquals("Jan", imie);
                assertEquals("Kowal", nazwisko);
                assertEquals("Florianska", ulica);
                assertEquals("2", nrdomu);
                assertEquals("4", nrmieszkania);
            } else if (count == 9) {
                assertEquals("10", id);
                assertEquals("Dominik", imie);
                assertEquals("Nowacki", nazwisko);
                assertEquals("Mikolajska", ulica);
                assertEquals("4", nrdomu);
                assertEquals("4", nrmieszkania);
            }
            count++;
        }
        assertEquals(10, count);
    }

    @Test
    public void secondTest() {
        CSVReader reader = new CSVReader("with-header.csv", ";", true);
        int count = 0;
        while (reader.next()) {
            Integer id = reader.getInt(0);
            String imie = reader.get(1);
            String nazwisko = reader.get(2);
            String ulica = reader.get(3);
            Integer nrdomu = reader.getInt(4);
            Integer nrmieszkania = reader.getInt(5);

            if (count == 0) {
                assertEquals(Integer.valueOf(1), id);
                assertEquals("Jan", imie);
                assertEquals("Kowal", nazwisko);
                assertEquals("Florianska", ulica);
                assertEquals(Integer.valueOf(2), nrdomu);
                assertEquals(Integer.valueOf(4), nrmieszkania);
            } else if (count == 9) {
                assertEquals(Integer.valueOf(10), id);
                assertEquals("Dominik", imie);
                assertEquals("Nowacki", nazwisko);
                assertEquals("Mikolajska", ulica);
                assertEquals(Integer.valueOf(4), nrdomu);
                assertEquals(Integer.valueOf(4), nrmieszkania);
            }
            count++;
        }
        assertEquals(10, count);
    }

    /**
     * Tests parsing handling of missing values and nulls to prevent NullPointerExceptions.
     */
    @Test
    public void thirdTest() {
        CSVReader reader = new CSVReader("missing-values.csv", ";", true);
        int count = 0;
        while (reader.next()) {
            Long id = reader.getLong(0);
            Long parent = reader.getLong(1);
            String name = reader.get(2);
            Integer admin_level = reader.getInt(3);
            Long population = reader.getLong(4);
            Double area = reader.getDouble(5);
            Double density = reader.getDouble(6);
            if (count == 0) {
                assertEquals(Long.valueOf(11670), id);
                assertEquals(Long.valueOf(11649), parent);
                assertEquals("gmina Lanckorona", name);
                assertEquals(Integer.valueOf(7), admin_level);
                assertEquals(Long.valueOf(6165), population);
                assertEquals(40.4298, area, 0.01);
                assertEquals(152.487, density, 0.01);
            } else if (count == 1) {
                assertEquals(Long.valueOf(11672), id);
                assertEquals(Long.valueOf(11670), parent);
                assertEquals("Lanckorona", name);
                assertEquals(Integer.valueOf(8), admin_level);
                assertNull(population);
                assertEquals(11.7616, area, 0.001);
                assertNull(density);
            } else if (count == 11) {
                assertNull(population);
                assertNull(density);
            }
            count++;
        }
        assertEquals(12, count);
    }

    @Test
    public void fourthTest() {
        CSVReader reader = new CSVReader("elec.csv", ",", true);
        while (reader.next()) {
            Integer day1 = reader.getInt(20);
            Integer month1 = reader.getInt("month1");

            String day2 = reader.get(21);
            String month2 = reader.get("month2");

            Double day3 = reader.getDouble(22);
            Double month3 = reader.getDouble("month3");

            Long day4 = reader.getLong(23);
            Long month4 = reader.getLong("month4");

            assertNull(day1);
            assertNull(month1);
            assertNull(day2);
            assertNull(month2);
            assertNull(day3);
            assertNull(month3);
            assertNull(day4);
            assertNull(month4);

        }

    }

    /**
     * Tests reading from a memory String instead of a file (useful for API/Web integrations).
     */
    @Test
    public void fifthTest() {
        String text = "a,b,c\n123.4,567.8,91011.12";
        CSVReader reader = new CSVReader(new StringReader(text), ",", true);
        int count = 0;
        while (reader.next()) {
            Double a = reader.getDouble("a");
            Double b = reader.getDouble("b");
            Double c = reader.getDouble("c");
            if (count == 0) {
                assertEquals(123.4, a, 0.001);
                assertEquals(567.8, b, 0.001);
                assertEquals(91011.12, c, 0.001);
            }
            count++;
        }
        assertEquals(1, count);

        String text2 = """
                a,b,c
                123.4,567.8,91011.12""";
        reader = new CSVReader(new StringReader(text2), ",", true);
        int count2 = 0;
        while (reader.next()) {
            Double a = reader.getDouble("a");
            Double b = reader.getDouble("b");
            Double c = reader.getDouble("c");
            if (count == 0) {
                assertEquals(123.4, a, 0.001);
                assertEquals(567.8, b, 0.001);
                assertEquals(91011.12, c, 0.001);
            }
            count2++;
        }
        assertEquals(1, count2);
    }

    /**
     * Tests advanced Regex parsing: Delimiters inside quotes should be ignored.
     * e.g., "Braund, Mr. Owen Harris" should be one column, not split at the comma.
     */
    @Test
    public void sixthTest() {
        CSVReader reader = new CSVReader("titanic-part.csv", ",", true);
        int count = 0;
        while (reader.next()) {
            Integer id = reader.getInt("PassengerId");
            String name = reader.get("Name");
            Double fare = reader.getDouble("Fare");
            if (count == 0) {
                assertEquals(Integer.valueOf(1), id);
                assertEquals("Braund, Mr. Owen Harris", name);
                assertEquals(7.250000, fare, 0.001);
            } else if (count == 9) {
                assertEquals(Integer.valueOf(10), id);
                assertEquals("Nasser, Mrs. Nicholas (Adele Achem)", name);
                assertEquals(30.070800, fare, 0.001);
            }
            count++;
        }
        assertEquals(18, count);
        CSVReader reader2 = new CSVReader("titanic-part.csv", ",", true);
        int count2 = 0;
        while (reader2.next()) {
            Integer id = reader2.getInt(0);
            String name = reader2.get(3);
            Double fare = reader2.getDouble(9);
            if (count == 0) {
                assertEquals(Integer.valueOf(1), id);
                assertEquals("Braund, Mr. Owen Harris", name);
                assertEquals(7.250000, fare, 0.001);
            } else if (count == 9) {
                assertEquals(Integer.valueOf(10), id);
                assertEquals("Nasser, Mrs. Nicholas (Adele Achem)", name);
                assertEquals(30.070800, fare, 0.001);
            }
            count2++;
        }
        assertEquals(18, count2);
    }
}
