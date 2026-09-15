document.addEventListener("DOMContentLoaded", async () => {
    const postsCount = document.getElementById("posts-count");
    const categoriesCount = document.getElementById("categories-count");
    const adsCount = document.getElementById("ads-count");

    try {
        const [posts, categories, ads] = await Promise.all([
            apiGet("/posts"),
            apiGet("/categories"),
            apiGet("/advertisements")
        ]);

        if (postsCount) {
            postsCount.textContent = posts.length;
        }

        if (categoriesCount) {
            categoriesCount.textContent = categories.length;
        }

        if (adsCount) {
            adsCount.textContent = ads.length;
        }

    } catch (error) {
        console.error("Error cargando dashboard:", error);
    }
});