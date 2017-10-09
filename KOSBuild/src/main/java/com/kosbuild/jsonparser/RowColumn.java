package com.kosbuild.jsonparser;

/**
 * @author sad
 */
public class RowColumn {

    private final int row;
    private final int column;

    public RowColumn(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return "[row=" + row + ", column=" + column + "]";
    }
}
