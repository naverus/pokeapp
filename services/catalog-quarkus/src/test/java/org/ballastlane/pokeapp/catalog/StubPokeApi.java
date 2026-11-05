package org.ballastlane.pokeapp.catalog;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** In-process PokeAPI double serving bulbasaur (1) and ivysaur (2). */
public final class StubPokeApi {

  public static final int PORT = 18089;
  private static final String BASE = "https://pokeapi.co/api/v2";

  private final HttpServer server;
  private final Map<String, AtomicInteger> hits = new ConcurrentHashMap<>();
  private final AtomicBoolean down = new AtomicBoolean();

  private StubPokeApi(HttpServer server) {
    this.server = server;
  }

  public static StubPokeApi start() throws IOException {
    var server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
    var stub = new StubPokeApi(server);
    server.createContext("/api/v2", stub::handle);
    server.start();
    return stub;
  }

  public void stop() {
    server.stop(0);
  }

  public void setDown(boolean value) {
    down.set(value);
  }

  public void reset() {
    hits.clear();
    down.set(false);
  }

  public int hits(String path) {
    var counter = hits.get(path);
    return counter == null ? 0 : counter.get();
  }

  public int totalHits() {
    return hits.values().stream().mapToInt(AtomicInteger::get).sum();
  }

  private void handle(HttpExchange exchange) throws IOException {
    var path = exchange.getRequestURI().getPath().substring("/api/v2".length());
    hits.computeIfAbsent(path, key -> new AtomicInteger()).incrementAndGet();
    if (down.get()) {
      respond(exchange, 500, "{\"detail\":\"boom\"}");
      return;
    }
    var query = exchange.getRequestURI().getQuery();
    var body = route(path, query);
    if (body == null) {
      respond(exchange, 404, "Not Found");
    } else {
      respond(exchange, 200, body);
    }
  }

  private static String route(String path, String query) {
    return switch (path) {
      case "/pokemon" -> list(query);
      case "/pokemon/1", "/pokemon/bulbasaur" -> pokemon(1, "bulbasaur");
      case "/pokemon/2", "/pokemon/ivysaur" -> pokemon(2, "ivysaur");
      case "/pokemon-species/1" -> species(1, "bulbasaur", "Seed Pok\u00e9mon");
      case "/pokemon-species/2" -> species(2, "ivysaur", "Seed Pok\u00e9mon");
      case "/evolution-chain/1" -> chain();
      default -> null;
    };
  }

  private static String list(String query) {
    if (query != null && query.contains("offset=100")) {
      return "{\"count\":2,\"results\":[]}";
    }
    return """
        {"count":2,"next":null,"previous":null,"results":[
          {"name":"bulbasaur","url":"%1$s/pokemon/1/"},
          {"name":"ivysaur","url":"%1$s/pokemon/2/"}]}
        """
        .formatted(BASE);
  }

  private static String pokemon(int id, String name) {
    return """
        {"id":%2$d,"name":"%3$s","height":7,"weight":69,"order":1,
         "sprites":{"front_default":"https://sprites/%2$d.png","back_default":null,
           "other":{"official-artwork":{"front_default":"https://art/%2$d.png"}}},
         "types":[{"slot":2,"type":{"name":"poison","url":""}},{"slot":1,"type":{"name":"grass","url":""}}],
         "abilities":[{"slot":1,"is_hidden":false,"ability":{"name":"overgrow","url":""}}],
         "stats":[{"base_stat":45,"effort":0,"stat":{"name":"hp","url":""}},
                  {"base_stat":49,"effort":0,"stat":{"name":"attack","url":""}}],
         "species":{"name":"%3$s","url":"%1$s/pokemon-species/%2$d/"}}
        """
        .formatted(BASE, id, name);
  }

  private static String species(int id, String name, String genus) {
    return """
        {"id":%2$d,"name":"%3$s",
         "genera":[{"genus":"Samen-Pokemon","language":{"name":"de","url":""}},
                   {"genus":"%4$s","language":{"name":"en","url":""}}],
         "flavor_text_entries":[{"flavor_text":"A strange seed was\\nplanted on its\\fback.","language":{"name":"en","url":""}}],
         "evolution_chain":{"url":"%1$s/evolution-chain/1/"}}
        """
        .formatted(BASE, id, name, genus);
  }

  private static String chain() {
    return """
        {"id":1,"chain":{"species":{"name":"bulbasaur","url":"%1$s/pokemon-species/1/"},
          "evolves_to":[{"species":{"name":"ivysaur","url":"%1$s/pokemon-species/2/"},"evolves_to":[]}]}}
        """
        .formatted(BASE);
  }

  private static void respond(HttpExchange exchange, int status, String body) throws IOException {
    var bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
    exchange.sendResponseHeaders(status, bytes.length);
    try (var out = exchange.getResponseBody()) {
      out.write(bytes);
    }
  }
}
