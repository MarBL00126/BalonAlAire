package basketball.projects.balonAlAire.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "advertisements")
@Data
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @NotBlank
    @Column(nullable = false)
    private String name;
    @NotBlank
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    @NotBlank
    @Column(name = "link_url", nullable = false)
    private String linkUrl;
    @Column(name = "position")
    private String position;
    @Column(nullable = false)
    private boolean active = true;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
