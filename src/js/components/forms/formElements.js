/**
 * Utility functions for creating reusable form elements with consistent styling and behavior.
 * Provides standardized form component creation for dynamic form building.
 *
 * Responsibilities:
 * - Create form groups with labels and inputs
 * - Generate form submit buttons with icons
 * - Provide consistent form element structure and styling
 * - Support child element injection for extended functionality
 *
 * @see {changeEmailForm}
 * @see {changePasswordForm}
 * @see {passwordRequirementsToggler}
 */
export function createFormGroup(inputId, type, labelText, placeholder, childElements = []) {
    const formGroup = document.createElement('div');
    formGroup.classList.add('form-group');

    const label = document.createElement('label');
    label.setAttribute('for', inputId);
    label.textContent = labelText;
    formGroup.appendChild(label);

    const input = document.createElement('input');
    input.setAttribute('id', inputId);
    input.setAttribute('type', type);
    input.setAttribute('placeholder', placeholder);
    input.classList.add('form-input');
    formGroup.appendChild(input);

    if (childElements.length > 0) {
        childElements.forEach(child => formGroup.appendChild(child));
    }

    return formGroup;
}

export function createFormSubmitButton(iconClasses, buttonText) {
    const submitButton = document.createElement('button');
    submitButton.setAttribute('type', 'submit');
    submitButton.classList.add('form-btn');

    const icon = document.createElement('i');
    iconClasses.forEach(iconClass => icon.classList.add(iconClass));
    submitButton.appendChild(icon);

    submitButton.textContent = ` ${buttonText}`;

    return submitButton;
}

export function createFormIcon(id, iconClasses) {
    const icon = document.createElement('i');
    icon.setAttribute('id', id);
    iconClasses.forEach(iconClass => icon.classList.add(iconClass));

    return icon;
}