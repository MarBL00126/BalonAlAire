// kept independent from news.js so this page doesn't trigger its auto-render on .news-grid
function createSearchResultCard(post) {
    const card = document.createElement("article");
    card.className = "news-card";

    const link = document.createElement("a");
    link.href = `/noticia?slug=${encodeURIComponent(post.slug)}`;

    const image = document.createElement("img");
    image.className = "news-card__image";
    image.src = post.imgUrl || "/assets/images/placeholder.jpg";
    image.alt = post.title;
    link.appendChild(image);

    const body = document.createElement("div");
    body.className = "news-card__body";

    const title = document.createElement("h3");
    title.className = "news-card__title";
    title.textContent = post.title;
    body.appendChild(title);

    const excerpt = document.createElement("p");
    excerpt.className = "news-card__excerpt";
    excerpt.textContent = post.excerpt || "";
    body.appendChild(excerpt);

    link.appendChild(body);
    card.appendChild(link);
    return card;
}

function matchesQuery(post, query) {
    const haystack = `${post.title} ${post.excerpt || ""}`.toLowerCase();
    return haystack.includes(query.toLowerCase());
}

async function runSearch(query) {
    const resultsGrid = document.getElementById("searchResults");
    const status = document.getElementById("searchStatus");
    resultsGrid.innerHTML = "";

    if (!query) {
        status.textContent = "Escribí una palabra clave para buscar noticias.";
        return;
    }

    const posts = await fetchJSON(`/posts?search=${encodeURIComponent(query)}`);
    const matches = posts.filter((post) => matchesQuery(post, query));

    status.textContent = matches.length
        ? `${matches.length} resultado(s) para "${query}"`
        : `No se encontraron noticias para "${query}"`;

    matches.forEach((post) => resultsGrid.appendChild(createSearchResultCard(post)));
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
