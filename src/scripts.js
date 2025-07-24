const loginForm = document.getElementById('login-form');
const signUpForm = document.getElementById('sign-up-form');
const listUsersButton = document.getElementById('list-users-btn');
const userProfileContainer = document.getElementById('user-profile');
const changePasswordButton = document.getElementById('change-password-btn');
const changeEmailButton = document.getElementById('change-email-btn');

document.addEventListener('DOMContentLoaded', () => {
    let userProfile = JSON.parse(sessionStorage.getItem("user"));
    if (userProfile && userProfile.active) {
        document.getElementById('login-btn').classList.add('hidden');
        document.getElementById('sign-up-btn').classList.add('hidden');
        document.getElementById('logout-btn').classList.remove('hidden');
        document.getElementById('user-area-btn').classList.remove('hidden');
        document.getElementById('profile-btn').classList.remove('hidden');
    }
});

if (loginForm) {
    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const userData = {
            username: document.getElementById('login-username').value,
            password: document.getElementById('login-password').value,
        };

        fetch("http://localhost:9000/users/login/", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData),
        }).then(response => {
            if (response.ok) {
                return response.json();
            } else if (response.status === 401 || response.status === 404) {
                throw new Error("Invalid Attempt");
            } else {
                throw new Error("Error authenticating user");
            }
        }).then(data => {
            data.active = true;
            sessionStorage.setItem("user", JSON.stringify(data));
            window.location.href = "user-area.html";
            showToast("success", "Success!", "Logged in successfully!");
        }).catch(error => {
            if (error.message === "Invalid Attempt") {
                generateLoginError();
            } else {
                console.log("Error: ", error);
                showToast("error", "Oops!", "Something went wrong!");
            }
        });
    });
}

if (signUpForm) {
    signUpForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const userData = {
            username: document.getElementById('sign-up-username').value,
            email: document.getElementById('sign-up-email').value,
            password: document.getElementById('sign-up-password').value
        }

        fetch("http://localhost:9000/users/", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData),
        }).then(response => {
            if (response.ok) {
                return response.json();
            } else if (response.status === 409) {
                throw new Error("Already Exists");
            } else {
                throw new Error("Error registering user");
            }
        }).then(data => {
            data.active = true;
            sessionStorage.setItem("user", JSON.stringify(data));
            window.location.href = "user-area.html";
            showToast("success", "Success!", "Registered successfully!");
        }).catch(error => {
            if (error.message === "Already Exists") {
                generateRegistrationError();
            } else {
                console.log("Error: ", error);
                showToast("error", "Oops!", "Something went wrong!");
            }
        })
    })
}

if (listUsersButton) {
    listUsersButton.addEventListener('click', (e) => {
        e.preventDefault();

        fetch("http://localhost:9000/users/", {
            method: "GET",
            headers: {"Accept": "application/json"},
        }).then(response => {
            if (response.ok) {
                return response.json();
            } else if (response.status === 401) {
                throw new Error("Session Expired");
            } else {
                throw new Error("Error fetching users list");
            }
        }).then(data => {
            populateUsersList(data);
        }).catch(error => {
            if (error.message === "Session Expired") {
                handleSessionExpiry();
            } else {
                console.log("Error: ", error.message);
                showToast("error", "Oops!", "Something went wrong");
            }
        });
    });
}

if (changePasswordButton) {
    changePasswordButton.addEventListener('click', (e) => {
        e.preventDefault();

        showChangePasswordModal();
    });
}

if (changeEmailButton) {
    changeEmailButton.addEventListener('click', (e) => {
        e.preventDefault();

        showChangeEmailModal();
    });
}

if (userProfileContainer) {
    let userData = JSON.parse(sessionStorage.getItem("user"));

    if (userData) {
        document.getElementById('profile-username').textContent = userData.username;
        document.getElementById('profile-email').textContent = userData.email;
    }
}

function handleChangePassword() {
    let currentPassword = document.getElementById('current-password').value;
    let newPassword = document.getElementById('new-password').value;
    let userData = JSON.parse(sessionStorage.getItem("user"));

    if (userData) {
        fetch(`http://localhost:9000/users/${userData.id}/password/`, {
            method: "PATCH",
            credentials: "include",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                id: userData.id,
                currentPassword: currentPassword,
                newPassword: newPassword
            })
        }).then(response => {
            if (response.ok) {
                return response.json();
            } else if (response.status === 401) {
                throw new Error("Session Expired");
            } else {
                throw new Error("Error updating password");
            }
        }).then(data => {
            sessionStorage.setItem("user", JSON.stringify(data));
            closeModal();
            showToast("success", "Success!", "Password changed successfully");
        }).catch(error => {
            if (error.message === "Session Expired") {
                handleSessionExpiry();
            } else {
                console.log("Error: ", error.message);
                showToast("error", "Oops!", "Something went wrong");
            }
        });
    } else {
        handleSessionExpiry();
    }
}

function handleChangeEmail() {
    let password = document.getElementById('password').value;
    let newEmail = document.getElementById('new-email').value;
    let userData = JSON.parse(sessionStorage.getItem("user"));

    if (userData) {
        fetch(`http://localhost:9000/users/${userData.id}/email/`, {
            method: "PATCH",
            credentials: "include",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                id: userData.id,
                password: password,
                newEmail: newEmail
            })
        }).then(response => {
            if (response.ok) {
                return response.json();
            } else if (response.status === 401) {
                throw new Error("Session Expired");
            } else {
                throw new Error("Error updating email");
            }
        }).then(data => {
            sessionStorage.setItem("user", JSON.stringify(data));
            closeModal();
            document.getElementById('profile-email').textContent = data.email;
            showToast("success", "Success!", "Email changed successfully");
        }).catch(error => {
            if (error.message === "Session Expired") {
                handleSessionExpiry();
            } else {
                console.log("Error: ", error.message, "Status: ", error.status);
                showToast("error", "Oops!", "Something went wrong");
            }
        });
    } else {
        handleSessionExpiry();
    }
}

function redirectToLogin() {
    window.location.href = "login.html";
}

function redirectToRegistration() {
    window.location.href = "register.html";
}


/* Modals */
function generateLoginError() {
    let title = "Login Failed";
    let message = "Login unsuccessful - please try again or register as a new user"
    let options = {
        confirmAction: "redirectToRegistration()",
        confirmText: "Register",
    };

    createModal(title, message, options);
}

function generateRegistrationError() {
    let title = "Registration Failed";
    let message = "Username already exist - please log in or try a different username.";
    let options = {
        confirmAction: "redirectToLogin()",
        confirmText: "Login",
    };

    createModal(title, message, options);
}

function handleSessionExpiry() {
    let title = "Session Expired";
    let content = "Your session has expired - please log in again to continue";
    let options = {
        confirmAction: "redirectToLogin()",
        confirmText: "Log In",
    };

    createModal(title, content, options);
}

function populateUsersList(users) {
    let title = "Users List";
    let userItemsHTML = "";

    users.forEach(user => {
        userItemsHTML += `
        <li class="user-item">
            <div class="user-avatar">${user.username.substring(0, 1).toUpperCase()}</div>
            <div class="user-info">
                <div class="user-name">${user.username}</div>
                <div class="user-email">${user.email}</div>
            </div>
            <div class="user-role">Registered User</div>
        </li>
        `;
    });

    let userListHTML = `<ul class="user-list">${userItemsHTML}</ul>`;

    createModal(title, userListHTML);
}

function showChangePasswordModal() {
    let title = "Change Password";
    let contentHTML = `
        <form id="change-password-form">
            <div class="form-group">
                <label for="current-password">Current Password</label>
                <input type="password" id="current-password" class="form-input" placeholder="Enter your current password" required>
            </div>
            <div class="form-group">
                <label for="new-password">Password</label>
                <input type="password" id="new-password" class="form-input" placeholder="Enter your new password" required>
            </div>
            <button type="submit" class="form-btn">
                <i class="fas fa-user-plus"></i> Change Password
            </button>
        </form>
    `;

    createModal(title, contentHTML);

    document.getElementById('change-password-form').addEventListener('submit', (e) => {
        e.preventDefault();

        handleChangePassword();
    })
}

function showChangeEmailModal() {
    let title = "Change Email";
    let contentHTML = `
        <form id="change-email-form">
            <div class="form-group">
                <label for="new-email">New Email</label>
                <input type="email" id="new-email" class="form-input" placeholder="Enter your new email" required>
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" class="form-input" placeholder="Enter your password" required>
            </div>
            <button type="submit" class="form-btn">
                <i class="fas fa-user-plus"></i> Change Email
            </button>
        </form>
    `;

    createModal(title, contentHTML);

    document.getElementById('change-email-form').addEventListener('submit', (e) => {
        e.preventDefault();

        handleChangeEmail();
    })
}

function createModal(title, content, options = {}) {
    const modalId = options.id || "modal" + Date.now();

    const modalHTML = `
        <div id=${modalId} class="modal">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">${title}</h5>
                    <button type="button" class="modal-close-x" onclick="document.getElementById('${modalId}').remove()">&times;</button>
                </div>
                <div class="modal-body">
                    ${content}
                </div>
                <div class="modal-footer">
                    <div class="btn-container">
                        <button type="button" class="btn-secondary" onclick="document.getElementById('${modalId}').remove()">Close</button>
                        ${options.confirmText ? `<button type="button" class="btn-primary" onclick="${options.confirmAction || ''}">${options.confirmText}</button>` : ''}
                    </div>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHTML);

    const modal = document.getElementById(modalId);
    setTimeout(() => modal.classList.add('show'), 10);

    return modalId;
}

function closeModal() {
    document.querySelector('.modal').remove();
}


/* Toasts */
let toastCounter = 0;

function showToast(type = 'info', title = 'Notification', message = '', duration = 5000) {
    const toastId = `toast-${++toastCounter}`;

    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        container.id = 'toastContainer';
        document.body.appendChild(container);
    }

    const icons = {
        success: 'fas fa-check-circle',
        error: 'fas fa-exclamation-circle',
        warning: 'fas fa-exclamation-triangle',
        info: 'fas fa-info-circle'
    };

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.id = toastId;
    toast.innerHTML = `
                <div class="toast-icon">
                    <i class="${icons[type] || icons.info}"></i>
                </div>
                <div class="toast-content">
                    <div class="toast-title">${title}</div>
                    ${message ? `<div class="toast-message">${message}</div>` : ''}
                </div>
                <button class="toast-close" onclick="removeToast('${toastId}')">
                    <i class="fas fa-times"></i>
                </button>
                ${duration > 0 ? `<div class="toast-progress" style="width: 100%;"></div>` : ''}
            `;

    container.appendChild(toast);

    if (duration > 0) {
        const progressBar = toast.querySelector('.toast-progress');
        if (progressBar) {
            setTimeout(() => {
                progressBar.style.width = '0%';
                progressBar.style.transitionDuration = `${duration}ms`;
            }, 10);
        }

        setTimeout(() => {
            removeToast(toastId);
        }, duration);
    }

    return toastId;
}

function removeToast(toastId) {
    const toast = document.getElementById(toastId);
    if (toast && !toast.classList.contains('removing')) {
        toast.classList.add('removing');
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);

                const container = document.getElementById('toastContainer');
                if (container && container.children.length === 0) {
                    container.remove();
                }
            }
        }, 300);
    }
}

function clearAllToasts() {
    const container = document.getElementById('toastContainer');
    if (container) {
        const toasts = container.querySelectorAll('.toast');
        toasts.forEach(toast => {
            if (!toast.classList.contains('removing')) {
                removeToast(toast.id);
            }
        });
    }
}