import java.util.List;
import java.util.Map;

public class UserHandler {
    private final HttpRequest request;
    private final HttpResponse response;
    private final UserService userService;
    private SessionData sessionData;

    public UserHandler(HttpRequest request) {
        this.request = request;
        response = new HttpResponse();
        userService = new UserService();
    }

    public UserHandler(HttpRequest request, SessionData sessionData) {
        this.request = request;
        response = new HttpResponse();
        userService = new UserService();
        this.sessionData = sessionData;
    }

    public HttpResponse getResponse() {
        boolean hasActiveSession = sessionData != null && sessionData.isActive();

        response.setVersion("HTTP/1.1");
        response.setHeader("Content-Type", "application/json");

        String method = request.getMethod();
        String pathString = request.getPath();

        if (pathString.endsWith("/")) {
            pathString = pathString.substring(0, pathString.length() - 1);
        }

        String[] segments = pathString.split("/");
        int segmentsLength = segments.length;

        // Add additional endpoints
        if (segmentsLength == 2 && method.equals("GET")) {
            if (hasActiveSession) {
                handleGetAllUsers();
            } else {
                prepareSessionNotFoundResponse();
            }
        } else if (segmentsLength == 2 && method.equals("POST")) {
            handleRegisterNewUser();
        } else if (segmentsLength == 3 && segments[2].equals("login") && method.equals("POST")) {
            handleAuthenticateUser();
        } else if (segmentsLength == 3 && segments[2].equals("logout") && method.equals("POST")) {
            handleLogoutUser();
        } else if (segmentsLength == 4 && segments[3].equals("password") && method.equals("PATCH")) {
            if (hasActiveSession) {
                handleChangePassword();
            } else {
                prepareSessionNotFoundResponse();
            }
        } else if (segmentsLength == 4 && segments[3].equals("email") && method.equals("PATCH")) {
            if (hasActiveSession) {
                handleChangeEmail();
            } else {
                prepareSessionNotFoundResponse();
            }
        } else {
            preparePathNotFoundResponse();
        }

        return response;
    }

    private void handleGetAllUsers() {
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

        prepareSuccessfulResponse(200, "OK", sb.toString());
    }

    private void handleAuthenticateUser() {
        try {
            Map<String, String> requestFields = getExpectedRequestBodyFields(List.of("username", "password"));
            String username = requestFields.get("username");
            String password = requestFields.get("password");
            User savedUser = userService.getUserByUsername(username);

            if (savedUser == null) {
                prepareUsernameNotFoundResponse();
            } else if (savedUser.verifyPassword(password)) {
                setActiveSessionWithCookie(savedUser);
                prepareSuccessfulResponse(200, "OK", savedUser.toJson());
            } else {
                prepareInvalidPasswordResponse();
            }
        } catch (InvalidRequestFieldException e) {
            prepareInvalidInputResponse();
        }
    }

    private void handleLogoutUser() {
        User savedUser = sessionData.user();

        if (savedUser != null) {
            SessionManager.invalidateUserSessions(savedUser);
        }

        prepareSuccessfulResponse(200, "OK",
                "{\"message\": \"Logged out successfully\"}");
    }

    private void handleRegisterNewUser() {
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
                prepareSuccessfulResponse(201, "Created", registeredUser.toJson());
            } else {
                prepareUsernameAlreadyExistsResponse();
            }
        } catch (InvalidRequestFieldException e) {
            prepareInvalidInputResponse();
        }

    }

    private void handleChangePassword() {
        try {
            Map<String, String> requestFields = getExpectedRequestBodyFields(
                    List.of("id", "currentPassword", "newPassword"));
            User savedUser = sessionData.user();
            int userId = parseUserId(requestFields.get("id"));
            String currentPassword = requestFields.get("currentPassword");
            String newPassword = requestFields.get("newPassword");

            if (savedUser == null) {
                prepareSessionNotFoundResponse();
                return;
            }

            if (savedUser.getId() != userId) {
                prepareSessionUserMismatchResponse();
                return;
            }

            boolean passwordUpdated = userService.changePassword(savedUser, currentPassword, newPassword);

            if (passwordUpdated) {
                SessionManager.invalidateUserSessions(savedUser);
                setActiveSessionWithCookie(savedUser);
                prepareSuccessfulResponse(200, "OK", savedUser.toJson());
            } else {
                prepareInvalidPasswordResponse();
            }
        } catch (InvalidRequestFieldException e) {
            prepareInvalidInputResponse();
        }
    }

    private void handleChangeEmail() {
        try {
            Map<String, String> requestFields = getExpectedRequestBodyFields(
                    List.of("id", "newEmail", "password"));
            User savedUser = sessionData.user();
            int userId = parseUserId(requestFields.get("id"));
            String newEmail = requestFields.get("newEmail");
            String password = requestFields.get("password");

            if (savedUser == null) {
                prepareSessionNotFoundResponse();
                return;
            }

            if (savedUser.getId() != userId) {
                prepareSessionUserMismatchResponse();
                return;
            }

            boolean emailUpdated = userService.changeEmail(savedUser, newEmail, password);

            if (emailUpdated) {
                setActiveSessionWithCookie(savedUser);
                prepareSuccessfulResponse(200, "OK", savedUser.toJson());
            } else {
                prepareInvalidPasswordResponse();
            }
        } catch (InvalidRequestFieldException e) {
            prepareInvalidInputResponse();
        }
    }

    private void prepareSuccessfulResponse(int statusCode, String reasonPhrase, String responseBody) {
        response.setStatusCode(statusCode);
        response.setReasonPhrase(reasonPhrase);
        response.setBody(responseBody);
    }

    private void setActiveSessionWithCookie(User user) {
        String sessionId = SessionManager.setActiveSession(user);
        String cookieString = "sessionId=" + sessionId + "; Path=/; Max-Age=3600";
        response.setHeader("Set-Cookie", cookieString);
    }

    private void prepareSessionNotFoundResponse() {
        response.setStatusCode(401);
        response.setReasonPhrase("Unauthorized");
        String errorJson = createErrorJson("session_not_found", "No valid session found");
        response.setBody(errorJson);
    }

    private void prepareSessionUserMismatchResponse() {
        response.setStatusCode(403);
        response.setReasonPhrase("Forbidden");
        String errorJson = createErrorJson("session_user_mismatch", "The provided user does not match the current session user");
        response.setBody(errorJson);
    }

    private void preparePathNotFoundResponse() {
        response.setStatusCode(404);
        response.setReasonPhrase("Not Found");
        String errorJson = createErrorJson("path_not_found", "Requested path not found");
        response.setBody(errorJson);
    }

    private void prepareUsernameNotFoundResponse() {
        response.setStatusCode(404);
        response.setReasonPhrase("Not Found");
        String errorJson = createErrorJson("username_not_found", "Requested username not found");
        response.setBody(errorJson);
    }

    private void prepareUsernameAlreadyExistsResponse() {
        response.setStatusCode(409);
        response.setReasonPhrase("Conflict");
        String errorJson = createErrorJson("username_already_exists", "Requested username already exists");
        response.setBody(errorJson);
    }

    private void prepareInvalidInputResponse() {
        response.setStatusCode(400);
        response.setReasonPhrase("Bad Request");
        String errorJson = createErrorJson("invalid_input", "Invalid input provided");
        response.setBody(errorJson);
    }

    private void prepareInvalidPasswordResponse() {
        response.setStatusCode(401);
        response.setReasonPhrase("Unauthorized");
        String errorJson = createErrorJson("invalid_password", "Invalid password provided");
        response.setBody(errorJson);
    }

    private String createErrorJson(String error, String message) {
        return String.format("{\"error\": \"%s\", \"message\": \"%s\"}", error, message);
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