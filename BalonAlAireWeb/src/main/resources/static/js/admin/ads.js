let advertisements = [];
let currentImageUrl = null;

document.addEventListener("DOMContentLoaded", () => {

    loadAdvertisements();

    document
        .getElementById("new-advertisement-button")
        .addEventListener("click", () => {
            openAdvertisementForm();
        });

    document
        .getElementById("cancel-advertisement-button")
        .addEventListener("click", () => {
            closeAdvertisementForm();
        });

    document
        .getElementById("advertisement-form")
        .addEventListener("submit", saveAdvertisement);

    document
        .getElementById("advertisement-image")
        .addEventListener("change", previewImage);
});


/*
 * ============================================================
 * CARGAR ANUNCIOS
 * ============================================================
 */

async function loadAdvertisements() {

    try {

        advertisements = await apiGet("/advertisements");

        renderAdvertisements(advertisements);

    } catch (error) {

        console.error(
            "Error cargando anuncios:",
            error
        );

        showMessage(
            error.message,
            "error"
        );
    }
}


/*
 * ============================================================
 * RENDERIZAR TABLA
 * ============================================================
 */

function renderAdvertisements(ads) {

    const tbody =
        document.getElementById(
            "advertisements-table-body"
        );

    tbody.innerHTML = "";


    if (!ads || ads.length === 0) {

        tbody.innerHTML = `
            <tr>
                <td colspan="6">
                    No hay anuncios cargados.
                </td>
            </tr>
        `;

        return;
    }


    ads.forEach(ad => {

        const row =
            document.createElement("tr");


        const statusClass =
            ad.active
                ? "status-active"
                : "status-inactive";


        const statusText =
            ad.active
                ? "Activo"
                : "Inactivo";


        row.innerHTML = `

            <td>
                ${escapeHtml(ad.id)}
            </td>

            <td>

                ${
                    ad.imageUrl
                        ? `
                            <img
                                src="${escapeHtml(ad.imageUrl)}"
                                alt="${escapeHtml(ad.name)}"
                                class="ad-preview">
                          `
                        : "Sin imagen"
                }

            </td>

            <td>

                <strong>
                    ${escapeHtml(ad.name)}
                </strong>

            </td>

            <td>
                ${escapeHtml(ad.position || "-")}
            </td>

            <td>

                <span class="status ${statusClass}">
                    ${statusText}
                </span>

            </td>

            <td>

                <div class="admin-actions">

                    <button
                        type="button"
                        class="btn btn-secondary btn-small"
                        onclick="editAdvertisement(${ad.id})">

                        Editar

                    </button>

                    <button
                        type="button"
                        class="btn btn-danger btn-small"
                        onclick="deleteAdvertisement(${ad.id})">

                        Eliminar

                    </button>

                </div>

            </td>
        `;


        tbody.appendChild(row);

    });
}


/*
 * ============================================================
 * ABRIR FORMULARIO
 * ============================================================
 */

function openAdvertisementForm(ad = null) {

    const container =
        document.getElementById(
            "advertisement-form-container"
        );

    const form =
        document.getElementById(
            "advertisement-form"
        );

    const title =
        document.getElementById(
            "advertisement-form-title"
        );


    container.style.display = "block";


    /*
     * NUEVO ANUNCIO
     */

    if (!ad) {

        form.reset();

        document.getElementById(
            "advertisement-id"
        ).value = "";


        document.getElementById(
            "advertisement-active"
        ).checked = true;


        document.getElementById(
            "advertisement-image-preview"
        ).innerHTML = "";


        currentImageUrl = null;


        title.textContent =
            "Nuevo anuncio";


        return;
    }


    /*
     * EDITAR ANUNCIO
     */

    title.textContent =
        "Editar anuncio";


    document.getElementById(
        "advertisement-id"
    ).value = ad.id;


    document.getElementById(
        "advertisement-name"
    ).value =
        ad.name || "";


    document.getElementById(
        "advertisement-link"
    ).value =
        ad.linkUrl || "";


    document.getElementById(
        "advertisement-position"
    ).value =
        ad.position || "";


    document.getElementById(
        "advertisement-active"
    ).checked =
        ad.active;


    /*
     * Guardamos la URL actual.
     *
     * Si el administrador edita el anuncio
     * pero NO selecciona otra imagen,
     * mantenemos esta URL.
     */

    currentImageUrl =
        ad.imageUrl || null;


    const preview =
        document.getElementById(
            "advertisement-image-preview"
        );


    if (ad.imageUrl) {

        preview.innerHTML = `
            <img
                src="${escapeHtml(ad.imageUrl)}"
                alt="${escapeHtml(ad.name)}"
                style="
                    max-width: 300px;
                    max-height: 150px;
                    object-fit: contain;
                ">
        `;

    } else {

        preview.innerHTML = "";

    }


    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}


/*
 * ============================================================
 * CERRAR FORMULARIO
 * ============================================================
 */

function closeAdvertisementForm() {

    document.getElementById(
        "advertisement-form-container"
    ).style.display = "none";

}


/*
 * ============================================================
 * PREVIEW DE IMAGEN
 * ============================================================
 */

function previewImage(event) {

    const file =
        event.target.files[0];

    const preview =
        document.getElementById(
            "advertisement-image-preview"
        );


    preview.innerHTML = "";


    if (!file) {

        /*
         * Si estamos editando y quitamos
         * la nueva selección, dejamos la
         * imagen anterior.
         */

        if (currentImageUrl) {

            preview.innerHTML = `
                <img
                    src="${escapeHtml(currentImageUrl)}"
                    alt="Imagen actual"
                    style="
                        max-width: 300px;
                        max-height: 150px;
                        object-fit: contain;
                    ">
            `;
        }

        return;
    }


    /*
     * Validación básica del archivo
     */

    const allowedTypes = [
        "image/jpeg",
        "image/png",
        "image/webp"
    ];


    if (!allowedTypes.includes(file.type)) {

        showMessage(
            "Solo se permiten imágenes JPG, PNG o WebP.",
            "error"
        );

        event.target.value = "";

        return;
    }


    /*
     * Preview local
     */

    const imageUrl =
        URL.createObjectURL(file);


    preview.innerHTML = `
        <img
            src="${imageUrl}"
            alt="Vista previa"
            style="
                max-width: 300px;
                max-height: 150px;
                object-fit: contain;
            ">
    `;
}


/*
 * ============================================================
 * SUBIR IMAGEN
 * ============================================================
 */

async function uploadImage(file) {

    /*
     * Si no seleccionamos una nueva imagen,
     * devolvemos la imagen existente.
     */

    if (!file) {
        return currentImageUrl;
    }


    const formData =
        new FormData();


    formData.append(
        "file",
        file
    );


    const response =
        await fetch(
            "/api/uploads",
            {
                method: "POST",
                body: formData
            }
        );


    /*
     * Sesión expirada / sin permisos
     */

    if (
        response.status === 401 ||
        response.status === 403
    ) {

        window.location.href =
            "/admin/login";

        return null;
    }


    /*
     * Error HTTP
     */

    if (!response.ok) {

        let message =
            `Error subiendo imagen (${response.status})`;


        try {

            const errorData =
                await response.json();


            if (errorData.message) {

                message =
                    errorData.message;

            } else if (errorData.error) {

                message =
                    errorData.error;
            }

        } catch {
            // La respuesta no era JSON
        }


        throw new Error(message);
    }


    /*
     * Respuesta esperada:
     *
     * {
     *     "url": "/uploads/imagen.jpg"
     * }
     */

    const data =
        await response.json();


    if (!data.url) {

        throw new Error(
            "El servidor no devolvió la URL de la imagen."
        );
    }


    return data.url;
}


/*
 * ============================================================
 * GUARDAR ANUNCIO
 * ============================================================
 */

async function saveAdvertisement(event) {

    event.preventDefault();


    const id =
        document.getElementById(
            "advertisement-id"
        ).value;


    const file =
        document.getElementById(
            "advertisement-image"
        ).files[0];


    try {

        /*
         * Deshabilitamos el botón para evitar
         * doble envío.
         */

        const submitButton =
            document.querySelector(
                "#advertisement-form button[type='submit']"
            );


        if (submitButton) {

            submitButton.disabled = true;

            submitButton.textContent =
                "Guardando...";
        }


        /*
         * ====================================================
         * PASO 1: SUBIR IMAGEN
         * ====================================================
         */

        const imageUrl =
            await uploadImage(file);


        /*
         * Si estamos creando un anuncio,
         * la imagen es obligatoria.
         */

        if (!id && !imageUrl) {

            throw new Error(
                "Debés seleccionar una imagen."
            );
        }


        /*
         * ====================================================
         * PASO 2: CONSTRUIR ANUNCIO
         * ====================================================
         */

        const advertisement = {

            name:
                document.getElementById(
                    "advertisement-name"
                ).value.trim(),


            imageUrl:
                imageUrl,


            linkUrl:
                document.getElementById(
                    "advertisement-link"
                ).value.trim(),


            position:
                document.getElementById(
                    "advertisement-position"
                ).value,


            active:
                document.getElementById(
                    "advertisement-active"
                ).checked

        };


        /*
         * ====================================================
         * PASO 3: VALIDACIÓN
         * ====================================================
         */

        if (!advertisement.name) {

            throw new Error(
                "El nombre del anuncio es obligatorio."
            );
        }


        if (!advertisement.imageUrl) {

            throw new Error(
                "El anuncio necesita una imagen."
            );
        }


        if (!advertisement.linkUrl) {

            throw new Error(
                "La URL destino es obligatoria."
            );
        }


        if (!advertisement.position) {

            throw new Error(
                "Seleccioná una posición."
            );
        }


        /*
         * ====================================================
         * PASO 4: GUARDAR EN BACKEND
         * ====================================================
         */

        if (id) {

            await apiPut(
                `/advertisements/${id}`,
                advertisement
            );


            showMessage(
                "Anuncio actualizado correctamente.",
                "success"
            );

        } else {

            await apiPost(
                "/advertisements",
                advertisement
            );


            showMessage(
                "Anuncio creado correctamente.",
                "success"
            );
        }


        /*
         * ====================================================
         * PASO 5: LIMPIAR Y RECARGAR
         * ====================================================
         */

        closeAdvertisementForm();

        await loadAdvertisements();


    } catch (error) {

        console.error(
            "Error guardando anuncio:",
            error
        );


        showMessage(
            error.message,
            "error"
        );


    } finally {

        /*
         * Volver a habilitar botón.
         */

        const submitButton =
            document.querySelector(
                "#advertisement-form button[type='submit']"
            );


        if (submitButton) {

            submitButton.disabled = false;

            submitButton.textContent =
                "Guardar anuncio";
        }

    }
}


/*
 * ============================================================
 * EDITAR ANUNCIO
 * ============================================================
 */

function editAdvertisement(id) {

    const ad =
        advertisements.find(
            advertisement =>
                advertisement.id === id
        );


    if (!ad) {

        showMessage(
            "No se encontró el anuncio.",
            "error"
        );

        return;
    }


    openAdvertisementForm(ad);
}


/*
 * ============================================================
 * ELIMINAR ANUNCIO
 * ============================================================
 */

async function deleteAdvertisement(id) {

    const confirmed =
        confirm(
            "¿Seguro que querés eliminar este anuncio?"
        );


    if (!confirmed) {
        return;
    }


    try {

        await apiDelete(
            `/advertisements/${id}`
        );


        showMessage(
            "Anuncio eliminado correctamente.",
            "success"
        );


        await loadAdvertisements();


    } catch (error) {

        console.error(
            "Error eliminando anuncio:",
            error
        );


        showMessage(
            error.message,
            "error"
        );
    }
}


/*
 * ============================================================
 * MENSAJES
 * ============================================================
 */

function showMessage(text, type) {

    const message =
        document.getElementById(
            "advertisements-message"
        );


    message.textContent =
        text;


    message.className =
        `admin-message ${type}`;


    setTimeout(() => {

        message.textContent = "";

        message.className =
            "admin-message";

    }, 4000);
}


/*
 * ============================================================
 * ESCAPE HTML
 * ============================================================
 */

function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {

        return "";
    }


    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}