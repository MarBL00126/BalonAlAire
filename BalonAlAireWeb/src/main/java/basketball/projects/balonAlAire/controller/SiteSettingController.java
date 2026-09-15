package basketball.projects.balonAlAire.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.model.SiteSetting;
import basketball.projects.balonAlAire.service.SiteSettingService;

@RestController
@RequestMapping("/api/settings")
public class SiteSettingController {
    private final SiteSettingService siteSettingService;

    public SiteSettingController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping
    public List<SiteSetting> getAll() {
        return siteSettingService.getAllSettings();
    }

    @PutMapping
    public List<SiteSetting> update(@RequestBody Map<String, String> updates) {
        return siteSettingService.updateSettings(updates);
    }
}
