package basketball.projects.balonAlAire;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import basketball.projects.balonAlAire.model.Advertisement;
import basketball.projects.balonAlAire.model.Category;
import basketball.projects.balonAlAire.model.Post;
import basketball.projects.balonAlAire.model.PostStatus;
import basketball.projects.balonAlAire.model.Role;
import basketball.projects.balonAlAire.model.User;
import basketball.projects.balonAlAire.repository.AdvertisementRepository;
import basketball.projects.balonAlAire.repository.CategoryRepository;
import basketball.projects.balonAlAire.repository.PostRepository;
import basketball.projects.balonAlAire.repository.UserRepository;
import basketball.projects.balonAlAire.service.PasswordResetService;
import basketball.projects.balonAlAire.service.UserService;

/** Reglas de acceso: qué ve el público, qué puede cada rol y que los datos no se filtren. */
class SecurityRulesIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    PostRepository postRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    AdvertisementRepository advertisementRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @Autowired
    PasswordResetService passwordResetService;

    String draftSlug;
    String scheduledSlug;
    String publishedSlug;

    @BeforeEach
    void createContent() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        draftSlug = "borrador-" + suffix;
        scheduledSlug = "programada-" + suffix;
        publishedSlug = "publicada-" + suffix;

        postRepository.save(newPost(draftSlug, PostStatus.DRAFT, null));
        postRepository.save(newPost(scheduledSlug, PostStatus.PUBLISHED, LocalDateTime.now().plusDays(5)));
        postRepository.save(newPost(publishedSlug, PostStatus.PUBLISHED, LocalDateTime.now().minusHours(1)));
    }

    private Post newPost(String slug, PostStatus status, LocalDateTime publishedAt) {
        Category category = categoryRepository.findBySlug("basquet-nacional").orElseThrow();
        Post post = new Post();
        post.setTitle(slug);
        post.setSlug(slug);
        post.setExcerpt("resumen");
        post.setContent("<p>contenido</p>");
        post.setImgUrl("/img.jpg");
        post.setAuthor("autor");
        post.setStatus(status);
        post.setPublishedAt(publishedAt);
        post.setCategories(List.of(category));
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }

    // --- API pública ---

    @Test
    void anonymousSeesOnlyPublishedPosts() throws Exception {
        mockMvc.perform(get("/api/public/posts").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].slug", hasItem(publishedSlug)))
                .andExpect(jsonPath("$.content[*].slug", not(hasItem(draftSlug))))
                .andExpect(jsonPath("$.content[*].slug", not(hasItem(scheduledSlug))));

        mockMvc.perform(get("/api/public/posts/" + publishedSlug)).andExpect(status().isOk());
        mockMvc.perform(get("/api/public/posts/" + draftSlug)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/public/posts/" + scheduledSlug)).andExpect(status().isNotFound());
    }

    @Test
    void publicCategoryPostsExcludeDrafts() throws Exception {
        mockMvc.perform(get("/api/public/categories/basquet-nacional/posts").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].slug", hasItem(publishedSlug)))
                .andExpect(jsonPath("$.content[*].slug", not(hasItem(draftSlug))));
    }

    @Test
    void publicAdvertisementsAreActiveOnlyAndExposeNoInternalFields() throws Exception {
        Advertisement inactive = new Advertisement();
        inactive.setName("inactivo-" + UUID.randomUUID());
        inactive.setImageUrl("/x.png");
        inactive.setLinkUrl("https://example.com");
        inactive.setActive(false);
        inactive.setCreatedAt(LocalDateTime.now());
        inactive.setUpdatedAt(LocalDateTime.now());
        advertisementRepository.save(inactive);

        mockMvc.perform(get("/api/public/advertisements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", not(hasItem(inactive.getName()))))
                .andExpect(jsonPath("$[0].active").doesNotExist())
                .andExpect(jsonPath("$[0].createdAt").doesNotExist());
    }

    @Test
    void publicApiIsReadOnly() throws Exception {
        mockMvc.perform(post("/api/public/posts").with(csrf())).andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void legacyLoginEndpointIsGone() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    // --- Rendimiento: listados livianos, paginación y búsqueda ---

    @Test
    void listingsAreSummariesWithoutContentButDetailIsComplete() throws Exception {
        mockMvc.perform(get("/api/public/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").doesNotExist())
                .andExpect(jsonPath("$.content[0].title").exists())
                .andExpect(jsonPath("$.content[0].categories").isArray());

        mockMvc.perform(get("/api/public/posts/" + publishedSlug))
                .andExpect(jsonPath("$.content").value("<p>contenido</p>"));
    }

    @Test
    void categoryPostsArePaginated() throws Exception {
        mockMvc.perform(get("/api/public/categories/basquet-nacional/posts").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].content").doesNotExist())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void relatedPostsExcludeTheCurrentOne() throws Exception {
        mockMvc.perform(get("/api/public/posts/" + publishedSlug + "/related").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(3)))
                .andExpect(jsonPath("$[*].slug", not(hasItem(publishedSlug))))
                .andExpect(jsonPath("$[*].slug", not(hasItem(draftSlug))));

        mockMvc.perform(get("/api/public/posts/" + draftSlug + "/related")).andExpect(status().isNotFound());
    }

    @Test
    void searchRanksTitleMatchesAboveBodyMatches() throws Exception {
        String word = UUID.randomUUID().toString().replaceAll("[^a-f]", "") + "zzqk";

        Post inBody = newPost("cuerpo-" + word, PostStatus.PUBLISHED, LocalDateTime.now().minusMinutes(1));
        inBody.setTitle("Una nota cualquiera");
        inBody.setContent("<p>aca aparece " + word + " solo en el cuerpo</p>");
        postRepository.save(inBody);

        Post inTitle = newPost("titulo-" + word, PostStatus.PUBLISHED, LocalDateTime.now().minusDays(3));
        inTitle.setTitle("Todo sobre " + word);
        postRepository.save(inTitle);

        mockMvc.perform(get("/api/public/posts").param("search", word))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].slug").value("titulo-" + word))
                .andExpect(jsonPath("$.content[1].slug").value("cuerpo-" + word))
                .andExpect(jsonPath("$.content[0].content").doesNotExist());
    }
    // --- Admin ---

    @Test
    void adminApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/posts")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/advertisements")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminSeesDraftsAndScheduledPosts() throws Exception {
        mockMvc.perform(get("/api/admin/posts").param("size", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].slug", hasItem(draftSlug)))
                .andExpect(jsonPath("$.content[*].slug", hasItem(scheduledSlug)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void writesWithoutCsrfTokenAreRejected() throws Exception {
        mockMvc.perform(post("/api/admin/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void editorCanManagePostsAndReadCategories() throws Exception {
        mockMvc.perform(get("/api/admin/posts")).andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/categories")).andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/me")).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("EDITOR"));
    }

    @Test
    @WithMockUser(roles = "EDITOR")
    void editorCannotTouchAdminOnlyResources() throws Exception {
        mockMvc.perform(get("/api/admin/advertisements")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/settings")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"slug\":\"x\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanRegisterUsersWithARole() throws Exception {
        String username = "redactor-" + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@example.com","password":"una-clave-bien-larga-1","role":"EDITOR"}
                                """.formatted(username, username)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("EDITOR"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s2","email":"%s2@example.com","password":"corta","role":"EDITOR"}
                                """.formatted(username, username)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.password").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminListIsSummariesAndDetailIsLoadedById() throws Exception {
        Long id = postRepository.save(newPost("detalle-" + UUID.randomUUID(), PostStatus.DRAFT, null)).getId();

        mockMvc.perform(get("/api/admin/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].content").doesNotExist())
                .andExpect(jsonPath("$.content[0].status").exists());

        mockMvc.perform(get("/api/admin/posts/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("<p>contenido</p>"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteRemovesOnceAndThenReportsNotFound() throws Exception {
        Long id = postRepository.save(newPost("borrar-" + UUID.randomUUID(), PostStatus.DRAFT, null)).getId();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/admin/posts/" + id).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/admin/posts/" + id).with(csrf()))
                .andExpect(status().isNotFound());
    }
    // --- Validación y saneamiento ---

    @Test
    @WithMockUser(roles = "ADMIN")
    void postPayloadIsValidatedAndSanitized() throws Exception {
        Long categoryId = categoryRepository.findBySlug("basquet-nacional").orElseThrow().getId();

        mockMvc.perform(post("/api/admin/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson(categoryId, "Slug Con Espacios/", "/img.jpg", "<p>x</p>")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.slug").exists());

        mockMvc.perform(post("/api/admin/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson(categoryId, "ok-slug-1", "javascript:alert(1)", "<p>x</p>")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.imgUrl").exists());

        mockMvc.perform(post("/api/admin/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson(999999L, "ok-slug-2", "/img.jpg", "<p>x</p>")))
                .andExpect(status().isBadRequest());

        String slug = "sanitizada-" + UUID.randomUUID().toString().substring(0, 8);
        mockMvc.perform(post("/api/admin/posts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postJson(categoryId, slug, "/img.jpg", "<p onclick='x()'>hola</p><script>alert(1)</script>")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("<p>hola</p>"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    private static String postJson(Long categoryId, String slug, String imgUrl, String content) {
        return """
                {"title":"Titulo","slug":"%s","excerpt":"resumen","content":"%s","imgUrl":"%s",
                 "author":"autor","categoryIds":[%d]}
                """.formatted(slug, content.replace("\"", "\\\""), imgUrl, categoryId);
    }

    // --- Login y recuperación de contraseña ---

    @Test
    void loginIsLockedAfterRepeatedFailures() throws Exception {
        String username = "bloqueado-" + UUID.randomUUID().toString().substring(0, 8);

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/admin/login").with(csrf()).param("username", username).param("password", "mala"))
                    .andExpect(redirectedUrl("/admin/login?error=true"));
        }

        mockMvc.perform(post("/admin/login").with(csrf()).param("username", username).param("password", "mala"))
                .andExpect(redirectedUrl("/admin/login?locked=true"));
    }

    @Test
    void passwordResetTokenIsStoredHashedAndBogusTokensAreRejected() throws Exception {
        String username = "reset-" + UUID.randomUUID().toString().substring(0, 8);
        User user = userService.register(username, "una-clave-bien-larga-1", username + "@example.com", Role.EDITOR);

        passwordResetService.requestPasswordReset(username);

        String stored = userRepository.findById(user.getId()).orElseThrow().getPasswordResetToken();
        org.assertj.core.api.Assertions.assertThat(stored).matches("^[0-9a-f]{64}$");

        mockMvc.perform(post("/api/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"%s\",\"newPassword\":\"otra-clave-bien-larga-2\"}".formatted(stored)))
                .andExpect(status().isBadRequest());
    }
}
