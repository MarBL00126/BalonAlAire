package basketball.projects.balonAlAire.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.dto.PostResponse;
import basketball.projects.balonAlAire.model.Category;
import basketball.projects.balonAlAire.repository.CategoryRepository;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final PostService postService;

    public CategoryService(CategoryRepository categoryRepository, PostService postService) {
        this.categoryRepository = categoryRepository;
        this.postService = postService;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public Category update(Integer id, Category category) {
        Category existing = findById(id);
        existing.setName(category.getName());
        existing.setSlug(category.getSlug());
        existing.setUpdatedAt(LocalDateTime.now());
        return categoryRepository.save(existing);
    }

    public void delete(Integer id) {

        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Category not found");
        }

        categoryRepository.deleteById(id);
    }

    public List<PostResponse> findPostsByCategorySlug(String slug) {
        return postService.getPostsByCategorySlug(slug);
    }
}