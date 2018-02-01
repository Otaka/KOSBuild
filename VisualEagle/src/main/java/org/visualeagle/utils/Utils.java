package org.visualeagle.utils;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.visualeagle.gui.remotewindow.fileprovider.RFile;

/**
 * @author Dmitry
 */
public class Utils {

    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;

    public static String convertToStringRepresentation(final long value) {
        if(value==0){
            return "0 B";
        }
        final long[] dividers = new long[]{T, G, M, K, 1};
        final String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};
        if (value < 1) {
            throw new IllegalArgumentException("Invalid file size: " + value);
        }
        String result = null;
        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    private static String format(final long value,
            final long divider,
            final String unit) {
        final double result
                = divider > 1 ? (double) value / (double) divider : (double) value;
        return new DecimalFormat("#,##0.#").format(result) + " " + unit;
    }

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

    private static Executor executor = Executors.newCachedThreadPool();

    public static void runInThread(Runnable runnable) {
        executor.execute(runnable);
    }

    public static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static String[] splitFilePath(RFile file) {
        String path = file.getFullPath();
        path = path.replace('\\', '/').replace("//", "/").replace("./", "");

        String[] parts = StringUtils.splitPreserveAllTokens(path, '/');
        if (parts.length > 0) {
            if ("".equals(parts[parts.length - 1])) {
                parts = Arrays.copyOfRange(parts, 0, parts.length - 1);
            }
        }
        return parts;
    }
}
