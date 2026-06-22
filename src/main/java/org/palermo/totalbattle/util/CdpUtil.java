package org.palermo.totalbattle.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.palermo.totalbattle.util.bean.ChromeTab;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;

// Chrome DevTools Protocol (CDP)
@Log4j2
public class CdpUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    public static void closeAllTabsExceptOne() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();

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

    public static void clearBorwserCache() {
        try {
            HttpClient client = HttpClient.newHttpClient();

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

            /*
            webSocket.sendText("""
                    {
                      "id": 2,
                      "method": "Network.clearBrowserCookies"
                    }
                    """, true).join();
             */

            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
