import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    private static final int PORT = 9999;
    private static final int THREADS = 64;

    public static void main(String[] args) {
        final ExecutorService threadPool = Executors.newFixedThreadPool(THREADS);
        Server server = new Server(threadPool);
        server.serverStart(PORT);
    }
}