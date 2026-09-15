async function loadPost() {
    const container = document.getElementById("postDetail");
    const slug = new URLSearchParams(window.location.search).get("slug");

    if (!slug) {
        container.textContent = "No se especificó ninguna noticia.";
        return;
    }

    try {
        const post = await fetchJSON(`/posts/${encodeURIComponent(slug)}`);
        renderPost(post, container);
        await loadRelated(post);
    } catch (error) {
        console.error("No se pudo cargar la noticia", error);
        container.textContent = "No se encontró la noticia solicitada.";
    }
}

function renderPost(post, container) {
    document.title = `${post.title} - BALONALAIRE`;
    container.innerHTML = "";

    // Categoría badge
    if (post.categories && post.categories.length > 0) {
        const catEl = document.createElement("p");
        catEl.className = "post-detail__category";
        const cat = post.categories[0];
        const catLink = document.createElement("a");
        catLink.href = `/categorias/${cat.slug}`;
        catLink.textContent = cat.name;
        catEl.appendChild(catLink);
        container.appendChild(catEl);
    }

    const title = document.createElement("h1");
    title.textContent = post.title;
    container.appendChild(title);

    const meta = document.createElement("p");
    meta.className = "post-detail__meta";
    const publishedDate = post.publishedAt
        ? new Date(post.publishedAt).toLocaleDateString("es-AR", {
              day: "2-digit",
              month: "long",
              year: "numeric",
          })
        : "";
    meta.textContent = [post.author, publishedDate].filter(Boolean).join(" · ");
    container.appendChild(meta);

    if (post.imgUrl) {
        const image = document.createElement("img");
        image.className = "post-detail__image";
        image.src = post.imgUrl;
        image.alt = post.title;
        container.appendChild(image);
    }

    const content = document.createElement("div");
    content.className = "post-detail__content";
    // Use innerHTML to render HTML content; falls back to text if plain text stored
    content.innerHTML = post.content;
    container.appendChild(content);
}

async function loadRelated(currentPost) {
    const relatedSection = document.getElementById("relatedSection");
    const relatedGrid = document.getElementById("relatedNews");
    if (!relatedSection || !relatedGrid) return;

    try {
        let related = [];

        // Try fetching by first category
        if (currentPost.categories && currentPost.categories.length > 0) {
            const catSlug = currentPost.categories[0].slug;
            const catPosts = await fetchJSON(`/categories/${encodeURIComponent(catSlug)}/posts`);
            related = catPosts.filter((p) => p.slug !== currentPost.slug).slice(0, 3);
        }

        // Fallback: latest posts
        if (related.length === 0) {
            const allPosts = await fetchJSON("/posts");
            related = allPosts.filter((p) => p.slug !== currentPost.slug).slice(0, 3);
        }

        if (related.length > 0) {
            related.forEach((post) => {
                relatedGrid.appendChild(createRelatedCard(post));
            });
            relatedSection.style.display = "";
        }
    } catch (error) {
        console.error("No se pudieron cargar noticias relacionadas", error);
    }
}

function createRelatedCard(post) {
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

    const category = document.createElement("p");
    category.className = "news-card__category";
    category.textContent = post.categories?.[0]?.name || "";
    body.appendChild(category);

    const title = document.createElement("h3");
    title.className = "news-card__title";
    title.textContent = post.title;
    body.appendChild(title);

    const meta = document.createElement("p");
    meta.className = "news-card__meta";
    const date = post.publishedAt
        ? new Date(post.publishedAt).toLocaleDateString("es-AR")
        : "";
    meta.textContent = date;
    body.appendChild(meta);

    link.appendChild(body);
    card.appendChild(link);
    return card;
}

document.addEventListener("DOMContentLoaded", () => {
    loadPost();
});
