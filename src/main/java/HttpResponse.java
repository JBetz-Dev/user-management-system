/**
 * HTTP response message containing status information, headers, and body.
 * Extends HttpMessage to inherit common header and body handling functionality.
 * Implements HTTP/1.1 protocol specification for response formatting.
 * <p>
 * Responsibilities:
 * - Store HTTP status line components (version, status code, reason phrase)
 * - Format status line for HTTP message transmission
 * - Provide convenient access to response-specific data
 *
 * @see HttpMessage
 * @see HttpResponseBuilder
 */
public class HttpResponse extends HttpMessage{
    private String version;
    private int statusCode;
    private String reasonPhrase;

    public String getVersion() {
        return version != null ? version : "";
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase != null ? reasonPhrase : "";
    }

    public String getStartLine() {
        return version + " " +  statusCode + " " + reasonPhrase;
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
}
