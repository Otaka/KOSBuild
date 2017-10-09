package com.kosbuild.jsonparser;

/**
 * @author sad
 */
public class JsonBoolean extends JsonElement {

    private final boolean value;

    public JsonBoolean(boolean value) {
        this.value = value;
        
    }

    @Override
    public String getAsString() {
        return "" + value;
    }

    @Override
    public int getAsInt() {
        throw new UnsupportedOperationException("Cannot convert JsonBoolean to int");
    }

    @Override
    public long getAsLong() {
        throw new UnsupportedOperationException("Cannot convert JsonBoolean to long");
    }

    @Override
    public JsonArray getAsArray() {
        throw new UnsupportedOperationException("Cannot convert JsonBoolean to array");
    }

    @Override
    public JsonObject getAsObject() {
        throw new UnsupportedOperationException("Cannot convert JsonBoolean to array");
    }

    @Override
    public String toString() {
        return getAsString();
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean getAsBoolean() {
        return value;
    }

    @Override
    public float getAsFloat() {
        throw new UnsupportedOperationException("Cannot convert JsonBoolean to float");
    }

}
