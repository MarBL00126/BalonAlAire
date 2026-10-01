package basketball.projects.balonAlAire.service;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import basketball.projects.balonAlAire.model.SiteSetting;
import basketball.projects.balonAlAire.repository.SiteSettingRepository;

@Service
public class SiteSettingService {
    private final SiteSettingRepository siteSettingRepository;

    public SiteSettingService(SiteSettingRepository siteSettingRepository) {
        this.siteSettingRepository = siteSettingRepository;
    }

    @Cacheable("settings")
    public List<SiteSetting> getAllSettings() {
        return siteSettingRepository.findAll();
    }

    // upsert: updates existing keys, creates new ones for keys not yet stored
    @CacheEvict(value = "settings", allEntries = true)
    public List<SiteSetting> updateSettings(Map<String, String> updates) {
        return updates.entrySet().stream()
                .map(entry -> {
                    SiteSetting setting = siteSettingRepository.findBySettingsKey(entry.getKey())
                            .orElseGet(SiteSetting::new);
                    setting.setSettingsKey(entry.getKey());
                    setting.setSettingsValue(entry.getValue());
                    return siteSettingRepository.save(setting);
                })
                .toList();
    }
}
