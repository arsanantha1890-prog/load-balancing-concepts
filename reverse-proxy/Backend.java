import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

// A dummy backend server. Just replies with its own port so we can
// see that the proxy actually forwarded the request here.
public class Backend {
    public static void main(String[] args) throws Exception {
        int port = 9000;
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