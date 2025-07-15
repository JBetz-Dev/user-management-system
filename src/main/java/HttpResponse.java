import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private String version;
    private int statusCode;
    private String reasonPhrase;
    private final Map<String, String> headers;
    private String body;

    public HttpResponse(String version,  int statusCode, String reasonPhrase,
                        Map<String, String> headers, String body) {
        this.version = version;
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = new HashMap<>(headers);
        this.body = body;
    }

    public String generateResponse() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(version)
                .append(" ")
                .append(statusCode)
                .append(" ")
                .append(reasonPhrase)
                .append("\n");
        headers.forEach((k,v) -> stringBuilder.append(k)
                .append(": ")
                .append(v)
                .append("\n"));
        stringBuilder.append("\n")
                .append(body);

        return stringBuilder.toString();
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

    public String getBody() {
        return body;
    }

    public String toString() {
        return generateResponse();
    }
}
