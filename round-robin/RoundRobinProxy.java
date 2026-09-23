import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// A proxy that spreads requests across multiple backends, one after
// another in a fixed rotation: backend 0, then 1, then 2, then back
// to 0, and so on.
public class RoundRobinProxy {
    public static void main(String[] args) throws Exception {
        List<String> backends = List.of(
                "http://localhost:9001",
                "http://localhost:9002",
                "http://localhost:9003");

        int proxyPort = 8080;
        HttpClient client = HttpClient.newHttpClient();

        // Tracks which backend gets the next request. AtomicInteger is
        // used instead of a plain int so this stays correct even if
        // multiple requests arrive at the same time on different threads.
        AtomicInteger counter = new AtomicInteger(0);

        HttpServer server = HttpServer.create(new InetSocketAddress(proxyPort), 0);

        server.createContext("/", exchange -> {
            // Pick the next backend in rotation.
            int index = counter.getAndIncrement() % backends.size();
            String backendUrl = backends.get(index);

            HttpRequest backendRequest = HttpRequest.newBuilder()
                    .uri(URI.create(backendUrl + exchange.getRequestURI()))
                    .build();

            try {
                HttpResponse<byte[]> backendResponse = client.send(
                        backendRequest, HttpResponse.BodyHandlers.ofByteArray());

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
        System.out.println("Round-robin proxy running on port " + proxyPort
                + ", distributing across " + backends);
    }
}
