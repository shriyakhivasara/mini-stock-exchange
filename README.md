# Mini Stock Exchange

A paper-trading stock exchange simulator written in Java for the Object Oriented
Programming course project (BITS Pilani, Dubai Campus).

Users log in, place buy and sell orders on ten well-known companies, and trade
against two bot traders. A matching engine pairs buyers with sellers, and each
stock's price is simply the price of its latest trade.

The company names are real, but everything else is simulated. Starting prices are
round numbers in the region of real share prices; after that, prices move only
through trades made inside this program. No real market data is used.

## Features

- Ten listed companies: Apple, Microsoft, Alphabet, Amazon, Tesla, Nvidia, Meta,
  Netflix, JPMorgan Chase and Disney
- Limit and market orders, matched by price-time priority
- Two bot traders on their own threads that keep prices moving
- Swing GUI: login, market list, live price chart, order book, order form,
  portfolio, and tabs for your own trades and all market trades
- "My trades and profit": every trade you made, plus per company the average buy
  price, average sell price, profit and profit margin
- Accounts, holdings, prices and every trade saved in a SQLite database
- Trade history export to CSV
- Custom exceptions for invalid orders, insufficient cash and insufficient shares

## How to run

You need JDK 17 or newer. Nothing else has to be installed.

| System | Start the application | Run the tests |
|---|---|---|
| Windows | `run.bat` | `run-tests.bat` |
| macOS / Linux | `./run.sh` | `./run-tests.sh` |

On first run, click **Create account** on the login window. A new account starts
with 100,000 in cash.

### Running from IntelliJ IDEA

1. Open the project folder.
2. Right-click `src` and choose **Mark Directory as > Sources Root**.
3. Right-click `lib/sqlite-jdbc-3.41.2.2.jar` and choose **Add as Library**.
4. Open `src/Main.java` and click the green run arrow.

## Project structure

```
src/
  Main.java      program entry point: builds everything and opens the login window
  model/         the things on an exchange: Order, Trader, Stock, Trade
  engine/        order book, matching engine, trade listener, price history
  exceptions/    the custom exception classes
  data/          SQLite database access (JDBC) and CSV export
  bot/           the computer-controlled trader
  gui/           Swing windows and panels
  test/          tests and a console demo
lib/             the SQLite JDBC driver jar
data/            the database file and exported CSVs (created when you run)
docs/            class guide, and space for the report, diagrams and screenshots
```

`docs/CLASSES.md` lists every class, what it does and which OOP concept it shows.

## Tests

| Test | What it checks | Checks |
|---|---|---|
| `test.EngineTest` | matching rules and every exception | 40 |
| `test.DataTest` | database save and load, CSV export, profit calculation | 31 |
| `test.BotTest` | two bots on threads: cash and shares are conserved | 9 |

`test.ConsoleDemo` prints a step-by-step walkthrough of the matching engine
without the GUI: `java -cp out test.ConsoleDemo`.

## Build stages

| Stage | What | State |
|---|---|---|
| 1 | Project skeleton and model classes | Done |
| 2 | Order book and matching engine | Done |
| 3 | Custom exceptions and validation | Done |
| 4 | Database (SQLite) and CSV export | Done |
| 5 | Swing GUI | Done |
| 6 | Bot trader and live refresh | Done |
| 7 | Integration, run scripts and documentation | Done |
| 8 | Ten real companies, and the "My trades and profit" tab | Done |

## Team

Shriya Khivasara, Priyadarshini Mandelia, Prisha Gupta

See `PROJECT_PLAN.md` for the plan, scope and timeline.
