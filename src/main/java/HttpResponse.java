import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP response with status, headers, and body.
 * Provides unified byte-based body handling for all content types.
 *
 * Responsibilities:
 * - Store response data (status, headers, body)
 * - Generate properly formatted HTTP response bytes
 * - Handle both text and binary content uniformly
 *
 * Design decisions:
 * - Body stored as byte[] to support binary content (images, files, etc.)
 * - Convenience methods for text body manipulation
 */
public class HttpResponse {
    private String version;
    private int statusCode;
    private String reasonPhrase;
    private Map<String, String> headers;
    private byte[] body;

    public HttpResponse() {
        headers = new HashMap<>();
    }

    public String getVersion() {
        return version != null ? version : "";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase != null ? reasonPhrase : "";
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getResponseBytes() {
        byte[] headerBytes = getHeaderBytes();
        byte[] bodyBytes = getBodyBytes();
        byte[] responseBytes = new byte[headerBytes.length + bodyBytes.length];

        System.arraycopy(headerBytes, 0, responseBytes, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, responseBytes, headerBytes.length, bodyBytes.length);

        return responseBytes;
    }

    public byte[] getHeaderBytes() {
        StringBuilder sb = new StringBuilder();

        sb.append(version).append(" ")
                .append(statusCode).append(" ")
                .append(reasonPhrase).append("\n");
        headers.forEach((k, v) -> sb.append(k)
                .append(": ")
                .append(v)
                .append("\n"));
        sb.append("\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getBodyBytes() {
        return body != null ? body : new  byte[0];
    }
}
