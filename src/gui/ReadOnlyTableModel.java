package gui;

import javax.swing.table.DefaultTableModel;

/**
 * A table model whose cells cannot be edited by the user.
 *
 * Swing's DefaultTableModel lets the user type into any cell. All our
 * tables only display data, so this subclass overrides one method to
 * switch editing off (inheritance from a library class).
 */
public class ReadOnlyTableModel extends DefaultTableModel {

    public ReadOnlyTableModel(String[] columnNames) {
        super(columnNames, 0);   // 0 = start with no rows
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
