import {API_CONFIG} from "../utils/constants.js";

/**
 * Client-side user service for API communication and user operations.
 * Handles all HTTP requests to the backend user management endpoints.
 *
 * Responsibilities:
 * - Execute user authentication and registration requests
 * - Handle profile update operations (email, password changes)
 * - Manage user data retrieval and listing
 * - Process API responses and error handling
 * - Coordinate with backend RESTful API endpoints
 *
 * @see {sessionService}
 * @see {errorService}
 */
class UserService {

    async login(userData) {
        let url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.LOGIN}`;

        return this.#getResponse(url, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData)
        });
    }

    async logout() {
        let url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.LOGOUT}`;

        return this.#getResponse(url, {
            method: "POST",
            credentials: "include"
        });
    }

    async register(userData) {
        let url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.USERS}`;

        return this.#getResponse(url, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData)
        });
    }

    async getAllUsers() {
        let url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.USERS}`;

        return this.#getResponse(url, {
            method: "GET",
            credentials: "include",
            headers: {"Accept": "application/json"}
        });
    }

    async changePassword(userData) {
        let url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.CHANGE_PASSWORD(userData.id)}`;

        return this.#getResponse(url, {
            method: "PATCH",
            credentials: "include",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData)
        });
    }

    async changeEmail(userData) {
        let url = `${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.CHANGE_EMAIL(userData.id)}`;

        return this.#getResponse(url, {
            method: "PATCH",
            credentials: "include",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(userData)
        });
    }

    async #getResponse(url, request) {
        const response = await fetch(url, request);

        if (response.ok) {
            return response.json();
        } else {
            const errorData = await response.json();
            throw new Error(errorData.error || "unknown_error");
        }
    }
}

export const userService = new UserService();