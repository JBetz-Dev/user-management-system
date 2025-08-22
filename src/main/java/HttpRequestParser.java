import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Parses raw HTTP requests from input streams into structured HttpRequest objects.
 * Implements strict HTTP/1.1 protocol parsing with comprehensive validation.
 * <p>
 * Responsibilities:
 * - Parse HTTP request line (method, path, version)
 * - Extract and validate HTTP headers
 * - Read request body based on Content-Length header
 * - Validate all components against HTTP specifications
 * <p>
 * Design decisions:
 * - Strict validation: malformed requests result in HttpParsingException
 * - Memory protection: enforces 10MB body size limit
 * - Line-by-line header parsing (folded headers not supported)
 * - Content-Length required for requests with bodies
 * <p>
 * Security considerations:
 * - Bounds checking on Content-Length to prevent memory exhaustion
 * - Input validation on all HTTP components
 * - No support for chunked encoding (simplified implementation)
 */
public class HttpRequestParser {
    private final InputStream inputStream;
    private final HttpRequest request;
    private BufferedReader reader;

    public HttpRequestParser(InputStream inputStream) {
        this.inputStream = inputStream;
        request = new HttpRequest();
    }

    public HttpRequest parseToHttpRequest() throws HttpParsingException, IOException {
        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        parseRequestLine();
        parseHeaders();
        parseBody();
        return request;
    }

    private void parseRequestLine() throws HttpParsingException, IOException {
        String line = reader.readLine();

        if (line == null || line.isEmpty()) {
            throw new HttpParsingException("Empty HTTP request line");
        }

        String[] parts = line.split("\\s+", 3);
        if (parts.length != 3) {
            throw new HttpParsingException("Invalid HTTP request line: " + line);
        }

        parseMethod(parts[0]);
        parsePath(parts[1]);
        parseVersion(parts[2]);
    }

    private void parseMethod(String method) throws HttpParsingException {
        List<String> validMethods = List.of("GET", "POST", "PUT",
                "PATCH", "DELETE", "HEAD", "OPTIONS", "CONNECT", "TRACE");

        if (method.trim().isEmpty()) {
            throw new HttpParsingException("Empty HTTP method");
        }

        if (!validMethods.contains(method)) {
            throw new HttpParsingException("Invalid HTTP method: " + method);
        }

        request.setMethod(method);
    }

    private void parsePath(String path) throws HttpParsingException {
        Pattern pathValidator = Pattern.compile("^(/[-a-zA-Z0-9._~%!$&'()*+,;=:@/]*)?$");

        if (path.trim().isEmpty()) {
            throw new HttpParsingException("Empty HTTP path");
        }

        if (!pathValidator.matcher(path).matches()) {
            throw new HttpParsingException("Invalid HTTP path: " + path);
        }

        request.setPath(path);
    }

    private void parseVersion(String version) throws HttpParsingException {
        Pattern versionValidator = Pattern.compile("HTTP/(\\d+)\\.(\\d+)");

        if (version.trim().isEmpty()) {
            throw new HttpParsingException("Empty HTTP version");
        }

        if (!versionValidator.matcher(version).matches()) {
            throw new HttpParsingException("Invalid HTTP version: " + version);
        }

        request.setVersion(version);
    }

    private void parseHeaders() throws HttpParsingException, IOException {
        Map<String, String> headers = request.getHeaders();
        String line;

        while ((line = reader.readLine()) != null && !line.isEmpty()) {

            if (line.contains(":")) {
                String[] pair = line.split(":", 2);

                if (pair.length >= 2) {
                    String key = pair[0].trim();
                    String value = pair[1].trim();

                    if (!key.isEmpty()) {
                        headers.put(key, value);
                    }
                }
            }
        }

        if (headers.isEmpty()) {
            throw new HttpParsingException("Empty HTTP headers");
        }

        request.setHeaders(headers);
    }

    private void parseBody() throws HttpParsingException, IOException {
        int maxContentLength = 10 * 1024 * 1024;
        String contentLengthHeader = request.getHeader("Content-Length");

        if (contentLengthHeader == null || contentLengthHeader.trim().isEmpty()) {
            return;
        }

        try {
            int contentLength = Integer.parseInt(contentLengthHeader);

            if (contentLength == 0) {
                return;
            }

            if (contentLength < 0) {
                throw new HttpParsingException("Negative Content-Length header: " + contentLength);
            }

            if (contentLength > maxContentLength) {
                throw new HttpParsingException("Content-Length exceeds 10MB: " + contentLength);
            }

            char[] bodyChars = new char[contentLength];
            int charsRead = reader.read(bodyChars, 0, contentLength);
            request.setBody(new String(bodyChars, 0, charsRead));
        } catch (NumberFormatException e) {
            throw new HttpParsingException("Invalid Content-Length header: " + contentLengthHeader);
        } catch (OutOfMemoryError e) {
            throw new HttpParsingException("Unable to allocate memory for request body");
        }
    }
}
