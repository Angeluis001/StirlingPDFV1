package stirling.software.SPDF.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import stirling.software.SPDF.utils.ProcessExecutor;


@Controller
public class CompressController {

    private static final Logger logger = LoggerFactory.getLogger(CompressController.class);

    @GetMapping("/compress-pdf")
    public String compressPdfForm(Model model) {
        model.addAttribute("currentPage", "compress-pdf");
        return "compress-pdf";
    }

    
    @PostMapping("/compress-pdf")
    public ResponseEntity<byte[]> optimizePdf(
            @RequestParam("fileInput") MultipartFile inputFile,
            @RequestParam("optimizeLevel") int optimizeLevel,
            @RequestParam(name = "fastWebView", required = false) Boolean fastWebView,
            @RequestParam(name = "jbig2Lossy", required = false) Boolean jbig2Lossy) throws IOException, InterruptedException {

        // Save the uploaded file to a temporary location
        Path tempInputFile = Files.createTempFile("input_", ".pdf");
        inputFile.transferTo(tempInputFile.toFile());

        // Prepare the output file path
        Path tempOutputFile = Files.createTempFile("output_", ".pdf");

        // Prepare the OCRmyPDF command
        List<String> command = new ArrayList<>();
        command.add("ocrmypdf");
        command.add("--skip-text");
        command.add("--tesseract-timeout=0");
        command.add("--optimize");
        command.add(String.valueOf(optimizeLevel));

        if (fastWebView != null && fastWebView) {
            long fileSize = inputFile.getSize();
            long fastWebViewSize = (long) (fileSize * 1.25); // 25% higher than file size
            command.add("--fast-web-view");
            command.add(String.valueOf(fastWebViewSize));
        }

        if (jbig2Lossy != null && jbig2Lossy) {
            command.add("--jbig2-lossy");
        }

        command.add(tempInputFile.toString());
        command.add(tempOutputFile.toString());

        int returnCode = ProcessExecutor.getInstance(ProcessExecutor.Processes.OCR_MY_PDF).runCommandWithOutputHandling(command);
        
        // Read the optimized PDF file
        byte[] pdfBytes = Files.readAllBytes(tempOutputFile);

        // Clean up the temporary files
        Files.delete(tempInputFile);
        Files.delete(tempOutputFile);

        // Return the optimized PDF as a response
        String outputFilename = inputFile.getOriginalFilename().replaceFirst("[.][^.]+$", "") + "_Optimized.pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", outputFilename);
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
}

}
