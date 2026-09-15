/* ============================================================
   header.js — Hamburguesa animada + overlay + búsqueda mobile
   ============================================================ */

(function () {
    "use strict";

    /* ── Elementos ── */
    const menuToggle = document.getElementById("menuToggle");
    const mainMenu   = document.getElementById("mainMenu");
    const overlay    = document.getElementById("menuOverlay");
    const searchBtn  = document.getElementById("searchButton");

    /* ─────────────────────────────────────────────
       HAMBURGUESA
       ──────────────────────────────────────────── */
    function openMenu() {
        mainMenu.classList.add("is-open");
        overlay.classList.add("active");
        menuToggle.setAttribute("aria-expanded", "true");
        menuToggle.setAttribute("aria-label", "Cerrar menú");
        document.body.style.overflow = "hidden"; // bloquea scroll en mobile
    }

    function closeMenu() {
        mainMenu.classList.remove("is-open");
        overlay.classList.remove("active");
        menuToggle.setAttribute("aria-expanded", "false");
        menuToggle.setAttribute("aria-label", "Abrir menú");
        document.body.style.overflow = "";
    }

    function toggleMenu() {
        const isOpen = mainMenu.classList.contains("is-open");
        isOpen ? closeMenu() : openMenu();
    }

    if (menuToggle && mainMenu) {
        menuToggle.addEventListener("click", toggleMenu);

        // Cerrar al hacer click en un link del menú
        mainMenu.querySelectorAll("a").forEach((link) => {
            link.addEventListener("click", closeMenu);
        });
    }

    // Cerrar al hacer click en el overlay oscuro
    if (overlay) {
        overlay.addEventListener("click", closeMenu);
    }

    // Cerrar con Escape
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && mainMenu && mainMenu.classList.contains("is-open")) {
            closeMenu();
            menuToggle.focus(); // devolver el foco al botón
        }
    });

    // En resize a desktop: limpiar estado mobile
    window.addEventListener("resize", () => {
        if (window.innerWidth >= 1024) {
            closeMenu();
        }
    });

    /* ─────────────────────────────────────────────
       BÚSQUEDA: expandible en mobile, redirige en desktop
       ──────────────────────────────────────────── */

    // Crear panel de búsqueda dinámicamente si no existe
    let searchPanel = document.getElementById("searchPanel");

    if (!searchPanel && searchBtn) {
        searchPanel = document.createElement("div");
        searchPanel.id = "searchPanel";
        searchPanel.className = "search-overlay";
        searchPanel.setAttribute("aria-hidden", "true");
        searchPanel.innerHTML = `
            <form class="search-form" id="headerSearchForm">
                <input
                    type="search"
                    id="headerSearchInput"
                    name="q"
                    placeholder="Buscar noticias..."
                    aria-label="Buscar noticias"
                    autocomplete="off"
                >
                <button type="submit">Buscar</button>
            </form>
        `;
        // Insertar justo después del header-container
        const header = document.querySelector(".site-header");
        if (header) header.appendChild(searchPanel);
    }

    function openSearch() {
        if (!searchPanel) return;
        searchPanel.classList.add("active");
        searchPanel.setAttribute("aria-hidden", "false");
        const input = searchPanel.querySelector("input");
        if (input) input.focus();
    }

    function closeSearch() {
        if (!searchPanel) return;
        searchPanel.classList.remove("active");
        searchPanel.setAttribute("aria-hidden", "true");
    }

    if (searchBtn) {
        searchBtn.addEventListener("click", () => {
            // En mobile (<1024): abrir panel expandible
            if (window.innerWidth < 1024) {
                // Cerrar menú si estuviera abierto
                closeMenu();
                const isSearchOpen = searchPanel && searchPanel.classList.contains("active");
                isSearchOpen ? closeSearch() : openSearch();
            } else {
                // En desktop: ir a /buscar directamente
                window.location.href = "/buscar";
            }
        });
    }

    // Cerrar panel de búsqueda con Escape
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && searchPanel && searchPanel.classList.contains("active")) {
            closeSearch();
            if (searchBtn) searchBtn.focus();
        }
    });

    // Submit del panel de búsqueda del header
    if (searchPanel) {
        searchPanel.addEventListener("submit", (e) => {
            if (e.target.id === "headerSearchForm") {
                e.preventDefault();
                const input = document.getElementById("headerSearchInput");
                const query = input ? input.value.trim() : "";
                if (query) {
                    window.location.href = `/buscar?q=${encodeURIComponent(query)}`;
                }
            }
        });
    }

    /* ─────────────────────────────────────────────
       SIDEBAR SEARCH (en páginas con sidebar)
       ──────────────────────────────────────────── */
    const sidebarSearchForm = document.getElementById("sidebarSearchForm");
    if (sidebarSearchForm) {
        sidebarSearchForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const input = document.getElementById("sidebarSearchInput");
            const query = input ? input.value.trim() : "";
            if (query) {
                window.location.href = `/buscar?q=${encodeURIComponent(query)}`;
            }
        });
    }

})();
