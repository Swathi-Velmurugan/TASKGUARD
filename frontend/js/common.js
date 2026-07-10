// ============================================
// TaskGuard - common.js
// Shared helpers used by every page
// ============================================

const BASE_URL = "http://localhost:8080";
const LOGIN_URL = BASE_URL + "/login";

const INITIATIVES_URL = BASE_URL + "/api/initiatives";
const MILESTONES_URL = BASE_URL + "/api/milestones";
const TASKS_URL = BASE_URL + "/api/tasks";
const SUBMISSIONS_URL = BASE_URL + "/api/submissions";
const ACCOUNTS_URL = BASE_URL + "/api/accounts";

// ============================================
// TOKEN / SESSION HELPERS
// ============================================

function saveSession(token) {
    localStorage.setItem("tg_token", token);

    let payload = decodeToken(token);
    if (payload) {
        localStorage.setItem("tg_email", payload.sub || "");
        localStorage.setItem("tg_role", extractRole(payload));
    }
}

// Decode the JWT payload (no signature check needed on the frontend,
// we only read the claims that the server already signed)
function decodeToken(token) {
    try {
        let base64Payload = token.split(".")[1];
        let base64 = base64Payload.replace(/-/g, "+").replace(/_/g, "/");
        return JSON.parse(decodeURIComponent(escape(atob(base64))));
    } catch (error) {
        return null;
    }
}

// The backend puts the granted authorities in the "role" claim as
// [{ authority: "ROLE_PROJECT_DIRECTOR" }] so we pull the plain name out of it
function extractRole(payload) {
    try {
        let roleClaim = payload.role;
        if (Array.isArray(roleClaim) && roleClaim.length > 0) {
            let authority = roleClaim[0].authority || roleClaim[0];
            return authority.replace("ROLE_", "");
        }
        if (typeof roleClaim === "string") {
            return roleClaim.replace("ROLE_", "");
        }
    } catch (error) {
        // ignore
    }
    return null;
}

// ============================================
// REDIRECT / HISTORY HELPERS
// ============================================

function getToken() {
    return localStorage.getItem("tg_token");
}

function getCurrentEmail() {
    return localStorage.getItem("tg_email");
}

function getCurrentRole() {
    return localStorage.getItem("tg_role");
}

function isLoggedIn() {
    return getToken() != null;
}

function checkLogin() {
    if (!isLoggedIn() || !getCurrentRole()) {
        window.location.replace("login.html");
        // Stop the rest of this page's script from running (rendering the
        // navbar, firing API calls, etc.) while the redirect is in flight -
        // without this, a logged-out visit to a protected page would briefly
        // render with no user data ("Welcome back null") before leaving.
        throw new Error("Not authenticated - redirecting to login.");
    }
}

// If the user logs out and then presses the browser's Back button, some
// browsers restore the previous protected page from the back/forward cache
// instead of reloading it - which would show stale content with no session.
// Re-check the session whenever a cached page is restored like this.
window.addEventListener("pageshow", function (event) {
    if (event.persisted && !isLoggedIn()) {
        window.location.replace("index.html");
    }
});

function logout() {
    localStorage.removeItem("tg_token");
    localStorage.removeItem("tg_email");
    localStorage.removeItem("tg_role");
    localStorage.removeItem("tg_myAccountId");
    window.location.replace("index.html");
}

function getHeaders() {
    return {
        "Content-Type": "application/json",
        "Authorization": "Bearer " + getToken()
    };
}

// Only a PROJECT_DIRECTOR can call GET /api/accounts, so this is how a
// logged-in director looks up their own account id (needed as ?directorId=
// when creating a ProjectInitiative). Result is cached so we don't call
// the API on every single form open.
async function getMyAccountId() {
    let cached = localStorage.getItem("tg_myAccountId");
    if (cached) {
        return Number(cached);
    }
    let response = await axios.get(ACCOUNTS_URL, { headers: getHeaders() });
    let accounts = response.data;
    let me = accounts.find(a => a.email === getCurrentEmail());
    if (me) {
        localStorage.setItem("tg_myAccountId", me.id);
        return me.id;
    }
    return null;
}

// ============================================
// ALERT HELPERS
// ============================================

function showSuccess(message) {
    alert(message);
}

function showError(message) {
    alert(message);
}

// Reads a plain-text error body coming back from GlobalExceptionHandler
// (it returns the raw message string, not a JSON object)
function extractErrorMessage(error, fallback) {
    if (error.response) {
        if (typeof error.response.data === "string" && error.response.data.trim() !== "") {
            return error.response.data;
        }
        if (error.response.status === 401) {
            return "Invalid email or password";
        }
        if (error.response.status === 403) {
            return "You do not have permission to do that.";
        }
        if (error.response.status === 404) {
            return "The requested record was not found.";
        }
        return fallback;
    }
    return "Unable to connect to the server. Is the backend running on " + BASE_URL + "?";
}

// ============================================
// BUTTON LOADING STATE
// ============================================

function startLoading(btnId, loadingText) {
    let btn = document.getElementById(btnId);
    if (!btn) return;
    btn.dataset.originalHtml = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> ' + loadingText;
}

// ============================================
// BUTTON LOADING STATE
// ============================================

function stopLoading(btnId, restoreHtml) {
    let btn = document.getElementById(btnId);
    if (!btn) return;
    btn.disabled = false;
    btn.innerHTML = restoreHtml || btn.dataset.originalHtml || "Submit";
}

// ============================================
// VALIDATION HELPERS
// ============================================

function isValidEmail(email) {
    let pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return pattern.test(email);
}

function isBlank(value) {
    return value === undefined || value === null || value.toString().trim() === "";
}

function formatDate(value) {
    if (!value) return "-";
    return value;
}

function money(value) {
    if (value === undefined || value === null) return "0.00";
    return Number(value).toFixed(2);
}

// ============================================
// STATUS BADGE HELPERS
// ============================================

function statusBadgeClass(status) {
    switch (status) {
        case "ACTIVE":
        case "IN_PROGRESS":
        case "APPROVED":
            return "badge-teal";
        case "PLANNING":
        case "PENDING":
        case "SUBMITTED":
            return "badge-amber";
        case "ON_HOLD":
        case "IN_REVIEW":
            return "badge-slate";
        case "COMPLETED":
        case "ACHIEVED":
            return "badge-green";
        case "REJECTED":
            return "badge-red";
        default:
            return "badge-slate";
    }
}

function priorityBadgeClass(priority) {
    switch (priority) {
        case "LOW": return "badge-green";
        case "MEDIUM": return "badge-amber";
        case "HIGH": return "badge-slate-dark";
        case "CRITICAL": return "badge-red";
        default: return "badge-slate";
    }
}

// ============================================
// SIDEBAR NAVIGATION
// ============================================

const NAV_LINKS = [
    { id: "dashboard", href: "dashboard.html", icon: "bi-speedometer2", text: "Dashboard",
      roles: ["PROJECT_DIRECTOR", "PROJECT_MANAGER", "TEAM_CONTRIBUTOR"] },
    { id: "initiatives", href: "initiatives.html", icon: "bi-diagram-3", text: "Initiatives",
      roles: ["PROJECT_DIRECTOR", "PROJECT_MANAGER", "TEAM_CONTRIBUTOR"] },
    { id: "milestones", href: "milestones.html", icon: "bi-flag", text: "Milestones",
      roles: ["PROJECT_DIRECTOR", "PROJECT_MANAGER", "TEAM_CONTRIBUTOR"] },
    { id: "tasks", href: "tasks.html", icon: "bi-list-check", text: "Tasks",
      roles: ["PROJECT_MANAGER", "TEAM_CONTRIBUTOR"] },
    { id: "submissions", href: "submissions.html", icon: "bi-clipboard-check", text: "Submissions",
      roles: ["PROJECT_MANAGER", "TEAM_CONTRIBUTOR"] },
    { id: "accounts", href: "accounts.html", icon: "bi-people", text: "Accounts",
      roles: ["PROJECT_DIRECTOR"] }
];

function renderShell(activeId) {
    let role = getCurrentRole();
    let email = getCurrentEmail();

    let navHtml = "";
    NAV_LINKS.forEach(link => {
        if (link.roles.indexOf(role) === -1) return;
        let activeClass = (link.id === activeId) ? "active" : "";
        navHtml += `
            <a href="${link.href}" class="list-group-item list-group-item-action ${activeClass}">
                <i class="bi ${link.icon}"></i> ${link.text}
            </a>`;
    });
    navHtml += `
        <a href="#" class="list-group-item list-group-item-action" id="logoutBtn">
            <i class="bi bi-box-arrow-right"></i> Logout
        </a>`;

    let sidebar = document.getElementById("sidebarNav");
    if (sidebar) sidebar.innerHTML = navHtml;

    let greeting = document.getElementById("userGreeting");
    if (greeting) {
        greeting.innerHTML = `${email} <span class="role-pill">${role ? role.replace("_", " ") : ""}</span>`;
    }

    let logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) logoutBtn.addEventListener("click", logout);

    // Guard: if a page is opened directly by URL but this role has no
    // business being on it (e.g. TEAM_CONTRIBUTOR opening accounts.html),
    // send them back to the dashboard instead of letting the API 403 on them.
    let currentLink = NAV_LINKS.find(l => l.id === activeId);
    if (currentLink && currentLink.roles.indexOf(role) === -1) {
        window.location.replace("dashboard.html");
    }
}