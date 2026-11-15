package data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Writes trade history to a CSV file (file handling).
 *
 * CSV means "comma-separated values": one line per trade, with a comma
 * between columns. Excel and Google Sheets open it directly.
 */
public class CsvExporter {

    /**
     * @return how many trades were written
     * @throws IOException if the file cannot be created or written
     */
    public int export(List<TradeRecord> records, File file) throws IOException {
        // try-with-resources closes the file even if writing fails halfway.
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Trade ID,Time,Stock,Buyer,Seller,Quantity,Price,Value");
            writer.newLine();

            for (TradeRecord record : records) {
                writer.write(record.getId() + ","
                        + record.getTime() + ","
                        + record.getSymbol() + ","
                        + clean(record.getBuyer()) + ","
                        + clean(record.getSeller()) + ","
                        + record.getQuantity() + ","
                        + String.format("%.2f", record.getPrice()) + ","
                        + String.format("%.2f", record.getValue()));
                writer.newLine();
            }
        }
        return records.size();
    }

    /** A comma inside a name would break the columns, so wrap such names in quotes. */
    private String clean(String text) {
        if (text.contains(",") || text.contains("\"")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
