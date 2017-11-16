package org.visualeagle.gui.connectionmanager.connectionwindow;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.visualeagle.gui.small.IconButton;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.ReturnCallback;

/**
 * @author Dmitry
 */
public class SelectConnectionTypePanel extends JPanel {

    private ReturnCallback<String> onSelect;

    public SelectConnectionTypePanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JPanel panelLeft = new JPanel();
        panelLeft.setLayout(new BoxLayout(panelLeft, BoxLayout.Y_AXIS));

        JPanel panelRight = new JPanel();
        panelRight.setLayout(new BoxLayout(panelRight, BoxLayout.Y_AXIS));

        add(panelLeft);
        add(panelRight);

        IconButton serverButton = new IconButton(ImageManager.get().getImage("pictureCreateServer"));
        serverButton.setPreferredSize(new Dimension(100, 100));
        IconButton copyAgentButton = new IconButton(ImageManager.get().getImage("pictureCopyAgent"));
        copyAgentButton.setPreferredSize(new Dimension(100, 100));

        panelLeft.add(serverButton);
        panelRight.add(copyAgentButton);

        JLabel serverLabel = new JLabel("Create server");
        serverLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        JLabel copyAgentLabel = new JLabel("Copy agent");
        copyAgentLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        JLabel clientLabel = new JLabel("Connect to server");
        clientLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panelLeft.add(serverLabel);
        panelRight.add(copyAgentLabel);
        panelRight.add(clientLabel);

        serverButton.setActionListener((ActionEvent e) -> {
            onSelect.data(null);
        });

        copyAgentButton.setActionListener(this::runCopyAgent);
    }

    private void runCopyAgent(ActionEvent e) {
        Window window = SwingUtilities.getWindowAncestor(this);
        CopyAgentDialog copyAgentDialog = new CopyAgentDialog(window);
        copyAgentDialog.setVisible(true);
    }

    public SelectConnectionTypePanel setOnSelectEvent(ReturnCallback<String> onSelect) {
        this.onSelect = onSelect;
        return this;
    }
}
