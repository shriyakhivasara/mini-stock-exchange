# Mini Stock Exchange

A paper-trading stock exchange simulator written in Java for the Object Oriented
Programming course project (BITS Pilani, Dubai Campus).

Users place buy and sell orders, a matching engine pairs them into trades, and
the stock price moves with every trade.

## Status

| Stage | What | State |
|---|---|---|
| 1 | Project skeleton and model classes | Done |
| 2 | Order book and matching engine | Done |
| 3 | Custom exceptions and validation | Done |
| 4 | Database (SQLite) and CSV export | Done |
| 5 | Swing GUI | Done |
| 6 | Bot trader and live refresh | Done |
| 7 | Integration and final README | Not started |

## Folder structure

```
src/
  model/       Order, LimitOrder, MarketOrder, Stock, Trade, Trader, HumanTrader
  engine/      OrderBook, MatchingEngine, TradeListener
  exceptions/  ExchangeException and its three subclasses
  data/        database and CSV code (stage 4)
  gui/         Swing screens (stage 5)
  test/        EngineTest, which checks the matching rules
  Main.java    program entry point
lib/           library jars (added in later stages)
data/          database file and exported CSVs
docs/          report, diagrams, screenshots
```

## How to run

You need JDK 17 or newer.

From the project folder, on macOS or Linux:

```
javac -d out $(find src -name "*.java")
java -cp out Main
```

On Windows (PowerShell):

```
javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out Main
```

In IntelliJ IDEA: open the folder, mark `src` as the Sources Root, and run `Main`.

For now, `Main` runs a console demo: it places a series of orders and prints
the order book, the trades and the rejected orders.

## How to test

After compiling, run the engine checks:

```
java -cp out test.EngineTest
```

It prints PASS or FAIL for each rule (price priority, time priority, partial
fills, market orders, and every exception) and a total at the end.

## Team

Shriya Khivasara, Priyadarshini Mandelia, Prisha Gupta

See `PROJECT_PLAN.md` for the full plan, scope and timeline.
