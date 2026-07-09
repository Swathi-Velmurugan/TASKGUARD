// ============================================
// TaskGuard - login.js
// ============================================

// PAGE LOAD - if already logged in, skip straight to dashboard
if (isLoggedIn()) {
    window.location.replace("dashboard.html");
}

document.getElementById("loginForm").addEventListener("submit", login);

document.getElementById("togglePassword").addEventListener("click", function () {
    let passwordField = document.getElementById("password");
    let icon = this.querySelector("i");
    if (passwordField.type === "password") {
        passwordField.type = "text";
        icon.classList.remove("bi-eye");
        icon.classList.add("bi-eye-slash");
    } else {
        passwordField.type = "password";
        icon.classList.remove("bi-eye-slash");
        icon.classList.add("bi-eye");
    }
});

async function login(event) {
    event.preventDefault();

    document.getElementById("emailError").textContent = "";
    document.getElementById("passwordError").textContent = "";

    let email = document.getElementById("email").value.trim();
    let password = document.getElementById("password").value.trim();

    // VALIDATION
    let hasError = false;
    if (isBlank(email)) {
        document.getElementById("emailError").textContent = "Email is required";
        hasError = true;
    } else if (!isValidEmail(email)) {
        document.getElementById("emailError").textContent = "Enter a valid email address";
        hasError = true;
    }
    if (isBlank(password)) {
        document.getElementById("passwordError").textContent = "Password is required";
        hasError = true;
    }
    if (hasError) return;

    let loginDto = { email: email, password: password };

    try {
        startLoading("loginBtn", "Signing in...");
        let response = await axios.post(LOGIN_URL, loginDto);
        saveSession(response.data.token);
        window.location.replace("dashboard.html");
    } catch (error) {
        showError(extractErrorMessage(error, "Login failed. Please try again."));
    } finally {
        stopLoading("loginBtn", '<i class="bi bi-box-arrow-in-right"></i> Login');
    }
}