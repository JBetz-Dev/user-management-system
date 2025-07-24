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
                response.setStatusCode(401);
            }
        } else if (segmentsLength == 2 && method.equals("POST")) {
            handleRegisterNewUser();
        } else if (segmentsLength == 3 && segments[2].equals("login") && method.equals("POST")) {
            handleAuthenticateUser();
        } else if (segmentsLength == 4 && segments[3].equals("password") && method.equals("PATCH")) {
            if (hasActiveSession) {
                handleChangePassword();
            } else {
                response.setStatusCode(401);
            }
        } else if (segmentsLength == 4 && segments[3].equals("email") && method.equals("PATCH")) {
            if (hasActiveSession) {
                handleChangeEmail();
            } else {
                response.setStatusCode(401);
            }
        } else {
            response.setStatusCode(404);
        }

        return response;
    }

    private void handleGetAllUsers() {
        List<User> users = userService.getAllUsers();
        StringBuilder sb = new StringBuilder();

        sb.append("[");
        if (!users.isEmpty()) {
            for (User user : users) {
                String userJson = JsonUserParser.mapUserToJson(user);
                sb.append(userJson).append(",");
            }

            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("]");

        response.setStatusCode(200);
        response.setReasonPhrase("OK");
        response.setBody(sb.toString());
    }

    private void handleAuthenticateUser() {
        String requestBody = request.getBody();

        Map<String, String> providedFields = JsonUserParser.parseJsonToFieldMap(requestBody);
        String username = providedFields.get("username");
        String password = providedFields.get("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            response.setStatusCode(400);
            return;
        }

        User savedUser = userService.getUserByUsername(username);

        if (savedUser == null) {
            response.setStatusCode(404);
        } else if (savedUser.verifyPassword(password)) {
            response.setStatusCode(200);
            response.setReasonPhrase("OK");

            String sessionId = SessionManager.setActiveSession(savedUser);
            String cookieString = "sessionId=" + sessionId + "; Path=/; Max-Age=3600";
            response.setHeader("Set-Cookie", cookieString);

            response.setBody(JsonUserParser.mapUserToJson(savedUser));
        } else {
            response.setStatusCode(401);
        }
    }

    private void handleRegisterNewUser() {
        String requestBody = request.getBody();

        User user = JsonUserParser.mapJsonToUser(requestBody);

        if (user == null) {
            response.setStatusCode(400);
            return;
        }

        User registeredUser = userService.registerNewUser(user);

        if (registeredUser != null) {
            response.setStatusCode(201);
            response.setReasonPhrase("Created");

            String sessionId = SessionManager.setActiveSession(registeredUser);
            String cookieString = "sessionId=" + sessionId + "; Path=/; Max-Age=3600";
            response.setHeader("Set-Cookie", cookieString);

            response.setBody(JsonUserParser.mapUserToJson(registeredUser));
        } else {
            response.setStatusCode(409);
        }
    }

    private void handleChangePassword() {
        String requestBody = request.getBody();
        Map<String, String> providedFields = JsonUserParser.parseJsonToFieldMap(requestBody);

        String idString = providedFields.get("id");
        String currentPassword = providedFields.get("currentPassword");
        String newPassword = providedFields.get("newPassword");

        if (idString == null || idString.isEmpty() || currentPassword == null ||
            currentPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            response.setStatusCode(400);
            return;
        }

        int userId;

        try {
            userId = Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            response.setStatusCode(400);
            return;
        }

        User savedUser = sessionData.user();

        if (savedUser.getId() != userId) {
            response.setStatusCode(403);
            return;
        }

        boolean passwordUpdated = userService.changePassword(savedUser, currentPassword, newPassword);

        if (passwordUpdated) {
            response.setStatusCode(200);
            response.setReasonPhrase("OK");

            SessionManager.invalidateUserSessions(savedUser);
            String sessionId = SessionManager.setActiveSession(savedUser);
            String cookieString = "sessionId=" + sessionId + "; Path=/; Max-Age=3600";
            response.setHeader("Set-Cookie", cookieString);

            response.setBody(JsonUserParser.mapUserToJson(savedUser));
        } else {
            response.setStatusCode(401);
        }
    }

    private void handleChangeEmail() {
        String requestBody = request.getBody();
        Map<String, String> providedFields = JsonUserParser.parseJsonToFieldMap(requestBody);

        String idString = providedFields.get("id");
        String password = providedFields.get("password");
        String newEmail = providedFields.get("newEmail");

        if (idString == null || idString.isEmpty() || newEmail == null ||
            newEmail.isEmpty() || password == null || password.isEmpty()) {
            response.setStatusCode(400);
            return;
        }

        int userId;

        try {
            userId = Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            response.setStatusCode(400);
            return;
        }

        User savedUser = sessionData.user();

        if (savedUser.getId() != userId) {
            response.setStatusCode(403);
            return;
        }

        boolean emailUpdated = userService.changeEmail(savedUser, newEmail, password);

        if (emailUpdated) {
            response.setStatusCode(200);
            response.setReasonPhrase("OK");

            String sessionId = SessionManager.setActiveSession(savedUser);
            String cookieString = "sessionId=" + sessionId + "; Path=/; Max-Age=3600";
            response.setHeader("Set-Cookie", cookieString);

            response.setBody(JsonUserParser.mapUserToJson(savedUser));
        } else {
            response.setStatusCode(401);
        }
    }
}