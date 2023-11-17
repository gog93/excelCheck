package com.example.excelcheck;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.*;
import java.util.stream.IntStream;
@Service
public class ExcelCheckerService {
   public Map<String, Set<String>> symptomsAnalyse(FileInputStream excelFile, String[] columnNames) {
        Map<String, Set<String>> columnValuesMap = new HashMap<>();

        try {
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0);

            int[] columnIndices = Arrays.stream(columnNames)
                    .mapToInt(name -> IntStream.range(0, sheet.getRow(0).getLastCellNum())
                            .filter(i -> sheet.getRow(0).getCell(i).getStringCellValue().equals(name))
                            .findFirst().orElse(-1))
                    .toArray();

            IntStream.range(1, 16).forEach(rowIndex -> {
                Row currentRow = sheet.getRow(rowIndex);
                IntStream.range(0, columnNames.length).forEach(i -> {
                    Cell cell = currentRow.getCell(columnIndices[i]);
                    String columnName = columnNames[i];

                    columnValuesMap.computeIfAbsent(columnName, k -> new HashSet<>());

                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                columnValuesMap.get(columnName).add(cell.getStringCellValue());
                                break;
                            case NUMERIC:
                                columnValuesMap.get(columnName).add(String.valueOf(cell.getNumericCellValue()));
                                break;
                            default:
                                break;
                        }
                    }
                });
            });

            System.out.println(columnValuesMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return columnValuesMap;
    }

    public Map<String, Map<Integer, String>> symptomsAnalyse(FileInputStream excelFile, String[] columnNames, Map<String, Set<String>> symptomsMap) {
        Map<String, Map<Integer, String>> map = new HashMap<>();

        try {
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0);

            int[] columnIndices = Arrays.stream(columnNames)
                    .mapToInt(name -> IntStream.range(0, sheet.getRow(0).getLastCellNum())
                            .filter(i -> sheet.getRow(0).getCell(i).getStringCellValue().equals(name))
                            .findFirst().orElse(-1))
                    .toArray();

            IntStream.range(16, 112).forEach(rowIndex -> {
                Row currentRow = sheet.getRow(rowIndex);
                IntStream.range(0, columnNames.length).forEach(i -> {
                    Cell cell = currentRow.getCell(columnIndices[i]);
                    String columnName = columnNames[i];

                    map.computeIfAbsent(columnName, k -> new HashMap<>());

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
                    }
                });
            });

            System.out.println(map);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

   public Map<Integer, Map<String, String>> levenshteinDistance(Map<String, Map<Integer, String>> stringMapMap, Map<String, Set<String>> stringListMap) {
        Map<Integer, Map<String, String>> levenshteinDistance = new HashMap<>();

        stringMapMap.forEach((key, innerMap) ->
                innerMap.forEach((innerKey, innerValue) -> {
                    Set<String> stringList = stringListMap.get(key);
                    Map<String, String> innerLevenshteinDistance = new HashMap<>();

                    stringList.stream()
                            .filter(str -> {
                                int distance = LevenshteinDistance.getDefaultInstance().apply(str, innerValue);
                                int maxThreshold = Math.max(innerValue.length(), str.length()) / 2;
                                int threshold = 2; // Adjust the threshold as needed

                                // Adjust the threshold based on the length of the strings
                                if (innerValue.length() >= 3 && str.length() >= 3) {
                                    threshold = 3; // or any other value suitable for your case
                                }

                                return distance <= maxThreshold || (innerValue.length() <= threshold && distance <= threshold);
                            })
                            .findFirst()
                            .ifPresent(match -> innerLevenshteinDistance.put(innerValue, match));

                    if (!innerLevenshteinDistance.isEmpty()) {
                        levenshteinDistance.put(innerKey, innerLevenshteinDistance);
                        System.out.println(levenshteinDistance);
                    }

                }));

        return levenshteinDistance;
    }
}
