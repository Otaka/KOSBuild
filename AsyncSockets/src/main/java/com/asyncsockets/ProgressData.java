package com.asyncsockets;

/**
 * @author sad
 */
public class ProgressData {

    private Object max;
    private Object current;

    public ProgressData() {
    }

    public ProgressData(Object max, Object current) {
        this.max = max;
        this.current = current;
    }
    
    

    public ProgressData setCurrent(Object current) {
        this.current = current;
        return this;
    }

    public ProgressData setMax(Object max) {
        this.max = max;
        return this;
    }

    public Object getCurrent() {
        return current;
    }

    public Object getMax() {
        return max;
    }
}