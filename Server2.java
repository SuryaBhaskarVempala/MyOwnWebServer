import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server2 {
    private static final int PORT = 8010;  // Port to listen on
    private static ExecutorService threadPool;

    public Server2(int threads) {
        // Create a fixed thread pool to handle multiple client connections
        threadPool = Executors.newFixedThreadPool(threads);
    }

    public void handleRequest(Socket clientSocket) throws IOException {
        PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        writer.println("chintu");
        writer.close();
        clientSocket.close();
    }


    public static void run() throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            Server2 server = new Server2(100);  // 100 threads in the pool

            while (true) {

                Socket clientSocket = serverSocket.accept();  // Wait for client connection
                System.out.println("Connection Accepted: " + clientSocket.getRemoteSocketAddress());


                threadPool.execute(() -> {
                    try {
                        server.handleRequest(clientSocket);
                    } catch (IOException e) {
                        try {
                            serverSocket.close();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        finally {
            threadPool.shutdown();
        }
    }


    public static void main(String[] args) throws IOException {
        run();
    }
}
