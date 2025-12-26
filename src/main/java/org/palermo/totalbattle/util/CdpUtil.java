package org.palermo.totalbattle.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.palermo.totalbattle.util.bean.ChromeTab;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

// Chrome DevTools Protocol (CDP)
@Log4j2
public class CdpUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    public static void closeAllTabsExceptOne() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();

            // 1️⃣ Get list of tabs
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
            
            log.info("Number of tabs {}",  tabs.size());
            
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
                    
                    log.info("Closing {}",  tab.toString());

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
}
