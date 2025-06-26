@echo off
:start

echo Setting up environment
CALL "%~dp0\env.bat"

echo.
echo.
echo Building jar
cd InjectableJar
CALL mvn package

echo.
echo.
echo Remapping Jar
cd InjectableJar
CALL remap.bat
cd remapped

echo.
echo.
echo Writing jar bytes to header file
ignore_File2Hex.exe
copy InjectableJar.jar.hpp ..\..\..\src

echo.
echo.
echo Building dll
cd ..\..\..\
cmake -B build -G "NMake Makefiles" -DCMAKE_BUILD_TYPE=RelWithDebInfo
cmake --build build

echo.
echo.
echo Press enter to rebuild
pause > nul

goto start