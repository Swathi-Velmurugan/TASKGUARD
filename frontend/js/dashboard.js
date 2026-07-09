// ============================================
// TaskGuard - dashboard.js
// ============================================

checkLogin();
renderShell("dashboard");
loadDashboard();

function statCard(label, value, icon) {
    return `
        <div class="col-md-3 col-sm-6">
            <div class="card shadow-sm stat-card">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <div class="stat-label">${label}</div>
                            <div class="stat-value">${value}</div>
                        </div>
                        <i class="bi ${icon} fs-3 text-muted"></i>
                    </div>
                </div>
            </div>
        </div>`;
}

async function loadDashboard() {
    let role = getCurrentRole();
    let email = getCurrentEmail();
    let title = document.getElementById("dashboardTitle");
    let subtitle = document.getElementById("dashboardSubtitle");
    let cards = document.getElementById("statCards");

    if (role === "PROJECT_DIRECTOR") {
        title.textContent = "Executive Portfolio Control Center";
        subtitle.textContent = "Overview of all project initiatives and system accounts.";
        try {
            let [initiativesRes, accountsRes] = await Promise.all([
                axios.get(INITIATIVES_URL, { headers: getHeaders() }),
                axios.get(ACCOUNTS_URL, { headers: getHeaders() })
            ]);
            let initiatives = initiativesRes.data;
            let accounts = accountsRes.data;
            let activeCount = initiatives.filter(i => i.status === "ACTIVE").length;
            let totalBudget = initiatives.reduce((sum, i) => sum + Number(i.budgetAllocated || 0), 0);

            cards.innerHTML =
                statCard("Total Initiatives", initiatives.length, "bi-diagram-3") +
                statCard("Active Initiatives", activeCount, "bi-lightning-charge") +
                statCard("System Accounts", accounts.length, "bi-people") +
                statCard("Total Budget (USD)", money(totalBudget), "bi-cash-stack");
        } catch (error) {
            showError(extractErrorMessage(error, "Unable to load dashboard data."));
        }
        return;
    }

    if (role === "PROJECT_MANAGER") {
        title.textContent = "Operational Schedule Management Matrix";
        subtitle.textContent = "Overview of pipeline tasks, milestones, and pending reviews.";
        try {
            let [milestonesRes, tasksRes, submissionsRes] = await Promise.all([
                axios.get(MILESTONES_URL, { headers: getHeaders() }),
                axios.get(TASKS_URL, { headers: getHeaders() }),
                axios.get(SUBMISSIONS_URL, { headers: getHeaders() })
            ]);
            let milestones = milestonesRes.data;
            let tasks = tasksRes.data;
            let submissions = submissionsRes.data;
            let activeTasks = tasks.filter(t => t.status === "PENDING" || t.status === "IN_PROGRESS").length;
            let pendingReviews = submissions.filter(s => s.completionStatus === "SUBMITTED").length;

            cards.innerHTML =
                statCard("Total Milestones", milestones.length, "bi-flag") +
                statCard("Total Tasks", tasks.length, "bi-list-check") +
                statCard("Active Tasks", activeTasks, "bi-lightning-charge") +
                statCard("Pending Reviews", pendingReviews, "bi-clipboard-check");

            renderStatusChart(tasks);
        } catch (error) {
            showError(extractErrorMessage(error, "Unable to load dashboard data."));
        }
        return;
    }

    // TEAM_CONTRIBUTOR
    title.textContent = "Welcome back, " + email;
    subtitle.textContent = "Here is a summary of your assigned work.";
    try {
        let tasksRes = await axios.get(TASKS_URL, { headers: getHeaders() });
        let myTasks = tasksRes.data.filter(t => t.assignee && t.assignee.email === email);
        let activeTasks = myTasks.filter(t => t.status === "PENDING" || t.status === "IN_PROGRESS").length;

        let submissionsRes = await axios.get(SUBMISSIONS_URL, { headers: getHeaders() });
        let mySubs = submissionsRes.data.filter(s => s.contributor && s.contributor.email === email);
        let pendingSubs = mySubs.filter(s => s.completionStatus === "SUBMITTED").length;

        cards.innerHTML =
            statCard("My Assigned Tasks", myTasks.length, "bi-list-check") +
            statCard("Active Tasks", activeTasks, "bi-lightning-charge") +
            statCard("My Submissions", mySubs.length, "bi-clipboard-check") +
            statCard("Awaiting Review", pendingSubs, "bi-hourglass-split");
    } catch (error) {
        showError(extractErrorMessage(error, "Unable to load dashboard data."));
    }
}

// Simple CSS-only bar chart of task status distribution (manager view only)
function renderStatusChart(tasks) {
    let statuses = ["PENDING", "IN_PROGRESS", "IN_REVIEW", "COMPLETED"];
    let labels = {
        PENDING: "Pending Assignment",
        IN_PROGRESS: "Work In Progress",
        IN_REVIEW: "Pending Audit Review",
        COMPLETED: "Scope Achieved"
    };
    let counts = statuses.map(s => tasks.filter(t => t.status === s).length);
    let max = Math.max(...counts, 1);

    let html = "";
    statuses.forEach((status, i) => {
        let widthPct = Math.max(5, Math.round((counts[i] / max) * 100));
        html += `
            <div class="mb-3">
                <div class="d-flex justify-content-between small mb-1">
                    <span>${labels[status]}</span><span>${counts[i]}</span>
                </div>
                <div class="capacity-track">
                    <div class="capacity-fill" style="width:${widthPct}%"></div>
                </div>
            </div>`;
    });
    document.getElementById("statusChart").innerHTML = html;
}
