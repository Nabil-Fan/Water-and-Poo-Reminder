@echo off
echo ========================================
echo   Water & Poo Tracker - Compiler
echo ========================================
echo.

echo [1/3] Cleaning old files...
if exist *.class del *.class
echo Done!
echo.

echo [2/3] Compiling Java files...
javac TrackerData.java
if errorlevel 1 (
    echo ERROR: Failed to compile TrackerData.java
    pause
    exit /b 1
)

javac AplikasiTracker.java
if errorlevel 1 (
    echo ERROR: Failed to compile AplikasiTracker.java
    pause
    exit /b 1
)
echo Done!
echo.

echo [3/3] Running application...
java AplikasiTracker
echo.

echo Application closed.
pause