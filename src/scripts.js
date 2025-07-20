const loginForm = document.getElementById('login-form');
const signUpForm = document.getElementById('sign-up-form');
const listUsersButton = document.getElementById('list-users-btn');

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
                window.location.href = "user-area.html"
            } else {
                throw new Error("Error authenticating user");
            }
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

        fetch("http://localhost:9000/users/register/", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData),
        }).then(response => {
            if (response.ok) {
                window.location.href = "user-area.html"
            } else {
                throw new Error("Error registering user");
            }
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
            console.log("Error: ", error);
        });
    });
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