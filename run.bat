@echo off
rem Compile and start the application (Windows).
rem Usage:  double-click this file, or type run.bat in a terminal.

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

java -cp "out;lib/*" Main
