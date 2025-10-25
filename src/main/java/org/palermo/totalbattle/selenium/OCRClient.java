package org.palermo.totalbattle.selenium;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;

public class OCRClient {

    public static String perform(BufferedImage image) {
        return perform(image, " abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUWXYZ0123456789,.", "en");
    }

    public static String perform(BufferedImage image, String allowlist) {
        return perform(image, allowlist, "en");
    }

    public static String perform(BufferedImage image, String allowlist, String language) {
        try {
            // Convert BufferedImage to ByteArrayOutputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            URL url = new URL("http://localhost:5000/ocr");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            DataOutputStream requestStream = new DataOutputStream(conn.getOutputStream());

            // Add image part
            requestStream.writeBytes("--" + boundary + "\r\n");
            requestStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"image.png\"\r\n");
            requestStream.writeBytes("Content-Type: image/png\r\n\r\n");
            requestStream.write(imageBytes);
            requestStream.writeBytes("\r\n");

            // Add allowlist
            if (allowlist != null) {
                requestStream.writeBytes("--" + boundary + "\r\n");
                requestStream.writeBytes("Content-Disposition: form-data; name=\"allowlist\"\r\n\r\n");
                requestStream.writeBytes(allowlist + "\r\n");
            }

            // Add language
            if (language != null) {
                requestStream.writeBytes("--" + boundary + "\r\n");
                requestStream.writeBytes("Content-Disposition: form-data; name=\"language\"\r\n\r\n");
                requestStream.writeBytes(language + "\r\n");
            }

            // End boundary
            requestStream.writeBytes("--" + boundary + "--\r\n");
            requestStream.flush();
            requestStream.close();

            // Read response
            Scanner scanner = new Scanner(conn.getInputStream());
            StringBuilder responseBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                responseBuilder.append(scanner.nextLine());
            }
            scanner.close();

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
            JSONArray textArray = jsonResponse.getJSONArray("text");

            StringBuilder resultText = new StringBuilder();
            for (int i = 0; i < textArray.length(); i++) {
                resultText.append(textArray.getString(i)).append(" ");
            }

            return resultText.toString().trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}