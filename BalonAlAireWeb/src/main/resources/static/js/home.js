document.addEventListener("DOMContentLoaded", () => {
    initNews().catch((error) => console.error("No se pudieron cargar las noticias", error));
});
