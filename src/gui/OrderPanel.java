package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import engine.MatchingEngine;
import exceptions.ExchangeException;
import model.LimitOrder;
import model.MarketOrder;
import model.Order;
import model.OrderSide;
import model.Stock;
import model.Trade;
import model.Trader;

/**
 * The form for placing an order on the selected stock.
 *
 * This is where the custom exceptions reach the user: whatever the
 * engine rejects is caught here and shown in an error dialog.
 */
public class OrderPanel extends JPanel {

    private static final String LIMIT = "Limit";
    private static final String MARKET = "Market";

    private final MatchingEngine engine;
    private final Trader trader;
    private final MainFrame mainFrame;
    private Stock stock;

    private final JLabel stockLabel = new JLabel("-");
    private final JRadioButton buyButton = new JRadioButton("Buy", true);
    private final JRadioButton sellButton = new JRadioButton("Sell");
    private final JComboBox<String> typeBox = new JComboBox<>(new String[] {LIMIT, MARKET});
    private final JTextField quantityField = new JTextField("10");
    private final JTextField priceField = new JTextField();
    private final JTextArea resultArea = new JTextArea(4, 20);

    public OrderPanel(MatchingEngine engine, Trader trader, MainFrame mainFrame) {
        this.engine = engine;
        this.trader = trader;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 8));
        setBorder(Theme.padding());
        add(Theme.heading("Place an order"), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
    }

    private JPanel buildForm() {
        // Only one of the two radio buttons can be selected at a time.
        ButtonGroup sideGroup = new ButtonGroup();
        sideGroup.add(buyButton);
        sideGroup.add(sellButton);
        buyButton.setForeground(Theme.BUY_GREEN);
        sellButton.setForeground(Theme.SELL_RED);

        JPanel sidePanel = new JPanel(new GridLayout(1, 2));
        sidePanel.add(buyButton);
        sidePanel.add(sellButton);

        stockLabel.setFont(Theme.HEADING_FONT);

        // A market order has no price, so grey out the price box for it.
        typeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                priceField.setEnabled(LIMIT.equals(typeBox.getSelectedItem()));
            }
        });

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Stock"));
        form.add(stockLabel);
        form.add(new JLabel("Side"));
        form.add(sidePanel);
        form.add(new JLabel("Order type"));
        form.add(typeBox);
        form.add(new JLabel("Shares"));
        form.add(quantityField);
        form.add(new JLabel("Limit price"));
        form.add(priceField);

        // Wrapping the grid in a BorderLayout stops the rows stretching tall.
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel buildBottom() {
        JButton placeButton = new JButton("Place order");
        placeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                placeOrder();
            }
        });

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setOpaque(false);
        resultArea.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 2));
        resultArea.setText("Pick a stock in the market list, fill in the form and place an order.");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(placeButton, BorderLayout.NORTH);
        panel.add(resultArea, BorderLayout.CENTER);
        return panel;
    }

    /** Called by MainFrame when the user clicks a different stock. */
    public void setStock(Stock stock) {
        this.stock = stock;
        stockLabel.setText(stock.getSymbol());
        priceField.setText(String.format("%.2f", stock.getLastPrice()));
    }

    private void placeOrder() {
        if (stock == null) {
            return;
        }

        OrderSide side;
        if (buyButton.isSelected()) {
            side = OrderSide.BUY;
        } else {
            side = OrderSide.SELL;
        }

        try {
            // Integer.parseInt throws NumberFormatException for text like "abc".
            int quantity = Integer.parseInt(quantityField.getText().trim());

            Order order;
            if (LIMIT.equals(typeBox.getSelectedItem())) {
                double price = Double.parseDouble(priceField.getText().trim());
                price = Math.round(price * 100.0) / 100.0;   // keep prices to 2 decimal places
                order = new LimitOrder(trader, stock, side, quantity, price);
            } else {
                order = new MarketOrder(trader, stock, side, quantity);
            }

            List<Trade> trades = engine.placeOrder(order);
            resultArea.setText(describeResult(order, trades));
            mainFrame.refreshAll();

        } catch (NumberFormatException e) {
            showError("Shares must be a whole number and the price must be a number.");
        } catch (ExchangeException e) {
            // Catches all three of our custom exceptions through their parent class.
            showError(e.getMessage());
        }
    }

    /** A one-paragraph summary of what happened to the order. */
    private String describeResult(Order order, List<Trade> trades) {
        String verb;
        if (order.getSide() == OrderSide.BUY) {
            verb = "Bought";
        } else {
            verb = "Sold";
        }

        if (trades.isEmpty()) {
            return "No match yet. Your order for " + order.getQuantity() + " " + stock.getSymbol()
                    + " is waiting in the order book.";
        }

        int shares = 0;
        double value = 0;
        for (Trade trade : trades) {
            shares += trade.getQuantity();
            value += trade.getValue();
        }
        String text = verb + " " + shares + " " + stock.getSymbol() + " for " + Theme.money(value)
                + " (average " + Theme.money(value / shares) + " per share).";

        if (!order.isFilled()) {
            if (order instanceof LimitOrder) {
                text += " The other " + order.getRemainingQuantity() + " are waiting in the order book.";
            } else {
                text += " The other " + order.getRemainingQuantity() + " could not be filled.";
            }
        }
        return text;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Order not placed", JOptionPane.ERROR_MESSAGE);
    }
}
