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
- **Themen-Klassifikation**: Basiert in der MVP-/Iteration-2-Phase auf
  (a) manuell zugewiesenem Thema pro Feed und (b) vorhandenen `<category>`-Tags
  im Feed. Keine automatische inhaltliche Einzelartikel-Klassifikation im MVP.
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
      erkannte Kategorien
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

Stand Build 4: Zwei Punkte umgesetzt (Suche, OPML), noch nicht auf dem Gerät
getestet. Die restlichen drei bewusst zurückgestellt, da komplexer/spekulativer
(Benachrichtigungen brauchen einen Laufzeit-Berechtigungsdialog und eine neue
"Themen beobachten"-Einstellung; Keyword-Klassifikation und Offline-Feinschliff
haben unklaren Mehrwert ohne konkreten Bedarf) – sag Bescheid, falls einer davon
doch Priorität haben soll.

- [x] Volltextsuche über Titel/Zusammenfassung (Suchsymbol oben in der Leiste)
- [ ] Keyword-basierte Zusatzklassifikation einzelner Artikel (zurückgestellt)
- [x] OPML-Export/-Import der Feed-Liste (Symbole in der Feed-Verwaltung;
      Export teilt eine .opml-Datei, Import liest eine ausgewählte Datei ein
      und überspringt bereits vorhandene Feed-URLs)
- [ ] Benachrichtigungen bei neuen Artikeln zu ausgewählten Themen (zurückgestellt)
- [ ] Offline-Lesen / Bild-Caching-Feinschliff (zurückgestellt)

## Build & Auslieferung

- Jede Iteration erhält eine fortlaufende Buildnummer (versionCode), sichtbar
  im App-Info-Screen.
- APK wird nach jedem Iterationsschritt direkt als Datei bereitgestellt.
- Dateiname enthält einen Hash/Build-Identifier gegen Browser-/System-Caching.
- Debug-Keystore ist fest im Repo hinterlegt, damit Android jede neue APK als
  Update der vorherigen erkennt (gleicher Signaturschlüssel).
