package org.visualeagle.project.vnodes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.visualeagle.utils.ImageManager;

/**
 * @author Dmitry
 */
public class LocalFolderVNode extends AbstractVNode {

    private File fileObject;

    public LocalFolderVNode(String name, File fileObject) {
        setName(name);
        defaultIcon = ImageManager.get().getIcon("folder");
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
            AbstractVNode childVNode;
            if (child.isDirectory()) {
                childVNode = new LocalFolderVNode(child.getName(), child).setParent(this);
            } else {
                childVNode = new LocalFileVNode(child).setParent(this);
            }

            childVNode.setReadonly(isReadonly());
            result.add(childVNode);
        }

        return result;
    }

    public File getFileObject() {
        return fileObject;
    }

    public void setFileObject(File fileObject) {
        this.fileObject = fileObject;
    }

    
}
