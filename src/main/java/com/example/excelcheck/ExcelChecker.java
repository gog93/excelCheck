package com.example.excelcheck;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;

import org.apache.poi.ss.usermodel.*;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
@Controller
@RequiredArgsConstructor
public class ExcelChecker {
    @Autowired
    private AttachementUploadService attachmentService;

    @GetMapping("/checkExcel")
    public String firstStep(Model model) {
        return "checkExcel";
    }

    @PostMapping("/fileUpload")

    public String uploadAttachment(@RequestPart MultipartFile file, Model model) {
        String examplFilePath = attachmentService.upload(file);
        try {
            FileInputStream excelFile1 = new FileInputStream("C:\\Users\\gohar\\IdeaProjects\\excelCheck\\example.xlsx");
            FileInputStream excelFile2 = new FileInputStream("C:\\Users\\gohar\\IdeaProjects\\excelCheck\\exampl.xlsx");
            String[] columnNames = {"study_design", "mut1_genotype", "mut2_genotype", "mut3_genotype"};

            Map<String, Set<String>> stringListMap = symptomsAnalyse(excelFile1, columnNames);
            Map<String, Map<Integer, String>> stringMapMap = symptomsAnalyse(excelFile2, columnNames, stringListMap);
            Map<Integer, Map<String, String>> integerMapMap = levenshteinDistance(stringMapMap, stringListMap);
        model.addAttribute("map",stringMapMap );
        model.addAttribute("levenshteinDistance",integerMapMap );

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "checkExcel";

    }

    private static Map<String, Set<String>> symptomsAnalyse(FileInputStream excelFile, String[] columnNames) {
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

    private static Map<String, Map<Integer, String>> symptomsAnalyse(FileInputStream excelFile, String[] columnNames, Map<String, Set<String>> symptomsMap) {
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

    private static Map<Integer, Map<String, String>> levenshteinDistance(Map<String, Map<Integer, String>> stringMapMap, Map<String, Set<String>> stringListMap) {
        Map<Integer, Map<String, String>> levenshteinDistance = new HashMap<>();

        stringMapMap.forEach((key, innerMap) ->
                innerMap.forEach((innerKey, innerValue) -> {
                    Set<String> stringList = stringListMap.get(key);
                    Map<String, String> innerLevenshteinDistance = new HashMap<>();

                    stringList.stream()
                            .filter(str -> LevenshteinDistance.getDefaultInstance().apply(innerValue, str)
                                    <= Math.max(innerValue.length(), str.length()) / 2)
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
