package org.visualeagle.project.vnodes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry
 */
public class LocalFolderVNode extends AbstractVNode {

    private File fileObject;

    public LocalFolderVNode(File fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public List<AbstractVNode> getChildren() {
        List<AbstractVNode> result = new ArrayList<>();
        File[] children = fileObject.listFiles();
        for (File child : children) {
            if (child.isDirectory()) {
                result.add(new LocalFolderVNode(child).setParent(this));
            } else {
                result.add(new LocalFileVNode(child).setParent(this));
            }
        }

        return result;
    }

}
