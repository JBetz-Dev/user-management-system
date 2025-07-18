import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private String version;
    private int statusCode;
    private String reasonPhrase;
    private Map<String, String> headers;
    private String body;

    public HttpResponse() {
        headers = new HashMap<>();
    }

    public String getVersion() {
        return version;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getBody() {
        return body;
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
        this.body = body;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(version).append(" ")
                .append(statusCode).append(" ")
                .append(reasonPhrase).append("\n");
        headers.forEach((k, v) -> sb.append(k)
                .append(": ")
                .append(v)
                .append("\n"));
        sb.append("\n")
                .append(body);

        return sb.toString();
    }
}
