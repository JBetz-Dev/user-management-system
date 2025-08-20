import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Central HTTP request router that coordinates request processing and response generation.
 * Routes incoming HTTP requests to appropriate handlers based on URL path patterns.
 *
 * Responsibilities:
 * - Route requests to UserHandler or FileHandler based on path
 * - Generate error responses for failed requests
 * - Finalize HTTP response headers (Content-Length, Date, Connection)
 * - Content-type negotiation for error responses
 *
 * Design decision: Pure routing logic - no business logic or I/O concerns.
 * Session management is delegated to individual handlers for better separation of concerns.
 */
public class HttpRequestHandler {
    private final HttpErrorHandler errorHandler = new HttpErrorHandler();
    private final HttpRequest request;
    private HttpResponse response;

    public HttpRequestHandler(HttpRequest request) {
        this.request = request;
        response = new HttpResponse();
    }

    public HttpResponse getResponse() throws IOException {
        response = getHandlerResponse();
        int responseStatusCode = response.getStatusCode();

        if (responseStatusCode >= 400 && (response.getBody() == null || response.getBody().isEmpty())) {
            response = errorHandler.generateErrorResponse(responseStatusCode, getRequestContentType());
        }

        finalizeResponseHeaders();
        return response;
    }

    private HttpResponse getHandlerResponse() {
        String path = request.getPath();

        if (path.contains("/users")) {
            response = new UserHandler(request).getResponse();
        } else {
            try {
                response = new FileHandler(request).getResponse();
            } catch (IOException e) {
                response.setStatusCode(500);
            }
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