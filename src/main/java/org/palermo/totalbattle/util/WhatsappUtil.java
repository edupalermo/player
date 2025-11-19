package org.palermo.totalbattle.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WhatsappUtil {

    private static final String API_URL = "https://api.callmebot.com/whatsapp.php";
    private static final String API_KEY = "6581370";
    private static final String PHONE = "+491795487467";

    public static void main(String[] args) {
        send("My test!");
    }
    
    public static void send(String message)  {
        try {
            sendMessage(PHONE, API_KEY, message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void sendMessage(String phone, String apiKey, String message) throws IOException {
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

        String urlStr = API_URL
                + "?phone=" + URLEncoder.encode(phone, StandardCharsets.UTF_8)
                + "&text=" + encodedMessage
                + "&apikey=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        System.out.println("HTTP status: " + status);

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        status >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                        StandardCharsets.UTF_8))) {

            String line;
            StringBuilder response = new StringBuilder();
            while ((line = in.readLine()) != null) {
                response.append(line).append('\n');
            }
            System.out.println("Response: " + response);
        } finally {
            conn.disconnect();
        }
    }
}
