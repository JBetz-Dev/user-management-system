import {changePasswordForm} from "../components/forms/changePasswordForm.js";
import {changeEmailForm} from "../components/forms/changeEmailForm.js";
import {modal} from "../components/modal.js";
import {sessionService} from "../services/sessionService.js";

/**
 * User profile page script for profile management and updates.
 * Coordinates profile display and change form modal interactions.
 *
 * Responsibilities:
 * - Display current user profile information
 * - Handle profile update modal triggers
 * - Populate profile data from session storage
 * - Coordinate with change form components
 *
 * @see {changePasswordForm}
 * @see {changeEmailForm}
 * @see {modal}
 * @see {sessionService}
 */
const userProfileContainer = document.getElementById('user-profile');
const changePasswordButton = document.getElementById('change-password-btn');
const changeEmailButton = document.getElementById('change-email-btn');

if (changePasswordButton) {
    changePasswordButton.addEventListener('click', () => {
        modal.show('Change Password', changePasswordForm.build())
    });
}

if (changeEmailButton) {
    changeEmailButton.addEventListener('click', () => {
        modal.show('Change Email', changeEmailForm.build())
    });
}

if (userProfileContainer) {
    let userData = sessionService.getActiveSession();

    if (userData) {
        populateProfile(userData);
    }
}

function populateProfile(userData) {
    document.getElementById('profile-username').textContent = userData.username;
    document.getElementById('profile-email').textContent = userData.email;
}