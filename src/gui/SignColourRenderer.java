package gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Draws a table cell in green when its text is a gain and in red when
 * it is a loss (text starting with a minus sign). Used for the Change
 * column of the market list and the profit columns of "My trades".
 *
 * A renderer is the object a JTable asks "how should this cell look?".
 */
public class SignColourRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                row, column);
        String text = String.valueOf(value);
        if (!isSelected) {
            if (text.startsWith("-")) {
                cell.setForeground(Theme.SELL_RED);
            } else {
                cell.setForeground(Theme.BUY_GREEN);
            }
        }
        return cell;
    }
}
