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
            Map<String, String> requestFields = getExpectedRequestBodyFields(List.of("username", "password"));
            String username = requestFields.get("username");
            String password = requestFields.get("password");

            User authenticatedUser = userService.authenticateUser(username, password);
            setActiveSessionWithCookie(authenticatedUser.getId());

            return getSuccessfulResponse(200, authenticatedUser.toJson());
        } catch (UserService.UserAuthenticationException e) {
            return getErrorResponse(401, "authentication_failed");
        } catch (InvalidRequestFieldException e) {
            return getErrorResponse(400, "invalid_input");
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
            Map<String, String> requestFields = getExpectedRequestBodyFields(
                    List.of("username", "email", "password"));
            String username = requestFields.get("username");
            String email = requestFields.get("email");
            String password = requestFields.get("password");

            User registeredUser = userService.registerNewUser(username, email, password);
            setActiveSessionWithCookie(registeredUser.getId());

            return getSuccessfulResponse(201, registeredUser.toJson());
        } catch (UserService.UserAlreadyExistsException e) {
            return getErrorResponse(409, "user_already_exists");
        } catch (InvalidRequestFieldException e) {
            return getErrorResponse(400, "invalid_input");
        } catch (SQLException e) {
            return getErrorResponse(500, "database_error");
        }
    }

    private HttpResponse handleChangePassword() {
        if (activeSession == null) {
            return getErrorResponse(401, "session_not_found");
        }

        try {
            Map<String, String> requestFields = getExpectedRequestBodyFields(
                    List.of("id", "currentPassword", "newPassword"));
            int userId = parseUserId(requestFields.get("id"));
            String currentPassword = requestFields.get("currentPassword");
            String newPassword = requestFields.get("newPassword");
            int sessionUserId = activeSession.userId();

            if (userId != sessionUserId) {
                return getErrorResponse(403, "session_user_mismatch");
            }

            User updatedUser = userService.changePassword(userId, currentPassword, newPassword);
            SessionManager.invalidateUserSessions(userId);
            setActiveSessionWithCookie(userId);

            return getSuccessfulResponse(200, updatedUser.toJson());
        } catch (UserService.UserAuthenticationException e) {
            return getErrorResponse(401, "authentication_failed");
        } catch (InvalidRequestFieldException e) {
            return getErrorResponse(400, "invalid_input");
        } catch (SQLException e) {
            return getErrorResponse(500, "database_error");
        }
    }

    private HttpResponse handleChangeEmail() {
        if (activeSession == null) {
            return getErrorResponse(401, "session_not_found");
        }

        try {
            Map<String, String> requestFields = getExpectedRequestBodyFields(
                    List.of("id", "newEmail", "password"));
            int userId = parseUserId(requestFields.get("id"));
            String newEmail = requestFields.get("newEmail");
            String password = requestFields.get("password");
            int sessionUserId = activeSession.userId();

            if (userId != sessionUserId) {
                return getErrorResponse(403, "session_user_mismatch");
            }

            User updatedUser = userService.changeEmail(userId, newEmail, password);
            SessionManager.invalidateUserSessions(userId);
            setActiveSessionWithCookie(userId);

            return getSuccessfulResponse(200, updatedUser.toJson());
        } catch (UserService.UserAuthenticationException e) {
            return getErrorResponse(401, "authentication_failed");
        } catch (InvalidRequestFieldException e) {
            return getErrorResponse(400, "invalid_input");
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

    private Map<String, String> getExpectedRequestBodyFields(List<String> expectedFields)
            throws InvalidRequestFieldException {
        Map<String, String> providedFields = JsonUtil.parseJsonToFieldMap(request.getBody());

        for (String expectedField : expectedFields) {
            String providedField = providedFields.get(expectedField);
            if (providedField == null || providedField.trim().isEmpty()) {
                throw new InvalidRequestFieldException("Expected field not found: " + expectedField);
            }
        }

        return providedFields;
    }

    private int parseUserId(String userId) throws InvalidRequestFieldException {
        int parsedUserId;

        try {
            parsedUserId = Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            throw new InvalidRequestFieldException("Invalid user id: " + userId);
        }

        return parsedUserId;
    }

    private static class InvalidRequestFieldException extends Exception {
        InvalidRequestFieldException(String message) {
            super(message);
        }
    }
}