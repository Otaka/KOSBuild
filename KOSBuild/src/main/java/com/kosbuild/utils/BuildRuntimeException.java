package com.kosbuild.utils;

/**
 * @author sad
 */
public class BuildRuntimeException extends RuntimeException {

    private boolean doNotPrintStacktrace = false;

    public BuildRuntimeException(String message) {
        super(message);
    }

    public BuildRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public BuildRuntimeException setDoNotPrintStacktrace(boolean doNotPrintStacktrace) {
        this.doNotPrintStacktrace = doNotPrintStacktrace;
        return this;
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return doNotPrintStacktrace == true ? new StackTraceElement[0] : super.getStackTrace();
    }

}
