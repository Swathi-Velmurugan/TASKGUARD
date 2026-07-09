// ============================================
// TaskGuard - submissions.js
// ============================================

checkLogin();
renderShell("submissions");

const currentRole = getCurrentRole();
const currentEmail = getCurrentEmail();
const isManager = currentRole === "PROJECT_MANAGER";
const isContributor = currentRole === "TEAM_CONTRIBUTOR";
let submissionModal;
let myTasks = []; // tasks currently assigned to the logged-in contributor
let myContributorId = null;

if (isManager) {
    document.getElementById("actionsHeader").style.display = "table-cell";
}
if (isContributor) {
    document.getElementById("addBtn").style.display = "inline-block";
}

document.getElementById("addBtn").addEventListener("click", openSubmissionForm);
document.getElementById("submissionForm").addEventListener("submit", createSubmission);
document.getElementById("taskFilter").addEventListener("change", loadSubmissions);
document.getElementById("clearFilterBtn").addEventListener("click", function () {
    document.getElementById("taskFilter").value = "";
    loadSubmissions();
});

window.addEventListener("DOMContentLoaded", async function () {
    submissionModal = new bootstrap.Modal(document.getElementById("submissionModal"));
    await loadTaskFilterOptions();
    await loadSubmissions();

    // Support "Log Hours" deep link coming from tasks.html?taskId=123
    let urlParams = new URLSearchParams(window.location.search);
    let taskId = urlParams.get("taskId");
    if (isContributor && taskId) {
        await openSubmissionForm();
        document.getElementById("taskId").value = taskId;
    }
});

async function loadTaskFilterOptions() {
    try {
        let response = await axios.get(TASKS_URL, { headers: getHeaders() });
        let tasks = response.data;

        if (isContributor) {
            myTasks = tasks.filter(t => t.assignee && t.assignee.email === currentEmail);
            if (myTasks.length > 0) myContributorId = myTasks[0].assignee.id;
        }

        let options = '<option value="">All Tasks</option>';
        let sourceList = isContributor ? myTasks : tasks;
        sourceList.forEach(t => {
            options += `<option value="${t.id}">[${t.taskCode}] ${t.title}</option>`;
        });
        document.getElementById("taskFilter").innerHTML = options;
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load tasks list."));
    }
}

async function loadSubmissions() {
    let taskId = document.getElementById("taskFilter").value;
    let params = {};
    if (taskId !== "") params.taskId = taskId;

    try {
        let response = await axios.get(SUBMISSIONS_URL, { headers: getHeaders(), params: params });
        let submissions = response.data;

        if (isContributor) {
            submissions = submissions.filter(s => s.contributor && s.contributor.email === currentEmail);
        }
        renderTable(submissions);
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load submissions."));
    }
}

function renderTable(submissions) {
    let tbody = document.getElementById("submissionsTableBody");
    let emptyState = document.getElementById("emptyState");

    if (!submissions || submissions.length === 0) {
        tbody.innerHTML = "";
        emptyState.style.display = "block";
        return;
    }
    emptyState.style.display = "none";

    let rows = "";
    submissions.forEach(item => {
        let actionsCell = "";
        if (isManager) {
            if (item.completionStatus === "SUBMITTED") {
                actionsCell = `
                    <button class="btn btn-sm btn-outline-success me-1" onclick="reviewSubmission(${item.id}, 'APPROVED')">
                        <i class="bi bi-check-lg"></i> Approve
                    </button>
                    <button class="btn btn-sm btn-outline-danger me-1" onclick="reviewSubmission(${item.id}, 'REJECTED')">
                        <i class="bi bi-x-lg"></i> Reject
                    </button>`;
            }
            actionsCell += `
                <button class="btn btn-sm btn-outline-secondary" onclick="deleteSubmission(${item.id})">
                    <i class="bi bi-trash"></i> Delete
                </button>`;
        }

        rows += `
            <tr>
                <td class="code-tag">SUB-${item.id}</td>
                <td class="code-tag">TSK-${item.task ? item.task.id : "?"}</td>
                <td>${item.contributor ? item.contributor.fullName : "-"}</td>
                <td>${item.hoursSpent} Hours</td>
                <td>
                    <div>${item.submissionNotes}</div>
                    ${item.reviewerFeedback ? `<div class="text-muted small">Feedback: ${item.reviewerFeedback}</div>` : ""}
                </td>
                <td><span class="badge ${statusBadgeClass(item.completionStatus)}">${item.completionStatus}</span></td>
                <td>${actionsCell}</td>
            </tr>`;
    });
    tbody.innerHTML = rows;
}

async function openSubmissionForm() {
    document.getElementById("submissionForm").reset();
    document.querySelectorAll("#submissionForm .field-error").forEach(e => e.textContent = "");

    if (myTasks.length === 0) {
        showError("You have no tasks assigned yet. Ask your Project Manager to assign you a task first.");
        return;
    }

    let options = "";
    myTasks.forEach(t => {
        options += `<option value="${t.id}">[${t.taskCode}] ${t.title} (${t.status})</option>`;
    });
    document.getElementById("taskId").innerHTML = options;
    submissionModal.show();
}

async function createSubmission(event) {
    event.preventDefault();
    document.querySelectorAll("#submissionForm .field-error").forEach(e => e.textContent = "");

    let taskId = document.getElementById("taskId").value;
    let hoursSpent = document.getElementById("hoursSpent").value;
    let submissionNotes = document.getElementById("submissionNotes").value.trim();

    let hasError = false;
    if (isBlank(taskId)) {
        document.getElementById("taskIdError").textContent = "Select a task";
        hasError = true;
    }
    if (isBlank(hoursSpent) || Number(hoursSpent) <= 0) {
        document.getElementById("hoursSpentError").textContent = "Enter a positive number of hours";
        hasError = true;
    }
    if (isBlank(submissionNotes)) {
        document.getElementById("submissionNotesError").textContent = "Operational logging documentation summary is mandatory.";
        hasError = true;
    }
    if (!myContributorId) {
        showError("Could not resolve your contributor account id.");
        return;
    }
    if (hasError) return;

    let payload = {
        hoursSpent: Number(hoursSpent),
        submissionNotes: submissionNotes,
        completionStatus: "SUBMITTED"
    };

    try {
        startLoading("submitBtn", "Submitting...");
        await axios.post(SUBMISSIONS_URL, payload, {
            headers: getHeaders(),
            params: { taskId: taskId, contributorId: myContributorId }
        });
        showSuccess("Work log hours submitted for management evaluation successfully.");
        submissionModal.hide();
        loadSubmissions();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to submit work log."));
    } finally {
        stopLoading("submitBtn", "Finalize Submission");
    }
}

async function reviewSubmission(id, completionStatus) {
    let feedback = prompt("Enter reviewer feedback for this submission:");
    if (feedback === null) return;
    try {
        await axios.patch(SUBMISSIONS_URL + "/" + id + "/review", null, {
            headers: getHeaders(),
            params: { reviewerFeedback: feedback, completionStatus: completionStatus }
        });
        showSuccess("Submission " + completionStatus.toLowerCase() + " successfully.");
        loadSubmissions();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to review submission."));
    }
}

async function deleteSubmission(id) {
    if (!confirm("Delete this submission record?")) return;
    try {
        await axios.delete(SUBMISSIONS_URL + "/" + id, { headers: getHeaders() });
        showSuccess("Submission deleted successfully.");
        loadSubmissions();
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to delete submission."));
    }
}
