# EssentialsX dla BFSMC

Zmodyfikowany fork [EssentialsX](https://github.com/EssentialsX/Essentials), udostępniany na [GNU GPL v3](LICENSE). Oryginalni autorzy zachowują prawa do swojego kodu. Poniższe zmiany dodano 19 października 2026 r.

## Domy i limity

- Po zapisaniu własnego domu `/sethome` pokazuje liczbę wykorzystanych i wolnych slotów: `Domy — wykorzystane: 3, wolne: 22.`
- Limit jest obliczany przez istniejący mechanizm EssentialsX z efektywnych permisji gracza i konfiguracji `sethome-multiple`.
- Nadpisanie domu nie zużywa kolejnego slotu. Nielimitowane domy pokazują `wolne: bez limitu`; wolne sloty nie spadają poniżej zera po obniżeniu limitu.
- Po osiągnięciu limitu proponowana jest pierwsza wyższa ranga, która faktycznie zapewnia więcej domów. Kolejność to VIP, EVIP, MVIP, ELITA. Dodatkowe sloty i dziedziczenie permisji są uwzględniane.
- Posiadacz ELITY otrzymuje wyłącznie propozycję dokupienia dodatkowych sethome. Ten sam komunikat jest używany, gdy żadna wyższa skonfigurowana ranga nie zwiększa aktualnego limitu.
- Link do `https://bfsmc.pl` jest zielony, podkreślony i klikalny; tooltip brzmi `Kliknij, aby sprawdzić ofertę`.
- Kolory rang: VIP żółty, EVIP złoty, MVIP ciemnofioletowy, ELITA jasnoniebieski.
- Rangi są rozpoznawane przez permisje `essentials.sethome.multiple.<ranga>` oraz grupy; zestawy limitów w konfiguracji powinny nosić nazwy `vip`, `evip`, `mvip`, `elita`.

## Klikalna lista domów

Lista `/home` wyświetla podkreślone nazwy, a pod nimi w osobnej linii `Kliknij aby teleportować` (zmiana z 20 października 2026 r.). Kliknięcie nazwy uruchamia `/essentials:home nazwa`, a tooltip wskazuje wybrany dom. Zachowane są standardowe sprawdzanie uprawnień, opłaty i zasady teleportacji.

Łóżko jest widoczne wyłącznie z odpowiednią permisją. Ustawione łóżko jest klikalne; brakujące jest przekreślone i nieklikalne. Przy przeglądaniu domów innego gracza kliknięcie zachowuje jego nazwę w komendzie.

Wszystkie nowe komunikaty i tooltipy są wpisane na stałe w kodzie. Nie dodano wpisów do `messages*.properties`. Ustawianie domu innemu graczowi zachowuje standardowy komunikat o limicie.

## Wagi vanisha

Dodano hierarchię vanisha opartą na `essentials.vanish.weight.<liczba>`.
Osoba z `essentials.vanish.see` widzi ukrytych graczy o równej lub niższej wadze.
Najwyższa aktywna permisja wagowa wygrywa; brak wagi oznacza `0`.
LuckPerms automatycznie odświeża widoczność po zmianie uprawnień.
Wagi obejmują postać, TAB, podpowiedzi nicków oraz listy `/list` i `/near`.
Przykładowe komendy dla rang mod, hadmin i owner: [VANISH-WEIGHTS.md](VANISH-WEIGHTS.md).

## Weryfikacja i pliki

20 października 2026 r. połączono oficjalną gałąź EssentialsX `2.x` do commita `e70bdb81`, w tym obsługę Minecraft 26.3, nowe dane przedmiotów i mobów, poprawki asynchronicznej teleportacji oraz ładowania chunków na Paper 26.x. Zachowano dodatki BFSMC dotyczące domów, kitów oraz osobnych cooldownów `/heal` i `/feed`.

Po aktualizacji do bazy 26.3 przeszły wszystkie 62 testy głównego EssentialsX (bez błędów i pominięć), w tym 24 testy dodatków BFSMC i 3 testy nowego mechanizmu Paper 26.x. Przeszły kontrole stylu kodu; zbudowano główny EssentialsX i pasujący EssentialsSpawn. Nie przeprowadzono próby na działającym serwerze Minecraft 26.3.

Po przeniesieniu dopisku pod listę domów ponownie przeszło wszystkich 6 testów klikalnej listy (w tym sprawdzenie nowej linii), kontrole stylu i budowanie obu JAR-ów.

24 października 2026 r. po dodaniu wag vanisha przeszło wszystkich 76 testów głównego
EssentialsX (w tym 14 nowych testów wag, bez błędów i pominięć), kontrole stylu oraz
budowanie EssentialsX i EssentialsSpawn. Testy sprawdzają hierarchię mod/hadmin/owner,
równe wagi, awans i degradację, zmianę świata, podpowiedzi nicków oraz listę graczy
i jej licznik ukrytych. Nie przeprowadzono próby na działającym serwerze Minecraft.

Gotowe pliki znajdują się w [`dist/`](dist/). `EssentialsX-BFSMC.jar` zastępuje główny EssentialsX; `EssentialsXSpawn-BFSMC.jar` zastępuje moduł Spawn. Nie należy instalować kilku wersji tego samego modułu jednocześnie.

Wcześniejsze dodatki forka, takie jak osobne 600-sekundowe cooldowny `/heal` i `/feed` oraz komunikaty rang przy kitach, pozostają częścią pluginu.
