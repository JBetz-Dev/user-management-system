import {VALIDATION_RULES} from "../utils/constants.js";
import {createFormIcon} from "./forms/formElements.js";

class PasswordRequirementsToggler {
    build() {
        return this.#createPasswordRequirementsDiv();
    }

    #createPasswordRequirementsDiv() {
        const passwordRequirements = document.createElement('div');
        passwordRequirements.classList.add('password-requirements');

        const passwordRequirementsToggle = this.#createPasswordRequirementsToggle();
        passwordRequirements.appendChild(passwordRequirementsToggle);

        const passwordRequirementsContent = this.#createPasswordRequirementsContent();
        passwordRequirements.appendChild(passwordRequirementsContent);

        passwordRequirementsToggle.addEventListener('click', () => {
            passwordRequirementsContent.classList.toggle('expanded');
        });

        return passwordRequirements;
    }

    #createPasswordRequirementsToggle() {
        const passwordRequirementsToggle = document.createElement('div');
        passwordRequirementsToggle.setAttribute('id', 'password-requirements-toggle');
        passwordRequirementsToggle.classList.add('requirements-toggle');

        const span = document.createElement('span');
        span.classList.add('requirements-toggle-text');
        span.textContent = 'Password Requirements';
        passwordRequirementsToggle.appendChild(span);

        const icon = createFormIcon(
            'requirements-icon', ['fas', 'fa-chevron-down', 'requirements-icon']);
        passwordRequirementsToggle.appendChild(icon);

        passwordRequirementsToggle.addEventListener('click', () => {
            icon.classList.toggle('expanded');
        });

        return passwordRequirementsToggle;
    }

    #createPasswordRequirementsContent() {
        const requirementsContent = document.createElement('div');
        requirementsContent.setAttribute('id', 'requirements-content');
        requirementsContent.classList.add('requirements-content');

        const requirementsList = document.createElement('ul');

        const requirements = VALIDATION_RULES.PASSWORD.REQUIREMENTS;
        Object.values(requirements).forEach(requirement => {
            const requirementListItem = this.#createRequirementsItem(requirement);
            requirementsList.appendChild(requirementListItem);
        });

        requirementsContent.appendChild(requirementsList);

        return requirementsContent;
    }

    #createRequirementsItem(spanText) {
        const requirementsItem = document.createElement('li');
        requirementsItem.classList.add('requirements-item');

        const icon = createFormIcon(
            'requirements-icon', ['fas', 'fa-circle', 'requirements-item-icon']);
        requirementsItem.appendChild(icon);

        const span = document.createElement('span');
        span.textContent = spanText;
        requirementsItem.appendChild(span);

        return requirementsItem;
    }
}

export const passwordRequirementsToggler = new PasswordRequirementsToggler();