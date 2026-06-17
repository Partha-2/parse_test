package com.test;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ParserService {

    private static final String PARSED = "_parsed";

    public Map<String, Object> predLabsReportExtraction(String extractedText) {
        Map<String, Object> bloodReportData = new HashMap<>();
        if (StringUtils.isEmpty(extractedText)) {
            return bloodReportData;
        }

        List<String> lines = Arrays.asList(extractedText.split("\n"));
        bloodReportData.put("BLOODTEST" + PARSED, true);

        extractPatientInfoForPredLabs(lines, bloodReportData);
        extractCBCDataForPredLabs(lines, bloodReportData);
        extractESRForPredLabs(lines, bloodReportData);
        extractHba1cForPredLabs(lines, bloodReportData);
        extractLipidProfileForPredLabs(lines, bloodReportData);
        extractRenalProfileForPredLabs(lines, bloodReportData);
        extractLiverFunctionForPredLabs(lines, bloodReportData);
        extractGlucoseForPredLabs(lines, bloodReportData);
        extractPsaForPredLabs(lines, bloodReportData);
        extractUrineDataForPredLabs(lines, bloodReportData);
        extractVitaminProfileForPredLabs(lines, bloodReportData);
        extractCrpForPredLabs(lines, bloodReportData);
        extractStoolDataForPredLabs(lines, bloodReportData);

        return bloodReportData;
    }

    private Optional<String> getFieldValue(List<String> lines, String fieldName) {
        return Utils.getSafeStream(lines)
                .filter(line -> line.trim().startsWith(fieldName))
                .findFirst()
                .map(String::trim);
    }

    private boolean isValidNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void extractPatientInfoForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        Optional<String> nameLine = getFieldValue(lines, "Patient Name :");
        nameLine.ifPresent(line -> {
            String[] parts = line.split(":", 2);
            if (parts.length > 1) {
                String name = parts[1]
                        .replaceAll("Patient ID.*", "")
                        .replaceAll("(?i)\\b(Mr\\.|Mrs\\.|Miss|Ms\\.|Dr\\.)\\s*", "")
                        .trim();
                bloodReportData.put("BLOOD_PATIENT_NAME_REPORT", name);
            }
        });

        Optional<String> ageSexLine = getFieldValue(lines, "Age / Sex :");
        ageSexLine.ifPresent(line -> {
            String[] parts = line.split(":", 2);
            if (parts.length > 1) {
                String ageSex = parts[1].trim();
                String[] tokens = ageSex.split("/");
                if (tokens.length >= 2) {
                    bloodReportData.put("AGE", tokens[0].replaceAll("[^0-9]", "").trim());
                    bloodReportData.put("GENDER", tokens[1].trim().split("\\s+")[0].trim());
                }
            }
        });

        Optional<String> reportedOnLine = getFieldValue(lines, "Reported On :");
        reportedOnLine.ifPresent(line -> {
            String value = line.replaceFirst("(?i).Reported On\\s:\\s*", "");
            value = value.split("(?=HAEMATOLOGY|Sample ID|Patient ID|Ref\\. Doctor|Client Name)")[0].trim();
            bloodReportData.put("SAMPLE_REPORTED_DATE", value);
        });

        Optional<String> sampleTimeLine = getFieldValue(lines, "Sample Receiving Time :");
        sampleTimeLine.ifPresent(line -> {
            String value = line.replaceFirst("(?i).Sample Receiving Time\\s:\\s*", "");
            value = value.split("(?=Registration On|Reported On|Ref\\. Doctor|Client Name)")[0].trim();
            bloodReportData.put("SAMPLE_COLLECTED_DATE", value);
        });
    }

    private void extractCBCDataForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        Optional<String> haemoglobin = getFieldValue(lines, "Haemoglobin");
        haemoglobin.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("HB", part);
                    break;
                }
            }
        });

        Optional<String> rbc = getFieldValue(lines, "RBC");
        rbc.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("RBC", part);
                    break;
                }
            }
        });

        Optional<String> wbc = getFieldValue(lines, "Total WBC Count");
        wbc.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("WBC", String.valueOf(Double.parseDouble(part) * 1000));
                    break;
                }
            }
        });

        Optional<String> pcv = getFieldValue(lines, "Haematocrit/Packed Cell Volume");
        pcv.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Packed Cell Volume", part);
                    break;
                }
            }
        });

        Optional<String> mcv = getFieldValue(lines, "MCV");
        mcv.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("MCV", part);
                    break;
                }
            }
        });

        Optional<String> mch = getFieldValue(lines, "MCH");
        mch.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("MCH", part);
                    break;
                }
            }
        });

        Optional<String> mchc = getFieldValue(lines, "MCHC");
        mchc.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("MCHC", part);
                    break;
                }
            }
        });

        Optional<String> plcr = getFieldValue(lines, "P-LCR");
        plcr.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("PLATELET.PLCR", part);
                    break;
                }
            }
        });

        Optional<String> rdwSd = getFieldValue(lines, "RDW-SD");
        rdwSd.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("RDW (SD)", part);
                    break;
                }
            }
        });

        Optional<String> rdwCv = getFieldValue(lines, "RDW-CV");
        rdwCv.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("RDW (CV)", part);
                    break;
                }
            }
        });

        Optional<String> pct = getFieldValue(lines, "Platelet Crit (PCT)");
        pct.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("PLATELET.PCT", part);
                    break;
                }
            }
        });

        Optional<String> platelet = getFieldValue(lines, "Platelet Count");
        platelet.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("PLATELET", String.valueOf(Double.parseDouble(part) / 100));
                    break;
                }
            }
        });

        Optional<String> neutrophilPct = getFieldValue(lines, "Neutrophils");
        neutrophilPct.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Neutrophils", part);
                    break;
                }
            }
        });

        Optional<String> lymphocytePct = getFieldValue(lines, "Lymphocytes");
        lymphocytePct.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Lymphocytes", part);
                    break;
                }
            }
        });

        Optional<String> monocytePct = getFieldValue(lines, "Monocytes");
        monocytePct.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Monocytes", part);
                    break;
                }
            }
        });

        Optional<String> eosinophilPct = getFieldValue(lines, "Eosinophil");
        eosinophilPct.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Eosinophils", part);
                    break;
                }
            }
        });

        Optional<String> basophilPct = getFieldValue(lines, "Basophils");
        basophilPct.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Basophils", part);
                    break;
                }
            }
        });

        Optional<String> igPct = getFieldValue(lines, "Immature Granulocyte");
        igPct.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Immature Granulocyte", part);
                    break;
                }
            }
        });

        Optional<String> neutrophilAbs = getFieldValue(lines, "Absolute Neutrophils Count");
        neutrophilAbs.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Absolute Neutrophils Count",
                            String.valueOf(Double.parseDouble(part.replaceAll(",", ".")) * 1000));
                    break;
                }
            }
        });

        Optional<String> lymphocyteAbs = getFieldValue(lines, "Absolute Lymphocyte Count");
        lymphocyteAbs.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Absolute Lymphocytes Count",
                            String.valueOf(Double.parseDouble(part) * 1000));
                    break;
                }
            }
        });

        Optional<String> eosinophilAbs = getFieldValue(lines, "Absolute Eosinophil Count");
        eosinophilAbs.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Absolute Eosinophils Count",
                            String.valueOf(Double.parseDouble(part) * 1000));
                    break;
                }
            }
        });

        Optional<String> basophilAbs = getFieldValue(lines, "Absolute Basophils Count");
        basophilAbs.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Absolute Basophil Count",
                            String.valueOf(Double.parseDouble(part) * 1000));
                    break;
                }
            }
        });

        Optional<String> monocyteAbs = getFieldValue(lines, "Absolute Monocyte Count");
        monocyteAbs.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Absolute Monocytes Count",
                            String.valueOf(Double.parseDouble(part) * 1000));
                    break;
                }
            }
        });

        Optional<String> igAbs = getFieldValue(lines, "Absolute Immature");
        igAbs.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Immature Granulocytes.Absolute", part);
                    break;
                }
            }
        });
    }

    private void extractESRForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        Optional<String> esr = getFieldValue(lines, "ESR ( Erythrocyte");
        esr.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("ESR", part);
                    break;
                }
            }
        });
    }

    private void extractHba1cForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        Optional<String> hba1c = getFieldValue(lines, "HbA1c");
        hba1c.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("HBA1C", part);
                    break;
                }
            }
        });

        Optional<String> abg = Utils.getSafeStream(lines).filter(x -> x.startsWith("AVERAGE BLOOD GLUCOSE")).findFirst();
        abg.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("ESTIMATED_AVERAGE_GLUCOSE", part);
                    break;
                }
            }
        });
    }

    // ====================== LIPID PROFILE (FIX 2 APPLIED) ======================

    private void extractLipidProfileForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        Optional<String> totalCholesterol = Utils.getSafeStream(lines).filter(x -> x.startsWith("Total Cholesterol - serum") || x.startsWith("Cholesterol, Total")).findFirst();
        totalCholesterol.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("S.CHOLESTEROL", part);
                    break;
                }
            }
        });

        Optional<String> triglycerides = Utils.getSafeStream(lines).filter(x -> x.startsWith("Triglycerides -serum") || x.startsWith("Triglycerides")).findFirst();
        triglycerides.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("S.TRIGLYCERIDE", part);
                    break;
                }
            }
        });

        Optional<String> hdl = Utils.getSafeStream(lines).filter(x -> x.startsWith("HDL Cholesterol -serum") || x.startsWith("HDL Cholesterol")).findFirst();
        hdl.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("HDL Cholesterol", part);
                    break;
                }
            }
        });

        Optional<String> nonHdl = Utils.getSafeStream(lines).filter(x -> x.startsWith("Non HDL Cholesterol") || x.startsWith("Non HDL Cholesterolserum")).findFirst();
        nonHdl.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Non HDL Cholesterol", part);
                    break;
                }
            }
        });

        Optional<String> ldl = Utils.getSafeStream(lines).filter(x -> x.startsWith("LDL Cholesterol -serum") || x.startsWith("LDL Cholesterol")).findFirst();
        ldl.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("LDL", part);
                    break;
                }
            }
        });

        Optional<String> vldl = Utils.getSafeStream(lines).filter(x -> x.startsWith("VLDL Cholesterol -serum") || x.startsWith("VLDL Cholesterol")).findFirst();
        vldl.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("VLDL", part);
                    break;
                }
            }
        });

        Optional<String> cholHdlRatio = Utils.getSafeStream(lines).filter(x -> x.startsWith("CHOL/HDL Ratio")).findFirst();
        cholHdlRatio.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("CHOL/DHDL", part);
                    break;
                }
            }
        });

        Optional<String> ldlHdlRatio = Utils.getSafeStream(lines).filter(x -> x.startsWith("Cholesterol LDL/HDL Ratio")).findFirst();
        ldlHdlRatio.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("DLDL/DHDL", part);
                    break;
                }
            }
        });

        // FIX 8: vdrL — changed key from "DHDL/DLDL" to "vdrL"
        Optional<String> hdlLdlRatio = Utils.getSafeStream(lines).filter(x -> x.startsWith("HDL / LDL Cholesterol Ratio")).findFirst();
        hdlLdlRatio.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("vdrL", part);
                    break;
                }
            }
        });

        // FIX 9: ldlToHdlRatio — NEW extraction
        Optional<String> ldlToHdlRatio = Utils.getSafeStream(lines).filter(x -> x.startsWith("Cholesterol LDL/HDL Ratio") || x.startsWith("LDL/HDL Ratio")).findFirst();
        ldlToHdlRatio.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("ldlToHdlRatio", part);
                    break;
                }
            }
        });
    }

    // ====================== RENAL PROFILE (FIX 1 APPLIED) ======================

    private void extractRenalProfileForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        // FIX 3: bun — added x.startsWith("BUN") filter
        Optional<String> bun = Utils.getSafeStream(lines).filter(x -> x.startsWith("BUN") || x.startsWith("Blood Urea Nitrogen BUN")).findFirst();
        bun.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("bun", part);
                    break;
                }
            }
        });

        // FIX 2: bloodUrea — added "Serum Urea" filter, key changed to "bloodUrea"
        Optional<String> bloodUrea = Utils.getSafeStream(lines).filter(x -> x.startsWith("Serum Urea") || (x.startsWith("Blood Urea") && (x.contains("19 - 43") || !x.toLowerCase().contains("bun")))).findFirst();
        bloodUrea.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("bloodUrea", part);
                    break;
                }
            }
        });

        // FIX 1: screatinine — changed filter to "Serum Creatinine", key changed
        Optional<String> creatinine = Utils.getSafeStream(lines).filter(x -> x.startsWith("Serum Creatinine") || x.startsWith("Creatinine")).findFirst();
        creatinine.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("screatinine", part);
                    break;
                }
            }
        });

        // FIX 4: uricAcid — key changed from "URIC_ACID" to "uricAcid"
        Optional<String> uricAcid = getFieldValue(lines, "Serum Uric acid");
        uricAcid.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("uricAcid", part);
                    break;
                }
            }
        });

        // FIX 6: bloodUreaNitrogenToCreatinineRatio — key renamed
        Optional<String> bunCreatinineRatio = getFieldValue(lines, "BUN / Creatinine Ratio");
        bunCreatinineRatio.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("bloodUreaNitrogenToCreatinineRatio", part);
                    break;
                }
            }
        });

        // FIX 7: ureaToCreatinineRatio — key renamed
        Optional<String> ureaCreatinineRatio = getFieldValue(lines, "Urea / Creatinine Ratio");
        ureaCreatinineRatio.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("ureaToCreatinineRatio", part);
                    break;
                }
            }
        });

        // FIX 5: gfr — key changed from "GFR" to "gfr"
        Optional<String> egfr = getFieldValue(lines, "eGFR");
        egfr.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("gfr", part);
                    break;
                }
            }
        });
    }

    private void extractLiverFunctionForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        Optional<String> totalBilirubin = Utils.getSafeStream(lines).filter(x -> x.startsWith("Bilirubin Total - serum")).findFirst();
        totalBilirubin.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Total Bilirubin", part);
                    break;
                }
            }
        });

        Optional<String> directBilirubin = Utils.getSafeStream(lines).filter(x -> x.startsWith("Bilirubin Direct - serum")).findFirst();
        directBilirubin.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Direct Bilirubin", part);
                    break;
                }
            }
        });

        Optional<String> indirectBilirubin = Utils.getSafeStream(lines).filter(x -> x.startsWith("Bilirubin Indirect - serum")).findFirst();
        indirectBilirubin.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Indirect Bilirubin", part);
                    break;
                }
            }
        });

        Optional<String> sgot = Utils.getSafeStream(lines).filter(x -> x.startsWith("SGOT (AST) - serum") || x.startsWith("SGOT (AST)")).findFirst();
        sgot.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("SGOT", part);
                    break;
                }
            }
        });

        Optional<String> sgpt = Utils.getSafeStream(lines).filter(x -> x.startsWith("SGPT (ALT) - serum") || x.startsWith("SGPT (ALT)")).findFirst();
        sgpt.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("SGPT", part);
                    break;
                }
            }
        });

        Optional<String> sgotSgptRatio = Utils.getSafeStream(lines).filter(x -> x.startsWith("SGOT/SGPT Ratio")).findFirst();
        sgotSgptRatio.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("SGOT_SGPT_Ratio", part);
                    break;
                }
            }
        });

        Optional<String> alp = Utils.getSafeStream(lines).filter(x -> x.startsWith("Alkaline Phosphatase - serum") || x.startsWith("Alkaline Phosphatase")).findFirst();
        alp.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Alkaline Phosphatase", part);
                    break;
                }
            }
        });

        Optional<String> totalProtein = Utils.getSafeStream(lines).filter(x -> x.startsWith("Protein Total - serum")).findFirst();
        totalProtein.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Total Protein", part);
                    break;
                }
            }
        });

        Optional<String> albumin = Utils.getSafeStream(lines).filter(x -> x.startsWith("Albumin - serum")).findFirst();
        albumin.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Albumin", part);
                    break;
                }
            }
        });

        Optional<String> globulin = getFieldValue(lines, "Globulin");
        globulin.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("Globulin", part);
                    break;
                }
            }
        });

        Optional<String> agRatio = Utils.getSafeStream(lines).filter(x -> x.startsWith("A/G Ratio")).findFirst();
        agRatio.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("AG Ratio", part);
                    break;
                }
            }
        });

        Optional<String> ggt = Utils.getSafeStream(lines).filter(x -> x.startsWith("GGTP - Gamma GT") || x.startsWith("GGTP - Gamma")).findFirst();
        ggt.ifPresent(v -> {
            for (String part : v.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("GGT", part);
                    break;
                }
            }
        });
    }

    private void extractGlucoseForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        Optional<String> randomGlucose = getFieldValue(lines, "Glucose Random (Plasma)");
        randomGlucose.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("BLOOD SUGAR RANDOM", part);
                    break;
                }
            }
        });
    }

    private void extractPsaForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        Optional<String> psa = getFieldValue(lines, "PSA");
        psa.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("PSA", part);
                    break;
                }
            }
        });
    }

    // ====================== URINE DATA (FIX 3 APPLIED) ======================

    private void extractUrineDataForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        AtomicReference<Boolean> flagUrineHealthy = new AtomicReference<>(true);
        Map<String, String> urineProblems = new HashMap<>();

        Optional<String> colour = getFieldValue(lines, "Colour");
        colour.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            StringBuilder observed = new StringBuilder();
            for (String t : tokens) {
                if (t.equals("_") || t.equals("-")) break;
                if (!t.equalsIgnoreCase("Colour")) observed.append(t).append(" ");
            }
            String urineColor = observed.toString().trim();
            if (!urineColor.isEmpty()) {
                bloodReportData.put("URINE.COLOUR", urineColor);
                if (!urineColor.equalsIgnoreCase("Pale Yellow")
                        && !urineColor.equalsIgnoreCase("Yellow")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Colour", urineColor);
                }
            }
        });

        Optional<String> appearance = getFieldValue(lines, "Appearance");
        appearance.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            StringBuilder observed = new StringBuilder();
            for (String t : tokens) {
                if (t.equals("_") || t.equals("-")) break;
                if (!t.equalsIgnoreCase("Appearance")) observed.append(t).append(" ");
            }
            String val = observed.toString().trim();
            if (!val.isEmpty()) {
                bloodReportData.put("URINE.APPEARANCE", val);
                if (!val.equalsIgnoreCase("Clear")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Appearance", val);
                }
            }
        });

        Optional<String> specificGravity = getFieldValue(lines, "Specific Gravity");
        specificGravity.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("URINE.SPECIFIC_GRAVITY", part);
                    try {
                        double sgVal = Double.parseDouble(part);
                        if (sgVal < 1.003 || sgVal > 1.030) {
                            flagUrineHealthy.set(false);
                            urineProblems.put("Specific Gravity", part);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                    break;
                }
            }
        });

        Optional<String> ph = getFieldValue(lines, "pH");
        ph.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("URINE.Reaction(pH)", part);
                    try {
                        double pHVal = Double.parseDouble(part);
                        if (pHVal < 5.0 || pHVal > 8.0) {
                            flagUrineHealthy.set(false);
                            urineProblems.put("pH", part);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                    break;
                }
            }
        });

        Optional<String> protein = Utils.getSafeStream(lines).filter(x -> x.startsWith("Protein") && x.contains("Negative")).findFirst();
        protein.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 1 ? tokens[1] : null;
            if (val != null) {
                bloodReportData.put("URINE.PROTEIN", val);
                if (!val.equalsIgnoreCase("Absent") && !val.equalsIgnoreCase("Negative")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Protein", val);
                }
            }
        });

        Optional<String> glucose = getFieldValue(lines, "Glucose");
        glucose.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 1 ? tokens[1] : null;
            if (val != null) {
                bloodReportData.put("URINE.GLUCOSE", val);
                if (!val.equalsIgnoreCase("Absent") && !val.equalsIgnoreCase("Negative")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Glucose", val);
                }
            }
        });

        Optional<String> ketones = getFieldValue(lines, "Ketones");
        ketones.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 1 ? tokens[1] : null;
            if (val != null) {
                bloodReportData.put("URINE.KETONE", val);
                if (!val.equalsIgnoreCase("Absent") && !val.equalsIgnoreCase("Negative")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Ketone", val);
                }
            }
        });

        // FIX 10: urineBileSalts — key changed from "URINE.BILE_SALT" to "urineBileSalts"
        Optional<String> bileSalt = getFieldValue(lines, "Bile Salt");
        bileSalt.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 2 ? tokens[2] : null;
            if (val != null) {
                bloodReportData.put("urineBileSalts", val);
                if (!val.equalsIgnoreCase("Negative") && !val.equalsIgnoreCase("Absent")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Bile Salt", val);
                }
            }
        });

        Optional<String> bilePigment = getFieldValue(lines, "Bile Pigment");
        bilePigment.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 2 ? tokens[2] : null;
            if (val != null) {
                bloodReportData.put("URINE.BILE_PIGMENT", val);
                if (!val.equalsIgnoreCase("Negative") && !val.equalsIgnoreCase("Absent")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Bile Pigment", val);
                }
            }
        });

        Optional<String> urobilinogen = getFieldValue(lines, "Urobilinogen");
        urobilinogen.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 1 ? tokens[1] : null;
            if (val != null) {
                bloodReportData.put("URINE.UROBILINOGEN", val);
                try {
                    double numVal = Double.parseDouble(val);
                    if (numVal < 0.2 || numVal > 1.0) {
                        flagUrineHealthy.set(false);
                        urineProblems.put("Urobilinogen", val);
                    }
                } catch (NumberFormatException e) {
                    if (!val.equalsIgnoreCase("Normal")) {
                        flagUrineHealthy.set(false);
                        urineProblems.put("Urobilinogen", val);
                    }
                }
            }
        });

        Optional<String> nitrite = getFieldValue(lines, "Nitrite");
        nitrite.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 1 ? tokens[1] : null;
            if (val != null) {
                bloodReportData.put("URINE.NITRITE", val);
                if (!val.equalsIgnoreCase("Absent") && !val.equalsIgnoreCase("Negative")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Nitrite", val);
                }
            }
        });

        Optional<String> leucocyteEsterase = getFieldValue(lines, "Leucocyte Esterase");
        leucocyteEsterase.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 2 ? tokens[2] : null;
            if (val != null) {
                bloodReportData.put("URINE.LEUCOCYTE_ESTERASE", val);
                if (!val.equalsIgnoreCase("Absent") && !val.equalsIgnoreCase("Negative")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Leucocyte Esterase", val);
                }
            }
        });

        Optional<String> pusCells = getFieldValue(lines, "Pus Cell");
        pusCells.ifPresent(value -> {
            String val = null;
            if (value.toUpperCase().contains("ABSENT")) {
                val = "ABSENT";
            } else {
                for (String part : value.split("\\s+")) {
                    if (isValidNumeric(part) || part.matches("\\d+-\\d+")) {
                        val = part;
                        break;
                    }
                }
            }
            if (val != null) {
                bloodReportData.put("URINE.PUS_CELLS", val);
                try {
                    int count = val.contains("-")
                            ? Integer.parseInt(val.split("-")[1])
                            : Integer.parseInt(val);
                    if (count > 5) {
                        flagUrineHealthy.set(false);
                        urineProblems.put("Pus Cells", val);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        });

        Optional<String> epithelialCells = getFieldValue(lines, "Epithelial Cells");
        epithelialCells.ifPresent(value -> {
            String val = null;
            if (value.toUpperCase().contains("ABSENT")) {
                val = "ABSENT";
            } else {
                for (String part : value.split("\\s+")) {
                    if (isValidNumeric(part) || part.matches("\\d+-\\d+")) {
                        val = part;
                        break;
                    }
                }
            }
            if (val != null) {
                bloodReportData.put("URINE.EPITHELIAL_CELLS", val);
                try {
                    int count = val.contains("-")
                            ? Integer.parseInt(val.split("-")[1])
                            : Integer.parseInt(val);
                    if (count > 5) {
                        flagUrineHealthy.set(false);
                        urineProblems.put("Epithelial Cells", val);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        });

        Optional<String> rbc = getFieldValue(lines, "Red Blood Cells");
        rbc.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 3 ? tokens[3] : null;
            if (val != null) {
                bloodReportData.put("URINE.RED_BLOOD_CELLS", val);
                if (!val.equalsIgnoreCase("Absent") && !val.equalsIgnoreCase("Negative")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("RBC", val);
                }
            }
        });

        Optional<String> casts = getFieldValue(lines, "Casts");
        casts.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 1 ? tokens[1] : null;
            if (val != null) {
                bloodReportData.put("URINE.CASTS", val);
                if (!val.equalsIgnoreCase("Nil") && !val.equalsIgnoreCase("Absent")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Casts", val);
                }
            }
        });

        Optional<String> crystals = getFieldValue(lines, "Crystals");
        crystals.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 1 ? tokens[1] : null;
            if (val != null) {
                bloodReportData.put("URINE.CRYSTALS", val);
                if (!val.equalsIgnoreCase("Nil") && !val.equalsIgnoreCase("Absent")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Crystals", val);
                }
            }
        });

        Optional<String> amorphous = getFieldValue(lines, "Amorphous Deposit");
        amorphous.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 2 ? tokens[2] : null;
            if (val != null) {
                bloodReportData.put("URINE.AMORPHOUS_DEPOSIT", val);
            }
        });

        Optional<String> yeast = getFieldValue(lines, "Yeast Cells");
        yeast.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 2 ? tokens[2] : null;
            if (val != null) {
                bloodReportData.put("URINE.YEAST_CELL", val);
                if (!val.equalsIgnoreCase("Nil") && !val.equalsIgnoreCase("Absent")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Yeast Cells", val);
                }
            }
        });

        Optional<String> bacteria = getFieldValue(lines, "Bacteria");
        bacteria.ifPresent(value -> {
            String[] tokens = value.trim().split("\\s+");
            String val = tokens.length > 1 ? tokens[1] : null;
            if (val != null) {
                bloodReportData.put("URINE.BACTERIA", val);
                if (!val.equalsIgnoreCase("Nil") && !val.equalsIgnoreCase("Absent")) {
                    flagUrineHealthy.set(false);
                    urineProblems.put("Bacteria", val);
                }
            }
        });

        Optional<String> otherFindings = getFieldValue(lines, "Other findings");
        otherFindings.ifPresent(value -> {
            String cleaned = value.replaceFirst("(?i)Other findings", "").trim();
            String observed;
            if (cleaned.matches("(?i)^not seen.*")) {
                observed = "Not Seen";
            } else {
                observed = cleaned.split("(?i)not seen")[0].trim();
            }
            if (!observed.isEmpty() && !observed.equalsIgnoreCase("Not Seen")) {
                bloodReportData.put("URINE.OTHERS", observed);
            }
        });

        if (Objects.nonNull(bloodReportData.get("URINE.COLOUR"))) {
            bloodReportData.put("URINE_PROBLEMS", urineProblems);
        }
    }

    private void extractVitaminProfileForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        Optional<String> vitaminD = Utils.getSafeStream(lines).filter(x -> x.startsWith("Vitamin D Total-25 Hydroxy")
                || x.startsWith("25-Hydroxy Vitamin D Total")).findFirst();
        vitaminD.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("VitaminD", part);
                    break;
                }
            }
        });

        Optional<String> vitaminB12 = Utils.getSafeStream(lines).filter(x -> x.startsWith("VITAMIN B-12") || x.startsWith("Vitamin B-12")
                || x.startsWith("Vitamin - B12")).findFirst();
        vitaminB12.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("VitaminB12", part);
                    break;
                }
            }
        });
    }

    private void extractCrpForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        Optional<String> crp = getFieldValue(lines, "CRP Quantification");
        crp.ifPresent(value -> {
            for (String part : value.split("\\s+")) {
                if (isValidNumeric(part)) {
                    bloodReportData.put("CRP", part);
                    break;
                }
            }
        });
    }

    // ====================== STOOL DATA (FIX 4 APPLIED) ======================

    private void extractStoolDataForPredLabs(List<String> lines, Map<String, Object> bloodReportData) {
        int stoolStart = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().startsWith("STOOL ROUTINE")) {
                stoolStart = i;
                break;
            }
        }

        // FIX 11: stoolColor — key changed from "STOOL.COLOUR" to "stoolColor"
        if (stoolStart != -1) {
            for (int i = stoolStart; i < lines.size(); i++) {
                if (lines.get(i).trim().startsWith("Colour")) {
                    bloodReportData.put("stoolColor", lines.get(i).replaceFirst("Colour", "").trim());
                    break;
                }
            }
        }

        // FIX 12: stoolConsistency — key changed
        Optional<String> stoolConsistency = getFieldValue(lines, "Consistency");
        stoolConsistency.ifPresent(value -> bloodReportData.put("stoolConsistency", value.replaceFirst("Consistency", "").trim()));

        // FIX 13: stoolMucus — key changed
        Optional<String> stoolMucus = getFieldValue(lines, "Mucus");
        stoolMucus.ifPresent(value -> bloodReportData.put("stoolMucus", value.replaceFirst("Mucus", "").trim()));

        // FIX 14: stoolOccultBlood — key changed
        Optional<String> stoolOccultBlood = getFieldValue(lines, "Occult Blood");
        stoolOccultBlood.ifPresent(value -> bloodReportData.put("stoolOccultBlood", value.replaceFirst("Occult Blood", "").trim()));

        Optional<String> stoolReducing = getFieldValue(lines, "Stool Reducing Substance");
        stoolReducing.ifPresent(value -> bloodReportData.put("stoolReducingSugar", value.replaceFirst("Stool Reducing Substance", "").trim()));

        // FIX 15: stoolPusCells — key changed + index fixed from t[2] to t[1]
        Optional<String> stoolPusCells = getFieldValue(lines, "Pus Cells");
        stoolPusCells.ifPresent(value -> {
            String[] t = value.trim().split("\\s+");
            if (t.length > 1) bloodReportData.put("stoolPusCells", t[1]);
        });

        // FIX 16: stoolRedBloodCells — key changed
        Optional<String> stoolRBC = getFieldValue(lines, "RBCs");
        stoolRBC.ifPresent(value -> {
            String[] t = value.trim().split("\\s+");
            if (t.length > 1) bloodReportData.put("stoolRedBloodCells", t[1]);
        });

        // FIX 17: stoolEpithelialCells — key changed
        Optional<String> stoolEpithelial = getFieldValue(lines, "Epithelial Cells");
        stoolEpithelial.ifPresent(value -> {
            String[] t = value.trim().split("\\s+");
            if (t.length > 2) bloodReportData.put("stoolEpithelialCells", t[2]);
        });

        Optional<String> stoolOva = getFieldValue(lines, "Ova");
        stoolOva.ifPresent(value -> bloodReportData.put("stoolOva", value.replaceFirst("Ova", "").trim()));

        Optional<String> stoolCyst = getFieldValue(lines, "Cyst");
        stoolCyst.ifPresent(value -> bloodReportData.put("stoolCysts", value.replaceFirst("Cyst", "").trim()));

        // FIX 18: stoolBacteria — key changed
        Optional<String> stoolOther = getFieldValue(lines, "Other Finding");
        stoolOther.ifPresent(value -> bloodReportData.put("stoolBacteria",
                value.replaceFirst("Other Finding", "").replaceAll(",", "").trim()));
    }
}
