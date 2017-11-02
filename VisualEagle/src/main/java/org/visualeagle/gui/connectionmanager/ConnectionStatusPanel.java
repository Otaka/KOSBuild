package org.visualeagle.gui.connectionmanager;

import org.visualeagle.gui.connectionmanager.connectionwindow.NewConnectionWindow;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.visualeagle.gui.mainwindow.MainWindow;
import org.visualeagle.gui.small.IconButton;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;
import org.visualeagle.utils.ActorMessage;
import org.visualeagle.utils.Utils;

public class ConnectionStatusPanel extends JPanel {

    private IconButton connectDisconnectButton;
    private ConnectionManager connectionManager;
    private ConnectionStatus lastConnectionStatus;
    private JLabel label;

    public ConnectionStatusPanel() {
        init();
    }

    private void init() {
        setPreferredSize(new Dimension(200, 25));
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        connectDisconnectButton = createConnectDisconnectButton();
        add(connectDisconnectButton);
        label = new JLabel("Not connected");
        add(label);
        setDisconnectedStatus();
        createConnectionManager();
    }

    private void createConnectionManager() {
        connectionManager = Lookup.get().get(ConnectionManager.class);
        int delay = 500;
        Timer timer = new Timer(delay, (ActionEvent e) -> {
            checkStatus();
        });

        timer.setInitialDelay(20);
        timer.setRepeats(true);
        timer.start();
    }

    private void checkStatus() {
        ConnectionStatus newConnectionStatus = connectionManager.getConnectionStatus();
        if (newConnectionStatus != lastConnectionStatus) {
            lastConnectionStatus = newConnectionStatus;
            switch (lastConnectionStatus) {
                case CONNECTED:
                    setConnectedStatus();break;
                case DISCONNECTED:
                    setDisconnectedStatus();break;
                case CONNECTING:
                    setConnectingStatus();break;
                default:
                    throw new IllegalArgumentException("Unknow status [" + lastConnectionStatus + "]");
            }
        }
    }

    private IconButton createConnectDisconnectButton() {
        IconButton button = new IconButton(ImageManager.get().getImage("disconnected")) {
            @Override
            public Point getToolTipLocation(MouseEvent event) {
                return new Point(30, -30);
            }
        };

        button.setPreferredSize(new Dimension(20, 20));
        button.setActionListener(this::buttonPressed);
        return button;
    }

    private void buttonPressed(ActionEvent event) {
        SwingUtilities.invokeLater(() -> {
            if (connectionManager.getConnectionStatus() == ConnectionStatus.CONNECTED || connectionManager.getConnectionStatus() == ConnectionStatus.CONNECTING) {
                connectDisconnectButton.setEnabled(false);
                connectionManager.sendMessage(new ActorMessage("close"), (Object originalMessage, Object result, Throwable exception) -> {
                    connectDisconnectButton.setEnabled(true);
                    checkStatus();
                    if (exception != null) {
                        Utils.showErrorMessage("Cannot do disconnect [" + exception.getMessage() + "]");
                    }
                }, true);
            } else {
                NewConnectionWindow connectionWindow = new NewConnectionWindow();
                connectionWindow.setLocationRelativeTo(Lookup.get().get(MainWindow.class));
                connectionWindow.setVisible(true);
            }
        });
    }

    private void setDisconnectedStatus() {
        connectDisconnectButton.setIcon(ImageManager.get().getImage("disconnected"));
        connectDisconnectButton.setToolTipText("Press to connect to remote KolibriOs device");
        label.setText("Not connected");
    }

    private void setConnectedStatus() {
        connectDisconnectButton.setIcon(ImageManager.get().getImage("connected"));
        connectDisconnectButton.setToolTipText("Press to disconnect");
        label.setText("Connected");
    }

    private void setConnectingStatus() {
        connectDisconnectButton.setIcon(ImageManager.get().getImage("connecting_1"), ImageManager.get().getImage("connecting_2"));
        connectDisconnectButton.setToolTipText("Connecting...");
        label.setText("Connecting...");
    }
}
