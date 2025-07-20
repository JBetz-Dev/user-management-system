import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class HttpRequestHandler {
    private final Socket socket;
    HttpRequest request;
    HttpResponse response;

    public HttpRequestHandler(Socket socket) {
        this.socket = socket;
        request = new HttpRequest();
        response = new HttpResponse();
    }

    public void handle() {
        try (socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             OutputStream output = socket.getOutputStream()) {

            try {
                parseHttpRequest(reader);
                response = getHandlerResponse();
            } catch (ParsingException e) {
                response.setStatusCode(400);
            }

            int responseStatusCode = response.getStatusCode();
            if (responseStatusCode >= 400) {
                response = generateErrorResponse(responseStatusCode);
            }

            finalizeResponseHeaders();
            System.out.println(response.toString());
            output.write(response.toString().getBytes());
            output.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private HttpResponse getHandlerResponse() {
        String path = request.getPath();
        String cookie = request.getHeader("Cookie");
        SessionData activeSession = null;

        if (cookie != null) {
            activeSession = SessionManager.getActiveSession(cookie);
        }

        if (path.contains("users")) {
            UserHandler userHandler;

            if (activeSession != null) {
                userHandler = new UserHandler(request, activeSession);
            } else {
                userHandler = new UserHandler(request);
            }

            response = userHandler.getResponse();
        } else {
            FileHandler fileHandler;

            if (activeSession != null) {
                fileHandler = new FileHandler(request, activeSession);
            } else {
                fileHandler = new FileHandler(request);
            }

            try {
                response = fileHandler.getResponse();
            } catch (IOException e) {
                response.setStatusCode(500);
            }
        }

        return response;
    }

    static class ParsingException extends Exception {
        public ParsingException(String message) {
            super(message);
        }
    }

    private void parseHttpRequest(BufferedReader reader) throws ParsingException, IOException {
        parseRequestLine(reader);
        parseHeaders(reader);
        parseBody(reader);
    }

    private void parseRequestLine(BufferedReader reader) throws ParsingException, IOException {
        String line = reader.readLine();

        if (line == null || line.isEmpty()) {
            System.err.println("Invalid HTTP request line: " + line);
            throw new ParsingException("Invalid HTTP request line: " + line);
        } else {
            parseMethod(line.substring(0, line.indexOf(" ")));
            parsePath(line.substring(request.getMethod().length() + 1, line.lastIndexOf(" ")));
            parseVersion(line.substring(request.getMethod().length() + request.getPath().length() + 2));
        }
    }

    private void parseMethod(String method) throws ParsingException {
        List<String> validMethods = List.of("GET", "POST", "PUT",
                "PATCH", "DELETE", "HEAD", "OPTIONS", "CONNECT", "TRACE");

        if (method == null || method.isEmpty() || !validMethods.contains(method)) {
            System.err.println("Invalid HTTP method: " + method);
            throw new ParsingException("Invalid HTTP method: " + method);
        } else {
            request.setMethod(method);
        }
    }

    private void parsePath(String path) throws ParsingException {
        Pattern pathValidator = Pattern.compile("^(/[-a-zA-Z0-9._~%!$&'()*+,;=:@/]*)?$");

        if (path == null || path.isEmpty() || !pathValidator.matcher(path).matches()) {
            System.err.println("Invalid HTTP path: " + path);
            throw new ParsingException("Invalid HTTP path: " + path);
        } else {
            request.setPath(path);
        }
    }

    private void parseVersion(String version) throws ParsingException {
        Pattern versionValidator = Pattern.compile("HTTP/(\\d+)\\.(\\d+)");

        if (version == null || version.isEmpty() || !versionValidator.matcher(version).matches()) {
            System.err.println("Invalid HTTP version: " + version);
            throw new ParsingException("Invalid HTTP version: " + version);
        } else {
            request.setVersion(version);
        }
    }

    private void parseHeaders(BufferedReader reader) throws ParsingException, IOException {
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.contains(":")) {
                String key = line.substring(0, line.indexOf(":")).trim();
                String value = line.substring(line.indexOf(":") + 1).trim();
                request.setHeader(key, value);
            }
        }

        Map<String, String> headers = request.getHeaders();

        if (headers.isEmpty()) {
            System.err.println("Invalid HTTP request headers: " + request.getHeaders());
            throw new ParsingException("Invalid HTTP headers: No headers found");
        }
    }

    private void parseBody(BufferedReader reader) throws IOException {
        String contentLengthHeader = request.getHeader("Content-Length");

        if (contentLengthHeader != null) {
            int contentLength = Integer.parseInt(contentLengthHeader);
            if (contentLength > 0) {
                char[] bodyChars = new char[contentLength];
                int bytesRead = reader.read(bodyChars, 0, contentLength);
                request.setBody(new String(bodyChars, 0, bytesRead));
            } else {
                request.setBody("");
            }
        }
    }

    private HttpResponse generateErrorResponse(int statusCode) {
        String reasonPhrase = getReasonPhrase(statusCode);

        response.setVersion("HTTP/1.1");
        response.setStatusCode(statusCode);
        response.setReasonPhrase(reasonPhrase);

        String requestContentType = getRequestContentType();
        if (requestContentType.contains("application/json")) {
            response.setHeader("Content-Type", "application/json");
            response.setBody("{\"error\": \"" + reasonPhrase + "\"}");
        } else {
            response.setHeader("Content-Type", "text/html");
            response.setBody(generateErrorHtml(statusCode, reasonPhrase));
        }

        return response;
    }

    private String getRequestContentType() {
        String contentType = "";

        if (request.getHeader("Content-Type") != null) {
            contentType = request.getHeader("Content-Type");
        } else if (request.getHeader("Accept") != null) {
            contentType = request.getHeader("Accept");
        }

        return contentType;
    }

    private String getReasonPhrase(int statusCode) {
        String reasonPhrase;
        switch (statusCode) {
            case 400 -> reasonPhrase = "Bad Request";
            case 401 -> reasonPhrase = "Unauthorized";
            case 403 -> reasonPhrase = "Forbidden";
            case 404 -> reasonPhrase = "Not Found";
            case 405 -> reasonPhrase = "Method Not Allowed";
            case 406 -> reasonPhrase = "Not Acceptable";
            case 409 -> reasonPhrase = "Conflict";
            case 500 -> reasonPhrase = "Internal Server Error";
            default -> reasonPhrase = "Unknown";
        }

        return reasonPhrase;
    }

    private String generateErrorHtml(int statusCode, String reasonPhrase) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <title>%1$d (%2$s)</title>
                    </head>
                    <body>
                        <h1>%1$d Error: %2$s</h1>
                    </body>
                </html>""", statusCode, reasonPhrase);
    }

    private void finalizeResponseHeaders() {
        int responseBodyLength = response.getBody().getBytes(StandardCharsets.UTF_8).length;
        response.setHeader("Content-Length", String.valueOf(responseBodyLength));
        response.setHeader("Date", getHttpDateTime());
        response.setHeader("Connection", "close");
    }

    private String getHttpDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;

        return dateTime.atZone(ZoneId.of("UTC")).format(dateTimeFormatter);
    }
}