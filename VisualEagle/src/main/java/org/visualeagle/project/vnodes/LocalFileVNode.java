package org.visualeagle.project.vnodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.io.FileUtils;

/**
 * @author Dmitry
 */
public class LocalFileVNode extends AbstractVNode {

    private File fileObject;

    public LocalFileVNode(File fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public String getName() {
        return fileObject.getName();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(fileObject);
    }

    @Override
    public void saveStringToFile(String text) throws IOException {
        FileUtils.write(fileObject, text, StandardCharsets.UTF_8);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.fileObject);
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
        final LocalFileVNode other = (LocalFileVNode) obj;
        if (!Objects.equals(this.fileObject, other.fileObject)) {
            return false;
        }
        return true;
    }

}
