package org.visualeagle.utils;

import javax.swing.SwingUtilities;

/**
 * @author sad
 */
public abstract class LongRunningTask {

    private boolean cancel = false;

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public boolean isCancel() {
        return cancel;
    }

    public abstract Object run(LongRunningTaskWithDialog dialog) throws Exception;

    public void onError(LongRunningTaskWithDialog dialog,Exception ex){
    
    }
    
    public void onDone(LongRunningTaskWithDialog dialog,Object result) {
    }
}
