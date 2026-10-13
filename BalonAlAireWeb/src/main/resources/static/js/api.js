// API pública de solo lectura (API_BASE y PUBLIC_API vienen de api-base.js)
async function fetchJSON(path) {
    const response = await fetch(`${PUBLIC_API}${path}`);
    if (!response.ok) {
        throw new Error(`Request to ${path} failed with status ${response.status}`);
    }
    return response.json();
}

// Varias partes de una misma página (portada, barra lateral) piden los mismos datos:
// comparten una única petición por ruta mientras la página esté abierta.
const pendingRequests = new Map();

function fetchJSONCached(path) {
    if (!pendingRequests.has(path)) {
        const request = fetchJSON(path);
        pendingRequests.set(path, request);
        request.catch(() => pendingRequests.delete(path));
    }
    return pendingRequests.get(path);
}
