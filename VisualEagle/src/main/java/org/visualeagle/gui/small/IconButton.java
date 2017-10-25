package org.visualeagle.gui.small;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.visualeagle.utils.Utils;

/**
 * @author Dmitry
 */
public class IconButton extends JPanel {

    private BufferedImage[] image;
    private BufferedImage grayedImage;
    private Color backgroundColor;
    private Color borderColor;
    private boolean selected = false;
    private boolean pressed=false;
    private ActionListener actionListener;
    private Timer animationTimer;
    private int animationFrameIndex = 0;

    public IconButton(BufferedImage image, Color backgroundColor, Color borderColor) {
        setIcon(image);
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        initListeners();
    }

    public IconButton(BufferedImage image) {
        setIcon(image);
        this.backgroundColor = new Color(0, 0, 255, 50);
        this.borderColor = new Color(0, 0, 255, 100);
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
            public void mousePressed(MouseEvent e) {
                pressed=true;
                repaint();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                pressed=false;
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

    public void setIcon(BufferedImage... image) {
        this.image = image;
        grayedImage = null;
        animationFrameIndex = 0;
        restartTimer();
        repaint();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        restartTimer();
    }

    private void restartTimer() {
        if (isEnabled() && image.length > 1) {
            if (animationTimer == null) {
                animationTimer = createAnimationTimer();
            }
            if (!animationTimer.isRunning()) {
                animationTimer.start();
            }

        } else {
            if (animationTimer != null) {
                animationTimer.stop();
            }
        }
    }

    private Timer createAnimationTimer() {
        return new Timer(500, (ActionEvent e) -> {
            animationFrameIndex++;
            repaint();
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
            Color background=backgroundColor;
            if(pressed){
                background=background.darker().darker();
            }
            g.setColor(background);
            g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
            g.setColor(borderColor);
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }

        BufferedImage imageToDraw;
        if (isEnabled()) {
            imageToDraw = image[animationFrameIndex % image.length];
        } else {
            if (grayedImage == null) {
                grayedImage = createGrayscaledImage(image[0]);
            }
            imageToDraw = grayedImage;
        }

        int imageX = getWidth() / 2 - imageToDraw.getWidth() / 2;
        int imageY = getHeight() / 2 - imageToDraw.getHeight() / 2;
        g.drawImage(imageToDraw, imageX, imageY, this);
    }
}
