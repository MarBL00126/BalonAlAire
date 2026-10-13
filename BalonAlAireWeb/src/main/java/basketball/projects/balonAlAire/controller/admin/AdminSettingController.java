package basketball.projects.balonAlAire.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.dto.SiteSettingResponse;
import basketball.projects.balonAlAire.service.SiteSettingService;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingController {
    private final SiteSettingService siteSettingService;

    public AdminSettingController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping
    public List<SiteSettingResponse> getAll() {
        return siteSettingService.getAllSettings();
    }

    @PutMapping
    public List<SiteSettingResponse> update(@RequestBody Map<String, String> updates) {
        return siteSettingService.updateSettings(updates);
    }
}
