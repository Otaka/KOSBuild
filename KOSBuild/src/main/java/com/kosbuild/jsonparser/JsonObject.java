package com.kosbuild.jsonparser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sad
 */
public class JsonObject extends JsonElement {

    private final List<FieldValuePair> elements = new ArrayList<>();

    public JsonObject() {

    }

    public void addElement(String name, JsonElement element) {
        elements.add(new FieldValuePair(name, element));
    }

    @Override
    public String getAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        boolean first = true;
        for (FieldValuePair pair : elements) {
            if (!first) {
                sb.append(", \n");
            }
            sb.append(pair.toString());
            first = false;
        }
        return sb.append("\n}").toString();
    }

    public boolean contains(String name) {
        return getElementByName(name, false) != null;
    }

    public JsonElement getElementByName(String name) {
        return getElementByName(name, true);
    }

    public JsonElement getElementByName(String name, boolean failIfNotFound) {
        for (FieldValuePair p : elements) {
            if (p.getName().equals(name)) {
                return p.getValue();
            }
        }
        if (failIfNotFound) {
            throw new IllegalArgumentException("JsonObject [" + getAsString() + "] does not have element [" + name + "]");
        } else {
            return null;
        }
    }

    public JsonElement removeElementByName(String name) {
        JsonElement removedLastElement = null;
        for (int i = 0; i < elements.size(); i++) {
            FieldValuePair p = elements.get(i);
            if (p.getName().equals(name)) {
                removedLastElement = elements.remove(i).getValue();
            }
        }

        return removedLastElement;
    }

    public List<FieldValuePair> getElements() {
        return elements;
    }

    @Override
    public int getAsInt() {
        throw new UnsupportedOperationException("Cannot convert JsonObject to int");
    }

    @Override
    public long getAsLong() {
        throw new UnsupportedOperationException("Cannot convert JsonObject to long");
    }

    @Override
    public JsonObject getAsObject() {
        return this;
    }

    @Override
    public JsonArray getAsArray() {
        throw new UnsupportedOperationException("Cannot convert JsonObject to JsonArray");
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public boolean getAsBoolean() {
        throw new UnsupportedOperationException("Cannot convert JsonObject to boolean");
    }

    @Override
    public float getAsFloat() {
        throw new UnsupportedOperationException("Cannot convert JsonObject to float");
    }

    @Override
    public String toString() {
        return getAsString();
    }

}
