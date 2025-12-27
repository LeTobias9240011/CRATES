# CRATES Plugin

Ein umfassendes Minecraft-Crates-System mit GUI und Datenbankunterstützung.

## Funktionen

- **SQLite-Datenbank**: Persistente Speicherung von Crates, Schlüsseln und Belohnungen
- **GUI-System**: Mehrere Menüs zum Verwalten und Anzeigen von Crates
- **Befehle**:
  - `/crates` - Öffnet das Hauptmenü
  - `/crates editor` - Öffnet den Crates-Editor
  - `/crates givekey <spieler> <crate> [anzahl]` - Gib einem Spieler Schlüssel
  - `/crates removekey <spieler> <crate> [anzahl]` - Entferne Schlüssel von einem Spieler
  - `/crates setcrate` - Platziere eine Crate an einem Ort
  - `/crates wartungen <on/off>` - Wartungsmodus umschalten
  - `/crates preview <crate>` - Vorschau einer Crate
  - `/crates reload` - Plugin neu laden

## Installation

1. Baue das Plugin mit Maven: `mvn clean package`
2. Platziere die JAR-Datei im `plugins`-Ordner deines Servers
3. Starte den Server neu

## Konfiguration

Das Plugin verwendet SQLite zur Datenspeicherung. Die Datenbankdatei (`crates.db`) wird automatisch im Plugin-Datenordner erstellt.

## Berechtigungen

- `crates.admin` - Zugriff auf Admin-Befehle (Standard: op)
- `crates.use` - Crates verwenden (Standard: true)

## GUIs

### Hauptmenü
- Zeige alle verfügbaren Crates an
- Zeige Spielerprofil
- Menü-Schließen-Schaltfläche

### Vorschau
- Zeige Belohnungen für eine Crate an
- Seitennummerierung
- Zurück-Schaltfläche

### Editor
- Verwalte Crates
- Verwalte Schlüssel
- Menü-Schließen-Schaltfläche

### Crate-Editor
- Bearbeite Crate-Namen
- Verwalte Belohnungen
- Ändere Crate-Schlüsseltyp

### Belohnungen
- Zeige alle Belohnungen für eine Crate an
- Füge Belohnungen hinzu/bearbeite sie
- Seitennummerierung

## Entwicklung

Das Plugin ist wie folgt strukturiert:
- `CratesPlugin` - Hauptklasse des Plugins
- `DatabaseAPI` - Datenbankoperationen
- `CratesCommand` - Befehlshandler
- `GUI-Klassen` - Inventarmenüs
- `CrateInteractionListener` - Blockinteraktionshandling
- `HologramManager` - Hologrammerstellung und -verwaltung
