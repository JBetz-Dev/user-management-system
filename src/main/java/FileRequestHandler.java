import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Handles HTTP requests for static files and resources.
 * Supports both text files (HTML, CSS, JS) and binary files (images, videos).
 * Implements HTTP/1.1 protocol specification for file serving.
 * <p>
 * Responsibilities:
 * - Serve static files from the filesystem
 * - Set appropriate Content-Type headers based on file type
 * - Enforce session-based access control for restricted paths
 * - Generate HTML error responses for file-related failures
 *
 * @see HttpResponseBuilder
 * @see SessionManager
 */
public class FileRequestHandler {
    private final HttpRequest request;
    private final HttpResponseBuilder responseBuilder;
    private final List<String> restrictedPaths = List.of("user-area", "profile");
    private SessionData activeSession;

    public FileRequestHandler(HttpRequest request) {
        this.request = request;
        responseBuilder = new HttpResponseBuilder();
    }

    public HttpResponse getResponse() {
        boolean hasActiveSession = setupSessionIfCookie();
        String pathString = request.getPath();
        Path path = Path.of(pathString.equals("/") ? "src/index.html" : pathString.substring(1));

        if (isRestrictedPath(pathString) && !hasActiveSession) {
            return generateErrorResponse(401);
        } else {
            try {
                return handleFileRequest(path);
            } catch (IOException e) {
                return generateErrorResponse(500);
            }
        }
    }

    private boolean setupSessionIfCookie() {
        String cookie = request.getHeader("Cookie");

        if (cookie != null) {
            activeSession = SessionManager.getActiveSession(cookie);
        }

        return activeSession != null;
    }

    private boolean isRestrictedPath(String path) {
        for (String restrictedPath : restrictedPaths) {
            if (path.contains(restrictedPath)) {
                return true;
            }
        }
        return false;
    }

    private HttpResponse handleFileRequest(Path path) throws IOException {
        if (Files.isReadable(path)) {
            String contentType = Files.probeContentType(path);

            if (contentType == null) {
                return generateErrorResponse(500);
            } else {
                byte[] responseBody = Files.readAllBytes(path);
                return responseBuilder.version("HTTP/1.1")
                        .status(200)
                        .header("Content-Type", contentType)
                        .body(responseBody)
                        .build();
            }
        } else {
            return generateErrorResponse(404);
        }
    }

    private HttpResponse generateErrorResponse(int statusCode) {
        return responseBuilder.version("HTTP/1.1")
                .status(statusCode)
                .header("Content-Type", "text/html")
                .body(generateErrorHtml(statusCode))
                .build();
    }

    private String generateErrorHtml(int statusCode) {
        String reasonPhrase = HttpResponseBuilder.getReasonPhrase(statusCode);

        return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <title>%1$d (%2$s)</title>
                    </head>
                    <body>
                        <h1>%1$d Error: %2$s</h1>
                    </body>
                </html>""", statusCode, reasonPhrase);
    }
}