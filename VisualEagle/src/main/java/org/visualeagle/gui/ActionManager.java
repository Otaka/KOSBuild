package org.visualeagle.gui;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry
 */
public class ActionManager {

    private Map<String, ActionListener> actions = new HashMap<>();

    public ActionManager registerAction(String actionName, ActionListener listener) {
        actions.put(actionName, listener);
        return this;
    }

    public void fire(String actionName) {
        ActionListener listener = actions.get(actionName);
        if (listener == null) {
            System.err.println("No actions registered for action [" + actionName + "]");
            return;
        }

        listener.actionPerformed(null);
    }
}
