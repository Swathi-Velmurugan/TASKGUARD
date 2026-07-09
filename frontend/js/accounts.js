// ============================================
// TaskGuard - accounts.js
// ============================================

checkLogin();
renderShell("accounts");

let accountModal;

document.getElementById("addBtn").addEventListener("click", openCreateForm);
document.getElementById("accountForm").addEventListener("submit", saveAccount);
document.getElementById("roleFilter").addEventListener("change", loadAccounts);

window.addEventListener("DOMContentLoaded", function () {
    accountModal = new bootstrap.Modal(document.getElementById("accountModal"));
    loadAccounts();
});

async function loadAccounts() {
    let role = document.getElementById("roleFilter").value;
    let params = {};
    if (role !== "ALL") params.role = role;

    try {
        let response = await axios.get(ACCOUNTS_URL, { headers: getHeaders(), params: params });
        renderTable(response.data);
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load accounts."));
    }
}

function renderTable(accounts) {
    let tbody = document.getElementById("accountsTableBody");
    let emptyState = document.getElementById("emptyState");

    if (!accounts || accounts.length === 0) {
        tbody.innerHTML = "";
        emptyState.style.display = "block";
        return;
    }
    emptyState.style.display = "none";

    let rows = "";
    accounts.forEach(item => {
        let isActive = item.active !== false;
        let toggleLabel = isActive
            ? '<i class="bi bi-lock"></i> Suspend'
            : '<i class="bi bi-unlock"></i> Re-grant';
        let toggleClass = isActive ? "btn-outline-danger" : "btn-outline-success";

        rows += `
            <tr>
                <td class="code-tag">ACC-${item.id}</td>
                <td>${item.fullName}</td>
                <td>${item.email}</td>
                <td><span class="badge badge-slate-dark">${item.domainRole}</span></td>
                <td><span class="badge ${isActive ? "badge-green" : "badge-red"}">${isActive ? "ACTIVE" : "SUSPENDED"}</span></td>
                <td>
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editAccount(${item.id})">
                        <i class="bi bi-pencil-square"></i> Edit
                    </button>
                    <button class="btn btn-sm ${toggleClass} me-1" onclick="toggleStatus(${item.id})">
                        ${toggleLabel}
                    </button>
                    <button class="btn btn-sm btn-outline-secondary" onclick="deleteAccount(${item.id})">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </td>
            </tr>`;
    });
    tbody.innerHTML = rows;
}

function resetForm() {
    document.getElementById("accountForm").reset();
    document.getElementById("accountId").value = "";
    document.getElementById("email").disabled = false;
    document.getElementById("passwordField").style.display = "block";
    document.querySelectorAll("#accountForm .field-error").forEach(e => e.textContent = "");
}

function openCreateForm() {
    resetForm();
    document.getElementById("modalTitle").textContent = "Create New Account";
    accountModal.show();
}

async function editAccount(id) {
    try {
        let response = await axios.get(ACCOUNTS_URL + "/" + id, { headers: getHeaders() });
        let item = response.data;
        resetForm();
        document.getElementById("modalTitle").textContent = "Edit Account";
        document.getElementById("accountId").value = item.id;
        document.getElementById("fullName").value = item.fullName;
        document.getElementById("email").value = item.email;
        document.getElementById("email").disabled = true;
        document.getElementById("domainRole").value = item.domainRole;
        document.getElementById("passwordField").style.display = "none";
        accountModal.show();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load account."));
    }
}

async function saveAccount(event) {
    event.preventDefault();
    document.querySelectorAll("#accountForm .field-error").forEach(e => e.textContent = "");

    let id = document.getElementById("accountId").value;
    let fullName = document.getElementById("fullName").value.trim();
    let email = document.getElementById("email").value.trim();
    let password = document.getElementById("password").value;
    let domainRole = document.getElementById("domainRole").value;

    let hasError = false;
    if (isBlank(fullName)) {
        document.getElementById("fullNameError").textContent = "Full name is required";
        hasError = true;
    }
    if (!id) {
        if (isBlank(email) || !isValidEmail(email)) {
            document.getElementById("emailError").textContent = "Enter a valid email address";
            hasError = true;
        }
        if (isBlank(password) || password.length < 6) {
            document.getElementById("passwordError").textContent = "Password must be at least 6 characters";
            hasError = true;
        }
    }
    if (hasError) return;

    try {
        startLoading("saveBtn", "Saving...");
        if (id) {
            let payload = { fullName: fullName, domainRole: domainRole };
            await axios.put(ACCOUNTS_URL + "/" + id, payload, { headers: getHeaders() });
            showSuccess("Account updated successfully.");
        } else {
            // NOTE: the backend's SystemAccount entity only has a "passwordHash"
            // field. The service takes whatever is in that field and BCrypt
            // encodes it - so we send the plain password under that key.
            let payload = { email: email, passwordHash: password, fullName: fullName, domainRole: domainRole };
            await axios.post(ACCOUNTS_URL, payload, { headers: getHeaders() });
            showSuccess("New identity provisioned successfully.");
        }
        accountModal.hide();
        loadAccounts();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to save account."));
    } finally {
        stopLoading("saveBtn", "Save Account");
    }
}

async function toggleStatus(id) {
    if (!confirm("Change the active status of this account?")) return;
    try {
        await axios.patch(ACCOUNTS_URL + "/" + id + "/toggle-status", null, { headers: getHeaders() });
        showSuccess("Account status updated.");
        loadAccounts();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to update account status."));
    }
}

async function deleteAccount(id) {
    if (!confirm("Delete this account permanently?")) return;
    try {
        await axios.delete(ACCOUNTS_URL + "/" + id, { headers: getHeaders() });
        showSuccess("Account deleted successfully.");
        loadAccounts();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to delete account."));
    }
}
