import java.util.List;
import java.util.Map;

public class UserHandler {
    private final HttpRequest request;
    private final HttpResponse response;
    private final UserService userService;

    public UserHandler(HttpRequest request) {
        this.request = request;
        response = new HttpResponse();
        userService = new UserService();
    }

    public HttpResponse getResponse() {
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
            handleGetAllUsers();
        } else if (segmentsLength == 3 && segments[2].equals("login") && method.equals("POST")) {
            handleAuthenticateUser();
        } else if (segmentsLength == 3 && segments[2].equals("register") && method.equals("POST")) {
            handleRegisterNewUser();
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
            for (User user : userService.getAllUsers()) {
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
            response.setBody(JsonUserParser.mapUserToJson(savedUser));
        } else {
            response.setStatusCode(401);
        }
    }

    private void handleRegisterNewUser() {
        String requestBody = request.getBody();

        User user = JsonUserParser.mapJsonToUser(requestBody);
        user = userService.registerNewUser(user);
        if (user != null) {
            response.setStatusCode(201);
            response.setReasonPhrase("Created");
            response.setBody(JsonUserParser.mapUserToJson(user));
        } else {
            response.setStatusCode(409);
        }
    }
}