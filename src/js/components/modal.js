/**
 * Reusable modal dialog component with flexible content and callback support.
 * Provides programmatic modal creation with customizable actions and styling.
 *
 * Responsibilities:
 * - Create and manage modal dialogs dynamically
 * - Handle modal lifecycle (show, hide, cleanup)
 * - Support flexible content (string HTML or DOM elements)
 * - Provide callback hooks for user interactions (onShow, onConfirm, onClose)
 */
class Modal {

    show(title, content, options = {}) {
        const modalId = options.id || "modal" + Date.now();

        const modal = this.#createModal(modalId, title, content, options);
        document.body.appendChild(modal);
        setTimeout(() => modal.classList.add('show'), 10);

        if (options.onShow) {
            setTimeout(() => options.onShow(), 20);
        }

        return modalId;
    }

    close(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.remove();
        }
    }

    closeAll() {
        const modals = document.querySelectorAll('.modal');
        modals.forEach(modal => {
            modal.remove();
        })
    }

    #createModal(modalId, title, content, options = {}) {
        const modal = document.createElement('div');
        modal.classList.add('modal');
        modal.id = modalId;

        const modalContent = document.createElement('div');
        modalContent.classList.add('modal-content');
        modal.appendChild(modalContent);

        const modalHeader = this.#createModalHeader(modalId, title, options);
        modalContent.appendChild(modalHeader);

        const modalBody = this.#createModalBody(content);
        modalContent.appendChild(modalBody);

        const modalFooter = this.#createModalFooter(modalId, options);
        modalContent.appendChild(modalFooter);

        return modal;
    }

    #createModalHeader(modalId, title, options) {
        const modalHeader = document.createElement('div');
        modalHeader.classList.add('modal-header');

        const modalTitle = document.createElement('h5');
        modalTitle.classList.add('modal-title');
        modalTitle.textContent = title;
        modalHeader.appendChild(modalTitle);

        const modalTitleButton = document.createElement('button');
        modalTitleButton.type = "button";
        modalTitleButton.classList.add('modal-close-x');
        modalTitleButton.innerHTML = "&times;";
        modalTitleButton.addEventListener('click', () => {
            this.#handleClose(modalId, options);
        });
        modalHeader.appendChild(modalTitleButton);

        return modalHeader;
    }

    #createModalBody(content) {
        const modalBody = document.createElement('div');
        modalBody.classList.add('modal-body');

        if (typeof content === 'string') {
            modalBody.innerHTML = content;
        } else {
            modalBody.appendChild(content);
        }

        return modalBody;
    }

    #createModalFooter(modalId, options) {
        const modalFooter = document.createElement('div');
        modalFooter.classList.add('modal-footer');

        const buttonContainer = document.createElement('div');
        buttonContainer.classList.add('btn-container');

        const closeButton = this.#createCloseButton(modalId, options);
        buttonContainer.appendChild(closeButton);

        if (options.confirmText) {
            const confirmButton = this.#createConfirmButton(modalId, options);
            buttonContainer.appendChild(confirmButton);
        }

        modalFooter.appendChild(buttonContainer);

        return modalFooter;
    }

    #createCloseButton(modalId, options) {
        const closeButton = document.createElement('button');
        closeButton.type = "button";
        closeButton.classList.add('btn-secondary');
        closeButton.textContent = "Close";
        closeButton.addEventListener('click', () => {
            this.#handleClose(modalId, options);
        });

        return closeButton;
    }

    #createConfirmButton(modalId, options) {
        const confirmButton = document.createElement('button');
        confirmButton.type = "button";
        confirmButton.classList.add('btn-primary');
        confirmButton.textContent = options.confirmText;
        confirmButton.addEventListener('click', () => {
            this.#handleConfirm(modalId, options);
        });

        return confirmButton;
    }

    #handleClose(modalId, options) {
        if (options.onClose) {
            options.onClose();
        }

        this.close(modalId);
    }

    #handleConfirm(modalId, options) {
        if (options.onConfirm) {
            options.onConfirm();
        }

        this.close(modalId);
    }
}

export const modal = new Modal();