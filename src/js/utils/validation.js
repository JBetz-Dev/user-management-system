import {VALIDATION_RULES, ERROR_MESSAGES} from "./constants.js";
import {toast} from "../components/toast.js";

/**
 * Client-side input validation utility with user feedback integration.
 * Validates user input against defined rules and displays appropriate error messages.
 *
 * Responsibilities:
 * - Validate user input fields (username, email, password)
 * - Check for required field presence
 * - Display validation error messages via toast notifications
 * - Mirror backend validation rules for consistency
 *
 * @see {toast}
 * @see {VALIDATION_RULES}
 */
class Validator {

    validateInput(fieldName, fieldValue) {
        if (!this.checkIfInputProvided(fieldName, fieldValue)) {
            return false;
        }

        let errorMessage;
        let validatorMethod;

        switch (fieldName) {
            case "Username":
                validatorMethod = this.#isValidUsername;
                errorMessage = ERROR_MESSAGES.VALIDATION.INVALID_USERNAME;
                break;
            case "Email":
                validatorMethod = this.#isValidEmail;
                errorMessage = ERROR_MESSAGES.VALIDATION.INVALID_EMAIL;
                break;
            case "Password":
                validatorMethod = this.#isValidPassword;
                errorMessage = ERROR_MESSAGES.VALIDATION.INVALID_PASSWORD;
                break;
        }

        if (!validatorMethod(fieldValue)) {
            toast.show("error", `Invalid ${fieldName} Provided`, `${errorMessage}`);
            return false;
        }
        return true;
    }

    checkIfInputProvided(fieldName, fieldValue) {
        if (!fieldValue.trim()) {
            toast.show("error",
                `${fieldName} Required`,
                ERROR_MESSAGES.INPUT.NO_VALUE_PROVIDED(fieldName)
            );
            return false;
        }
        return true;
    }

    #isValidUsername(username) {
        return VALIDATION_RULES.USERNAME.REGEX.test(username);
    }

    #isValidEmail(email) {
        return VALIDATION_RULES.EMAIL.REGEX.test(email);
    }

    #isValidPassword(password) {
        return VALIDATION_RULES.PASSWORD.REGEX.test(password);
    }
}

export const validator = new Validator();
