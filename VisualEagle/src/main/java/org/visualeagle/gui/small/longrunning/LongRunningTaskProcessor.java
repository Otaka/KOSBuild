package org.visualeagle.gui.small.longrunning;

import java.awt.Dialog;
import java.awt.Dimension;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JProgressBar;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * @author sad
 */
public class LongRunningTaskProcessor<T> {

    private long silentWaitTime = 1000;
    private JDialog dialog;

    public LongRunningTaskProcessor setSilentWaitTime(long silentWaitTime) {
        this.silentWaitTime = silentWaitTime;
        return this;
    }

    public long getSilentWaitTime() {
        return silentWaitTime;
    }

    private static Executor executor = Executors.newCachedThreadPool();

    public T process(LongTask<T> task) {
        try {
            MutableObject result = new MutableObject();
            MutableBoolean finished = new MutableBoolean(false);
            LongTaskReporter reporter = new LongTaskReporter();
            executor.execute(() -> {
                try {
                    T taskResult = task.task(reporter);
                    result.setValue(taskResult);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    result.setValue(ex);
                } finally {
                    finished.setTrue();
                }
            });

            long startTime = System.currentTimeMillis();
            while (finished.isFalse()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - startTime > silentWaitTime) {
                    showDialog();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            return (T) result.getValue();
        } finally {
            destroy();
        }
    }

    private void destroy() {

    }

    private void showDialog() {
        dialog = new JDialog(null, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BoxLayout(dialog, BoxLayout.Y_AXIS));
        JProgressBar progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(200, 25));
        dialog.add(progressBar);
        dialog.pack();
        dialog.setVisible(true);
    }
}
