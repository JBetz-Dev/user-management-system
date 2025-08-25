/**
 * Checked exception thrown when HTTP request parsing fails due to
 * malformed or invalid HTTP protocol elements.
 * <p>
 * This exception indicates that the incoming request does not conform
 * to HTTP/1.1 specifications and cannot be processed safely.
 * <p>
 * Common scenarios:
 * - Invalid request line format
 * - Unsupported HTTP methods
 * - Malformed headers
 * - Invalid Content-Length values
 * - Request body size exceeding limits
 *
 * @see HttpRequestParser
 */
public class HttpParsingException extends Exception {
    public HttpParsingException(String message) {
        super(message);
    }
}