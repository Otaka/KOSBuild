package org.visualeagle.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 * @author sad
 */
public class LongRunningTaskWithDialog extends JDialog {

    private JProgressBar progressBar;
    private LongRunningTask task;
    private JLabel informationMessageLabel;

    public LongRunningTaskWithDialog(Window owner, LongRunningTask task) {
        super(owner, ModalityType.MODELESS);
        this.task = task;
        setSize(600, 300);
        init();
    }

    private void init() {
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(500, 30));
        informationMessageLabel = new JLabel();
        informationMessageLabel.setPreferredSize(new Dimension(500, 50));
        informationMessageLabel.setMaximumSize(new Dimension(500, 50));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this::cancel);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancel(null);
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        informationMessageLabel.setAlignmentX(1);
        informationMessageLabel.setOpaque(true);
        informationMessageLabel.setBackground(Color.red);
        add(Box.createVerticalStrut(5));
        add(informationMessageLabel);
        progressBar.setAlignmentX(0.5f);
        add(progressBar);
        
        add(Box.createVerticalStrut(5));
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(cancelButton);
        setLocationRelativeTo(getOwner());
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
                    
                    //setVisible(false);
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        task.onError(LongRunningTaskWithDialog.this, ex);
                    });
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
        setVisible(true);
    }

    public void setDialogTitle(String caption) {
        SwingUtilities.invokeLater(() -> {
            setTitle(caption);
        });
    }

    public void setInformationMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            informationMessageLabel.setText(message);
        });
    }

    private void cancel(ActionEvent e) {

    }

}
