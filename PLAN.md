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

- [ ] Projekt-Grundgerüst (Gradle, Compose, Room, WorkManager, Navigation)
- [ ] Debug-Signing-Konfiguration fix (konsistenter Keystore für Update-Erkennung)
- [ ] Datenmodell: FeedSource, Article (Room-Entities + DAO)
- [ ] RSS/Atom-Parser (Titel, Bild, Zusammenfassung, Volltext falls vorhanden,
      Herausgeber, Datum, Link, Kategorie)
- [ ] Feed-Verwaltung: Hinzufügen/Bearbeiten/Entfernen von Feed-URLs
- [ ] Feed-Refresh: manuell (Pull-to-Refresh) + periodisch im Hintergrund
- [ ] Artikel-Liste: chronologisch (neueste zuerst), Bild + Titel + max.
      2-zeilige Zusammenfassung + Herausgeber
- [ ] Artikel-Detailansicht (Volltext aus Feed oder Custom-Tab-Fallback)
- [ ] Automatische Gelesen-Markierung bei Erreichen des Artikelendes +
      hellgrüne Hervorhebung in der Liste
- [ ] Swipe-to-dismiss (nach rechts) zum dauerhaften Ausblenden von Artikeln
- [ ] Basisfilter: nach Herausgeber (Feed) und Gelesen/Ungelesen
- [ ] Edge-to-edge-Layout ohne Überlappung des Fußbereichs
- [ ] App-Info-Screen: Buildnummer, Anzeige dieser PLAN.md mit erledigten
      Punkten und nächstem Schritt

## Iteration 2 – Themen- und Filtersystem

- [ ] Themen-Tag pro Feed (manuell zuweisbar: Politik, Weltgeschehen,
      Wirtschaft, Sport, …)
- [ ] Auswertung vorhandener `<category>`-Tags aus dem Feed als Zusatzfilter
- [ ] Filter-UI: Themen ein-/ausschließen (z. B. Sport ausblenden)
- [ ] Sprachfilter (pro Feed hinterlegt)
- [ ] Datums-/Zeitraumfilter
- [ ] Kombinierbare Filter (Herausgeber + Thema + Datum + Sprache gleichzeitig)
- [ ] Dauerhafte Persistenz verworfener Artikel

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
