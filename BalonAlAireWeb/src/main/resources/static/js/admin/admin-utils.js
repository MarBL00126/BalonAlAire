function escapeHtml(value) {
    if (value === null || value === undefined) {
        return "";
    }

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
function showMessage(elementId,text, type) {
    const message = document.getElementById(elementId);

    message.textContent = text;
    message.className = `admin-message ${type}`;

    setTimeout(() => {
        message.textContent = "";
        message.className = "admin-message";
    }, 4000);
}