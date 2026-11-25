Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "    SmartHire: Resume Analyzer & Job Matcher" -ForegroundColor Green
Write-Host "         Programming in Java (25CSE0202)" -ForegroundColor Yellow
Write-Host "===================================================" -ForegroundColor Cyan

# 1. Check if Maven is available
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "[INFO] Running via Maven..." -ForegroundColor Yellow
    mvn clean compile exec:java
    exit
}

# 2. Check JDK executables
$javaCmd = "java"
$javacCmd = "javac"

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Program Files\Java\jdk-26.0.1\bin\java.exe") {
        $javaCmd = "C:\Program Files\Java\jdk-26.0.1\bin\java.exe"
        $javacCmd = "C:\Program Files\Java\jdk-26.0.1\bin\javac.exe"
    }
}

# 3. Compile sources
Write-Host "[INFO] Compiling SmartHire sources..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path "target\classes" | Out-Null
$files = (Get-ChildItem -Path src\main\java -Filter *.java -Recurse).FullName
& $javacCmd -d target/classes $files

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    exit
}

Write-Host "[INFO] Compilation successful. Starting SmartHire Console..." -ForegroundColor Green
& $javaCmd -cp target/classes main.Main
