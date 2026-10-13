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

    if (post.categories && post.categories.length > 0) {
        const catEl = document.createElement("div");
        catEl.className = "post-detail__category";

        post.categories.forEach((cat) => {
            const catLink = document.createElement("a");
            catLink.href = `/categorias/${cat.slug}`;
            catLink.textContent = cat.name;
            catEl.appendChild(catLink);
        });

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

    if (post.imgUrl && isSafeUrl(post.imgUrl)) {
        const image = document.createElement("img");
        image.className = "post-detail__image";
        image.src = post.imgUrl;
        image.alt = post.title;
        container.appendChild(image);
    }

    const content = document.createElement("div");
    content.className = "post-detail__content";
    // El HTML ya se sanea en el servidor; DOMPurify es una segunda barrera en el navegador
    content.innerHTML = DOMPurify.sanitize(post.content || "");
    container.appendChild(content);

    if (post.downloadUrl && isSafeUrl(post.downloadUrl)) {
        const download = document.createElement("p");
        const link = document.createElement("a");

        link.className = "btn btn--primary";
        link.href = post.downloadUrl;
        link.target = "_blank";
        link.rel = "noopener noreferrer";
        link.download = "";
        link.textContent = post.downloadText || "Descargar";

        download.appendChild(link);
        container.appendChild(download);
    }
}

// Solo http(s) o rutas relativas al sitio; bloquea javascript:, data:, etc.
function isSafeUrl(value) {
    try {
        const url = new URL(value, window.location.origin);
        return url.protocol === "http:" || url.protocol === "https:";
    } catch {
        return false;
    }
}

async function loadRelated(currentPost) {
    const relatedSection = document.getElementById("relatedSection");
    const relatedGrid = document.getElementById("relatedNews");
    if (!relatedSection || !relatedGrid) return;

    try {
        // El servidor devuelve las 3 más cercanas (misma categoría, o las últimas si no hay)
        const related = await fetchJSON(`/posts/${encodeURIComponent(currentPost.slug)}/related?limit=3`);

        if (related.length > 0) {
            related.forEach((post) => {
                relatedGrid.appendChild(createNewsCard(post, { parts: ["category", "title", "date"] }));
            });
            relatedSection.style.display = "";
        }
    } catch (error) {
        console.error("No se pudieron cargar noticias relacionadas", error);
    }
}
document.addEventListener("DOMContentLoaded", () => {
    loadPost();
});
