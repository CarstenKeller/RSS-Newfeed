# RSS Newsfeed – Projektplan

Android-Nachrichten-App zum gezielten, thematisch gefilterten Lesen von RSS-Feeds
mehrerer Agenturen/Anbieter. Zielplattform: Android 16 (API 36), minSdk 26.

Fortschritt wird über den Info-Button in der App angezeigt (Buildnummer +
diese Datei + nächster offener Schritt).

## Technologie-Entscheidungen

- Kotlin, Jetpack Compose + Material 3
- Room (lokale Datenbank: Feeds, Artikel, Status)
- WorkManager (periodischer Hintergrund-Refresh)
- OkHttp + eigener/leichter RSS/Atom-Parser (XmlPullParser-basiert)
- Coil (Bilder laden)
- Chrome Custom Tabs (Fallback für Volltext, wenn Feed keinen liefert)
- Edge-to-edge via WindowInsets/Scaffold – keine Überlappung mit System-Leisten

## Bekannte Einschränkungen (bewusste Scope-Entscheidungen)

- **Volltext**: Nur angezeigt, wenn der Feed ihn liefert (`content:encoded`).
  Sonst Öffnen des Originalartikels via Custom Tab. Kein Scraping fremder Seiten.
- **Themen-Klassifikation**: Basiert auf (a) manuell zugewiesenem Thema pro
  Feed, (b) vorhandenen `<category>`-Tags im Feed und (c) seit Iteration 3
  einer einfachen deutschen Stichwortliste pro Thema als dritter, schwächster
  Signal-Ebene. Das ist weiterhin keine echte inhaltliche Klassifikation
  (kein ML) – Stichwörter können Artikel verpassen oder gelegentlich falsch
  zuordnen. Wird sowohl für Filter als auch für Themen-Benachrichtigungen
  verwendet.
- **Sprache**: Wird pro Feed hinterlegt (aus `<language>`-Tag oder manuell),
  nicht pro Artikel erkannt.

## Iteration 1 – MVP (vollständig nutzbare Basisfunktionen)

Stand Build 3: Build 1 → Build 2 hatte einen ernsten, jetzt behobenen Bug:
`fallbackToDestructiveMigration()` hat beim Schema-Update vermutlich alle
gespeicherten Feeds gelöscht. Ab jetzt gibt es für jede Schema-Änderung eine
echte `Migration`, die bestehende Daten erhält – Feed-Quellen bleiben ab Build 3
versionsübergreifend gespeichert. App-Name korrigiert zu "RSS Newsfeed"
(Paketname/Repo-Name bleiben unverändert, um die Update-Kette nicht zu brechen).

- [x] Projekt-Grundgerüst (Gradle, Compose, Room, WorkManager, Navigation)
- [x] Debug-Signing-Konfiguration fix (konsistenter Keystore für Update-Erkennung)
- [x] Datenmodell: FeedSource, Article (Room-Entities + DAO)
- [x] RSS/Atom-Parser (Titel, Bild, Zusammenfassung, Volltext falls vorhanden,
      Herausgeber, Datum, Link, Kategorie) – gegen echte Feeds (n-tv, tagesschau)
      erfolgreich getestet
- [x] Feed-Verwaltung: Hinzufügen/Bearbeiten/Entfernen von Feed-URLs –
      bleiben jetzt zuverlässig über Updates hinweg gespeichert
- [x] Feed-Refresh: manuell (Pull-to-Refresh) + periodisch im Hintergrund
- [x] Artikel-Liste: chronologisch (neueste zuerst), Bild + Titel + max.
      2-zeilige Zusammenfassung + Herausgeber + Datum/Uhrzeit + aufklappbare
      erkannte Kategorien + "Nach oben"-Button (erscheint beim Herunterscrollen)
- [x] Artikel-Detailansicht (Volltext aus Feed oder Custom-Tab-Fallback)
- [x] Automatische Gelesen-Markierung bei Erreichen des Artikelendes +
      hellgrüne Hervorhebung in der Liste
- [x] Swipe nach rechts markiert einen Artikel als gelesen (verschiebt ihn in
      die "Gelesen"-Ansicht, statt ihn zu löschen)
- [x] Standardansicht zeigt nur ungelesene Artikel; ein Umschalter "Gelesen
      anzeigen" blendet die gelesenen/wegewischten Artikel ein
- [x] Edge-to-edge-Layout ohne Überlappung des Fußbereichs – von dir bereits
      erfolgreich getestet
- [x] App-Info-Screen: Buildnummer, Anzeige dieser PLAN.md mit erledigten
      Punkten und nächstem Schritt

## Iteration 2 – Themen- und Filtersystem

Stand Build 4: von dir getestet, funktioniert gut. Zwei kleine Bugs behoben:
Filter-Chip-Beschriftung wechselte nicht zwischen "Gelesen"/"Ungelesen
anzeigen", und die WebView-Erkennung "Artikelende erreicht" nutzte die
unzuverlässige `WebView.getScale()`-API und löste dadurch fast nie aus –
jetzt eine robuste, rein JavaScript-basierte Prüfung.

- [x] Themen-Tag pro Feed (manuell zuweisbar: Politik, Weltgeschehen,
      Wirtschaft, Sport, Kultur, Wissenschaft & Technik, Sonstiges) –
      Zuweisung über Bearbeiten-Dialog in der Feed-Verwaltung
- [x] Auswertung vorhandener `<category>`-Tags aus dem Feed als Zusatzfilter,
      pro Artikel aufklappbar einsehbar
- [x] Filter-UI: Themen ein-/ausschließen (z. B. Sport ausblenden) – neues
      Filter-Sheet über das Filter-Icon (rot hervorgehoben, wenn ein Filter aktiv ist)
- [x] Sprachfilter (pro Feed hinterlegt)
- [x] Datums-/Zeitraumfilter (Von/Bis über Datumsauswahl)
- [x] Kombinierbare Filter (Herausgeber + Thema + Datum + Sprache gleichzeitig)
- [x] Dauerhafte Persistenz gelesener/weggewischter Artikel (Room-Datenbank,
      jetzt mit echter Migration statt destruktivem Fallback)

## Iteration 3 – Ausbaustufen (optional, nach Bedarf)

Stand Build 5: alle fünf Punkte umgesetzt, noch nicht auf dem Gerät getestet.
Damit ist der ursprüngliche Plan vollständig abgearbeitet – weitere Schritte
gibt es nur noch, wenn du neue Wünsche hast.

- [x] Volltextsuche über Titel/Zusammenfassung (Suchsymbol oben in der Leiste)
- [x] Keyword-basierte Zusatzklassifikation einzelner Artikel (deutsche
      Stichwortliste pro Thema als dritte, schwächste Signal-Ebene nach
      Feed-Thema und `<category>`-Tag; siehe „Bekannte Einschränkungen“)
- [x] OPML-Export/-Import der Feed-Liste (Symbole in der Feed-Verwaltung;
      Export teilt eine .opml-Datei, Import liest eine ausgewählte Datei ein
      und überspringt bereits vorhandene Feed-URLs)
- [x] Benachrichtigungen bei neuen Artikeln zu ausgewählten Themen
      (Glocken-Symbol in der Artikelliste öffnet die Themen-Auswahl; fragt
      beim ersten aktivierten Thema die Benachrichtigungs-Berechtigung an;
      der Hintergrund-Refresh benachrichtigt bei neuen, thematisch
      passenden Artikeln)
- [x] Offline-Lesen / Bild-Caching-Feinschliff (Artikeltext liegt ohnehin in
      der Datenbank; Bilder in der Artikel-Detailansicht laufen jetzt über
      einen plattenzwischengespeicherten HTTP-Client, sodass bereits gesehene
      Bilder auch offline angezeigt werden – Listen-Vorschaubilder cacht Coil
      bereits von sich aus)

## Iteration 4 – Feedback nach Build 5 (Filter-UX, Favoriten)

Stand Build 6: alle Punkte umgesetzt, noch nicht auf dem Gerät getestet.
Kein ursprünglicher Plan-Punkt, sondern direktes Nutzer-Feedback nach dem
ersten intensiveren Gebrauch mit mehreren Quellen/Themen.

- [x] Filter-Chips (Herausgeber, Themen, Sprache) brechen jetzt um, statt bei
      vielen Einträgen horizontal scrollen zu müssen (wichtig auf schmalen
      Displays mit vielen Quellen)
- [x] Bugfix: Von/Bis-Datum ließ sich nach Auswahl nicht mehr einzeln auf
      neutral zurücksetzen – je Feld jetzt ein eigenes Zurücksetzen-Symbol
- [x] Filter-Favoriten: aktuelle Themen-/Herausgeber-/Sprachauswahl unter
      einem Namen speichern (z. B. "Politik & Wirtschaft", "Wissenschaft")
      und per Tippen wieder anwenden – als Schnellauswahl-Leiste direkt in
      der Artikelliste UND zur Verwaltung (speichern/löschen) im Filter-Sheet.
      Bewusst nicht Teil eines Favoriten: Lesestatus (bleibt laut Vorgabe ein
      einziger gemeinsamer "Gelesen"-Bereich) und Zeitraum (wird schnell veraltet)
- [x] Suche: eigener Bereich-Schalter (Alle/Ungelesen/Gelesen), unabhängig
      vom normalen Gelesen-Umschalter der Übersicht

## Iteration 5 – Feedback nach Build 6

Stand Build 7: beide Punkte umgesetzt, noch nicht auf dem Gerät getestet.

- [x] Bugfix: Der Bereich-Schalter (Alle/Ungelesen/Gelesen) bei der Suche
      brach auf schmalen Displays um, sodass die "Gelesen"-Beschriftung
      buchstabenweise untereinander stand – die Zeile ist jetzt horizontal
      scrollbar (wie die Favoriten-Schnellauswahl)
- [x] Herausgeber-Filter erlaubt jetzt Mehrfachauswahl (z. B. zwei Quellen
      gleichzeitig anzeigen) statt nur eines einzelnen Herausgebers – analog
      zur bestehenden Themen-Mehrfachauswahl. Gespeicherte Filter-Favoriten
      aus älteren Builds (einzelner Herausgeber) werden beim Laden automatisch
      in das neue Format überführt

## Iteration 6 – Feedback nach Build 7 (Header, Darstellung, Sprache)

Stand Build 8: umgesetzt, noch nicht auf dem Gerät getestet.

- [x] Header neu strukturiert: Titelzeile ("RSS Newsfeed") jetzt immer als eigene
      erste Zeile, ohne mit Icons um Platz konkurrieren zu müssen. Darunter ein
      Hamburger-Menü, das Suche, Filter, Themen-Benachrichtigungen, Feeds
      verwalten, Darstellung und Sprache bündelt (vorher einzelne Icons in der
      Kopfzeile). Rechts daneben weiterhin direkt erreichbar: der
      Gelesen/Ungelesen-Umschalter und (bei Bedarf) "Nach oben". Ein roter
      Punkt am Menü-Symbol zeigt an, wenn ein Filter aktiv ist. Der unterste
      Menüpunkt zeigt Versionsnummer und Build-Nummer direkt an und führt wie
      bisher zum vollständigen Info-Screen mit PLAN.md.
- [x] Darstellung: Hell/Dunkel/System (Standard) über das Hamburger-Menü
      wählbar, wird dauerhaft gespeichert und wirkt sofort ohne Neustart.
- [x] Sprache: Deutsch/English über das Hamburger-Menü wählbar (technischer
      Umschalter über Androids Pro-App-Sprache-API). **Wichtige Einschränkung**:
      Dies stellt bisher nur die interne Locale um (Datumsformate, Tastatur) –
      die Bildschirmtexte selbst sind weiterhin durchgängig als deutscher Text
      im Code hinterlegt (kein strings.xml mit Übersetzungen) und ändern sich
      beim Umschalten noch nicht sichtbar. Eine echte Übersetzung erfordert das
      Auslagern aller Texte in String-Ressourcen (de/en) – das ist eine eigene,
      größere Folge-Iteration und noch nicht umgesetzt.
- [x] Bugfix: Das Suchfeld saß im engen Titel-Slot der alten Kopfzeile, wodurch
      der lange Platzhaltertext umbrach und das Feld ungewöhnlich hoch wurde.
      Jetzt eigenständiger Bereich mit kurzem, einzeiligem Platzhalter.
- [x] Suchfeld und Alle/Ungelesen/Gelesen-Auswahl sind jetzt in einer
      gemeinsamen Karte zusammengefasst, damit der Zusammenhang optisch
      erkennbar ist; das zusätzliche Label "Durchsuchen:" entfällt dadurch.

## Iteration 7 – Feedback nach Build 8

Stand Build 9: umgesetzt, noch nicht auf dem Gerät getestet.

- [x] **Bugfix (echter Funktionsfehler, nicht nur fehlende Übersetzung)**: Die
      Sprachumschaltung (Deutsch/English) hat gar nicht gegriffen. Ursache laut
      offizieller Android-Dokumentation: `AppCompatDelegate.setApplicationLocales()`
      funktioniert nur, wenn die Activity von `AppCompatActivity` erbt – die App
      nutzte aber die einfache `ComponentActivity`. Jetzt behoben. Die bereits in
      Build 8 genannte Einschränkung gilt weiterhin: es ändert sich nur die
      interne Locale, die Bildschirmtexte bleiben bis zur String-Auslagerung
      Deutsch.
- [x] Bugfix: Die blaue Kopfzeile hatte keine feste Breite und schrumpfte im
      Such-Modus auf die Breite des Titeltextes zusammen (dadurch wirkten
      Kopfzeile und Suchbereich uneinheitlich/verunglückt). Jetzt spannt die
      Kopfzeile immer die volle Breite auf.
- [x] Tooltips an den beiden Icon-Buttons in der Feed-Verwaltung (Import/Export
      der OPML-Datei), da diese ohne Beschriftung unklar waren.
- [x] Neues Thema "Auto" ergänzt (inkl. Stichwortliste für die
      Keyword-Zusatzklassifikation) – automatisch auch bei den
      Themen-Benachrichtigungen wählbar.
- [x] Themen-Benachrichtigungen können jetzt zusätzlich an einen gespeicherten
      Filter-Favoriten gekoppelt werden (nicht nur an einzelne Themen) –
      Herausgeber/Themen/Sprache des Favoriten werden gegen neue Artikel geprüft.
- [x] Neuer Filter "Quellart": Alle / Mit Link zur Quelle / Nur Kurznachricht –
      filtert danach, ob ein Artikel einen Link zum Original-Artikel mitliefert
      oder nicht.

## Iteration 7b – Nachbesserung Build 9

Stand Build 10: umgesetzt, noch nicht auf dem Gerät getestet.

- [x] Bugfix: Der Themen-Benachrichtigungen-Dialog hatte keinen Scroll-Bereich.
      Bei genügend Themen (jetzt inkl. "Auto") plus Filter-Favoriten lief der
      Inhalt über die verfügbare Dialoghöhe hinaus und wurde abgeschnitten –
      dadurch waren die Filter-Favoriten praktisch unsichtbar/nicht auswählbar,
      sichtbar nur als abgeschnittenes Checkbox-Fragment unter "Sonstiges".
      Der Dialog-Inhalt ist jetzt vertikal scrollbar.
- Sprachumschaltung bestätigt: Der Umschalter selbst funktioniert jetzt
  (Locale wechselt), wie in Build 9 bereits angekündigt bleiben die
  Bildschirmtexte aber bis zur noch ausstehenden String-Auslagerung Deutsch.

## Build & Auslieferung

- Jede Iteration erhält eine fortlaufende Buildnummer (versionCode), sichtbar
  im App-Info-Screen.
- APK wird nach jedem Iterationsschritt direkt als Datei bereitgestellt.
- Dateiname enthält einen Hash/Build-Identifier gegen Browser-/System-Caching.
- Debug-Keystore ist fest im Repo hinterlegt, damit Android jede neue APK als
  Update der vorherigen erkennt (gleicher Signaturschlüssel).
