import {changePasswordForm} from "../components/forms/changePasswordForm.js";
import {changeEmailForm} from "../components/forms/changeEmailForm.js";
import {Modal} from "../components/modal.js";
import {sessionService} from "../services/sessionService.js";

const userProfileContainer = document.getElementById('user-profile');
const changePasswordButton = document.getElementById('change-password-btn');
const changeEmailButton = document.getElementById('change-email-btn');

if (changePasswordButton) {
    changePasswordButton.addEventListener('click', () => {
        Modal.show('Change Password', changePasswordForm.build())
    });
}

if (changeEmailButton) {
    changeEmailButton.addEventListener('click', () => {
        Modal.show('Change Email', changeEmailForm.build())
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