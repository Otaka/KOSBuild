package org.visualeagle.gui.mainwindow;

import java.awt.BorderLayout;
import org.visualeagle.gui.projectnavigation.ProjectNavigationWindow;
import org.visualeagle.gui.editorwindow.EditorWindow;
import org.visualeagle.gui.componenteditor.ComponentEditorWindow;
import org.visualeagle.utils.ChunkedTextCollector;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.visualeagle.utils.GuiUtils;
import org.visualeagle.gui.small.directorychooser.ProjectDirectoryChooser;
import org.visualeagle.gui.logwindow.GuiLogPrinter;
import org.visualeagle.gui.logwindow.LogWindow;
import org.visualeagle.gui.remoteconnection.RemoteConnectionManager;
import org.visualeagle.gui.remotewindow.RemoteCommanderWindow;
import org.visualeagle.project.ProjectManager;
import org.visualeagle.project.projectloaders.KosBuildGccProjectLoader;
import org.visualeagle.project.projectloaders.ProjectLoader;
import org.visualeagle.project.projectloaders.ProjectStructure;
import org.visualeagle.utils.ConfigNames;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.LongRunningTask;
import org.visualeagle.utils.LongRunningTaskWithDialog;
import org.visualeagle.utils.Lookup;
import org.visualeagle.utils.Utils;
import org.visualeagle.utils.WindowLocationService;

/**
 * @author Dmitry
 */
public class MainWindow extends JFrame {

    private WindowLocationService windowLocationService;
    private ChunkedTextCollector title;
    private JDesktopPane desktop;
    private ProjectNavigationWindow projectNavigationWindow;
    private ComponentEditorWindow componentEditorWindow;
    private EditorWindow editorWindow;
    private LogWindow logWindow;
    private MainMenu mainMenu;
    private StatusPanel statusPanel;

    public MainWindow() throws HeadlessException {
        init();
    }

    private void init() {
        title = new ChunkedTextCollector(event -> {
            setTitle(event);
        });

        setIconImage(ImageManager.get().getImage("eagle"));

        title.setTitle("main", "Visual Eagle");
        title.setTitle("modified", "");
        title.setTitle("filename", "");
        Lookup.get().put("title", title);
        Lookup.get().put(ActionManager.class, new ActionManager());

        windowLocationService = new WindowLocationService();
        getRootPane().putClientProperty("name", "mainWindow");
        windowLocationService.setInitialState(this, "0%", "0%", "100%", "100%", true);
        windowLocationService.register(this);

        desktop = new JDesktopPane();
        desktop.setBackground(new Color(240, 240, 240));
        desktop.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
        getContentPane().add(desktop);

        windowLocationService.setRootComponent(desktop);
        SwingUtilities.invokeLater(() -> {
            createProjectNavigationWindow();
            createComponentEditorWindow();
            createEditorWindow();
            createLogWindow();
        });

        //runTestProgressDialogWindow();

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Settings.clear();
                System.exit(0);
            }
        });

        mainMenu = new MainMenu();
        setJMenuBar(mainMenu.constructMainMenu());

        RemoteConnectionManager remoteConnectionManager = new RemoteConnectionManager();
        Lookup.get().put(RemoteConnectionManager.class, remoteConnectionManager);
        statusPanel = new StatusPanel();
        getContentPane().add(statusPanel, BorderLayout.SOUTH);
        try {
            remoteConnectionManager.init();//init should be after status panel, because status panel sets own callbacks
        } catch (Exception ex) {
            ex.printStackTrace();
            Utils.showErrorMessage("Error while start connectionManager \n" + ExceptionUtils.getRootCauseMessage(ex));
        }
        MainToolBar mainToolbar = new MainToolBar();
        getContentPane().add(mainToolbar, BorderLayout.NORTH);
        Lookup.get().put(MainWindow.class, this);
        Lookup.get().put(ProjectManager.class, new ProjectManager());

        ActionManager actionManager = Lookup.get().get(ActionManager.class);
        actionManager.registerAction("open_project", this::openProject);
        actionManager.registerAction("open_recent_project", this::openRecentProject);
        actionManager.registerAction("close_project", this::closeOpenedProject);
        actionManager.registerAction("remote_commander", this::remoteCommander);
    }

    public JDesktopPane getDesktop() {
        return desktop;
    }

    public WindowLocationService getWindowLocationService() {
        return windowLocationService;
    }

    private void remoteCommander() {
        RemoteConnectionManager connectionManager = Lookup.get().get(RemoteConnectionManager.class);
        if (!connectionManager.isConnectionEstablished()) {
            Utils.showErrorMessage("Please make connection to remote machine first");
            return;
        }

        RemoteCommanderWindow remoteCommanderWindow = new RemoteCommanderWindow();
        remoteCommanderWindow.setVisible(true);

    }

    private void openProject() {
        File projectFolder = new ProjectDirectoryChooser().chooseFolder(Lookup.get().get(MainWindow.class));
        if (projectFolder != null) {
            System.out.println("Open project = " + projectFolder.getAbsolutePath());
        } else {
            System.out.println("No project directory was selected");
            return;
        }

        String projectFolderPath = projectFolder.getAbsolutePath();
        Lookup.get().get(RecentItemsProcessor.class).addToRecentList(projectFolderPath);
        loadProject(projectFolder);
    }

    private void openRecentProject(Object actionEvent) {
        RecentItemsProcessor recentItemsProcessor = Lookup.get().get(RecentItemsProcessor.class);
        String projectFolderString = (String) actionEvent;
        File buildFile = new File(projectFolderString, "kosbuild.json");
        if (!buildFile.exists()) {
            GuiUtils.error("Cannot find project at [" + buildFile.getAbsolutePath() + "]");
            recentItemsProcessor.removeFromRecentList(projectFolderString);
            return;
        }
        recentItemsProcessor.addToRecentList(projectFolderString);
        System.out.println("Opened project [" + projectFolderString + "]");
        loadProject(new File(projectFolderString));
    }

    private void createProjectNavigationWindow() {
        projectNavigationWindow = new ProjectNavigationWindow("Projects Navigation", true, false, false, false);
        projectNavigationWindow.getRootPane().putClientProperty("name", "ProjectNavigations");
        projectNavigationWindow.setFrameIcon(ImageManager.get().getIcon("eagle"));
        windowLocationService.setInitialState(projectNavigationWindow, "0%", "0%", "20%", "50%", false);
        windowLocationService.register(projectNavigationWindow);
        projectNavigationWindow.setVisible(true);
        Lookup.get().put(ProjectNavigationWindow.class, projectNavigationWindow);
        desktop.add(projectNavigationWindow);
    }

    private void createComponentEditorWindow() {
        componentEditorWindow = new ComponentEditorWindow("Component Editor", true, false, false, false);
        componentEditorWindow.setFrameIcon(ImageManager.get().getIcon("eagle"));
        componentEditorWindow.getRootPane().putClientProperty("name", "ComponentEditor");
        windowLocationService.setInitialState(componentEditorWindow, "0%", "50%", "20%", "47%", false);
        windowLocationService.register(componentEditorWindow);
        componentEditorWindow.setVisible(true);
        Lookup.get().put(ComponentEditorWindow.class, componentEditorWindow);
        desktop.add(componentEditorWindow);
    }

    private void createEditorWindow() {
        editorWindow = new EditorWindow("Editor", true, false, false, false);
        editorWindow.setFrameIcon(ImageManager.get().getIcon("eagle"));
        editorWindow.getRootPane().putClientProperty("name", "Editor");
        windowLocationService.setInitialState(editorWindow, "20%", "0%", "81%", "75%", false);
        windowLocationService.register(editorWindow);
        editorWindow.setVisible(true);
        Lookup.get().put(EditorWindow.class, editorWindow);
        desktop.add(editorWindow);
    }

    private void createLogWindow() {
        logWindow = new LogWindow("Output", true, false, false, false);
        logWindow.setFrameIcon(ImageManager.get().getIcon("eagle"));
        logWindow.getRootPane().putClientProperty("name", "logWindow");
        windowLocationService.setInitialState(logWindow, "20%", "75%", "81%", "22%", false);
        windowLocationService.register(logWindow);
        logWindow.setVisible(true);
        Lookup.get().put(LogWindow.class, logWindow);
        desktop.add(logWindow);
        Lookup.get().put(GuiLogPrinter.class, logWindow);
    }

    public void loadProject(File projectFolder) {
        File kosBuildFile = new File(projectFolder, "kosbuild.json");
        ProjectLoader projectLoader;
        if (kosBuildFile.exists()) {
            projectLoader = new KosBuildGccProjectLoader();
        } else {
            throw new IllegalArgumentException("Project in folder [" + projectFolder + "] has unknown type");
        }
        try {
            ProjectStructure projectStructure = projectLoader.loadProject(projectFolder);
            if (closeOpenedProject()) {
                projectNavigationWindow.loadProject(projectStructure);
            }

            Lookup.get().get(ProjectManager.class).setCurrentProject(projectStructure);
            Lookup.get().put(ConfigNames.PROJECT_IS_LOADED, Boolean.TRUE);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public boolean closeOpenedProject() throws IOException {
        if (!editorWindow.closeAllEditorWindows()) {
            return false;
        }

        projectNavigationWindow.closeCurrentProject();
        Lookup.get().get(ProjectManager.class).closeCurrentProject();
        Lookup.get().put(ConfigNames.PROJECT_IS_LOADED, Boolean.FALSE);
        return true;
    }

    private void runTestProgressDialogWindow() {
        SwingUtilities.invokeLater(() -> {
            LongRunningTaskWithDialog lg = new LongRunningTaskWithDialog(this, new LongRunningTask() {
                @Override
                public Object run(LongRunningTaskWithDialog dialog) {
                    for (int i = 0; i <= 10; i++) {
                        if (dialog.isCanceled()) {
                            return false;
                        }

                        long max = 10_000_000_000l;
                        long delta = max / 1000;
  
                        dialog.setMaxProgressValue(max);
                        for (int j = 0; j < 1000; j++) {

                            dialog.setInformationMessage1("some text " + i + "_" + j);
                            dialog.setInformationMessage2("" + i + "_" + j);
                            dialog.addCurrentProgressValue(delta);
                            dialog.setTextInProgressBar("" + i + "%" + j);

                            try {
                                Thread.sleep(3);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    return "343";
                }

                @Override
                public void onDone(LongRunningTaskWithDialog dialog, Object result) {
                    System.out.println("Done " + result);
                }
            });

            lg.setMaxProgressValue(10);
            lg.setDialogTitle("Test progress bar dialog");
            lg.start();
        });
    }
}
