# Nice Client (mod Fabric, Minecraft 1.21.4)

Co jest w środku (kod źródłowy, jeszcze NIEPRZETESTOWANY w grze):
- Mod Menu (klawisz domyślnie Prawy Shift, zmiana: Esc > Opcje > Sterowanie > Nice Client): kategorie All/Combat/HUD/Animations/Sound/World/Utility/Client, wyszukiwarka, ulubione (serce), karty z Enabled/Disabled i zębatką ustawień, rozmyte tło, animacja otwarcia.
- Zakładki USTAWIENIA i PROFILE (zapis/wczytywanie/usuwanie zestawów ustawień).
- Edytor HUD (przycisk "Edytuj HUD"): przeciąganie myszką, scroll = rozmiar, PPM = reset, czipy = pokaż/ukryj.
- Widgety: Reach, Armor HUD, Inventory HUD, FPS, Coordinates, Ping, Clock, Combo Counter.
- Moduły: Hit Sounds, Fullbright, Toggle Sprint.
- Zapis do config/niceclient.json (moduły, ulubione, ustawienia, pozycje i rozmiary HUD, profile).

## Budowanie
1. JDK 21. W tym folderze: `gradlew build` (Windows: `gradlew.bat build`).
2. Jar: build/libs/niceclient-1.0.0.jar (NIE ten z -sources). Wrzuć do mods razem z Fabric API 0.119.4+1.21.4.
3. Błędy kompilacji wklej do rozmowy z Claude – poprawię.

## Wiele wersji naraz
`buildall.bat` (Windows) albo `./buildall.sh` buduje jary dla wszystkich wersji z `versions.txt` (1.21 – 1.21.8) do folderu `dist/`. Fabric API dobierane automatycznie z Modrinth (wymaga internetu).
Pojedyncza wersja: `gradlew build -Pmc=1.21.4`.
Nazwy jarów: `niceclient-1.0.0+mc<wersja>.jar`. Wrzuć je do `lib/mod/` launchera – launcher sam wybierze właściwy dla wersji gry.
Starsze wersje (1.20.x i niżej) oraz 1.21.9+ mają inne API i wymagają osobnego kodu.

## Budowanie bez instalowania czegokolwiek (GitHub Actions)
1. Załóż konto na github.com, kliknij New repository (np. "niceclient"), wgraj do niego CAŁĄ zawartość tego folderu (Add file > Upload files; folder .github też).
2. Wejdź w zakładkę Actions – budowanie startuje samo (albo Run workflow).
3. Po kilku minutach w każdym zielonym przebiegu na dole jest "Artifacts": NiceClient-<wersja> – pobierz jar.
4. Czerwony przebieg = błąd kompilacji: otwórz go, skopiuj czerwone linie i wklej do rozmowy z Claude.
