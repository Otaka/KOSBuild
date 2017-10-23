package org.visualeagle.gui.small.directorychooser;

import java.io.File;
import org.visualeagle.utils.ImageManager;

/**
 * @author Dmitry
 */
public class ProjectDirectoryChooser extends DirectoryChooser {

    public ProjectDirectoryChooser() {
        setOnFileFoundEvent(createOnFileFoundEvent());
        setCheckAllowToSelectCallback(new CheckAllowToSelectCallback() {
            @Override
            public boolean onSelect(TreeElement treeElement) {
                File folder=treeElement.getFile();
                return new File(folder,"kosbuild.json").exists();
            }
        });
    }

    private OnFileFound createOnFileFoundEvent() {
        return new OnFileFound() {
            @Override
            public TreeElement onFileFound(File file) {
                if (file.isDirectory()) {
                    if (new File(file, "kosbuild.json").exists()) {
                        return new TreeElement(ImageManager.get().getIcon("eagle_small"), file.getName(), file, false);
                    }
                }

                return new TreeElement(null, file.getName(), file, file.isDirectory());
            }
        };
    }
}
