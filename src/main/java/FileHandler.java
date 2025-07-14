import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class FileHandler {
    private HttpRequest request;
    private HttpResponse response;

    public FileHandler(HttpRequest request) {
        this.request = request;
        response = processRequest();
    }

    public HttpResponse processRequest() {
        System.out.println("FileHandler called");
        String version = request.getVersion();
        int statusCode;
        String reasonPhrase;
        String responseBody;
        String contentType;
        String pathString =  request.getPath();

        Path path = Path.of(request.getPath().equals("/") ? "src/index.html" : pathString.substring(1));
        System.out.println("path: " + path.toString());

        if (Files.isReadable(path)) {
            try {
                responseBody = Files.readString(path);
                contentType = Files.probeContentType(path);
                statusCode = 200;
                reasonPhrase = "OK";
            } catch (IOException e) {
                System.err.println("Error creating response body");
                responseBody = """
                        <!DOCTYPE html>
                        <html lang="en">
                        <head>
                            <meta charset="UTF-8">
                            <title>500 Error</title>
                        </head>
                        <body>
                            <h1>500 Error: Internal Service Error</h1>
                            <p>Sorry, we encountered an error while trying to access the requested page.</p>
                        </body>
                        </html>
                        """;
                contentType = "text/html";
                statusCode = 500;
                reasonPhrase = "Internal Server Error";
            }
        } else {
            responseBody = """
                    <!DOCTYPE html>
                    <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <title>404 Error</title>
                    </head>
                    <body>
                        <h1>404 Error: Page Not Found</h1>
                        <p>Sorry, we could not find the page you are looking for.</p>
                    </body>
                    </html>
                    """;
            contentType = "text/html";
            statusCode = 404;
            reasonPhrase = "Not Found";
        }

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        String formattedDateTime = dateTime.atZone(ZoneId.of("UTC")).format(dateTimeFormatter);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);

        byte[] bodyBytes =  responseBody.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(bodyBytes.length));
        headers.put("Connection", "close");
        headers.put("Date", formattedDateTime);

        return new HttpResponse(version, statusCode, reasonPhrase, headers, responseBody);
    }

    public HttpResponse getResponse() {
        return response;
    }
}
