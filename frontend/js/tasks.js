// ============================================
// TaskGuard - tasks.js
// ============================================

checkLogin();
renderShell("tasks");

const currentRole = getCurrentRole();
const currentEmail = getCurrentEmail();
const isManager = currentRole === "PROJECT_MANAGER";
const isContributor = currentRole === "TEAM_CONTRIBUTOR";
let taskModal;
let allInitiatives = [];

if (isManager) {
    document.getElementById("addBtn").style.display = "inline-block";
    document.getElementById("statusField").style.display = "block";
}
if (isContributor) {
    document.getElementById("welcomeBanner").style.display = "block";
    document.getElementById("welcomeText").textContent = "Hello, " + currentEmail + "!";
}

document.getElementById("addBtn").addEventListener("click", openCreateForm);
document.getElementById("taskForm").addEventListener("submit", saveTask);
document.getElementById("statusFilter").addEventListener("change", loadTasks);
document.getElementById("searchText").addEventListener("keyup", function (e) {
    if (e.key === "Enter") loadTasks();
});
document.getElementById("clearFilterBtn").addEventListener("click", function () {
    document.getElementById("searchText").value = "";
    document.getElementById("statusFilter").value = "ALL";
    loadTasks();
});
document.getElementById("initiativeId").addEventListener("change", loadMilestoneOptionsForForm);

window.addEventListener("DOMContentLoaded", async function () {
    taskModal = new bootstrap.Modal(document.getElementById("taskModal"));
    if (isManager) {
        await loadInitiativeOptions();
    }
    loadTasks();
});

async function loadInitiativeOptions() {
    try {
        let response = await axios.get(INITIATIVES_URL, { headers: getHeaders() });
        allInitiatives = response.data;
        let options = '<option value="">Select Initiative Linkage...</option>';
        allInitiatives.forEach(item => {
            options += `<option value="${item.id}">[${item.projectCode}] ${item.title}</option>`;
        });
        document.getElementById("initiativeId").innerHTML = options;
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load initiatives list."));
    }
}

async function loadMilestoneOptionsForForm() {
    let initiativeId = document.getElementById("initiativeId").value;
    let select = document.getElementById("milestoneId");
    select.innerHTML = '<option value="">No milestone linkage</option>';
    if (!initiativeId) return;
    try {
        let response = await axios.get(MILESTONES_URL, {
            headers: getHeaders(),
            params: { initiativeId: initiativeId }
        });
        response.data.forEach(m => {
            select.innerHTML += `<option value="${m.id}">${m.title}</option>`;
        });
    } catch (error) {
        // Non-fatal - milestone linkage is optional
    }
}

async function loadTasks() {
    let status = document.getElementById("statusFilter").value;
    let query = document.getElementById("searchText").value.trim();

    let params = {};
    if (status !== "ALL") params.status = status;
    if (query !== "") params.query = query;

    try {
        let response = await axios.get(TASKS_URL, { headers: getHeaders(), params: params });
        let tasks = response.data;

        // A TEAM_CONTRIBUTOR can only ever act on tasks assigned to them,
        // so we narrow the list client-side to their own tasks.
        if (isContributor) {
            tasks = tasks.filter(t => t.assignee && t.assignee.email === currentEmail);
        }
        renderTable(tasks);
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load tasks."));
    }
}

function renderTable(tasks) {
    let tbody = document.getElementById("tasksTableBody");
    let emptyState = document.getElementById("emptyState");

    if (!tasks || tasks.length === 0) {
        tbody.innerHTML = "";
        emptyState.style.display = "block";
        return;
    }
    emptyState.style.display = "none";

    let rows = "";
    tasks.forEach(item => {
        let pct = 0;
        if (item.estimatedHours > 0) {
            pct = Math.min(100, Math.round((item.loggedHours / item.estimatedHours) * 100));
        }
        let barClass = pct >= 90 ? "red" : (pct >= 70 ? "amber" : "");

        let actionsCell = "";
        if (isManager) {
            actionsCell = `
                <button class="btn btn-sm btn-outline-primary me-1" onclick="editTask(${item.id})">
                    <i class="bi bi-pencil-square"></i> Edit
                </button>
                <button class="btn btn-sm btn-outline-secondary me-1" onclick="assignTask(${item.id})">
                    <i class="bi bi-person-plus"></i> Assign
                </button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteTask(${item.id})">
                    <i class="bi bi-trash"></i> Delete
                </button>`;
        } else if (isContributor) {
            actionsCell = `
                <a class="btn btn-sm btn-brand me-1" href="submissions.html?taskId=${item.id}">
                    <i class="bi bi-clipboard-plus"></i> Log Hours
                </a>
                <select class="form-select form-select-sm d-inline-block" style="width:140px;"
                        onchange="updateStatus(${item.id}, this.value)">
                    <option value="">Change status...</option>
                    <option value="PENDING" ${item.status === "PENDING" ? "selected" : ""}>PENDING</option>
                    <option value="IN_PROGRESS" ${item.status === "IN_PROGRESS" ? "selected" : ""}>IN_PROGRESS</option>
                    <option value="IN_REVIEW" ${item.status === "IN_REVIEW" ? "selected" : ""}>IN_REVIEW</option>
                    <option value="COMPLETED" ${item.status === "COMPLETED" ? "selected" : ""}>COMPLETED</option>
                </select>`;
        }

        rows += `
            <tr>
                <td class="code-tag">${item.taskCode}</td>
                <td>
                    <div class="fw-semibold">${item.title}</div>
                    <div class="text-muted small">Due ${formatDate(item.dueDate)}</div>
                </td>
                <td><span class="badge ${priorityBadgeClass(item.priority)}">${item.priority}</span></td>
                <td style="min-width:150px;">
                    <div class="capacity-text">${item.loggedHours} / ${item.estimatedHours} Hrs</div>
                    <div class="capacity-track"><div class="capacity-fill ${barClass}" style="width:${pct}%"></div></div>
                </td>
                <td>${item.assignee ? item.assignee.fullName : "Unassigned"}</td>
                <td><span class="badge ${statusBadgeClass(item.status)}">${item.status}</span></td>
                <td>${actionsCell}</td>
            </tr>`;
    });
    tbody.innerHTML = rows;
}

function resetForm() {
    document.getElementById("taskForm").reset();
    document.getElementById("taskId").value = "";
    document.getElementById("taskCode").disabled = false;
    document.getElementById("milestoneId").innerHTML = '<option value="">No milestone linkage</option>';
    document.querySelectorAll("#taskForm .field-error").forEach(e => e.textContent = "");
}

function openCreateForm() {
    resetForm();
    document.getElementById("modalTitle").textContent = "Create New Task";
    document.getElementById("taskCode").value = "TSK-" + Math.floor(100 + Math.random() * 900);
    document.getElementById("priority").value = "HIGH";
    taskModal.show();
}

async function editTask(id) {
    try {
        let response = await axios.get(TASKS_URL + "/" + id, { headers: getHeaders() });
        let item = response.data;
        resetForm();
        document.getElementById("modalTitle").textContent = "Edit Task";
        document.getElementById("taskId").value = item.id;
        document.getElementById("taskCode").value = item.taskCode;
        document.getElementById("taskCode").disabled = true;
        document.getElementById("title").value = item.title;
        document.getElementById("description").value = item.description || "";
        document.getElementById("priority").value = item.priority;
        document.getElementById("estimatedHours").value = item.estimatedHours;
        document.getElementById("dueDate").value = item.dueDate;
        document.getElementById("status").value = item.status;
        document.getElementById("initiativeId").value = item.initiative ? item.initiative.id : "";
        document.getElementById("initiativeId").disabled = true;
        await loadMilestoneOptionsForForm();
        if (item.milestone) document.getElementById("milestoneId").value = item.milestone.id;
        document.getElementById("assigneeId").value = item.assignee ? item.assignee.id : "";
        document.getElementById("assigneeId").disabled = true;
        taskModal.show();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load task."));
    }
}

async function saveTask(event) {
    event.preventDefault();
    document.querySelectorAll("#taskForm .field-error").forEach(e => e.textContent = "");

    let id = document.getElementById("taskId").value;
    let taskCode = document.getElementById("taskCode").value.trim();
    let title = document.getElementById("title").value.trim();
    let description = document.getElementById("description").value.trim();
    let priority = document.getElementById("priority").value;
    let estimatedHours = document.getElementById("estimatedHours").value;
    let dueDate = document.getElementById("dueDate").value;
    let status = document.getElementById("status").value;
    let initiativeId = document.getElementById("initiativeId").value;
    let milestoneId = document.getElementById("milestoneId").value;
    let assigneeId = document.getElementById("assigneeId").value;

    let hasError = false;
    if (isBlank(taskCode)) {
        document.getElementById("taskCodeError").textContent = "Task code is required";
        hasError = true;
    }
    if (isBlank(title)) {
        document.getElementById("titleError").textContent = "Actionable title label is mandatory.";
        hasError = true;
    }
    if (isBlank(estimatedHours) || Number(estimatedHours) <= 0) {
        document.getElementById("estimatedHoursError").textContent = "Estimated hours must be a positive number";
        hasError = true;
    }
    if (isBlank(dueDate)) {
        document.getElementById("dueDateError").textContent = "Due date is required";
        hasError = true;
    }
    if (!id && isBlank(initiativeId)) {
        document.getElementById("initiativeIdError").textContent = "Select a parent initiative";
        hasError = true;
    }
    if (hasError) return;

    let payload = {
        taskCode: taskCode,
        title: title,
        description: description,
        priority: priority,
        estimatedHours: Number(estimatedHours),
        dueDate: dueDate,
        status: status || "PENDING"
    };

    try {
        startLoading("saveBtn", "Saving...");
        if (id) {
            await axios.put(TASKS_URL + "/" + id, payload, { headers: getHeaders() });
            showSuccess("Task updated successfully.");
        } else {
            let params = { initiativeId: initiativeId };
            if (milestoneId) params.milestoneId = milestoneId;
            if (assigneeId) params.assigneeId = assigneeId;
            await axios.post(TASKS_URL, payload, { headers: getHeaders(), params: params });
            showSuccess("New workload task entry added to pipeline successfully.");
        }
        taskModal.hide();
        loadTasks();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to save task."));
    } finally {
        stopLoading("saveBtn", "Finalize Task Configuration");
    }
}

async function assignTask(id) {
    let assigneeId = prompt("Enter the contributor's Account ID to assign this task to:");
    if (!assigneeId) return;
    try {
        await axios.patch(TASKS_URL + "/" + id + "/assign", null, {
            headers: getHeaders(),
            params: { assigneeId: assigneeId }
        });
        showSuccess("Task assigned successfully.");
        loadTasks();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to assign task. (The contributor may already be at their 40-hour weekly limit.)"));
    }
}

async function updateStatus(id, newStatus) {
    if (!newStatus) return;
    try {
        await axios.patch(TASKS_URL + "/" + id + "/status", null, {
            headers: getHeaders(),
            params: { newStatus: newStatus }
        });
        showSuccess("Task status updated to " + newStatus + ".");
        loadTasks();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to update task status."));
    }
}

async function deleteTask(id) {
    if (!confirm("Delete this task? This will also remove its submission history.")) {
        return;
    }
    try {
        await axios.delete(TASKS_URL + "/" + id, { headers: getHeaders() });
        showSuccess("Task deleted successfully.");
        loadTasks();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to delete task."));
    }
}
