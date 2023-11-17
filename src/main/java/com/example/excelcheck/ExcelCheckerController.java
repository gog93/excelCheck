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
public class ExcelCheckerController {
    @Autowired
    private AttachementUploadService attachmentService;
    @Autowired
    private ExcelCheckerService excelCheckerService;

    @GetMapping("/checkExcel")
    public String firstStep(Model model) {
        return "checkExcel";
    }

    @PostMapping("/fileUpload")

    public String uploadAttachment(@RequestPart MultipartFile file, Model model) {
        String examplFilePath = attachmentService.upload(file);
        try {
            FileInputStream excelFile1 = new FileInputStream("C:\\Users\\gohar\\IdeaProjects\\excelCheck\\example.xlsx");
            FileInputStream excelFile2 = new FileInputStream(examplFilePath);
            String[] columnNames = {"study_design", "mut1_genotype", "mut2_genotype", "mut3_genotype"};

            Map<String, Set<String>> stringListMap = excelCheckerService.symptomsAnalyse(excelFile1, columnNames);
            Map<String, Map<Integer, String>> stringMapMap = excelCheckerService.symptomsAnalyse(excelFile2, columnNames, stringListMap);
            Map<Integer, Map<String, String>> integerMapMap = excelCheckerService.levenshteinDistance(stringMapMap, stringListMap);
        model.addAttribute("map",stringMapMap );
        model.addAttribute("levenshteinDistance",integerMapMap );

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "checkExcel";

    }


}
