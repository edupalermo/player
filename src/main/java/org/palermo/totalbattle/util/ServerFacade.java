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
import org.palermo.totalbattle.server.model.Player;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ServerFacade {
    
    private static final String URI_AS_STRING = "http://tbserver:8080";

    private static final ObjectMapper objectMapper = createObjectMapper();
    
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2); 

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(DEFAULT_TIMEOUT)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

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

        HttpResponse<Player> response = client.send(request, jsonBodyHandler(objectMapper, Player.class));

        if (response.statusCode() == HttpStatus.SC_CONFLICT) {
            return Optional.empty();
        }
        
        if (response.statusCode() != HttpStatus.SC_OK) {
            throw new IllegalStateException("Unexpected HTTP status: " + response.statusCode());
        }

        return Optional.of(response.body());
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
    

    private static <T> HttpResponse.BodyHandler<T> jsonBodyHandler(
            ObjectMapper objectMapper,
            Class<T> type) {

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
}
