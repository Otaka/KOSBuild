package com.kosbuild.jsonparser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sad
 */
public class JsonArray extends JsonElement {

    private final List<JsonElement> elements = new ArrayList<>();

    public JsonArray() {
    }

    public JsonElement get(int i){
        return elements.get(i);
    }
    
    @Override
    public String getAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        boolean first = true;
        for (JsonElement e : elements) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(e.toString());
            first = false;
        }
        return sb.append(']').toString();
    }

    @Override
    public boolean isArray() {
        return true;
    }

    public int size() {
        return elements.size();
    }

    public List<JsonElement> getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return getAsString();
    }

    @Override
    public int getAsInt() {
        throw new UnsupportedOperationException("Cannot getAsInt from JsonArray");
    }

    @Override
    public long getAsLong() {
        throw new UnsupportedOperationException("Cannot getAsLong from JsonArray");
    }

    @Override
    public JsonArray getAsArray() {
        return this;
    }

    @Override
    public JsonObject getAsObject() {
        throw new UnsupportedOperationException("Cannot convert JsonArray to JsonObject");
    }

    @Override
    public boolean getAsBoolean() {
        throw new UnsupportedOperationException("Cannot convert JsonArray to boolean");
    }

    @Override
    public float getAsFloat() {
        throw new UnsupportedOperationException("Cannot convert JsonArray to float");
    }
}
