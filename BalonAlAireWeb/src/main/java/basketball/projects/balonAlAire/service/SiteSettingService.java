package basketball.projects.balonAlAire.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import basketball.projects.balonAlAire.dto.SiteSettingResponse;
import basketball.projects.balonAlAire.model.SiteSetting;
import basketball.projects.balonAlAire.repository.SiteSettingRepository;

@Service
public class SiteSettingService {
    private final SiteSettingRepository siteSettingRepository;

    public SiteSettingService(SiteSettingRepository siteSettingRepository) {
        this.siteSettingRepository = siteSettingRepository;
    }

    @Cacheable("settings")
    public List<SiteSettingResponse> getAllSettings() {
        return siteSettingRepository.findAll().stream()
                .map(SiteSettingResponse::from)
                .toList();
    }

    // upsert: actualiza las claves existentes y crea las que todavía no están guardadas
    @CacheEvict(value = "settings", allEntries = true)
    public List<SiteSettingResponse> updateSettings(Map<String, String> updates) {
        return updates.entrySet().stream()
                .map(entry -> {
                    LocalDateTime now = LocalDateTime.now();
                    SiteSetting setting = siteSettingRepository.findBySettingsKey(entry.getKey())
                            .orElseGet(() -> {
                                SiteSetting created = new SiteSetting();
                                created.setCreatedAt(now);
                                return created;
                            });
                    setting.setSettingsKey(entry.getKey());
                    setting.setSettingsValue(entry.getValue());
                    setting.setUpdatedAt(now);
                    return SiteSettingResponse.from(siteSettingRepository.save(setting));
                })
                .toList();
    }
}
