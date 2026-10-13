document.addEventListener("DOMContentLoaded", async () => {
    // Cada contador se carga por separado: un editor no tiene acceso a publicidad y no debe romper el resto
    const counters = [
        { id: "posts-count", path: "/admin/posts?size=1" },
        { id: "categories-count", path: "/admin/categories" },
        { id: "ads-count", path: "/admin/advertisements?size=1" }
    ];

    await Promise.all(counters.map(async ({ id, path }) => {
        const element = document.getElementById(id);
        if (!element) {
            return;
        }

        try {
            const data = await apiGet(path);
            element.textContent = data.totalElements ?? data.length;
        } catch (error) {
            element.textContent = "-";
            console.warn(`No se pudo cargar ${path}`, error);
        }
    }));
});