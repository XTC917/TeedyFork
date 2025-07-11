package com.sismics.docs.core.util;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.google.common.io.Resources;
import com.lowagie.text.FontFactory;
import com.sismics.docs.core.constant.Constants;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.util.format.FormatHandler;
import com.sismics.docs.core.util.format.FormatHandlerUtil;
import com.sismics.docs.core.util.pdf.PdfPage;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.DocsPDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * PDF utilities.
 * 
 * @author bgamard
 */
public class PdfUtil {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(PdfUtil.class);

    /**
     * Convert a document and its files to a merged PDF file.
     * 
     * @param documentDto Document DTO
     * @param fileList List of files
     * @param fitImageToPage Fit images to the page
     * @param metadata Add a page with metadata
     * @param margin Margins in millimeters
     * @param outputStream Output stream to write to, will be closed
     * @param translate Whether to translate the content
     * @param targetLanguage Target language for translation (e.g., "zh", "en")
     */
    public static void convertToPdf(DocumentDto documentDto, List<File> fileList,
            boolean fitImageToPage, boolean metadata, int margin, OutputStream outputStream,
            boolean translate, String targetLanguage) throws Exception {
        // Setup PDFBox
        Closer closer = Closer.create();
        MemoryUsageSetting memUsageSettings = MemoryUsageSetting.setupMixed(1000000); // 1MB max memory usage
        memUsageSettings.setTempDir(new java.io.File(System.getProperty("java.io.tmpdir"))); // To OS temp

        // Create a blank PDF
        try (PDDocument doc = new PDDocument(memUsageSettings)) {
            // Add metadata
            if (metadata) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PdfPage pdfPage = new PdfPage(doc, page, margin * Constants.MM_PER_INCH, DocsPDType1Font.HELVETICA, 12)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String title = documentDto.getTitle();
                    if (translate) {
                        title = TranslationService.getInstance().translateText(title, documentDto.getLanguage(), targetLanguage);
                    }
                    pdfPage.addText(title, true, DocsPDType1Font.HELVETICA_BOLD, 16)
                        .newLine();
                    
                    String creator = documentDto.getCreator();
                    if (translate) {
                        creator = TranslationService.getInstance().translateText(creator, documentDto.getLanguage(), targetLanguage);
                    }
                    pdfPage.addText("Created by " + creator
                        + " on " + dateFormat.format(new Date(documentDto.getCreateTimestamp())), true)
                        .newLine();
                    
                    String description = documentDto.getDescription();
                    if (translate) {
                        description = TranslationService.getInstance().translateText(description, documentDto.getLanguage(), targetLanguage);
                    }
                    pdfPage.addText(description)
                        .newLine();
                    
                    if (!Strings.isNullOrEmpty(documentDto.getSubject())) {
                        String subject = documentDto.getSubject();
                        if (translate) {
                            subject = TranslationService.getInstance().translateText(subject, documentDto.getLanguage(), targetLanguage);
                        }
                        pdfPage.addText("Subject: " + subject);
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getIdentifier())) {
                        pdfPage.addText("Identifier: " + documentDto.getIdentifier());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getPublisher())) {
                        pdfPage.addText("Publisher: " + documentDto.getPublisher());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getFormat())) {
                        pdfPage.addText("Format: " + documentDto.getFormat());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getSource())) {
                        pdfPage.addText("Source: " + documentDto.getSource());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getType())) {
                        pdfPage.addText("Type: " + documentDto.getType());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getCoverage())) {
                        pdfPage.addText("Coverage: " + documentDto.getCoverage());
                    }
                    if (!Strings.isNullOrEmpty(documentDto.getRights())) {
                        pdfPage.addText("Rights: " + documentDto.getRights());
                    }
                    pdfPage.addText("Language: " + (translate ? targetLanguage : documentDto.getLanguage()))
                        .newLine()
                        .addText("Files in this document : " + fileList.size(), false, DocsPDType1Font.HELVETICA_BOLD, 12);
                }
            }
            
            // Add files
            for (File file : fileList) {
                Path storedFile = DirectoryUtil.getStorageDirectory().resolve(file.getId());

                // Decrypt the file to a temporary file
                Path unencryptedFile = EncryptionUtil.decryptFile(storedFile, file.getPrivateKey());
                FormatHandler formatHandler = FormatHandlerUtil.find(file.getMimeType());
                if (formatHandler != null) {
                    // Extract text content
                    String content = null;
                    try {
                        content = formatHandler.extractContent(documentDto.getLanguage(), unencryptedFile);
                    } catch (Exception e) {
                        // Ignore files that can't extract content
                    }
                    if (content != null && !content.trim().isEmpty() && translate) {
                        // Translate content if translation is enabled
                        content = TranslationService.getInstance().translateText(content, documentDto.getLanguage(), targetLanguage);
                        // Add translated content to PDF
                        PDPage page = new PDPage();
                        doc.addPage(page);
                        try (PdfPage pdfPage = new PdfPage(doc, page, margin * Constants.MM_PER_INCH, DocsPDType1Font.HELVETICA, 12)) {
                            pdfPage.addText(content);
                        }
                    }
                    // Continue with original logic
                    formatHandler.appendToPdf(unencryptedFile, doc, fitImageToPage, margin, memUsageSettings, closer);
                }
            }
            
            doc.save(outputStream); // Write to the output stream
            closer.close(); // Close all remaining opened PDF
        }
    }

    /**
     * Register fonts.
     */
    public static void registerFonts() {
        URL url = Resources.getResource("fonts/LiberationMono-Regular.ttf");
        try (InputStream is = url.openStream()) {
            Path file = Files.createTempFile("sismics_docs_font_mono", ".ttf");
            try (OutputStream os = Files.newOutputStream(file)) {
                ByteStreams.copy(is, os);
            }
            FontFactory.register(file.toAbsolutePath().toString(), "LiberationMono-Regular");
            FontFactory.registerDirectories();
        } catch (IOException e) {
            log.error("Error loading font", e);
        }
    }
}
