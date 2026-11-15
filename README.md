# Mini Stock Exchange

A paper-trading stock exchange simulator written in Java for the Object Oriented
Programming course project (BITS Pilani, Dubai Campus).

Users place buy and sell orders, a matching engine pairs them into trades, and
the stock price moves with every trade.

## Status

| Stage | What | State |
|---|---|---|
| 1 | Project skeleton and model classes | Done |
| 2 | Order book and matching engine | Not started |
| 3 | Custom exceptions and validation | Not started |
| 4 | Database (SQLite) and CSV export | Not started |
| 5 | Swing GUI | Not started |
| 6 | Bot trader and live refresh | Not started |
| 7 | Integration and final README | Not started |

## Folder structure

```
src/
  model/       Order, LimitOrder, MarketOrder, Stock, Trade, Trader, HumanTrader
  engine/      order book and matching engine (stage 2)
  exceptions/  custom exceptions (stage 3)
  data/        database and CSV code (stage 4)
  gui/         Swing screens (stage 5)
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

At stage 1, `Main` prints a short demo of the model classes.

## Team

Shriya Khivasara, Priyadarshini Mandelia, Prisha Gupta

See `PROJECT_PLAN.md` for the full plan, scope and timeline.
