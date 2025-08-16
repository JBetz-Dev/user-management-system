import {Modal} from "../components/Modal.js";
import {userService} from "../services/userService.js";
import {errorService} from "../services/errorService.js";

const listUsersButton = document.getElementById('list-users-btn');

if (listUsersButton) {
    listUsersButton.addEventListener('click', handleListUsers);
}

function handleListUsers() {
    userService.getAllUsers()
        .then(users => createUsersList(users))
        .catch(error => errorService.handleRequestError(error));
}

function createUsersList(users) {
    const usersList = document.createElement('ul');

    users.forEach(user => {
        const userItem = createUserItem(user);
        usersList.appendChild(userItem);
    });

    Modal.show("Users List", usersList);
}

function createUserItem(user) {
    const userItem = document.createElement('li');
    userItem.classList.add('user-item');

    const userAvatar = createUserAvatar(user);
    userItem.appendChild(userAvatar);

    const userInfo = createUserInfo(user);
    userItem.appendChild(userInfo);

    const userRole = createUserRole();
    userItem.appendChild(userRole);

    return userItem;
}

function createUserAvatar(user) {
    const userAvatar = document.createElement('div');
    userAvatar.classList.add('user-avatar');
    userAvatar.textContent = `${user.username.substring(0, 1).toUpperCase()}`;

    return userAvatar;
}

function createUserInfo(user) {
    const userInfo = document.createElement('div');
    userInfo.classList.add('user-info');

    const userName = document.createElement('div');
    userName.classList.add('user-name');
    userName.textContent = `${user.username}`;
    userInfo.appendChild(userName);

    const userEmail = document.createElement('div');
    userEmail.classList.add('user-email');
    userEmail.textContent = `${user.email}`;
    userInfo.appendChild(userEmail);

    return userInfo;
}

function createUserRole() {
    const userRole = document.createElement('div');
    userRole.classList.add('user-role');
    userRole.textContent = "Registered User";

    return userRole;
}