package org.palermo.totalbattle.util.bean;

import lombok.SneakyThrows;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class Histogram {

    protected final long[][][] data;
    
    private Histogram(long[][][] data) {
        this.data = data;
    }
    
    public Histogram intersect(Histogram input) {
        long[][][] result = new long[256][256][256];
        
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    if (this.data[i][j][k] <= input.data[i][j][k]) {
                        result[i][j][k] = this.data[i][j][k];    
                    }
                    else {
                        System.out.println(String.format("%02X%02X%02X %d -> %d", i, j, k, this.data[i][j][k], input.data[i][j][k]));
                        result[i][j][k] = input.data[i][j][k];
                    }
                }
            }
        }
        return new Histogram(result);
    }

    public boolean contained(Histogram input) {
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    if (this.data[i][j][k] > input.data[i][j][k]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    public Histogram minus(Histogram input) {
        long[][][] result = new long[256][256][256];

        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    if (input.data[i][j][k] >= this.data[i][j][k]) {
                        result[i][j][k] = 0;
                    }
                    else {
                        result[i][j][k] = this.data[i][j][k];
                    }
                }
            }
        }
        return new Histogram(result);
    }

    public long getWeight() {
        long total  = 0;

        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 256; k++) {
                    total = total + this.data[i][j][k];;
                }
            }
        }
        return total;
    }

    public static Histogram from(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        long[][][] data = new long[256][256][256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = image.getRGB(x, y);

                // Extract color components
                int alpha = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if (alpha == 0) {
                    continue; // Transparent
                }
                data[r][g][b] = data[r][g][b] + 1;
            }
        }
        return new Histogram(data);
    }

    @SneakyThrows
    public void save( Path file) {
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file)))) {

            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data.length; j++) {
                    for (int k = 0; k < data.length; k++) {
                        if (data[i][j][k] > 0) {
                            out.writeInt(i);
                            out.writeInt(j);
                            out.writeInt(k);
                            out.writeLong(data[i][j][k]);
                        }
                    }
                }
            }
        }
    }

    public static Histogram load(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            return load(is);
        }
    }

    private static Histogram load(InputStream is) throws IOException {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(is))) {

            long[][][] data = new long[256][256][256];

            try {
                while (true) {
                    int i = in.readInt();
                    int j = in.readInt();
                    int k = in.readInt();
                    long value = in.readLong();

                    data[i][j][k] = value;
                }
            } catch (EOFException ignored) {
                // End of file
            }

            return new Histogram(data);
        }
    }


    public static Histogram loadResource(String resourceName) {
        try (InputStream is = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                if (resourceName.charAt(0) != '/') {
                    return loadResource("/" + resourceName);
                }
                throw new RuntimeException("Resource not found: " + resourceName);
            }
            return load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
