/**
 * Application constants and configuration values.
 * Centralizes API endpoints, validation rules, messages, and application settings.
 *
 * Responsibilities:
 * - Define API endpoints and routing configuration
 * - Store validation regex patterns and requirements
 * - Provide standardized success and error messages
 * - Configure toast and application behavior settings
 * - Ensure consistency across frontend components
 */
export const API_CONFIG = {
    BASE_URL: "http://localhost:9000",
    ENDPOINTS: {
        USERS: "/users/",
        LOGIN: "/users/login/",
        LOGOUT: "/users/logout/",
        CHANGE_PASSWORD: (userId) => `/users/${userId}/password/`,
        CHANGE_EMAIL: (userId) => `/users/${userId}/email/`
    }
};

export const ROUTES = {
    HOME: "index.html",
    LOGIN: "login.html",
    REGISTER: "register.html",
    USER_AREA: "user-area.html",
    PROFILE: "profile.html"
};

export const SESSION_STORAGE_KEYS = {
    USER: "user"
};

export const VALIDATION_RULES = {
    USERNAME: {
        REGEX: /^[a-zA-Z0-9._-]{4,25}$/
    },
    EMAIL: {
        REGEX: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,50}$/
    },
    PASSWORD: {
        REGEX: /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>])[A-Za-z\d!@#$%^&*(),.?":{}|<>]{8,40}$/,
        REQUIREMENTS: {
            LENGTH: "Between 8 and 40 characters long",
            CASE: "Contains at least one uppercase and one lowercase letter",
            NUMBER: "Contains at least one number",
            SPECIAL_CHARACTER: "Contains at least one special character"
        }
    }
}

export const TOAST_CONFIG = {
    DEFAULT_DURATION: 5000,
    ICONS: {
        SUCCESS: 'fas fa-check-circle',
        ERROR: 'fas fa-exclamation-circle',
        WARNING: 'fas fa-exclamation-triangle',
        INFO: 'fas fa-info-circle'
    }
};

export const SUCCESS_MESSAGES = {
    LOGIN: "Logged in successfully!",
    LOGOUT: "Logged out successfully!",
    REGISTRATION: "Registered successfully!",
    PASSWORD_CHANGE: "Password changed successfully - please log in again to continue",
    EMAIL_CHANGE: "Email changed successfully"
};

export const ERROR_MESSAGES = {
    VALIDATION: {
        INVALID_USERNAME: "Username must be between 4-25 characters - please try again",
        INVALID_EMAIL: "Invalid email provided - please try again",
        INVALID_PASSWORD: "Password does not meet the complexity requirements - please try again",
        SAME_AS_CURRENT: (fieldName) => {
            let field = fieldName.toLowerCase();
            return `Your new ${field} must be different from your current ${field} - please try again`
        }
    },
    AUTH: {
        LOGIN_FAILED: "Login unsuccessful - please try again or register as a new user",
        UNSUCCESSFUL: "Authentication unsuccessful - please try again",
        SESSION_EXPIRED: "Your session has expired - please log in again to continue",
        ALREADY_EXISTS: (fieldName) => {
            let field = fieldName.toLowerCase();
            return `The requested ${field} already exists - please try a different ${field}`
        }
    },
    INPUT: {
        NO_VALUE_PROVIDED: (fieldName) => `You must enter a ${fieldName.toLowerCase()} - please try again`,
        INVALID: "Invalid input provided - please try again"
    },
    DEFAULT: "Something went wrong - please try again"
};