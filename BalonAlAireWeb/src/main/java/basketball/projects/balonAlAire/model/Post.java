package basketball.projects.balonAlAire.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "posts")
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToMany
    @JoinTable(name = "post_categories", joinColumns = @JoinColumn(name = "post_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private List<Category> categories;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String slug;
    @Column(nullable = false)
    private String excerpt;
    @Column(nullable = false)
    private String content;
    @Column(name = "img_url", nullable = false)
    private String imgUrl;
    @Column(nullable = false)
    private String author;
    @Column(nullable = false)
    private String status;
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
