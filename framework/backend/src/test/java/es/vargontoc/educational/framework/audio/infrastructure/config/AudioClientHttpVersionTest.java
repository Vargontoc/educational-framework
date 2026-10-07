package es.vargontoc.educational.framework.audio.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.sun.net.httpserver.HttpServer;

/**
 * Regresion: con el HttpClient del JDK por defecto, el POST a Chatterbox (Uvicorn/FastAPI) iba con una cabecera
 * `Upgrade: h2c` y el servidor perdia el cuerpo (422 "body: Field required"). El cliente debe hablar HTTP/1.1.
 */
class AudioClientHttpVersionTest {

    private HttpServer server;
    private final AtomicReference<String> upgradeHeader = new AtomicReference<>();
    private final AtomicReference<String> receivedBody = new AtomicReference<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/audio/speech", exchange -> {
            upgradeHeader.set(exchange.getRequestHeaders().getFirst("Upgrade"));
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void audioClient_postsJsonBodyOverHttp11WithoutH2cUpgrade() {
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        RestClient client = new AudioConfiguration().getAudioClient(baseUrl, 3, 5);

        client.post().uri("/audio/speech")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("input", "¡Hola Angela!"))
            .retrieve()
            .body(byte[].class);

        assertNull(upgradeHeader.get(), "no debe pedir upgrade a h2c");
        assertEquals("{\"input\":\"¡Hola Angela!\"}", receivedBody.get());
    }
}
