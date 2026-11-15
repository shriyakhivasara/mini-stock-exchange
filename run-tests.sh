#!/bin/sh
# Compile, then run every test (macOS and Linux).
# Usage:  ./run-tests.sh

set -e
cd "$(dirname "$0")"

mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java")

echo "=== Engine tests ==="
java -cp "out:lib/*" test.EngineTest
echo
echo "=== Database and CSV tests ==="
java -cp "out:lib/*" test.DataTest
echo
echo "=== Bot (multithreading) tests ==="
java -cp "out:lib/*" test.BotTest
