import {ROUTES, SUCCESS_MESSAGES} from "../utils/constants.js";
import {toast} from "./toast.js";
import {userService} from "../services/userService.js";
import {sessionService} from "../services/sessionService.js";
import {errorService} from "../services/errorService.js";

class Navigation {

    initialize() {
        let userProfile = sessionService.getActiveSession();

        if (userProfile) {
            this.showActiveSessionNavBar();
        } else {
            this.showDefaultNavBar();
        }
    }

    showActiveSessionNavBar() {
        document.getElementById('login-btn').classList.add('hidden');
        document.getElementById('sign-up-btn').classList.add('hidden');
        document.getElementById('logout-btn').classList.remove('hidden');
        document.getElementById('user-area-btn').classList.remove('hidden');
        document.getElementById('profile-btn').classList.remove('hidden');

        this.#setupLogoutEventListener();
    }

    showDefaultNavBar() {
        document.getElementById('login-btn').classList.remove('hidden');
        document.getElementById('sign-up-btn').classList.remove('hidden');
        document.getElementById('logout-btn').classList.add('hidden');
        document.getElementById('user-area-btn').classList.add('hidden');
        document.getElementById('profile-btn').classList.add('hidden');
    }

    #setupLogoutEventListener() {
        const logoutButton = document.getElementById('logout-btn');

        if (!logoutButton.hasAttribute('data-logout-handler')) {
            logoutButton.addEventListener('click', () => {
                userService.logout()
                    .catch(error => errorService.handleLogoutError(error))
                    .finally(() => {
                        sessionService.clearActiveSession();
                        toast.showSuccessAndRedirect(SUCCESS_MESSAGES.LOGOUT, ROUTES.HOME);
                    });
            });

            logoutButton.setAttribute('data-logout-handler', 'true');
        }
    }
}

export const navigation = new Navigation();