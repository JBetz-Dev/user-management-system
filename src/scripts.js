document.getElementById('userForm').addEventListener('submit', (e) => {
    e.preventDefault();

    const userData = {
        username: document.getElementById('username').value,
        email: document.getElementById('email').value,
        password: document.getElementById('password').value
    };

    console.log(JSON.stringify(userData));

    fetch('http://localhost:9000/users/', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(userData)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Response not OK');
            }
            return response.json();
        })
        .then(data => {
            console.log('Success: ', data);
        })
        .catch(error => {
            console.log('Error: ', error);
        });
});

document.getElementById('userListContainer').addEventListener('click', (e) => {
    e.preventDefault();

    fetch('http://localhost:9000/users', {
        method: 'GET',
        headers: {'Content-Type': 'application/json'}
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Response not OK');
            }
            console.log(JSON.stringify(response));
            return response.json();
        })
        .then(data => {
            console.log('Success: ', data);
        })
        .catch(error => {
            console.log('Error: ', error);
        });

    // let userList = document.createElement('ul');
    // userList.appendChild(document.createElement('li').append(document.createTextNode(user)));

});