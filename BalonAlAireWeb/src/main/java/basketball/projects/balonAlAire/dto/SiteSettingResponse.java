package basketball.projects.balonAlAire.dto;

import basketball.projects.balonAlAire.model.SiteSetting;

public record SiteSettingResponse(String settingsKey, String settingsValue) {

    public static SiteSettingResponse from(SiteSetting setting) {
        return new SiteSettingResponse(setting.getSettingsKey(), setting.getSettingsValue());
    }
}
