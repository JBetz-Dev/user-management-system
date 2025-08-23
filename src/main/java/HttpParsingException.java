/**
 * Checked exception thrown when HTTP request parsing fails due to
 * malformed or invalid HTTP protocol elements.
 *
 * This exception indicates that the incoming request does not conform
 * to HTTP/1.1 specifications and cannot be processed safely.
 *
 * Common scenarios:
 * - Invalid request line format
 * - Unsupported HTTP methods
 * - Malformed headers
 * - Invalid Content-Length values
 * - Request body size exceeding limits
 */
public class HttpParsingException extends Exception {
    public HttpParsingException(String message) {
        super(message);
    }
}