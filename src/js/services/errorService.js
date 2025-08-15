import {ERROR_MESSAGES, ROUTES} from "../utils/constants";
import {Toast} from "../components/toast";
import {Modal} from "../components/modal";

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
        Modal.show("Login Failed", ERROR_MESSAGES.AUTH.LOGIN_FAILED, {
            confirmText: "Register",
            onConfirm: () => window.location.href = ROUTES.REGISTER
        });
    }

    showRegistrationError() {
        Modal.show("Registration Failed", ERROR_MESSAGES.AUTH.USERNAME_ALREADY_EXISTS, {
            confirmText: "Log In",
            onConfirm: () => window.location.href = ROUTES.LOGIN
        });
    }

    showSessionExpiredError() {
        Modal.show("Session Expired", ERROR_MESSAGES.AUTH.SESSION_EXPIRED, {
            confirmText: "Log In",
            onConfirm: () => window.location.href = ROUTES.LOGIN
        });
    }

    showInvalidPasswordError() {
        Toast.show("error", "Invalid Password", ERROR_MESSAGES.AUTH.INVALID_PASSWORD);
    }

    showInvalidInputError() {
        Toast.show("error", "Invalid Input", ERROR_MESSAGES.INPUT.INVALID);
    }

    showGenericError() {
        Toast.show("error", "Oops!", ERROR_MESSAGES.DEFAULT);
    }
}

export const errorService = new ErrorService();