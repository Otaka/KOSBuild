package org.visualeagle.gui.small.directorychooser;

import java.io.File;
import java.util.Objects;
import javax.swing.Icon;

/**
 * @author Dmitry
 */
public class TreeElement {

    private Icon icon;
    private String text;
    private File file;
    boolean shouldHaveChildren;

    public TreeElement(Icon icon, String text, File file, boolean shouldHaveChildren) {
        this.icon = icon;
        this.text = text;
        this.file = file;
        this.shouldHaveChildren = shouldHaveChildren;
    }

    public boolean isShouldHaveChildren() {
        return shouldHaveChildren;
    }

    public Icon getIcon() {
        return icon;
    }

    public File getFile() {
        return file;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.file);
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
        final TreeElement other = (TreeElement) obj;
        if (!Objects.equals(this.file, other.file)) {
            return false;
        }
        return true;
    }

}
