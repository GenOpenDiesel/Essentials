# Wagi vanisha

Waga wynika z permisji `essentials.vanish.weight.<liczba>`, np. `essentials.vanish.weight.100`.
Ta sama waga określa poziom ukrycia i poziom widoczności gracza.

Gracz z dodatnią wagą widzi osoby na vanishu o **równej lub niższej wadze**,
także bez `essentials.vanish.see`. Waga 100 widzi vanish 100 i 50, ale 50 nie widzi 100.
Przy wadze 0 nadal potrzeba `essentials.vanish.see` do widzenia innych graczy z wagą 0.
Samo `essentials.vanish.see` nie omija wag, nawet przy OP lub `essentials.*`.

Przykład dla LuckPerms (komendy w konsoli):

```text
lp group mod permission set essentials.vanish true
lp group mod permission set essentials.vanish.see true
lp group mod permission set essentials.vanish.weight.10 true

lp group hadmin permission set essentials.vanish true
lp group hadmin permission set essentials.vanish.see true
lp group hadmin permission set essentials.vanish.weight.50 true

lp group owner permission set essentials.vanish true
lp group owner permission set essentials.vanish.see true
lp group owner permission set essentials.vanish.weight.100 true
```

| Obserwator | Mod (10) | Hadmin (50) | Owner (100) |
| --- | --- | --- | --- |
| Mod (10) | Widzi | Nie widzi | Nie widzi |
| Hadmin (50) | Widzi | Widzi | Nie widzi |
| Owner (100) | Widzi | Widzi | Widzi |

Brak permisji wagowej oznacza wagę `0`, więc dotychczasowe zachowanie pozostaje zachowane,
dopóki nie przypiszesz wag. Przy wielu permisjach (także odziedziczonych) wygrywa najwyższa
aktywna waga. Wartości `false`, wildcardy, liczby ujemne, błędne i większe niż `2147483647`
są pomijane. Wildcard ani OP nie przyznają automatycznie wagi liczbowej.

Widoczność jest sprawdzana przy włączeniu vanisha, dołączeniu gracza i zmianie świata.
Zmiana uprawnień w LuckPerms odświeża widoczność automatycznie w obie strony.
Przy innych dostawcach uprawnień po zmianie wag należy ponownie włączyć vanish lub wejść na serwer.
Ukrywanie korzysta z Bukkit, więc obejmuje postać, listę TAB oraz sprawdzenia `canSee`
używane przez komendy EssentialsX. Inne pluginy z własnym systemem widoczności mogą wymagać osobnej integracji.
Listy `/list` (także licznik ukrytych) i `/near` pomijają ukryte osoby o wyższej wadze,
nawet przy `essentials.list.hidden` lub `essentials.vanish.interact`.

## Oznaczenie nad głową z pluginem TAB

Jeśli TAB jest zainstalowany na tym samym serwerze Bukkit/Paper i ma włączone
`scoreboard-teams`, Essentials automatycznie dopisuje szare `[VANISH]` do nicku
nad głową. Dotychczasowy suffix i jego placeholdery zostają zachowane.
Zmiana jest widoczna w ciągu sekundy; oznaczenie znika po wyłączeniu vanisha.
Integracja ponawia oznaczenie po dołączeniu gracza i `/tab reload`.
Obserwator musi mieć `tab.seevanished`, ponieważ TAB bez tej permisji usuwa
format nicku osoby na vanishu. Nadaj ją rangom obsługującym vanish, np.:

```text
lp group mod permission set tab.seevanished true
lp group hadmin permission set tab.seevanished true
lp group owner permission set tab.seevanished true
```

Sama permisja TAB nie omija wag ani `hidePlayer` w Essentials.
TAB może jednak rozsyłać takim obserwatorom dane drużyn również wyższych rang;
nie traktuj jego metadanych jako gwarancji ukrycia obecności przed zmodyfikowanym klientem.
Nie ustawia `tabprefix`, `tabsuffix` ani nazwy gracza w tabliście.
Aby Minecraft nie użył suffixu znad głowy również w TAB-ie, pozostaw w TAB
włączone `tablist-name-formatting` (standardowe ustawienie TAB).
Gracze z niższą wagą nie widzą ukrytej postaci ani oznaczenia.

Dla graczy z dodatnią wagą Essentials nie nakłada efektu mikstury z
`essentials.vanish.effect`: ukrywanie obsługuje `hidePlayer`, a równi/wyżsi
rangą widzą postać i nick. Zewnętrzne efekty niewidzialności oraz ustawienia
TAB ukrywające nicki mogą nadal ukryć postać lub oznaczenie.
TAB na proxy bez TAB na serwerze Bukkit/Paper nie jest obsługiwany przez tę integrację.
