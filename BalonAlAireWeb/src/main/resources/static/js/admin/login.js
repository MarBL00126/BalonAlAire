document.addEventListener("DOMContentLoaded", () => {
    const message = document.getElementById("login-message");

    const params = new URLSearchParams(window.location.search);

    if (params.get("error") === "true") {
        message.textContent = "Usuario o contraseña incorrectos.";
        message.className = "admin-message error";
    }

    if (params.get("logout") === "true") {
        message.textContent = "Sesión cerrada correctamente.";
        message.className = "admin-message success";
    }
});