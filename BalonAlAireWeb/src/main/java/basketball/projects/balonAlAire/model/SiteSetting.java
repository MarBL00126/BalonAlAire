package basketball.projects.balonAlAire.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "site_settings")
@Getter
@Setter
public class SiteSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settings_key", nullable = false)
    private String settingsKey;

    @Column(name = "settings_value")
    private String settingsValue;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof SiteSetting that && id != null && id.equals(that.getId()));
    }

    @Override
    public int hashCode() {
        return SiteSetting.class.hashCode();
    }
}
