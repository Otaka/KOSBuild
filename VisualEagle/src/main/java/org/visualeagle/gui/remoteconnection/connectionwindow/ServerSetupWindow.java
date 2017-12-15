package org.visualeagle.gui.remoteconnection.connectionwindow;

import com.asyncsockets.SocketHandler;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.visualeagle.gui.mainwindow.MainWindow;
import org.visualeagle.gui.remoteconnection.ConnectionStatusChangedEvent;
import org.visualeagle.gui.remoteconnection.RemoteConnectionManager;
import org.visualeagle.utils.Lookup;
import org.visualeagle.utils.Settings;

/**
 * @author sad
 */
public class ServerSetupWindow extends JDialog {

    private JLabel statusLabel;
    private JTextField portTextField;
    private ConnectionStatusChangedEvent statusChangedEvent;
    private RemoteConnectionManager connectionManager;

    public ServerSetupWindow() throws HeadlessException {
        super(Lookup.get().get(MainWindow.class), true);
        setTitle("Server setup window");
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Please choose a port");
        label.setAlignmentX(LEFT_ALIGNMENT);
        getContentPane().add(label);

        portTextField = new JTextField();
        portTextField.setText("8085");
        portTextField.setPreferredSize(new Dimension(150, 25));
        portTextField.setMaximumSize(new Dimension(150, 25));
        portTextField.setAlignmentX(LEFT_ALIGNMENT);
        getContentPane().add(portTextField);

        JButton connectButton = new JButton("Start server");
        connectButton.addActionListener(this::onServerStartPressed);
        getContentPane().add(connectButton);

        statusLabel = new JLabel("Not connected");
        statusLabel.setMinimumSize(new Dimension(400, 25));
        statusLabel.setPreferredSize(new Dimension(400, 25));
        statusLabel.setAlignmentX(LEFT_ALIGNMENT);
        getContentPane().add(statusLabel);

        pack();
        setLocationRelativeTo(Lookup.get().get(MainWindow.class));

        connectionManager = Lookup.get().get(RemoteConnectionManager.class);
        statusChangedEvent = new ConnectionStatusChangedEvent() {
            @Override
            public void clientConnected(SocketHandler socketHandler) {
                statusLabel.setText("Client connected:"+socketHandler.getRemoteAddress());
            }

            @Override
            public void clientDisconnected() {
                statusLabel.setText("Client disconnected");
            }

            @Override
            public void serverStarted() {
                statusLabel.setText("Server started. Waiting for connection");
                connectButton.setEnabled(false);
            }

            @Override
            public void serverStopped() {
                statusLabel.setText("Server stopped.");
                connectButton.setEnabled(true);
            }

            @Override
            public void error(Throwable thr) {
                statusLabel.setText("Error:" + ExceptionUtils.getRootCauseMessage(thr));
            }
        };

        connectionManager.addConnectionEvent(statusChangedEvent);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                connectionManager.removeConnectionEvent(statusChangedEvent);
            }
        });
    }

    private void onServerStartPressed(ActionEvent e) {
        int port;
        try {
            port = Integer.parseInt(portTextField.getText().trim());
        } catch (Exception ex) {
            statusLabel.setText("Error:Please provide proper port number");
            return;
        }
        try {
            connectionManager.createServer(port);
        } catch (IOException ex) {
            ex.printStackTrace();
            statusLabel.setText("Error:" + ExceptionUtils.getRootCauseMessage(ex));
        }

        writeServerPortToSettings(port);
    }

    private void writeServerPortToSettings(int port){
        Settings.putIntProperty("connectionManager.port", port);
        Settings.flush();
    }
}
