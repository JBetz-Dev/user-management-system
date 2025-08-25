import java.util.Map;

/**
 * Builder class for constructing HTTP responses with method chaining support.
 * Provides a clean API for setting HTTP status codes, headers, and response bodies.
 * Implements HTTP/1.1 protocol specification for response formatting.
 * <p>
 * Responsibilities:
 * - Construct HttpResponse objects with builder interface
 * - Automatically map status codes to appropriate reason phrases
 * - Support both string and binary response bodies
 * - Enable method chaining for readable response construction
 * <p>
 * Automatic reason phrase generation covers common HTTP status codes.
 *
 * @see HttpResponse
 * @see UserRequestHandler
 * @see FileRequestHandler
 */
public class HttpResponseBuilder {
    private final HttpResponse response;

    public HttpResponseBuilder() {
        this.response = new HttpResponse();
    }

    public HttpResponse build() {
        return response;
    }

    public HttpResponseBuilder version(String version) {
        response.setVersion(version);
        return this;
    }

    public HttpResponseBuilder status(int status) {
        String reasonPhrase = getReasonPhrase(status);
        response.setStatusCode(status);
        response.setReasonPhrase(reasonPhrase);
        return this;
    }

    public HttpResponseBuilder header(String header, String value) {
        response.setHeader(header, value);
        return this;
    }

    public HttpResponseBuilder headers(Map<String, String> headers) {
        response.setHeaders(headers);
        return this;
    }

    public HttpResponseBuilder body(String body) {
        response.setBody(body);
        return this;
    }

    public HttpResponseBuilder body(byte[] body) {
        response.setBody(body);
        return this;
    }

    public static String getReasonPhrase(int statusCode) {
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
}
