package com.test;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collection;
import java.util.stream.Stream;

public class Utils {

    public static <T> Stream<T> getSafeStream(Collection<T> collection) {
        if (collection == null) return Stream.empty();
        return collection.stream();
    }

    public static String extractTextFromPdfUrl(String pdfUrl) {
        File tempFile = null;
        try {
            URL url = new URL(pdfUrl);
            tempFile = Files.createTempFile("predlabs_", ".pdf").toFile();
            org.apache.commons.io.FileUtils.copyURLToFile(url, tempFile);

            try (PDDocument document = PDDocument.load(tempFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                return stripper.getText(document);
            }
        } catch (Exception e) {
            System.err.println("Error extracting text from PDF URL: " + pdfUrl);
            e.printStackTrace();
            return "";
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
}
