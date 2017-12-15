package org.visualeagle.gui.remoteconnection;

import com.asyncsockets.SocketHandler;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.visualeagle.gui.remoteconnection.connectionwindow.ServerSetupWindow;
import org.visualeagle.gui.small.IconButton;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;

public class ConnectionStatusPanel extends JPanel {

    private IconButton connectDisconnectButton;
    private RemoteConnectionManager connectionManager;
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
        addPopupMenu();
        setDisconnectedStatus();
        initConnectionManager();

    }

    private void addPopupMenu() {
        connectDisconnectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    JPopupMenu popupMenu = new JPopupMenu();
                }
            }
        });
    }

    private void initConnectionManager() {
        connectionManager = Lookup.get().get(RemoteConnectionManager.class);
        connectionManager.addConnectionEvent(new ConnectionStatusChangedEvent() {
            @Override
            public void clientConnected(SocketHandler socketHandler) {
                setConnectedStatus(socketHandler.getRemoteAddress().toString());
            }

            @Override
            public void clientDisconnected() {
                setConnectingStatus();
            }

            @Override
            public void serverStarted() {
                setConnectingStatus();
            }

            @Override
            public void serverStopped() {
                setDisconnectedStatus();
            }

            @Override
            public void error(Throwable thr) {
                setDisconnectedStatus();
                connectDisconnectButton.setToolTipText(ExceptionUtils.getRootCauseMessage(thr));
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
        SwingUtilities.invokeLater(() -> {
            if (connectionManager.isConnectionEstablished() || connectionManager.isServerStarted()) {
                connectionManager.stopServer();
            } else {
                showServerSetupWindow();
            }
        });
    }

    private void showServerSetupWindow() {
        ServerSetupWindow serverSetupWindow = new ServerSetupWindow();
        serverSetupWindow.setVisible(true);
    }

    private void setDisconnectedStatus() {
        connectDisconnectButton.setIcon(ImageManager.get().getImage("disconnected"));
        connectDisconnectButton.setToolTipText("Press to connect to remote KolibriOs device");
        label.setText("Not connected");
    }

    private void setConnectedStatus(String remoteAddress) {
        connectDisconnectButton.setIcon(ImageManager.get().getImage("connected"));
        connectDisconnectButton.setToolTipText("Connected to [" + remoteAddress + "]. Press to disconnect");
        label.setText("Connected");
    }

    private void setConnectingStatus() {
        int port = connectionManager.getServerPort();
        connectDisconnectButton.setIcon(ImageManager.get().getImage("connecting_1"), ImageManager.get().getImage("connecting_2"));
        connectDisconnectButton.setToolTipText("Waiting for incoming connection on port " + port);
        label.setText("Connecting...");
    }
}
