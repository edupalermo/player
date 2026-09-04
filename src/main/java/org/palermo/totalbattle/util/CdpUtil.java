package org.palermo.totalbattle.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.palermo.totalbattle.util.bean.ChromeTab;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

// Chrome DevTools Protocol (CDP)
@Log4j2
public class CdpUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void closeAllTabsExceptOne() {
        try {
            // 1️⃣ Get list of tabs via CDP (Chrome DevTools Protocol)
            HttpRequest listRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:9222/json"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> listResponse =
                    client.send(listRequest, HttpResponse.BodyHandlers.ofString());

            if (listResponse.statusCode() != 200) {
                throw new RuntimeException("Failed to list tabs: " + listResponse.body());
            }

            List<ChromeTab> tabs = MAPPER.readValue(
                    listResponse.body(),
                    new TypeReference<List<ChromeTab>>() {}
            );
            
            //log.info("Number of tabs {}",  tabs.size());
            
            // Nothing to do
            if (tabs.size() <= 1) {
                return;
            }
            
            String saved = null;
            
            for (int i = 0; i < tabs.size(); i++) {
                ChromeTab tab = tabs.get(i);
                
                
                if (tab.getUrl().startsWith("https://www.totalbattle") || tab.getUrl().startsWith("https://totalbattle")) {
                    if (saved == null) {
                        saved = tab.getId();
                        continue;
                    }
                        
                    if (tab.getId() == saved) {
                        continue;
                    }
                    
                    //log.info("Closing {}",  tab.toString());

                    HttpRequest closeRequest = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:9222/json/close/" + tab.getId()))
                            .timeout(Duration.ofSeconds(5))
                            .GET()
                            .build();

                    client.send(closeRequest, HttpResponse.BodyHandlers.discarding());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to close Chrome tabs", e);
        }
    }

    
    public static boolean clickOnAcceptAll() {
        String expression = """
        (() => {
            const span = [...document.querySelectorAll('span')]
                .find(element => element.textContent.trim() === 'Accept all');

            if (!span) {
                return false;
            }

            const clickable = span.closest('button, a, [role="button"]') || span;
            clickable.click();

            return true;
        })()
        """;
        return evaluate(expression);
    }

    public static boolean isPageLoaded() {
        String expression = """
            document.readyState === 'complete'
            """;

        return evaluate(expression);
    }    

    @SneakyThrows
    public static boolean evaluate(String expression) {
        String webSocketUrl = getWebSocketDebuggerUrl();

        CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();

        WebSocket webSocket = client.newWebSocketBuilder()
                .buildAsync(
                        URI.create(webSocketUrl),
                        new WebSocket.Listener() {

                            private final StringBuilder response = new StringBuilder();

                            @Override
                            public CompletionStage<?> onText(
                                    WebSocket webSocket,
                                    CharSequence data,
                                    boolean last) {

                                response.append(data);

                                if (last) {
                                    try {
                                        JsonNode root = objectMapper.readTree(response.toString());

                                        if (root.path("id").asInt() == 1) {

                                            /*
                                            System.out.println(
                                                    objectMapper.writerWithDefaultPrettyPrinter()
                                                            .writeValueAsString(root)
                                            );
                                            */

                                            JsonNode exceptionDetails = root
                                                    .path("result")
                                                    .path("exceptionDetails");

                                            if (!exceptionDetails.isMissingNode()) {
                                                resultFuture.completeExceptionally(
                                                        new RuntimeException(
                                                                "JavaScript error: " + exceptionDetails
                                                        )
                                                );
                                                return null;
                                            }

                                            JsonNode result = root
                                                    .path("result")
                                                    .path("result");

                                            //System.out.println("Type: " + result.path("type"));
                                            //System.out.println("Value: " + result.path("value"));

                                            resultFuture.complete(
                                                    result.path("value").asBoolean()
                                            );
                                        }
                                        
                                    } catch (Exception e) {
                                        resultFuture.completeExceptionally(e);
                                    }
                                }

                                webSocket.request(1);
                                return null;
                            }

                            @Override
                            public void onOpen(WebSocket webSocket) {
                                webSocket.request(1);
                            }

                            @Override
                            public void onError(WebSocket webSocket, Throwable error) {
                                resultFuture.completeExceptionally(error);
                            }
                        }
                )
                .join();

        ObjectNode command = objectMapper.createObjectNode();

        command.put("id", 1);
        command.put("method", "Runtime.evaluate");

        ObjectNode params = command.putObject("params");
        params.put("expression", expression);
        params.put("returnByValue", true);

        webSocket.sendText(
                objectMapper.writeValueAsString(command),
                true
        ).join();

        boolean result = resultFuture.get(5, TimeUnit.SECONDS);

        webSocket.sendClose(
                WebSocket.NORMAL_CLOSURE,
                "done"
        ).join();

        return result;
    }
    
    @SneakyThrows
    private static String getWebSocketDebuggerUrl() {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:9222/json"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode tabs = mapper.readTree(response.body());

        if (!tabs.isArray() || tabs.isEmpty()) {
            throw new IllegalStateException("No Chrome tabs found");
        }

        String webSocketDebuggerUrl = tabs.get(0)
                .path("webSocketDebuggerUrl")
                .asText();

        if (webSocketDebuggerUrl.isBlank()) {
            throw new IllegalStateException("webSocketDebuggerUrl not found");
        }
        return webSocketDebuggerUrl;
    }
    
    public static void clearBrowserCache() {
        try {

            String webSocketDebuggerUrl = getWebSocketDebuggerUrl();

            WebSocket webSocket = client.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .buildAsync(URI.create(webSocketDebuggerUrl), new WebSocket.Listener() {
                        @Override
                        public CompletionStage<?> onText(WebSocket webSocket,
                                                         CharSequence data,
                                                         boolean last) {
                            System.out.println("CDP response: " + data);
                            return WebSocket.Listener.super.onText(webSocket, data, last);
                        }
                    })
                    .join();

            webSocket.sendText("""
                    {
                      "id": 1,
                      "method": "Network.clearBrowserCache"
                    }
                    """, true).join();

            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    @SneakyThrows
    public static Map<String, String> getTotalBattleCookies() {
        String webSocketUrl = getWebSocketDebuggerUrl();

        CompletableFuture<Map<String, String>> resultFuture =
                new CompletableFuture<>();

        WebSocket webSocket = client.newWebSocketBuilder()
                .buildAsync(
                        URI.create(webSocketUrl),
                        new WebSocket.Listener() {

                            private final StringBuilder response =
                                    new StringBuilder();

                            @Override
                            public CompletionStage<?> onText(
                                    WebSocket webSocket,
                                    CharSequence data,
                                    boolean last) {

                                response.append(data);

                                if (last) {
                                    try {
                                        JsonNode root = objectMapper.readTree(
                                                response.toString()
                                        );

                                        if (root.path("id").asInt() == 1) {

                                            Map<String, String> cookies =
                                                    new LinkedHashMap<>();

                                            JsonNode cookieArray = root
                                                    .path("result")
                                                    .path("cookies");

                                            for (JsonNode cookie : cookieArray) {
                                                cookies.put(
                                                        cookie.path("name").asText(),
                                                        cookie.path("value").asText()
                                                );
                                            }

                                            resultFuture.complete(cookies);
                                        }

                                        response.setLength(0);

                                    } catch (Exception e) {
                                        resultFuture.completeExceptionally(e);
                                    }
                                }

                                webSocket.request(1);
                                return null;
                            }

                            @Override
                            public void onOpen(WebSocket webSocket) {
                                webSocket.request(1);
                            }

                            @Override
                            public void onError(
                                    WebSocket webSocket,
                                    Throwable error) {

                                resultFuture.completeExceptionally(error);
                            }
                        }
                )
                .join();

        ObjectNode command = objectMapper.createObjectNode();

        command.put("id", 1);
        command.put("method", "Network.getCookies");

        ObjectNode params = command.putObject("params");
        ArrayNode urls = params.putArray("urls");

        urls.add("https://totalbattle.com/en/");

        webSocket.sendText(
                objectMapper.writeValueAsString(command),
                true
        ).join();

        Map<String, String> cookies =
                resultFuture.get(5, TimeUnit.SECONDS);

        webSocket.sendClose(
                WebSocket.NORMAL_CLOSURE,
                "done"
        ).join();

        return cookies;
    }


    @SneakyThrows
    public static void setTotalBattleCookies(Map<String, String> cookies) {
        String webSocketUrl = getWebSocketDebuggerUrl();

        WebSocket webSocket = client.newWebSocketBuilder()
                .buildAsync(
                        URI.create(webSocketUrl),
                        new WebSocket.Listener() {

                            @Override
                            public void onOpen(WebSocket webSocket) {
                                webSocket.request(1);
                            }

                            @Override
                            public CompletionStage<?> onText(
                                    WebSocket webSocket,
                                    CharSequence data,
                                    boolean last) {

                                webSocket.request(1);
                                return null;
                            }
                        }
                )
                .join();

        int id = 1;

        for (Map.Entry<String, String> entry : cookies.entrySet()) {

            ObjectNode command = objectMapper.createObjectNode();

            command.put("id", id++);
            command.put("method", "Network.setCookie");

            ObjectNode params = command.putObject("params");

            params.put("name", entry.getKey());
            params.put("value", entry.getValue());
            params.put("url", "https://totalbattle.com/en/");

            webSocket.sendText(
                    objectMapper.writeValueAsString(command),
                    true
            ).join();
        }

        webSocket.sendClose(
                WebSocket.NORMAL_CLOSURE,
                "done"
        ).join();
    }    
}
