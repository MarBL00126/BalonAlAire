/* ============================================================
   site-settings.js — Aplica logo, nombre del sitio y texto del
   footer configurados desde /admin/configuracion.
   ============================================================ */

async function applySiteSettings() {
    let settings;
    try {
        settings = await fetchJSON("/settings");
    } catch (error) {
        console.error("No se pudo cargar la configuración del sitio", error);
        return;
    }

    const settingsMap = {};
    settings.forEach((setting) => {
        settingsMap[setting.settingsKey] = setting.settingsValue;
    });

    if (settingsMap.logo_url) {
        document.querySelectorAll(".site-logo img").forEach((img) => {
            img.src = settingsMap.logo_url;
        });
    }

    if (settingsMap.site_name) {
        document.querySelectorAll(".site-logo img").forEach((img) => {
            img.alt = settingsMap.site_name;
        });
    }

    if (settingsMap.footer_text) {
        document.querySelectorAll("[data-footer-text]").forEach((el) => {
            el.textContent = settingsMap.footer_text;
        });
    }
}

document.addEventListener("DOMContentLoaded", () => {
    applySiteSettings();
});
