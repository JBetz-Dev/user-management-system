import { TOAST_CONFIG } from "../utils/constants.js";

class ToastComponent {
    constructor() {
        this.counter = 0;
        this.container = null;
    }

    show(type = "info", title = "Notification", message = "", duration = TOAST_CONFIG.DEFAULT_DURATION) {
        const toastId = `toast-${++this.counter}`;
        this.#ensureContainer();

        const toast = this.#createToast(toastId, type, title, message, duration);
        this.container.appendChild(toast);

        if (duration > 0) {
            this.#setupProgressBar(toast, duration);
            setTimeout(() => {
                this.remove(toastId);
            }, duration);
        }

        return toastId;
    }

    showSuccessAndRedirect(message, redirectUrl, delay = 500) {
        this.show("success", "Success!", message);
        setTimeout(() => {
            window.location.href = redirectUrl;
        }, delay);
    }

    remove(toastId) {
        const toast = document.getElementById(toastId);

        if (toast && !toast.classList.contains("removing")) {
            toast.classList.add("removing");
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                    this.#cleanupContainer();
                }
            }, 300);
        }
    }

    removeAll() {
        if (this.container) {
            const toasts = this.container.querySelectorAll(".toast");
            toasts.forEach(toast => {
                if (!toast.classList.contains("removing")) {
                    this.remove(toast.id);
                }
            });
        }
    }

    #ensureContainer() {
        if (!this.container) {
            this.container = document.createElement("div");
            this.container.className = "toast-container";
            this.container.id = "toastContainer";
            document.body.appendChild(this.container);
        }
    }

    #createToast(toastId, type, title, message, duration) {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.id = toastId;

        const toastIcon = this.#createToastIcon(type);
        toast.appendChild(toastIcon);

        const toastContent = this.#createToastContent(title, message);
        toast.appendChild(toastContent);

        const toastCloseButton = this.#createToastCloseButton(toastId);
        toast.appendChild(toastCloseButton);

        return toast;
    }

    #createToastIcon(type) {
        const toastIcon = document.createElement('div');
        toastIcon.className = "toast-icon";

        const icon = document.createElement('i');
        icon.className = `${TOAST_CONFIG.ICONS[type.toUpperCase()] || TOAST_CONFIG.ICONS.INFO}`;
        toastIcon.appendChild(icon);

        return toastIcon;
    }

    #createToastContent(title, message) {
        const toastContent = document.createElement('div');
        toastContent.className = "toast-content";

        const toastTitle = document.createElement('div');
        toastTitle.className = "toast-title";
        toastTitle.textContent = title;
        toastContent.appendChild(toastTitle);

        if (message) {
            const messageContainer = document.createElement('div');
            messageContainer.className = "toast-message";
            messageContainer.textContent = message;
            toastContent.appendChild(messageContainer);
        }

        return toastContent;
    }

    #createToastCloseButton(toastId) {
        const toastCloseButton = document.createElement('button');
        toastCloseButton.className = "toast-close";

        const icon = document.createElement('i');
        icon.className = "fas fa-times";
        toastCloseButton.appendChild(icon);

        toastCloseButton.addEventListener('click', () => {
            this.remove(toastId);
        });

        return toastCloseButton;
    }

    #setupProgressBar(toast, duration) {
        const progressBar = document.createElement('div');
        progressBar.className = "toast-progress";
        progressBar.style.width = "100%";
        toast.appendChild(progressBar);

        setTimeout(() => {
            progressBar.style.width = "0%";
            progressBar.style.transitionDuration = `${duration}ms`;
        }, 10);
    }

    #cleanupContainer() {
        if (this.container && this.container.children.length === 0) {
            this.container.remove();
            this.container = null;
        }
    }
}

export const Toast = new ToastComponent();