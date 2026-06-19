@REM Apache Maven Wrapper startup script for Windows
@REM Licensed to the Apache Software Foundation (ASF)

@echo off
setlocal

set MAVEN_VERSION=3.9.9
set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin
set MAVEN_CMD=%MAVEN_HOME%\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd
set DISTRIBUTION_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip

if not exist "%MAVEN_CMD%" (
    echo Downloading Maven %MAVEN_VERSION% ...
    if not exist "%MAVEN_HOME%" mkdir "%MAVEN_HOME%"
    powershell -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%MAVEN_HOME%\maven.zip'"
    powershell -Command "Expand-Archive -Path '%MAVEN_HOME%\maven.zip' -DestinationPath '%MAVEN_HOME%' -Force"
    del "%MAVEN_HOME%\maven.zip"
    echo Maven %MAVEN_VERSION% downloaded.
)

"%MAVEN_CMD%" %*
endlocal
