package org.palermo.totalbattle.util;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class OcrUtil {

    /**
     * Extracts OCR text from a BufferedImage using Google Cloud Vision.
     *
     * Requirements:
     * - GOOGLE_APPLICATION_CREDENTIALS env var points to a service account JSON
     *   OR you configured ADC via gcloud auth application-default login.
     */
    public static String extractLabelText(BufferedImage bufferedImage) throws IOException {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("bufferedImage must not be null");
        }

        // Convert BufferedImage -> PNG bytes (lossless)
        ByteString imgBytes;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            boolean ok = ImageIO.write(bufferedImage, "png", os);
            if (!ok) throw new IOException("No ImageIO writer found for PNG");
            imgBytes = ByteString.copyFrom(os.toByteArray());
        }

        Image visionImage = Image.newBuilder().setContent(imgBytes).build();

        // KEY CHANGE: TEXT_DETECTION is usually best for short text / labels
        Feature feature = Feature.newBuilder()
                .setType(Feature.Type.TEXT_DETECTION)
                .build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(visionImage)
                .build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse batch = client.batchAnnotateImages(List.of(request));
            if (batch.getResponsesCount() == 0) return "";

            AnnotateImageResponse res = batch.getResponses(0);

            if (res.hasError()) {
                throw new IOException("Vision API error: " + res.getError().getMessage());
            }

            // For TEXT_DETECTION, element 0 is typically the full text, others are words/parts.
            if (res.getTextAnnotationsCount() == 0) return "";

            String full = res.getTextAnnotations(0).getDescription();
            if (full == null) return "";

            // Labels often come with a trailing newline; normalize to a single line:
            return full.replace('\n', ' ').trim();
        }
    }
    
    public static void main(String[] intput) {
        System.getenv().forEach((key, val) ->
                System.out.println(key + "=" + val)
        );
    }
}
