@echo off
setlocal
cd /d "%~dp0"

set "MAVEN_HOME=%USERPROFILE%\.m2\apache-maven-3.9.11"
set "MAVEN_ZIP=%TEMP%\apache-maven-3.9.11-bin.zip"
set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.11/apache-maven-3.9.11-bin.zip"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo [SUNOVA] Dang tai Maven 3.9.11...
  powershell -NoProfile -Command "Invoke-WebRequest -Uri '%MAVEN_URL%' -OutFile '%MAVEN_ZIP%' -UseBasicParsing; Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%USERPROFILE%\.m2' -Force"
  if errorlevel 1 (
    echo [SUNOVA] Khong tai duoc Maven. Kiem tra mang hoac cai Maven thu cong.
    exit /b 1
  )
)

echo [SUNOVA] Khoi dong backend tai http://localhost:8080 ...
call "%MAVEN_HOME%\bin\mvn.cmd" -DskipTests spring-boot:run
exit /b %ERRORLEVEL%
