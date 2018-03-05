package org.visualeagle.utils.atable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.visualeagle.utils.atable.annotations.ATableField;
import org.visualeagle.utils.atable.annotations.ATableFieldType;

/**
 * @author Dmitry Savchenko
 */
public class ClassData {

    private Class clazz = null;
    private List<PreprocessedColumn> columns = null;
    private Map<Integer, PreprocessedColumn> columnMap = new TreeMap<Integer, PreprocessedColumn>();

    public Class getClazz() {
        return clazz;
    }

    public void processClazz(Class clazz) throws NoSuchMethodException, Exception {
        this.clazz = clazz;
        columns = processClass(clazz);
    }

    public List<PreprocessedColumn> getColumns() {
        return columns;
    }

    public PreprocessedColumn getColumnDataById(int id) {
        return columnMap.get(id);
    }

    private List<PreprocessedColumn> processClass(Class cd) throws NoSuchMethodException, Exception {
        List<PreprocessedColumn> tColumns = new ArrayList<PreprocessedColumn>(10);
        Class tClazz = cd;
        Class currentClass = tClazz;
        while (currentClass != null) {
            java.lang.reflect.Field[] fields = currentClass.getDeclaredFields();
            for (java.lang.reflect.Field f : fields) {
                ATableField fieldAnnotation = f.getAnnotation(ATableField.class);
                if (fieldAnnotation != null) {
                    tColumns.add(processField(fieldAnnotation, f, currentClass));
                }
            }

            java.lang.reflect.Method[] methods = currentClass.getDeclaredMethods();
            for (java.lang.reflect.Method m : methods) {
                ATableField methodAnnotation = m.getAnnotation(ATableField.class);
                if (methodAnnotation != null) {
                    tColumns.add(processMethod(methodAnnotation, m, currentClass));
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return tColumns;
    }

    private static String makeFirstLetterBig(String string) {
        if (string == null || string.length() == 0) {
            return "";
        }

        String firstLetter = String.valueOf(string.charAt(0)).toUpperCase();
        return firstLetter + string.substring(1);
    }

    private static TypeOfColumn resolveColumnType(Class columnType) throws Exception {
        String typeName = columnType.getName();
        if ("int".equals(typeName) || "java.lang.Integer".equals(typeName) || "long".equals(typeName)) {
            return TypeOfColumn.INTEGER;
        } else if ("float".equals(typeName) || "java.lang.Float".equals(typeName) || "double".equals(typeName) || "java.lang.Double".equals(typeName)) {
            return TypeOfColumn.DOUBLE;
        } else if ("java.lang.String".equals(typeName)) {
            return TypeOfColumn.STRING;
        } else if ("java.utils.Date".equals(typeName)) {
            return TypeOfColumn.DATE;
        } else if ("java.sql.Timestamp".equals(typeName)) {
            return TypeOfColumn.TIMESTAMP;
        } else if ("boolean".equals(typeName) || "java.lang.Boolean".equals(typeName)) {
            return TypeOfColumn.BOOLEAN;
        }

        throw new Exception("Do not know field type [" + typeName + "]");
    }

    private PreprocessedColumn processField(ATableField fieldAnnotation, Field field, Class clazz) throws NoSuchMethodException, Exception {
        PreprocessedColumn column = new PreprocessedColumn();
        String fieldName = field.getName();
        Class fieldType = field.getType();
        String getterMethodName = "get" + makeFirstLetterBig(fieldName);
        Method getter = clazz.getMethod(getterMethodName);
        String setterMethodName = "set" + makeFirstLetterBig(fieldName);
        Method setter = clazz.getMethod(setterMethodName, fieldType);
        String getterStringValueName = "get" + makeFirstLetterBig(fieldName) + "StringValue";
        Method getterStringValue = null;
        try {
            getterStringValue = clazz.getMethod(getterStringValueName);
        } catch (NoSuchMethodException nsme) {
        }
        column.setVisualFormatter(getterStringValue);
        column.setColumnName(fieldAnnotation.column());
        column.setId(fieldAnnotation.id());
        ATableFieldType typeAnnotation = field.getAnnotation(ATableFieldType.class);
        if (typeAnnotation != null && typeAnnotation.getType() != TypeOfColumn.NULL) {
            column.setColumnType(typeAnnotation.getType());
        } else {
            column.setColumnType(resolveColumnType(fieldType));
        }

        columnMap.put(fieldAnnotation.id(), column);
        column.setGetter(getter);
        column.setSetter(setter);
        return column;
    }

    private String createSetterNameFromGetterName(String name) {
        if (name.startsWith("get")) {
            name = name.substring(3);
        }

        if (name.startsWith("is")) {
            name = name.substring(2);
        }

        name = makeFirstLetterBig(name);
        return "set" + name;
    }

    private PreprocessedColumn processMethod(ATableField methodAnnotation, Method method, Class clazz) throws NoSuchMethodException, Exception {
        PreprocessedColumn column = new PreprocessedColumn();
        String methodName = method.getName();
        Class methodType = method.getReturnType();
        Method getter = method;

        String setterMethodName = createSetterNameFromGetterName(methodName);
        Method setter = null;
        try {
            setter = clazz.getMethod(setterMethodName, methodType);
        } catch (NoSuchMethodException nsme) {
        }

        String getterStringValueName = methodName + "StringValue";
        Method getterStringValue = null;
        try {
            getterStringValue = clazz.getMethod(getterStringValueName);
        } catch (NoSuchMethodException nsme) {
        }
        column.setVisualFormatter(getterStringValue);
        column.setColumnName(methodAnnotation.column());
        column.setId(methodAnnotation.id());
        ATableFieldType typeAnnotation = method.getAnnotation(ATableFieldType.class);
        if (typeAnnotation != null && typeAnnotation.getType() != TypeOfColumn.NULL) {
            column.setColumnType(typeAnnotation.getType());
        } else {
            column.setColumnType(resolveColumnType(methodType));
        }

        columnMap.put(methodAnnotation.id(), column);
        column.setGetter(getter);
        column.setSetter(setter);
        return column;
    }
}
