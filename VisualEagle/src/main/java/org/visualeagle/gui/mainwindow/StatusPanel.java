package org.visualeagle.gui.mainwindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.visualeagle.utils.Lookup;

/**
 * @author Dmitry
 */
public class StatusPanel extends JPanel {

    private JLabel cursorPositionLabel;
    private static int defaultComponentHeight = 20;
    private static Color borderColor = new Color(160, 160, 160);

    public StatusPanel() {
        init();
    }

    private void init() {
        setPreferredSize(new Dimension(10, 27));
        setOpaque(true);
        setLayout(new FlowLayout(FlowLayout.TRAILING));
        cursorPositionLabel = new JLabel("0:0", JLabel.CENTER);
        JPanel cursorPositionPanel = new JPanel();
        cursorPositionPanel.setPreferredSize(new Dimension(100, defaultComponentHeight));
        cursorPositionPanel.setLayout(new BorderLayout(0, 0));
        cursorPositionPanel.add(cursorPositionLabel);
        add(new SeparatorPanel());
        add(cursorPositionPanel);
        Lookup.get().addChangeEvent("cursorPosition", (String oldValue, String newValue) -> {
            cursorPositionLabel.setText(newValue);
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(borderColor);
        g.drawLine(0, 0, getWidth(), 0);
        g.setColor(Color.WHITE);
        g.drawLine(0, 1, getWidth(), 1);
    }


}
