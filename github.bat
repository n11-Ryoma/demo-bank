@echo off
setlocal enabledelayedexpansion

REM ===== 現在日時を取得 =====
for /f "tokens=1-3 delims=/ " %%a in ("%date%") do (
    set yyyy=%%a
    set mm=%%b
    set dd=%%c
)

for /f "tokens=1-2 delims=: " %%a in ("%time%") do (
    set hh=%%a
    set min=%%b
)

REM 1桁時間対策（" 9" → "09"）
if "%hh:~0,1%"==" " set hh=0%hh:~1,1%

set COMMIT_MSG=%yyyy%-%mm%-%dd% %hh%:%min%

echo Commit message: %COMMIT_MSG%

REM ===== Git 操作 =====
git add .

git commit -m "%COMMIT_MSG%"

if errorlevel 1 (
    echo Commit failed (nothing to commit?)
    pause
    exit /b
)

git push

if errorlevel 1 (
    echo Push failed
    pause
    exit /b
)

echo Done.
pause
