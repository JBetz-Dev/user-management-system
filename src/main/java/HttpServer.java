import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * HTTP server that accepts client connections and manages the complete
 * request-response lifecycle using worker threads. Uses virtual threads
 * for lightweight concurrency.
 * <p>
 * Responsibilities:
 * - Socket lifecycle management
 * - Request parsing coordination through HttpRequestParser
 * - Response writing (response generation via HttpRequestHandler)
 * - Error handling and logging for connection and parsing failures
 * <p>
 * Design decision: Centralizes I/O concerns while delegating business
 * logic to specialized handlers.
 */
public class HttpServer {

    public static void main(String[] args) {
        try (ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor()) {

            try (ServerSocket serverSocket = new ServerSocket(9000)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    threadPool.submit(() -> handleRequest(clientSocket));
                }
            } catch (IOException e) {
                System.err.println("Server Exception: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (clientSocket;
             InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream()) {

            HttpRequest request = new HttpRequestParser(inputStream).parseToHttpRequest();
            HttpResponse response = new HttpRequestHandler(request).getResponse();

            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            System.err.println("Server Exception: " + e.getMessage());
            e.printStackTrace();
        } catch (HttpParsingException e) {
            System.err.println("Parsing Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}