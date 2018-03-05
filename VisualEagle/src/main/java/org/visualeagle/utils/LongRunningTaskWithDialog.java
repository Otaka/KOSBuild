package org.visualeagle.utils;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import net.miginfocom.swing.MigLayout;

/**
 * @author sad
 */
public class LongRunningTaskWithDialog extends JDialog {

    private JProgressBar progressBar;
    private LongRunningTask task;
    private JLabel informationMessageLabel1;
    private JLabel informationMessageLabel2;
    private boolean canceled = false;
    private Timer throtterTimer;

    private String newInformationMessage1;
    private String newInformationMessage2;
    private String newProgressBarMessage;
    private int newProgressBarValue = -1;

    private long lastChangeTimestamp = System.currentTimeMillis();
    private double multiplier = 1;//in case if maxvalue bigger than max int, then we will use multiplier

    public LongRunningTaskWithDialog(Window owner, LongRunningTask task) {
        super(owner, ModalityType.MODELESS);
        this.task = task;
        init();
    }

    private void init() {
        progressBar = new JProgressBar();

        informationMessageLabel1 = new JLabel("  ");
        informationMessageLabel2 = new JLabel("  ");

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this::cancel);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel(null);
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new MigLayout());
        setLayout(new BorderLayout(5, 30));
        add(panel, BorderLayout.CENTER);

        panel.add(informationMessageLabel1, "wrap, width 10cm, al center");
        panel.add(informationMessageLabel2, "wrap, width 10cm, al center");
        panel.add(progressBar, "wrap, width 10cm");
        panel.add(cancelButton, "wrap, al center");
        revalidate();
        validate();
        pack();
        setLocationRelativeTo(getOwner());

        throtterTimer = new Timer(50, (ActionEvent e) -> {
            long currentTimestamp = System.currentTimeMillis();
            if ((currentTimestamp - lastChangeTimestamp) > 100) {
                if (newInformationMessage1 != null || newInformationMessage2 != null || newProgressBarValue != -1 || newProgressBarMessage != null) {
                    SwingUtilities.invokeLater(() -> {
                        if (newInformationMessage1 != null) {
                            informationMessageLabel1.setText(newInformationMessage1);
                            newInformationMessage1 = null;
                        }
                        if (newInformationMessage2 != null) {
                            informationMessageLabel2.setText(newInformationMessage2);
                            newInformationMessage2 = null;
                        }
                        if (newProgressBarMessage != null) {
                            if (progressBar.isStringPainted() == false) {
                                progressBar.setStringPainted(true);
                            }

                            progressBar.setString(newProgressBarMessage);
                            newProgressBarMessage = null;
                        }
                        if (newProgressBarValue != -1) {
                            progressBar.setValue(newProgressBarValue);
                            newProgressBarValue = -1;
                        }
                    });
                    lastChangeTimestamp = currentTimestamp;
                }
            }
        });

        throtterTimer.setRepeats(true);
        throtterTimer.start();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (b == false) {
            throtterTimer.stop();
            throtterTimer = null;
            task = null;
        }
    }

    public void start() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Object result = task.run(LongRunningTaskWithDialog.this);
                    SwingUtilities.invokeLater(() -> {
                        task.onDone(LongRunningTaskWithDialog.this, result);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        task.onError(LongRunningTaskWithDialog.this, ex);
                        ex.printStackTrace();
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        if (isCanceled()) {
                            try {
                                task.onError(LongRunningTaskWithDialog.this, new CancelException());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        setVisible(false);
                    });

                }
            }
        };

        thread.setDaemon(true);
        thread.start();
        setVisible(true);
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setTextInProgressBar(String text) {
        newProgressBarMessage = text;
    }

    public void setDialogTitle(String caption) {
        SwingUtilities.invokeLater(() -> {
            if (isCanceled()) {
                setTitle("[Canceled]" + caption);
            } else {
                setTitle(caption);
            }
        });
    }

    public void setInformationMessage1(String message) {
        newInformationMessage1 = message;
    }

    public void setInformationMessage2(String message) {
        newInformationMessage2 = message;
    }

    public void setIndeterminate(boolean indeterminate) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(indeterminate);
        });
    }

    public void setMaxProgressValue(long maxProgressValue) {
        int maxValue;
        if (maxProgressValue >= Integer.MAX_VALUE) {
            int newMaxProgressValue = 2_000_000_000;
            multiplier = (double)maxProgressValue / (double)newMaxProgressValue;
            maxValue = newMaxProgressValue;
        } else {
            maxValue = (int) maxProgressValue;
        }

        SwingUtilities.invokeLater(() -> {

            progressBar.setMaximum(maxValue);
        });
    }

    public void setCurrentProgressValue(long currentValue) {
        if (multiplier == 1) {
            newProgressBarValue = (int) currentValue;
        } else {
            newProgressBarValue = (int) (currentValue / multiplier);
        }
    }

    public void addCurrentProgressValue(long currentValue) {
        if (multiplier == 1) {
            newProgressBarValue = progressBar.getValue() + (int) currentValue;
        } else {
            newProgressBarValue = progressBar.getValue() + (int) (currentValue / multiplier);
        }
    }

    private void cancel(ActionEvent e) {
        if (canceled == false) {
            canceled = true;
            SwingUtilities.invokeLater(() -> {
                String dialogTitle = getTitle();
                setTitle("[Canceled]" + dialogTitle);
            });
        }
    }

    public static class CancelException extends RuntimeException {

    }

}
