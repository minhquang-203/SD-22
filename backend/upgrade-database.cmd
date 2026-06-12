@echo off
setlocal
cd /d "%~dp0"

set "SERVER=localhost"
set "USER=sa"
set "PASS=123456789"
set "ALTER_FILE=%~dp0..\..\files\alter_san_pham_noi_bat.sql"
if not exist "%ALTER_FILE%" set "ALTER_FILE=%~dp0sql\alter_san_pham_noi_bat.sql"

echo [SUNOVA] Nang cap schema DB (noi_bat) ...
echo [SUNOVA] File: %ALTER_FILE%
echo.

where sqlcmd >nul 2>&1
if errorlevel 1 (
  echo [SUNOVA] Khong tim thay sqlcmd.
  exit /b 1
)

sqlcmd -S %SERVER% -U %USER% -P %PASS% -i "%ALTER_FILE%" -f 65001
if errorlevel 1 (
  echo [SUNOVA] Nang cap THAT BAI.
  exit /b 1
)

echo.
echo [SUNOVA] Nang cap thanh cong. Restart backend neu dang chay.
exit /b 0
