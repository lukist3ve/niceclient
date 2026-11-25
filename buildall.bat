@echo off
if not exist dist mkdir dist
for /f %%v in (versions.txt) do (
  echo === Minecraft %%v ===
  call gradlew.bat clean build -Pmc=%%v --no-daemon
  if errorlevel 1 (echo BLAD budowania %%v >> dist\errors.txt) else (copy /Y build\libs\niceclient-*+mc%%v.jar dist\ >nul)
)
echo Gotowe. Jary w folderze dist\.
