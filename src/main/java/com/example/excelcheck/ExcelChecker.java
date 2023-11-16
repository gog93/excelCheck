package com.example.excelcheck;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExcelChecker {

    public static void main(String[] args) {
        try {
            String exampleFilePath = "C:\\Users\\gohar\\IdeaProjects\\excelCheck\\example.xlsx";
            String examplFilePath = "C:\\Users\\gohar\\IdeaProjects\\excelCheck\\exampl.xlsx";

            FileInputStream exampleFileInputStream = new FileInputStream(exampleFilePath);
            FileInputStream examplFileInputStream = new FileInputStream(examplFilePath);

            String[] columnNames = {"study_design", "mut1_genotype", "mut2_genotype", "mut3_genotype"};

            Map<String, List<String>> symptomsMap = extractColumnValues(exampleFileInputStream, columnNames);
            Map<String, Map<Integer, Boolean>> result = analyzeSymptoms(examplFileInputStream, columnNames, symptomsMap);

            System.out.println(symptomsMap);
            System.out.println(result);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, List<String>> extractColumnValues(FileInputStream excelFile, String[] columnNames) {
        Map<String, List<String>> columnValuesMap = new HashMap<>();

        try {
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0);

            IntStream.range(0, columnNames.length).forEach(i -> {
                int columnIndex = findColumnIndex(sheet.getRow(0), columnNames[i]);
                if (columnIndex != -1) {
                    List<String> valuesList = IntStream.range(1, 16)
                            .mapToObj(rowIndex -> sheet.getRow(rowIndex).getCell(columnIndex))
                            .map(cell -> handleCellType(cell))
                            .collect(Collectors.toList());

                    columnValuesMap.put(columnNames[i], valuesList);
                } else {
                    System.out.println("Column not found: " + columnNames[i]);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return columnValuesMap;
    }

    private static Map<String, Map<Integer, Boolean>> analyzeSymptoms(FileInputStream excelFile, String[] columnNames, Map<String, List<String>> symptomsMap) {
        Map<String, Map<Integer, Boolean>> resultMap = new HashMap<>();

        try {
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0);

            IntStream.range(0, columnNames.length).forEach(i -> {
                int columnIndex = findColumnIndex(sheet.getRow(0), columnNames[i]);
                if (columnIndex != -1) {
                    Map<Integer, Boolean> valuesMap = IntStream.range(16, 112)
                            .boxed()
                            .collect(Collectors.toMap(rowIndex -> rowIndex, rowIndex -> {
                                Cell cell = sheet.getRow(rowIndex).getCell(columnIndex);
                                return handleCellType(cell, symptomsMap.get(columnNames[i]));
                            }));

                    resultMap.put(columnNames[i], valuesMap);
                } else {
                    System.out.println("Column not found: " + columnNames[i]);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultMap;
    }

    private static int findColumnIndex(Row headerRow, String columnName) {
        return IntStream.range(0, headerRow.getLastCellNum())
                .filter(i -> headerRow.getCell(i).getStringCellValue().equals(columnName))
                .findFirst()
                .orElse(-1);
    }

    private static String handleCellType(Cell cell) {
        return Optional.ofNullable(cell)
                .map(c -> {
                    switch (c.getCellType()) {
                        case STRING:
                            return c.getStringCellValue();
                        case NUMERIC:
                            return String.valueOf(c.getNumericCellValue());
                        default:
                            return "";
                    }
                })
                .orElse("");
    }

    private static boolean handleCellType(Cell cell, List<String> symptomsList) {
        return Optional.ofNullable(cell)
                .map(c -> {
                    switch (c.getCellType()) {
                        case STRING:
                            return symptomsList.contains(c.getStringCellValue());
                        case NUMERIC:
                            return symptomsList.contains(String.valueOf(c.getNumericCellValue()));
                        default:
                            return false;
                    }
                })
                .orElse(false);
    }
}
