import {SUCCESS_MESSAGES} from "../../utils/constants.js";
import {Validator} from "../../utils/validation.js";
import {Modal} from "../modal.js";
import {Toast} from "../toast.js";
import {sessionService} from "../../services/sessionService.js";
import {errorService} from "../../services/errorService.js";
import {userService} from "../../services/userService.js";
import {createFormGroup, createFormSubmitButton} from "./formElements.js";

class ChangeEmailForm {
    build() {
        return this.#createChangeEmailForm();
    }

    #createChangeEmailForm() {
        const changeEmailForm = document.createElement('form');
        changeEmailForm.setAttribute('novalidate', '');

        const emailFormGroup = createFormGroup(
            'new-email', 'email', 'New Email', 'Enter your new email'
        );
        changeEmailForm.appendChild(emailFormGroup);

        const passwordFormGroup = createFormGroup(
            'password', 'password', 'Password', 'Enter your password'
        );
        changeEmailForm.appendChild(passwordFormGroup);

        const submitButton = createFormSubmitButton(
            ['fas', 'fa=user-plus'], 'Change Email'
        );
        changeEmailForm.appendChild(submitButton);

        changeEmailForm.addEventListener('submit', (e) => {
            e.preventDefault();
            this.#handleChangeEmail();
        });

        return changeEmailForm;
    }

    #handleChangeEmail() {
        const formData = this.#getEmailFormData();
        const userData = sessionService.getActiveSession();

        if (userData === null) {
            errorService.showSessionExpiredError();
            return;
        }

        if (this.#validateEmailFormData(formData)) {
            userData.password = formData.password;
            userData.newEmail = formData.newEmail;

            userService.changeEmail(userData)
                .then(userData => this.#handleEmailChangeSuccess(userData))
                .catch(error => errorService.handleProfileUpdateError(error));
        }
    }

    #getEmailFormData() {
        return {
            password: document.getElementById('password').value,
            newEmail: document.getElementById('new-email').value
        };
    }

    #validateEmailFormData(formData) {
        return Validator.checkIfInputProvided('Password', formData.password) &&
            Validator.validateInput('Email', formData.newEmail);
    }

    #handleEmailChangeSuccess(userData) {
        sessionService.setActiveSession(userData);
        Modal.closeAll();
        Toast.show('success', 'Success!', SUCCESS_MESSAGES.EMAIL_CHANGE);
        document.getElementById('profile-email').textContent = userData.email;
    }
}

export const changeEmailForm = new ChangeEmailForm();