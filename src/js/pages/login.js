import {ROUTES, SUCCESS_MESSAGES} from "../utils/constants.js";
import {Validator} from "../utils/validation.js"
import {Toast} from "../components/toast.js";
import {userService} from "../services/userService.js";
import {sessionService} from "../services/sessionService.js";
import {errorService} from "../services/errorService.js";

const loginForm = document.getElementById('login-form');

if (loginForm) {
    loginForm.addEventListener('submit', handleLogin);
}

function handleLogin(e) {
    e.preventDefault();
    const credentials = getLoginFormData();

    if (validateLoginFormData(credentials)) {
        userService.login(credentials)
            .then(userData => handleLoginSuccess(userData))
            .catch(error => errorService.handleLoginError(error));
    }
}

function getLoginFormData() {
    return {
        username: document.getElementById('login-username').value,
        password: document.getElementById('login-password').value,
    };
}

function validateLoginFormData(formData) {
    return Validator.checkIfInputProvided('Username', formData.username) &&
        Validator.checkIfInputProvided('Password', formData.password);
}

function handleLoginSuccess(userData) {
    sessionService.setActiveSession(userData);
    Toast.showSuccessAndRedirect(SUCCESS_MESSAGES.LOGIN, ROUTES.USER_AREA)
}