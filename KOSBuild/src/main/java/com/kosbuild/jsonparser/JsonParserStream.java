package com.kosbuild.jsonparser;

import java.util.Stack;

/**
 * @author sad
 */
public class JsonParserStream {

    private final Stack<Integer> marks = new Stack<>();
    private final char[] values;
    private int currentIndex;

    public JsonParserStream(char[] values) {
        this.values = values;
        currentIndex = 0;
    }

    public void mark() {
        marks.push(currentIndex);
    }

    public void rewind() {
        int value = marks.pop();
        currentIndex = value;
    }

    public void reset() {
        currentIndex = 0;
    }

    public boolean eof() {
        return currentIndex >= values.length;
    }

    public String readWord() {
        int start = currentIndex;
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = getChar();
            if (!(Character.isJavaIdentifierPart(c)||c=='-'||c=='+'||c=='~')) {
                currentIndex--;
                break;
            }

            sb.append(c);
        }
        
        return sb.toString();
    }

    public String readString() {
        int start = currentIndex;
        char endSymbol = '\"';
        char c = getChar();
        if (!(c == '"' || c == '\'')) {
            throw new JsonParseException("String should start with ''' or '\"' symbols [" + determinePosition(start) + "]");
        }
        if (c == '\'') {
            endSymbol = '\'';
        }
        
        StringBuilder sb = new StringBuilder();
        boolean isNextEscape = false;
        while (true) {
            c = getChar();
            if (c == -1) {
                throw new JsonParseException("Error while parse Json. Looking for end of string started at " + determinePosition(start));
            }
            if (isNextEscape) {
                switch (c) {
                    case 'r':
                    case 'n':
                        c = '\n';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    case '\\':
                        c = '\\';
                        break;
                }
                sb.append(c);
                isNextEscape = false;
            } else if (c == endSymbol) {
                break;
            } else if (c == '\\') {
                isNextEscape = true;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public char getChar() {
        if (eof()) {
            return (char) -1;
        }

        char c = values[currentIndex];
        currentIndex++;
        return c;
    }

    public char getCharNotMove(int lookForwardCount) {
        if (eof()) {
            return (char) -1;
        }

        if((currentIndex+lookForwardCount)>=values.length){
            return (char)-1;
        }

        return values[currentIndex+lookForwardCount];
    }

    public char getCharNotMove() {
        if (eof()) {
            return (char) -1;
        }

        return values[currentIndex];
    }

    public void skipBlank() {
        while (true) {
            if (eof()) {
                return;
            }
            char c = getChar();
            if (!(c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f')) {
                currentIndex--;
                break;
            }
        }
    }

    public String readDigit() {
        StringBuilder sb = new StringBuilder();
        boolean alreadyHasPoint = false;
        while (true) {
            if (eof()) {
                break;
            }
            char c = getChar();
            if (Character.isDigit(c)) {
                sb.append(c);
            } else if (c == '.') {
                if (alreadyHasPoint) {
                    throw new JsonParseException("Parsing error [index " + currentIndex + "] digit already has separator '.'");
                } else {
                    sb.append(".");
                    alreadyHasPoint = true;
                }
            } else {
                currentIndex--;
                break;
            }
        }
        return sb.toString();
    }

    public int getCurrentPosition() {
        return currentIndex;
    }

    public RowColumn determinePosition(int position) {
        return new RowColumn(0, 0);
    }
}
