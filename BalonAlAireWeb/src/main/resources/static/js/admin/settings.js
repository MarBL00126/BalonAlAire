let socialLinks = [];
let elementId="settings-message"

document.addEventListener("DOMContentLoaded", () => {

    loadSettings();
    loadSocialLinks();

    document
        .getElementById("settings-form")
        .addEventListener("submit", saveSettings);

    document
        .getElementById("logo-url")
        .addEventListener("input", previewLogo);
});


/* =====================================================
   CONFIGURACIÓN GENERAL
   ===================================================== */

async function loadSettings() {

    try {

        const settings = await apiGet("/settings");

        if (!settings) {
            return;
        }

        const settingsMap = {};
        settings.forEach(setting => {
            settingsMap[setting.settingsKey] = setting.settingsValue;
        });

        document.getElementById("site-name").value =
            settingsMap.site_name || "";

        document.getElementById("logo-url").value =
            settingsMap.logo_url || "";

        document.getElementById("footer-text").value =
            settingsMap.footer_text || "";

        previewLogo();

    } catch (error) {

        console.error(
            "Error cargando configuración:",
            error
        );

        showMessage(elementId,
            error.message,
            "error"
        );
    }
}


async function saveSettings(event) {

    event.preventDefault();

    const settings = {

        site_name:
            document.getElementById("site-name")
                .value
                .trim(),

        logo_url:
            document.getElementById("logo-url")
                .value
                .trim(),

        footer_text:
            document.getElementById("footer-text")
                .value
                .trim()
    };


    try {

        await apiPut(
            "/settings",
            settings
        );

        showMessage(elementId,
            "Configuración guardada correctamente.",
            "success"
        );

    } catch (error) {

        console.error(
            "Error guardando configuración:",
            error
        );

        showMessage(elementId,
            error.message,
            "error"
        );
    }
}


/* =====================================================
   PREVIEW DEL LOGO
   ===================================================== */

function previewLogo() {

    const url =
        document.getElementById("logo-url")
            .value
            .trim();

    const preview =
        document.getElementById("logo-preview");


    preview.innerHTML = "";


    if (!url) {
        return;
    }


    const image =
        document.createElement("img");

    image.src = url;

    image.alt = "Vista previa del logo";

    image.style.maxWidth = "300px";
    image.style.maxHeight = "120px";
    image.style.objectFit = "contain";


    image.onerror = () => {

        preview.innerHTML =
            "<p>No se pudo cargar la imagen.</p>";
    };


    preview.appendChild(image);
}


/* =====================================================
   REDES SOCIALES
   ===================================================== */

async function loadSocialLinks() {

    try {

        socialLinks =
            await apiGet("/social-links");

        renderSocialLinks(
            socialLinks
        );

    } catch (error) {

        console.error(
            "Error cargando redes sociales:",
            error
        );

        showMessage(
            "social-message",
            error.message,
            "error"
        );
    }
}


/* =====================================================
   RENDER
   ===================================================== */

function renderSocialLinks(links) {

    const container =
        document.getElementById(
            "social-links-container"
        );

    container.innerHTML = "";


    const networks = [
        "YouTube",
        "Instagram",
        "Twitter"
    ];


    networks.forEach(network => {

        const existing =
            links.find(
                link =>
                    link.platform &&
                    link.platform.toLowerCase() ===
                    network.toLowerCase()
            );


        const card =
            document.createElement("div");

        card.className =
            "admin-card social-config-card";


        card.innerHTML = `

            <div class="form-group">

                <label>
                    ${escapeHtml(network)}
                </label>

                <input
                    type="url"
                    id="social-url-${network.toLowerCase()}"
                    placeholder="https://..."
                    value="${
                        escapeHtml(
                            existing?.linkUrl || ""
                        )
                    }"
                >

            </div>


            <div class="form-group">

                <label>

                    <input
                        type="checkbox"
                        id="social-active-${network.toLowerCase()}"
                        ${
                            existing?.active
                                ? "checked"
                                : ""
                        }
                    >

                    Activo

                </label>

            </div>


            <div class="admin-actions">

                <button
                    type="button"
                    class="btn btn-primary"
                    onclick="saveSocialNetwork('${network}')">

                    Guardar

                </button>

            </div>

        `;


        container.appendChild(card);
    });
}


/* =====================================================
   GUARDAR RED SOCIAL
   ===================================================== */

async function saveSocialNetwork(network) {

    const key =
        network.toLowerCase();


    const url =
        document.getElementById(
            `social-url-${key}`
        )
        .value
        .trim();


    const active =
        document.getElementById(
            `social-active-${key}`
        )
        .checked;


    const existing =
        socialLinks.find(
            link =>
                link.platform &&
                link.platform.toLowerCase() ===
                key
        );


    const data = {

        platform: key,

        linkUrl: url,

        active: active
    };


    try {

        if (existing) {

            await apiPut(
                `/social-links/${existing.id}`,
                data
            );

        } else {

            await apiPost(
                "/social-links",
                data
            );
        }


        showMessage(
            "social-message",
            `${network} guardado correctamente.`,
            "success"
        );


        await loadSocialLinks();

    } catch (error) {

        console.error(
            `Error guardando ${network}:`,
            error
        );

        showMessage(
            "social-message",
            error.message,
            "error"
        );
    }
}

