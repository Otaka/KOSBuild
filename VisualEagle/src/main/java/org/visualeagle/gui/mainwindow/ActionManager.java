package org.visualeagle.gui.mainwindow;

import java.util.HashMap;
import java.util.Map;
import org.visualeagle.utils.RunnableWithParams;
import org.visualeagle.utils.RunnableWithoutParams;

/**
 * @author Dmitry
 */
public class ActionManager {

    private Map<String, Object> actions = new HashMap<>();

    public ActionManager registerAction(String actionName, RunnableWithParams runnable) {
        actions.put(actionName, runnable);
        return this;
    }

    public ActionManager registerAction(String actionName, RunnableWithoutParams runnable) {
        actions.put(actionName, runnable);
        return this;
    }

    public void fire(String actionName) {
        Object listener = actions.get(actionName);
        if (listener == null) {
            System.out.println("No actions registered for action [" + actionName + "]");
            return;
        }

        try {
            if (listener instanceof RunnableWithParams) {
                ((RunnableWithParams) listener).run(null);
            } else {
                ((RunnableWithoutParams) listener).run();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void fire(String actionName, Object param) {
        Object listener = actions.get(actionName);
        if (listener == null) {
            System.out.println("No actions registered for action [" + actionName + "]");
            return;
        }

        if (listener instanceof RunnableWithParams) {
            try {
                ((RunnableWithParams) listener).run(param);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("You try to pass argument [" + param + "] to listener of the action [" + actionName + "] but it cannot accept arguments");
        }

    }
}
