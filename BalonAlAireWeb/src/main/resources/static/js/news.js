function createNewsCard(post) {
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

    const category = document.createElement("p");
    category.className = "news-card__category";
    category.textContent = post.categories?.[0]?.name || "";
    body.appendChild(category);

    const meta = document.createElement("p");
    meta.className = "news-card__meta";
    const author=post.author || "";
    const date=post.publishedAt ? new Date(post.publishedAt).toLocaleDateString("es-AR"):""
    meta.textContent = [author,date].filter(Boolean).join(" · ")
    body.appendChild(meta);

    link.appendChild(body);
    card.appendChild(link);
    return card;
}

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

async function renderNews() {
    const featuredGrid = document.querySelector(".featured-grid");
    const newsGrid = document.querySelector(".news-grid");
    if (!featuredGrid && !newsGrid) {
        return;
    }

    const data = await fetchJSON("/posts");
    const posts = data.content ?? data;   // soporta Page<> y array plano
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

document.addEventListener("DOMContentLoaded", () => {
    renderNews().catch((error) => console.error("No se pudieron cargar las noticias", error));
});
