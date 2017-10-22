package org.visualeagle.gui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * @author Dmitry
 */
public class IconButton extends JPanel {

    private BufferedImage image;
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
                selected = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                selected = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (actionListener != null) {
                    actionListener.actionPerformed(new ActionEvent(IconButton.this, 0, "click"));
                }
            }
        });
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (selected) {
            g.setColor(backgroundColor);
            g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
            g.setColor(borderColor);
            g.drawRect(0, 0, getWidth()-1, getHeight()-1);
        }

        int imageX = getWidth() / 2 - image.getWidth() / 2;
        int imageY = getHeight() / 2 - image.getHeight() / 2;
        g.drawImage(image, imageX, imageY, this);
    }
}
