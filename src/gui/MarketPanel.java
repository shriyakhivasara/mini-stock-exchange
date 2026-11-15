package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import engine.MatchingEngine;
import model.Stock;

/**
 * The list of stocks with their current price and change.
 * Clicking a row tells the StockSelectionListener which stock was chosen.
 */
public class MarketPanel extends JPanel {

    private final List<Stock> stocks;
    private final ReadOnlyTableModel model =
            new ReadOnlyTableModel(new String[] {"Symbol", "Company", "Price", "Change"});
    private final JTable table = new JTable(model);

    public MarketPanel(MatchingEngine engine, final StockSelectionListener listener) {
        this.stocks = engine.getStocks();

        setLayout(new BorderLayout());
        setBorder(Theme.padding());
        add(Theme.heading("Market"), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        for (Stock stock : stocks) {
            model.addRow(new Object[] {stock.getSymbol(), stock.getName(), "", ""});
        }

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(115);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(3).setPreferredWidth(65);
        table.getColumnModel().getColumn(3).setCellRenderer(new ChangeRenderer());

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                int row = table.getSelectedRow();
                // getValueIsAdjusting is true while the mouse is still moving.
                if (!event.getValueIsAdjusting() && row >= 0) {
                    listener.stockSelected(stocks.get(row));
                }
            }
        });

        refresh();
    }

    /** Select the first stock (called once when the window opens). */
    public void selectFirst() {
        if (!stocks.isEmpty()) {
            table.setRowSelectionInterval(0, 0);
        }
    }

    /** Update the price and change columns in place, keeping the selection. */
    public void refresh() {
        for (int row = 0; row < stocks.size(); row++) {
            Stock stock = stocks.get(row);
            model.setValueAt(Theme.money(stock.getLastPrice()), row, 2);
            model.setValueAt(String.format("%+.2f%%", stock.getChangePercent()), row, 3);
        }
    }

    /** Draws the Change column in green for a rise and red for a fall. */
    private static class ChangeRenderer extends DefaultTableCellRenderer {
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
}
