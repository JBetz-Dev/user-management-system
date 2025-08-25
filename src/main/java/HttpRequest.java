/**
 * HTTP request message containing method, path, version, headers, and body.
 * Extends HttpMessage to inherit common header and body handling functionality.
 * Implements HTTP/1.1 protocol specification for request formatting.
 * <p>
 * Responsibilities:
 * - Store HTTP request line components (method, path, version)
 * - Format request line for HTTP message transmission
 * - Provide convenient access to request-specific data
 *
 * @see HttpMessage
 * @see HttpRequestParser
 */
public class HttpRequest extends HttpMessage {
    private String method;
    private String path;
    private String version;

    public String getMethod() {
        return method != null ? method : "";
    }

    public String getPath() {
        return path != null ? path : "";
    }

    public String getVersion() {
        return version != null ? version : "";
    }

    public String getStartLine() {
        return method + " " +  path + " " + version;
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
}