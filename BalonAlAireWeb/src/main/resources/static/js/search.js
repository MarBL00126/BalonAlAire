async function runSearch(query) {
    const resultsGrid = document.getElementById("searchResults");
    const status = document.getElementById("searchStatus");
    resultsGrid.innerHTML = "";

    if (!query) {
        status.textContent = "Escribí una palabra clave para buscar noticias.";
        return;
    }

    // El filtrado lo hace el servidor (búsqueda de texto completo)
    const data = await fetchJSON(`/posts?search=${encodeURIComponent(query)}`);
    const matches = data.content;

    status.textContent = matches.length
        ? `${matches.length} resultado(s) para "${query}"`
        : `No se encontraron noticias para "${query}"`;

    matches.forEach((post) => resultsGrid.appendChild(createNewsCard(post, { parts: ["title", "excerpt"] })));
}

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("searchForm");
    const input = document.getElementById("searchInput");
    const initialQuery = new URLSearchParams(window.location.search).get("q") || "";

    input.value = initialQuery;
    runSearch(initialQuery).catch((error) => console.error("Error al buscar noticias", error));

    form.addEventListener("submit", (event) => {
        event.preventDefault();
        const query = input.value.trim();

        const url = new URL(window.location.href);
        url.searchParams.set("q", query);
        window.history.replaceState({}, "", url);

        runSearch(query).catch((error) => console.error("Error al buscar noticias", error));
    });
});
