package com.kosbuild.jsonparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author sad
 */
public class JsonParser {

    private JsonParserStream stream;

    private String readFile(InputStreamReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(reader)) {
            boolean first = true;
            for (String line; (line = br.readLine()) != null;) {
                if (!first) {
                    sb.append("\n");
                }

                sb.append(line);
                first = false;
            }
        }
        return sb.toString();
    }

    public JsonElement parse(InputStreamReader reader) {
        try {
            return parse(readFile(reader));
        } catch (IOException ex) {
            throw new JsonParseException("Cannot read input stream");
        }
    }

    public JsonElement parse(String str) {
        stream = new JsonParserStream(str.toCharArray());
        JsonObject object = parseJsonObject(null);
        return object;
    }

    protected JsonObject parseJsonObject(JsonElement parent) {
        stream.skipBlank();
        char c = stream.getChar();
        if (c != '{') {
            throw new JsonParseException("JsonObject should start with '{' symbol [" + stream.determinePosition(stream.getCurrentPosition() - 1) + "]");
        }

        JsonObject obj = new JsonObject();
        OUTER:
        while (true) {
            stream.skipBlank();
            c = stream.getCharNotMove();
            switch (c) {
                case '}':
                    stream.getChar();
                    break OUTER;
                case '"':
                case '\'':
                default:
                    FieldValuePair element = readEntry(obj);
                    obj.getElements().add(element);
                    stream.skipBlank();
                    char comma = stream.getCharNotMove();
                    if (comma == ',') {
                        stream.getChar();
                    }
                    break;

                //throw new JsonParseException("Object should contain only 'name':'value' pairs ["+stream.determinePosition(stream.getCurrentPosition())+"]");
            }
        }

        return obj;
    }

    protected JsonArray parseJsonArray(JsonElement parent) {
        stream.skipBlank();
        char c = stream.getChar();
        if (c != '[') {
            throw new JsonParseException("JsonArray should start with '[' symbol [" + stream.determinePosition(stream.getCurrentPosition() - 1) + "]");
        }

        JsonArray obj = new JsonArray();
        while (true) {
            stream.skipBlank();
            c = stream.getCharNotMove();
            if (c == ']') {
                stream.getChar();
                break;
            } else {
                JsonElement element = readValuePart(parent);
                obj.getElements().add(element);
                stream.skipBlank();
                char comma = stream.getCharNotMove();
                if (comma == ',') {
                    stream.getChar();
                }
            }
        }

        return obj;
    }

    protected JsonElement readValuePart(JsonElement parent) {
        JsonElement element = null;
        stream.skipBlank();
        char c = stream.getCharNotMove();
        if (c == '{') {
            element = parseJsonObject(parent);
        } else if (c == '[') {
            element = parseJsonArray(parent);
        } else if (c == '"' || c == '\'') {
            String str = stream.readString();
            element = new JsonString(str);
        } else if (Character.isDigit(c)) {
            int position = stream.getCurrentPosition();
            String numbString = stream.readDigit();
            try {
                float digit = Float.parseFloat(numbString);
                element = new JsonNumber(digit);
            } catch (NumberFormatException ex) {
                throw new JsonParseException("Cannot parse number '" + numbString + "' [" + stream.determinePosition(position) + "'");
            }
        } else if (c == 't' || c == 'T' || c == 'f' || c == 'F') {
            String word = stream.readWord();
            if (word.equalsIgnoreCase("true") || word.equalsIgnoreCase("false")) {
                element = new JsonBoolean(word.equalsIgnoreCase("true"));
            }
        }
        return element;
    }
    
    protected FieldValuePair readEntry(JsonElement parent) {
        String fieldName;
        char c = stream.getCharNotMove();
        if (c == '\"' || c == '\'') {
            fieldName = stream.readString();
        } else {
            fieldName = stream.readWord();
        }
        stream.skipBlank();
        char separator = stream.getChar();
        if (separator != ':') {
            throw new JsonParseException("You should separate field and value with ':' [" + stream.determinePosition(stream.getCurrentPosition() - 1) + "]. Field name=["+fieldName+"]");
        }

        JsonElement element = readValuePart(parent);
        return new FieldValuePair(fieldName, element);
    }
}
