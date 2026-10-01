package basketball.projects.balonAlAire.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.dto.AdvertisementRequest;
import basketball.projects.balonAlAire.model.Advertisement;
import basketball.projects.balonAlAire.repository.AdvertisementRepository;

@Service
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;

    public AdvertisementService(
            AdvertisementRepository advertisementRepository) {

        this.advertisementRepository = advertisementRepository;
    }

    public Page<Advertisement> getAllAdvertisements(Pageable pageable) {

        return advertisementRepository.findAll(pageable);
    }

    public Advertisement saveAdvertisement(
            AdvertisementRequest request) {

        LocalDateTime now = LocalDateTime.now();

    Advertisement advertisement = new Advertisement();

    advertisement.setName(request.getName());
    advertisement.setImageUrl(request.getImageUrl());
    advertisement.setLinkUrl(request.getLinkUrl());
    advertisement.setPosition(request.getPosition());
    advertisement.setActive(request.isActive());

    advertisement.setCreatedAt(now);
    advertisement.setUpdatedAt(now);

    return advertisementRepository.save(advertisement);
    }

    public Optional<Advertisement> updateAdvertisement(
            Integer id,
            AdvertisementRequest request) {

        return advertisementRepository.findById(id)
            .map(existing -> {

                existing.setName(request.getName());
                existing.setImageUrl(request.getImageUrl());
                existing.setLinkUrl(request.getLinkUrl());
                existing.setPosition(request.getPosition());
                existing.setActive(request.isActive());
                existing.setUpdatedAt(LocalDateTime.now());

                return advertisementRepository.save(existing);
            });
    }

    public void deleteAdvertisement(Integer id) {

        if (!advertisementRepository.existsById(id)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Advertisement not found");
        }

        advertisementRepository.deleteById(id);
    }
}
