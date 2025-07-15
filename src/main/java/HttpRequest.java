import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final InputStream request;
    private String method;
    private String path;
    private String version;
    private final Map<String, String> headers;
    private String body;

    public HttpRequest(InputStream request) {
        this.request = request;
        headers = new HashMap<>();
        parseHttpRequest();
    }

    private void parseHttpRequest() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(request));

            // Parse first line
            String line =  reader.readLine();
            if (line == null || line.isEmpty()) {
                System.err.println("Invalid request");
                return;
            } else {
                method = line.substring(0, line.indexOf(" "));
                path = line.substring(method.length() + 1, line.lastIndexOf(" "));
                version = line.substring(method.length() + path.length() + 2);
            }

            // Parse headers
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (line.contains(":")) {
                    String key =  line.substring(0, line.indexOf(":")).trim();
                    String value = line.substring(line.indexOf(":") + 1).trim();
                    headers.put(key, value);
                }
            }

            // Parse body
            String contentLengthHeader = headers.get("Content-Length");
            if (contentLengthHeader != null) {
                int contentLength = Integer.parseInt(contentLengthHeader);
                if (contentLength > 0) {
                    char[] bodyChars = new char[contentLength];
                    int bytesRead = reader.read(bodyChars, 0, contentLength);
                    body = new String(bodyChars, 0, bytesRead);
                } else {
                    body = "";
                }
            }
        } catch (IOException e) {
            System.err.println("Error parsing http request: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String toString() {
        return String.format("""
                %s %s %s
                %s
                %s
                """, method, path, version, headers, body);
    }
}
