package org.visualeagle.gui.small;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.visualeagle.utils.Utils;

/**
 * @author Dmitry
 */
public class IconButton extends JPanel {

    private BufferedImage image;
    private BufferedImage grayedImage;
    private Color backgroundColor;
    private Color borderColor;
    private boolean selected = false;
    private ActionListener actionListener;

    public IconButton(BufferedImage image, Color backgroundColor, Color borderColor) {
        this.image = image;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        initListeners();
    }

    private void initListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    selected = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selected = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (isEnabled()) {
                    if (actionListener != null) {
                        actionListener.actionPerformed(new ActionEvent(IconButton.this, 0, "click"));
                    }
                }
            }
        });
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    private BufferedImage createGrayscaledImage(BufferedImage colorImage) {
        BufferedImage newImage = Utils.convertToGrayscaleWithAlphaChannel(colorImage);
        return newImage;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (selected) {
            g.setColor(backgroundColor);
            g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
            g.setColor(borderColor);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }

        BufferedImage imageToDraw;
        if (isEnabled()) {
            imageToDraw = image;
        } else {
            if (grayedImage == null) {
                grayedImage = createGrayscaledImage(image);
            }
            imageToDraw = grayedImage;
        }

        int imageX = getWidth() / 2 - imageToDraw.getWidth() / 2;
        int imageY = getHeight() / 2 - imageToDraw.getHeight() / 2;
        g.drawImage(imageToDraw, imageX, imageY, this);
    }
}
