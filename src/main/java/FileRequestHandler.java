import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Handles HTTP requests for static files and resources.
 * Supports both text files (HTML, CSS, JS) and binary files (images, videos).
 *
 * Responsibilities:
 * - Serve static files from the filesystem
 * - Set appropriate Content-Type headers based on file type
 * - Enforce access restrictions for protected resources
 * - Handle session-based access control
 *
 * Design decisions:
 * - Uses Files.readAllBytes() for universal file type support
 * - Content-Type detection via Files.probeContentType()
 * - Session management for restricted path access
 */
public class FileRequestHandler {
    private final HttpRequest request;
    private final HttpResponse response;
    private final List<String> restrictedPaths = List.of("user-area", "profile");
    private SessionData activeSession;

    public FileRequestHandler(HttpRequest request) {
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

        if (isRestrictedPath(pathString) && !hasActiveSession) {
            response.setStatusCode(401);
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
            String contentType = Files.probeContentType(path);

            if (contentType == null) {
                response.setStatusCode(500);
            } else {
                byte[] responseBody = Files.readAllBytes(path);
                response.setStatusCode(200);
                response.setReasonPhrase("OK");
                response.setHeader("Content-Type", contentType);
                response.setBody(responseBody);
            }
        } else {
            response.setStatusCode(404);
        }
    }
}