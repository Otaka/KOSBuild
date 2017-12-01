package org.visualeagle.gui.small.longrunning;

/**
 * @author sad
 */
public abstract class LongTask<T> {

    public abstract T task(LongTaskReporter reporter);

}
