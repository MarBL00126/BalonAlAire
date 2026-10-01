/* =============================================================
 * ESTADO DEL MÓDULO
 * ============================================================= */

let advertisements = [];
let currentImageUrl = null;
const elementId = "advertisements-message";


/* =============================================================
 * INICIALIZACIÓN
 * ============================================================= */

document.addEventListener("DOMContentLoaded", () => {

    loadAdvertisements();

    document
        .getElementById("new-advertisement-button")
        .addEventListener("click", openNewAdvertisementForm);

    document
        .getElementById("cancel-advertisement-button")
        .addEventListener("click", closeAdvertisementForm);

    document
        .getElementById("advertisement-form")
        .addEventListener("submit", saveAdvertisement);

    document
        .getElementById("advertisement-image")
        .addEventListener("change", previewImage);
});


/* =============================================================
 * CARGAR ANUNCIOS
 * ============================================================= */

async function loadAdvertisements() {
    try {
        const page = await apiGet("/advertisements");
        advertisements = page.content;
        renderAdvertisements(advertisements);
    } catch (error) {
        console.error("Error cargando anuncios:", error);
        showMessage(elementId, error.message, "error");
    }
}


/* =============================================================
 * RENDERIZAR TABLA
 * ============================================================= */

function renderAdvertisements(ads) {
    const tbody = document.getElementById("advertisements-table-body");
    tbody.innerHTML = "";

    if (!ads || ads.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6">No hay anuncios cargados.</td>
            </tr>
        `;
        return;
    }

    ads.forEach(ad => {
        const row = document.createElement("tr");
        row.innerHTML = buildAdRow(ad);
        tbody.appendChild(row);
    });
}

function buildAdRow(ad) {
    const statusClass = ad.active ? "status-active" : "status-inactive";
    const statusText  = ad.active ? "Activo" : "Inactivo";
    const imageCell   = ad.imageUrl
        ? `<img src="${escapeHtml(ad.imageUrl)}" alt="${escapeHtml(ad.name)}" class="ad-preview">`
        : "Sin imagen";

    return `
        <td>${escapeHtml(ad.id)}</td>
        <td>${imageCell}</td>
        <td><strong>${escapeHtml(ad.name)}</strong></td>
        <td>${escapeHtml(ad.position || "-")}</td>
        <td><span class="status ${statusClass}">${statusText}</span></td>
        <td>
            <div class="admin-actions">
                <button type="button" class="btn btn-secondary btn-small"
                    onclick="editAdvertisement(${ad.id})">Editar</button>
                <button type="button" class="btn btn-danger btn-small"
                    onclick="deleteAdvertisement(${ad.id})">Eliminar</button>
            </div>
        </td>
    `;
}


/* =============================================================
 * ABRIR FORMULARIO — NUEVO
 * ============================================================= */

function openNewAdvertisementForm() {
    document.getElementById("advertisement-form").reset();
    document.getElementById("advertisement-id").value = "";
    document.getElementById("advertisement-active").checked = true;

    currentImageUrl = null;
    renderAdImagePreview(null);

    document.getElementById("advertisement-form-title").textContent = "Nuevo anuncio";

    document.getElementById("advertisement-form-container").style.display = "block";
}


/* =============================================================
 * ABRIR FORMULARIO — EDITAR
 * ============================================================= */

function openEditAdvertisementForm(ad) {
    document.getElementById("advertisement-id").value    = ad.id;
    document.getElementById("advertisement-name").value  = ad.name    || "";
    document.getElementById("advertisement-link").value  = ad.linkUrl || "";
    document.getElementById("advertisement-position").value = ad.position || "";
    document.getElementById("advertisement-active").checked = ad.active;

    // Guardamos la URL actual: si el admin no sube otra imagen, la mantenemos
    currentImageUrl = ad.imageUrl || null;
    renderAdImagePreview(currentImageUrl);

    document.getElementById("advertisement-form-title").textContent = "Editar anuncio";

    document.getElementById("advertisement-form-container").style.display = "block";

    window.scrollTo({ top: 0, behavior: "smooth" });
}


/* =============================================================
 * CERRAR FORMULARIO
 * ============================================================= */

function closeAdvertisementForm() {
    document.getElementById("advertisement-form-container").style.display = "none";
}


/* =============================================================
 * PREVIEW DE IMAGEN
 * ============================================================= */

/** Renderiza la imagen de preview dado una URL (local blob o remota). */
function renderAdImagePreview(url) {
    const preview = document.getElementById("advertisement-image-preview");

    if (!url) {
        preview.innerHTML = "";
        return;
    }

    preview.innerHTML = `
        <img
            src="${escapeHtml(url)}"
            alt="Vista previa"
            style="max-width:300px; max-height:150px; object-fit:contain;">
    `;
}

/** Handler del input file: valida el tipo y muestra un preview local. */
function previewImage(event) {
    const file = event.target.files[0];

    if (!file) {
        // Si quitaron la selección, restauramos la imagen existente (si la hay)
        renderAdImagePreview(currentImageUrl);
        return;
    }

    const allowedTypes = ["image/jpeg", "image/png", "image/webp"];

    if (!allowedTypes.includes(file.type)) {
        showMessage(elementId, "Solo se permiten imágenes JPG, PNG o WebP.", "error");
        event.target.value = "";
        renderAdImagePreview(currentImageUrl);
        return;
    }

    renderAdImagePreview(URL.createObjectURL(file));
}


/* =============================================================
 * SUBIR IMAGEN AL SERVIDOR
 * ============================================================= */

async function uploadImage(file) {
    if (!file) {
        return currentImageUrl; // Sin archivo nuevo → conservar la URL actual
    }

    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch("/api/uploads", { method: "POST", body: formData });

    if (response.status === 401 || response.status === 403) {
        window.location.href = "/admin/login";
        return null;
    }

    if (!response.ok) {
        let message = `Error subiendo imagen (${response.status})`;
        try {
            const errorData = await response.json();
            message = errorData.message || errorData.error || message;
        } catch { /* respuesta no-JSON */ }
        throw new Error(message);
    }

    const data = await response.json();
    if (!data.url) throw new Error("El servidor no devolvió la URL de la imagen.");
    return data.url;
}


/* =============================================================
 * GUARDAR ANUNCIO (crear o actualizar)
 * ============================================================= */

async function saveAdvertisement(event) {
    event.preventDefault();

    const id   = document.getElementById("advertisement-id").value;
    const file = document.getElementById("advertisement-image").files[0];
    const submitButton = document.querySelector("#advertisement-form button[type='submit']");

    setSubmitLoading(submitButton, true);

    try {
        const imageUrl = await uploadImage(file);

        if (!id && !imageUrl) {
            throw new Error("Debés seleccionar una imagen.");
        }

        const advertisement = readFormValues(imageUrl);
        validateAdvertisement(advertisement);

        if (id) {
            await apiPut(`/advertisements/${id}`, advertisement);
            showMessage(elementId, "Anuncio actualizado correctamente.", "success");
        } else {
            await apiPost("/advertisements", advertisement);
            showMessage(elementId, "Anuncio creado correctamente.", "success");
        }

        closeAdvertisementForm();
        await loadAdvertisements();

    } catch (error) {
        console.error("Error guardando anuncio:", error);
        showMessage(elementId, error.message, "error");
    } finally {
        setSubmitLoading(submitButton, false);
    }
}

/** Lee los valores del formulario y construye el objeto a enviar. */
function readFormValues(imageUrl) {
    return {
        name:     document.getElementById("advertisement-name").value.trim(),
        imageUrl,
        linkUrl:  document.getElementById("advertisement-link").value.trim(),
        position: document.getElementById("advertisement-position").value,
        active:   document.getElementById("advertisement-active").checked,
    };
}

/** Valida los campos obligatorios antes de enviar. Lanza Error si algo falta. */
function validateAdvertisement(ad) {
    if (!ad.name)     throw new Error("El nombre del anuncio es obligatorio.");
    if (!ad.imageUrl) throw new Error("El anuncio necesita una imagen.");
    if (!ad.linkUrl)  throw new Error("La URL destino es obligatoria.");
    if (!ad.position) throw new Error("Seleccioná una posición.");
}

/** Activa/desactiva el estado de carga del botón de submit. */
function setSubmitLoading(button, loading) {
    if (!button) return;
    button.disabled    = loading;
    button.textContent = loading ? "Guardando..." : "Guardar anuncio";
}


/* =============================================================
 * EDITAR ANUNCIO
 * ============================================================= */

function editAdvertisement(id) {
    const ad = advertisements.find(a => a.id === id);

    if (!ad) {
        showMessage(elementId, "No se encontró el anuncio.", "error");
        return;
    }

    openEditAdvertisementForm(ad);
}


/* =============================================================
 * ELIMINAR ANUNCIO
 * ============================================================= */

async function deleteAdvertisement(id) {
    if (!confirm("¿Seguro que querés eliminar este anuncio?")) return;

    try {
        await apiDelete(`/advertisements/${id}`);
        showMessage(elementId, "Anuncio eliminado correctamente.", "success");
        await loadAdvertisements();
    } catch (error) {
        console.error("Error eliminando anuncio:", error);
        showMessage(elementId, error.message, "error");
    }
}
