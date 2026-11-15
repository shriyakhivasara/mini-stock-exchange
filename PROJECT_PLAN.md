# Project plan

## Course requirements

- Java project, 10% weightage (40 marks). Marked on application design and Java coding.
- Use as many concepts as possible: inheritance, interfaces, exception handling,
  GUI, file handling, database connectivity.
- Submission: 30 November 2026 (report plus code or GitHub link, uploaded on LMS).
- Demo: 4 to 7 December 2026. 5 to 10 minutes, then an individual viva for each member.
  Bring a printed report and a working project.
- Report: cover page, abstract (100 to 150 words), introduction, system analysis and
  design (requirements, use case / UML, class diagram), implementation with
  screenshots, conclusion and future work, references. Times New Roman 12 (text) and
  14 (headings), line spacing 1.15, minimum 10 pages.

## What we are building

A mini stock exchange. Traders place orders; the exchange matches buyers with
sellers; each match is a trade; the last trade price is the stock price.

- **Order book:** per stock, buy orders sorted highest price first and sell orders
  lowest price first. Ties go to the earlier order.
- **Limit order:** trade only at a stated price or better. Waits in the book.
- **Market order:** trade now at the best available price. Never waits.
- **Trade price:** the price of the order that was already waiting in the book.

## Scope (fixed, do not add to it)

- 10 stocks named after real companies. Prices and trades are simulated; no real
  market data is used.
- Limit and market orders only.
- Two instances of one bot trader class, each on its own background thread.
- Swing GUI: login, market list, order form, live order book tables, price chart,
  portfolio, the user's own trades with profit per company, and all market trades.
- SQLite via JDBC for accounts, holdings and trades.
- CSV export of trade history.

## Languages and libraries

- Java (JDK 17 or newer) for everything, with a few SQL statements inside the data code.
- One library, as a jar in `lib/`: the SQLite JDBC driver.
- The price chart is drawn by hand with `Graphics2D`, and the theme is Nimbus,
  which ships with Java, so no chart or theme library is needed.
- No build tool. Use the run scripts, `javac`, or IntelliJ IDEA.

## Where each OOP concept appears

| Concept | Where |
|---|---|
| Inheritance, abstract classes | `Order` -> `LimitOrder`, `MarketOrder`; `Trader` -> `HumanTrader`, `BotTrader` |
| Polymorphism | `Order.acceptsPrice()`, `Trader.getTraderType()` |
| Interfaces | `TradeListener` (engine tells the GUI, database and chart about trades), `StockSelectionListener`, `Runnable` |
| Exception handling | `InsufficientFundsException`, `InsufficientSharesException`, `InvalidOrderException` |
| Collections | `PriorityQueue` order book, `HashMap` holdings |
| Multithreading | `BotTrader` threads, `synchronized` methods, `SwingUtilities.invokeLater` |
| File handling | CSV export |
| Database | SQLite via JDBC |
| GUI | Swing, including custom painting for the chart |

## Stages

1. Project skeleton and model classes
2. Order book and matching engine, with a console test
3. Custom exceptions and validation
4. Database and CSV export
5. GUI screens
6. Bot trader and live refresh
7. Integration, run scripts and documentation
8. Ten real companies, and the "My trades and profit" tab (added on request after stage 7)

All stages are complete. What remains is the report, the diagrams, the
screenshots and viva preparation.

## Module owners

- **Engine** (`model`, `engine`, `exceptions`): Shriya
- **GUI** (`gui`): to be assigned
- **Data and bot** (`data`, `bot`), report lead: to be assigned

## Timeline

- 15 to 17 Nov: class diagram and method signatures agreed
- 18 to 22 Nov: build modules; engine matching orders in the console by 20 Nov
- 23 to 25 Nov: integrate
- 26 to 27 Nov: test, fix, screenshots
- 28 to 29 Nov: report
- 30 Nov: submit
- 1 to 3 Dec: viva preparation; each member explains the other two modules

If the project is behind on 23 Nov, cut the bot and keep a simple refresh thread.

## The flow everyone must be able to explain

What happens when a user clicks "Buy":

1. The GUI reads the form and creates a `LimitOrder` or `MarketOrder`.
2. The engine validates it and throws a custom exception if the trader lacks cash or shares.
3. The order enters the matching loop for that stock.
4. While the best buy and best sell prices cross, the engine creates a `Trade`.
5. Both traders' cash and holdings are updated, and the stock's last price changes.
6. The trade and new balances are saved to the database.
7. The engine calls `TradeListener.onTrade()` and the GUI redraws.
