package stirling.software.SPDF.controller.security;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.util.Matrix;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import stirling.software.SPDF.utils.PdfUtils;
import stirling.software.SPDF.utils.WatermarkRemover;

@Controller
public class WatermarkController {

    @GetMapping("/add-watermark")
    public String addWatermarkForm(Model model) {
        model.addAttribute("currentPage", "add-watermark");
        return "security/add-watermark";
    }

    @GetMapping("/remove-watermark")
    public String removeWatermarkForm(Model model) {
        model.addAttribute("currentPage", "remove-watermark");
        return "security/remove-watermark";
    }
    
    @PostMapping("/add-watermark")
    public ResponseEntity<byte[]> addWatermark(@RequestParam("fileInput") MultipartFile pdfFile, @RequestParam("watermarkText") String watermarkText,
            @RequestParam(defaultValue = "30", name = "fontSize") float fontSize, @RequestParam(defaultValue = "0", name = "rotation") float rotation,
            @RequestParam(defaultValue = "50", name = "widthSpacer") int widthSpacer, @RequestParam(defaultValue = "50", name = "heightSpacer") int heightSpacer)
            throws IOException {

        // Load the input PDF
        PDDocument document = PDDocument.load(pdfFile.getInputStream());

        // Create a page in the document
        for (PDPage page : document.getPages()) {
            // Get the page's content stream
            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true);

            // Set font of watermark
            PDFont font = PDType1Font.HELVETICA_BOLD;
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.setNonStrokingColor(Color.LIGHT_GRAY);

            // Set size and location of watermark
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float watermarkWidth = widthSpacer + font.getStringWidth(watermarkText) * fontSize / 1000;
            float watermarkHeight = heightSpacer + fontSize;
            int watermarkRows = (int) (pageHeight / watermarkHeight + 1);
            int watermarkCols = (int) (pageWidth / watermarkWidth + 1);

            // Add the watermark text
            for (int i = 0; i < watermarkRows; i++) {
                for (int j = 0; j < watermarkCols; j++) {
                    contentStream.setTextMatrix(Matrix.getRotateInstance((float) Math.toRadians(rotation), j * watermarkWidth, i * watermarkHeight));
                    contentStream.showTextWithPositioning(new Object[] { watermarkText });
                }
            }

            contentStream.endText();

            // Close the content stream
            contentStream.close();
        }
        return PdfUtils.pdfDocToWebResponse(document, pdfFile.getOriginalFilename().replaceFirst("[.][^.]+$", "")  + "_watermarked.pdf");
    }
    
    
    
    
    @PostMapping("/remove-watermark")
    public ResponseEntity<byte[]> removeWatermark(@RequestParam("fileInput") MultipartFile pdfFile, @RequestParam("watermarkText") String watermarkText) throws Exception {
        
        // Load the input PDF
        PDDocument document = PDDocument.load(pdfFile.getInputStream());

     // Create a new PDF document for the output
        PDDocument outputDocument = new PDDocument();

        // Loop through the pages
        int numPages = document.getNumberOfPages();
        for (int i = 0; i < numPages; i++) {
            PDPage page = document.getPage(i);

            // Process the content stream to remove the watermark text
            WatermarkRemover editor = new WatermarkRemover(watermarkText) {};
            editor.processPage(page);
            editor.processPage(page);
            // Add the page to the output document
            outputDocument.addPage(page);
        }

        for (PDPage page : outputDocument.getPages()) {
            List<PDAnnotation> annotations = page.getAnnotations();
            List<PDAnnotation> annotationsToRemove = new ArrayList<>();

            for (PDAnnotation annotation : annotations) {
                if (annotation instanceof PDAnnotationMarkup) {
                    PDAnnotationMarkup markup = (PDAnnotationMarkup) annotation;
                    String contents = markup.getContents();
                    if (contents != null && contents.contains(watermarkText)) {
                        annotationsToRemove.add(markup);
                    }
                }
            }

            annotations.removeAll(annotationsToRemove);
        }
        PDDocumentCatalog catalog = outputDocument.getDocumentCatalog();
        PDAcroForm acroForm = catalog.getAcroForm();
        if (acroForm != null) {
            List<PDField> fields = acroForm.getFields();
            for (PDField field : fields) {
                String fieldValue = field.getValueAsString();
                if (fieldValue.contains(watermarkText)) {
                    field.setValue(fieldValue.replace(watermarkText, ""));
                }
            }
        }
        
        return PdfUtils.pdfDocToWebResponse(outputDocument, "removed.pdf");
    }

    
}
