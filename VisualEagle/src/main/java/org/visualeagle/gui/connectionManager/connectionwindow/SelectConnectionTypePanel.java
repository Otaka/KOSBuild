package org.visualeagle.gui.connectionManager.connectionwindow;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

    private ReturnCallback<ConnectionType> onSelect;

    public SelectConnectionTypePanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JPanel panelLeft = new JPanel();
        panelLeft.setLayout(new BoxLayout(panelLeft, BoxLayout.Y_AXIS));

        JPanel panelCenter = new JPanel();
        panelCenter.setLayout(new BoxLayout(panelCenter, BoxLayout.Y_AXIS));

        JPanel panelRight = new JPanel();
        panelRight.setLayout(new BoxLayout(panelRight, BoxLayout.Y_AXIS));

        add(panelLeft);
        add(panelCenter);
        add(panelRight);

        IconButton serverButton = new IconButton(ImageManager.get().getImage("pictureCreateServer"));
        serverButton.setPreferredSize(new Dimension(100, 100));
        IconButton copyAgentButton = new IconButton(ImageManager.get().getImage("pictureCopyAgent"));
        copyAgentButton.setPreferredSize(new Dimension(100, 100));
        IconButton clientButton = new IconButton(ImageManager.get().getImage("pictureConnectToRemoteServer"));
        clientButton.setPreferredSize(new Dimension(100, 100));

        panelLeft.add(serverButton);
        panelCenter.add(copyAgentButton);
        panelRight.add(clientButton);

        JLabel serverLabel = new JLabel("Create server");
        serverLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        JLabel copyAgentLabel = new JLabel("Copy agent");
        copyAgentLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        JLabel clientLabel = new JLabel("Connect to server");
        clientLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        panelLeft.add(serverLabel);
        panelCenter.add(copyAgentLabel);
        panelRight.add(clientLabel);

        serverButton.setActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSelect.data(ConnectionType.SERVER);
            }
        });

        copyAgentButton.setActionListener(this::runCopyAgent);

        serverButton.setActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSelect.data(ConnectionType.SERVER);
            }
        });
    }

    private void runCopyAgent(ActionEvent e) {
        Window window=SwingUtilities.getWindowAncestor(this);
        CopyAgentDialog copyAgentDialog=new CopyAgentDialog(window);
        copyAgentDialog.setVisible(true);
    }

    public SelectConnectionTypePanel setOnSelectEvent(ReturnCallback<ConnectionType> onSelect) {
        this.onSelect = onSelect;
        return this;
    }
}
