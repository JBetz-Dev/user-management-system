import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {

    public static void main(String[] args) {
        try (ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor()) {

            try (ServerSocket serverSocket = new ServerSocket(9000)) {
                System.out.println("Server listening on port 9000");

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Accepting client socket from: " + clientSocket.getInetAddress());

                    threadPool.submit(() -> new HttpRequestHandler(clientSocket).handle());
                }
            } catch (IOException e) {
                System.out.println("Server Exception: " + e.getMessage());
            }
        }
    }
}
