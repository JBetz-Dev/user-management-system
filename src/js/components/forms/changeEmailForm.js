import {SUCCESS_MESSAGES} from "../../utils/constants.js";
import {validator} from "../../utils/validation.js";
import {modal} from "../modal.js";
import {toast} from "../toast.js";
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
        return validator.checkIfInputProvided('Password', formData.password) &&
            validator.validateInput('Email', formData.newEmail);
    }

    #handleEmailChangeSuccess(userData) {
        sessionService.setActiveSession(userData);
        modal.closeAll();
        toast.show('success', 'Success!', SUCCESS_MESSAGES.EMAIL_CHANGE);
        document.getElementById('profile-email').textContent = userData.email;
    }
}

export const changeEmailForm = new ChangeEmailForm();