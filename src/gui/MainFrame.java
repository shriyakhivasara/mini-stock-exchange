package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import engine.MatchingEngine;
import engine.TradeListener;
import model.HumanTrader;
import model.Stock;
import model.Trade;

/**
 * The main trading window. It arranges the panels and keeps them up to date.
 *
 * Layout (BorderLayout):
 *   NORTH  - header with the user's name and a log out button
 *   WEST   - market list
 *   CENTER - price chart above the order book
 *   EAST   - order form above the portfolio
 *   SOUTH  - two tabs: the user's own trades and profit, and all market trades
 *
 * It implements two interfaces:
 *   - TradeListener: the engine tells it when a trade happens
 *   - StockSelectionListener: the market list tells it which stock was clicked
 */
public class MainFrame extends JFrame implements TradeListener, StockSelectionListener {

    private final ExchangeServices services;
    private final MatchingEngine engine;

    private final MarketPanel marketPanel;
    private final PriceChartPanel chartPanel;
    private final OrderBookPanel orderBookPanel;
    private final OrderPanel orderPanel;
    private final PortfolioPanel portfolioPanel;
    private final TradeHistoryPanel historyPanel;
    private final MyTradesPanel myTradesPanel;

    private final Timer refreshTimer;
    private Stock selectedStock;
    private int tradesShown = -1;          // trade count when the history table was last loaded

    public MainFrame(ExchangeServices services, HumanTrader trader) {
        super("Mini Stock Exchange");
        this.services = services;
        this.engine = services.getEngine();

        marketPanel = new MarketPanel(engine, this);
        chartPanel = new PriceChartPanel();
        orderBookPanel = new OrderBookPanel(engine);
        orderPanel = new OrderPanel(engine, trader, this);
        portfolioPanel = new PortfolioPanel(engine, trader);
        historyPanel = new TradeHistoryPanel(services.getTradeDAO());
        myTradesPanel = new MyTradesPanel(services.getTradeDAO(), trader);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(buildHeader(trader), BorderLayout.NORTH);
        add(marketPanel, BorderLayout.WEST);
        add(buildCentre(), BorderLayout.CENTER);
        add(buildRightSide(), BorderLayout.EAST);
        add(buildBottomTabs(), BorderLayout.SOUTH);

        marketPanel.setPreferredSize(new Dimension(310, 0));

        setSize(1280, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        // Ask the engine to call onTrade() on this window.
        engine.addTradeListener(this);

        // Orders can join the book without any trade happening, so also
        // refresh once a second. A Swing Timer runs on the GUI thread.
        refreshTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                refreshAll();
            }
        });
        refreshTimer.start();

        marketPanel.selectFirst();         // triggers stockSelected()
    }

    private JPanel buildHeader(HumanTrader trader) {
        JLabel title = new JLabel("Mini Stock Exchange");
        title.setFont(Theme.TITLE_FONT);
        title.setForeground(Color.WHITE);

        JLabel user = new JLabel("Logged in as " + trader.getName() + " (" + trader.getUsername() + ")");
        user.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Log out");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                logOut();
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(user);
        right.add(logoutButton);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.HEADER_BLUE);
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCentre() {
        orderBookPanel.setPreferredSize(new Dimension(0, 205));

        JPanel centre = new JPanel(new BorderLayout(0, 10));
        centre.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        centre.add(chartPanel, BorderLayout.CENTER);
        centre.add(orderBookPanel, BorderLayout.SOUTH);
        return centre;
    }

    /** Two tabs sharing the bottom of the window. */
    private JTabbedPane buildBottomTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My trades and profit", myTradesPanel);
        tabs.addTab("All market trades", historyPanel);
        tabs.setPreferredSize(new Dimension(0, 235));
        return tabs;
    }

    private JPanel buildRightSide() {
        JPanel right = new JPanel(new BorderLayout());
        right.setPreferredSize(new Dimension(310, 0));
        right.add(orderPanel, BorderLayout.NORTH);
        right.add(portfolioPanel, BorderLayout.CENTER);
        return right;
    }

    // ---------------------------------------------------------------
    // Keeping the screen up to date
    // ---------------------------------------------------------------

    /** Redraw every panel from the current state of the exchange. */
    public void refreshAll() {
        marketPanel.refresh();
        portfolioPanel.refresh();
        if (selectedStock != null) {
            chartPanel.setData(selectedStock,
                    services.getPriceHistory().getPrices(selectedStock.getSymbol()));
            orderBookPanel.refresh(selectedStock);
        }

        // Reading the database is slower, so only do it after a new trade.
        int tradeCount = engine.getTradeCount();
        if (tradeCount != tradesShown) {
            historyPanel.refresh();
            myTradesPanel.refresh();
            tradesShown = tradeCount;
        }
    }

    /**
     * Called by the engine after every trade, possibly from the bot's
     * thread. Swing components may only be touched from the GUI thread,
     * so invokeLater() hands the refresh over to that thread.
     */
    @Override
    public void onTrade(Trade trade) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                refreshAll();
            }
        });
    }

    /** Called by MarketPanel when the user clicks a stock. */
    @Override
    public void stockSelected(Stock stock) {
        selectedStock = stock;
        orderPanel.setStock(stock);
        refreshAll();
    }

    private void logOut() {
        refreshTimer.stop();
        engine.removeTradeListener(this);
        new LoginFrame(services).setVisible(true);
        dispose();
    }
}
