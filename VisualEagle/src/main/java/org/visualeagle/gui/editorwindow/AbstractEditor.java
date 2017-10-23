package org.visualeagle.gui.editorwindow;

import java.io.IOException;
import javax.swing.JPanel;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.visualeagle.project.vnodes.AbstractVNode;
import org.visualeagle.utils.Lookup;

/**
 * @author Dmitry
 */
public abstract class AbstractEditor extends JPanel {

    private AbstractVNode file;
    private boolean modified = false;
    private TabComponent tabComponent;

    public AbstractEditor(AbstractVNode file, TabComponent tabComponent) {
        this.file = file;
        this.tabComponent = tabComponent;
    }

    public TabComponent getTabComponent() {
        return tabComponent;
    }

    public void setModified() {
        tabComponent.setModified();
        this.modified = true;
    }

    public void clearModified() {
        tabComponent.clearModified();
        this.modified = false;
    }

    public boolean isModified() {
        return modified;
    }

    public AbstractVNode getFile() {
        return file;
    }

    private String preprocessText(String value) {
        return value.replace("\r", "");
    }

    public String readFileFully() throws IOException {
        String text = IOUtils.toString(file.getInputStream(), Charsets.UTF_8);
        text = preprocessText(text);
        return text;
    }

    public void setFocus() {
        //to be overriden
    }

    public abstract String getDataToSave();

    public void save() throws IOException {
        if (isModified()) {
            file.saveStringToFile(getDataToSave());
            clearModified();
        }
    }

    public void destroy() {
        Lookup.get().put("cursorPosition", "0:0");
    }

    public void undo() {
    }
    public void redo() {
    }
    public void copy() {
    }
    public void cut() {
    }
    public void paste() {
    }
    public void selectAll() {
    }

}
