// Tarjeta de noticia compartida por portada, búsqueda, categorías y noticias relacionadas.
// `parts` define qué se muestra y en qué orden: title, excerpt, category, meta (autor · fecha) y date (solo fecha).

const NEWS_CARD_DEFAULT_PARTS = ["title", "excerpt", "category", "meta"];

function formatNewsDate(post) {
    return post.publishedAt ? new Date(post.publishedAt).toLocaleDateString("es-AR") : "";
}

function createNewsCardPart(post, part, headingTag) {
    const element = document.createElement(part === "title" ? headingTag : "p");

    switch (part) {
        case "title":
            element.className = "news-card__title";
            element.textContent = post.title;
            break;
        case "excerpt":
            element.className = "news-card__excerpt";
            element.textContent = post.excerpt || "";
            break;
        case "category":
            element.className = "news-card__category";
            element.textContent = post.categories?.[0]?.name || "";
            break;
        case "meta":
            element.className = "news-card__meta";
            element.textContent = [post.author, formatNewsDate(post)].filter(Boolean).join(" · ");
            break;
        case "date":
            element.className = "news-card__meta";
            element.textContent = formatNewsDate(post);
            break;
        default:
            return null;
    }
    return element;
}

function createNewsCard(post, { parts = NEWS_CARD_DEFAULT_PARTS, headingTag = "h3" } = {}) {
    const card = document.createElement("article");
    card.className = "news-card";

    const link = document.createElement("a");
    link.href = `/noticia?slug=${encodeURIComponent(post.slug)}`;

    const image = document.createElement("img");
    image.className = "news-card__image";
    image.src = post.imgUrl || "/assets/images/placeholder.jpg";
    image.alt = post.title;
    // Las tarjetas suelen estar fuera de pantalla: la imagen se descarga cuando se acerca al viewport
    image.loading = "lazy";
    image.decoding = "async";
    link.appendChild(image);

    const body = document.createElement("div");
    body.className = "news-card__body";
    parts.forEach((part) => {
        const element = createNewsCardPart(post, part, headingTag);
        if (element) {
            body.appendChild(element);
        }
    });

    link.appendChild(body);
    card.appendChild(link);
    return card;
}
