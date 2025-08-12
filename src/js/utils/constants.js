export const API_CONFIG = {
    BASE_URL: "http://localhost:9000",
    ENDPOINTS: {
        USERS: "/users/",
        LOGIN: "/users/login/",
        LOGOUT: "/users/logout/",
        CHANGE_PASSWORD: (userId) => `/users/${userId}/password/`,
        CHANGE_EMAIL: (email) => `/users/${email}/email/`
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
        REGEX: /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>])[A-Za-z\d!@#$%^&*(),.?":{}|<>]{8,40}$/
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
        USERNAME_ALREADY_EXISTS: "Username already exist - please log in or try a different username",
        INVALID_EMAIL: "Invalid email provided - please try again",
        EMAIL_ALREADY_EXISTS: "Email already exists - please log in or try a different email",
        INVALID_PASSWORD: "Password does not meet the complexity requirements - please try again",
        PASSWORD_SAME_AS_CURRENT: "Your new password must be different from your old password - please try again"
    },
    AUTH: {
        LOGIN_FAILED: "Login unsuccessful - please try again or register as a new user",
        INVALID_PASSWORD: "Invalid password - please try again",
        SESSION_EXPIRED: "Your session has expired - please log in again to continue"
    },
    INPUT: {
        NO_VALUE_PROVIDED: (fieldName) => `You must enter a ${fieldName.toLowerCase()} - please try again`,
        INVALID: "Invalid input provided - please try again"
    },
    DEFAULT: "Something went wrong - please try again"
};