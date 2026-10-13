document.addEventListener("DOMContentLoaded", () => {
    const message = document.getElementById("login-message");

    const params = new URLSearchParams(window.location.search);

    if (params.get("error") === "true") {
        message.textContent = "Usuario o contraseña incorrectos.";
        message.className = "admin-message error";
    }

    if (params.get("locked") === "true") {
        message.textContent = "Demasiados intentos fallidos. Esperá unos minutos antes de volver a intentar.";
        message.className = "admin-message error";
    }

    if (params.get("logout") === "true") {
        message.textContent = "Sesión cerrada correctamente.";
        message.className = "admin-message success";
    }
});