# Wagi vanisha

Waga wynika z permisji `essentials.vanish.weight.<liczba>`, np. `essentials.vanish.weight.100`.
Ta sama waga określa poziom ukrycia i poziom widoczności gracza.

Gracz z `essentials.vanish.see` widzi osoby na vanishu o **równej lub niższej wadze**.
Bez `essentials.vanish.see` nie widzi innych osób na vanishu, niezależnie od swojej wagi.
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
