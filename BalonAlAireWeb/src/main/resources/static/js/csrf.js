// Protección CSRF: Spring Security deja el token en la cookie XSRF-TOKEN.
// fetch lo envía en el header X-XSRF-TOKEN y los formularios nativos en el campo _csrf.

function getCsrfToken() {
    const cookie = document.cookie
        .split("; ")
        .find((row) => row.startsWith("XSRF-TOKEN="));

    return cookie ? decodeURIComponent(cookie.substring("XSRF-TOKEN=".length)) : null;
}

function csrfHeaders(headers = {}) {
    const token = getCsrfToken();
    return token ? { ...headers, "X-XSRF-TOKEN": token } : { ...headers };
}

document.addEventListener("submit", (event) => {
    const form = event.target;

    if (!(form instanceof HTMLFormElement)
        || form.method.toLowerCase() !== "post"
        || !form.getAttribute("action")) {
        return;
    }

    const token = getCsrfToken();
    if (!token) {
        return;
    }

    let input = form.querySelector('input[name="_csrf"]');
    if (!input) {
        input = document.createElement("input");
        input.type = "hidden";
        input.name = "_csrf";
        form.appendChild(input);
    }
    input.value = token;
}, true);
