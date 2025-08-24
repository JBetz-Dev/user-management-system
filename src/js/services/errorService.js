import {ERROR_MESSAGES, ROUTES} from "../utils/constants.js";
import {toast} from "../components/toast.js";
import {modal} from "../components/modal.js";

class ErrorService {

    handleLoginError(error) {
        switch (error.message) {
            case "authentication_failed":
                this.showLoginFailedModal();
                break;
            default:
                this.handleRequestError(error);
                break;
        }
    }

    handleLogoutError(error) {
        console.warn("Error logging out (local session cleared): " + error.message);
    }

    handleRegistrationError(error) {
        switch (error.message) {
            case "user_already_exists":
                this.showRegistrationFailedModal("username");
                break;
            case "email_already_exists":
                this.showRegistrationFailedModal("email");
                break;
            default:
                this.handleRequestError(error);
                break;
        }
    }

    handleProfileUpdateError(error) {
        switch (error.message) {
            case "authentication_failed":
                this.showAuthenticationFailedToast();
                break;
            case "user_already_exists":
                this.showProfileUpdateErrorToast("username");
                break;
            case "email_already_exists":
                this.showProfileUpdateErrorToast("email");
                break;
            default:
                this.handleRequestError(error);
                break;
        }
    }

    handleRequestError(error) {
        switch (error.message) {
            case "session_not_found":
            case "session_user_mismatch":
                this.showSessionExpiredModal();
                break;
            case "invalid_input":
                this.showInvalidInputToast();
                break;
            default:
                this.showGenericErrorToast();
                break;
        }
    }

    showLoginFailedModal() {
        modal.show("Login Failed", ERROR_MESSAGES.AUTH.LOGIN_FAILED, {
            confirmText: "Register",
            onConfirm: () => window.location.href = ROUTES.REGISTER
        });
    }

    showRegistrationFailedModal(fieldName) {
        modal.show("Registration Failed", ERROR_MESSAGES.AUTH.ALREADY_EXISTS(fieldName), {
            confirmText: "Log In",
            onConfirm: () => window.location.href = ROUTES.LOGIN
        });
    }

    showSessionExpiredModal() {
        modal.show("Session Expired", ERROR_MESSAGES.AUTH.SESSION_EXPIRED, {
            confirmText: "Log In",
            onConfirm: () => window.location.href = ROUTES.LOGIN
        });
    }

    showAuthenticationFailedToast() {
        toast.show("error", "Authentication Failed", ERROR_MESSAGES.AUTH.UNSUCCESSFUL);
    }

    showProfileUpdateErrorToast(fieldName) {
        let field = fieldName.charAt(0).toUpperCase() + fieldName.slice(1);
        toast.show("error", `${field} Already Exists`, ERROR_MESSAGES.AUTH.ALREADY_EXISTS(fieldName));
    }

    showInvalidInputToast() {
        toast.show("error", "Invalid Input", ERROR_MESSAGES.INPUT.INVALID);
    }

    showGenericErrorToast() {
        toast.show("error", "Oops!", ERROR_MESSAGES.DEFAULT);
    }
}

export const errorService = new ErrorService();