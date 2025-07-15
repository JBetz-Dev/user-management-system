import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class UserHandler {
    private HttpRequest request;
    private HttpResponse response;
    private UserService userService;

    public UserHandler(HttpRequest request) {
        this.request = request;
        userService = new UserService();
        processRequest();
    }

    private void processRequest() {
        String method = request.getMethod();
        String pathString = request.getPath();
        String version = request.getVersion();
        String requestBody = request.getBody();
        int statusCode = 400;
        String reasonPhrase = "Bad Request";
        String responseBody = "Bad Request";
        String contentType = "application/json";

        System.out.println("HTTP Request:\n" + request);
        // Trim trailing "/" for standardization
        if (pathString.endsWith("/")) {
            pathString = pathString.substring(0, pathString.length() - 1);
        }

        String[] segments = pathString.split("/");
        int segmentsLength = segments.length;

        if (segmentsLength == 2 && method.equals("GET")) {
            System.out.println("getAllUsers()");
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (User user : userService.getAllUsers()) {
                String userJson = JsonUserParser.mapUserToJson(user);
                sb.append(userJson).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("]");
            statusCode = 200;
            reasonPhrase = "OK";
            contentType = "application/json";
            responseBody = sb.toString();
        } else if (segmentsLength == 2 && method.equals("POST")) {
            System.out.println("registerNewUser()");
            User user = JsonUserParser.mapJsonToUser(requestBody);
            user = userService.registerNewUser(user);
            if (user != null) {
                responseBody = JsonUserParser.mapUserToJson(user);
                statusCode = 200;
                reasonPhrase = "OK";
                contentType = "application/json";
            }
        }

//        else if (segmentsLength == 3 && segments[1].equals("id") && method.equals("GET")) {
//            System.out.println("getUserById()");
//        } else if (segmentsLength == 3 && segments[1].equals("id") && method.equals("DELETE")) {
//            System.out.println("removeUserById()");
//        } else if (segmentsLength == 3 && segments[1].equals("username") && method.equals("GET")) {
//            System.out.println("getUserByUsername()");
//        }

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        String formattedDateTime = dateTime.atZone(ZoneId.of("UTC")).format(dateTimeFormatter);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);

        byte[] bodyBytes =  responseBody.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(bodyBytes.length));
        headers.put("Connection", "close");
        headers.put("Date", formattedDateTime);

        response = new HttpResponse(version, statusCode, reasonPhrase, headers, responseBody);
    }

    public HttpResponse getResponse() {
        System.out.println("HTTP Response:\n" + response);
        return response;
    }
}