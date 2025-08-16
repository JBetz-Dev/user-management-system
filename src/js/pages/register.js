import {ROUTES, SUCCESS_MESSAGES} from "../utils/constants.js";
import {Validator} from "../utils/validation.js"
import {Toast} from "../components/toast.js";
import {userService} from "../services/userService.js";
import {sessionService} from "../services/sessionService.js";
import {errorService} from "../services/errorService.js";
import {passwordRequirementsToggler} from "../components/passwordRequirementsToggler.js";

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
    }
}

function validateRegistrationFormData(formData) {
    return Validator.validateInput('Username', formData.username) &&
        Validator.validateInput('Email', formData.email) &&
        Validator.validateInput('Password', formData.password)
}

function handleRegisterSuccess(userData) {
    sessionService.setActiveSession(userData);
    Toast.showSuccessAndRedirect(SUCCESS_MESSAGES.REGISTRATION, ROUTES.USER_AREA)
}