import java.util.List;

public class UserHandler {
    private final HttpRequest request;
    private final HttpResponse response;
    private final UserService userService = new UserService();

    public UserHandler(HttpRequest request) {
        this.request = request;
        response = new HttpResponse();
    }

    public HttpResponse getResponse() {
        response.setVersion(request.getVersion());
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
        } else if (segmentsLength == 2 && method.equals("POST")) {
            handleRegisterNewUser();
        } else {
            response.setStatusCode(404);
            response.setReasonPhrase("Not Found");
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

        response.setBody(sb.toString());
        response.setStatusCode(200);
        response.setReasonPhrase("OK");
    }

    private void handleRegisterNewUser() {
        String requestBody = request.getBody();

        User user = JsonUserParser.mapJsonToUser(requestBody);
        user = userService.registerNewUser(user);
        if (user != null) {
            response.setBody(JsonUserParser.mapUserToJson(user));
            response.setStatusCode(201);
            response.setReasonPhrase("Created");
        } else {
            response.setStatusCode(409);
            response.setReasonPhrase("Conflict");
            response.setBody("{\"Error\": \"User already exists\"}");
        }
    }
}