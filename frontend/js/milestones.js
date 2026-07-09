// ============================================
// TaskGuard - milestones.js
// ============================================

checkLogin();
renderShell("milestones");

const isManager = getCurrentRole() === "PROJECT_MANAGER";
let milestoneModal;
let allInitiatives = [];

if (isManager) {
    document.getElementById("addBtn").style.display = "inline-block";
    document.getElementById("actionsHeader").style.display = "table-cell";
    document.getElementById("statusField").style.display = "block";
}

document.getElementById("addBtn").addEventListener("click", openCreateForm);
document.getElementById("milestoneForm").addEventListener("submit", saveMilestone);
document.getElementById("statusFilter").addEventListener("change", loadMilestones);
document.getElementById("initiativeFilter").addEventListener("change", loadMilestones);
document.getElementById("searchText").addEventListener("keyup", function (e) {
    if (e.key === "Enter") loadMilestones();
});
document.getElementById("clearFilterBtn").addEventListener("click", function () {
    document.getElementById("searchText").value = "";
    document.getElementById("statusFilter").value = "ALL";
    document.getElementById("initiativeFilter").value = "";
    loadMilestones();
});

window.addEventListener("DOMContentLoaded", async function () {
    milestoneModal = new bootstrap.Modal(document.getElementById("milestoneModal"));
    await loadInitiativeOptions();
    loadMilestones();
});

async function loadInitiativeOptions() {
    try {
        let response = await axios.get(INITIATIVES_URL, { headers: getHeaders() });
        allInitiatives = response.data;

        let filterOptions = '<option value="">All Initiatives</option>';
        let formOptions = '<option value="">Select Initiative...</option>';
        allInitiatives.forEach(item => {
            let label = `[${item.projectCode}] ${item.title}`;
            filterOptions += `<option value="${item.id}">${label}</option>`;
            formOptions += `<option value="${item.id}">${label}</option>`;
        });
        document.getElementById("initiativeFilter").innerHTML = filterOptions;
        document.getElementById("initiativeId").innerHTML = formOptions;
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load initiatives list."));
    }
}

async function loadMilestones() {
    let status = document.getElementById("statusFilter").value;
    let initiativeId = document.getElementById("initiativeFilter").value;
    let query = document.getElementById("searchText").value.trim();

    // Unlike initiatives, the milestone endpoint applies all filters together.
    let params = {};
    if (status !== "ALL") params.status = status;
    if (initiativeId !== "") params.initiativeId = initiativeId;
    if (query !== "") params.query = query;

    try {
        let response = await axios.get(MILESTONES_URL, { headers: getHeaders(), params: params });
        renderTable(response.data);
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load milestones."));
    }
}

function renderTable(milestones) {
    let tbody = document.getElementById("milestonesTableBody");
    let emptyState = document.getElementById("emptyState");

    if (!milestones || milestones.length === 0) {
        tbody.innerHTML = "";
        emptyState.style.display = "block";
        return;
    }
    emptyState.style.display = "none";

    let rows = "";
    milestones.forEach(item => {
        let actionsCell = "";
        if (isManager) {
            actionsCell = `
                <td>
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editMilestone(${item.id})">
                        <i class="bi bi-pencil-square"></i> Edit
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteMilestone(${item.id})">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </td>`;
        }
        rows += `
            <tr>
                <td class="code-tag">MLS-${item.id}</td>
                <td>${item.title}</td>
                <td class="small">${item.initiative ? "[" + item.initiative.projectCode + "] " + item.initiative.title : "-"}</td>
                <td>${formatDate(item.targetDate)}</td>
                <td>${item.allocatedHours} Hours</td>
                <td><span class="badge ${statusBadgeClass(item.status)}">${item.status}</span></td>
                ${actionsCell}
            </tr>`;
    });
    tbody.innerHTML = rows;
}

function resetForm() {
    document.getElementById("milestoneForm").reset();
    document.getElementById("milestoneId").value = "";
    document.getElementById("initiativeId").disabled = false;
    document.querySelectorAll("#milestoneForm .field-error").forEach(e => e.textContent = "");
}

function openCreateForm() {
    resetForm();
    document.getElementById("modalTitle").textContent = "Create New Milestone";
    document.getElementById("status").value = "PENDING";
    milestoneModal.show();
}

async function editMilestone(id) {
    try {
        let response = await axios.get(MILESTONES_URL + "/" + id, { headers: getHeaders() });
        let item = response.data;
        resetForm();
        document.getElementById("modalTitle").textContent = "Edit Milestone";
        document.getElementById("milestoneId").value = item.id;
        document.getElementById("title").value = item.title;
        document.getElementById("initiativeId").value = item.initiative.id;
        document.getElementById("initiativeId").disabled = true;
        document.getElementById("targetDate").value = item.targetDate;
        document.getElementById("allocatedHours").value = item.allocatedHours;
        document.getElementById("status").value = item.status;
        milestoneModal.show();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load milestone."));
    }
}

async function saveMilestone(event) {
    event.preventDefault();
    document.querySelectorAll("#milestoneForm .field-error").forEach(e => e.textContent = "");

    let id = document.getElementById("milestoneId").value;
    let title = document.getElementById("title").value.trim();
    let initiativeId = document.getElementById("initiativeId").value;
    let targetDate = document.getElementById("targetDate").value;
    let allocatedHours = document.getElementById("allocatedHours").value;
    let status = document.getElementById("status").value;

    let hasError = false;
    if (isBlank(title)) {
        document.getElementById("titleError").textContent = "Title is required";
        hasError = true;
    }
    if (isBlank(initiativeId)) {
        document.getElementById("initiativeIdError").textContent = "Select a parent initiative";
        hasError = true;
    }
    if (isBlank(targetDate)) {
        document.getElementById("targetDateError").textContent = "Target date is required";
        hasError = true;
    }
    if (isBlank(allocatedHours) || Number(allocatedHours) <= 0) {
        document.getElementById("allocatedHoursError").textContent = "Enter a valid number of hours";
        hasError = true;
    }
    if (hasError) return;

    let payload = {
        title: title,
        targetDate: targetDate,
        allocatedHours: Number(allocatedHours),
        status: status
    };

    try {
        startLoading("saveBtn", "Saving...");
        if (id) {
            await axios.put(MILESTONES_URL + "/" + id, payload, { headers: getHeaders() });
            showSuccess("Milestone updated successfully.");
        } else {
            await axios.post(MILESTONES_URL, payload, {
                headers: getHeaders(),
                params: { initiativeId: initiativeId }
            });
            showSuccess("New milestone created successfully.");
        }
        milestoneModal.hide();
        loadMilestones();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to save milestone."));
    } finally {
        stopLoading("saveBtn", "Save Milestone");
    }
}

async function deleteMilestone(id) {
    if (!confirm("Delete this milestone? This will also remove tasks linked to it.")) {
        return;
    }
    try {
        await axios.delete(MILESTONES_URL + "/" + id, { headers: getHeaders() });
        showSuccess("Milestone deleted successfully.");
        loadMilestones();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to delete milestone."));
    }
}
