const SOCIAL_ICONS = {
    youtube: "/assets/images/logoYoutube.png",
    instagram: "/assets/images/logoInstagram.png",
    twitter: "/assets/images/logoX.png",
};

async function renderCategories() {
    const list = document.querySelector("[data-categories-list]");
    if (!list) {
        return;
    }

    const categories = await fetchJSON("/categories");
    categories.forEach((category) => {
        const item = document.createElement("li");
        const link = document.createElement("a");
        link.href = `/categorias/${category.slug}`;
        link.textContent = category.name;
        item.appendChild(link);
        list.appendChild(item);
    });
}

async function renderLatestNews() {
    const lists = document.querySelectorAll("[data-latest-list]");
    if (lists.length === 0) {
        return;
    }

    const posts = await fetchJSON("/posts");
    const latest = posts.slice(0, 5);

    lists.forEach((list) => {
        latest.forEach((post) => {
            const item = document.createElement("li");
            const link = document.createElement("a");
            link.href = `/noticia?slug=${encodeURIComponent(post.slug)}`;
            link.textContent = post.title;
            item.appendChild(link);
            list.appendChild(item);
        });
    });
}

async function renderSocialLinks() {
    const lists = document.querySelectorAll("[data-social-links]");
    if (lists.length === 0) {
        return;
    }

    const socialLinks = await fetchJSON("/social-links");
    const activeLinks = socialLinks.filter((social) => social.active);

    lists.forEach((list) => {
        activeLinks.forEach((social) => {
            const link = document.createElement("a");
            link.href = social.linkUrl;
            link.target = "_blank";
            link.rel = "noopener noreferrer";
            const icon=SOCIAL_ICONS[social.platform];
            if (icon){
                const img=document.createElement("img");
                img.src=icon;
                img.alt=social.platform;
                img.classList.add("social-icon");
                link.appendChild(img);
            }else{
                link.textContent=social.platform;
            }
            list.appendChild(link);
        });
    });
}

document.addEventListener("DOMContentLoaded", () => {
    renderCategories().catch((error) => console.error("No se pudieron cargar las categorías", error));
    renderLatestNews().catch((error) => console.error("No se pudieron cargar las últimas noticias", error));
    renderSocialLinks().catch((error) => console.error("No se pudieron cargar las redes sociales", error));
});
