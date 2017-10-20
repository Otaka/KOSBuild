package org.visualeagle.project.vnodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.visualeagle.utils.ImageManager;

/**
 * @author Dmitry
 */
public class AbstractVNode {

    private AbstractVNode parent;
    private String name;
    private static final Icon defaultIcon = ImageManager.get().getIcon("file");

    public InputStream getInputStream() throws IOException{
        throw new IllegalStateException("Cannot get InputStream in [" + getClass().getName() + "] class");
    }

    public AbstractVNode getParent() {
        return parent;
    }

    public AbstractVNode setParent(AbstractVNode parent) {
        this.parent = parent;
        return this;
    }

    public AbstractVNode setName(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public Icon getIcon() {
        return defaultIcon;
    }

    public boolean isLeaf() {
        return true;
    }

    public List<AbstractVNode> getChildren() {
        return new ArrayList<>();
    }
}
