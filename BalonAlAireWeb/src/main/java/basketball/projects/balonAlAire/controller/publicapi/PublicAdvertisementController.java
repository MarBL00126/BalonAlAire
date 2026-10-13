package basketball.projects.balonAlAire.controller.publicapi;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.dto.AdvertisementResponse;
import basketball.projects.balonAlAire.service.AdvertisementService;

// Solo anuncios activos y solo los campos necesarios para mostrarlos
@RestController
@RequestMapping("/api/public/advertisements")
public class PublicAdvertisementController {
    private final AdvertisementService advertisementService;

    public PublicAdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @GetMapping
    public List<AdvertisementResponse> getActive() {
        return advertisementService.getActiveAdvertisements();
    }
}
