package org.palermo.totalbattle.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.palermo.totalbattle.server.model.FlagInfo;
import org.palermo.totalbattle.server.model.FlagScenario;
import org.palermo.totalbattle.server.model.Player;
import org.palermo.totalbattle.util.bean.MatchResult;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ServerFacade {
    
    private static final String URI_AS_STRING = "http://192.168.178.73:8080";

    private static final ObjectMapper objectMapper = createObjectMapper();
    
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2); 

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(DEFAULT_TIMEOUT)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    @SneakyThrows
    public Optional<Player> retrievePlayer(String playerName) {
        URI uri = UriComponentsBuilder
                .fromUriString(URI_AS_STRING)
                .path("/player/{playerName}")
                .buildAndExpand(playerName)
                .encode()
                .toUri();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(DEFAULT_TIMEOUT)
                .GET()
                .build();

        HttpResponse<Player> response = client.send(request, jsonBodyHandler(Player.class));

        if (response.statusCode() == HttpStatus.SC_CONFLICT) {
            return Optional.empty();
        }

        if (response.statusCode() != HttpStatus.SC_OK) {
            throw new IllegalStateException("Unexpected HTTP status: " + response.statusCode());
        }

        return Optional.of(response.body());
    }
    
    
    @SneakyThrows
    public Optional<Player> startPlaying() {
        URI uri = UriComponentsBuilder
                .fromUriString(URI_AS_STRING)
                .path("/player/start").build()
                .toUri();
                
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(DEFAULT_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<Player> response = client.send(request, jsonBodyHandler(Player.class));

        if (response.statusCode() == HttpStatus.SC_CONFLICT) {
            return Optional.empty();
        }
        
        if (response.statusCode() != HttpStatus.SC_OK) {
            throw new IllegalStateException("Unexpected HTTP status: " + response.statusCode());
        }

        return Optional.of(response.body());
    }

    @SneakyThrows
    public void updatePlayer(Player player) {
        var uri = UriComponentsBuilder
                .fromUriString(URI_AS_STRING)
                .path("/player").build()
                .toUri();

        var request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(DEFAULT_TIMEOUT)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .PUT(jsonBody(player))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.discarding());

        if (response.statusCode() != HttpStatus.SC_OK) {
            throw new RuntimeException("Update Player returned " + response.statusCode());
        }
    }

    @SneakyThrows
    public void stopPlaying(Player player) {
        URI uri = UriComponentsBuilder
                .fromUriString(URI_AS_STRING)
                .path("/player/{playerName}/stop")
                .buildAndExpand(player.getName())
                .encode()
                .toUri();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(DEFAULT_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
                
        if (response.statusCode() != HttpStatus.SC_OK) {
            throw new IllegalStateException("Unexpected HTTP status: " + response.statusCode());
        }
    }

    @SneakyThrows
    public void flag(Player player, FlagScenario scenario, LocalDateTime expiration, String message) {
        Map pathParameters = Map.of("playerName", player.getName(),
                "scenario", scenario.name());
        URI uri = UriComponentsBuilder
                .fromUriString(URI_AS_STRING)
                .path("/player/{playerName}/flag/{scenario}")
                .buildAndExpand(pathParameters)
                .encode()
                .toUri();

        FlagInfo flagInfo = FlagInfo.builder()
                .createdAt(LocalDateTime.now())
                .expiration(expiration)
                .message(message)
                .build();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(DEFAULT_TIMEOUT)
                .PUT(jsonBody(flagInfo))
                .build();

        HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

        if (response.statusCode() != HttpStatus.SC_OK) {
            throw new IllegalStateException("Unexpected HTTP status: " + response.statusCode());
        }
    }

    @SneakyThrows
    public boolean hasFlag(Player player, FlagScenario scenario) {
        Map pathParameters = Map.of("playerName", player.getName(),
                "scenario", scenario.name());
        URI uri = UriComponentsBuilder
                .fromUriString(URI_AS_STRING)
                .path("/player/{playerName}/flag/{scenario}")
                .buildAndExpand(pathParameters)
                .encode()
                .toUri();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(DEFAULT_TIMEOUT)
                .GET()
                .build();

        HttpResponse<Player> response = client.send(request, jsonBodyHandler(Player.class));

        if (response.statusCode() != HttpStatus.SC_OK) {
            throw new IllegalStateException("Unexpected HTTP status: " + response.statusCode());
        }
        
        Player upToDatePlayer = response.body();
        FlagInfo flagInfo = upToDatePlayer.getFlags().get(scenario); 
        if (flagInfo == null) {
            return false;
        } 
        return flagInfo.getExpiration().isAfter(LocalDateTime.now());
    }

    private static <T> HttpResponse.BodyHandler<T> jsonBodyHandler(Class<T> type) {

        return responseInfo ->
                HttpResponse.BodySubscribers.mapping(
                        HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8),
                        body -> {
                            try {
                                return objectMapper.readValue(body, type);
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
    }


    private HttpRequest.BodyPublisher jsonBody(Object object) {
        try {
            return HttpRequest.BodyPublishers.ofString(
                    objectMapper.writeValueAsString(object)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not serialize request body", e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        javaTimeModule.addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(formatter)
        );

        javaTimeModule.addDeserializer(
                LocalDateTime.class,
                new LocalDateTimeDeserializer(formatter)
        );

        return JsonMapper.builder()
                .addModule(javaTimeModule)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    @SneakyThrows
    public Optional<MatchResult> compare(BufferedImage screen, BufferedImage template) {
        byte[] screenPng = toPng(screen);
        byte[] templatePng = toPng(template);

        String boundary = "----ImageMatcherBoundary" + UUID.randomUUID();

        byte[] body = createMultipartBody(
                boundary,
                screenPng,
                templatePng
        );

        URI uri = UriComponentsBuilder
                .fromUriString(URI_AS_STRING)
                .path("/image/find").build()
                .toUri();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(15))
                .header(
                        "Content-Type",
                        "multipart/form-data; boundary=" + boundary
                )
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<MatchResult> response = client.send(
                request,
                jsonBodyHandler(MatchResult.class));

        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Image matcher returned HTTP "
                            + response.statusCode()
                            + ": "
                            + response.body()
            );
        }

        return Optional.of(response.body());
    }

    @SneakyThrows
    private byte[] toPng(BufferedImage image) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        if (!ImageIO.write(image, "png", output)) {
            throw new IOException("Could not encode image as PNG");
        }

        return output.toByteArray();
    }

    private byte[] createMultipartBody(
            String boundary,
            byte[] screen,
            byte[] template
    ) throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        writePart(
                output,
                boundary,
                "screen",
                "screen.png",
                screen
        );

        writePart(
                output,
                boundary,
                "template",
                "template.png",
                template
        );

        output.write(
                ("--" + boundary + "--\r\n")
                        .getBytes(StandardCharsets.UTF_8)
        );

        return output.toByteArray();
    }

    private void writePart(
            ByteArrayOutputStream output,
            String boundary,
            String fieldName,
            String filename,
            byte[] data
    ) throws IOException {

        output.write(
                ("--" + boundary + "\r\n")
                        .getBytes(StandardCharsets.UTF_8)
        );

        output.write(
                ("Content-Disposition: form-data; name=\""
                        + fieldName
                        + "\"; filename=\""
                        + filename
                        + "\"\r\n")
                        .getBytes(StandardCharsets.UTF_8)
        );

        output.write(
                "Content-Type: image/png\r\n\r\n"
                        .getBytes(StandardCharsets.UTF_8)
        );

        output.write(data);
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }    
    
}
