package com.example.excelcheck;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class ExcelChecker {
    @Autowired
    private AttachementUploadService attachmentService;

    @GetMapping("/checkExcel")
    public String firstStep(Model model) {
//        model.addAttribute("retypeEmail", new RetypeEmail());
        return "checkExcel";
    }

    @PostMapping("/fileUpload")

    public String uploadAttachment(@RequestPart MultipartFile file) {
        String examplFilePath = attachmentService.upload(file);
        try {
            String exampleFilePath = "C:\\Users\\gohar\\IdeaProjects\\excelCheck\\example.xlsx";

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
        return "checkExcel";
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
                    Map<Integer, Boolean> valuesMap = IntStream.range(16, 110)
                            .boxed()
                            .collect(Collectors.toMap(rowIndex -> rowIndex, rowIndex -> {
                                Cell cell = sheet.getRow(rowIndex - 1).getCell(columnIndex);
                                return !handleCellType(cell, symptomsMap.get(columnNames[i]));
                            }))
                            .entrySet().stream()
                            .filter(entry -> entry.getValue()) // Filter only values that are true
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    resultMap.put(columnNames[i], valuesMap);
                } else {
                    // If columnIndex is -1, add to resultMap only if the condition is false
                    boolean condition = false; // Replace with your actual condition
                    if (!condition) {
                        Map<Integer, Boolean> valuesMap = IntStream.range(16, 112)
                                .boxed()
                                .collect(Collectors.toMap(rowIndex -> rowIndex, rowIndex -> false));
                        resultMap.put(columnNames[i], valuesMap);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        resultMap.forEach((key, innerMap) ->
                innerMap.forEach((innerKey, value) -> innerMap.put(innerKey, false)));

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
