@echo off
rem Build and start MessKhata with only the JDK (no Maven or IDE needed).
cd /d "%~dp0"
setlocal EnableDelayedExpansion
if exist out rmdir /s /q out
(for /r "src\main\java" %%f in (*.java) do (set "p=%%f" & echo "!p:\=/!")) > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    del sources.txt
    echo Build failed. Check that JDK 11 or newer is installed: java -version
    pause
    exit /b 1
)
del sources.txt
java -cp out com.messkhata.Main
