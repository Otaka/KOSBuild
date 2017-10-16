package org.visualeagle.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * @author Dmitry
 */
public class ImageManager {

    private static ImageManager instance = new ImageManager();
    private Map<String, BufferedImage> imagesCache = new HashMap<>();

    private ImageManager() {
    }

    public static ImageManager get() {
        return instance;
    }

    public BufferedImage getImage(String name) {
        if (imagesCache.containsKey(name)) {
            return imagesCache.get(name);
        }

        String path = "/org/visualeagle/resources/icons/" + name + ".png";
        InputStream inputStream = ImageManager.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot find icon [" + name + "]");
        }

        try {
            BufferedImage image = ImageIO.read(inputStream);
            imagesCache.put(name, image);
            return image;
        } catch (IOException ex) {
            throw new RuntimeException("Error while downloading icon [" + name + "]", ex);
        }
    }

    public ImageIcon getIcon(String name) {
        BufferedImage image = getImage(name);
        return new ImageIcon(image);
    }
}
