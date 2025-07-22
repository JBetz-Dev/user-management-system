const loginForm = document.getElementById('login-form');
const signUpForm = document.getElementById('sign-up-form');
const listUsersButton = document.getElementById('list-users-btn');
const userProfileContainer = document.getElementById('user-profile');
const changePasswordButton = document.getElementById('change-password-btn');

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
            } else {
                throw new Error("Error authenticating user");
            }
        }).then(data => {
            sessionStorage.setItem("user", JSON.stringify(data));
            window.location.href = "user-area.html";
        }).catch(error => {
            generateLoginError();
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
            } else {
                throw new Error("Error registering user");
            }
        }).then(data => {
            sessionStorage.setItem("user", JSON.stringify(data));
            window.location.href = "user-area.html";
        }).catch(() => {
            generateRegistrationError();
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
            if (response.status === 401) {
                throw new Error("Session Expired");
            } else if (response.ok) {
                return response.json();
            } else {
                throw new Error("Error fetching users list");
            }
        }).then(data => {
            populateUsersList(data);
        }).catch(error => {
            if (error.message === "Session Expired") {
                handleSessionExpiry();
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

if (userProfileContainer) {
    let userData = JSON.parse(sessionStorage.getItem("user"));

    if (userData) {
        document.getElementById('profile-username').textContent = userData.username;
        document.getElementById('profile-email').textContent = userData.email;
    }
}

function redirectToLogin() {
    window.location.href="login.html";
}

function redirectToRegistration() {
    window.location.href="register.html";
}

function generateLoginError() {
    let title = "Login Unsuccessful";
    let content = "Login attempt unsuccessful - please try again or register as a new user.";

    createModal(title, content);
}

function generateRegistrationError() {
    let title = "Registration Unsuccessful";
    let content = "Registration attempt unsuccessful - please try again.";

    createModal(title, content);
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
    let userListHTML = "";

    users.forEach(user => {
        userListHTML += `
        <li class="user-item">
            <div class="user-avatar">${user.username.substring(0,1).toUpperCase()}</div>
            <div class="user-info">
                <div class="user-name">${user.username}</div>
                <div class="user-email">${user.email}</div>
            </div>
            <div class="user-role">Registered User</div>
        </li>
        `;
    });

    showUserListModal(userListHTML);
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
            <button type="button" class="form-btn" onclick="handleChangePassword()">
                <i class="fas fa-user-plus"></i> Change Password
            </button>
        </form>
    `;

    createModal(title, contentHTML);
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
            if (response.status === 401) {
                throw new Error("Session Expired");
            } else if (response.ok) {
                return response.json();
            } else {
                throw new Error("Error updating password");
            }
        }).then(data => {
            console.log("Success: ", data);
            closeModal();
        }).catch(error => {
            if (error.message === "Session Expired") {
                handleSessionExpiry();
            }
            console.log("Error: ", error);
        });
    } else {
        handleSessionExpiry();
    }
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

function showUserListModal(content) {
    const modalId = "modal" + Date.now();

    const modalHTML = `
    <div id=${modalId} class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">All Users</h5>
                <button type="button" class="modal-close-x" onclick="document.getElementById('${modalId}').remove()">&times;</button>
            </div>
            <div class="modal-body">
                <ul class="user-list">
                    ${content}
                </ul>
            </div>
            <div class="modal-footer">
                <div class="btn-container">
                    <button type="button" class="btn-secondary" onclick="document.getElementById('${modalId}').remove()">Close</button>
                </div>
            </div>
        </div>
    </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHTML);

    const modal = document.getElementById(modalId);
    setTimeout(() => modal.classList.add('show'), 10);
}

function closeModal() {
    document.querySelector('.modal').remove();
}