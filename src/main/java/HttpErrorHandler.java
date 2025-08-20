/**
 * Generates standardized HTTP error responses with appropriate content types.
 * Provides content negotiation for error responses based on client Accept headers.
 *
 * Responsibilities:
 * - Map HTTP status codes to standard reason phrases
 * - Generate JSON error responses for API clients
 * - Generate HTML error pages for browser clients
 * - Ensure proper Content-Type headers
 *
 * Design decision: Content-type aware responses improve client compatibility.
 * JSON responses for API requests, HTML for browser requests.
 */
public class HttpErrorHandler {

    public HttpResponse generateErrorResponse(int statusCode, String contentType) {
        HttpResponse response = new HttpResponse();
        String reasonPhrase = getReasonPhrase(statusCode);

        response.setVersion("HTTP/1.1");
        response.setStatusCode(statusCode);
        response.setReasonPhrase(reasonPhrase);

        if (contentType.contains("application/json")) {
            response.setHeader("Content-Type", "application/json");
            response.setBody("{\"error\": \"" + reasonPhrase + "\"}");
        } else {
            response.setHeader("Content-Type", "text/html");
            response.setBody(generateErrorHtml(statusCode, reasonPhrase));
        }

        return response;
    }

    private String getReasonPhrase(int statusCode) {
        String reasonPhrase;
        switch (statusCode) {
            case 400 -> reasonPhrase = "Bad Request";
            case 401 -> reasonPhrase = "Unauthorized";
            case 403 -> reasonPhrase = "Forbidden";
            case 404 -> reasonPhrase = "Not Found";
            case 405 -> reasonPhrase = "Method Not Allowed";
            case 406 -> reasonPhrase = "Not Acceptable";
            case 409 -> reasonPhrase = "Conflict";
            case 500 -> reasonPhrase = "Internal Server Error";
            default -> reasonPhrase = "Unknown";
        }

        return reasonPhrase;
    }

    private String generateErrorHtml(int statusCode, String reasonPhrase) {
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
