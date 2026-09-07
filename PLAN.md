# RSS Newfeed – Projektplan

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

Stand Build 2: Auf Build 1 hin gab es echtes Nutzer-Feedback (zwei reale
RSS-Feeds getestet) mit zwei Bugs, beide behoben:
- Herausgeber-Name zeigte die rohe Feed-URL statt eines lesbaren Titels
  (off-by-one bei der XML-Tiefenprüfung für RSS-2.0-Feeds).
- Der rote Swipe-Hintergrund war dauerhaft sichtbar statt nur beim Wischen
  (Karte und Hintergrund hatten unterschiedliche Ränder).
Zusätzlich Farbschema komplett überarbeitet (kein zufälliges Wallpaper-Farbschema
mehr, bewusste kontrastreiche Palette, Rot nur noch für die Lösch-Geste).

- [x] Projekt-Grundgerüst (Gradle, Compose, Room, WorkManager, Navigation)
- [x] Debug-Signing-Konfiguration fix (konsistenter Keystore für Update-Erkennung)
- [x] Datenmodell: FeedSource, Article (Room-Entities + DAO)
- [x] RSS/Atom-Parser (Titel, Bild, Zusammenfassung, Volltext falls vorhanden,
      Herausgeber, Datum, Link, Kategorie) – Feed-Titel-Bug behoben, gegen
      zwei echte Feeds (n-tv, tagesschau) erfolgreich getestet
- [x] Feed-Verwaltung: Hinzufügen/Bearbeiten/Entfernen von Feed-URLs
- [x] Feed-Refresh: manuell (Pull-to-Refresh) + periodisch im Hintergrund
- [x] Artikel-Liste: chronologisch (neueste zuerst), Bild + Titel + max.
      2-zeilige Zusammenfassung + Herausgeber
- [x] Artikel-Detailansicht (Volltext aus Feed oder Custom-Tab-Fallback)
- [x] Automatische Gelesen-Markierung bei Erreichen des Artikelendes +
      hellgrüne Hervorhebung in der Liste
- [x] Swipe-to-dismiss (nach rechts) zum dauerhaften Ausblenden von Artikeln
- [x] Basisfilter: nach Herausgeber (Feed) und Gelesen/Ungelesen
- [x] Edge-to-edge-Layout ohne Überlappung des Fußbereichs – von dir bereits
      erfolgreich getestet
- [x] App-Info-Screen: Buildnummer, Anzeige dieser PLAN.md mit erledigten
      Punkten und nächstem Schritt

## Iteration 2 – Themen- und Filtersystem

Stand Build 2: implementiert, aber (wie schon bei Build 1) nur CI-kompiliert,
noch nicht von dir auf dem Gerät getestet – bitte insbesondere die
Themen-Zuweisung und das Filter-Sheet ausprobieren.

- [x] Themen-Tag pro Feed (manuell zuweisbar: Politik, Weltgeschehen,
      Wirtschaft, Sport, Kultur, Wissenschaft & Technik, Sonstiges) –
      Zuweisung über Bearbeiten-Dialog in der Feed-Verwaltung
- [x] Auswertung vorhandener `<category>`-Tags aus dem Feed als Zusatzfilter
- [x] Filter-UI: Themen ein-/ausschließen (z. B. Sport ausblenden) – neues
      Filter-Sheet über das Filter-Icon (rot hervorgehoben, wenn ein Filter aktiv ist)
- [x] Sprachfilter (pro Feed hinterlegt)
- [x] Datums-/Zeitraumfilter (Von/Bis über Datumsauswahl)
- [x] Kombinierbare Filter (Herausgeber + Thema + Datum + Sprache gleichzeitig)
- [x] Dauerhafte Persistenz verworfener Artikel (bereits seit Iteration 1
      durch die Room-Datenbank gegeben)

## Iteration 3 – Ausbaustufen (optional, nach Bedarf)

- [ ] Volltextsuche über Titel/Zusammenfassung
- [ ] Keyword-basierte Zusatzklassifikation einzelner Artikel
- [ ] OPML-Export/-Import der Feed-Liste
- [ ] Benachrichtigungen bei neuen Artikeln zu ausgewählten Themen
- [ ] Offline-Lesen / Bild-Caching-Feinschliff

## Build & Auslieferung

- Jede Iteration erhält eine fortlaufende Buildnummer (versionCode), sichtbar
  im App-Info-Screen.
- APK wird nach jedem Iterationsschritt direkt als Datei bereitgestellt.
- Dateiname enthält einen Hash/Build-Identifier gegen Browser-/System-Caching.
- Debug-Keystore ist fest im Repo hinterlegt, damit Android jede neue APK als
  Update der vorherigen erkennt (gleicher Signaturschlüssel).
