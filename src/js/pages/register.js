import {ROUTES, SUCCESS_MESSAGES} from "../utils/constants.js";
import {validator} from "../utils/validation.js"
import {toast} from "../components/toast.js";
import {userService} from "../services/userService.js";
import {sessionService} from "../services/sessionService.js";
import {errorService} from "../services/errorService.js";
import {passwordRequirementsToggler} from "../components/passwordRequirementsToggler.js";

/**
 * Registration page script handling new user account creation.
 * Manages registration form submission and password requirements display.
 *
 * Responsibilities:
 * - Handle registration form validation and submission
 * - Display password requirements for user guidance
 * - Coordinate with userService for account creation
 * - Manage session creation and redirect on successful registration
 *
 * @see {userService}
 * @see {sessionService}
 * @see {validator}
 * @see {passwordRequirementsToggler}
 */
const registerForm = document.getElementById('sign-up-form');
const passwordFormGroup = document.getElementById('password-form-group');

if (registerForm) {
    registerForm.addEventListener('submit', handleRegister);
}

if (passwordFormGroup) {
    passwordFormGroup.appendChild(passwordRequirementsToggler.build());
}

function handleRegister(e) {
    e.preventDefault();
    const userData = getRegistrationFormData();

    if (validateRegistrationFormData(userData)) {
        userService.register(userData)
            .then(userData => handleRegisterSuccess(userData))
            .catch(error => errorService.handleRegistrationError(error));
    }
}

function getRegistrationFormData() {
    return {
        username: document.getElementById('sign-up-username').value,
        email: document.getElementById('sign-up-email').value,
        password: document.getElementById('sign-up-password').value
    };
}

function validateRegistrationFormData(formData) {
    return validator.validateInput('Username', formData.username) &&
        validator.validateInput('Email', formData.email) &&
        validator.validateInput('Password', formData.password);
}

function handleRegisterSuccess(userData) {
    sessionService.setActiveSession(userData);
    toast.showSuccessAndRedirect(SUCCESS_MESSAGES.REGISTRATION, ROUTES.USER_AREA);
}