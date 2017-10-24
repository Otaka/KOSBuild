package org.visualeagle.utils;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dmitry
 */
public class Utils {

    public static int[] generateRangeIntArray(int start, int size) {
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = start + i;
        }

        return result;
    }

    public static <T> T first(List<T> array) {
        if (array.isEmpty()) {
            throw new IllegalArgumentException("Array is empty. Cannot get first element");
        }

        return array.get(0);
    }

    public static <T> T first(T[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty. Cannot get first element");
        }
        return array[0];
    }

    public static int first(int[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty. Cannot get first element");
        }
        return array[0];
    }

    public static <T> T last(List<T> array) {
        if (array.isEmpty()) {
            throw new IllegalArgumentException("Array is empty. Cannot get last element");
        }
        return array.get(array.size() - 1);
    }

    public static <T> T last(T[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty. Cannot get last element");
        }
        return array[array.length - 1];
    }

    public static int last(int[] array) {
        if (array.length == 0) {
            throw new IllegalArgumentException("Array is empty. Cannot get last element");
        }
        return array[array.length - 1];
    }

    public static void sortFiles(File[] files) {
        Arrays.sort(files, (File o1, File o2) -> {
            if (o1.isDirectory() == o2.isDirectory()) {
                return o1.compareTo(o2);
            }
            if (o1.isDirectory()) {
                return -1;
            }
            return 1;
        });
    }

    public static BufferedImage convertToGrayscaleWithAlphaChannel(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = newImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        byte[] pixels = ((DataBufferByte) newImage.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < pixels.length; i += 4) {
            int val = (int) ((0.299f * (pixels[i + 1] & 0xff)) + (0.587f * (pixels[i + 2] & 0xff)) + (0.114f * (pixels[i + 3] & 0xff)));
            pixels[i + 1] = (byte) val;
            pixels[i + 2] = (byte) val;
            pixels[i + 3] = (byte) val;
        }

        return newImage;
    }
}
