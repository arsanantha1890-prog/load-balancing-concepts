import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// A minimal reverse proxy. It listens on one port, forwards every
// request to the backend, and sends the backend's response back
// to the original client.
public class ReverseProxy {
    public static void main(String[] args) throws Exception {
        String backendUrl = "http://localhost:9000";
        int proxyPort = 8080;

        HttpClient client = HttpClient.newHttpClient();
        HttpServer server = HttpServer.create(new InetSocketAddress(proxyPort), 0);

        server.createContext("/", exchange -> {
            // 1. Build a request to the backend, reusing the client's path
            HttpRequest backendRequest = HttpRequest.newBuilder()
                    .uri(URI.create(backendUrl + exchange.getRequestURI()))
                    .build();

            // 2. Forward it and get the backend's response
            try {
                HttpResponse<byte[]> backendResponse = client.send(
                        backendRequest, HttpResponse.BodyHandlers.ofByteArray());

                // 3. Send that response back to the original client
                byte[] body = backendResponse.body();
                exchange.sendResponseHeaders(backendResponse.statusCode(), body.length);
                exchange.getResponseBody().write(body);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });

        server.start();
        System.out.println("Reverse proxy running on port " + proxyPort
                + ", forwarding to " + backendUrl);
    }
}