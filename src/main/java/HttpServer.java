import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP server that accepts client connections and manages the complete
 * request-response lifecycle using worker threads. Uses virtual threads
 * for lightweight concurrency.
 *
 * Responsibilities:
 * - Socket lifecycle management
 * - Request parsing coordination
 * - Response writing
 * - Error handling and logging
 *
 * Design decision: Centralizes I/O concerns while delegating business
 * logic to specialized handlers.
 */
public class HttpServer {

    public static void main(String[] args) {
        try (ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor()) {

            try (ServerSocket serverSocket = new ServerSocket(9000)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> {
                        try (clientSocket) {
                            HttpRequest request = new HttpRequestParser(clientSocket.getInputStream()).parseToHttpRequest();
                            HttpResponse response = new HttpRequestHandler(request).getResponse();
                            writeResponse(clientSocket, response);
                        } catch (IOException e) {
                            System.err.println("Server Exception: " + e.getMessage());
                            e.printStackTrace();
                        } catch (HttpParsingException e) {
                            System.err.println("Parsing Exception: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }
            } catch (IOException e) {
                System.err.println("Server Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void writeResponse(Socket socket, HttpResponse response) throws IOException {
        try (OutputStream outputStream = socket.getOutputStream()) {
            outputStream.write(response.toString().getBytes());
            outputStream.flush();
        }
    }
}
