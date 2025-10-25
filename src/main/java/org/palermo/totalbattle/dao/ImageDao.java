package org.palermo.totalbattle.dao;

import org.palermo.totalbattle.entity.NamedImage;
import org.palermo.totalbattle.entity.TextImage;

import java.nio.ByteBuffer;
import java.sql.Connection;

public class ImageDao {
    
    private final Connection conn;
    
    public ImageDao(Connection conn) {
        this.conn = conn;
    }
    
    public TextImage persist(TextImage imageEntity) {
        
        
        
        return null;
    }

    public NamedImage persistNamedImage(String name, int[][] image) {

        return null;
    }

    public NamedImage findByName(String name) {
        return null;
    }


    public static byte[] longArrayToBytes(int[][] image) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * image.length * image[0].length);
        for (int x = 0; x < image.length; x++) {
            for (int y = 0; y < image[0].length; y++) {
                buffer.putInt(image[x][y]);
            }
        }
        return buffer.array();
    }
    
    public static int[][] bytesToLongArray(byte[] bytes, int width, int height) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        int[][] image = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image[x][y] = buffer.getInt() ;
            }
        }
        return image;
    }
}
