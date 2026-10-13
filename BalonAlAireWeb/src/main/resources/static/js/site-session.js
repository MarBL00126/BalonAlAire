// Si hay sesión del panel, el botón "Admin" del encabezado muestra el nombre del usuario
async function showLoggedInUser() {
    const link = document.querySelector(".btn-admin");
    if (!link) {
        return;
    }

    try {
        const response = await fetch(`${PUBLIC_API}/session`, { credentials: "same-origin" });
        if (response.status !== 200) {
            return;
        }

        const user = await response.json();
        link.textContent = user.username;
        link.classList.add("btn-admin--user");
        link.title = "Ir al panel de administración";
        link.setAttribute("aria-label", `Panel de administración (${user.username})`);
    } catch (error) {
        console.warn("No se pudo consultar la sesión", error);
    }
}

document.addEventListener("DOMContentLoaded", showLoggedInUser);
