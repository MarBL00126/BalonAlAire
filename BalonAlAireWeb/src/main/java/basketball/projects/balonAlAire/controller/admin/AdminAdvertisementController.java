package basketball.projects.balonAlAire.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.dto.AdvertisementAdminResponse;
import basketball.projects.balonAlAire.dto.AdvertisementRequest;
import basketball.projects.balonAlAire.service.AdvertisementService;
import jakarta.validation.Valid;

// Incluye anuncios inactivos. Solo ADMIN (SecurityConfig).
@RestController
@RequestMapping("/api/admin/advertisements")
public class AdminAdvertisementController {
    private final AdvertisementService advertisementService;

    public AdminAdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @GetMapping
    public Page<AdvertisementAdminResponse> getAll(
            @PageableDefault(size = 100, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return advertisementService.getAllAdvertisements(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdvertisementAdminResponse create(@Valid @RequestBody AdvertisementRequest request) {
        return advertisementService.saveAdvertisement(request);
    }

    @PutMapping("/{id}")
    public AdvertisementAdminResponse update(
            @PathVariable Long id,
            @Valid @RequestBody AdvertisementRequest request) {

        return advertisementService
                .updateAdvertisement(id, request)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Anuncio no encontrado"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        advertisementService.deleteAdvertisement(id);
    }
}
