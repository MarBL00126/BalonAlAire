const API_BASE = "/api";

async function apiFetch(url, options = {}) {
    const response = await fetch(`${API_BASE}${url}`, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });

    if (response.status === 401 || response.status === 403) {
        window.location.href = "/admin/login";
        return;
    }

    if (!response.ok) {
        let message = `Error HTTP ${response.status}`;

        try {
            const errorData = await response.json();

            if (errorData.message) {
                message = errorData.message;
            } else if (errorData.error) {
                message = errorData.error;
            }
        } catch {
            // La respuesta no era JSON
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