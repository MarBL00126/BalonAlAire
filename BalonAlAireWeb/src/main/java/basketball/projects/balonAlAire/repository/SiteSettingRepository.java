package basketball.projects.balonAlAire.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import basketball.projects.balonAlAire.model.SiteSetting;

@Repository
public interface SiteSettingRepository extends JpaRepository<SiteSetting, Integer> {
    Optional<SiteSetting> findBySettingsKey(String settingsKey);
}
