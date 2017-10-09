package com.kosbuild.jsonparser;

/**
 * @author sad
 */
public class JsonNumber extends JsonElement {

    private final float value;

    public JsonNumber(float value) {
        this.value = value;
    }

    @Override
    public String getAsString() {
        return "" + value;
    }

    @Override
    public int getAsInt() {
        return (int) value;
    }

    @Override
    public long getAsLong() {
        return (long) value;
    }

    @Override
    public JsonArray getAsArray() {
        throw new UnsupportedOperationException("Cannot convert JsonNumber to array");
    }

    @Override
    public JsonObject getAsObject() {
        throw new UnsupportedOperationException("Cannot convert JsonNumber to array");
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean getAsBoolean() {
        return value != 0;
    }

    @Override
    public float getAsFloat() {
        return value;
    }

}
