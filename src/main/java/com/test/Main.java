package com.test;

import java.util.*;

public class Main {

    private static final List<String> EXPECTED_FIELDS = Arrays.asList(
        "screatinine",
        "bloodUrea",
        "bun",
        "uricAcid",
        "gfr",
        "bloodUreaNitrogenToCreatinineRatio",
        "ureaToCreatinineRatio",
        "vdrL",
        "ldlToHdlRatio",
        "urineBileSalts",
        "stoolColor",
        "stoolConsistency",
        "stoolMucus",
        "stoolOccultBlood",
        "stoolPusCells",
        "stoolRedBloodCells",
        "stoolEpithelialCells",
        "stoolBacteria"
    );

    public static void main(String[] args) {
        String pdfUrl = "https://drive.google.com/uc?export=download&id=1bcADf4IuzGhjO7wLcD7jr70n4eZswo90";

        System.out.println("========================================");
        System.out.println("PredLabs PDF Parser Fix Verification");
        System.out.println("========================================");

        System.out.println("\nExtracting text from PDF...");
        String extractedText = Utils.extractTextFromPdfUrl(pdfUrl);

        if (extractedText.isEmpty()) {
            System.out.println("FAILED to extract text from PDF. Check URL.");
            return;
        }

        System.out.println("Text extracted (" + extractedText.length() + " chars)");
        System.out.println("\nRunning predLabsReportExtraction...");

        ParserService parserService = new ParserService();
        Map<String, Object> result = parserService.predLabsReportExtraction(extractedText);

        System.out.println("\n========== FULL OUTPUT MAP ==========");
        result.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.println(e.getKey() + " = " + e.getValue()));

        System.out.println("\n========== FIX VERIFICATION ==========");
        int pass = 0;
        int fail = 0;
        List<String> failedFields = new ArrayList<>();

        for (String field : EXPECTED_FIELDS) {
            Object val = result.get(field);
            if (val != null && !val.toString().trim().isEmpty() && !val.toString().equalsIgnoreCase("null")) {
                System.out.println("  PASS  " + field + " = " + val);
                pass++;
            } else {
                System.out.println("  FAIL  " + field + " = NULL");
                fail++;
                failedFields.add(field);
            }
        }

        System.out.println("\n========== RESULT ==========");
        System.out.println("PASS: " + pass + " / " + (pass + fail));
        System.out.println("FAIL: " + fail + " / " + (pass + fail));

        if (fail == 0) {
            System.out.println("\nALL 18 FIXES VERIFIED SUCCESSFULLY!");
        } else {
            System.out.println("\nSOME FIELDS STILL FAILING:");
            for (String f : failedFields) {
                System.out.println("  - " + f);
            }

            System.out.println("\n========== DEBUG: Raw PDF Lines ==========");
            List<String> lines = Arrays.asList(extractedText.split("\\r?\\n"));
            for (String field : failedFields) {
                String keyword = field
                        .replaceAll("([a-z])([A-Z])", "$1 $2")
                        .split(" ")[0];
                System.out.println("\n--- Lines containing '" + keyword + "' (for field: " + field + ") ---");
                for (String line : lines) {
                    if (line.toLowerCase().contains(keyword.toLowerCase())) {
                        System.out.println("  [" + line + "]");
                    }
                }
            }
        }
    }
}
