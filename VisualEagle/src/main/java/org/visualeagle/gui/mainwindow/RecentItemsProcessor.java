package org.visualeagle.gui.mainwindow;

import java.awt.event.ActionEvent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.visualeagle.gui.mainwindow.ActionManager;
import org.visualeagle.utils.Lookup;
import org.visualeagle.utils.Settings;

/**
 * @author sad
 */
public class RecentItemsProcessor {

    private JMenu recentItemMenu;

    public RecentItemsProcessor(JMenu recentItemMenu) {
        this.recentItemMenu = recentItemMenu;
        createRecentProjectsSubmenuItem();
    }

    private void createRecentProjectsSubmenuItem() {
        String recentProjectString = Settings.getStringProperty("recentProjectsString", null);
        if (recentProjectString == null) {
            recentItemMenu.setEnabled(false);
        } else {
            recentItemMenu.removeAll();
            String[] values = StringUtils.split(recentProjectString, '|');
            for (String path : values) {
                String menuCaption = StringUtils.abbreviateMiddle(path, "...", 100);
                JMenuItem recentItem = new JMenuItem(menuCaption);
                recentItem.addActionListener((ActionEvent e) -> {
                    System.out.println("Action [" + path + "]");
                    Lookup.get().get(ActionManager.class).fire("open_recent_project", path);
                });

                recentItemMenu.add(recentItem);
                recentItemMenu.setEnabled(true);
            }
        }
    }

    public void addToRecentList(String path) {
        String recentProjectString = Settings.getStringProperty("recentProjectsString", null);
        String[] recentProjects = StringUtils.split(recentProjectString, '|');
        if (ArrayUtils.indexOf(recentProjects, path) >= 0) {
            
            int index = ArrayUtils.indexOf(recentProjects, path);
            String elementValue= recentProjects[index];
            recentProjects=ArrayUtils.remove(recentProjects, index);
            recentProjects=ArrayUtils.add(recentProjects, 0, elementValue);
        } else {
            recentProjects = ArrayUtils.add(recentProjects, 0, path);
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String project : recentProjects) {
            if (!first) {
                sb.append("|");
            }
            sb.append(project);
            first = false;
        }

        Settings.putStringProperty("recentProjectsString", sb.toString());
        Settings.flush();
        createRecentProjectsSubmenuItem();
    }

    public void removeFromRecentList(String path) {
        String recentProjectString = Settings.getStringProperty("recentProjectsString", null);
        String[] recentProjects = StringUtils.split(recentProjectString, '|');
        int index = ArrayUtils.indexOf(recentProjects, path);
        if (index == -1) {
            return;
        }

        recentProjects = ArrayUtils.remove(recentProjects, index);
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String project : recentProjects) {
            if (!first) {
                sb.append("|");
            }
            sb.append(project);
            first = false;
        }

        Settings.putStringProperty("recentProjectsString", sb.toString());
        Settings.flush();
        createRecentProjectsSubmenuItem();
    }
}
