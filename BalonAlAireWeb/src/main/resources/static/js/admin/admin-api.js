async function apiFetch(url, options = {}) {
    const method = (options.method || "GET").toUpperCase();
    const headers = new Headers(options.headers || {});
    if (options.body != null && !headers.has("Content-Type")) {
        headers.set("Content-Type", "application/json");
    }
    const safeMethods = ["GET", "HEAD", "OPTIONS", "TRACE"];
    if (!safeMethods.includes(method)) {
        Object.entries(csrfHeaders()).forEach(([name, value]) => headers.set(name, value));
    }
    const response = await fetch(`${API_BASE}${url}`, {
        ...options,
        method,
        headers,
        credentials: "same-origin"
    });
    if (response.status === 401) {
        window.location.href = "/admin/login";
        return;
    }
    if (!response.ok) {
        let message = response.status === 403
            ? "No tenés permiso para esta acción o venció la sesión de seguridad. Recargá la página e intentá de nuevo."
            : `Error HTTP ${response.status}`;
        try {
            const errorData = await response.json();
            if (errorData.details && typeof errorData.details === "object") {
                message = Object.values(errorData.details).join(". ");
            } else if (errorData.message) {
                message = errorData.message;
            } else if (errorData.error) {
                message = errorData.error;
            }
        } catch {
            // La respuesta no era JSON.
        }
        throw new Error(message);
    }
    if (response.status === 204) {
        return null;
    }
    return response.json();
}
async function apiGet(url) {
    return apiFetch(url, {
        method: "GET"
    });
}

async function apiPost(url, data) {
    return apiFetch(url, {
        method: "POST",
        body: JSON.stringify(data)
    });
}

async function apiPut(url, data) {
    return apiFetch(url, {
        method: "PUT",
        body: JSON.stringify(data)
    });
}

async function apiDelete(url) {
    return apiFetch(url, {
        method: "DELETE"
    });
}

// Los editores solo usan noticias: se ocultan las secciones que su rol no puede abrir
const ADMIN_ONLY_LINKS = ["/admin/categorias", "/admin/publicidad", "/admin/configuracion", "/admin/register"];

async function applyRoleVisibility() {
    try {
        const me = await apiGet("/admin/me");
        if (me && me.role !== "ADMIN") {
            ADMIN_ONLY_LINKS.forEach((href) => {
                document.querySelectorAll(`a[href="${href}"]`).forEach((link) => {
                    (link.closest("li") || link).hidden = true;
                });
            });
        }
    } catch (error) {
        console.warn("No se pudo cargar el usuario actual", error);
    }
}

document.addEventListener("DOMContentLoaded", applyRoleVisibility);