package org.visualeagle.project.vnodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.visualeagle.utils.ImageManager;

/**
 * @author Dmitry
 */
public class VirtualFolderVNode extends AbstractVNode {

    private List<AbstractVNode> children;
    private Object userObject;

    public VirtualFolderVNode(String name) {
        defaultIcon = ImageManager.get().getIcon("folder");
        setName(name);
    }

    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    public Object getUserObject() {
        return userObject;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    public void addChild(AbstractVNode node) {
        node.setParent(this);
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(node);
    }

    @Override
    public List<AbstractVNode> getChildren() {
        return children;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.userObject);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VirtualFolderVNode other = (VirtualFolderVNode) obj;
        if (!Objects.equals(this.userObject, other.userObject)) {
            return false;
        }
        return true;
    }
    
    

}
