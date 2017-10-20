package org.visualeagle.project.vnodes;

import javax.swing.Icon;

/**
 * @author Dmitry
 */
public class VirtualFileVNode extends AbstractVNode {

    private Icon icon;

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    @Override
    public Icon getIcon() {
        if (icon != null) {
            return icon;
        }

        return super.getIcon();
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
}
