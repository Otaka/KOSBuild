package org.visualeagle.gui;

import org.visualeagle.gui.components.ProjectNavigationWindow;
import org.visualeagle.gui.components.MainMenu;
import org.visualeagle.gui.components.EditorWindow;
import org.visualeagle.gui.components.ComponentEditorWindow;
import org.visualeagle.utils.ChunkedTextCollector;
import java.awt.Color;
import java.awt.HeadlessException;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.visualeagle.utils.ImageManager;
import org.visualeagle.utils.Lookup;
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
    private MainMenu mainMenu;

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

        windowLocationService = new WindowLocationService();
        getRootPane().putClientProperty("name", "mainWindow");
        windowLocationService.setInitialState(this, "0%", "0%", "100%", "100%", true);
        windowLocationService.register(this);

        desktop = new JDesktopPane();
        desktop.setBackground(new Color(240, 240, 240));
        desktop.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
        setContentPane(desktop);
        windowLocationService.setRootComponent(desktop);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createProjectNaviagionWindow();
                createComponentEditorWindow();
                createEditorWindow();
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        mainMenu = new MainMenu();
        setJMenuBar(mainMenu.constructMainMenu());
        Lookup.get().put(MainWindow.class,this);
    }

    private void createProjectNaviagionWindow() {
        projectNavigationWindow = new ProjectNavigationWindow("Projects Navigation", true, false, false, false);
        projectNavigationWindow.getRootPane().putClientProperty("name", "ProjectNavigations");
        windowLocationService.setInitialState(projectNavigationWindow, "0%", "0%", "20%", "50%", false);
        windowLocationService.register(projectNavigationWindow);
        projectNavigationWindow.setVisible(true);
        Lookup.get().put(ProjectNavigationWindow.class, projectNavigationWindow);
        desktop.add(projectNavigationWindow);
    }

    private void createComponentEditorWindow() {
        componentEditorWindow = new ComponentEditorWindow("Component Editor", true, false, false, false);
        componentEditorWindow.getRootPane().putClientProperty("name", "ComponentEditor");
        windowLocationService.setInitialState(componentEditorWindow, "0%", "50%", "20%", "50%", false);
        windowLocationService.register(componentEditorWindow);
        componentEditorWindow.setVisible(true);
        Lookup.get().put(ComponentEditorWindow.class, componentEditorWindow);
        desktop.add(componentEditorWindow);
    }

    private void createEditorWindow() {
        editorWindow = new EditorWindow("Editor", true, false, false, false);
        editorWindow.getRootPane().putClientProperty("name", "Editor");
        windowLocationService.setInitialState(editorWindow, "20%", "0%", "80%", "100%", false);
        windowLocationService.register(editorWindow);
        editorWindow.setVisible(true);
        Lookup.get().put(EditorWindow.class, editorWindow);
        desktop.add(editorWindow);
    }
}
