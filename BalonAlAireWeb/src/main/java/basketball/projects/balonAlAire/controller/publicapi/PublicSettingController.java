package basketball.projects.balonAlAire.controller.publicapi;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.dto.SiteSettingResponse;
import basketball.projects.balonAlAire.service.SiteSettingService;

@RestController
@RequestMapping("/api/public/settings")
public class PublicSettingController {
    private final SiteSettingService siteSettingService;

    public PublicSettingController(SiteSettingService siteSettingService) {
        this.siteSettingService = siteSettingService;
    }

    @GetMapping
    public List<SiteSettingResponse> getAll() {
        return siteSettingService.getAllSettings();
    }
}
