# CountMyHours — Project Context

## Architecture

Pure Java desktop app (no Spring Boot). Three-layer structure:

- **model/** — Records and POJOs: `WorkHourItem`, `WorkHourSelling`, `MonthSelling`, `WorkPeriodTracker`, `ImportRecord`, `VacationEntry`, `MonthNote`
- **service/** — Business logic: import, persistence, calculations, business day calendar
- **view/** — JavaFX UI (programmatic, no FXML): Dashboard, Timeline, ExtraHours (Month Balance), ExtraBalance (Extras), HourSelling, DataEntry, Settings
- **util/** — Chart styling, color palette, month names, i18n, toast notifications

No `module-info.java` — runs on classpath. The `javafx-maven-plugin` handles `--add-modules`.

## Model Classes

- `WorkHourItem` — Java record. One row from CSV: date (LocalDate), client, project, item, hours, sourceFile. Convenience constructor without sourceFile. `withSourceFile()` for immutable copy. equals/hashCode excludes sourceFile.
- `WorkHourSelling` — Java record: year, hoursSold, vacationDaysSold, note. Derived `vacationHoursSold()`.
- `MonthSelling` — Java record: year, month, hoursSold. Stores hours sold per calendar month.
- `ImportRecord` — Java record: fileName, filePath, importDate, entriesImported. equals/hashCode on filePath + importDate only.
- `VacationEntry` — Java record: year, month, days (double, supports half-day with 0.5)
- `MonthNote` — Java record: year, month, holidays (double, supports half-day), observation (holiday names/notes)
- `WorkPeriodTracker` — Mutable class (not a record): entries + sellings + importHistory + vacationDays + monthNotes + monthSellings + hiddenProjects + metadata. `getVisibleEntries()` filters out hidden projects (used by CalculationService). `@JsonIgnore` on derived getters. `setYearlySelling(year, hoursSold, vacDays, note)` and `setMonthSelling(year, month, hours)` are upsert helpers (remove-then-add).

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
- `CsvImportService` — Parses CSV (`;` delimiter only, validates format on import) and XLSX. Project names are lowercased on import. Format validation with descriptive error messages (wrong delimiter, missing columns, wrong headers).
- `JsonPersistenceService` — Jackson ObjectMapper with JavaTimeModule. Stores at `~/.countmyhours/data.json` (path from `AppDirs.DATA_DIR`). Atomic write via temp file + rename (falls back to regular rename on sandbox FileSystemException).
- `AppDirs` utility (`util/`) — resolves `~/.countmyhours` using `System.getenv("HOME")` with fallback to `System.getProperty("user.home")`. GraalVM native images call `getpwuid()` for `user.home` which returns the real home directory; the App Store sandbox blocks writes there. `HOME` env var is correctly set to the container path by macOS.
- `HolidayCalendar` / `HolidayCalendarLoader` / `HolidayCalendarFactory` — data-driven holiday system. Each locale has a `resources/com/countmyh/holidays/holidays_<locale>.properties` file with `YYYY-MM-DD=Name` entries for 2017–2027. `BusinessDayService` calls `HolidayCalendarFactory.forLocale(I18n.getLocale().toLanguageTag())` dynamically so switching language in Settings immediately updates the holiday calendar.
- On first load (empty data), `App.java` auto-imports bundled `sample-data.csv` from classpath resources.
- Splash screen with eye-blinking logo animation (4s minimum), data loads in background thread.
- Year filters in Dashboard and Data views are dynamic (derived from actual data years) and default to the current year or the most recent year in the data.
- `CalculationService` — Monthly/yearly aggregation, extra hours (worked - expected with vacation/holiday adjustments), proportional sold-hours attribution across projects, project summaries. Uses `getVisibleEntries()` to respect hidden projects. Key result types: `MonthlyBalance`, `YearlyBalance` (worked, gross, sold, vacationSold, net, note), `ProjectExtra` (totalHours, grossExtra, sold, net, pct, yearlyBreakdown).

## Data Entry Features

- **New Spreadsheet**: creates a template CSV and opens it in the default spreadsheet app (Numbers/Excel)
- **Export & Edit**: exports all entries to CSV for external editing
- **Reimport Last**: quick reimport of the last created/exported spreadsheet
- **Import History**: table of all imported files with Export and Delete actions per file
  - Delete removes all entries tagged with that file's sourceFile and persists the change
- **Erase All Data**: clears all entries, hour sellings, and import history with confirmation dialog. Calls `WorkPeriodTracker.clearAll()`.
- **Toast notifications** (`Toast` util): floating top-right feedback messages (success/error/warning) with fade-in/out animation, replacing inline status labels.
- **Month Balance view** (`ExtraHoursView`): monthly cards with worked/expected/extra/accumulated hours. Editable vacation days and holiday count (double spinners with 0.5 step for half-days), holiday observation text field (auto-filled from Brazilian calendar). Custom holidays adjust expected hours. All persisted to data.json.
- **Extras view** (`ExtraBalanceView`): two-section analytical view. Section 1: yearly balance `BarChart<String,Number>` with three series — gross extras (indigo), hours sold (orange, negative), net balance (green/red per bar, colored via post-render). Section 2: per-project horizontal `BarChart<Number,String>` (gross vs sold), a `StackedBarChart<String,Number>` with per-project series stacked per year, and a `TableView` with worked/gross/sold/net columns (cells color-coded). All data from `CalculationService.getYearlyBalance()` and `getExtraPerProject()`.
- **Hour Selling view** (`HourSellingView`): toggle between Monthly and Yearly mode. Monthly mode shows one card per month with a spinner for hours sold that month (stored as `MonthSelling`). Yearly mode shows one card per year with spinners for hoursSold and vacationDaysSold, plus a note field (stored as `WorkHourSelling`). Both modes have a year filter row. Cards display worked hours as context.
- **Settings view** (`SettingsView`): language selector (English/pt-BR, rebuilds UI on apply), project management (toggle visibility per project — hidden projects excluded from all charts/calculations), and uninstall (deletes `~/.countmyhours/`, closes app). Pinned at sidebar bottom.
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

- `package-appstore-native.sh` builds a signed `.pkg` for App Store submission via GraalVM native image (Gluon GluonFX, JDK 21)
  - Output: `target/appstore/CountMyHours-3.2.6.pkg` (~34MB)
  - Signed with `3rd Party Mac Developer Application` + `Installer` certificates
- `package-macos.sh` builds a `.dmg` installer via `jpackage` (JDK 23, Zulu)
  - Output: `target/installer/CountMyHours-3.2.6.dmg` (~52MB)
- Logo: smiling clock with transparent background (`logo.svg` / `logo.png` / `logo-blink.png` / `CountMyHours.icns`)

## Conventions

- Java 23, Maven 3.8+
- Group: `com.countmyh`
- JUnit 5 + Mockito for tests
- No Spring Boot, no FXML, no module-info.java
- Programmatic JavaFX UI construction
- Chart colors applied post-render via `Platform.runLater()` node lookup
- i18n via `I18n` utility + `ResourceBundle` at `resources/com/countmyh/i18n/messages[_locale].properties` (en default + pt_BR + it_IT + ja_JP + zh_CN + hi_IN; en_GB/en_CA/en_US fall back to default)
- Holiday calendars at `resources/com/countmyh/holidays/holidays_<locale>.properties` — explicit `YYYY-MM-DD=Name` entries; add a new country with one file + one switch line in `HolidayCalendarFactory`
- Date formats at `resources/com/countmyh/calendar/calendar_<locale>.properties` — key `date.format` (e.g. `MM/dd/yyyy` for en-US); loaded by `CalendarConfig` using the same hyphen→underscore convention. Controls CSV import parsing, CSV export formatting, and date column display. Timestamp columns (import history) stay fixed at `dd/MM/yyyy HH:mm`.
- Every code change must update unit tests, README.md, and CLAUDE.md if affected

## Before Every Package or Release

**Always do these steps before running any packaging script or publishing a test build:**

1. Bump version in `pom.xml`, `package-appstore-native.sh`, and `package-macos.sh`
2. Add a `[X.Y.Z] - YYYY-MM-DD` entry to `CHANGELOG.md` describing all changes since the last release
3. Update `README.md` if any features, setup steps, or packaging info changed
4. Update `CLAUDE.md` if any architecture, models, services, views, or conventions changed

Never run `./package-appstore-native.sh` or `./package-macos.sh` without completing all four steps first.
