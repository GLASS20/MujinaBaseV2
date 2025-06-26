@echo off
set "PATH=%~dp0cmake-3.31.5-windows-x86_64\bin;%~dp0jdk8u442-b06\bin;%~dp0apache-maven-3.9.9\bin;%PATH%"
set "JAVA_HOME=%~dp0jdk8u442-b06"
CALL "%~dp0BuildTools\devcmd.bat"