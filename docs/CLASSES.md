# Class guide

Every class in the project, what it is for, and the main concept it demonstrates.
Use this when drawing the class diagram and writing the implementation section
of the report.

## model

| Class | Purpose | Concept |
|---|---|---|
| `Order` | Abstract request to buy or sell shares | Abstract class |
| `LimitOrder` | Order with a price limit; can wait in the book | Inheritance, overriding |
| `MarketOrder` | Order that takes the best available price | Inheritance, overriding |
| `OrderSide` | BUY or SELL | Enum |
| `Trader` | Abstract account holder with cash and holdings | Abstract class, `HashMap`, `synchronized` |
| `HumanTrader` | A user who logs in | Inheritance |
| `Stock` | A listed company and its last price | Encapsulation |
| `Trade` | A completed deal between a buyer and a seller | Immutable object (`final` fields) |

## engine

| Class | Purpose | Concept |
|---|---|---|
| `OrderBook` | Waiting orders for one stock, best first | `PriorityQueue`, `Comparator` |
| `MatchingEngine` | Validates orders, matches them, creates trades | Polymorphism, `throws`, `synchronized` |
| `TradeListener` | "Tell me when a trade happens" | Interface |
| `PriceHistory` | Recent prices of each stock, for the chart | Implements an interface |

## exceptions

| Class | Purpose | Concept |
|---|---|---|
| `ExchangeException` | Parent of all exchange errors | Custom checked exception |
| `InvalidOrderException` | Bad quantity, bad price, unknown stock, empty market | Exception hierarchy |
| `InsufficientFundsException` | Not enough free cash to buy | Exception carrying data |
| `InsufficientSharesException` | Not enough free shares to sell | Exception carrying data |

## data

| Class | Purpose | Concept |
|---|---|---|
| `DatabaseManager` | Opens the SQLite file and creates the tables | JDBC connection |
| `UserDAO` | Register, log in, save accounts | `PreparedStatement`, `ResultSet` |
| `StockDAO` | Load stocks and save prices | JDBC |
| `TradeDAO` | Save trades and read history | JDBC |
| `TradeRecord` | One saved row of the trades table | Plain data class |
| `CsvExporter` | Writes trade history to a CSV file | File handling, try-with-resources |
| `DatabaseTradeListener` | Saves each trade as it happens | Implements an interface |

## bot

| Class | Purpose | Concept |
|---|---|---|
| `BotTrader` | Computer trader running on its own thread | Inheritance plus `Runnable`, multithreading |

## gui

| Class | Purpose | Concept |
|---|---|---|
| `LoginFrame` | Log in or create an account | `JFrame`, `ActionListener` |
| `MainFrame` | Main window; arranges and refreshes the panels | `BorderLayout`, implements two interfaces, `invokeLater` |
| `MarketPanel` | List of stocks with price and change | `JTable`, selection listener |
| `PriceChartPanel` | Line chart of recent prices | Custom painting with `Graphics2D` |
| `OrderBookPanel` | Buy and sell orders for the selected stock | `JTable` |
| `OrderPanel` | Form for placing an order | Event handling, `try`/`catch` |
| `PortfolioPanel` | Cash, holdings and total worth | `JTable` |
| `TradeHistoryPanel` | Recent trades and the CSV export button | `JFileChooser`, exception handling |
| `StockSelectionListener` | "Tell me which stock was clicked" | Interface |
| `ReadOnlyTableModel` | Table model with editing switched off | Extending a library class |
| `ExchangeServices` | Bundle of shared objects passed to each screen | Composition |
| `Theme` | Shared colours, fonts and formatting | Static helpers and constants |

## test

| Class | Purpose |
|---|---|
| `EngineTest` | 40 checks of the matching rules and exceptions |
| `DataTest` | 17 checks of the database and CSV code |
| `BotTest` | 9 checks that two bots on threads keep the exchange consistent |
| `ConsoleDemo` | Step-by-step console walkthrough of the engine |

## How the parts connect

```
LoginFrame -> MainFrame -> OrderPanel --placeOrder()--> MatchingEngine <-- BotTrader (own thread)
                                                              |
                                                     onTrade(trade) to every TradeListener
                                                              |
                         +------------------------------------+-----------------------------+
                         |                                    |                             |
                 DatabaseTradeListener                   PriceHistory                   MainFrame
                 (saves to SQLite)                    (feeds the chart)            (redraws the screen)
```
