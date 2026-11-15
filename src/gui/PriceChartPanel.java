package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import model.Stock;

/**
 * A line chart of one stock's recent trade prices.
 *
 * No chart library is used. The panel overrides paintComponent() and
 * draws the grid lines, the labels and the price line itself with
 * Graphics2D (custom painting).
 */
public class PriceChartPanel extends JPanel {

    private static final int LEFT = 62;     // space for the price labels
    private static final int RIGHT = 18;
    private static final int TOP = 42;      // space for the title
    private static final int BOTTOM = 26;
    private static final int GRID_LINES = 4;

    private Stock stock;
    private List<Double> prices = new ArrayList<>();

    public PriceChartPanel() {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(210, 214, 220)));
    }

    /** Give the chart new data and ask Swing to redraw it. */
    public void setData(Stock stock, List<Double> prices) {
        this.stock = stock;
        this.prices = prices;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);            // clears the background
        if (stock == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawTitle(g2);

        int chartWidth = getWidth() - LEFT - RIGHT;
        int chartHeight = getHeight() - TOP - BOTTOM;
        if (chartWidth <= 0 || chartHeight <= 0) {
            return;
        }

        if (prices.size() < 2) {
            g2.setColor(Theme.MUTED_TEXT);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString("Waiting for the first trade...", LEFT, TOP + chartHeight / 2);
            return;
        }

        // Find the lowest and highest price so the line fills the chart.
        double lowest = prices.get(0);
        double highest = prices.get(0);
        for (double price : prices) {
            lowest = Math.min(lowest, price);
            highest = Math.max(highest, price);
        }
        // Add a little room above and below (and avoid dividing by zero
        // when every price is the same).
        double room = Math.max((highest - lowest) * 0.10, highest * 0.002);
        lowest -= room;
        highest += room;

        drawGrid(g2, chartWidth, chartHeight, lowest, highest);
        drawPriceLine(g2, chartWidth, chartHeight, lowest, highest);

        g2.setColor(Theme.MUTED_TEXT);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.drawString("last " + (prices.size() - 1) + " trades  (oldest on the left)",
                LEFT, getHeight() - 8);
    }

    private void drawTitle(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.setFont(Theme.HEADING_FONT);
        String title = stock.getSymbol() + "   " + stock.getName();
        g2.drawString(title, LEFT, 26);

        double change = stock.getChangePercent();
        if (change < 0) {
            g2.setColor(Theme.SELL_RED);
        } else {
            g2.setColor(Theme.BUY_GREEN);
        }
        String priceText = Theme.money(stock.getLastPrice()) + "   " + String.format("%+.2f%%", change);
        int textWidth = g2.getFontMetrics().stringWidth(priceText);
        g2.drawString(priceText, getWidth() - RIGHT - textWidth, 26);
    }

    /** Horizontal lines with the price each one stands for. */
    private void drawGrid(Graphics2D g2, int chartWidth, int chartHeight, double lowest, double highest) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        for (int i = 0; i <= GRID_LINES; i++) {
            int y = TOP + chartHeight * i / GRID_LINES;
            double price = highest - (highest - lowest) * i / GRID_LINES;

            g2.setColor(new Color(232, 235, 240));
            g2.drawLine(LEFT, y, LEFT + chartWidth, y);

            g2.setColor(Theme.MUTED_TEXT);
            String label = Theme.money(price);
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, LEFT - labelWidth - 6, y + 4);
        }
    }

    /** Joins the prices with a line, and marks the latest one with a dot. */
    private void drawPriceLine(Graphics2D g2, int chartWidth, int chartHeight, double lowest, double highest) {
        // Green if the price ended higher than it started, red if lower.
        if (prices.get(prices.size() - 1) < prices.get(0)) {
            g2.setColor(Theme.SELL_RED);
        } else {
            g2.setColor(Theme.BUY_GREEN);
        }
        g2.setStroke(new BasicStroke(2f));

        int previousX = 0;
        int previousY = 0;
        for (int i = 0; i < prices.size(); i++) {
            // Convert (position in list, price) into pixel coordinates.
            int x = LEFT + chartWidth * i / (prices.size() - 1);
            int y = TOP + (int) Math.round(chartHeight * (highest - prices.get(i)) / (highest - lowest));
            if (i > 0) {
                g2.drawLine(previousX, previousY, x, y);
            }
            previousX = x;
            previousY = y;
        }
        g2.fillOval(previousX - 4, previousY - 4, 8, 8);
    }
}
