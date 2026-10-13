const CATEGORY_PAGE_SIZE = 12;

function getCategorySlug() {
    const path = window.location.pathname;
    const parts = path.split("/").filter(Boolean);
    return parts[parts.length - 1];
}

function createLoadMoreButton(container, onClick) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "btn btn--primary";
    button.textContent = "Cargar más noticias";
    button.addEventListener("click", onClick);

    const wrapper = document.createElement("p");
    wrapper.className = "category-load-more";
    wrapper.appendChild(button);
    container.after(wrapper);
    return { button, wrapper };
}

async function renderCategory() {
    const slug = getCategorySlug();
    const titleElement = document.querySelector("#categoryTitle");
    const newsContainer = document.querySelector("#categoryNews");
    if (!slug || !newsContainer) {
        return;
    }

    try {
        const category = await fetchJSON(`/categories/${encodeURIComponent(slug)}`);
        if (category && titleElement) {
            titleElement.textContent = category.name;
            document.title = `${category.name} - BALONALAIRE`;
        }

        // Clear loading message
        newsContainer.innerHTML = "";

        let page = 0;
        let loadMore = null;

        // Las noticias se piden de a páginas: la categoría puede crecer sin que la carga se encarezca
        const loadPage = async () => {
            const data = await fetchJSON(
                `/categories/${encodeURIComponent(slug)}/posts?page=${page}&size=${CATEGORY_PAGE_SIZE}`
            );

            if (page === 0 && data.content.length === 0) {
                newsContainer.innerHTML = "<p>No hay noticias en esta categoría aún.</p>";
                return;
            }

            data.content.forEach((post) => {
                newsContainer.appendChild(createNewsCard(post, {
                    parts: ["category", "title", "excerpt", "meta"],
                    headingTag: "h2"
                }));
            });
            page += 1;

            if (data.last) {
                loadMore?.wrapper.remove();
            } else if (!loadMore) {
                loadMore = createLoadMoreButton(newsContainer, () => {
                    loadMore.button.disabled = true;
                    loadPage()
                        .catch((error) => console.error("No se pudieron cargar más noticias", error))
                        .finally(() => { loadMore.button.disabled = false; });
                });
            }
        };

        await loadPage();
    } catch (error) {
        console.error("No se pudo cargar la categoría", error);
        newsContainer.innerHTML = "<p>No se pudo cargar las noticias. Intentá más tarde.</p>";
    }
}

document.addEventListener("DOMContentLoaded", () => {
    renderCategory().catch((error) => {
        console.error("No se pudo cargar la categoría", error);
    });
});
