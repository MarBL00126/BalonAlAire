// Subida de archivos del panel.
// Primero intenta subir directo al storage con una URL prefirmada (no ocupa un hilo del servidor);
// si el storage no lo soporta o la subida directa falla (por ejemplo, CORS del bucket sin configurar),
// sube a través del servidor.

class UploadRejectedError extends Error {}

async function uploadFile(file) {
    if (!file) {
        return null;
    }

    try {
        const publicUrl = await uploadDirectToStorage(file);
        if (publicUrl) {
            return publicUrl;
        }
    } catch (error) {
        if (error instanceof UploadRejectedError) {
            throw error;
        }
        console.warn("La subida directa falló; se usa la subida por el servidor.", error);
    }

    return uploadThroughServer(file);
}

async function uploadDirectToStorage(file) {
    const response = await fetch("/api/admin/uploads/presign", {
        method: "POST",
        headers: csrfHeaders({ "Content-Type": "application/json" }),
        credentials: "same-origin",
        body: JSON.stringify({ filename: file.name, contentType: file.type, size: file.size })
    });

    if (response.status === 401) {
        window.location.href = "/admin/login";
        return null;
    }

    // 501: el storage es local y no admite subida directa
    if (response.status === 501) {
        return null;
    }

    // El servidor rechazó el archivo (tipo, tamaño): no tiene sentido reintentar por otra vía
    if (response.status === 400 || response.status === 413) {
        throw new UploadRejectedError(await readUploadError(response, "El archivo no es válido."));
    }

    if (!response.ok) {
        throw new Error(`No se pudo preparar la subida (${response.status})`);
    }

    const presigned = await response.json();
    const upload = await fetch(presigned.uploadUrl, {
        method: "PUT",
        headers: presigned.headers,
        body: file
    });

    if (!upload.ok) {
        throw new Error(`El storage rechazó la subida (${upload.status})`);
    }

    return presigned.publicUrl;
}

async function uploadThroughServer(file) {
    const formData = new FormData();
    formData.append("file", file);

    const response = await fetch("/api/admin/uploads", {
        method: "POST",
        headers: csrfHeaders(),
        credentials: "same-origin",
        body: formData
    });

    if (response.status === 401) {
        window.location.href = "/admin/login";
        return null;
    }

    if (!response.ok) {
        throw new Error(await readUploadError(response, `Error subiendo el archivo (${response.status})`));
    }

    const data = await response.json();
    if (!data.url) {
        throw new Error("El servidor no devolvió la URL del archivo.");
    }
    return data.url;
}

async function readUploadError(response, fallback) {
    try {
        const errorData = await response.json();
        if (errorData.details && typeof errorData.details === "object") {
            return Object.values(errorData.details).join(". ");
        }
        return errorData.message || errorData.error || fallback;
    } catch {
        return fallback;
    }
}
