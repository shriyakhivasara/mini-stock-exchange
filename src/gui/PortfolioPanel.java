package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import engine.MatchingEngine;
import model.Stock;
import model.Trader;

/**
 * Shows what the logged-in trader owns: cash, shares of each stock,
 * and what it is all worth at current prices.
 */
public class PortfolioPanel extends JPanel {

    private final MatchingEngine engine;
    private final Trader trader;

    private final JLabel cashLabel = new JLabel("", SwingConstants.RIGHT);
    private final JLabel freeCashLabel = new JLabel("", SwingConstants.RIGHT);
    private final JLabel totalLabel = new JLabel("", SwingConstants.RIGHT);
    private final ReadOnlyTableModel model =
            new ReadOnlyTableModel(new String[] {"Stock", "Shares", "Price", "Value"});

    public PortfolioPanel(MatchingEngine engine, Trader trader) {
        this.engine = engine;
        this.trader = trader;

        setLayout(new BorderLayout(0, 6));
        setBorder(Theme.padding());
        add(Theme.heading("My portfolio"), BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.setRowSelectionAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        totalLabel.setFont(Theme.HEADING_FONT);

        JPanel summary = new JPanel(new GridLayout(3, 2, 6, 4));
        summary.add(new JLabel("Cash"));
        summary.add(cashLabel);
        summary.add(new JLabel("Free cash"));
        summary.add(freeCashLabel);
        summary.add(new JLabel("Total worth"));
        summary.add(totalLabel);
        add(summary, BorderLayout.SOUTH);

        freeCashLabel.setToolTipText("Cash not already promised to your waiting buy orders");

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);

        double sharesValue = 0;
        for (Stock stock : engine.getStocks()) {
            int shares = trader.getSharesOf(stock.getSymbol());
            if (shares > 0) {
                double value = shares * stock.getLastPrice();
                sharesValue += value;
                model.addRow(new Object[] {
                        stock.getSymbol(),
                        shares,
                        Theme.money(stock.getLastPrice()),
                        Theme.money(value)
                });
            }
        }

        cashLabel.setText(Theme.money(trader.getCash()));
        freeCashLabel.setText(Theme.money(engine.getFreeCash(trader)));
        totalLabel.setText(Theme.money(trader.getCash() + sharesValue));
    }
}
