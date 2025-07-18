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
                response = getResponse();
            } catch (ParsingException e) {
                response = handleBadRequest();
            }

            int responseBodyLength = response.getBody().getBytes(StandardCharsets.UTF_8).length;
            response.setHeader("Content-Length", String.valueOf(responseBodyLength));
            response.setHeader("Date", getHttpDateTime());
            response.setHeader("Connection", "close");

            System.out.println(response.toString());
            output.write(response.toString().getBytes());
            output.flush();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private HttpResponse getResponse() {
        String path = request.getPath();

        if (path.contains("users")) {
            UserHandler userHandler = new UserHandler(request);
            response = userHandler.getResponse();

            if (isInvalidResponse()) {
                response = handleInternalServerError();
            }
        } else {
            FileHandler fileHandler = new FileHandler(request);
            try {
                response = fileHandler.getResponse();

                if (isInvalidResponse()) {
                    response = handleInternalServerError();
                }
            } catch (IOException e) {
                response = handleInternalServerError();
            }
        }

        if (response.getStatusCode() == 404) {
            response = handleResourceNotFoundError();
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

    public boolean isInvalidResponse() {
        Pattern versionValidator = Pattern.compile("HTTP/(\\d+)\\.(\\d+)");

        if (response == null) {
            return true;
        }

        String version = response.getVersion();
        if (version == null || version.isEmpty() || !versionValidator.matcher(version).matches()) {
            System.err.println("Invalid HTTP version: " + version);
            return true;
        }

        int statusCode = response.getStatusCode();
        if (statusCode < 100 || statusCode > 599) {
            System.err.println("Invalid HTTP status code: " + statusCode);
            return true;
        }

        String reasonPhrase = response.getReasonPhrase();
        if (reasonPhrase == null || reasonPhrase.isEmpty()) {
            System.err.println("Invalid HTTP reason phrase (none provided)");
            return true;
        }

        Map<String, String> headers = response.getHeaders();
        if (!headers.containsKey("Content-Type")) {
            System.err.println("Invalid HTTP format: Content-Type header not provided");
            return true;
        }

        return false;
    }

    private HttpResponse handleBadRequest() {
        int statusCode = 400;
        String reasonPhrase = "Bad Request";

        response.setVersion(request.getVersion());
        response.setStatusCode(statusCode);
        response.setReasonPhrase(reasonPhrase);
        response.setHeader("Content-Type", "text/html");
        response.setBody(generateErrorHtml(statusCode, reasonPhrase));

        return response;
    }

    private HttpResponse handleInternalServerError() {
        int statusCode = 500;
        String reasonPhrase = "Internal Server Error";

        response.setVersion(request.getVersion());
        response.setStatusCode(statusCode);
        response.setReasonPhrase(reasonPhrase);
        response.setHeader("Content-Type", "text/html");
        response.setBody(generateErrorHtml(statusCode, reasonPhrase));

        return response;
    }

    private HttpResponse handleResourceNotFoundError() {
        int statusCode = 404;
        String reasonPhrase = "Not Found";

        response.setVersion(request.getVersion());
        response.setStatusCode(statusCode);
        response.setReasonPhrase("Not Found");
        response.setHeader("Content-Type", "text/html");
        response.setBody(generateErrorHtml(statusCode, reasonPhrase));

        return response;
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

    private String getHttpDateTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;

        return dateTime.atZone(ZoneId.of("UTC")).format(dateTimeFormatter);
    }
}
