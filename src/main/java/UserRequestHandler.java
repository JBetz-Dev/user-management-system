import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Handles HTTP requests for user management operations including authentication,
 * registration, and profile updates. Provides RESTful JSON API endpoints.
 * <p>
 * Responsibilities:
 * - Coordinate with UserRouter for request routing and UserService for domain operations
 * - Validate user sessions and enforce authentication requirements
 * - Transform business/database exceptions into appropriate HTTP status codes and error responses
 * - Handle request validation, field parsing, and JSON response formatting
 * - Manage user session lifecycle including creation, validation, and invalidation
 * <p>
 * Design decisions:
 * - Uses UserRouter for clean separation of routing logic from business logic
 * - Session requirements determined by route metadata for consistent auth handling
 * - Uses HttpResponseBuilder for consistent response construction
 * - Centralized error handling maps business/database exceptions to HTTP responses
 * - Session management integrated with response cookie handling
 * <p>
 * Session integration:
 * - Works with minimal SessionData (userId + expiry) to avoid stale data
 * - Session validation occurs before route execution for protected endpoints
 * - Session invalidation and renewal handled for security-sensitive operations
 *
 * @see UserRouter
 * @see UserService
 * @see SessionManager
 */
public class UserRequestHandler {
    private final HttpRequest request;
    private final HttpResponseBuilder responseBuilder;
    private final UserService userService;
    private SessionData activeSession;

    public UserRequestHandler(HttpRequest request) {
        this.request = request;
        responseBuilder = new HttpResponseBuilder();
        userService = new UserService();
    }

    public HttpResponse getResponse() {
        boolean hasActiveSession = setupSessionIfCookie();
        UserRoute route = new UserRouter().getRoute(request.getMethod(), request.getPath());

        if (route.requiresSession() && !hasActiveSession) {
            return getErrorResponse(401, "session_not_found");
        }

        return switch (route) {
            case LOGIN -> handleAuthenticateUser();
            case LOGOUT -> handleLogoutUser();
            case REGISTER -> handleRegisterNewUser();
            case CHANGE_PASSWORD -> handleChangePassword();
            case CHANGE_EMAIL -> handleChangeEmail();
            case GET_ALL_USERS -> handleGetAllUsers();
            default -> getErrorResponse(404, "path_not_found");
        };
    }

    private boolean setupSessionIfCookie() {
        String cookie = request.getHeader("Cookie");

        if (cookie != null) {
            activeSession = SessionManager.getActiveSession(cookie);
        }

        return activeSession != null;
    }

    private HttpResponse handleGetAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            StringBuilder sb = new StringBuilder();

            sb.append("[");
            if (!users.isEmpty()) {
                for (User user : users) {
                    String userJson = user.toJson();
                    sb.append(userJson).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("]");

            return getSuccessfulResponse(200, sb.toString());
        } catch (SQLException e) {
            return getErrorResponse(500, "database_error");
        }
    }

    private HttpResponse handleAuthenticateUser() {
        try {
            Map<String, String> requestFields = JsonUtil.parseJsonWithRequiredFields(
                    request.getBody(), List.of("username", "password"));

            User authenticatedUser = userService.authenticateUser(
                    requestFields.get("username"),
                    requestFields.get("password")
            );
            setActiveSessionWithCookie(authenticatedUser.getId());

            return getSuccessfulResponse(200, authenticatedUser.toJson());
        } catch (IllegalArgumentException e) {
            return getErrorResponse(400, "invalid_input");
        } catch (UserService.UserAuthenticationException e) {
            return getErrorResponse(401, "authentication_failed");
        } catch (SQLException e) {
            return getErrorResponse(500, "database_error");
        }
    }

    private HttpResponse handleLogoutUser() {
        if (activeSession != null) {
            SessionManager.invalidateUserSessions(activeSession.userId());
        }

        return getSuccessfulResponse(200, "{\"message\": \"Logged out successfully\"}");
    }

    private HttpResponse handleRegisterNewUser() {
        try {
            Map<String, String> requestFields = JsonUtil.parseJsonWithRequiredFields(
                    request.getBody(), List.of("username", "email", "password"));

            User registeredUser = userService.registerNewUser(
                    requestFields.get("username"),
                    requestFields.get("email"),
                    requestFields.get("password")
            );
            setActiveSessionWithCookie(registeredUser.getId());

            return getSuccessfulResponse(201, registeredUser.toJson());
        } catch (IllegalArgumentException e) {
            return getErrorResponse(400, "invalid_input");
        } catch (UserService.UserAlreadyExistsException e) {
            return getErrorResponse(409, "user_already_exists");
        } catch (UserService.EmailAlreadyExistsException e) {
            return getErrorResponse(409, "email_already_exists");
        } catch (SQLException e) {
            return getErrorResponse(500, "database_error");
        }
    }

    private HttpResponse handleChangePassword() {
        if (activeSession == null) {
            return getErrorResponse(401, "session_not_found");
        }

        try {
            Map<String, String> requestFields = JsonUtil.parseJsonWithRequiredFields(
                    request.getBody(), List.of("id", "currentPassword", "newPassword"));
            int userId = Integer.parseInt(requestFields.get("id"));

            if (userId != activeSession.userId()) {
                return getErrorResponse(403, "session_user_mismatch");
            }

            User updatedUser = userService.changePassword(
                    userId,
                    requestFields.get("currentPassword"),
                    requestFields.get("newPassword")
            );
            SessionManager.invalidateUserSessions(userId);
            setActiveSessionWithCookie(userId);

            return getSuccessfulResponse(200, updatedUser.toJson());
        } catch (IllegalArgumentException e) {
            return getErrorResponse(400, "invalid_input");
        } catch (UserService.UserAuthenticationException e) {
            return getErrorResponse(401, "authentication_failed");
        } catch (SQLException e) {
            return getErrorResponse(500, "database_error");
        }
    }

    private HttpResponse handleChangeEmail() {
        if (activeSession == null) {
            return getErrorResponse(401, "session_not_found");
        }

        try {
            Map<String, String> requestFields = JsonUtil.parseJsonWithRequiredFields(
                    request.getBody(), List.of("id", "newEmail", "password"));
            int userId = Integer.parseInt(requestFields.get("id"));

            if (userId != activeSession.userId()) {
                return getErrorResponse(403, "session_user_mismatch");
            }

            User updatedUser = userService.changeEmail(
                    userId,
                    requestFields.get("newEmail"),
                    requestFields.get("password")
            );
            SessionManager.invalidateUserSessions(userId);
            setActiveSessionWithCookie(userId);

            return getSuccessfulResponse(200, updatedUser.toJson());
        } catch (IllegalArgumentException e) {
            return getErrorResponse(400, "invalid_input");
        } catch (UserService.UserAuthenticationException e) {
            return getErrorResponse(401, "authentication_failed");
        } catch (UserService.EmailAlreadyExistsException e) {
            return getErrorResponse(409, "email_already_exists");
        } catch (SQLException e) {
            return getErrorResponse(500, "database_error");
        }
    }

    private void setActiveSessionWithCookie(int userId) {
        String sessionId = SessionManager.setActiveSession(userId);
        String cookieString = "sessionId=" + sessionId + "; Path=/; Max-Age=3600";
        responseBuilder.header("Set-Cookie", cookieString);
    }

    private HttpResponse getSuccessfulResponse(int statusCode, String responseBody) {
        return responseBuilder.version("HTTP/1.1")
                .status(statusCode)
                .header("Content-Type", "application/json")
                .body(responseBody)
                .build();
    }

    private HttpResponse getErrorResponse(int statusCode, String error) {
        String message;

        switch (error) {
            case "authentication_failed" -> message = "Authentication failed";
            case "session_not_found" -> message = "No valid session found";
            case "session_user_mismatch" -> message = "The provided user does not match the current session user";
            case "path_not_found" -> message = "The requested path was not found";
            case "user_already_exists" -> message = "User already exists";
            case "invalid_input" -> message = "Invalid input provided";
            case "database_error" -> message = "Database error";
            default -> message = "Unknown error";
        }

        String responseBody = String.format("{\"error\": \"%s\", \"message\": \"%s\"}", error, message);

        return responseBuilder.version("HTTP/1.1")
                .status(statusCode)
                .header("Content-Type", "application/json")
                .body(responseBody)
                .build();
    }
}