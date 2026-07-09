// ============================================
// TaskGuard - initiatives.js
// ============================================

checkLogin();
renderShell("initiatives");

const isDirector = getCurrentRole() === "PROJECT_DIRECTOR";
let initiativeModal;

if (isDirector) {
    document.getElementById("addBtn").style.display = "inline-block";
    document.getElementById("actionsHeader").style.display = "table-cell";
    document.getElementById("statusField").style.display = "block";
}

document.getElementById("addBtn").addEventListener("click", openCreateForm);
document.getElementById("initiativeForm").addEventListener("submit", saveInitiative);
document.getElementById("statusFilter").addEventListener("change", loadInitiatives);
document.getElementById("searchText").addEventListener("keyup", function (e) {
    if (e.key === "Enter") loadInitiatives();
});
document.getElementById("clearFilterBtn").addEventListener("click", function () {
    document.getElementById("searchText").value = "";
    document.getElementById("statusFilter").value = "ALL";
    loadInitiatives();
});

window.addEventListener("DOMContentLoaded", function () {
    initiativeModal = new bootstrap.Modal(document.getElementById("initiativeModal"));
    loadInitiatives();
});

async function loadInitiatives() {
    let status = document.getElementById("statusFilter").value;
    let query = document.getElementById("searchText").value.trim();

    let params = {};
    // The backend only applies one filter at a time: status wins over query.
    if (status !== "ALL") {
        params.status = status;
    } else if (query !== "") {
        params.query = query;
    }

    try {
        let response = await axios.get(INITIATIVES_URL, { headers: getHeaders(), params: params });
        renderTable(response.data);
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load initiatives."));
    }
}

function renderTable(initiatives) {
    let tbody = document.getElementById("initiativesTableBody");
    let emptyState = document.getElementById("emptyState");

    if (!initiatives || initiatives.length === 0) {
        tbody.innerHTML = "";
        emptyState.style.display = "block";
        return;
    }
    emptyState.style.display = "none";

    let rows = "";
    initiatives.forEach(item => {
        let pct = 0;
        if (item.budgetAllocated > 0) {
            pct = Math.min(100, Math.round((item.budgetConsumed / item.budgetAllocated) * 100));
        }
        let barClass = pct >= 90 ? "red" : (pct >= 70 ? "amber" : "");

        let actionsCell = "";
        if (isDirector) {
            actionsCell = `
                <td>
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editInitiative(${item.id})">
                        <i class="bi bi-pencil-square"></i> Edit
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteInitiative(${item.id})">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </td>`;
        }

        rows += `
            <tr>
                <td class="code-tag">${item.projectCode}</td>
                <td>
                    <div class="fw-semibold">${item.title}</div>
                    <div class="text-muted small">${item.description || ""}</div>
                </td>
                <td style="min-width:160px;">
                    <div class="capacity-text">$${money(item.budgetConsumed)} / $${money(item.budgetAllocated)}</div>
                    <div class="capacity-track"><div class="capacity-fill ${barClass}" style="width:${pct}%"></div></div>
                </td>
                <td class="small">${formatDate(item.startDate)} &rarr; ${formatDate(item.targetEndDate)}</td>
                <td>${item.director ? item.director.fullName : "-"}</td>
                <td><span class="badge ${statusBadgeClass(item.status)}">${item.status}</span></td>
                ${actionsCell}
            </tr>`;
    });
    tbody.innerHTML = rows;
}

function resetForm() {
    document.getElementById("initiativeForm").reset();
    document.getElementById("initiativeId").value = "";
    document.getElementById("projectCode").disabled = false;
    document.querySelectorAll("#initiativeForm .field-error").forEach(e => e.textContent = "");
}

function openCreateForm() {
    resetForm();
    document.getElementById("modalTitle").textContent = "Create New Initiative";
    document.getElementById("projectCode").value = "PRJ-" + Math.floor(1000 + Math.random() * 9000);
    document.getElementById("status").value = "PLANNING";
    initiativeModal.show();
}

async function editInitiative(id) {
    try {
        let response = await axios.get(INITIATIVES_URL + "/" + id, { headers: getHeaders() });
        let item = response.data;
        resetForm();
        document.getElementById("modalTitle").textContent = "Edit Initiative";
        document.getElementById("initiativeId").value = item.id;
        document.getElementById("projectCode").value = item.projectCode;
        document.getElementById("projectCode").disabled = true;
        document.getElementById("title").value = item.title;
        document.getElementById("description").value = item.description || "";
        document.getElementById("budgetAllocated").value = item.budgetAllocated;
        document.getElementById("startDate").value = item.startDate;
        document.getElementById("targetEndDate").value = item.targetEndDate;
        document.getElementById("status").value = item.status;
        initiativeModal.show();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load initiative."));
    }
}

async function saveInitiative(event) {
    event.preventDefault();
    document.querySelectorAll("#initiativeForm .field-error").forEach(e => e.textContent = "");

    let id = document.getElementById("initiativeId").value;
    let projectCode = document.getElementById("projectCode").value.trim();
    let title = document.getElementById("title").value.trim();
    let description = document.getElementById("description").value.trim();
    let budgetAllocated = document.getElementById("budgetAllocated").value;
    let startDate = document.getElementById("startDate").value;
    let targetEndDate = document.getElementById("targetEndDate").value;
    let status = document.getElementById("status").value;

    let hasError = false;
    if (isBlank(projectCode)) {
        document.getElementById("projectCodeError").textContent = "Project code is required";
        hasError = true;
    }
    if (isBlank(title)) {
        document.getElementById("titleError").textContent = "Title is required";
        hasError = true;
    }
    if (isBlank(budgetAllocated) || Number(budgetAllocated) <= 0) {
        document.getElementById("budgetError").textContent = "Enter a valid budget amount";
        hasError = true;
    }
    if (isBlank(startDate)) {
        document.getElementById("startDateError").textContent = "Start date is required";
        hasError = true;
    }
    if (isBlank(targetEndDate)) {
        document.getElementById("targetEndDateError").textContent = "Target end date is required";
        hasError = true;
    }
    if (hasError) return;

    let payload = {
        projectCode: projectCode,
        title: title,
        description: description,
        budgetAllocated: Number(budgetAllocated),
        startDate: startDate,
        targetEndDate: targetEndDate,
        status: status
    };

    try {
        startLoading("saveBtn", "Saving...");
        if (id) {
            await axios.put(INITIATIVES_URL + "/" + id, payload, { headers: getHeaders() });
            showSuccess("Initiative updated successfully.");
        } else {
            let directorId = await getMyAccountId();
            if (!directorId) {
                showError("Could not resolve your director account id.");
                return;
            }
            await axios.post(INITIATIVES_URL, payload, {
                headers: getHeaders(),
                params: { directorId: directorId }
            });
            showSuccess("New initiative created successfully.");
        }
        initiativeModal.hide();
        loadInitiatives();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to save initiative."));
    } finally {
        stopLoading("saveBtn", "Save Initiative");
    }
}

async function deleteInitiative(id) {
    if (!confirm("Delete this initiative? This will also remove its milestones and tasks.")) {
        return;
    }
    try {
        await axios.delete(INITIATIVES_URL + "/" + id, { headers: getHeaders() });
        showSuccess("Initiative deleted successfully.");
        loadInitiatives();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to delete initiative."));
    }
}
