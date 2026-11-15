# Instructions for Claude

This is a second-year university Java course project built by three students.
Read `PROJECT_PLAN.md` before making changes; it holds the scope, stages and timeline.

## Rules

- Java only, JDK 17 or newer, no build tool. Keep library use to the three jars
  named in the plan.
- Keep code at second-year level: no streams-heavy code, lambdas only where they
  are clearly simpler, no frameworks, no design patterns beyond what the plan names.
- Every class gets a short comment saying what it is for. The students must be
  able to explain every line in a viva.
- Do not add features outside the fixed scope in `PROJECT_PLAN.md`.
- Work one stage at a time and update the status table in `README.md`.
- After each change, compile and run:
  `javac -d out $(find src -name "*.java") && java -cp out Main`

## Layout

- `src/model` plain data classes
- `src/engine` order book and matching
- `src/exceptions` custom exceptions
- `src/data` JDBC and CSV
- `src/gui` Swing screens
