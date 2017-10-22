package org.visualeagle.gui.components.editorwindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.visualeagle.gui.components.IconButton;
import org.visualeagle.utils.ImageManager;

/**
 * @author Dmitry
 */
public class TabComponent extends JPanel {

    private JLabel label;
    private boolean modified = false;
    private IconButton close;

    public TabComponent(String title) {
        init(title);
    }

    private void init(String title) {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        label = new JLabel();
        label.setText(title);
        add(label);
        close = new IconButton(ImageManager.get().getImage("closeTabIcon"), new Color(200,80,80).brighter(),new Color(220,100,100).brighter());
        close.setPreferredSize(new Dimension(11, 11));
        close.setMaximumSize(close.getPreferredSize());

        add(close);
    }

    public void addCloseEvent(ActionListener actionListener) {
        close.setActionListener(actionListener);
    }

    public void setModified() {
        if (modified == false) {
            modified = true;
            label.setFont(label.getFont().deriveFont(Font.BOLD));
        }
    }

    public void clearModified() {
        if (modified == true) {
            modified = false;
            label.setFont(label.getFont().deriveFont(0));
        }
    }
}
