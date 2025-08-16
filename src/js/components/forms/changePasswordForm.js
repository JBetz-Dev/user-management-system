import {ERROR_MESSAGES, ROUTES, SUCCESS_MESSAGES} from "../../utils/constants.js";
import {createFormGroup, createFormSubmitButton} from "./formElements.js";
import {passwordRequirementsToggler} from "../passwordRequirementsToggler.js";
import {validator} from "../../utils/validation.js";
import {toast} from "../toast.js";
import {modal} from "../modal.js";
import {userService} from "../../services/userService.js";
import {sessionService} from "../../services/sessionService.js";
import {errorService} from "../../services/errorService.js";

class ChangePasswordForm {
    build() {
        return this.#createChangePasswordForm();
    }

    /* Form Creation */
    #createChangePasswordForm() {
        const changePasswordForm = document.createElement('form');
        changePasswordForm.setAttribute('id', 'change-password-form');
        changePasswordForm.setAttribute('novalidate', '');

        const currentPasswordFormGroup = createFormGroup(
            'current-password', 'password', 'Current Password', 'Enter your current password'
        );
        changePasswordForm.appendChild(currentPasswordFormGroup);

        const passwordRequirements = passwordRequirementsToggler.build();

        const newPasswordFormGroup = createFormGroup(
            'new-password', 'password', 'New Password',
            'Enter your new password', [passwordRequirements]
        );
        changePasswordForm.appendChild(newPasswordFormGroup);

        const submitButton = createFormSubmitButton(
            ['fas', 'fa-user-plus'], 'Change Password');
        changePasswordForm.appendChild(submitButton);

        if (changePasswordForm) {
            changePasswordForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.#handleChangePassword();
            });
        }

        return changePasswordForm;
    }

    /* Form Logic */
    #handleChangePassword() {
        const formData = this.#getPasswordFormData();
        const userData = sessionService.getActiveSession();

        if (userData.id === null) {
            errorService.showSessionExpiredError();
            return;
        }


        if (this.#validatePasswordFormData(formData)) {
            formData.id = userData.id;

            userService.changePassword(formData)
                .then(data => this.#handlePasswordChangeSuccess(data))
                .catch(error => errorService.handleProfileUpdateError(error));
        }
    }

    #getPasswordFormData() {
        return {
            currentPassword: document.getElementById('current-password').value,
            newPassword: document.getElementById('new-password').value
        };
    }

    #validatePasswordFormData(formData) {
        if (formData.newPassword === formData.currentPassword) {
            toast.show("error", "Passwords Must Be Different", ERROR_MESSAGES.VALIDATION.PASSWORD_SAME_AS_CURRENT);
            return false;
        }

        return validator.checkIfInputProvided('Password', formData.currentPassword) &&
            validator.validateInput('Password', formData.newPassword);
    }

    #handlePasswordChangeSuccess(userData) {
        sessionService.clearActiveSession(userData);
        modal.closeAll();
        toast.showSuccessAndRedirect(SUCCESS_MESSAGES.PASSWORD_CHANGE, ROUTES.LOGIN);
    }
}

export const changePasswordForm = new ChangePasswordForm();