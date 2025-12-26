package org.palermo.totalbattle.util;

import lombok.extern.slf4j.Slf4j;
import org.palermo.totalbattle.dao.OcrDao;
import org.palermo.totalbattle.entity.ProcessedImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class OcrUtil {

    private static final String HOSTNAME_NOTEBOOK = "eduardo-XPS-15-9500";
    
    public static final Pattern PATTERN_FOR_NUMBERS_WITH_THOUSAND_SEPARATOR = Pattern.compile("^(\\d{1,3})(,\\d{3})*$");
    public static final Pattern PATTERN_FOR_ONLY_NUMBERS = Pattern.compile("^[0-9]+$");
    public static final Pattern PATTERN_FOR_NUMBERS_WITH_MULTIPLIER = Pattern.compile("^[0-9]+(\\.[0-9]+)?[KM]?$");
    public static final Pattern PATTERN_FOR_COUNTDOWN = Pattern.compile("^(?:\\d{1,2}d:?\\d{1,2}h|" +
            "\\d{1,2}h:?\\d{1,2}m|" +
            "(?:\\d{1,2}m:?)?\\d{1,2}s)$");

    public static final int OCR_HEIGHT = 70;

    public static final String WHITELIST_FOR_SPEED_UPS = "0123456789dhm.";
    public static final String WHITELIST_FOR_COUNTDOWN = "0123456789:dhms";
    public static final String WHITELIST_FOR_ONLY_NUMBERS = "0123456789";
    public static final String WHITELIST_FOR_NUMBERS_WITH_THOUSAND_SEPARATOR = "0123456789,";
    public static final String WHITELIST_FOR_NUMBERS_AND_MULTIPLIER = "0123456789.KM";
    public static final String WHITELIST_FOR_NUMBERS_AND_SLASH_AND_MULTIPLIER = "0123456789,./K";
    public static final String WHITELIST_FOR_USERNAME = buildWhitelist("Mightshaper", "Palermo", "Peter II", "Grirana", "Elanin");
    public static final String WHITELIST_FOR_NUMBERS = "0123456789,";
    public static final int PSM_DEFAULT = 3;
    public static final int LINE_OF_PRINTED_TEXT = 6;
    public static final int SINGLE_LINE_MODE = 7;
    public static final int SINGLE_WORD_MODE = 8;
    public static final int PSM_SINGLE_CHARACTER = 10;
    public static final int PSM_SPARSE_TEXT = 11;

    private static OcrDao ocrDao = new OcrDao();

    public static String ocrBestMethod(BufferedImage image, String whitelist) {

        String result = ocr(image, whitelist, SINGLE_LINE_MODE, null);

        String temp = ocr(image, whitelist, SINGLE_WORD_MODE, null);
        if (temp.length() > result.length()) {
            result = temp;
        }

        temp = ocr(image, whitelist, PSM_SINGLE_CHARACTER, null);
        if (temp.length() > result.length()) {
            result = temp;
        }
        return result;
    }

    public static String ocr(BufferedImage image, String whitelist, int pageSegMode, String language) {
        return OcrUtil.callOcrService(image, language, pageSegMode, whitelist);
    }

    public static String ocr(BufferedImage image, String whitelist, int pageSegMode) {
        return ocr(image, whitelist, pageSegMode, null);
    }

    public static String ocr(BufferedImage image, String whitelist, Pattern pattern, boolean manualOcr) {

        try {
            List<ProcessedImage> list =  ocrDao.retrieve(image.getWidth(), image.getHeight(), whitelist);
            ProcessedImage databaseAnswer = list.stream()
                    .filter((pi) -> ImageUtil.compare(pi.getImage(), image, 0.05))
                    .findAny()
                    .orElse(null);
            if (databaseAnswer != null) {
                if (pattern.matcher(databaseAnswer.getText()).matches()) {
                    return databaseAnswer.getText();
                }
            }

            String stringValue = ocrBestMethod(image, whitelist);
            if (stringValue != null && stringValue.length() > 0) {
                if (pattern.matcher(stringValue).matches()) {
                    return stringValue;
                }
                else {
                    if (whitelist.equals(WHITELIST_FOR_COUNTDOWN)) {
                        stringValue = replaceLastDigitIfFive(stringValue);
                        stringValue = replaceSExceptLast(stringValue);
                        if (pattern.matcher(stringValue).matches()) {
                            return stringValue;
                        }
                    }
                    log.info("Tesseract returned a string that doesn't match the given pattern: " + stringValue);
                }
            }

            if (manualOcr || HOSTNAME_NOTEBOOK.equalsIgnoreCase(InetAddress.getLocalHost().getHostName())) {
                stringValue = askManualOcr(image);

                if (stringValue != null && stringValue.length() > 0) {
                    if (pattern.matcher(stringValue).matches()) {
                        ocrDao.persist(image, stringValue, whitelist);
                        return stringValue;
                    }
                }
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // ImageUtil.showImageFor5Seconds(image, "Fail to parse it as " + whitelist);

        File ocrFolder = createFolderIsThereIsNot(new File("."), "ocr");
        File file = new File(ocrFolder, Long.toString(ImageUtil.crcImage(image)) + ".png");
        if (!file.exists()) {
            ImageUtil.write(image, file);
        }

        throw new RuntimeException("It was not possible to make ocr of the given image!");
    }

    private static String callOcrService(
            BufferedImage image,    // input image
            String lang,             // "eng"
            int psm,                 // 0..13
            String whitelist         // nullable
    ) {

        try {
            String boundary = "----JavaBoundary" + UUID.randomUUID();
            byte[] body = buildMultipartBody(image, boundary);

            String query = String.format(
                    "/ocr?lang=%s&psm=%d%s",
                    lang,
                    psm,
                    (whitelist != null && !whitelist.isEmpty())
                            ? "&whitelist=" + urlEncode(whitelist)
                            : ""
            );
                                       
            URI baseUri = new URI("http://192.168.178.73:8000");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(baseUri.resolve(query))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();
            
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("OCR failed: " + response.body());
            }

            return extractTextFromJson(response.body());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] buildMultipartBody(BufferedImage image, String boundary)
            throws IOException {

        ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
        ImageIO.write(image, "png", imageOut);
        byte[] pngBytes = imageOut.toByteArray();

        ByteArrayOutputStream body = new ByteArrayOutputStream();
        String CRLF = "\r\n";

        body.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Disposition: form-data; name=\"file\"; filename=\"image.png\"" + CRLF)
                .getBytes(StandardCharsets.UTF_8));
        body.write(("Content-Type: image/png" + CRLF + CRLF)
                .getBytes(StandardCharsets.UTF_8));
        body.write(pngBytes);
        body.write(CRLF.getBytes(StandardCharsets.UTF_8));
        body.write(("--" + boundary + "--" + CRLF)
                .getBytes(StandardCharsets.UTF_8));

        return body.toByteArray();
    }

    private static String extractTextFromJson(String json) {
        // Expected: {"text":"ABC123"}
        int start = json.indexOf("\"text\":\"");
        if (start < 0) return "";
        start += 8;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private static String urlEncode(String s) {
        return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static File createFolderIsThereIsNot(File parentFolder, String name) {
        File file = new File(parentFolder, name);

        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new RuntimeException("Could not create folder " + name);
            }
        }
        return file;
    }

    private static String buildWhitelist(String... inputs) {
        Set<Character> uniqueChars = new LinkedHashSet<>();
        for (String input : inputs) {
            for (char c : input.toCharArray()) {
                uniqueChars.add(c);
            }
        }

        StringBuilder result = new StringBuilder();
        for (char c : uniqueChars) {
            result.append(c);
        }

        return result.toString();
    }

    private static String replaceLastDigitIfFive(String text) {
        // checks if the string ends with exactly 3 digits and the last one is 5
        if (text.matches(".*\\d{2}5$")) {
            return text.substring(0, text.length() - 1) + "s";
        }
        return text;
    }

    private static String replaceSExceptLast(String text) {
        if (text == null || text.isEmpty()) return text;

        int lastIndex = text.length() - 1;
        StringBuilder result = new StringBuilder(text.length());

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // replace 's' unless it's the final character
            if (c == 's' && i != lastIndex) {
                result.append('5');
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Shows a modal popup with the given image, a text field, and a confirm button.
     * Returns the text the user typed, or null if the user cancelled/closed the dialog.
     */
    private static String askManualOcr(BufferedImage image) {
        // Panel with image and text field
        JLabel imageLabel = new JLabel(new ImageIcon(image));

        JTextField textField = new JTextField(20);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.add(imageLabel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(new JLabel("OCR text:"), BorderLayout.WEST);
        bottomPanel.add(textField, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        // Custom button text
        String[] options = { "Confirm", "Cancel" };

        int result = JOptionPane.showOptionDialog(
                null,
                content,
                "Manual OCR",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        if (result == JOptionPane.OK_OPTION) {
            return textField.getText();
        } else {
            return null; // user cancelled or closed
        }
    }

    public static LocalDateTime ocrTimer(BufferedImage image, boolean invert) {
        BufferedImage timeLeft = ImageUtil.toGrayscale(image);
        if (invert) {
            timeLeft = ImageUtil.invertGrayscale(timeLeft);
        }
        timeLeft = ImageUtil.linearNormalization(timeLeft);

        if (timeLeft.getHeight() < 50) {
            timeLeft = ImageUtil.resize(timeLeft, 50);
        }

        String timeLeftAsText = ocr(timeLeft, WHITELIST_FOR_COUNTDOWN, LINE_OF_PRINTED_TEXT);
        System.out.println("Time Left: " + timeLeftAsText);

        LocalDateTime localDateTime = null;
        try {
            localDateTime = calculateNext(timeLeftAsText).orElse(null);
        } catch (Exception e) {
            ImageUtil.showImageFor5Seconds(image, "Fail to parse timer");
            throw e;
        }

        return localDateTime;
    }

    public static int ocrNumber(BufferedImage image, boolean invert) {
        BufferedImage timeLeft = ImageUtil.toGrayscale(image);
        if (invert) {
            timeLeft = ImageUtil.invertGrayscale(timeLeft);
        }
        timeLeft = ImageUtil.linearNormalization(timeLeft);

        if (timeLeft.getHeight() < 50) {
            timeLeft = ImageUtil.resize(timeLeft, 50);
        }

        String numberAsText = ocr(timeLeft, WHITELIST_FOR_NUMBERS, LINE_OF_PRINTED_TEXT);
        return Integer.parseInt(numberAsText);
    }

    private static Optional<LocalDateTime> calculateNext(String input) {
        Pattern pattern = Pattern.compile("(\\d+)h[:]?([\\d+]+)m");
        Matcher matcher = pattern.matcher(input.trim());

        int days = 0;
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        boolean parsed = false;

        if (matcher.matches()) {
            hours = Integer.parseInt(matcher.group(1));
            minutes = Integer.parseInt(matcher.group(2));
            parsed = true;
        }

        if (!parsed) {
            pattern = Pattern.compile("(\\d+)m[:]?([\\d+]+)5");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                minutes = Integer.parseInt(matcher.group(1));
                seconds = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }

        if (!parsed) {
            pattern = Pattern.compile("(\\d+)m[:]?([\\d+]+)s");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                minutes = Integer.parseInt(matcher.group(1));
                seconds = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }

        if (!parsed) {
            pattern = Pattern.compile("(\\d+)d[:]?([\\d+]+)h");
            matcher = pattern.matcher(input.trim());
            if (matcher.matches()) {
                days = Integer.parseInt(matcher.group(1));
                hours = Integer.parseInt(matcher.group(2));
                parsed = true;
            }
        }

        if (!parsed) {
            throw new RuntimeException("Impossible to parse " + input);
        }

        LocalDateTime answer = LocalDateTime.now()
                .plusDays(days)
                .plusHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds);

        return Optional.of(answer);
    }
}
