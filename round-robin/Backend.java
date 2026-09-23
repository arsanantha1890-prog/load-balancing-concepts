import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

// Same dummy backend as before, but the port is passed in as an
// argument so we can start several copies of it (one per terminal).
public class Backend {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {
            String response = "Hello from backend on port " + port;
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        });

        server.start();
        System.out.println("Backend running on port " + port);
    }
}
