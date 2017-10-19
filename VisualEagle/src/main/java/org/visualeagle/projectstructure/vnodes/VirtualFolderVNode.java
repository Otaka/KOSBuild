package org.visualeagle.projectstructure.vnodes;

import java.util.List;

/**
 * @author Dmitry
 */
public class VirtualFolderVNode extends AbstractVNode {

    private List<AbstractVNode> children;

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public List<AbstractVNode> getChildren() {
        return children;
    }

}
