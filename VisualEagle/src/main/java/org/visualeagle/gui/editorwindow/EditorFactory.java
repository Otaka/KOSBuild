package org.visualeagle.gui.editorwindow;

import java.io.IOException;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.visualeagle.gui.editorwindow.langeditors.RSyntaxEditor;
import org.visualeagle.project.vnodes.AbstractVNode;

/**
 * @author Dmitry
 */
public class EditorFactory {
    
    public AbstractEditor createEditor(AbstractVNode file,TabComponent tabComponent) throws IOException{
        String extension=file.getExtension();
        if(extension.equalsIgnoreCase("cpp")||extension.equalsIgnoreCase("h")||extension.equalsIgnoreCase("hpp")||extension.equalsIgnoreCase("c")){
            return new RSyntaxEditor(file,tabComponent, SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
        }
        if(extension.equalsIgnoreCase("json")){
            return new RSyntaxEditor(file,tabComponent, SyntaxConstants.SYNTAX_STYLE_JSON);
        }
        if(extension.equalsIgnoreCase("xml")){
            return new RSyntaxEditor(file,tabComponent, SyntaxConstants.SYNTAX_STYLE_XML);
        }
        if(extension.equalsIgnoreCase("inc")){
            return new RSyntaxEditor(file,tabComponent, SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86);
        }
        if(extension.equalsIgnoreCase("py")){
            return new RSyntaxEditor(file,tabComponent, SyntaxConstants.SYNTAX_STYLE_PYTHON);
        }
        if(extension.equalsIgnoreCase("perl")){
            return new RSyntaxEditor(file,tabComponent, SyntaxConstants.SYNTAX_STYLE_PERL);
        }
        if(extension.equalsIgnoreCase("bat")){
            return new RSyntaxEditor(file,tabComponent, SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH);
        }
        if(extension.equalsIgnoreCase("sh")){
            return new RSyntaxEditor(file,tabComponent, SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
        }
        if(extension.equalsIgnoreCase("yaml")){
            return new RSyntaxEditor(file,tabComponent, SyntaxConstants.SYNTAX_STYLE_YAML);
        }
        if(extension.equalsIgnoreCase("properties")){
            return new RSyntaxEditor(file,tabComponent, SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
        }
        
        return new RSyntaxEditor(file,tabComponent, SyntaxConstants.SYNTAX_STYLE_NONE);
    }
}
