@echo off
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_HOME=%DIRNAME%
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

if defined JAVA_HOME (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_EXE=java.exe
)

if exist "%JAVA_HOME%\bin\java.exe" goto init
if not "%JAVA_HOME%" == "" goto fail
if exist "%JAVA_EXE%" goto init

:fail
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
exit /b 1

:init
"%JAVA_EXE%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
