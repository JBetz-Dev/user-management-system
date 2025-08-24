import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class HttpMessage {
    private Map<String, String> headers;
    private byte[] body;

    public HttpMessage() {
        headers = new HashMap<>();
    }

    public abstract String getStartLine();

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getBody() {
        return new String(getBodyBytes(), StandardCharsets.UTF_8);
    }

    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setBody(String body) {
        this.body = body.getBytes(StandardCharsets.UTF_8);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBytes() {
        byte[] headerBytes = getHeaderBytes();
        byte[] bodyBytes = getBodyBytes();
        byte[] messageBytes = new byte[headerBytes.length + bodyBytes.length];

        System.arraycopy(headerBytes, 0, messageBytes, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, messageBytes, headerBytes.length, bodyBytes.length);

        return messageBytes;
    }

    public byte[] getHeaderBytes() {
        StringBuilder sb = new StringBuilder();

        sb.append(getStartLine()).append("\n");
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