@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM ----------------------------------------------------------------------------

@echo off
@setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_CMD_LINE_ARGS=%*

@REM Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome
set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto init
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
goto error

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe
if exist "%JAVA_EXE%" goto init
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
goto error

:init
@REM Determine wrapper jar location
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

@REM Download maven-wrapper.jar if not present
if exist %WRAPPER_JAR% goto runMaven

echo Downloading Maven Wrapper...
for /f "tokens=2 delims==" %%a in ('findstr /r "wrapperUrl" %WRAPPER_PROPERTIES%') do set WRAPPER_URL=%%a
"%JAVA_EXE%" -cp "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" org.apache.maven.wrapper.MavenWrapperMain 2>NUL
if not exist %WRAPPER_JAR% (
    powershell -Command "(New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%')"
)

:runMaven
@REM Determine Maven distribution
for /f "tokens=2 delims==" %%a in ('findstr /r "distributionUrl" %WRAPPER_PROPERTIES%') do set MAVEN_DIST_URL=%%a

set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.6
if exist "%MAVEN_HOME%\bin\mvn.cmd" goto execMaven

echo Downloading Maven 3.9.6...
if not exist "%MAVEN_HOME%" mkdir "%MAVEN_HOME%"
set MAVEN_ZIP=%TEMP%\apache-maven-3.9.6-bin.zip
powershell -Command "(New-Object Net.WebClient).DownloadFile('%MAVEN_DIST_URL%', '%MAVEN_ZIP%')"
powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force"
del "%MAVEN_ZIP%" 2>NUL

:execMaven
"%MAVEN_HOME%\bin\mvn.cmd" %MAVEN_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%
