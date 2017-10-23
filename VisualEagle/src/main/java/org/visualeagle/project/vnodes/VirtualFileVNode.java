package org.visualeagle.project.vnodes;

import java.util.Objects;

/**
 * @author Dmitry
 */
public class VirtualFileVNode extends AbstractVNode {

    private Object userObject;

    public Object getUserObject() {
        return userObject;
    }

    public void setUserObject(Object userObject) {
        this.userObject = userObject;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.userObject);
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
        final VirtualFileVNode other = (VirtualFileVNode) obj;
        if (!Objects.equals(this.userObject, other.userObject)) {
            return false;
        }
        return true;
    }

    
}
