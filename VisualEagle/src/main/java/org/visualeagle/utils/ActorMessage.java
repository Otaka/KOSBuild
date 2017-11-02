package org.visualeagle.utils;

/**
 * @author sad
 */
public class ActorMessage {

    private String command;
    private Object data;

    public ActorMessage(String command, Object data) {
        this.command = command;
        this.data = data;
    }

    public ActorMessage(String command) {
        this(command, null);
    }

    public String getCommand() {
        return command;
    }

    public Object getData() {
        return data;
    }

}
