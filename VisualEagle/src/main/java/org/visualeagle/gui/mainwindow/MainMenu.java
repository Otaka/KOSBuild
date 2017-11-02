package org.visualeagle.gui.mainwindow;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.visualeagle.project.ProjectManager;
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
        MenuList menuList = new MenuList();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        fileMenu.add(createJMenuItem("New Project...", "new_project", "ctrl shift N", "newProject"));
        fileMenu.add(createJMenuItem("Open Project...", "open_project", "ctrl shift O", "openProject"));
        fileMenu.add(menuList.put(createJMenuItem("Close Project", "close_project", null, null)));
        JMenu recentItemMenu = new JMenu("Recent Projects...");
        recentItemMenu.setEnabled(true);
        RecentItemsProcessor recentItemsProcessor = new RecentItemsProcessor(recentItemMenu);
        Lookup.get().put(RecentItemsProcessor.class, recentItemsProcessor);
        fileMenu.add(recentItemMenu);
        fileMenu.addSeparator();
        fileMenu.add(menuList.put(createJMenuItem("Save", "save_file", "ctrl S", "save")));
        fileMenu.add(menuList.put(createJMenuItem("Save All", "save_all", null, "save_all")));
        fileMenu.addSeparator();

        fileMenu.add(createJMenuItem("Exit", "exit", null, null));
        processEnableDisableRequireMenu(fileMenu, menuList);
        return fileMenu;
    }

    private JMenu createEditMenu() {
        MenuList menuList = new MenuList();
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        editMenu.add(menuList.put(createJMenuItem("Undo", "undo", "ctrl Z", "undo")));
        editMenu.add(menuList.put(createJMenuItem("Redo", "redo", "ctrl shift Z", "redo")));
        editMenu.addSeparator();
        editMenu.add(menuList.put(createJMenuItem("Cut", "cut", "ctrl X", "cut")));
        editMenu.add(menuList.put(createJMenuItem("Copy", "copy", "ctrl C", "copy")));
        editMenu.add(menuList.put(createJMenuItem("Paste", "paste", "ctrl V", "paste")));
        editMenu.addSeparator();
        editMenu.add(menuList.put(createJMenuItem("Select All", "select_all", "ctrl A", null)));
        editMenu.addSeparator();
        editMenu.add(menuList.put(createJMenuItem("Find...", "find", "ctrl F", null)));
        editMenu.add(menuList.put(createJMenuItem("Replace...", "replace", "ctrl H", null)));
        editMenu.addSeparator();
        editMenu.add(createJMenuItem("Preferences...", "preferences", null, null));
        processEnableDisableRequireMenu(editMenu, menuList);
        return editMenu;
    }

    private JMenu createProjectMenu() {
        MenuList menuList = new MenuList();
        JMenu projectMenu = new JMenu("Project");
        projectMenu.setMnemonic('P');
        projectMenu.add(menuList.put(createJMenuItem("Clean", "clean", null, null)));
        projectMenu.add(menuList.put(createJMenuItem("Install", "install", null, null)));
        projectMenu.add(menuList.put(createJMenuItem("Clean Install", "clean_install", "shift F11","build")));
        projectMenu.addSeparator();
        projectMenu.add(menuList.put(createJMenuItem("Run", "run_app", "F9", "run")));
        projectMenu.add(menuList.put(createJMenuItem("Debug", "debug_app", "ctrl F9", "debug")));
        projectMenu.addSeparator();
        projectMenu.add(menuList.put(createJMenuItem("Remote commander", "remote_commander", null, "debug")));
        processEnableDisableRequireMenu(projectMenu, menuList);
        return projectMenu;
    }

    private JMenu createHelpMenu() {
        JMenu fileMenu = new JMenu("Help");
        fileMenu.setMnemonic('H');

        fileMenu.add(createJMenuItem("Help Content", "help_content", null, "help"));
        fileMenu.add(createJMenuItem("About", "help_about", null, null));
        return fileMenu;
    }

    private JMenuItem createJMenuItem(final String text, final String action, String hotKey, String icon) {
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

        return menuItem;
    }

    /**
    Create event that handles enabling disabling menu items that requires project
     */
    private void processEnableDisableRequireMenu(JMenu menu, MenuList subMenuList) {
        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                boolean hasProject = Lookup.get().get(ProjectManager.class).getCurrentProject() != null;
                for(JMenuItem menuItem:subMenuList){
                    menuItem.setEnabled(hasProject);
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

    private static class MenuList extends ArrayList<JMenuItem> {

        public JMenuItem put(JMenuItem item) {
            add(item);
            return item;
        }
    }
}
