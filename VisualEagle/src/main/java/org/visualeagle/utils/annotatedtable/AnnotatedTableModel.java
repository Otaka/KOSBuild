package org.visualeagle.utils.annotatedtable;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * @author Dmitry Savchenko
 */
public class AnnotatedTableModel<T> extends AbstractTableModel {

    private boolean isCellEditable = false;
    private ClassData classData;
    private ColumnData[] visibleColumns = null;
    private List<T> data=new ArrayList<T>(0);

    public AnnotatedTableModel(Class<T> clazz) {
        try{
        setClass(clazz);
        }catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
    
    public List<T> getData() {
        return data;
    }

    final public void setClass(Class clazz) throws NoSuchMethodException, Exception {
        ClassData cData = new ClassData();
        cData.setClazz(clazz);
        classData = cData;
        setVisibleColumns(getDefaultAllVisibleColumns(classData));
    }

    private static int[] getDefaultAllVisibleColumns(ClassData classData) {
        int size = classData.getColumns().size();
        int[] columnIds = new int[size];
        for (int i = 0; i < size; i++) {
            columnIds[i] = classData.getColumns().get(i).getId();
        }

        return columnIds;
    }

    public void setVisibleColumns(int... visibleColumnsIds) {
        visibleColumns = new ColumnData[visibleColumnsIds.length];
        for (int i = 0; i < visibleColumnsIds.length; i++) {
            visibleColumns[i] = classData.getColumnDataById(visibleColumnsIds[i]);
        }
    }

    public void setData(List<T> data) {
        if (data == null) {
            this.data = new ArrayList<T>(0);
        } else {
            this.data = data;
        }
    }

    @Override
    public String getColumnName(int column) {
        return visibleColumns[column].getColumnName();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public boolean isIsCellEditable() {
        return isCellEditable;
    }

    public void setIsCellEditable(boolean isCellEditable) {
        this.isCellEditable = isCellEditable;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return isCellEditable;
    }

    @Override
    public int getRowCount() {
        if (data == null) {
            return 0;
        }
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return visibleColumns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object record = data.get(rowIndex);
        ColumnData column = visibleColumns[columnIndex];
        String value = null;
        if (column.getVisualFormatter() != null) {
            try {
                value = (String) column.getVisualFormatter().invoke(record);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                Object object = column.getGetter().invoke(record);
                if (object != null) {
                    value = object.toString();
                } else {
                    value = "NULL";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return value;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
}
