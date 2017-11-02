package org.visualeagle.gui.connectionmanager.connectionwindow;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import org.visualeagle.gui.connectionmanager.ConnectionManager;
import org.visualeagle.utils.ActorMessage;
import org.visualeagle.utils.Lookup;
import org.visualeagle.utils.MessageProcessedEvent;

/**
 * @author Dmitry
 */
public class ConnectAsServerPanel extends JPanel {

    private JLabel titleLabel;
    private JTextField portTextField;
    private JButton startServerButton;
    private Timer timer;

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

        startServerButton.addActionListener((ActionEvent e) -> {
            startServer();
        });
    }

    private int getPort() {
        return Integer.parseInt(portTextField.getText());
    }

    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
        timer = null;
    }

    private void startTimer() {
        stopTimer();
        timer = new Timer(500, (ActionEvent e) -> {
            if (!isValid()) {
                timer.stop();
                return;
            }

            ConnectionManager connectionManager = Lookup.get().get(ConnectionManager.class);
            titleLabel.setText(connectionManager.getStatusMessage());
        });

        timer.setRepeats(true);
        timer.start();
    }

    private void startServer() {
        startTimer();
        int port;
        try {
            port = getPort();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Port should be integer", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        startServerButton.setEnabled(false);
        portTextField.setEnabled(false);
        ConnectionManager connectionManager = Lookup.get().get(ConnectionManager.class);
        connectionManager.sendMessage(new ActorMessage("create_server", new ConnectionManager.CreateServerMessage(port)), (MessageProcessedEvent) (Object originalMessage, Object result, Throwable exception) -> {
            if (exception != null) {
                startServerButton.setEnabled(true);
                portTextField.setEnabled(true);
                stopTimer();
                titleLabel.setText("Error [" + exception.getMessage() + "]");
            } else {
                titleLabel.setText((String) result);
            }
        }, true);
    }
}
