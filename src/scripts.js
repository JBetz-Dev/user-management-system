const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');
const changePasswordButton = document.getElementById('changePasswordButton');
const listAllUsersButton = document.getElementById('listAllUsersButton');

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
            console.log("Error: ", error); // placeholder
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
                return response.json();
            } else {
                throw new Error("Error registering user.");
            }
        }).then(data => {
            console.log("Success: ", data); // placeholder
        }).catch(error => {
            console.log("Error: ", error); // placeholder
        })
    })
}

if (listAllUsersButton) {
    listAllUsersButton.addEventListener('click', (e) => {
        e.preventDefault();

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
            console.log("Success: ", data);
        }).catch(error => {
            console.log("Error: ", error);
        })
    })
}