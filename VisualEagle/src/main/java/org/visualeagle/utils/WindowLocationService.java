package org.visualeagle.utils;

import org.visualeagle.gui.GuiUtils;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.RootPaneContainer;

/**
 * @author Dmitry
 */
public class WindowLocationService {

    private JComponent rootComponent;

    public WindowLocationService() {

    }

    public void setRootComponent(JComponent rootComponent) {
        this.rootComponent = rootComponent;
    }

    public String getName(RootPaneContainer frame) {
        String name = (String) frame.getRootPane().getClientProperty("name");
        if (name == null) {
            throw new IllegalArgumentException("Frame does not have client property 'name'");
        }

        return name;
    }

    public void setInitialState(RootPaneContainer frame, String defaultX, String defaultY, String defaultWidth, String defaultHeight, boolean defaultMaximized) {
        String name = getName(frame);
        int width;
        int height;
        if(rootComponent==null){
            width= Toolkit.getDefaultToolkit().getScreenSize().width;
            height= Toolkit.getDefaultToolkit().getScreenSize().height;
        }else{
            width=rootComponent.getWidth();
            height=rootComponent.getHeight();
        }
        int x = Settings.getIntProperty(name + ".x", GuiUtils.parseWidthString(defaultX, width));
        int y = Settings.getIntProperty(name + ".y", GuiUtils.parseWidthString(defaultY, height));
        int windowWidth = Settings.getIntProperty(name + ".width", GuiUtils.parseWidthString(defaultWidth, width));
        int windowHeight = Settings.getIntProperty(name + ".height", GuiUtils.parseWidthString(defaultHeight, height));
        boolean maximized = Settings.getBooleanProperty(name + ".maximized", defaultMaximized);

        if (frame instanceof JFrame) {
            JFrame jframe = (JFrame) frame;
            jframe.setLocation(x, y);
            jframe.setSize(windowWidth, windowHeight);
            if (maximized) {
                maximizeJFrame(jframe);
            }
        } else if (frame instanceof JInternalFrame) {
            JInternalFrame jframe = (JInternalFrame) frame;
            jframe.setLocation(x, y);
            jframe.setSize(windowWidth, windowHeight);
        }
    }

    private void maximizeJFrame(JFrame frame) {
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
    }

    private void onWindowStateChanged(RootPaneContainer frame, WindowEvent e) {
        String name = getName(frame);
        Settings.putBooleanProperty(name + ".maximized", (e.getNewState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH);
    }

    private ComponentAdapter createComponentAdapter(RootPaneContainer frame) {
        return new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                String name = getName(frame);
                Settings.putIntProperty(name + ".x", e.getComponent().getLocation().x);
                Settings.putIntProperty(name + ".y", e.getComponent().getLocation().y);
                Settings.flush();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                String name = getName(frame);
                Settings.putIntProperty(name + ".width", e.getComponent().getWidth());
                Settings.putIntProperty(name + ".height", e.getComponent().getHeight());
                Settings.flush();
            }
        };
    }

    public void register(RootPaneContainer frame) {
        if (frame instanceof JFrame) {
            ((JFrame) frame).addWindowStateListener(new WindowAdapter() {
                @Override
                public void windowStateChanged(WindowEvent e) {
                    onWindowStateChanged(frame, e);
                }
            });
            ((JFrame) frame).addComponentListener(createComponentAdapter(frame));
        } else {
            ((JInternalFrame) frame).addComponentListener(createComponentAdapter(frame));
        }
    }
}
