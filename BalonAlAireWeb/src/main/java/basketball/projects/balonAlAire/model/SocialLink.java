package basketball.projects.balonAlAire.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "social_links")
@Getter
@Setter
public class SocialLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String platform;

    @Column(name = "link_url")
    private String linkUrl;

    private boolean active = true;

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof SocialLink that && id != null && id.equals(that.getId()));
    }

    @Override
    public int hashCode() {
        return SocialLink.class.hashCode();
    }
}
