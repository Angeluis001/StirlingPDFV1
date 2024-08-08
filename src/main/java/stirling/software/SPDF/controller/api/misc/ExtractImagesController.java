package stirling.software.SPDF.controller.api.misc;

import io.github.pixee.security.Filenames;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import stirling.software.SPDF.model.api.PDFWithImageFormatRequest;
import stirling.software.SPDF.utils.WebResponseUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/v1/misc")
@Tag(name = "Misc", description = "Miscellaneous APIs")
public class ExtractImagesController {

    private static final Logger logger = LoggerFactory.getLogger(ExtractImagesController.class);

    @PostMapping(consumes = "multipart/form-data", value = "/extract-images")
    @Operation(
            summary = "Extract images from a PDF file",
            description =
                    "This endpoint extracts images from a given PDF file and returns them in a zip file. Users can specify the output image format. Input: PDF Output: IMAGE/ZIP Type: SIMO")
    public ResponseEntity<byte[]> extractImages(@ModelAttribute PDFWithImageFormatRequest request)
            throws IOException, InterruptedException, ExecutionException {
        MultipartFile file = request.getFileInput();
        String format = request.getFormat();

        System.out.println(
                System.currentTimeMillis() + " file=" + file.getName() + ", format=" + format);
        PDDocument document = Loader.loadPDF(file.getBytes());

        // Determine if multithreading should be used based on PDF size or number of pages
        boolean useMultithreading = shouldUseMultithreading(file, document);

        // Create ByteArrayOutputStream to write zip file to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Create ZipOutputStream to create zip file
        ZipOutputStream zos = new ZipOutputStream(baos);

        // Set compression level
        zos.setLevel(Deflater.BEST_COMPRESSION);

        String filename =
                Filenames.toSimpleFileName(file.getOriginalFilename())
                        .replaceFirst("[.][^.]+$", "");
        Set<Integer> processedImages = new HashSet<>();

        if (useMultithreading) {
            // Executor service to handle multithreading
            ExecutorService executor =
                    Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            Set<Future<Void>> futures = new HashSet<>();

            // Iterate over each page
            for (int pgNum = 0; pgNum < document.getPages().getCount(); pgNum++) {
                PDPage page = document.getPage(pgNum);
                int pageNum = document.getPages().indexOf(page) + 1;
                // Submit a task for processing each page
                Future<Void> future =
                        executor.submit(
                                () -> {
                                    extractImagesFromPage(
                                            page, format, filename, pageNum, processedImages, zos);
                                    return null;
                                });

                futures.add(future);
            }

            // Wait for all tasks to complete
            for (Future<Void> future : futures) {
                future.get();
            }

            // Close executor service
            executor.shutdown();
        } else {
            // Single-threaded extraction
            for (int pgNum = 0; pgNum < document.getPages().getCount(); pgNum++) {
                PDPage page = document.getPage(pgNum);
                extractImagesFromPage(page, format, filename, pgNum + 1, processedImages, zos);
            }
        }

        // Close PDDocument and ZipOutputStream
        document.close();
        zos.close();

        // Create ByteArrayResource from byte array
        byte[] zipContents = baos.toByteArray();

        return WebResponseUtils.boasToWebResponse(
                baos, filename + "_extracted-images.zip", MediaType.APPLICATION_OCTET_STREAM);
    }

    private boolean shouldUseMultithreading(MultipartFile file, PDDocument document) {
        // Criteria: Use multithreading if file size > 10MB or number of pages > 20
        long fileSizeInMB = file.getSize() / (1024 * 1024);
        int numberOfPages = document.getPages().getCount();
        return fileSizeInMB > 10 || numberOfPages > 20;
    }

    private void extractImagesFromPage(
            PDPage page,
            String format,
            String filename,
            int pageNum,
            Set<Integer> processedImages,
            ZipOutputStream zos)
            throws IOException {
        for (COSName name : page.getResources().getXObjectNames()) {
            if (page.getResources().isImageXObject(name)) {
                PDImageXObject image = (PDImageXObject) page.getResources().getXObject(name);
                int imageHash = image.hashCode();
                synchronized (processedImages) {
                    if (processedImages.contains(imageHash)) {
                        continue; // Skip already processed images
                    }
                    processedImages.add(imageHash);
                }

                RenderedImage renderedImage = image.getImage();

                // Convert to standard RGB colorspace if needed
                BufferedImage bufferedImage = convertToRGB(renderedImage, format);

                // Write image to zip file
                String imageName = filename + "_" + imageHash + " (Page " + pageNum + ")." + format;
                synchronized (zos) {
                    zos.putNextEntry(new ZipEntry(imageName));
                    ByteArrayOutputStream imageBaos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, format, imageBaos);
                    zos.write(imageBaos.toByteArray());
                    zos.closeEntry();
                }
            }
        }
    }

    private BufferedImage convertToRGB(RenderedImage renderedImage, String format) {
        int width = renderedImage.getWidth();
        int height = renderedImage.getHeight();
        BufferedImage rgbImage;

        if ("png".equalsIgnoreCase(format)) {
            rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        } else if ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format)) {
            rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        } else if ("gif".equalsIgnoreCase(format)) {
            rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED);
        } else {
            rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g = rgbImage.createGraphics();
        g.drawImage((Image) renderedImage, 0, 0, null);
        g.dispose();
        return rgbImage;
    }
}
