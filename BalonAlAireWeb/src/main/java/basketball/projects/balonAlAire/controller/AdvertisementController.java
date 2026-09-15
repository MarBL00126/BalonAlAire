package basketball.projects.balonAlAire.controller;

import java.util.List;

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
    public List<Advertisement> getAll() {

        return advertisementService
                .getAllAdvertisements();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Advertisement create(
            @Valid @RequestBody Advertisement advertisement) {

        return advertisementService
                .saveAdvertisement(advertisement);
    }

    @PutMapping("/{id}")
    public Advertisement update(
            @PathVariable Integer id,
            @Valid @RequestBody Advertisement changes) {

        return advertisementService
                .updateAdvertisement(id, changes)
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
