async function renderAds() {
    const slots = document.querySelectorAll("[data-ad-slot]");
    if (slots.length === 0) {
        return;
    }

    const ads = await fetchJSON("/advertisements");
    slots.forEach((slot) => {
        const position = slot.getAttribute("data-ad-slot");
        const candidates = ads
            .filter((item) => item.active && item.position === position)
            .sort((a, b) => (b.id || 0) - (a.id || 0));

        renderAdCandidates(slot, candidates);
    });
}

function renderAdCandidates(slot, candidates) {
    slot.innerHTML = "";
    slot.classList.remove("has-ad");

    candidates.forEach((ad) => {
        const image = document.createElement("img");
        image.alt = ad.name || "Publicidad";

        image.addEventListener("load", () => {
            const link = document.createElement("a");
            link.href = ad.linkUrl;
            link.target = "_blank";
            link.rel = "noopener noreferrer";

            link.appendChild(image);
            slot.appendChild(link);
            slot.classList.add("has-ad");
        });

        image.addEventListener("error", () => {
            console.warn("No se pudo cargar la publicidad", ad.imageUrl);
        });

        image.src = ad.imageUrl;
    });
}

document.addEventListener("DOMContentLoaded", () => {
    renderAds().catch((error) => console.error("No se pudieron cargar las publicidades", error));
});
