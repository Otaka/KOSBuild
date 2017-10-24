package org.visualeagle.gui.mainwindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 * @author sad
 */
public class SeparatorPanel extends JPanel {

    private static int defaultComponentHeight = 20;
    private static Color borderColor = new Color(160, 160, 160);

    public SeparatorPanel() {
        setPreferredSize(new Dimension(2, defaultComponentHeight));
    }
    
    public SeparatorPanel(int height) {
        setPreferredSize(new Dimension(2, height));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(borderColor);
        g.drawLine(0, 0, 0, getHeight());
        g.setColor(Color.WHITE);
        g.drawLine(1, 0, 1, getHeight());
    }

}
