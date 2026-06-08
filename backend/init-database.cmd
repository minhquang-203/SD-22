@echo off
setlocal
cd /d "%~dp0"

set "SERVER=localhost"
set "USER=sa"
set "PASS=123456789"
set "SQL_FILE=%~dp0sql\SUNOVA_full.sql"

echo [SUNOVA] Dang nap database tu SUNOVA_full.sql ...
echo [SUNOVA] Server: %SERVER%  Database: SUNOVA
echo.

where sqlcmd >nul 2>&1
if errorlevel 1 (
  echo [SUNOVA] Khong tim thay sqlcmd. Cai SQL Server Command Line Tools hoac chay file SQL bang SSMS.
  echo File: %SQL_FILE%
  exit /b 1
)

sqlcmd -S %SERVER% -U %USER% -P %PASS% -i "%SQL_FILE%"
if errorlevel 1 (
  echo.
  echo [SUNOVA] Nap database THAT BAI. Kiem tra SQL Server dang chay va mat khau sa.
  exit /b 1
)

echo.
echo [SUNOVA] Nap database thanh cong. Chay start-backend.cmd de khoi dong API.
exit /b 0
