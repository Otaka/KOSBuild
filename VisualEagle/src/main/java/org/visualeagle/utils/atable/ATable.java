package org.visualeagle.utils.atable;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;

/**
 * @author Dmitry Savchenko
 */
public class ATable<T> extends JTable {

    private ATableModel<T> tableModel;

    public ATable(ATableModel<T> tableModel) {
        super(tableModel);
        this.tableModel = tableModel;

    }

    public void ensureRowIsVisible(int index) {
        int columnIndex = getSelectedColumn();
        if (columnIndex == -1) {
            columnIndex = 0;
        }

        scrollRectToVisible(getCellRect(index, columnIndex, true));
    }

    public void setSelectedRow(int row) {
        setRowSelectionInterval(row, row);
    }

    public void clear() {
        tableModel.getData().clear();
    }

    public void addData(T value) {
        tableModel.getData().add(value);
    }

    public T getRow(int row) {
        return tableModel.getData().get(row);
    }

    public List<T> getRows() {
        return tableModel.getData();
    }

    public void fireDataChanged() {
        tableModel.fireTableDataChanged();
    }

    public ATableModel<T> getTableModel() {
        return tableModel;
    }

    public T getSelectedObject() {
        int row = getSelectedRow();
        if (row != -1) {
            return tableModel.getData().get(row);
        }

        return null;
    }

    public List<T> getSelectedObjects() {
        int[] rows = getSelectedRows();
        if (rows.length != 0) {
            List<T> result = new ArrayList<T>(rows.length);
            for (int i : rows) {
                result.add(tableModel.getData().get(i));
            }

            return result;
        }

        return new ArrayList(0);
    }

    public static <T> ATable<T> createATable(Class<T> clazz) throws Exception {
        ATableModel<T> tableModel = new ATableModel(clazz);
        ATable table = new ATable(tableModel);
        table.setModel(tableModel);
        return table;
    }
}
