package basketball.projects.balonAlAire.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.dto.CategoryRequest;
import basketball.projects.balonAlAire.dto.CategoryResponse;
import basketball.projects.balonAlAire.model.Category;
import basketball.projects.balonAlAire.repository.CategoryRepository;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable("categories")
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    public CategoryResponse findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(CategoryResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse save(CategoryRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Category category = new Category();
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setCreatedAt(now);
        category.setUpdatedAt(now);
        return CategoryResponse.from(categoryRepository.save(category));
    }

    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
        existing.setName(request.name());
        existing.setSlug(request.slug());
        existing.setUpdatedAt(LocalDateTime.now());
        return CategoryResponse.from(categoryRepository.save(existing));
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id) {
        if (categoryRepository.deleteOneById(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada");
        }
    }
}
