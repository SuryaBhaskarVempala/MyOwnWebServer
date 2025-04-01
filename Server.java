import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8020;
    private static final int THREAD_POOL_SIZE = 100;

    private static ExecutorService threadPool;

    public Server(int threads) {
        threadPool = Executors.newFixedThreadPool(threads);
    }

    public void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }

            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                sendResponse(out, "400 Bad Request", "text/plain", "Invalid request");
                return;
            }

            String method = parts[0];
            String path = parts[1];

            // Set default to index.html
            if ("/".equals(path) && method.equals("GET")) {
                path = "./index.html";
            }

            if ("/about".equals(path) && method.equals("GET")) {
                path = "./about.html";
            }

            if ("/contact".equals(path) && method.equals("GET")) {
                path = "./contact.html";
            }

            File file = new File(path);

            if (file.exists() && !file.isDirectory()) {
                String contentType = Files.probeContentType(file.toPath());
                sendFileResponse(out, "200 OK", contentType, file);
            } else if ("POST".equals(method)) {
                handlePostRequest(in, out);
            } else {
                sendResponse(out, "404 Not Found", "text/plain", "File Not Found");
            }
        } catch (IOException e) {
            System.out.println("Server Down !");
            e.printStackTrace();
        }
    }

    private void handlePostRequest(BufferedReader in, OutputStream out) throws IOException {
        int contentLength = 0;
        String line;

        // Read headers to find Content-Length
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            if (line.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }

        // Read the body based on Content-Length
        char[] body = new char[contentLength];
        in.read(body, 0, contentLength);
        String requestBody = new String(body);

        // Parse key-value pairs
        StringBuilder responseContent = new StringBuilder("Received:\n");
        String[] pairs = requestBody.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length >= 2) {
                responseContent.append("Key: ").append(keyValue[0])
                        .append(", Value: ").append(keyValue[1]).append("\n");
            }
        }

        // Send Response
        sendResponse(out, "200 OK", "text/plain", responseContent.toString());
    }

    private void sendResponse(OutputStream out, String status, String contentType, String content) throws IOException {
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 " + status);
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + content.length());
        writer.println("Connection: keep-alive");
        writer.println();
        writer.println(content);
    }

    private void sendFileResponse(OutputStream out, String status, String contentType, File file) throws IOException {
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 " + status);
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + file.length());
        writer.println("Connection: keep-alive");
        writer.println();
        writer.flush();
        Files.copy(file.toPath(), out);
        out.flush();
    }

    public static void run() throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Server server = new Server(THREAD_POOL_SIZE);
        System.out.println("Server is Listening");

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket.getRemoteSocketAddress());
                threadPool.execute(() -> server.handleRequest(clientSocket));
            }
        } finally {
            threadPool.shutdown();
            serverSocket.close();
        }
    }

    public static void main(String[] args) throws Exception {
        run();
    }
}
