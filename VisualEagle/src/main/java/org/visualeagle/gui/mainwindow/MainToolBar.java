package org.visualeagle.gui.mainwindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.visualeagle.gui.small.IconButton;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;
import org.visualeagle.utils.RunnableWithParams;

/**
 * @author sad
 */
public class MainToolBar extends JPanel {

    private static Color borderColor = new Color(160, 160, 160);
    private List<JComponent> componentsThatRequireProject = new ArrayList<>();

    public MainToolBar() {
        init();
    }

    private void init() {
        setPreferredSize(new Dimension(10, 27));
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        addButton("Create new project","newProject", "new_project", false);
        addButton("Open existing project","openProject", "open_project", false);
        add(new SeparatorPanel(25));

        addButton("Save file","save", "save_file", true);
        addButton("Save all modified files","save_all", "save_all", true);
        add(new SeparatorPanel(25));

        addButton("Undo","undo", "undo", true);
        addButton("Redo","redo", "redo", true);
        add(new SeparatorPanel(25));

        addButton("Cut","cut", "cut", true);
        addButton("Copy","copy", "copy", true);
        addButton("Paste","paste", "paste", true);
        add(new SeparatorPanel(25));

        addButton("Clean and install project","build", "clean_install", true);
        add(new SeparatorPanel(25));
        addButton("Run application","run", "run_app", true);
        addButton("Debug application","debug", "debug_app", true);
        projectOpened(false);
        Lookup.get().get(ActionManager.class).registerAction("projectOpened", new RunnableWithParams() {
            @Override
            public void run(Object param) throws Exception {
                projectOpened(true);
            }
        });

        Lookup.get().get(ActionManager.class).registerAction("projectClosed", new RunnableWithParams() {
            @Override
            public void run(Object param) throws Exception {
                projectOpened(false);
            }
        });
    }

    private void projectOpened(boolean opened) {
        for (JComponent c : componentsThatRequireProject) {
            c.setEnabled(opened);
        }
    }

    private void addButton(String tooltip, String image, String action, boolean requireProject) {
        IconButton iconButton = new IconButton(ImageManager.get().getImage(image));
        iconButton.setPreferredSize(new Dimension(25, 25));
        iconButton.setToolTipText(tooltip);
        iconButton.setActionListener((ActionEvent e) -> {
            Lookup.get().get(ActionManager.class).fire(action);
        });

        add(iconButton);
        if (requireProject) {
            componentsThatRequireProject.add(iconButton);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(borderColor);
        g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
    }
}
