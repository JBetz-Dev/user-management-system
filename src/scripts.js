const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const changePasswordButton = document.getElementById('changePasswordButton');
const listUsersButton = document.getElementById('listUsersButton');

if (loginForm) {
    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const userData = {
            username: document.getElementById('username').value,
            password: document.getElementById('password').value,
        };

        fetch("http://localhost:9000/users/login/", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData),
        }).then(response => {
            if (response.ok) {
                window.location.href = "user-area.html"
            } else {
                throw new Error("Error authenticating user.");
            }
        }).catch(error => {
            generateLoginError();
        });
    });
}

if (registerForm) {
    registerForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const userData = {
            username: document.getElementById('username').value,
            email: document.getElementById('email').value,
            password: document.getElementById('password').value
        }

        fetch("http://localhost:9000/users/register/", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData),
        }).then(response => {
            if (response.ok) {
                window.location.href = "user-area.html"
            } else {
                throw new Error("Error registering user.");
            }
        }).catch(() => {
            generateRegistrationError();
        })
    })
}

if (listUsersButton) {
    listUsersButton.addEventListener('click', (e) => {
        e.preventDefault();

        const userListContainer = document.getElementById('userListContainer');

        if (userListContainer.classList.contains("hidden")) {
            fetch("http://localhost:9000/users/", {
                method: "GET",
                headers: {"Accept": "application/json"},
            }).then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new Error("Error fetching users list");
                }
            }).then(data => {
                populateUsersList(data);
                userListContainer.classList.remove("hidden");
                listUsersButton.textContent = "Hide Users";
            }).catch(error => {
                console.log("Error: ", error);
            })
        } else {
            userListContainer.replaceChildren();
            userListContainer.classList.add("hidden");
            listUsersButton.textContent = "Show Users";
        }
    })
}

function generateLoginError() {
    const loginErrorContainer = document.getElementById('loginErrorContainer');

    let errorMessage = document.createElement("h4");
    errorMessage.textContent = "Login Attempt Unsuccessful - Please try again or register as a new user";
    loginErrorContainer.appendChild(errorMessage);
    loginErrorContainer.style.color = "red";
    loginErrorContainer.classList.remove("hidden");
}

function generateRegistrationError() {
    const registrationErrorContainer = document.getElementById('registrationErrorContainer');

    let errorMessage = document.createElement("h4");
    errorMessage.textContent = "Registration Attempt Unsuccessful - Please try again";
    registrationErrorContainer.appendChild(errorMessage);
    registrationErrorContainer.style.color = "red";
    registrationErrorContainer.classList.remove("hidden");
}

function populateUsersList(users) {
    const userList = document.getElementById('userList');

    for (let user in users) {
        let userListItem = document.createElement('li');
        userListItem.textContent = users[user].username;
        userList.appendChild(userListItem);
    }
}