import {SESSION_STORAGE_KEYS} from "../utils/constants.js";

/**
 * Client-side session management service for user authentication state.
 * Handles session storage operations and user data persistence in browser.
 *
 * Responsibilities:
 * - Store and retrieve user session data from sessionStorage
 * - Validate active session status
 * - Clear session data on logout
 * - Provide consistent session data access across components
 *
 * @see {userService}
 * @see {navigation}
 */
class SessionService {

    getActiveSession() {
        const userData = JSON.parse(sessionStorage.getItem(SESSION_STORAGE_KEYS.USER));

        if (userData && userData.active === true) {
            return userData;
        }

        return null;
    }

    setActiveSession(userData) {
        this.clearActiveSession();
        userData.active = true;
        sessionStorage.setItem(SESSION_STORAGE_KEYS.USER, JSON.stringify(userData));
    }

    clearActiveSession() {
        sessionStorage.removeItem(SESSION_STORAGE_KEYS.USER);
    }
}

export const sessionService = new SessionService();