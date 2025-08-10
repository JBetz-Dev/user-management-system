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

    document.getElementById('logout-btn').addEventListener('click', (e) => {
        e.preventDefault();

        handleLogout();
    })

    addPasswordRequirementsEventListener();
});

function handleLogout() {
    fetch("http://localhost:9000/users/logout/", {
        method: "POST",
        credentials: "include",
    }).then(response => {
        if (response.ok) {
            return response.json();
        } else {
            throw new Error("Error logging out");
        }
    }).then(() => {
        sessionStorage.removeItem("user");
        showSuccessToastAndRedirect("Logged out successfully!", "index.html");
    }).catch(error => {
        console.log("Error: ", error);
        showToast("error", "Oops!", "Something went wrong - please try again!");
    });
}

if (loginForm) {
    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const userData = {
            username: document.getElementById('login-username').value,
            password: document.getElementById('login-password').value,
        };

        if (!fieldValueProvided(userData.username, "Username") ||
            !fieldValueProvided(userData.password, "Password")) {
            return;
        }

        fetch("http://localhost:9000/users/login/", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData),
        }).then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(errorData => {
                    throw new Error(errorData.error || "unknown_error");
                });
            }
        }).then(data => {
            data.active = true;
            sessionStorage.setItem("user", JSON.stringify(data));
            showSuccessToastAndRedirect("Logged in successfully!", "user-area.html");
        }).catch(error => {
            switch (error.message) {
                case "invalid_input":
                    showToast("error", "Invalid Input", "Invalid input provided");
                    break;
                case "username_not_found":
                case "invalid_password":
                    generateLoginError();
                    break;
                default:
                    showToast("error", "Oops!", "Something went wrong");
                    break;
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

        if (!validateField(userData.username, "Username") ||
            !validateField(userData.email, "Email") ||
            !validateField(userData.password, "Password")) {
            return;
        }

        fetch("http://localhost:9000/users/", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData),
        }).then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(errorData => {
                    throw new Error(errorData.error || "unknown_error");
                });
            }
        }).then(data => {
            data.active = true;
            sessionStorage.setItem("user", JSON.stringify(data));
            showSuccessToastAndRedirect("Registered successfully!", "user-area.html");
        }).catch(error => {
            switch (error.message) {
                case "invalid_input":
                    showToast("error", "Invalid Input", "Invalid input provided");
                    break;
                case "username_already_exists":
                    generateRegistrationError();
                    break;
                default:
                    showToast("error", "Oops!", "Something went wrong");
                    break;
            }
        })
    })
}

if (listUsersButton) {
    listUsersButton.addEventListener('click', (e) => {
        e.preventDefault();

        fetch("http://localhost:9000/users/", {
            method: "GET",
            credentials: "include",
            headers: {"Accept": "application/json"},
        }).then(response => {
            if (response.ok) {
                return response.json();
            } else {
                return response.json().then(errorData => {
                    throw new Error(errorData.error || "unknown_error");
                });
            }
        }).then(data => {
            populateUsersList(data);
        }).catch(error => {
            if (error.message === "session_not_found") {
                handleSessionExpiry();
            } else {
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

function addPasswordRequirementsEventListener() {
    const passwordRequirementsToggle = document.getElementById('password-requirements-toggle');

    if (passwordRequirementsToggle) {
        passwordRequirementsToggle.addEventListener('click', (e) => {
            e.preventDefault();

            const content = document.getElementById('requirements-content');
            const icon = document.getElementById('requirements-icon');

            content.classList.toggle('expanded');
            icon.classList.toggle('expanded');
        })
    }
}

function handleChangePassword() {
    let currentPassword = document.getElementById('current-password').value;
    let newPassword = document.getElementById('new-password').value;
    let userData = JSON.parse(sessionStorage.getItem("user"));

    if (!fieldValueProvided(currentPassword, "Password") ||
        !validateField(newPassword, "Password")) {
        return;
    }

    if (newPassword === currentPassword) {
        showToast(
            "error",
            "Invalid Password",
            "Your new password must be different from your old password - please try again"
        );
        return;
    }

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
            } else {
                return response.json().then(errorData => {
                    throw new Error(errorData.error || "unknown_error");
                });
            }
        }).then(data => {
            sessionStorage.setItem("user", JSON.stringify(data));
            closeModal();
            showSuccessToastAndRedirect("Password changed successfully - please log in again to continue", "login.html");
        }).catch(error => {
            switch (error.message) {
                case "invalid_input":
                    showToast("error", "Invalid Input", "Invalid input provided");
                    break;
                case "session_not_found":
                case "session_user_mismatch":
                    handleSessionExpiry();
                    break;
                case "invalid_password":
                    showToast("error", "Invalid Password", "Please try again");
                    break;
                default:
                    showToast("error", "Oops!", "Something went wrong");
                    break;
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

    if (!validateField(newEmail, "Email") ||
        !fieldValueProvided(password, "Password")) {
        return;
    }

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
            } else {
                return response.json().then(errorData => {
                    throw new Error(errorData.error || "unknown_error");
                });
            }
        }).then(data => {
            sessionStorage.setItem("user", JSON.stringify(data));
            closeModal();
            document.getElementById('profile-email').textContent = data.email;
            showToast("success", "Success!", "Email changed successfully");
        }).catch(error => {
            switch (error.message) {
                case "invalid_input":
                    showToast("error", "Invalid Input", "Invalid input provided");
                    break;
                case "session_not_found":
                case "session_user_mismatch":
                    handleSessionExpiry();
                    break;
                case "invalid_password":
                    showToast("error", "Invalid Password", "Please try again");
                    break;
                default:
                    showToast("error", "Oops!", "Something went wrong");
                    break;
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


/* Validation */
function validateField(fieldValue, fieldName) {
    if (!fieldValueProvided(fieldValue, fieldName)) {
        return false;
    }

    let errorMessage;
    let validatorMethod;

    switch (fieldName) {
        case "Username":
            validatorMethod = isValidUsername;
            errorMessage = "Username must be between 4-25 characters - please try again";
            break;
        case "Email":
            validatorMethod = isValidEmail;
            errorMessage = "Invalid email provided - please try again";
            break;
        case "Password":
            validatorMethod = isValidPassword;
            errorMessage = "Password does not meet the complexity requirements - please try again";
            break;
    }

    if (!validatorMethod(fieldValue)) {
        showToast("error", `Invalid ${fieldName} Provided`, `${errorMessage}`);
        return false;
    }
    return true;
}

function fieldValueProvided(fieldValue, fieldName) {
    if (!fieldValue.trim()) {
        showToast("error",
            `${fieldName} Required`,
            `You must enter a ${fieldName.toLowerCase()} - please try again`
        );
        return false;
    }
    return true;
}

function isValidUsername(username) {
    const usernameRegex = /^[a-zA-Z0-9._-]{4,25}$/;

    return usernameRegex.test(username);
}

function isValidEmail(email) {
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    return emailRegex.test(email);
}

function isValidPassword(password) {
    const passwordRegex = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>])[A-Za-z\d!@#$%^&*(),.?":{}|<>]{8,40}$/;

    return passwordRegex.test(password);
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
        <form id="change-password-form" novalidate>
            <div class="form-group">
                <label for="current-password">Current Password</label>
                <input type="password" id="current-password" class="form-input" placeholder="Enter your current password">
            </div>
            <div class="form-group">
                <label for="new-password">New Password</label>
                <input type="password" id="new-password" class="form-input" placeholder="Enter your new password">
                <div class="password-requirements">
                    <div class="requirements-toggle" id="password-requirements-toggle">
                        <span class="requirements-toggle-text">Password requirements</span>
                        <i class="fas fa-chevron-down requirements-icon" id="requirements-icon"></i>
                    </div>
                    <div class="requirements-content" id="requirements-content">
                        <ul class="requirements-list">
                            <li class="requirements-item">
                                <i class="fas fa-circle requirements-item-icon"></i>
                                <span>Between 8 and 40 characters long</span>
                            </li>
                            <li class="requirements-item">
                                <i class="fas fa-circle requirements-item-icon"></i>
                                <span>Contains at least one uppercase and one lowercase letter</span>
                            </li>
                            <li class="requirements-item">
                                <i class="fas fa-circle requirements-item-icon"></i>
                                <span>Contains at least one number</span>
                            </li>
                            <li class="requirements-item">
                                <i class="fas fa-circle requirements-item-icon"></i>
                                <span>Contains at least one special character</span>
                            </li>
                        </ul>
                    </div>
                </div>
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
    });

    addPasswordRequirementsEventListener();
}

function showChangeEmailModal() {
    let title = "Change Email";
    let contentHTML = `
        <form id="change-email-form" novalidate>
            <div class="form-group">
                <label for="new-email">New Email</label>
                <input type="email" id="new-email" class="form-input" placeholder="Enter your new email">
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" class="form-input" placeholder="Enter your password">
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

function showSuccessToastAndRedirect(message, redirectUrl, delay = 500) {
    showToast("success", "Success!", message);
    setTimeout(() => {
        window.location.href = redirectUrl;
    }, delay);
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