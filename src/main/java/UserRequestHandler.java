import java.util.List;
import java.util.Map;

/**
 * Handles HTTP requests for user management operations including authentication,
 * registration, and profile updates. Provides RESTful JSON API endpoints.
 * <p>
 * Responsibilities:
 * - Route user-specific requests to appropriate business logic handlers
 * - Coordinate with UserService for domain operations
 * - Manage user session creation and validation
 * - Generate JSON responses with appropriate HTTP status codes
 * - Handle request validation and field parsing
 * <p>
 * Design decisions:
 * - Uses HttpResponseBuilder for consistent response construction
 * - Self-contained error handling with JSON responses appropriate for API clients
 * - Session management integrated with response cookie handling
 * - Path-based routing using segment analysis for RESTful endpoint matching
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
        String cookie = request.getHeader("Cookie");

        if (cookie != null) {
            activeSession = SessionManager.getActiveSession(cookie);
        }

        boolean hasActiveSession = activeSession != null;

        String method = request.getMethod();
        String pathString = request.getPath();

        if (pathString.endsWith("/")) {
            pathString = pathString.substring(0, pathString.length() - 1);
        }

        String[] segments = pathString.split("/");
        int segmentsLength = segments.length;

        // Endpoints
        if (segmentsLength == 2 && method.equals("GET")) {
            if (hasActiveSession) {
                return handleGetAllUsers();
            } else {
                return getErrorResponse("session_not_found");
            }
        } else if (segmentsLength == 2 && method.equals("POST")) {
            return handleRegisterNewUser();
        } else if (segmentsLength == 3 && segments[2].equals("login") && method.equals("POST")) {
            return handleAuthenticateUser();
        } else if (segmentsLength == 3 && segments[2].equals("logout") && method.equals("POST")) {
            return handleLogoutUser();
        } else if (segmentsLength == 4 && segments[3].equals("password") && method.equals("PATCH")) {
            if (hasActiveSession) {
                return handleChangePassword();
            } else {
                return getErrorResponse("session_not_found");
            }
        } else if (segmentsLength == 4 && segments[3].equals("email") && method.equals("PATCH")) {
            if (hasActiveSession) {
                return handleChangeEmail();
            } else {
                return getErrorResponse("session_not_found");
            }
        } else {
            return getErrorResponse("path_not_found");
        }
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