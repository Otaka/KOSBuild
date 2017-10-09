package com.kosbuild.jsonparser;

/**
 * @author sad
 */
public abstract class JsonElement {
   
    public JsonElement() {
    }
    
    
    public abstract String getAsString();

    public abstract int getAsInt();

    public abstract long getAsLong();

    public abstract boolean getAsBoolean();

    public abstract float getAsFloat();

    public abstract JsonArray getAsArray();

    public abstract JsonObject getAsObject();

    public boolean isArray() {
        return false;
    }
    
    public boolean isObject() {
        return false;
    }

    public boolean isPrimitive() {
        return false;
    }

    private int x, y;

    public int getColumn() {
        return x;
    }

    public int getRow() {
        return y;
    }
    
    protected boolean parseAsBoolean(String value){
        value=value.toLowerCase();
        return value.equals("1")|| value.equals("y")||value.equals("yes")||value.equals("true");
    }
}
