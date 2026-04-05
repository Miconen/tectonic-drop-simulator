package weighttables;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Server {

    static final Set<String> VALID_BOSSES = Set.of(
            "Duke", "Leviathan", "Whisperer", "Vardorvis");

    static final Set<String> VALID_RAIDS = Set.of(
            "Chambers of Xeric");

    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true");

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/kill", Server::handleKill);
        server.createContext("/raid", Server::handleRaid);
        server.createContext("/supports", Server::handleSupports);

        server.start();
        System.out.println("Listening on http://localhost:" + port);
    }

    static void handleKill(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI());

        String boss = params.get("boss");
        String countStr = params.get("count");

        if (boss == null || countStr == null) {
            sendText(exchange, 400, "Missing required params: boss and count");
            return;
        }

        if (!VALID_BOSSES.contains(boss)) {
            sendText(exchange, 400, "Unknown boss: " + boss
                    + ". Valid bosses: " + VALID_BOSSES);
            return;
        }

        int count;
        try {
            count = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            sendText(exchange, 400, "count must be a number");
            return;
        }

        if (count < 1 || count > 100000) {
            sendText(exchange, 400, "count must be between 1 and 100000");
            return;
        }

        try {
            String headerText = "Loot from " + count + " " + boss + " kills:";
            HashMap<String, Integer> loot = DT2.simulateBoss(boss, count);
            byte[] image = ImageGenerator.GenerateLootImage(loot, headerText);
            sendImage(exchange, image);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            sendText(exchange, 500, "Error: " + e.getMessage());
        }
    }

    static void handleRaid(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI());

        String name = params.get("name");
        String countStr = params.get("count");
        String pointsStr = params.get("points");
        String partyStr = params.get("party");

        if (name == null || countStr == null || pointsStr == null || partyStr == null) {
            sendText(exchange, 400,
                    "Missing required params: name, count, points, party."
                            + " Example: /raid?name=Chambers of Xeric&count=100&points=30000&party=3");
            return;
        }

        if (!VALID_RAIDS.contains(name)) {
            sendText(exchange, 400, "Unknown raid: " + name
                    + ". Valid raids: " + VALID_RAIDS);
            return;
        }

        int count, points, party;
        try {
            count = Integer.parseInt(countStr);
            points = Integer.parseInt(pointsStr);
            party = Integer.parseInt(partyStr);
        } catch (NumberFormatException e) {
            sendText(exchange, 400, "count, points, and party must be numbers");
            return;
        }

        if (count < 1 || count > 100000) {
            sendText(exchange, 400, "count must be between 1 and 100000");
            return;
        }
        if (points < 1 || points > 10000000) {
            sendText(exchange, 400, "points must be between 1 and 10000000");
            return;
        }
        if (party < 1 || party > 100) {
            sendText(exchange, 400, "party must be between 1 and 100");
            return;
        }

        try {
            String headerText = "Loot from " + count + " " + name
                    + " (" + points + " pts, " + party + " man):";
            HashMap<String, Integer> loot = CoX.runCoX(count, points, party);
            byte[] image = ImageGenerator.GenerateLootImage(loot, headerText);
            sendImage(exchange, image);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            sendText(exchange, 500, "Error: " + e.getMessage());
        }
    }

    static void handleSupports(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String json = "{"
                + "\"bosses\":" + toJsonArray(VALID_BOSSES) + ","
                + "\"raids\":" + toJsonArray(VALID_RAIDS)
                + "}";

        byte[] bytes = json.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    static String toJsonArray(Set<String> items) {
        return "[" + items.stream()
                .map(s -> "\"" + s + "\"")
                .collect(java.util.stream.Collectors.joining(","))
                + "]";
    }

    static void sendImage(HttpExchange exchange, byte[] image) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "image/png");
        exchange.sendResponseHeaders(200, image.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(image);
        }
    }

    static Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getQuery();
        if (query == null)
            return params;

        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0], kv[1]);
            }
        }
        return params;
    }

    static void sendText(HttpExchange exchange, int code, String message) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
