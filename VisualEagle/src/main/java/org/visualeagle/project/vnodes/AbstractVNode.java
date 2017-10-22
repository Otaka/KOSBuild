package org.visualeagle.project.vnodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.apache.commons.io.FilenameUtils;
import org.visualeagle.utils.ImageManager;

/**
 * @author Dmitry
 */
public class AbstractVNode {

    private boolean readonly = false;
    private AbstractVNode parent;
    private String name;
    protected Icon defaultIcon = ImageManager.get().getIcon("file");
    private Icon icon;

    public String getExtension() {
        return FilenameUtils.getExtension(getName()).toLowerCase();
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public Icon getIcon() {
        if (icon != null) {
            return icon;
        }

        return defaultIcon;
    }

    public InputStream getInputStream() throws IOException {
        throw new IllegalStateException("Cannot get InputStream in [" + getClass().getName() + "] class");
    }

    public AbstractVNode getParent() {
        return parent;
    }

    public AbstractVNode setParent(AbstractVNode parent) {
        this.parent = parent;
        return this;
    }

    public final AbstractVNode setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public boolean isLeaf() {
        return true;
    }

    public List<AbstractVNode> getChildren() {
        return new ArrayList<>();
    }
    
    public void saveStringToFile(String text)throws IOException{
        throw new IllegalStateException("saveStringToFile is not implemented for ["+getClass().getName()+"]");
    }

    @Override
    public String toString() {
        return getName();
    }

}
