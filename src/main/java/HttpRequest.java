import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an HTTP request with method, path, headers, and body.
 * Stores body content as byte arrays to support both text and binary data.
 *
 * Design decisions:
 * - Body stored as byte[] for universal content type support
 * - Headers stored as String map for easy manipulation
 * - Convenience methods provided for text body access
 */
public class HttpRequest {
    private String method;
    private String path;
    private String version;
    private Map<String, String> headers;
    private byte[] body;

    public HttpRequest() {
        headers = new HashMap<>();
    }

    public String getMethod() {
        return method != null ? method : "";
    }

    public String getPath() {
        return path != null ? path : "";
    }

    public String getVersion() {
        return version != null ? version : "";
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getBody() {
        return new String(getBodyBytes(), StandardCharsets.UTF_8);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getRequestBytes() {
        byte[] headerBytes = getHeaderBytes();
        byte[] bodyBytes = getBodyBytes();
        byte[] requestBytes = new byte[headerBytes.length + bodyBytes.length];

        System.arraycopy(headerBytes, 0, requestBytes, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, requestBytes, headerBytes.length, bodyBytes.length);

        return requestBytes;
    }

    public byte[] getHeaderBytes() {
        StringBuilder sb = new StringBuilder();

        sb.append(version).append(" ")
                .append(path).append(" ")
                .append(method).append("\n");
        headers.forEach((k, v) -> sb.append(k)
                .append(": ")
                .append(v)
                .append("\n"));
        sb.append("\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getBodyBytes() {
        return body != null ? body : new byte[0];
    }
}