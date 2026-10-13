package basketball.projects.balonAlAire.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import basketball.projects.balonAlAire.config.AppProperties;
import basketball.projects.balonAlAire.dto.EditorConfigResponse;

@RestController
@RequestMapping("/api/admin/config")
public class AdminConfigController {

    private final String tinymceApiKey;

    public AdminConfigController(AppProperties properties) {
        this.tinymceApiKey = properties.tinymce().apiKey();
    }

    @GetMapping("/editor")
    public EditorConfigResponse editorConfig() {
        return new EditorConfigResponse(tinymceApiKey);
    }
}
