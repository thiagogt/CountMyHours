# CountMyHours — Project Context

## Architecture

Pure Java desktop app (no Spring Boot). Three-layer structure:

- **model/** — POJOs for persistence: `WorkHourItem`, `WorkHourSelling`, `WorkPeriodTracker`
- **service/** — Business logic: import, persistence, calculations, business day calendar
- **view/** — JavaFX UI (programmatic, no FXML): Dashboard, Timeline, ExtraHours, DataEntry
- **util/** — Chart styling, color palette, month names

No `module-info.java` — runs on classpath. The `javafx-maven-plugin` handles `--add-modules`.

## Model Classes

- `WorkHourItem` — One row from CSV: date (LocalDate), client, project, item, hours
- `WorkHourSelling` — Year-level record: year, hoursSold, vacationDaysSold, note
- `WorkPeriodTracker` — Root container: List of entries + sellings + metadata

All monthly/yearly aggregations are computed by `CalculationService`, not stored.

## CSV Format

Semicolon-delimited. Date as `dd/MM/yyyy`:
```
Data;Cliente;Projeto;Item;Hs
01/06/2026;Opus;Medscript;MED-238, PR;8
```

## Key Services

- `BusinessDayService` — Brazilian national holidays (fixed + Easter-based moveable) + São Paulo state holiday (Jul 9). Calculates working days per month, expected hours = business days × 8.
- `CsvImportService` — Parses CSV (`;` delimiter) and XLSX with the same column structure.
- `LegacyXlsxImportService` — Parses the old monthly-aggregated spreadsheet (2017-2025). Converts to `WorkHourItem` entries dated to 1st of month with item="legacy-import", client="Opus".
- `JsonPersistenceService` — Jackson ObjectMapper with JavaTimeModule. Stores at `~/.countmyhours/data.json`. Atomic write via temp file + rename.
- `CalculationService` — Monthly/yearly aggregation, extra hours (worked - expected), proportional sold-hours attribution across projects, project summaries.

## Build & Run

```bash
mvn clean compile    # compile
mvn test             # run unit tests (JUnit 5 + Mockito)
mvn javafx:run       # launch the app
```

## Dark Theme

CSS at `src/main/resources/com/countmyh/dark-theme.css`. Colors: bg `#0f1117`, card `#1a1d27`, border `#2a2d3a`, text `#e4e4e7`, accent `#6366f1`.

## Conventions

- Java 23, Maven 3.8+
- Group: `com.countmyh`
- JUnit 5 + Mockito for tests
- No Spring Boot, no FXML, no module-info.java
- Programmatic JavaFX UI construction
- Chart colors applied post-render via `Platform.runLater()` node lookup
