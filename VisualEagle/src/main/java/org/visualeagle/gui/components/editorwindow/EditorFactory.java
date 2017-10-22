package org.visualeagle.gui.components.editorwindow;

import java.io.IOException;
import org.visualeagle.gui.components.editorwindow.langeditors.CPPEditor;
import org.visualeagle.project.vnodes.AbstractVNode;

/**
 * @author Dmitry
 */
public class EditorFactory {
    
    public AbstractEditor createEditor(AbstractVNode file,TabComponent tabComponent) throws IOException{
        String extension=file.getExtension();
        if(extension.equalsIgnoreCase("cpp")||extension.equalsIgnoreCase("h")||extension.equalsIgnoreCase("hpp")||extension.equalsIgnoreCase("c")){
            return new CPPEditor(file,tabComponent);
        }
        return null;
    }
}
