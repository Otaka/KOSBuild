package org.visualeagle.utils.annotatedtable;

import java.lang.reflect.Method;

/**
 * @author Dmitry Savchenko
 */
public class ColumnData {

    private String columnName;
    private int id;
    private Method setter;
    private Method getter;
    private Method visualFormatter;
    private COLUMNTYPE columnType;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Method getGetter() {
        return getter;
    }

    public void setGetter(Method getter) {
        this.getter = getter;
    }

    public Method getSetter() {
        return setter;
    }

    public void setSetter(Method setter) {
        this.setter = setter;
    }

    public COLUMNTYPE getColumnType() {
        return columnType;
    }

    public void setColumnType(COLUMNTYPE type) {
        this.columnType = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Method getVisualFormatter() {
        return visualFormatter;
    }

    public void setVisualFormatter(Method visualFormatter) {
        this.visualFormatter = visualFormatter;
    }
}
