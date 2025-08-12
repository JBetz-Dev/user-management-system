import { VALIDATION_RULES, ERROR_MESSAGES } from "../utils/constants.js";
import { Toast } from "./toast.js";

class ValidatorUtil {

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
            Toast.show("error", `Invalid ${fieldName} Provided`, `${errorMessage}`);
            return false;
        }
        return true;
    }

    checkIfInputProvided(fieldName, fieldValue) {
        if (!fieldValue.trim()) {
            Toast.show("error",
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

export const Validator = new ValidatorUtil();
