package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import engine.MatchingEngine;
import model.LimitOrder;
import model.OrderSide;
import model.Stock;

/**
 * Shows the waiting orders for the selected stock: buy orders on the
 * left (highest price first) and sell orders on the right (lowest first).
 */
public class OrderBookPanel extends JPanel {

    private static final int MAX_ROWS = 15;
    private static final String[] COLUMNS = {"Price", "Shares", "Trader"};

    private final MatchingEngine engine;
    private final ReadOnlyTableModel buyModel = new ReadOnlyTableModel(COLUMNS);
    private final ReadOnlyTableModel sellModel = new ReadOnlyTableModel(COLUMNS);

    public OrderBookPanel(MatchingEngine engine) {
        this.engine = engine;

        setLayout(new BorderLayout());
        add(Theme.heading("Order book"), BorderLayout.NORTH);

        JPanel tables = new JPanel(new GridLayout(1, 2, 10, 0));
        tables.add(buildSide("Buy orders (highest price first)", buyModel, Theme.BUY_GREEN));
        tables.add(buildSide("Sell orders (lowest price first)", sellModel, Theme.SELL_RED));
        add(tables, BorderLayout.CENTER);
    }

    private JPanel buildSide(String title, ReadOnlyTableModel model, Color colour) {
        JLabel label = new JLabel(title);
        label.setForeground(colour);

        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.setForeground(colour);
        table.setRowSelectionAllowed(false);

        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    /** Reload both tables for the given stock. */
    public void refresh(Stock stock) {
        if (stock == null) {
            return;
        }
        fill(buyModel, engine.getWaitingOrders(stock.getSymbol(), OrderSide.BUY));
        fill(sellModel, engine.getWaitingOrders(stock.getSymbol(), OrderSide.SELL));
    }

    private void fill(ReadOnlyTableModel model, List<LimitOrder> orders) {
        model.setRowCount(0);               // remove the old rows
        int rows = Math.min(orders.size(), MAX_ROWS);
        for (int i = 0; i < rows; i++) {
            LimitOrder order = orders.get(i);
            model.addRow(new Object[] {
                    Theme.money(order.getLimitPrice()),
                    order.getRemainingQuantity(),
                    order.getTrader().getName()
            });
        }
    }
}
