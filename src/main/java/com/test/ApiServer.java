package com.test;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class ApiServer {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/test", exchange -> {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                    return;
                }

                String query = exchange.getRequestURI().getRawQuery();
                Map<String, String> params = parseQuery(query);
                String pdfUrl = params.get("url");

                if (pdfUrl == null || pdfUrl.isEmpty()) {
                    sendJson(exchange, 400, "{\"error\":\"Missing 'url' query parameter\"}");
                    return;
                }

                pdfUrl = URLDecoder.decode(pdfUrl, StandardCharsets.UTF_8);

                System.out.println("Processing PDF: " + pdfUrl);
                String extractedText = Utils.extractTextFromPdfUrl(pdfUrl);

                if (extractedText.isEmpty()) {
                    sendJson(exchange, 500, "{\"error\":\"Failed to extract text from PDF\"}");
                    return;
                }

                ParserService parserService = new ParserService();
                Map<String, Object> result = parserService.predLabsReportExtraction(extractedText);

                sendJson(exchange, 200, toJson(buildResponse(result)));
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sendJson(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                } catch (Exception ignored) {}
            }
        });

        // POST /upload — upload PDF file directly
        server.createContext("/upload", exchange -> {
            try {
                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"error\":\"Use POST with PDF file as binary body\"}");
                    return;
                }

                byte[] pdfBytes = exchange.getRequestBody().readAllBytes();
                if (pdfBytes.length == 0) {
                    sendJson(exchange, 400, "{\"error\":\"Empty file\"}");
                    return;
                }

                File tempFile = Files.createTempFile("upload_", ".pdf").toFile();
                try {
                    Files.write(tempFile.toPath(), pdfBytes);

                    String extractedText;
                    try (PDDocument document = PDDocument.load(tempFile)) {
                        PDFTextStripper stripper = new PDFTextStripper();
                        stripper.setSortByPosition(true);
                        extractedText = stripper.getText(document);
                    }

                    if (extractedText.isEmpty()) {
                        sendJson(exchange, 500, "{\"error\":\"Failed to extract text from PDF\"}");
                        return;
                    }

                    ParserService parserService = new ParserService();
                    Map<String, Object> result = parserService.predLabsReportExtraction(extractedText);

                    Map<String, Object> response = buildResponse(result);

                    sendJson(exchange, 200, toJson(response));
                } finally {
                    tempFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sendJson(exchange, 500, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
                } catch (Exception ignored) {}
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("========================================");
        System.out.println("Server started on http://localhost:" + port);
        System.out.println("========================================");
        System.out.println("Postman Testing:");
        System.out.println();
        System.out.println("1) Upload PDF:");
        System.out.println("   POST http://localhost:" + port + "/upload");
        System.out.println("   Body -> binary -> select PDF file");
        System.out.println();
        System.out.println("2) Test with URL:");
        System.out.println("   GET http://localhost:" + port + "/test?url=<PDF_URL>");
        System.out.println();
        System.out.println("Example URL:");
        System.out.println("   GET http://localhost:" + port + "/test?url=" + URLDecoder.decode(
            "https://drive.google.com/uc?export=download&id=1bcADf4IuzGhjO7wLcD7jr70n4eZswo90",
            StandardCharsets.UTF_8));
    }

    private static Map<String, Object> buildResponse(Map<String, Object> result) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("patientName", result.getOrDefault("BLOOD_PATIENT_NAME_REPORT", ""));
        response.put("allFields", result);

        Map<String, Object> verification = new LinkedHashMap<>();
        List<String> expectedFields = Arrays.asList(
            "screatinine", "bloodUrea", "bun", "uricAcid", "gfr",
            "bloodUreaNitrogenToCreatinineRatio", "ureaToCreatinineRatio",
            "vdrL", "ldlToHdlRatio", "urineBileSalts",
            "stoolColor", "stoolConsistency", "stoolMucus", "stoolOccultBlood",
            "stoolPusCells", "stoolRedBloodCells", "stoolEpithelialCells", "stoolBacteria"
        );

        int pass = 0, fail = 0;
        for (String field : expectedFields) {
            Object val = result.get(field);
            boolean ok = val != null && !val.toString().trim().isEmpty() && !val.toString().equalsIgnoreCase("null");
            if (ok) pass++; else fail++;
            verification.put(field, ok ? (field + " = " + val) : "NULL");
        }

        response.put("verification", verification);
        response.put("pass", pass);
        response.put("fail", fail);
        response.put("total", pass + fail);
        return response;
    }

    private static void sendJson(HttpExchange exchange, int code, String json) throws Exception {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null) return params;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) params.put(pair[0], pair[1]);
        }
        return params;
    }

    private static String toJson(Object obj) {
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(escapeJson(String.valueOf(e.getKey()))).append("\":");
                sb.append(toJson(e.getValue()));
            }
            sb.append("}");
            return sb.toString();
        } else if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return String.valueOf(obj);
        } else {
            return obj == null ? "null" : "\"" + escapeJson(obj.toString()) + "\"";
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
