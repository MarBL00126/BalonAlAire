let categories = [];

document.addEventListener("DOMContentLoaded", () => {
    loadCategories();

    document
        .getElementById("new-category-button")
        .addEventListener("click", () => {
            openCategoryForm();
        });

    document
        .getElementById("cancel-category-button")
        .addEventListener("click", () => {
            closeCategoryForm();
        });

    document
        .getElementById("category-form")
        .addEventListener("submit", saveCategory);
});


async function loadCategories() {
    try {
        categories = await apiGet("/categories");

        renderCategories(categories);

    } catch (error) {
        showMessage(error.message, "error");
    }
}


function renderCategories(categories) {
    const tbody = document.getElementById("categories-table-body");

    tbody.innerHTML = "";

    if (!categories || categories.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4">No hay categorías cargadas.</td>
            </tr>
        `;
        return;
    }

    categories.forEach(category => {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${escapeHtml(category.id)}</td>

            <td>
                <strong>${escapeHtml(category.name)}</strong>
            </td>

            <td>
                ${escapeHtml(category.slug)}
            </td>

            <td>
                <div class="admin-actions">
                    <button
                        class="btn btn-secondary btn-small"
                        onclick="editCategory(${category.id})">
                        Editar
                    </button>

                    <button
                        class="btn btn-danger btn-small"
                        onclick="deleteCategory(${category.id})">
                        Eliminar
                    </button>
                </div>
            </td>
        `;

        tbody.appendChild(row);
    });
}


function openCategoryForm(category = null) {
    const container =
        document.getElementById("category-form-container");

    const form =
        document.getElementById("category-form");

    container.hidden = false;
    container.style.display = "block";

    if (!category) {
        form.reset();
        document.getElementById("category-id").value = "";
        return;
    }

    document.getElementById("category-id").value = category.id;
    document.getElementById("category-name").value = category.name || "";
    document.getElementById("category-slug").value = category.slug || "";

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
}


function closeCategoryForm() {
    const container = document.getElementById(
        "category-form-container"
    );

    container.style.display = "none";
    container.hidden = true;
}


async function saveCategory(event) {
    event.preventDefault();

    const id = document.getElementById("category-id").value;

    const category = {
        name: document.getElementById("category-name").value.trim(),
        slug: document.getElementById("category-slug").value.trim()
    };

    try {
        if (id) {
            await apiPut(`/categories/${id}`, category);

            showMessage(
                "Categoría actualizada correctamente.",
                "success"
            );
        } else {
            await apiPost("/categories", category);

            showMessage(
                "Categoría creada correctamente.",
                "success"
            );
        }

        closeCategoryForm();
        await loadCategories();

    } catch (error) {
        showMessage(error.message, "error");
    }
}


function editCategory(id) {
    const category = categories.find(
        category => category.id === id
    );

    if (!category) {
        showMessage("No se encontró la categoría.", "error");
        return;
    }

    openCategoryForm(category);
}


async function deleteCategory(id) {
    const confirmed = confirm(
        "¿Seguro que querés eliminar esta categoría?"
    );

    if (!confirmed) {
        return;
    }

    try {
        await apiDelete(`/categories/${id}`);

        showMessage(
            "Categoría eliminada correctamente.",
            "success"
        );

        await loadCategories();

    } catch (error) {
        showMessage(error.message, "error");
    }
}


function showMessage(text, type) {
    const message = document.getElementById("categories-message");

    message.textContent = text;
    message.className = `admin-message ${type}`;

    setTimeout(() => {
        message.textContent = "";
        message.className = "admin-message";
    }, 4000);
}


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
