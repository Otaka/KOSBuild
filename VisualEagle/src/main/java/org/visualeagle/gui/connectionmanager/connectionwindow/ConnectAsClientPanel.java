package org.visualeagle.gui.connectionmanager.connectionwindow;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import org.visualeagle.gui.connectionmanager.ConnectionManager;
import org.visualeagle.utils.ActorMessage;
import org.visualeagle.utils.Lookup;
import org.visualeagle.utils.MessageProcessedEvent;
import org.visualeagle.utils.Utils;

/**
 * @author Dmitry
 */
public class ConnectAsClientPanel extends JPanel {

    private JLabel titleLabel;
    private JTextField hostTextField;
    private JButton connectToServerButton;
    private Timer timer;

    public ConnectAsClientPanel() {
        JPanel contentPanel = new JPanel();
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        add(contentPanel);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.add(new JLabel("Please enter hostname:"));
        hostTextField = new JTextField("");
        hostTextField.setPreferredSize(new Dimension(300, 25));
        hostTextField.setMaximumSize(new Dimension(300, 25));
        hostTextField.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        contentPanel.add(hostTextField);
        contentPanel.add(new JLabel(""));
        connectToServerButton = new JButton("Connect to server");
        connectToServerButton.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        contentPanel.add(connectToServerButton);
        contentPanel.add(new JLabel(""));
        titleLabel = new JLabel("Stopped");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titleLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        connectToServerButton.addActionListener((ActionEvent e) -> {
            connectToServer();
        });
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

    private void connectToServer() {
        startTimer();
        String hostName = hostTextField.getText().trim();
        if (hostName.isEmpty()) {
            Utils.showErrorMessage("Please enter hostname");
            return;
        }

        connectToServerButton.setEnabled(false);
        hostTextField.setEnabled(false);
        ConnectionManager connectionManager = Lookup.get().get(ConnectionManager.class);
        connectionManager.sendMessage(new ActorMessage("connect_to_client", new ConnectionManager.ConnectToServerMessage(hostName)), (MessageProcessedEvent) (Object originalMessage, Object result, Throwable exception) -> {
            if (exception != null) {
                connectToServerButton.setEnabled(true);
                hostTextField.setEnabled(true);
                stopTimer();
                titleLabel.setText("Error [" + exception.getMessage() + "]");
            } else {
                titleLabel.setText((String) result);
            }
        }, true);
    }
}
