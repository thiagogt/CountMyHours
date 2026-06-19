# CountMyHours — Project Context

## Architecture

Pure Java desktop app (no Spring Boot). Three-layer structure:

- **model/** — POJOs for persistence: `WorkHourItem`, `WorkHourSelling`, `WorkPeriodTracker`
- **service/** — Business logic: import, persistence, calculations, business day calendar
- **view/** — JavaFX UI (programmatic, no FXML): Dashboard, Timeline, ExtraHours, DataEntry
- **util/** — Chart styling, color palette, month names

No `module-info.java` — runs on classpath. The `javafx-maven-plugin` handles `--add-modules`.

## Model Classes

- `WorkHourItem` — Java record. One row from CSV: date (LocalDate), client, project, item, hours, sourceFile. Convenience constructor without sourceFile. `withSourceFile()` for immutable copy. equals/hashCode excludes sourceFile.
- `WorkHourSelling` — Java record: year, hoursSold, vacationDaysSold, note. Derived `vacationHoursSold()`.
- `ImportRecord` — Java record: fileName, filePath, importDate, entriesImported. equals/hashCode on filePath + importDate only.
- `VacationEntry` — Java record: year, month, days
- `MonthNote` — Java record: year, month, holidays (custom count), observation (holiday names/notes)
- `WorkPeriodTracker` — Mutable class (not a record): List of entries + sellings + importHistory + vacationDays + monthNotes + metadata

All monthly/yearly aggregations are computed by `CalculationService`, not stored.
Record accessors use component-name style (`item.date()`, `item.project()`) not getter style (`getDate()`).

## CSV Format

Semicolon-delimited. Date as `dd/MM/yyyy`:
```
Data;Cliente;Projeto;Item;Hs
01/06/2026;Monsters SA;Wasowski;SCARE-42, door calibration;8
```

## Key Services

- `BusinessDayService` — Brazilian national holidays (fixed + Easter-based moveable) + São Paulo state holiday (Jul 9). Calculates working days per month, expected hours = business days × 8. `getHolidaysInMonth()` returns named holidays on weekdays. `getNamedHolidays()` returns all holidays with names.
- `CsvImportService` — Parses CSV (`;` delimiter only, validates format on import) and XLSX with the same column structure.
- `LegacyXlsxImportService` — Parses the old monthly-aggregated spreadsheet (2017-2025). Converts to `WorkHourItem` entries dated to 1st of month with item="legacy-import", client="Opus".
- `JsonPersistenceService` — Jackson ObjectMapper with JavaTimeModule. Stores at `~/.countmyhours/data.json`. Atomic write via temp file + rename.
- On first load (empty data), `App.java` auto-imports bundled `sample-data.csv` from classpath resources.
- Splash screen with eye-blinking logo animation (4s minimum), data loads in background thread.
- Year filters in Dashboard and Data views are dynamic (derived from actual data years) and default to the current year or the most recent year in the data.
- `CalculationService` — Monthly/yearly aggregation, extra hours (worked - expected), proportional sold-hours attribution across projects, project summaries.

## Data Entry Features

- **New Spreadsheet**: creates a template CSV and opens it in the default spreadsheet app (Numbers/Excel)
- **Export & Edit**: exports all entries to CSV for external editing
- **Reimport Last**: quick reimport of the last created/exported spreadsheet
- **Import History**: table of all imported files with Export and Delete actions per file
  - Delete removes all entries tagged with that file's sourceFile and persists the change
- **Erase All Data**: clears all entries, hour sellings, and import history with confirmation dialog. Calls `WorkPeriodTracker.clearAll()`.
- **Toast notifications** (`Toast` util): floating top-right feedback messages (success/error/warning) with fade-in/out animation, replacing inline status labels.
- **Month Balance view** (`ExtraHoursView`): monthly cards with worked/expected/extra/accumulated hours. Editable vacation days, holiday count, and holiday observation per month. Custom holidays adjust expected hours. All persisted to data.json.
- Each imported entry is tagged with `sourceFile` for traceability

## Build & Run

```bash
mvn clean compile    # compile
mvn test             # run unit tests (JUnit 5 + Mockito)
mvn javafx:run       # launch the app
./package-macos.sh   # build macOS .dmg installer
```

## Dark Theme

CSS at `src/main/resources/com/countmyh/dark-theme.css`. Colors: bg `#0f1117`, card `#1a1d27`, border `#2a2d3a`, text `#e4e4e7`, accent `#6366f1`.

## Packaging

- `package-macos.sh` builds a `.dmg` installer via `jpackage` (JDK 23)
- Bundles JDK runtime + JavaFX + all dependencies (~52MB)
- Output: `target/installer/CountMyHours-1.0.0.dmg`
- Logo: smiling clock with transparent background (`logo.svg` / `logo.png` / `logo-blink.png` / `CountMyHours.icns`)

## Conventions

- Java 23, Maven 3.8+
- Group: `com.countmyh`
- JUnit 5 + Mockito for tests
- No Spring Boot, no FXML, no module-info.java
- Programmatic JavaFX UI construction
- Chart colors applied post-render via `Platform.runLater()` node lookup
- i18n via `I18n` utility + `ResourceBundle` at `resources/com/countmyh/i18n/messages[_locale].properties` (en default + pt_BR)
- Every code change must update unit tests, README.md, and CLAUDE.md if affected
