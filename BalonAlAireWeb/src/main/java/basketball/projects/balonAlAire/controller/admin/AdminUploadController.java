package basketball.projects.balonAlAire.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.dto.PresignRequest;
import basketball.projects.balonAlAire.dto.PresignResponse;
import basketball.projects.balonAlAire.dto.UploadResponse;
import basketball.projects.balonAlAire.service.FileStorageService;
import basketball.projects.balonAlAire.service.PresignedUpload;
import basketball.projects.balonAlAire.service.UploadValidator;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/uploads")
public class AdminUploadController {

    private final FileStorageService fileStorageService;
    private final UploadValidator uploadValidator;

    public AdminUploadController(FileStorageService fileStorageService, UploadValidator uploadValidator) {
        this.fileStorageService = fileStorageService;
        this.uploadValidator = uploadValidator;
    }

    /** Subida a través del servidor: se usa con storage local o si falla la subida directa. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se recibió ningún archivo");
        }
        uploadValidator.validate(file.getOriginalFilename(), file.getContentType(), file.getSize());
        return new UploadResponse(fileStorageService.upload(file));
    }

    /** Datos para que el navegador suba directo al storage (PUT). 501 si el storage no lo soporta. */
    @PostMapping("/presign")
    public PresignResponse presign(@Valid @RequestBody PresignRequest request) {
        uploadValidator.validate(request.filename(), request.contentType(), request.size());

        PresignedUpload presigned = fileStorageService
                .presignUpload(request.filename(), request.contentType(), request.size())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_IMPLEMENTED, "El storage actual no admite subida directa"));
        return new PresignResponse(presigned.uploadUrl(), presigned.publicUrl(), presigned.headers());
    }
}
