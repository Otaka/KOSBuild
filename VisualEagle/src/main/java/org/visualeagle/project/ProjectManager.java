package org.visualeagle.project;

import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.ArrayUtils;
import org.visualeagle.gui.editorwindow.EditorWindow;
import org.visualeagle.gui.logwindow.GuiLogPrinter;
import org.visualeagle.gui.mainwindow.ActionManager;
import org.visualeagle.project.projectloaders.ProjectStructure;
import org.visualeagle.utils.ExternalProcessRunner;
import org.visualeagle.utils.Lookup;

/**
 * @author sad
 */
public class ProjectManager {

    private ProjectStructure projectStructure;

    public ProjectManager() {
        ActionManager actionManager = Lookup.get().get(ActionManager.class);
        actionManager.registerAction("clean", this::cleanCurrentProject);
        actionManager.registerAction("install", this::installCurrentProject);
        actionManager.registerAction("clean_install", this::cleanInstallCurrentProject);
    }

    public ProjectStructure getCurrentProject() {
        return projectStructure;
    }

    public void closeCurrentProject() {
        projectStructure = null;
    }

    public void setCurrentProject(ProjectStructure projectStructure) {
        this.projectStructure = projectStructure;
    }

    private boolean runKosBuild(String caption, String... commands) throws IOException {
        EditorWindow editor = Lookup.get().get(EditorWindow.class);
        editor.saveAll();
        GuiLogPrinter consolePrinter = Lookup.get().get(GuiLogPrinter.class);
        consolePrinter.clear();
        File directory = new File(projectStructure.getBuildFile()).getParentFile();
        consolePrinter.println(String.format(caption, directory.getAbsolutePath()));
        ExternalProcessRunner externalRun = new ExternalProcessRunner();
        String[] args = new String[]{"java", "-jar", "KOSBuild-0.1.jar", "-f", directory.getAbsolutePath()};
        args = ArrayUtils.addAll(args, commands);
        try {
            boolean result = externalRun.run(args, new File("./kosbuild"), (String line, boolean first) -> {
                SwingUtilities.invokeLater(() -> {
                    consolePrinter.println(line);
                });
            }, true);
            SwingUtilities.invokeLater(() -> {
                if (result) {
                    consolePrinter.println("SUCCESS");
                } else {
                    consolePrinter.println("FAILURE");
                }
            });

            return result;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void cleanCurrentProject() throws IOException {
        runKosBuild("Clean project [%s]", "clean");
    }

    public void installCurrentProject() throws IOException {
        runKosBuild("Install project [%s]", "install");
    }

    public void cleanInstallCurrentProject() throws IOException {
        runKosBuild("Do clean install on project [%s]", "clean", "install");
    }

}
