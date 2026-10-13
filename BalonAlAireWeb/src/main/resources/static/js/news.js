function createFeaturedCard(post, isSecondary) {
    const card = document.createElement("article");
    card.className = isSecondary ? "featured-card featured-card--secondary" : "featured-card";

    const link = document.createElement("a");
    link.href = `/noticia?slug=${encodeURIComponent(post.slug)}`;

    const image = document.createElement("img");
    image.src = post.imgUrl || "/assets/images/placeholder.jpg";
    image.alt = post.title;
    link.appendChild(image);

    const overlay = document.createElement("div");
    overlay.className = "featured-card__overlay";

    const title = document.createElement("h2");
    title.className = "featured-card__title";
    title.textContent = post.title;
    overlay.appendChild(title);

    link.appendChild(overlay);
    card.appendChild(link);
    return card;
}

// Portada: no se ejecuta sola, la página la inicia explícitamente (ver home.js)
async function initNews() {
    const featuredGrid = document.querySelector(".featured-grid");
    const newsGrid = document.querySelector(".news-grid");
    if (!featuredGrid && !newsGrid) {
        return;
    }

    const data = await fetchJSONCached("/posts");
    const posts = data.content;
    const featuredCount = 3;

    if (featuredGrid) {
        posts.slice(0, featuredCount).forEach((post, index) => {
            featuredGrid.appendChild(createFeaturedCard(post, index > 0));
        });
    }

    if (newsGrid) {
        posts.slice(featuredCount).forEach((post) => {
            newsGrid.appendChild(createNewsCard(post));
        });
    }
}
