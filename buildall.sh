#!/usr/bin/env bash
# Buduje Nice Client dla wszystkich wersji z versions.txt -> dist/
set -u
mkdir -p dist
while read -r v; do
  [ -z "$v" ] && continue
  echo "=== Minecraft $v ==="
  if ./gradlew clean build -Pmc="$v" --no-daemon; then
    cp build/libs/niceclient-*+mc"$v".jar dist/ 2>/dev/null || echo "Brak jara dla $v"
  else
    echo "BLAD budowania $v" | tee -a dist/errors.txt
  fi
done < versions.txt
echo "Gotowe. Jary w folderze dist/."
