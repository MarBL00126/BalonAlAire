let posts = [];
let categories = [];
let currentPostImageUrl = null;
let elementId="posts-message"

document.addEventListener("DOMContentLoaded", () => {
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


async function loadPosts() {
    const tbody = document.getElementById("posts-table-body");

    try {
        const page = await apiGet("/posts");
        posts = page.content;

        renderPosts(posts);

    } catch (error) {
        showMessage(elementId, error.message, "error");
    }
}


async function loadCategories() {
    try {
        categories = await apiGet("/categories");
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

        document.getElementById("post-status").value = "DRAFT";
        currentPostImageUrl = null;
        document.getElementById("post-image-preview").innerHTML = "";

        renderCategoryOptions([]);

        return;
    }

    document.getElementById("post-id").value = post.id;
    document.getElementById("post-title").value = post.title || "";
    document.getElementById("post-slug").value = post.slug || "";
    document.getElementById("post-excerpt").value = post.excerpt || "";
    document.getElementById("post-content").value = post.content || "";
    document.getElementById("post-img-url").value = post.imgUrl || "";
    document.getElementById("post-author").value = post.author || "";
    document.getElementById("post-status").value = post.status || "DRAFT";
    currentPostImageUrl = post.imgUrl || null;
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
    if (!file) {
        return null;
    }

    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch("/api/uploads", {
        method: "POST",
        body: formData
    });

    if (response.status === 401 || response.status === 403) {
        window.location.href = "/admin/login";
        return null;
    }

    if (!response.ok) {
        let message = `Error subiendo imagen (${response.status})`;

        try {
            const errorData = await response.json();
            message = errorData.message || errorData.error || message;
        } catch {
            // La respuesta no era JSON.
        }

        throw new Error(message);
    }

    const data = await response.json();

    if (!data.url) {
        throw new Error("El servidor no devolvió la URL de la imagen.");
    }

    return data.url;
}


async function savePost(event) {
    event.preventDefault();

    const id = document.getElementById("post-id").value;
    const file = document.getElementById("post-image").files[0];
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

        if (!imgUrl) {
            throw new Error("La noticia necesita una imagen.");
        }

        const postData = {
            title:       document.getElementById("post-title").value.trim(),
            slug:        document.getElementById("post-slug").value.trim(),
            excerpt:     document.getElementById("post-excerpt").value.trim(),
            content:     document.getElementById("post-content").value.trim(),
            imgUrl,
            author:      document.getElementById("post-author").value.trim(),
            status:      document.getElementById("post-status").value,
            publishedAt: getPublishedAt(),
            categoryIds: selectedCategoryIds
        };

        if (id) {
            // ACTUALIZAR — PERF-02: actualizar en array local sin refetch
            const updated = await apiPut(`/posts/${id}`, postData);
            const idx = posts.findIndex(p => String(p.id) === String(id));
            if (idx !== -1) posts[idx] = updated;
            showMessage(elementId, "Noticia actualizada correctamente.", "success");
        } else {
            // CREAR — PERF-02: agregar al array local sin refetch
            const created = await apiPost("/posts", postData);
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
        const post = posts.find(post => post.id === id);

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
        await apiDelete(`/posts/${id}`);

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


