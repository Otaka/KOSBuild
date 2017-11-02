package org.visualeagle.utils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.SwingUtilities;

/**
 * @author sad
 */
public abstract class BaseActor<T> {

    private ArrayBlockingQueue<MessageWrapper> messageQueue;
    private Thread thread;
    private static Timer timeoutChecker = new Timer("actorTimeoutChecker");

    public BaseActor() {
        this(200);
    }

    public BaseActor(int messageQueueSize) {
        messageQueue = new ArrayBlockingQueue(messageQueueSize);
    }

    public void stop() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            thread = null;
            messageQueue.clear();
        }
    }

    protected abstract Object processMessage(T message) throws Exception;

    public BaseActor start() {
        stop();
        scheduleTimeoutChecker();
        thread = new Thread("Actor-thread") {
            @Override
            public void run() {
                while (!isInterrupted()) {
                    try {
                        MessageWrapper<T> messageWrapper = messageQueue.poll(1, TimeUnit.SECONDS);
                        if (messageWrapper != null) {
                            Object result = null;
                            Throwable throwable = null;
                            try {
                                result = processMessage(messageWrapper.object);
                            } catch (Throwable thr) {
                                if (thr instanceof InterruptedException) {
                                    throw (InterruptedException) thr;
                                }

                                throwable = thr;
                            }

                            processFinishEvent(messageWrapper, result, throwable);
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };

        thread.setDaemon(true);
        thread.start();
        return this;
    }

    private void processFinishEvent(MessageWrapper<T> messageWrapper, Object result, Throwable exception) {
        if (messageWrapper.event != null) {
            if (messageWrapper.eventDispatcher) {
                SwingUtilities.invokeLater(() -> {
                    messageWrapper.event.processed(messageWrapper.object, result, exception);
                });
            } else {
                messageWrapper.event.processed(messageWrapper.object, result, exception);
            }
        }
    }

    private void scheduleTimeoutChecker() {
        timeoutChecker.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!messageQueue.isEmpty()) {
                    ArrayList<MessageWrapper> messages = new ArrayList();
                    messageQueue.drainTo(messages);
                    long currentTimestamp = System.currentTimeMillis();
                    for (MessageWrapper mw : messages) {
                        if (mw.deadlineTimestamp >= currentTimestamp) {
                            processFinishEvent(mw, null, new TimeoutException("Timeout"));
                        } else {
                            messageQueue.offer(mw);
                        }
                    }
                }

                if (thread != null && !thread.isInterrupted()) {
                    scheduleTimeoutChecker();
                }
            }
        }, 1000);
    }

    public void sendMessage(ActorMessage message) {
        int oneMinuteTimeout = 60 * 1000;
        sendMessage(message, null, false, oneMinuteTimeout);
    }

    public void sendMessage(ActorMessage message, MessageProcessedEvent event, boolean eventDispatcher) {
        int oneMinuteTimeout = 60 * 1000;
        sendMessage(message, event, eventDispatcher, oneMinuteTimeout);
    }

    public void sendMessage(Object message, MessageProcessedEvent event, boolean eventDispatcher, int timeoutMilliseconds) {
        MessageWrapper messageWrapper = new MessageWrapper();
        messageWrapper.object = message;
        messageWrapper.event = event;
        messageWrapper.deadlineTimestamp = System.currentTimeMillis() + timeoutMilliseconds;
        messageQueue.offer(messageWrapper);
    }

    private static class MessageWrapper<T> {

        private long deadlineTimestamp;
        private T object;
        private MessageProcessedEvent event;
        private boolean eventDispatcher;
    }

}
