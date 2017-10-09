package com.kosbuild.jsonparser;

/**
 * @author sad
 */
public class JsonString extends JsonElement {

    private String value;

    public JsonString(String value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getAsString() {
        return value;
    }

    @Override
    public int getAsInt() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new UnsupportedOperationException("Cannot parse string \"" + value + "\" as integer");
        }
    }

    @Override
    public long getAsLong() {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new UnsupportedOperationException("Cannot parse string \"" + value + "\" as long");
        }
    }

    @Override
    public JsonArray getAsArray() {
        throw new UnsupportedOperationException("Cannot convert string to array");
    }

    @Override
    public JsonObject getAsObject() {
        throw new UnsupportedOperationException("Cannot convert string to object");
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean getAsBoolean() {
        return parseAsBoolean(value);
    }

    @Override
    public float getAsFloat() {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException ex) {
            throw new UnsupportedOperationException("Cannot parse string \"" + value + "\" as float");
        }
    }

}
