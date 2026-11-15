@echo off
rem Compile, then run every test (Windows).
rem Usage:  double-click this file, or type run-tests.bat in a terminal.

cd /d "%~dp0"
if not exist out mkdir out

dir /s /b src\*.java > sources.txt
javac -cp "lib/*" -d out @sources.txt
if errorlevel 1 (
    del sources.txt
    echo Compilation failed.
    pause
    exit /b 1
)
del sources.txt

echo === Engine tests ===
java -cp "out;lib/*" test.EngineTest
echo.
echo === Database and CSV tests ===
java -cp "out;lib/*" test.DataTest
echo.
echo === Bot (multithreading) tests ===
java -cp "out;lib/*" test.BotTest
pause
