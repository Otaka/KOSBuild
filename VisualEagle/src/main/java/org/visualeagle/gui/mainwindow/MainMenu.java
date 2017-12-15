package org.visualeagle.gui.mainwindow;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.visualeagle.utils.ConfigNames;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;

/**
 * @author Dmitry
 */
public class MainMenu {

    private ActionManager actionManager;
    private String[]requireLoadedProjectLookupFlag=new String[]{ConfigNames.PROJECT_IS_LOADED};
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
        fileMenu.add(createJMenuItem("New Project...", "new_project", "ctrl shift N", "newProject",null));
        fileMenu.add(createJMenuItem("Open Project...", "open_project", "ctrl shift O", "openProject",null));
        fileMenu.add(createJMenuItem("Close Project", "close_project", null, null,requireLoadedProjectLookupFlag));
        JMenu recentItemMenu = new JMenu("Recent Projects...");
        recentItemMenu.setEnabled(true);
        RecentItemsProcessor recentItemsProcessor = new RecentItemsProcessor(recentItemMenu);
        Lookup.get().put(RecentItemsProcessor.class, recentItemsProcessor);
        fileMenu.add(recentItemMenu);
        fileMenu.addSeparator();
        fileMenu.add(createJMenuItem("Save", "save_file", "ctrl S", "save",requireLoadedProjectLookupFlag));
        fileMenu.add(createJMenuItem("Save All", "save_all", null, "save_all",requireLoadedProjectLookupFlag));
        fileMenu.addSeparator();

        fileMenu.add(createJMenuItem("Exit", "exit", null, null,null));
        processEnableDisableRequireProjectMenu(fileMenu);
        return fileMenu;
    }

    private JMenu createEditMenu() {

        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        editMenu.add(createJMenuItem("Undo", "undo", "ctrl Z", "undo",requireLoadedProjectLookupFlag));
        editMenu.add(createJMenuItem("Redo", "redo", "ctrl shift Z", "redo",requireLoadedProjectLookupFlag));
        editMenu.addSeparator();
        editMenu.add(createJMenuItem("Cut", "cut", "ctrl X", "cut",requireLoadedProjectLookupFlag));
        editMenu.add(createJMenuItem("Copy", "copy", "ctrl C", "copy",requireLoadedProjectLookupFlag));
        editMenu.add(createJMenuItem("Paste", "paste", "ctrl V", "paste",requireLoadedProjectLookupFlag));
        editMenu.addSeparator();
        editMenu.add(createJMenuItem("Select All", "select_all", "ctrl A", null,requireLoadedProjectLookupFlag));
        editMenu.addSeparator();
        editMenu.add(createJMenuItem("Find...", "find", "ctrl F", null,requireLoadedProjectLookupFlag));
        editMenu.add(createJMenuItem("Replace...", "replace", "ctrl H", null,requireLoadedProjectLookupFlag));
        editMenu.addSeparator();
        editMenu.add(createJMenuItem("Preferences...", "preferences", null, null,null));
        processEnableDisableRequireProjectMenu(editMenu);
        return editMenu;
    }

    private JMenu createProjectMenu() {
        JMenu projectMenu = new JMenu("Project");
        projectMenu.setMnemonic('P');
        projectMenu.add(createJMenuItem("Clean", "clean", null, null,requireLoadedProjectLookupFlag));
        projectMenu.add(createJMenuItem("Install", "install", null, null,requireLoadedProjectLookupFlag));
        projectMenu.add(createJMenuItem("Clean Install", "clean_install", "shift F11", "build",requireLoadedProjectLookupFlag));
        projectMenu.addSeparator();
        projectMenu.add(createJMenuItem("Run", "run_app", "F9", "run",requireLoadedProjectLookupFlag));
        projectMenu.add(createJMenuItem("Debug", "debug_app", "ctrl F9", "debug",requireLoadedProjectLookupFlag));
        projectMenu.addSeparator();
        projectMenu.add(createJMenuItem("Remote commander", "remote_commander", null, null,new String[]{"connectedClient"}));
        processEnableDisableRequireProjectMenu(projectMenu);
        return projectMenu;
    }

    private JMenu createHelpMenu() {
        JMenu fileMenu = new JMenu("Help");
        fileMenu.setMnemonic('H');

        fileMenu.add(createJMenuItem("Help Content", "help_content", null, "help",null));
        fileMenu.add(createJMenuItem("About", "help_about", null, null,null));
        return fileMenu;
    }

    private JMenuItem createJMenuItem(final String text, final String action, String hotKey, String icon, String[] enableDisableLookupFlagNames) {
        JMenuItem menuItem = new JMenuItem(text);
        if (hotKey != null) {
            menuItem.setAccelerator(KeyStroke.getKeyStroke(hotKey));
        }

        menuItem.addActionListener((ActionEvent e) -> {
            System.out.println("Action [" + action + "]");
            actionManager.fire(action);
        });

        if (icon != null) {
            menuItem.setIcon(ImageManager.get().getIcon(icon));
        }
        
        menuItem.putClientProperty("enableDisableFlagLookupNames", enableDisableLookupFlagNames);

        return menuItem;
    }

    /**
    Create event that handles enabling disabling menu items that requires project
     */
    private void processEnableDisableRequireProjectMenu(JMenu menu) {
        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                for (Component childComponent : menu.getMenuComponents()) {
                    if (childComponent instanceof JMenuItem) {
                        JMenuItem menuItem = (JMenuItem) childComponent;
                        String[] lookupFlags = (String[]) menuItem.getClientProperty("enableDisableFlagLookupNames");
                        if (lookupFlags == null) {
                            menuItem.setEnabled(true);
                        } else {
                            boolean enabled = true;
                            for (String lookupFlag : lookupFlags) {
                                Boolean value = (Boolean) Lookup.get().get(lookupFlag, Boolean.FALSE);
                                if (Objects.equals(value, Boolean.FALSE)) {
                                    enabled = false;
                                    break;
                                }
                            }
                            menuItem.setEnabled(enabled);
                        }
                    }
                }
            }

            @Override
            public void menuDeselected(MenuEvent e) {
            }

            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
    }
}
