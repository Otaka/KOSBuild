package org.visualeagle.gui.mainwindow;

import org.visualeagle.gui.projectnavigation.RecentItemsProcessor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.visualeagle.gui.mainwindow.ActionManager;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;

/**
 * @author Dmitry
 */
public class MainMenu {

    private ActionManager actionManager;

    public MainMenu() {
        actionManager = Lookup.get().get(ActionManager.class);
    }

    public JMenuBar constructMainMenu() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createProjectMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        fileMenu.add(createJMenuItem("New Project...", "new_project", "ctrl shift N", "newProject"));
        fileMenu.add(createJMenuItem("Open Project...", "open_project", "ctrl shift O", "openProject"));
        fileMenu.add(createJMenuItem("Close Project", "close_project", null));
        JMenu recentItemMenu = new JMenu("Recent Projects...");
        recentItemMenu.setEnabled(true);
        RecentItemsProcessor recentItemsProcessor = new RecentItemsProcessor(recentItemMenu);
        Lookup.get().put(RecentItemsProcessor.class, recentItemsProcessor);
        fileMenu.add(recentItemMenu);
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem("Save", "save_file", "ctrl S", "save"));
        fileMenu.add(createJMenuItem("Save All", "save_all", null, "save_all"));
        fileMenu.addSeparator();

        fileMenu.add(createJMenuItem("Exit", "exit", null));
        return fileMenu;
    }

    private JMenu createEditMenu() {
        JMenu fileMenu = new JMenu("Edit");
        fileMenu.setMnemonic('E');
        fileMenu.add(createJMenuItem("Undo", "undo", "ctrl Z", "undo"));
        fileMenu.add(createJMenuItem("Redo", "redo", "ctrl shift Z", "redo"));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem("Cut", "cut", "ctrl X", "cut"));
        fileMenu.add(createJMenuItem("Copy", "copy", "ctrl C", "copy"));
        fileMenu.add(createJMenuItem("Paste", "paste", "ctrl V", "paste"));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem("Select All", "select_all", "ctrl A"));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem("Find...", "find", "ctrl F"));
        fileMenu.add(createJMenuItem("Replace...", "replace", "ctrl H"));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem("Preferences...", "preferences", null));
        return fileMenu;
    }

    private JMenu createProjectMenu() {
        JMenu fileMenu = new JMenu("Project");
        fileMenu.setMnemonic('P');
        fileMenu.add(createJMenuItem("Clean", "clean", null, null));
        fileMenu.add(createJMenuItem("Build", "build", null, null));
        fileMenu.add(createJMenuItem("Install", "install", null, null));
        fileMenu.add(createJMenuItem("Clean Install", "clean_install", null, null));
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem("Run", "run_app", "F9", "run"));
        fileMenu.add(createJMenuItem("Debug", "debug_app", "ctrl F9", "debug"));
        return fileMenu;
    }

    private JMenu createHelpMenu() {
        JMenu fileMenu = new JMenu("Help");
        fileMenu.setMnemonic('H');

        fileMenu.add(createJMenuItem("Help Content", "help_content", null, "help"));
        fileMenu.add(createJMenuItem("About", "help_about", null));
        return fileMenu;
    }

    private JMenuItem createJMenuItem(String text, String action, String hotKey) {
        return createJMenuItem(text, action, hotKey, null);
    }

    private JMenuItem createJMenuItem(final String text, final String action, String hotKey, String icon) {
        JMenuItem menuItem = new JMenuItem(text);
        if (hotKey != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(hotKey));
        }

        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Action [" + action + "]");
                actionManager.fire(action);
            }
        });

        if (icon != null) {
            menuItem.setIcon(ImageManager.get().getIcon(icon));
        }

        return menuItem;
    }

}
