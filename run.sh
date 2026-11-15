#!/bin/sh
# Compile and start the application (macOS and Linux).
# Usage:  ./run.sh

set -e
cd "$(dirname "$0")"

mkdir -p out
javac -cp "lib/*" -d out $(find src -name "*.java")
java -cp "out:lib/*" Main
