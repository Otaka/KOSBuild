package org.visualeagle.gui.connectionManager.connectionwindow;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.visualeagle.gui.connectionManager.AbstractSession;
import org.visualeagle.gui.connectionManager.ServerSession;
import org.visualeagle.gui.connectionmanager.ConnectionManager;
import org.visualeagle.utils.Lookup;
import org.visualeagle.utils.Utils;

/**
 * @author Dmitry
 */
public class ConnectAsServerPanel extends JPanel {

    private JLabel titleLabel;
    private JTextField portTextField;
    private JButton startServerButton;

    public ConnectAsServerPanel() {
        JPanel contentPanel = new JPanel();

        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        add(contentPanel);

        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(new JLabel("Please enter port"));
        portTextField = new JTextField("8085");
        portTextField.setPreferredSize(new Dimension(300, 25));
        portTextField.setMaximumSize(new Dimension(300, 25));
        portTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        contentPanel.add(portTextField);
        contentPanel.add(new JLabel(""));
        startServerButton = new JButton("Start server");
        startServerButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        contentPanel.add(startServerButton);

        contentPanel.add(new JLabel(""));
        titleLabel = new JLabel("Stopped");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titleLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        startServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });
    }

    private int getPort() {
        return Integer.parseInt(portTextField.getText());
    }

    private void startServer() {
        int port;
        try {
            port = getPort();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Port should be integer", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Utils.runInThread(() -> {
            try {
                SwingUtilities.invokeLater(() -> {
                    startServerButton.setEnabled(false);
                    portTextField.setEnabled(false);
                    titleLabel.setText("Waiting for incoming connection");
                });

                ServerSession serverSession = new ServerSession(port);
                serverSession.initConnection();
                SwingUtilities.invokeLater(() -> {
                    titleLabel.setText("Connected");
                    Lookup.get().get(ConnectionManager.class).setSession(serverSession);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    titleLabel.setText("Error while waiting for connection [" + ex.getMessage() + "]");
                    portTextField.setEnabled(true);
                    startServerButton.setEnabled(true);
                });
            }
        });

    }
}
