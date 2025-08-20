import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileHandler {
    private final HttpRequest request;
    private final HttpResponse response;
    private final List<String> restrictedPaths = List.of("user-area", "profile");
    private SessionData activeSession;

    public FileHandler(HttpRequest request) {
        this.request = request;
        response = new HttpResponse();
    }

    public HttpResponse getResponse() throws IOException {
        String cookie = request.getHeader("Cookie");

        if (cookie != null) {
            activeSession = SessionManager.getActiveSession(cookie);
        }

        boolean hasActiveSession = activeSession != null;

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