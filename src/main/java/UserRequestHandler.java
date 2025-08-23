import java.util.List;
import java.util.Map;

/**
 * Handles HTTP requests for user management operations including authentication,
 * registration, and profile updates. Provides RESTful JSON API endpoints.
 * <p>
 * Responsibilities:
 * - Coordinate with UserRouter for request routing and UserService for domain operations
 * - Validate user sessions and enforce authentication requirements
 * - Generate JSON responses with appropriate HTTP status codes
 * - Handle request validation and field parsing
 * - Manage user session creation and cookie handling
 * <p>
 * Design decisions:
 * - Uses UserRouter for clean separation of routing logic from business logic
 * - Session requirements determined by route metadata for consistent auth handling
 * - Uses HttpResponseBuilder for consistent response construction
 * - Self-contained error handling with JSON responses appropriate for API clients
 * - Session management integrated with response cookie handling
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
            return getErrorResponse("session_not_found");
        }

        return switch (route) {
            case LOGIN -> handleAuthenticateUser();
            case LOGOUT -> handleLogoutUser();
            case REGISTER -> handleRegisterNewUser();
            case CHANGE_PASSWORD -> handleChangePassword();
            case CHANGE_EMAIL -> handleChangeEmail();
            case GET_ALL_USERS -> handleGetAllUsers();
            default -> getErrorResponse("path_not_found");
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
    }

    private HttpResponse handleAuthenticateUser() {
        try {
            Map<String, String> requestFields = getExpectedRequestBodyFields(List.of("username", "password"));
            String username = requestFields.get("username");
            String password = requestFields.get("password");
            User savedUser = userService.getUserByUsername(username);

            if (savedUser == null) {
                return getErrorResponse("username_not_found");
            } else if (savedUser.verifyPassword(password)) {
                setActiveSessionWithCookie(savedUser);
                return getSuccessfulResponse(200, savedUser.toJson());
            } else {
                return getErrorResponse("invalid_password");
            }
        } catch (InvalidRequestFieldException e) {
            return getErrorResponse("invalid_input");
        }
    }

    private HttpResponse handleLogoutUser() {
        User savedUser = activeSession.user();

        if (savedUser != null) {
            SessionManager.invalidateUserSessions(savedUser);
        }

        return getSuccessfulResponse(200,"{\"message\": \"Logged out successfully\"}");
    }

    private HttpResponse handleRegisterNewUser() {
        try {
            Map<String, String> requestFields = getExpectedRequestBodyFields(
                    List.of("username", "email", "password"));
            String username = requestFields.get("username");
            String email = requestFields.get("email");
            String password = requestFields.get("password");

            User registeredUser = userService.registerNewUser(
                    new User(username, email, password));

            if (registeredUser != null) {
                setActiveSessionWithCookie(registeredUser);
                return getSuccessfulResponse(201, registeredUser.toJson());
            } else {
                return getErrorResponse("username_already_exists");
            }
        } catch (InvalidRequestFieldException e) {
            return getErrorResponse("invalid_input");
        }

    }

    private HttpResponse handleChangePassword() {
        try {
            Map<String, String> requestFields = getExpectedRequestBodyFields(
                    List.of("id", "currentPassword", "newPassword"));
            User savedUser = activeSession.user();
            int userId = parseUserId(requestFields.get("id"));
            String currentPassword = requestFields.get("currentPassword");
            String newPassword = requestFields.get("newPassword");

            if (savedUser == null) {
                return getErrorResponse("session_not_found");
            }

            if (savedUser.getId() != userId) {
                return getErrorResponse("session_user_mismatch");
            }

            boolean passwordUpdated = userService.changePassword(savedUser, currentPassword, newPassword);

            if (passwordUpdated) {
                SessionManager.invalidateUserSessions(savedUser);
                setActiveSessionWithCookie(savedUser);
                return getSuccessfulResponse(200, savedUser.toJson());
            } else {
                return getErrorResponse("invalid_password");
            }
        } catch (InvalidRequestFieldException e) {
            return getErrorResponse("invalid_input");
        }
    }

    private HttpResponse handleChangeEmail() {
        try {
            Map<String, String> requestFields = getExpectedRequestBodyFields(
                    List.of("id", "newEmail", "password"));
            User savedUser = activeSession.user();
            int userId = parseUserId(requestFields.get("id"));
            String newEmail = requestFields.get("newEmail");
            String password = requestFields.get("password");

            if (savedUser == null) {
                return getErrorResponse("session_not_found");
            }

            if (savedUser.getId() != userId) {
                return getErrorResponse("session_user_mismatch");
            }

            boolean emailUpdated = userService.changeEmail(savedUser, newEmail, password);

            if (emailUpdated) {
                setActiveSessionWithCookie(savedUser);
                return getSuccessfulResponse(200, savedUser.toJson());
            } else {
                return getErrorResponse("invalid_password");
            }
        } catch (InvalidRequestFieldException e) {
            return getErrorResponse("invalid_input");
        }
    }

    private void setActiveSessionWithCookie(User user) {
        String sessionId = SessionManager.setActiveSession(user);
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

    private HttpResponse getErrorResponse(String error) {
        int statusCode;
        String message;

        switch (error) {
            case "session_not_found": {
                statusCode = 401;
                message = "No valid session found";
                break;
            }
            case "session_user_mismatch": {
                statusCode = 403;
                message = "The provided user does not match the current session user";
                break;
            }
            case "path_not_found": {
                statusCode = 404;
                message = "Requested path not found";
                break;
            }
            case "username_not_found": {
                statusCode = 404;
                message = "Requested username not found";
                break;
            }
            case "username_already_exists": {
                statusCode = 409;
                message = "Requested username already exists";
                break;
            }
            case "invalid_input": {
                statusCode = 400;
                message = "Invalid input provided";
                break;
            }
            case "invalid_password": {
                statusCode = 401;
                message = "Invalid password provided";
                break;
            }
            default: {
                statusCode = 500;
                message = "Unknown error";
                break;
            }
        }

        String responseBody = String.format("{\"error\": \"%s\", \"message\": \"%s\"}", error, message);

        return responseBuilder.version("HTTP/1.1")
                .status(statusCode)
                .header("Content-Type", "application/json")
                .body(responseBody)
                .build();
    }

    private Map<String, String> getExpectedRequestBodyFields(List<String> expectedFields)
            throws InvalidRequestFieldException, NumberFormatException {
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