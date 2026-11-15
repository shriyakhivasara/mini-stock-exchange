package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import data.ProfitCalculator;
import data.StockProfit;
import data.TradeDAO;
import data.TradeRecord;
import model.HumanTrader;

/**
 * The logged-in user's own history, in two tables:
 *   - left:  every trade they made, newest first
 *   - right: per company, what they bought and sold, at what average
 *            prices, and the profit and profit margin on what they sold
 */
public class MyTradesPanel extends JPanel {

    private final TradeDAO tradeDAO;
    private final String username;
    private final ProfitCalculator calculator = new ProfitCalculator();

    private final ReadOnlyTableModel tradesModel = new ReadOnlyTableModel(
            new String[] {"Time", "Company", "Action", "Shares", "Price", "Total", "With"});
    private final ReadOnlyTableModel profitModel = new ReadOnlyTableModel(
            new String[] {"Company", "Bought", "Avg buy", "Sold", "Avg sell", "Profit", "Margin"});
    private final JLabel totalLabel = new JLabel(" ", SwingConstants.RIGHT);

    public MyTradesPanel(TradeDAO tradeDAO, HumanTrader trader) {
        this.tradeDAO = tradeDAO;
        this.username = trader.getUsername();

        setLayout(new GridLayout(1, 2, 12, 0));
        setBorder(Theme.padding());
        add(buildTradesSide());
        add(buildProfitSide());

        refresh();
    }

    private JPanel buildTradesSide() {
        JTable table = new JTable(tradesModel);
        table.setRowHeight(22);
        table.setRowSelectionAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(140);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(Theme.heading("My trades"), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildProfitSide() {
        JTable table = new JTable(profitModel);
        table.setRowHeight(22);
        table.setRowSelectionAllowed(false);
        table.getColumnModel().getColumn(5).setCellRenderer(new SignColourRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new SignColourRenderer());

        totalLabel.setFont(Theme.HEADING_FONT);

        JPanel top = new JPanel(new BorderLayout());
        top.add(Theme.heading("Profit by company"), BorderLayout.WEST);
        top.add(totalLabel, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    /** Reload the user's trades from the database and redo the sums. */
    public void refresh() {
        try {
            List<TradeRecord> records = tradeDAO.loadForUser(username);   // oldest first
            fillTradesTable(records);
            fillProfitTable(calculator.calculate(records, username));
        } catch (SQLException e) {
            totalLabel.setText("Could not load your trades: " + e.getMessage());
        }
    }

    private void fillTradesTable(List<TradeRecord> records) {
        tradesModel.setRowCount(0);
        // Walk the list backwards so the newest trade is the top row.
        for (int i = records.size() - 1; i >= 0; i--) {
            TradeRecord record = records.get(i);

            String action;
            String otherTrader;
            if (record.wasBoughtBy(username) && record.wasSoldBy(username)) {
                action = "Bought and sold";      // the user's own two orders matched
                otherTrader = "myself";
            } else if (record.wasBoughtBy(username)) {
                action = "Bought";
                otherTrader = record.getSeller();
            } else {
                action = "Sold";
                otherTrader = record.getBuyer();
            }

            tradesModel.addRow(new Object[] {
                    record.getTime(),
                    record.getSymbol(),
                    action,
                    record.getQuantity(),
                    Theme.money(record.getPrice()),
                    Theme.money(record.getValue()),
                    otherTrader
            });
        }
    }

    private void fillProfitTable(List<StockProfit> summaries) {
        profitModel.setRowCount(0);

        double totalProfit = 0;
        double totalCost = 0;
        for (StockProfit summary : summaries) {
            totalProfit += summary.getProfit();
            totalCost += summary.getCostOfSharesSold();

            // Until something is sold there is no sell price or profit to show.
            String averageSell = "-";
            String profit = "";
            String margin = "";
            if (summary.getSharesSold() > 0) {
                averageSell = Theme.money(summary.getAverageSellPrice());
                profit = String.format("%+,.2f", summary.getProfit());
                margin = String.format("%+.2f%%", summary.getProfitMarginPercent());
            }

            profitModel.addRow(new Object[] {
                    summary.getSymbol(),
                    summary.getSharesBought(),
                    Theme.money(summary.getAverageBuyPrice()),
                    summary.getSharesSold(),
                    averageSell,
                    profit,
                    margin
            });
        }

        if (totalCost > 0) {
            totalLabel.setText("Total profit: " + String.format("%+,.2f", totalProfit)
                    + "  (" + String.format("%+.2f%%", totalProfit / totalCost * 100.0) + ")");
            if (totalProfit < 0) {
                totalLabel.setForeground(Theme.SELL_RED);
            } else {
                totalLabel.setForeground(Theme.BUY_GREEN);
            }
        } else {
            totalLabel.setText("Sell some shares to see your profit");
            totalLabel.setForeground(Theme.MUTED_TEXT);
        }
    }
}
