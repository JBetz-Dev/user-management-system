import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileHandler {
    private final HttpRequest request;
    private final HttpResponse response;
    private SessionData sessionData;
    private final List<String> restrictedPaths = List.of("user-area", "profile");

    public FileHandler(HttpRequest request) {
        this.request = request;
        response = new HttpResponse();
    }

    public FileHandler(HttpRequest request, SessionData sessionData) {
        this.request = request;
        response = new HttpResponse();
        this.sessionData = sessionData;
    }

    public HttpResponse getResponse() throws IOException {
        boolean hasActiveSession = sessionData != null && sessionData.isActive();

        response.setVersion("HTTP/1.1");
        String pathString = request.getPath();
        Path path = Path.of(pathString.equals("/") ? "src/index.html" : pathString.substring(1));

        if (isRestrictedPath(pathString)) {
            if (hasActiveSession) {
                handleFileRequest(path);
            } else {
                response.setStatusCode(401);
            }
        } else {
            handleFileRequest(path);
        }

        return response;
    }

    private boolean isRestrictedPath(String path) {
        // Add additional paths
        for (String restrictedPath : restrictedPaths) {
            if (path.contains(restrictedPath)) {
                return true;
            }
        }
        return false;
    }

    private void handleFileRequest(Path path) throws IOException {
        if (Files.isReadable(path)) {
            String responseBody = Files.readString(path);
            response.setStatusCode(200);
            response.setReasonPhrase("OK");
            response.setHeader("Content-Type", Files.probeContentType(path));
            response.setBody(responseBody);
        } else {
            response.setStatusCode(404);
        }
    }
}