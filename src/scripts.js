const loginForm = document.querySelector('#loginForm');
const registerForm = document.querySelector('#registerForm');
const loginButton = document.querySelector('#loginButton');
const registerButton = document.querySelector('#registerButton');

if (loginForm) {
    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();

        const userData = {
            username: document.getElementById('username').value,
            password: document.getElementById('password').value,
        };

        fetch('http://localhost:9000/users/login/', {
            method: 'POST',
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData),
        }).then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Error authenticating user.');
            }
        }).then(data => {
            console.log('Success: ', data); // placeholder
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

        fetch('http://localhost:9000/users/register/', {
            method: 'POST',
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData),
        }).then(response => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error('Error registering user.');
            }
        }).then(data => {
            console.log('Success: ', data); // placeholder
        }).catch(error => {
            console.log("Error: ", error); // placeholder
        })
    })
}