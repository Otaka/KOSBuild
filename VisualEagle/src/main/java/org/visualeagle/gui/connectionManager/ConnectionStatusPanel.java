package org.visualeagle.gui.connectionManager;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import org.visualeagle.gui.small.IconButton;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;

/**
 * @author sad
 */
public class ConnectionStatusPanel extends JPanel {

    private IconButton connectDisconnectButton;
    private ConnectionManager connectionManager;

    public ConnectionStatusPanel() {
        setPreferredSize(new Dimension(200, 25));
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        connectDisconnectButton = createConnectDisconnectButton();
        setDisconnectedStatus();
        add(connectDisconnectButton);

        connectionManager = Lookup.get().get(ConnectionManager.class);
        connectionManager.addConnectionListener(new ConnectionEvent() {
            @Override
            public void connected() {
                setConnectedStatus();
            }

            @Override
            public void disconnected() {
                setDisconnectedStatus();
            }

            @Override
            public void connecting() {
                setConnectingStatus();
            }
        });
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
        if (connectionManager.isConnected()) {
            connectionManager.closeConnection();
        } else {
            setConnectingStatus();
            NewConnectionWindow connectionWindow = new NewConnectionWindow();
            connectionWindow.setVisible(true);
            setDisconnectedStatus();
        }
    }

    private void setDisconnectedStatus() {
        connectDisconnectButton.setIcon(ImageManager.get().getImage("disconnected"));
        connectDisconnectButton.setToolTipText("Press to connect to remote KolibriOs device");
    }

    private void setConnectedStatus() {
        connectDisconnectButton.setIcon(ImageManager.get().getImage("connected"));
        connectDisconnectButton.setToolTipText("Press to disconnect");
    }

    private void setConnectingStatus() {
        connectDisconnectButton.setIcon(ImageManager.get().getImage("connecting_1"), ImageManager.get().getImage("connecting_2"));
        connectDisconnectButton.setToolTipText("Connecting...");
    }
}
