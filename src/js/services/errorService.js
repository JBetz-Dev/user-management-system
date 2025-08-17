import {ERROR_MESSAGES, ROUTES} from "../utils/constants.js";
import {toast} from "../components/toast.js";
import {modal} from "../components/modal.js";

class ErrorService {

    handleRequestError(error) {
        switch (error.message) {
            case "session_not_found":
            case "session_user_mismatch":
                this.showSessionExpiredError();
                break;
            default:
                this.showGenericError();
                break;
        }
    }

    handleLoginError(error) {
        switch (error.message) {
            case "username_not_found":
            case "invalid_password":
                this.showLoginError();
                break;
            case "invalid_input":
                this.showInvalidInputError();
                break;
            default:
                this.showGenericError();
                break;
        }
    }

    handleLogoutError(error) {
        console.warn("Error logging out (local session cleared): " + error.message);
    }

    handleRegistrationError(error) {
        switch (error.message) {
            case "username_already_exists":
            case "invalid_password":
                this.showRegistrationError();
                break;
            case "invalid_input":
                this.showInvalidInputError();
                break;
            default:
                this.showGenericError();
                break;
        }
    }

    handleProfileUpdateError(error) {
        switch (error.message) {
            case "session_not_found":
            case "session_user_mismatch":
                this.showSessionExpiredError();
                break;
            case "invalid_password":
                this.showInvalidPasswordError();
                break;
            case "invalid_input":
                this.showInvalidInputError();
                break;
            default:
                this.showGenericError();
                break;
        }
    }

    showLoginError() {
        modal.show("Login Failed", ERROR_MESSAGES.AUTH.LOGIN_FAILED, {
            confirmText: "Register",
            onConfirm: () => window.location.href = ROUTES.REGISTER
        });
    }

    showRegistrationError() {
        modal.show("Registration Failed", ERROR_MESSAGES.AUTH.USERNAME_ALREADY_EXISTS, {
            confirmText: "Log In",
            onConfirm: () => window.location.href = ROUTES.LOGIN
        });
    }

    showSessionExpiredError() {
        modal.show("Session Expired", ERROR_MESSAGES.AUTH.SESSION_EXPIRED, {
            confirmText: "Log In",
            onConfirm: () => window.location.href = ROUTES.LOGIN
        });
    }

    showInvalidPasswordError() {
        toast.show("error", "Invalid Password", ERROR_MESSAGES.AUTH.INVALID_PASSWORD);
    }

    showInvalidInputError() {
        toast.show("error", "Invalid Input", ERROR_MESSAGES.INPUT.INVALID);
    }

    showGenericError() {
        toast.show("error", "Oops!", ERROR_MESSAGES.DEFAULT);
    }
}

export const errorService = new ErrorService();