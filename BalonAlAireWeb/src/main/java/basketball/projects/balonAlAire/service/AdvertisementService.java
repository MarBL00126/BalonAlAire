package basketball.projects.balonAlAire.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.dto.AdvertisementAdminResponse;
import basketball.projects.balonAlAire.dto.AdvertisementRequest;
import basketball.projects.balonAlAire.dto.AdvertisementResponse;
import basketball.projects.balonAlAire.model.Advertisement;
import basketball.projects.balonAlAire.repository.AdvertisementRepository;

@Service
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;

    public AdvertisementService(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    public List<AdvertisementResponse> getActiveAdvertisements() {
        return advertisementRepository.findAllByActiveTrue().stream()
                .map(AdvertisementResponse::from)
                .toList();
    }

    public Page<AdvertisementAdminResponse> getAllAdvertisements(Pageable pageable) {
        return advertisementRepository.findAll(pageable).map(AdvertisementAdminResponse::from);
    }

    public AdvertisementAdminResponse saveAdvertisement(AdvertisementRequest request) {
        LocalDateTime now = LocalDateTime.now();

        Advertisement advertisement = new Advertisement();
        advertisement.setCreatedAt(now);
        apply(advertisement, request, now);

        return AdvertisementAdminResponse.from(advertisementRepository.save(advertisement));
    }

    public Optional<AdvertisementAdminResponse> updateAdvertisement(Long id, AdvertisementRequest request) {
        return advertisementRepository.findById(id)
                .map(existing -> {
                    apply(existing, request, LocalDateTime.now());
                    return AdvertisementAdminResponse.from(advertisementRepository.save(existing));
                });
    }

    @Transactional
    public void deleteAdvertisement(Long id) {
        if (advertisementRepository.deleteOneById(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anuncio no encontrado");
        }
    }

    private void apply(Advertisement advertisement, AdvertisementRequest request, LocalDateTime now) {
        advertisement.setName(request.name());
        advertisement.setImageUrl(request.imageUrl());
        advertisement.setLinkUrl(request.linkUrl());
        advertisement.setPosition(request.position());
        advertisement.setActive(request.isActiveOrDefault());
        advertisement.setUpdatedAt(now);
    }
}
