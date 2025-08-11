import {TOAST_CONFIG} from "../utils/constants.js";

class ToastComponent {
    constructor() {
        this.counter = 0;
        this.container = null; //let container = document.getElementById("toastContainer");
    }

    show(type = "info", title = "Notification", message = "", duration = TOAST_CONFIG.DEFAULT_DURATION) {
        const toastId = `toast-${++this.counter}`;
        this.#ensureContainer();

        const toast = this.#createToastElement(toastId, type, title, message, duration);
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

    clear() {
        if (this.container) {
            const toasts = this.container.querySelectorAll(".toast");
            toasts.forEach(toast => {
                if (!toast.classList.contains("removing")) {
                    removeToast(toast.id);
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

    #createToastElement(toastId, type, title, message, duration) {
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.id = toastId;

        const toastIcon = document.createElement('div');
        toastIcon.className = "toast-icon";
        toastIcon.innerHTML = `
            <i class="${TOAST_CONFIG.ICONS[type.toUpperCase()] || TOAST_CONFIG.ICONS.INFO}"></i>
        `;
        toast.appendChild(toastIcon);

        const toastContent = document.createElement('div');
        toastContent.className = "toast-content";

        const toastTitle = document.createElement('div');
        toastTitle.className = "toast-title";
        toastTitle.innerText = title;
        toastContent.appendChild(toastTitle);

        if (message) {
            const messageContainer = document.createElement('div');
            messageContainer.className = "toast-message";
            messageContainer.innerText = message;
            toastContent.appendChild(messageContainer);
        }

        toast.appendChild(toastContent);

        const toastCloseButton = document.createElement('button');
        toastCloseButton.className = "toast-close";
        toastCloseButton.innerHTML = `<i class="fas fa-times"></i>`;
        toastCloseButton.addEventListener('click', (e) => {
            e.preventDefault();
            this.remove(toastId);
        });
        toast.appendChild(toastCloseButton);

        if (duration > 0) {
            const toastProgress = document.createElement('div');
            toastProgress.className = "toast-progress";
            toastProgress.style.width = "100%";
            toast.appendChild(toastProgress);
        }

        return toast;
    }

    #setupProgressBar(toast, duration) {
        const progressBar = toast.querySelector(".toast-progress");

        if (progressBar) {
            setTimeout(() => {
                progressBar.style.width = "0%";
                progressBar.style.transitionDuration = `${duration}ms`;
            }, 10);
        }
    }

    #cleanupContainer() {
        if (this.container && this.container.children.length === 0) {
            this.container.remove();
            this.container = null;
        }
    }
}

export const Toast = new ToastComponent();