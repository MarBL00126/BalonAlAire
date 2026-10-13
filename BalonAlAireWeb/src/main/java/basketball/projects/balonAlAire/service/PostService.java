package basketball.projects.balonAlAire.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import basketball.projects.balonAlAire.dto.AdminPostSummaryResponse;
import basketball.projects.balonAlAire.dto.CategoryResponse;
import basketball.projects.balonAlAire.dto.PostRequest;
import basketball.projects.balonAlAire.dto.PostResponse;
import basketball.projects.balonAlAire.dto.PostSummaryResponse;
import basketball.projects.balonAlAire.model.Category;
import basketball.projects.balonAlAire.model.Post;
import basketball.projects.balonAlAire.model.PostStatus;
import basketball.projects.balonAlAire.repository.CategoryRepository;
import basketball.projects.balonAlAire.repository.PostCategoryView;
import basketball.projects.balonAlAire.repository.PostRepository;
import basketball.projects.balonAlAire.repository.PostSummaryView;

// Los listados devuelven resúmenes (sin el contenido); el artículo completo solo sale por slug o, en el panel, por id.
// Las lecturas son @Transactional porque las categorías son LAZY (open-in-view está desactivado).
@Service
public class PostService {
    private static final int MAX_PUBLIC_PAGE_SIZE = 50;
    private static final int MAX_ADMIN_PAGE_SIZE = 100;
    private static final int MAX_RELATED = 12;

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public PostService(PostRepository postRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
    }

    // --- Lectura pública: solo posts PUBLISHED cuya fecha de publicación ya llegó ---

    @Transactional(readOnly = true)
    public PostResponse getBySlug(String slug) {
        return findPublishedBySlug(slug).map(PostResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Noticia no encontrada"));
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getAllPosts(Pageable pageable) {
        Page<PostSummaryView> page = postRepository.findPublishedSummaries(LocalDateTime.now(), limited(pageable, MAX_PUBLIC_PAGE_SIZE));
        return mapPage(page, PostSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getPostsByCategorySlug(String slug, Pageable pageable) {
        Page<PostSummaryView> page = postRepository.findPublishedSummariesByCategory(
                slug, LocalDateTime.now(), limited(pageable, MAX_PUBLIC_PAGE_SIZE));
        return mapPage(page, PostSummaryResponse::from);
    }

    /** Noticias que comparten categoría con la indicada; si no hay, las últimas publicadas. */
    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getRelated(String slug, int limit) {
        Post post = findPublishedBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Noticia no encontrada"));

        LocalDateTime now = LocalDateTime.now();
        Pageable top = PageRequest.of(0, Math.max(1, Math.min(limit, MAX_RELATED)));
        List<Long> categoryIds = post.getCategories().stream().map(Category::getId).toList();

        List<PostSummaryView> related = categoryIds.isEmpty()
                ? List.of()
                : postRepository.findRelatedSummaries(post.getId(), categoryIds, now, top);
        if (related.isEmpty()) {
            related = postRepository.findLatestSummariesExcluding(post.getId(), now, top);
        }
        return withCategories(related, PostSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> searchPosts(String search, Pageable pageable) {
        // Primero los ids ordenados por relevancia, después los datos livianos de esas noticias
        Page<Long> ids = postRepository.searchPublishedIds(search, LocalDateTime.now(), limited(pageable, MAX_PUBLIC_PAGE_SIZE));

        Map<Long, PostSummaryView> byId = ids.isEmpty()
                ? Map.of()
                : postRepository.findSummariesByIdIn(ids.getContent()).stream()
                        .collect(Collectors.toMap(PostSummaryView::getId, view -> view));
        List<PostSummaryView> ordered = ids.getContent().stream().map(byId::get).filter(Objects::nonNull).toList();

        return new PageImpl<>(withCategories(ordered, PostSummaryResponse::from), ids.getPageable(), ids.getTotalElements());
    }

    // --- Lectura administrativa: incluye borradores y programados ---

    @Transactional(readOnly = true)
    public Page<AdminPostSummaryResponse> getAllPostsForAdmin(Pageable pageable) {
        return mapPage(postRepository.findAllSummaries(limited(pageable, MAX_ADMIN_PAGE_SIZE)), AdminPostSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public PostResponse getForAdmin(Long id) {
        return postRepository.findById(id)
                .map(PostResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Noticia no encontrada"));
    }

    // --- Escritura ---

    @Transactional
    public PostResponse save(PostRequest request) {
        Post post = new Post();
        LocalDateTime now = LocalDateTime.now();
        post.setCreatedAt(now);
        applyRequest(post, request, now);
        return PostResponse.from(postRepository.save(post));
    }

    @Transactional
    public PostResponse update(Long id, PostRequest request) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Noticia no encontrada"));

        applyRequest(existing, request, LocalDateTime.now());
        return PostResponse.from(postRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (postRepository.deleteOneById(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Noticia no encontrada");
        }
    }

    private Optional<Post> findPublishedBySlug(String slug) {
        return postRepository.findBySlugAndStatusAndPublishedAtLessThanEqual(slug, PostStatus.PUBLISHED, LocalDateTime.now());
    }

    private void applyRequest(Post post, PostRequest request, LocalDateTime now) {
        Set<Long> requestedIds = new HashSet<>(request.categoryIds());
        List<Category> categories = categoryRepository.findAllById(requestedIds);
        if (categories.size() != requestedIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Alguna de las categorías no existe");
        }

        PostStatus status = request.status() != null ? request.status() : PostStatus.DRAFT;
        LocalDateTime publishedAt = request.publishedAt();
        if (status == PostStatus.PUBLISHED && publishedAt == null) {
            publishedAt = post.getPublishedAt() != null ? post.getPublishedAt() : now;
        }

        post.setTitle(request.title());
        post.setSlug(request.slug());
        post.setExcerpt(request.excerpt());
        post.setContent(sanitizeHtml(request.content()));
        post.setImgUrl(request.imgUrl());
        post.setAuthor(request.author());
        post.setStatus(status);
        post.setPublishedAt(publishedAt);
        post.setCategories(categories);
        post.setUpdatedAt(now);
        post.setDownloadText(blankToNull(request.downloadText()));
        post.setDownloadUrl(blankToNull(request.downloadUrl()));
    }

    // El orden ya está fijado en las consultas; se ignora el sort del cliente y se limita el tamaño de página
    private static Pageable limited(Pageable pageable, int maxSize) {
        return PageRequest.of(pageable.getPageNumber(), Math.min(pageable.getPageSize(), maxSize));
    }

    private <T> Page<T> mapPage(Page<PostSummaryView> page, BiFunction<PostSummaryView, List<CategoryResponse>, T> mapper) {
        Map<Long, List<CategoryResponse>> categories = categoriesByPost(page.getContent());
        return page.map(view -> mapper.apply(view, categories.getOrDefault(view.getId(), List.of())));
    }

    private <T> List<T> withCategories(List<PostSummaryView> views, BiFunction<PostSummaryView, List<CategoryResponse>, T> mapper) {
        Map<Long, List<CategoryResponse>> categories = categoriesByPost(views);
        return views.stream()
                .map(view -> mapper.apply(view, categories.getOrDefault(view.getId(), List.of())))
                .toList();
    }

    // Una sola consulta para las categorías de todas las noticias del listado
    private Map<Long, List<CategoryResponse>> categoriesByPost(Collection<PostSummaryView> views) {
        if (views.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = views.stream().map(PostSummaryView::getId).toList();

        Map<Long, List<CategoryResponse>> result = new LinkedHashMap<>();
        for (PostCategoryView row : postRepository.findCategoriesOfPosts(ids)) {
            result.computeIfAbsent(row.getPostId(), id -> new ArrayList<>())
                    .add(new CategoryResponse(row.getCategoryId(), row.getName(), row.getSlug()));
        }
        return result;
    }

    // El contenido viene del editor HTML y se renderiza con innerHTML en el sitio público
    private static String sanitizeHtml(String html) {
        Safelist safelist = Safelist.relaxed()
                .preserveRelativeLinks(true)
                .addEnforcedAttribute("a", "rel", "noopener noreferrer");
        return Jsoup.clean(html, "", safelist, new Document.OutputSettings().prettyPrint(false));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
