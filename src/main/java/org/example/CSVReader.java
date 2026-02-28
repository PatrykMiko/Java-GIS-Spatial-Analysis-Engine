package org.example;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom, lightweight CSV parser.
 * Designed to handle standard delimiters, missing values, and complex quoted strings
 * without relying on heavy external dependencies like Apache Commons CSV.
 */
public class CSVReader {
    BufferedReader reader;
    String delimiter;
    boolean hasHeader;
    List<String> columnLabels = new ArrayList<>();
    Map<String,Integer> columnLabelsToInt = new HashMap<>();
    String[] current;

    public CSVReader(String filename, String delimiter, boolean hasHeader) {
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        this.delimiter = delimiter;
        this.hasHeader = hasHeader;
        if(hasHeader) parseHeader();
    }

    public CSVReader(String filename, String delimiter){
        this(filename, delimiter, false);
    }

    public CSVReader(String filename){
        this(filename, ",", false);
    }

    public CSVReader(Reader reader, String delimiter, boolean hasHeader){
        this.reader = (reader instanceof BufferedReader) ? (BufferedReader) reader : new BufferedReader(reader);
        this.delimiter = delimiter;
        this.hasHeader = hasHeader;
        if(hasHeader) parseHeader();
    }

    List<String> getColumnLabels(){
        return columnLabels;
    }

    void parseHeader() {
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (line == null) {
            return;
        }
        String[] header = line.split(delimiter);
        for (int i = 0; i < header.length; i++) {
            columnLabels.add(header[i]);
            columnLabelsToInt.put(header[i], i);
        }
    }

    /**
     * Reads the next row from the CSV file.
     * Uses Regex to safely split by delimiter while ignoring delimiters inside quotes.
     */
    boolean next(){
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (line == null) {
            return false;
        }

        // Advanced Regex: Matches delimiters that are followed by an even number of quotes.
        String regex = String.format("%s(?=([^\"]*\"[^\"]*\")*[^\"]*$)", delimiter);
        String[] row = line.split(regex);

        current = row;
        for (int i = 0; i < row.length; i++) {
            current[i] = unquote(row[i]);
        }
        return true;
    }

    private String unquote(String text){
        if (text == null) return null;
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        } else {
            return text;
        }
    }

    int getRecordLength(){
        return current.length;
    }

    boolean isMissing(int columnIndex){
        if (columnIndex < 0 || columnIndex >= current.length) {
            return true;
        }
        return current[columnIndex] == null || current[columnIndex].isEmpty();
    }

    boolean isMissing(String columnLabel){
        if (!columnLabelsToInt.containsKey(columnLabel)) {
            return true;
        }
        return current[columnLabelsToInt.get(columnLabel)] == null || current[columnLabelsToInt.get(columnLabel)].isEmpty();
    }

    String get(int columnIndex){
        if(isMissing(columnIndex)) return null;
        return current[columnIndex];
    }

    String get(String columnLabel){
        if(isMissing(columnLabel)) return null;
        return current[columnLabelsToInt.get(columnLabel)];
    }

    // --- Type conversion utility methods below ---
    // Decided to handle parsing exceptions gracefully by returning null,
    // which is essential for messy real-world CSV data.

    Integer getInt(int columnIndex){
        try { return Integer.parseInt(get(columnIndex)); }
        catch (RuntimeException e) { return null; }
    }

    Integer getInt(String columnLabel){
        try { return Integer.parseInt(get(columnLabel)); }
        catch (RuntimeException e) { return null; }
    }

    Long getLong(int columnIndex){
        try { return Long.parseLong(get(columnIndex)); }
        catch (RuntimeException e) { return null; }
    }

    Long getLong(String columnLabel){
        try { return Long.parseLong(get(columnLabel)); }
        catch (RuntimeException e) { return null; }
    }

    Double getDouble(int columnIndex){
        try { return Double.parseDouble(get(columnIndex)); }
        catch (RuntimeException e) { return null; }
    }

    Double getDouble(String columnLabel){
        try { return Double.parseDouble(get(columnLabel)); }
        catch (RuntimeException e) { return null; }
    }

    LocalTime getTime(int columnIndes, String format){
        try { return LocalTime.parse(get(columnIndes), DateTimeFormatter.ofPattern(format)); }
        catch (RuntimeException e) { return null; }
    }
    LocalTime getTime(String columnLabel, String format){
        try { return LocalTime.parse(get(columnLabel), DateTimeFormatter.ofPattern(format)); }
        catch (RuntimeException e) { return null; }
    }
    LocalDate getDate(int columnIndes, String format){
        try { return LocalDate.parse(get(columnIndes), DateTimeFormatter.ofPattern(format)); }
        catch (RuntimeException e) { return null; }
    }
    LocalDate getDate(String columnLabel, String format){
        try { return LocalDate.parse(get(columnLabel), DateTimeFormatter.ofPattern(format)); }
        catch (RuntimeException e) { return null; }
    }
    LocalDateTime getDateTime(int columnIndes, String format){
        try { return LocalDateTime.parse(get(columnIndes), DateTimeFormatter.ofPattern(format)); }
        catch (RuntimeException e) { return null; }
    }
    LocalDateTime getDateTime(String columnLabel, String format){
        try { return LocalDateTime.parse(get(columnLabel), DateTimeFormatter.ofPattern(format)); }
        catch (RuntimeException e) { return null; }
    }
}
