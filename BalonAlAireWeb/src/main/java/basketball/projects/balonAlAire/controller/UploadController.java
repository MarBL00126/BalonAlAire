package basketball.projects.balonAlAire.controller;


import java.util.Map;
import java.util.Set;


import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.service.FileStorageService;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final long MAX_PDF_SIZE = 100 * 1024 * 1024; // 100 MB
    private final FileStorageService fileStorageService;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp",
            MediaType.APPLICATION_PDF_VALUE);
    public  UploadController(FileStorageService fileStorageService){
        this.fileStorageService=fileStorageService;
    }
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> upload(
            @RequestParam("file") MultipartFile file) {
                validateFile(file);
                String url = fileStorageService.upload(file);
                return Map.of("url", url);
    }
    private void validateFile(MultipartFile file){
        if(file==null || file.isEmpty()){
                throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "No se recibió ningún archivo"
            );
        }
        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten imágenes JPG, PNG, WebP o archivos PDF"
            );
        }

        long maxFileSize = MediaType.APPLICATION_PDF_VALUE.equals(contentType)
                ? MAX_PDF_SIZE
                : MAX_IMAGE_SIZE;

        if (file.getSize() > maxFileSize) {
                throw new ResponseStatusException(
                    HttpStatus.CONTENT_TOO_LARGE,
                    MediaType.APPLICATION_PDF_VALUE.equals(contentType)
                            ? "El PDF no puede superar los 100 MB"
                            : "La imagen no puede superar los 5 MB"
            );
        }
    }
}
