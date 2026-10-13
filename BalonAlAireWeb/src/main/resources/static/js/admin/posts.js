let posts = [];
let categories = [];
let currentPostImageUrl = null;
let currentDownloadUrl = null;
let elementId="posts-message"

document.addEventListener("DOMContentLoaded", () => {
    initTinyEditor();
    loadPosts();
    loadCategories();

    document
        .getElementById("new-post-button")
        .addEventListener("click", () => {
            openPostForm();
        });

    document
        .getElementById("cancel-post-button")
        .addEventListener("click", () => {
            closePostForm();
        });

    document
        .getElementById("post-form")
        .addEventListener("submit", savePost);

    document
        .getElementById("post-image")
        .addEventListener("change", previewPostImage);
});

async function initTinyEditor() {
    await loadTinyMceScript();

    if (!window.tinymce) {
        console.warn("TinyMCE no está disponible. Se usará el textarea original.");
        return;
    }

    tinymce.init({
        selector: "#post-content",
        height: 360,
        menubar: false,
        branding: false,
        promotion: false,
        language: "es",
        plugins: "autolink lists link table code wordcount",
        toolbar: "undo redo | blocks | bold italic underline | bullist numlist | link table | removeformat | code",
        block_formats: "Párrafo=p; Título 2=h2; Título 3=h3",
        content_style: "body { font-family: Arial, sans-serif; font-size: 16px; line-height: 1.65; }",
        setup: (editor) => {
            editor.on("change keyup undo redo", () => {
                editor.save();
            });
        }
    });
}

async function loadTinyMceScript() {
    if (window.tinymce) {
        return;
    }

    try {
        const config = await apiGet("/admin/config/editor");
        const apiKey = encodeURIComponent(config.tinymceApiKey || "no-api-key");

        await new Promise((resolve, reject) => {
            const script = document.createElement("script");
            script.src = `https://cdn.tiny.cloud/1/${apiKey}/tinymce/8/tinymce.min.js`;
            script.referrerPolicy = "origin";
            script.crossOrigin = "anonymous";
            script.onload = resolve;
            script.onerror = () => reject(new Error("No se pudo cargar TinyMCE."));
            document.head.appendChild(script);
        });
    } catch (error) {
        console.warn(error.message || "No se pudo cargar la configuración del editor.");
    }
}

function getPostContent() {
    if (window.tinymce) {
        const editor = tinymce.get("post-content");
        if (editor) {
            editor.save();
            return editor.getContent().trim();
        }
    }

    return document.getElementById("post-content").value.trim();
}

function setPostContent(value) {
    const content = value || "";
    const textarea = document.getElementById("post-content");
    textarea.value = content;

    if (window.tinymce) {
        const editor = tinymce.get("post-content");
        if (editor) {
            editor.setContent(content);
            editor.save();
        }
    }
}


async function loadPosts() {
    const tbody = document.getElementById("posts-table-body");

    try {
        const page = await apiGet("/admin/posts");
        posts = page.content;

        renderPosts(posts);

    } catch (error) {
        showMessage(elementId, error.message, "error");
    }
}


async function loadCategories() {
    try {
        categories = await apiGet("/admin/categories");
        renderCategoryOptions();
    } catch (error) {
        console.error("Error cargando categorías:", error);
    }
}


function renderPosts(posts) {
    const tbody = document.getElementById("posts-table-body");

    tbody.innerHTML = "";

    if (!posts || posts.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7">No hay noticias cargadas.</td>
            </tr>
        `;
        return;
    }

    posts.forEach(post => {
        const row = document.createElement("tr");

        const publishedDate = post.publishedAt
            ? formatDate(post.publishedAt)
            : "-";

        const statusClass = post.status === "PUBLISHED"
            ? "status-published"
            : "status-draft";

        row.innerHTML = `
            <td>${escapeHtml(post.id)}</td>

            <td>
                <strong>${escapeHtml(post.title)}</strong>
            </td>

            <td>
                ${renderCategories(post.categories)}
            </td>

            <td>
                ${escapeHtml(post.author || "-")}
            </td>

            <td>
                <span class="status ${statusClass}">
                    ${escapeHtml(post.status || "DRAFT")}
                </span>
            </td>

            <td>
                ${publishedDate}
            </td>

            <td>
                <div class="admin-actions">
                    <button
                        class="btn btn-secondary btn-small"
                        onclick="editPost(${post.id})">
                        Editar
                    </button>

                    <button
                        class="btn btn-danger btn-small"
                        onclick="deletePost(${post.id})">
                        Eliminar
                    </button>
                </div>
            </td>
        `;

        tbody.appendChild(row);
    });
}


function renderCategories(postCategories) {
    if (!postCategories || postCategories.length === 0) {
        return "-";
    }

    return postCategories
        .map(category => escapeHtml(category.name))
        .join(", ");
}


function renderCategoryOptions(selectedCategories = []) {
    let container = document.getElementById("post-categories");

    if (!container) {
        return;
    }

    container.innerHTML = "";

    categories.forEach(category => {
        const checked = selectedCategories.some(
            selected => selected.slug === category.slug
        );

        const label = document.createElement("label");

        label.innerHTML = `
            <input
                type="checkbox"
                name="categories"
                value="${category.id}"
                ${checked ? "checked" : ""}
            >
            ${escapeHtml(category.name)}
        `;

        container.appendChild(label);
    });
}


function openPostForm(post = null) {
    const container = document.getElementById("post-form-container");
    const form = document.getElementById("post-form");

    container.hidden = false;
    container.style.display = "block";

    if (!post) {
        form.reset();

        document.getElementById("post-id").value = "";
        setPostContent("");

        document.getElementById("post-status").value = "DRAFT";
        document.getElementById("post-download-text").value = "";
        document.getElementById("post-download-url").value = "";
        document.getElementById("post-download-file").value = "";
        currentPostImageUrl = null;
        currentDownloadUrl = null;
        document.getElementById("post-image-preview").innerHTML = "";

        renderCategoryOptions([]);

        return;
    }

    document.getElementById("post-id").value = post.id;
    document.getElementById("post-title").value = post.title || "";
    document.getElementById("post-slug").value = post.slug || "";
    document.getElementById("post-excerpt").value = post.excerpt || "";
    setPostContent(post.content || "");
    document.getElementById("post-download-text").value = post.downloadText || "";
    document.getElementById("post-download-url").value = post.downloadUrl || "";
    document.getElementById("post-download-file").value = "";
    document.getElementById("post-img-url").value = post.imgUrl || "";
    document.getElementById("post-author").value = post.author || "";
    document.getElementById("post-status").value = post.status || "DRAFT";
    currentPostImageUrl = post.imgUrl || null;
    currentDownloadUrl = post.downloadUrl || null;
    renderPostImagePreview(currentPostImageUrl);

    if (post.publishedAt) {
        document.getElementById("post-published-at").value =
            formatDateForInput(post.publishedAt);
    } else {
        document.getElementById("post-published-at").value = "";
    }

    renderCategoryOptions(post.categories || []);

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}


function closePostForm() {
    const container = document.getElementById("post-form-container");

    container.style.display = "none";
    container.hidden = true;
}

function previewPostImage(event) {
    const file = event.target.files[0];

    if (!file) {
        renderPostImagePreview(currentPostImageUrl);
        return;
    }

    const allowedTypes = [
        "image/jpeg",
        "image/png",
        "image/webp"
    ];

    if (!allowedTypes.includes(file.type)) {
        showMessage(elementId,"Solo se permiten imágenes JPG, PNG o WebP.", "error");
        event.target.value = "";
        renderPostImagePreview(currentPostImageUrl);
        return;
    }

    renderPostImagePreview(URL.createObjectURL(file));
}

function renderPostImagePreview(imageUrl) {
    const preview = document.getElementById("post-image-preview");

    if (!preview) {
        return;
    }

    if (!imageUrl) {
        preview.innerHTML = "";
        return;
    }

    preview.innerHTML = `
        <img
            src="${escapeHtml(imageUrl)}"
            alt="Vista previa de la noticia"
        >
    `;
}

async function uploadPostImage(file) {
    return uploadFile(file);
}

async function uploadDownloadFile(file) {
    if (file && file.type !== "application/pdf") {
        throw new Error("Solo se permiten archivos PDF para descarga.");
    }

    return uploadFile(file);
}

async function savePost(event) {
    event.preventDefault();

    const content = getPostContent();
    const id = document.getElementById("post-id").value;
    const file = document.getElementById("post-image").files[0];
    const downloadFile = document.getElementById("post-download-file").files[0];
    const manualImageUrl = document.getElementById("post-img-url").value.trim();

    const selectedCategoryIds = Array.from(
        document.querySelectorAll(
            '#post-categories input[name="categories"]:checked'
        )
    ).map(input => Number(input.value));

    const submitButton = document.querySelector(
        "#post-form button[type='submit']"
    );

    try {
        if (submitButton) {
            submitButton.disabled = true;
            submitButton.textContent = "Guardando...";
        }

        const uploadedImageUrl = await uploadPostImage(file);
        const imgUrl = uploadedImageUrl || manualImageUrl || currentPostImageUrl;
        const uploadedDownloadUrl = await uploadDownloadFile(downloadFile);
        const downloadUrl =
            uploadedDownloadUrl ||
            document.getElementById("post-download-url").value.trim() ||
            currentDownloadUrl;

        if (!imgUrl) {
            throw new Error("La noticia necesita una imagen.");
        }

        if (!content) {
            throw new Error("La noticia necesita contenido.");
        }

        const postData = {
            title:       document.getElementById("post-title").value.trim(),
            slug:        document.getElementById("post-slug").value.trim(),
            excerpt:     document.getElementById("post-excerpt").value.trim(),
            content,
            imgUrl,
            author:      document.getElementById("post-author").value.trim(),
            status:      document.getElementById("post-status").value,
            publishedAt: getPublishedAt(),
            categoryIds: selectedCategoryIds,
            downloadText: document.getElementById("post-download-text").value.trim(),
            downloadUrl
        };

        if (id) {
            // ACTUALIZAR — PERF-02: actualizar en array local sin refetch
            const updated = await apiPut(`/admin/posts/${id}`, postData);
            const idx = posts.findIndex(p => String(p.id) === String(id));
            if (idx !== -1) posts[idx] = updated;
            showMessage(elementId, "Noticia actualizada correctamente.", "success");
        } else {
            // CREAR — PERF-02: agregar al array local sin refetch
            const created = await apiPost("/admin/posts", postData);
            posts.unshift(created);
            showMessage(elementId, "Noticia creada correctamente.", "success");
        }

        closePostForm();
        renderPosts(posts);

    } catch (error) {
        showMessage(elementId, error.message, "error");
    } finally {
        if (submitButton) {
            submitButton.disabled = false;
            submitButton.textContent = "Guardar";
        }
    }
}


function getPublishedAt() {
    const value = document.getElementById("post-published-at").value;

    if (!value) {
        return null;
    }

    return value.length === 16
        ? `${value}:00`
        : value;
}


async function editPost(id) {
    try {
        // La tabla solo trae resúmenes: el contenido completo se pide al editar
        const post = await apiGet(`/admin/posts/${id}`);

        if (!post) {
            throw new Error("No se encontró la noticia.");
        }

        openPostForm(post);

    } catch (error) {
        showMessage(elementId, error.message, "error");
    }
}

async function deletePost(id) {
    const confirmed = confirm(
        "¿Seguro que querés eliminar esta noticia?"
    );

    if (!confirmed) {
        return;
    }

    try {
        await apiDelete(`/admin/posts/${id}`);

        // PERF-02: filtrar el array local sin refetch
        posts = posts.filter(p => p.id !== id);
        renderPosts(posts);

        showMessage(elementId, "Noticia eliminada correctamente.", "success");

    } catch (error) {
        showMessage(elementId, error.message, "error");
    }
}





function formatDate(dateString) {
    if (!dateString) {
        return "-";
    }

    return new Date(dateString).toLocaleString("es-AR");
}


function formatDateForInput(dateString) {
    const date = new Date(dateString);

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");

    return `${year}-${month}-${day}T${hours}:${minutes}`;
}


