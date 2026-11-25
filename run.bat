@echo off
setlocal

echo ===================================================
echo     SmartHire: Resume Analyzer & Job Matcher
echo         Programming in Java (25CSE0202)
echo ===================================================

:: 1. Check if Maven is available
where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Maven detected. Running via Maven...
    call mvn clean compile exec:java
    goto end
)

:: 2. If Maven is not available, compile and run directly with javac/java
echo [INFO] Compiling SmartHire sources...
if not exist "target\classes" mkdir "target\classes"
dir /s /b src\main\java\*.java > sources.txt
javac -d target\classes @sources.txt
del sources.txt

if %ERRORLEVEL% EQU 0 (
    echo [INFO] Compilation successful. Starting SmartHire Console...
    java -cp target/classes main.Main
) else (
    echo [ERROR] Compilation failed. Please ensure JDK 17+ is installed and on PATH.
)

:end
pause
