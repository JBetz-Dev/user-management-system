import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestHandler {
    private final Socket socket;
    private HttpRequest request;
    private HttpResponse response;

    public HttpRequestHandler(Socket socket) {
        this.socket = socket;
    }

    public void handle() {
        try (socket;
             BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
             OutputStream output = socket.getOutputStream()) {
            request = new HttpRequest(input);
            switch (request.getMethod()) {
                case "GET" -> response = handleGet(request);
                case "POST" -> response = handlePost(request);
                default -> System.err.println("Invalid request");
            }

            System.out.println("Method: " + request.getMethod());
            System.out.println("Path: " + request.getPath());
            System.out.println("Version: " + request.getVersion());
            System.out.println("Headers:");
            Map<String, String> headers = request.getHeaders();
            headers.forEach((k, v) -> System.out.println(k + ": " + v));
            System.out.println("Body: " + request.getBody() + "\n");

//            System.out.println("--- Response ---\n" + response.generateResponse());
            byte[] responseBytes = response.generateResponse().getBytes(StandardCharsets.UTF_8);
            output.write(responseBytes);
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse handleGet(HttpRequest request) {
        // Just handle html returns for now
        String version = request.getVersion();
        int statusCode;
        String reasonPhrase;
        String responseBody;

        String pathString =  request.getPath();
        Path path = Path.of(request.getPath().equals("/") ? "src/index.html" : pathString);

        if (Files.isReadable(path)) {
            try {
                responseBody = Files.readString(path);
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
            statusCode = 404;
            reasonPhrase = "Not Found";
        }

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        String formattedDateTime = dateTime.atZone(ZoneId.of("UTC")).format(dateTimeFormatter);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/html; charset=UTF-8");

        byte[] bodyBytes =  responseBody.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(bodyBytes.length));
        headers.put("Connection", "close");
        headers.put("Date", formattedDateTime);

        return new HttpResponse(version, statusCode, reasonPhrase, headers, responseBody);
    }

    private HttpResponse handlePost(HttpRequest request) {
        String version = request.getVersion();
        int statusCode;
        String reasonPhrase;
        String responseBody = "";

        File jsonFile = new File("src/JSON.txt");
        try (PrintWriter writer = new PrintWriter(jsonFile)) {
            String requestBody = request.getBody();
            writer.write(requestBody);
            statusCode = 200;
            reasonPhrase = "Created";
            responseBody = requestBody;
        } catch (FileNotFoundException e) {
            System.err.println("Error writing to JSON file");
            statusCode = 500;
            reasonPhrase = "Internal Server Error";
        }

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        String formattedDateTime = dateTime.atZone(ZoneId.of("UTC")).format(dateTimeFormatter);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");

        byte[] bodyBytes =  responseBody.getBytes(StandardCharsets.UTF_8);
        headers.put("Content-Length", String.valueOf(bodyBytes.length));
        headers.put("Connection", "close");
        headers.put("Date", formattedDateTime);

        return new HttpResponse(version, statusCode, reasonPhrase, headers, responseBody);
    }
}
