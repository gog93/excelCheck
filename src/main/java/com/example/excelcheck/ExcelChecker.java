package com.example.excelcheck;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelChecker {

    public static void main(String[] args) {
        try {
            FileInputStream excelFile = new FileInputStream("C:\\Users\\gohar\\IdeaProjects\\excelCheck\\example.xlsx");
            FileInputStream excelFil = new FileInputStream("C:\\Users\\gohar\\IdeaProjects\\excelCheck\\exampl.xlsx");
            String[] columnNames = {"study_design", "mut1_genotype", "mut2_genotype", "mut3_genotype"};
            Map<String, List<String>> stringListMap = symptomsAnalyse(excelFile, columnNames);
            Map<String, Map<Integer, String>> stringMapMap = symptomsAnalyse(excelFil, columnNames, stringListMap);
            for (Map.Entry<String, Map<Integer, String>> entry : stringMapMap.entrySet()) {
                String key = entry.getKey();
                Map<Integer, String> innerMap = entry.getValue();

                for (Map.Entry<Integer, String> inner : innerMap.entrySet()) {
                    String secondString = inner.getValue();
                    List<String> stringList = stringListMap.get(key);

                    // Compare the second string with each string in the list
                    for (String str : stringList) {
                     int distance = LevenshteinDistance.getDefaultInstance().apply(secondString, str);
                        boolean a = distance <= Math.max(secondString.length(), str.length()) / 2;
                        if (a) {
                            System.out.println(inner.getKey()+" " + secondString + "' and '" + str);

                        }
                        // You can use the Levenshtein distance as needed
                    }
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    private static Map<String, List<String>> symptomsAnalyse(FileInputStream excelFile, String[] columnNames) {
        Map<String, List<String>> columnValuesMap = new HashMap<>();
        try {
            // Load the Excel file
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            // Define the column names

            // Find the column indices based on the column names
            int[] columnIndices = new int[columnNames.length];
            Row headerRow = sheet.getRow(0);

            for (int i = 0; i < columnNames.length; i++) {
                columnIndices[i] = -1;
                for (Cell cell : headerRow) {
                    if (cell.getStringCellValue().equals(columnNames[i])) {
                        columnIndices[i] = cell.getColumnIndex();
                        break;
                    }
                }

                if (columnIndices[i] == -1) {
                    System.out.println("Column not found: " + columnNames[i]);
                    return columnValuesMap;
                }
            }

            // Check each value in the specified columns
// Check each value in the specified columns
            for (int rowIndex = 1; rowIndex <= 15; rowIndex++) {
                Row currentRow = sheet.getRow(rowIndex);
                for (int i = 0; i < columnNames.length; i++) {
                    Cell cell = currentRow.getCell(columnIndices[i]);
                    String columnName = columnNames[i];

                    // If the column name is not already in the map, create a new list for it
                    columnValuesMap.putIfAbsent(columnName, new ArrayList<>());

                    // Check the cell type and handle accordingly
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:

                                columnValuesMap.get(columnName).add(cell.getStringCellValue());
                                break;
                            case NUMERIC:
                                // Handle numeric values, e.g., format as string
                                columnValuesMap.get(columnName).add(String.valueOf(cell.getNumericCellValue()));
                                break;
                            default:
                                break;
                        }
                    } else {
                        // Add an empty string if the cell is null
                        break;
                    }
                }
            }
            System.out.println(columnValuesMap);
            // Now, columnValuesMap contains a mapping of column names to lists of row values.

            // You can print or process the values as needed.

        } catch (Exception e) {
            e.printStackTrace();
        }
        return columnValuesMap;
    }

    private static Map<String, Map<Integer, String>> symptomsAnalyse(FileInputStream excelFile, String[] columnNames, Map<String, List<String>> symptomsMap) {
        Map<String, Map<Integer, String>> map = new HashMap<>();
        try {
            // Load the Excel file
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0); // Assuming data is in the first sheet

            // Define the column names

            // Find the column indices based on the column names
            int[] columnIndices = new int[columnNames.length];
            Row headerRow = sheet.getRow(0);

            for (int i = 0; i < columnNames.length; i++) {
                columnIndices[i] = -1;
                for (Cell cell : headerRow) {
                    if (cell.getStringCellValue().equals(columnNames[i])) {
                        columnIndices[i] = cell.getColumnIndex();
                        break;
                    }
                }

                if (columnIndices[i] == -1) {
                    System.out.println("Column not found: " + columnNames[i]);
                    return map;
                }
            }
            boolean check = true;
            for (int rowIndex = 16; rowIndex <= 111; rowIndex++) {
                Row currentRow = sheet.getRow(rowIndex);
                for (int i = 0; i < columnNames.length; i++) {
                    Cell cell = currentRow.getCell(columnIndices[i]);
                    String columnName = columnNames[i];

                    map.putIfAbsent(columnName, new HashMap<>());

                    // Check the cell type and handle accordingly
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:

                                boolean contains = symptomsMap.get(columnName).contains(cell.getStringCellValue());
                                if (!contains) {
                                    map.get(columnName).put(rowIndex, cell.getStringCellValue());
                                }
                                break;
                            case NUMERIC:
                                contains = symptomsMap.get(columnName).contains(String.valueOf(cell.getNumericCellValue()));
                                if (!contains) {
                                    map.get(columnName).put(rowIndex, String.valueOf(cell.getNumericCellValue()));
                                }

                                break;
                            default:
                                break;
                        }
                    } else {
                        // Add an empty string if the cell is null
                        break;
                    }
                }
            }
            System.out.println(map);
            // Now, columnValuesMap contains a mapping of column names to lists of row values.

            // You can print or process the values as needed.

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

}