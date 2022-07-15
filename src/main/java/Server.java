import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Server {
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");
    final ExecutorService threadPool;

    public Server(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public void serverStart(int port) {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                final Socket socket = serverSocket.accept();
                threadPool.submit(() -> requestProcess(socket));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void requestProcess(Socket socket) {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {

            final String requestLine = in.readLine();
            final String[] parts = requestLine.split(" ");

            if (parts.length != 3) {
                socket.close();
            } else {
                responseProcess(parts, out);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void responseProcess(String[] parts, BufferedOutputStream out) {
        try {
            final String path = parts[1];

            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
            } else {
                final Path filePath = Path.of(".", "public", path);
                final String mimeType = Files.probeContentType(filePath);
                final long length = Files.size(filePath);
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}