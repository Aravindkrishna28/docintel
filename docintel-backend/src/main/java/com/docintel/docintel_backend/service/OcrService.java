package com.docintel.docintel_backend.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class OcrService {

    private final Tesseract tesseract;

    public OcrService(@Value("${tesseract.datapath}") String tessDataPath) {
        this.tesseract = new Tesseract();
        this.tesseract.setDatapath(tessDataPath);
        this.tesseract.setLanguage("eng");
    }

    public String extractText(File file) throws IOException, TesseractException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".pdf")) {
            return extractFromPdf(file);
        }
        return extractFromImage(file);
    }

    private String extractFromPdf(File file) throws IOException, TesseractException {
        StringBuilder text = new StringBuilder();
        try (PDDocument document = PDDocument.load(file)) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = renderer.renderImageWithDPI(page, 150);
                text.append(tesseract.doOCR(image)).append("\n");
            }
        }
        return text.toString();
    }

    private String extractFromImage(File file) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Could not read image file: " + file.getName());
        }
        return tesseract.doOCR(image);
    }
}