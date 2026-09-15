function getCategorySlug(){
    const path=window.location.pathname;
    const parts=path.split("/").filter(Boolean);
    return parts[parts.length-1];
}
function createCategoryCard(post){
    const card=document.createElement("article");
    card.className="news-card";
    
    const link=document.createElement("a");
    link.href= `/noticia?slug=${encodeURIComponent(post.slug)}`;

    const image=document.createElement("img");
    image.className="news-card__image";
    image.src=post.imgUrl ||  "/assets/images/placeholder.jpg";
    image.alt=post.title

    link.appendChild(image)

    const body=document.createElement("div");
    body.className= "news-card__body";

    const category=document.createElement("p");
    category.className= "news-card__category";
    category.textContent=post.categories?.[0]?.name || "";
    
    body.appendChild(category);

    const title = document.createElement("h2");
    title.className = "news-card__title";
    title.textContent = post.title;

    body.appendChild(title);

    const excerpt = document.createElement("p");
    excerpt.className = "news-card__excerpt";
    excerpt.textContent = post.excerpt || "";

    body.appendChild(excerpt);

    const meta = document.createElement("p");
    meta.className = "news-card__meta";

    const author = post.author || "";
    
    const date = post.publishedAt
        ? new Date(post.publishedAt).toLocaleDateString("es-AR")
        : "";
    meta.textContent=[author,date].filter(Boolean).join(" · ");

    body.appendChild(meta);
    link.appendChild(body);
    card.appendChild(link);
    return card;
}
async function renderCategory(){
    const slug=getCategorySlug();
    const titleElement = document.querySelector("#categoryTitle");
    const newsContainer = document.querySelector("#categoryNews");
    if (!slug || !newsContainer){
        return;
    }
    try {
        const category=await fetchJSON(`/categories/${encodeURIComponent(slug)}`);
        const posts = await fetchJSON(`/categories/${encodeURIComponent(slug)}/posts`);

        if (category && titleElement){
            titleElement.textContent=category.name;
            document.title = `${category.name} - BALONALAIRE`;
        }

        // Clear loading message
        newsContainer.innerHTML = "";

        if (posts.length === 0) {
            newsContainer.innerHTML = "<p>No hay noticias en esta categoría aún.</p>";
            return;
        }

        posts.forEach((post)=> {
            newsContainer.appendChild(createCategoryCard(post));
        });
    } catch (error) {
        console.error("No se pudo cargar la categoría", error);
        newsContainer.innerHTML = "<p>No se pudo cargar las noticias. Intentá más tarde.</p>";
    }
}
document.addEventListener("DOMContentLoaded", () => {
    renderCategory().catch((error) => {
        console.error(
            "No se pudo cargar la categoría",
            error
        );
    });
});