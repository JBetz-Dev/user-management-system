import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
            String pathString = request.getPath();

            if (pathString.contains("users")) {
                UserHandler userHandler = new UserHandler(request);
                response = userHandler.getResponse();
            } else {
                FileHandler fileHandler = new FileHandler(request);
                response = fileHandler.getResponse();
            }

            byte[] responseBytes = response.generateResponse().getBytes(StandardCharsets.UTF_8);
            output.write(responseBytes);
            output.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
