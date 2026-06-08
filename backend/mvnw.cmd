@ECHO OFF
setlocal
set "MAVEN_CMD="

if defined MAVEN_HOME if exist "%MAVEN_HOME%\bin\mvn.cmd" set "MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
if not defined MAVEN_CMD if exist "%USERPROFILE%\.m2\apache-maven-3.9.11\bin\mvn.cmd" set "MAVEN_CMD=%USERPROFILE%\.m2\apache-maven-3.9.11\bin\mvn.cmd"
if not defined MAVEN_CMD (
  for /f "delims=" %%i in ('where mvn 2^>nul') do (
    set "MAVEN_CMD=%%i"
    goto :run
  )
)

if not defined MAVEN_CMD (
  echo [SUNOVA] Maven chua cai. Chay start-backend.cmd hoac cai Maven 3.9+
  exit /b 1
)

:run
call "%MAVEN_CMD%" %*
exit /b %ERRORLEVEL%
