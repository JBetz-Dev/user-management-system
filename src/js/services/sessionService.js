import {SESSION_STORAGE_KEYS} from "../utils/constants.js";

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