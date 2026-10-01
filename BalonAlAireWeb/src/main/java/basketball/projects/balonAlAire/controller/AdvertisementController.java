package basketball.projects.balonAlAire.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import jakarta.validation.Valid;
import basketball.projects.balonAlAire.dto.AdvertisementRequest;
import basketball.projects.balonAlAire.model.Advertisement;
import basketball.projects.balonAlAire.service.AdvertisementService;

@RestController
@RequestMapping("/api/advertisements")
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    public AdvertisementController(
            AdvertisementService advertisementService) {

        this.advertisementService = advertisementService;
    }

    @GetMapping
    public Page<Advertisement> getAll(@PageableDefault(size=20) Pageable pageable) {

        return advertisementService
                .getAllAdvertisements(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Advertisement create(
            @Valid @RequestBody AdvertisementRequest request) {

        return advertisementService
                .saveAdvertisement(request);
    }

    @PutMapping("/{id}")
    public Advertisement update(
            @PathVariable Integer id,
            @Valid @RequestBody AdvertisementRequest request) {

        return advertisementService
                .updateAdvertisement(id, request)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Advertisement not found"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Integer id) {

        advertisementService
                .deleteAdvertisement(id);
    }
}
