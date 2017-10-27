package org.visualeagle.gui.connectionmanager.connectionwindow;

import fi.iki.elonen.NanoHTTPD;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;
import org.visualeagle.utils.ImageManager;

/**
 * @author Dmitry
 */
public class CopyAgentDialog extends JDialog {

    private byte[] agentByteBuffer;
    private int port = 80;
    private NanoHTTPD server;
    private JLabel statusLabel;

    public CopyAgentDialog(Window parent) {
        super(parent, "Copy agent", ModalityType.APPLICATION_MODAL);
        startServer();
        setIconImage(ImageManager.get().getImage("eagle"));
        add(new JLabel(ImageManager.get().getIcon("pictureCopyAgent")), BorderLayout.WEST);
        JPanel outlineContentPanel = new JPanel();
        outlineContentPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        JLabel label = new JLabel("<html>You can open link in browser or with kolibri downloader application</html>");
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        contentPanel.add(label);

        contentPanel.add(new JLabel(" "));

        for (String ip : getAllNetworkInterfaces()) {
            JLabel label2 = new JLabel("http://" + ip + ":" + port + "/files/agent");
            contentPanel.add(label2);
        }
        contentPanel.add(new JLabel(" "));
        statusLabel = new JLabel("Waiting...");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        contentPanel.add(statusLabel);
        outlineContentPanel.add(contentPanel);
        add(outlineContentPanel);
        readAgentBytes();
        pack();
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopServer();
            }
        });
    }

    private void setStatusText(String text) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(text);
        });
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    private void startServer() {
        for (int tPort = 8080; tPort < 9000; tPort++) {
            try {
                NanoHTTPD tServer = new NanoHTTPD(tPort) {
                    @Override
                    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession session) {
                        String ip = session.getRemoteIpAddress();
                        setStatusText("Start to copy agent from [" + ip + "]");
                        Response response = new Response(Response.Status.OK, "application/octet-stream", new ByteArrayInputStream(agentByteBuffer), agentByteBuffer.length) {
                            @Override
                            public void close() throws IOException {
                                setStatusText("Agent copied to [" + ip + "]");
                                super.close();
                            }
                        };
                        response.closeConnection(true);
                        return response;
                    }
                };

                tServer.start();
                this.server = tServer;
                this.port = tPort;
                return;
            } catch (Exception ex) {
                continue;
            }
        }
    }

    private List<String> getAllNetworkInterfaces() {
        try {
            List<String> result = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();
                    if (address instanceof Inet4Address) {
                        result.add(getIpAddress(address.getAddress()));
                    }
                }
            }

            return result;
        } catch (SocketException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getIpAddress(byte[] rawBytes) {
        int i = 4;
        String ipAddress = "";
        for (byte raw : rawBytes) {
            ipAddress += (raw & 0xFF);
            if (--i > 0) {
                ipAddress += ".";
            }
        }

        return ipAddress;
    }

    private void readAgentBytes() {
        String path = "/org/visualeagle/resources/agent/agent";
        try (InputStream agentInputStream = CopyAgentDialog.class.getResourceAsStream(path)) {
            if (agentInputStream == null) {
                throw new IllegalArgumentException("Cannot find resource \"/org/visualeagle/resources/agent/agent\"");
            }

            agentByteBuffer = IOUtils.toByteArray(agentInputStream);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
