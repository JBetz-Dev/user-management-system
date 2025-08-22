import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Central HTTP request router that coordinates request processing and response generation.
 * Routes incoming HTTP requests to appropriate handlers based on URL path patterns.
 * <p>
 * Responsibilities:
 * - Route requests to UserRequestHandler or FileRequestHandler based on path
 * - Finalize HTTP response headers (Content-Length, Date, Connection)
 * <p>
 * Design decision: Pure routing and infrastructure concerns only.
 * Business logic, error handling, session management, and response generation delegated to specific handlers.
 * Each handler owns complete response construction including domain-specific error handling.
 */
public class HttpRequestHandler {
    private final HttpRequest request;
    private HttpResponse response;

    public HttpRequestHandler(HttpRequest request) {
        this.request = request;
        response = new HttpResponse();
    }

    public HttpResponse getResponse() {
        String path = request.getPath();

        if (path.contains("/users")) {
            response = new UserRequestHandler(request).getResponse();
        } else {
            response = new FileRequestHandler(request).getResponse();
        }

        finalizeResponseHeaders();
        return response;
    }

    private void finalizeResponseHeaders() {
        int responseBodyLength = response.getBodyBytes().length;

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