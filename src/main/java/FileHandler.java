import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileHandler {
    private final HttpRequest request;
    private final HttpResponse response;

    public FileHandler(HttpRequest request) {
        this.request = request;
        response = new HttpResponse();
    }

    public HttpResponse getResponse() throws IOException {
        response.setVersion("HTTP/1.1");

        String pathString = request.getPath();
        Path path = Path.of(pathString.equals("/") ? "src/index.html" : pathString.substring(1));

        if (Files.isReadable(path)) {
            String responseBody = Files.readString(path);
            response.setStatusCode(200);
            response.setReasonPhrase("OK");
            response.setHeader("Content-Type", Files.probeContentType(path));
            response.setBody(responseBody);
        } else {
            response.setStatusCode(404);
        }

        return response;
    }
}
