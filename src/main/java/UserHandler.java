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

        Map<String, String> unauthenticatedUser = JsonUserParser.mapJsonToUserFields(requestBody);
        User savedUser = userService.getUserByUsername(unauthenticatedUser.get("username"));

        if (savedUser == null) {
            response.setStatusCode(404);
        } else if (savedUser.verifyPassword(unauthenticatedUser.get("password"))) {
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

        User user = userService.registerNewUser(JsonUserParser.mapJsonToUser(requestBody));
        if (user != null) {
            response.setStatusCode(201);
            response.setReasonPhrase("Created");

            String sessionId = SessionManager.setActiveSession(user);
            String cookieString = "sessionId=" + sessionId + "; Path=/; Max-Age=3600";
            response.setHeader("Set-Cookie", cookieString);

            response.setBody(JsonUserParser.mapUserToJson(user));
        } else {
            response.setStatusCode(409);
        }
    }

    private void handleChangePassword() {
        String requestBody = request.getBody();

        Map<String, String> unauthenticatedUser = JsonUserParser.mapJsonToUserFields(requestBody);
        int userId = Integer.parseInt(unauthenticatedUser.get("id"));
        String currentPassword = unauthenticatedUser.get("currentPassword");
        String newPassword = unauthenticatedUser.get("newPassword");

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
}