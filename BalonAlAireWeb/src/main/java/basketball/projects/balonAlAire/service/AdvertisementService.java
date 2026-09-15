package basketball.projects.balonAlAire.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.model.Advertisement;
import basketball.projects.balonAlAire.repository.AdvertisementRepository;

@Service
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;

    public AdvertisementService(
            AdvertisementRepository advertisementRepository) {

        this.advertisementRepository = advertisementRepository;
    }

    public List<Advertisement> getAllAdvertisements() {

        return advertisementRepository.findAll();
    }

    public Advertisement saveAdvertisement(
            Advertisement advertisement) {

        LocalDateTime now = LocalDateTime.now();

        if (advertisement.getCreatedAt() == null) {
            advertisement.setCreatedAt(now);
        }

        advertisement.setUpdatedAt(now);

        return advertisementRepository.save(advertisement);
    }

    public Optional<Advertisement> updateAdvertisement(
            Integer id,
            Advertisement changes) {

        return advertisementRepository.findById(id)
                .map(existing -> {

                    existing.setName(
                            changes.getName());

                    existing.setImageUrl(
                            changes.getImageUrl());

                    existing.setLinkUrl(
                            changes.getLinkUrl());

                    existing.setPosition(
                            changes.getPosition());

                    existing.setActive(
                            changes.isActive());

                    existing.setUpdatedAt(
                            LocalDateTime.now());

                    return advertisementRepository.save(
                            existing);
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
