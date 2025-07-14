public class UserHandler {
    private HttpRequest request;
    private HttpResponse response;
    private UserService userService;

    public UserHandler(HttpRequest request) {
        this.request = request;
        processRequest();
    }

    private void processRequest() {
        String method = request.getMethod();
        String pathString = request.getPath();
        String version = request.getVersion();
        int statusCode;
        String reasonPhrase;
        String responseBody;
        String contentType;

        // Trim trailing "/" for standardization
        if (pathString.endsWith("/")) {
            pathString = pathString.substring(0, pathString.length() - 1);
        }

        String[] segments = pathString.split("/");
        int segmentsLength = segments.length;

        System.out.println("Debug:");
        for (String segment : segments) {
            System.out.println(segment);
        }
        System.out.println();

        if (segmentsLength == 2 && method.equals("GET")) {
            System.out.println("getAllUsers()");
        } else if (segmentsLength == 2 && method.equals("POST")) {
            System.out.println("registerNewUser()");
        } else if (segmentsLength == 3 && segments[1].equals("id") && method.equals("GET")) {
            System.out.println("getUserById()");
        } else if (segmentsLength == 3 && segments[1].equals("id") && method.equals("DELETE")) {
            System.out.println("removeUserById()");
        } else if (segmentsLength == 3 && segments[1].equals("username") && method.equals("GET")) {
            System.out.println("getUserByUsername()");
        }
//
//        LocalDateTime dateTime = LocalDateTime.now();
//        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;
//        String formattedDateTime = dateTime.atZone(ZoneId.of("UTC")).format(dateTimeFormatter);
//
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Content-Type", contentType);
//
//        byte[] bodyBytes =  responseBody.getBytes(StandardCharsets.UTF_8);
//        headers.put("Content-Length", String.valueOf(bodyBytes.length));
//        headers.put("Connection", "close");
//        headers.put("Date", formattedDateTime);
//
//        return new HttpResponse(version, statusCode, reasonPhrase, headers, responseBody);
    }
}