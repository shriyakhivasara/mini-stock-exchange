package gui;

import model.Stock;

/**
 * Implemented by whoever needs to know when the user clicks a different
 * stock in the market list. MarketPanel calls it; MainFrame implements it.
 * This keeps MarketPanel from depending on MainFrame directly.
 */
public interface StockSelectionListener {

    void stockSelected(Stock stock);
}
