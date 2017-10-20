package org.visualeagle.project.vnodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Dmitry
 */
public class LocalFileVNode extends AbstractVNode {

    private File fileObject;

    public LocalFileVNode(File fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(fileObject);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
}
