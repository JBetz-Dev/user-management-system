import {ROUTES, SUCCESS_MESSAGES} from "../utils/constants.js";
import {validator} from "../utils/validation.js"
import {toast} from "../components/toast.js";
import {userService} from "../services/userService.js";
import {sessionService} from "../services/sessionService.js";
import {errorService} from "../services/errorService.js";

/**
 * Login page script handling user authentication workflow.
 * Manages login form submission and success/error handling.
 *
 * Responsibilities:
 * - Handle login form validation and submission
 * - Coordinate with userService for authentication
 * - Manage session creation on successful login
 * - Redirect users to user area after authentication
 *
 * @see {userService}
 * @see {sessionService}
 * @see {validator}
 * @see {errorService}
 */
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
    return validator.checkIfInputProvided('Username', formData.username) &&
        validator.checkIfInputProvided('Password', formData.password);
}

function handleLoginSuccess(userData) {
    sessionService.setActiveSession(userData);
    toast.showSuccessAndRedirect(SUCCESS_MESSAGES.LOGIN, ROUTES.USER_AREA)
}