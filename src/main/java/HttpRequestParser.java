import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Parses raw HTTP requests from input streams into structured HttpRequest objects.
 * Implements strict HTTP/1.1 protocol parsing with comprehensive validation.
 * Supports both text and binary request bodies without data corruption.
 * <p>
 * Responsibilities:
 * - Parse HTTP request line (method, path, version)
 * - Extract and validate HTTP headers
 * - Read request body as byte array based on Content-Length header
 * - Validate all components against HTTP/1.1 specifications
 * <p>
 * Additional considerations:
 * - Strict validation: malformed requests result in HttpParsingException
 * - Memory protection: enforces 10MB body size limit
 * - Content-Length required for requests with bodies
 *
 * @see HttpRequest
 * @see HttpParsingException
 */
public class HttpRequestParser {
    private final HttpRequest request;
    private final InputStream inputStream;

    public HttpRequestParser(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        request = new HttpRequest();
    }

    public HttpRequest parseToHttpRequest() throws HttpParsingException, IOException {
        parseHeaders();
        parseBody();
        return request;
    }

    private void parseHeaders() throws IOException, HttpParsingException {
        String requestLineAndHeaders = parseHeadersToString();

        try (BufferedReader reader = new BufferedReader(new StringReader(requestLineAndHeaders))) {
            parseRequestLine(reader);
            parseHeaders(reader);
        }
    }

    private String parseHeadersToString() throws IOException {
        ByteArrayOutputStream headerBuffer = new ByteArrayOutputStream();
        int currentByte;
        int crlfCount = 0; // Tracks position in \r\n\r\n sequence

        while ((currentByte = inputStream.read()) != -1) {
            headerBuffer.write(currentByte);

            if (currentByte == '\r') {
                crlfCount = (crlfCount == 0 || crlfCount == 2) ? crlfCount + 1 : 0;
            } else if (currentByte == '\n') {
                crlfCount = (crlfCount == 1 || crlfCount == 3) ? crlfCount + 1 : 0;

                if (crlfCount == 4) {
                    break; // Found \r\n\r\n - end of headers
                }
            } else {
                crlfCount = 0; // Reset on any other byte
            }
        }

        return headerBuffer.toString(StandardCharsets.UTF_8);
    }

    private void parseRequestLine(BufferedReader reader) throws HttpParsingException, IOException {
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

    private void parseHeaders(BufferedReader reader) throws HttpParsingException, IOException {
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
                throw new HttpParsingException("Negative Content-Length: " + contentLength);
            }

            if (contentLength > maxContentLength) {
                throw new HttpParsingException("Content-Length exceeds 10MB: " + contentLength);
            }

            byte[] bodyBytes = parseBodyToByteArray(contentLength);
            request.setBody(bodyBytes);
        } catch (NumberFormatException e) {
            throw new HttpParsingException("Invalid Content-Length header: " + contentLengthHeader);
        } catch (OutOfMemoryError e) {
            throw new HttpParsingException("Unable to allocate memory for request body");
        }
    }

    private byte[] parseBodyToByteArray(int contentLength) throws HttpParsingException, IOException {
        byte[] bodyBytes = new byte[contentLength];
        int totalBytesRead = 0;

        while (totalBytesRead < contentLength) {
            int bytesRead =  inputStream.read(bodyBytes, totalBytesRead, contentLength - totalBytesRead);

            if (bytesRead == -1) {
                throw new  HttpParsingException("Unexpected end of stream while reading request body");
            }

            totalBytesRead += bytesRead;
        }

        return bodyBytes;
    }
}
