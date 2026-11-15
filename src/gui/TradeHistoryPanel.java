package gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import data.CsvExporter;
import data.TradeDAO;
import data.TradeRecord;

/**
 * A table of the most recent trades on the exchange, read from the
 * database, with a button to export the full history to a CSV file.
 */
public class TradeHistoryPanel extends JPanel {

    private static final int ROWS_SHOWN = 100;

    private final TradeDAO tradeDAO;
    private final ReadOnlyTableModel model = new ReadOnlyTableModel(
            new String[] {"Time", "Stock", "Buyer", "Seller", "Shares", "Price", "Value"});
    private final JLabel statusLabel = new JLabel(" ");

    public TradeHistoryPanel(TradeDAO tradeDAO) {
        this.tradeDAO = tradeDAO;

        setLayout(new BorderLayout(0, 4));
        setBorder(Theme.padding());

        JButton exportButton = new JButton("Export all trades to CSV");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                exportToCsv();
            }
        });

        statusLabel.setForeground(Theme.MUTED_TEXT);

        JPanel top = new JPanel(new BorderLayout(10, 0));
        top.add(Theme.heading("Recent trades"), BorderLayout.WEST);
        top.add(statusLabel, BorderLayout.CENTER);
        top.add(exportButton, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.setRowSelectionAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    /** Reload the newest trades from the database. */
    public void refresh() {
        try {
            List<TradeRecord> records = tradeDAO.loadRecent(ROWS_SHOWN);
            model.setRowCount(0);
            for (TradeRecord record : records) {
                model.addRow(new Object[] {
                        record.getTime(),
                        record.getSymbol(),
                        record.getBuyer(),
                        record.getSeller(),
                        record.getQuantity(),
                        Theme.money(record.getPrice()),
                        Theme.money(record.getValue())
                });
            }
        } catch (SQLException e) {
            statusLabel.setText("Could not load trades: " + e.getMessage());
        }
    }

    private void exportToCsv() {
        // Let the user choose where to save, starting in the data folder.
        JFileChooser chooser = new JFileChooser(new File("data"));
        chooser.setSelectedFile(new File("trades.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;                         // the user pressed Cancel
        }

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getPath() + ".csv");
        }

        try {
            List<TradeRecord> records = tradeDAO.loadAll();
            int count = new CsvExporter().export(records, file);
            JOptionPane.showMessageDialog(this,
                    count + " trades saved to\n" + file.getAbsolutePath(),
                    "Export complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showError("Could not read the trades from the database: " + e.getMessage());
        } catch (IOException e) {
            showError("Could not write the file: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Export failed", JOptionPane.ERROR_MESSAGE);
    }
}
