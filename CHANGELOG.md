# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [3.2.4] - 2026-06-26

### Fixed
- **Welcome screen restored to first-time-only** — reverted 3.2.3 change; welcome/language picker now shows only when no locale is saved (first install), then goes directly to main app on all subsequent launches
- Lambda parameters renamed from `_` to named variables (`e`, `ev`, `obs`) so code compiles cleanly without `--enable-preview` and is compatible with GraalVM native image

## [3.2.3] - 2026-06-26

### Added
- **Welcome screen always shown on launch** — the language picker now appears on every startup (not just first run); the previously-saved locale is pre-selected and Continue is immediately enabled, so returning users just press one button
- `--enable-preview` added to maven-compiler-plugin and maven-surefire-plugin so unnamed lambda parameters (`_`) compile and test cleanly with GraalVM JDK 21

## [3.2.2] - 2026-06-26

### Added
- **Locale-aware date format** — CSV import, export, and date display now use the format defined for the active locale:
  - `dd/MM/yyyy` — pt-BR, en-GB, hi-IN, it-IT, es-ES
  - `MM/dd/yyyy` — en-US
  - `yyyy-MM-dd` — en-CA (ISO standard)
  - `yyyy/MM/dd` — ja-JP, zh-CN
- `CalendarConfig` service reads `date.format` from `calendar_<locale>.properties` at classpath; adding a new format requires only one file
- 9 new tests in `CalendarConfigTest` (one per supported locale)

### Changed
- Total tests: 115 → 124

## [3.2.1] - 2026-06-26

### Added
- **Spanish (ES)** locale — full UI translation (`messages_es_ES.properties`) and Spain public holiday calendar (`holidays_es_ES.properties`) with 10 national holidays including Epiphany, Good Friday, and Fiesta Nacional, pre-populated 2017–2027
- `Español (ES)` button added to welcome screen and Settings language picker

## [3.2.0] - 2026-06-25

### Added
- **8-language support** — Chinese (CN), English UK, English US, English Canada, Hindi (India), Japanese, Italian join the existing English and Português (BR)
- **Data-driven holiday calendars** — each locale has a `holidays_<locale>.properties` file with explicit `YYYY-MM-DD=Name` entries for 2017–2027; files accumulate over time (past years never removed); adding a new country requires only one file + one switch-case in `HolidayCalendarFactory`
- `HolidayCalendar` interface, `HolidayCalendarLoader` (classpath reader, cached), `HolidayCalendarFactory` (locale → calendar)
- Full UI translations for Italian, Japanese, Simplified Chinese, and Hindi
- 19 new tests in `HolidayCalendarTest` (at least one verified date per country)
- Welcome screen and Settings language picker now show all 8 locales in a `FlowPane` grid

### Changed
- `BusinessDayService` no longer contains hardcoded Brazilian logic; delegates to `HolidayCalendarFactory.forLocale(I18n.getLocale().toLanguageTag())` dynamically — switching locale in Settings immediately changes which holidays appear in Month Balance cards
- `BusinessDayServiceTest` now sets `pt-BR` locale in `@BeforeEach` to keep existing Brazilian assertions valid
- Total tests: 94 → 112

## [3.1.1] - 2026-06-25

### Fixed
- **Save error on delete/import** — "Operation not permitted" when deleting an import file or erasing all data. Root cause: `StandardCopyOption.ATOMIC_MOVE` in `JsonPersistenceService` uses a `renameat()` variant blocked by the macOS App Store sandbox. Fix: try atomic move first and fall back to a regular `REPLACE_EXISTING` move on `FileSystemException`, which uses plain `rename()` and is permitted in the sandbox.

## [3.1.0] - 2026-06-25

### Added
- **Extras view** — analytical breakdown of extra hours with two charts and a summary table (moved from 3.0.2 scope)

### Changed
- Version bump to 3.1.0 for App Store submission

## [3.0.2] - 2026-06-25

### Added
- **Extras view** — analytical breakdown of extra hours with two charts and a table
  - Yearly Balance chart: grouped bars for gross extras (indigo), hours sold (orange, negative), and net balance (green/red per bar)
  - Per-Project horizontal bar chart: gross extras vs sold hours attributed proportionally per project
  - Per-Project stacked bar chart: extra hours broken down by project per year
  - Summary table: worked, gross extras, sold, and net per project with color-coded cells
- **Hour Selling view** — new sidebar section to record sold hours per month or per year
  - Monthly mode: one card per month with a "Sold" spinner; stored as `MonthSelling` (year + month + hoursSold)
  - Yearly mode: one card per year with spinners for hours sold and vacation days sold, plus a note field; stored in `WorkHourSelling`
  - Both modes show worked hours per period as context and share the same year filter buttons
- `MonthSelling` model record (year, month, hoursSold) for month-level sold-hours tracking
- `WorkPeriodTracker.setYearlySelling()` — upsert helper replacing append-only `addHourSelling` for edits
- `WorkPeriodTracker.getYearlySelling(year)` — lookup helper for yearly selling
- `WorkPeriodTracker.setMonthSelling()` / `getMonthSelling()` — upsert and lookup for monthly sold hours
- `WorkPeriodTracker.clearAll()` now also clears `monthSellings`

## [2.2.0] - 2026-06-23

### Added
- **Project Management** in Settings — toggle visibility per project with checkbox; hidden projects excluded from Dashboard, Timeline, and Month Balance charts/calculations
- **Half-day support** for holidays and vacation — spinners use 0.5 step increments (e.g., 1.5 holidays = 1 full + 1 half-day = 12h deducted)
- **Vacation tooltip** — asterisk on Vacation* label with hover explanation to enter only working days
- Dark-themed dialog styling for all Alert popups

### Changed
- `VacationEntry.days` and `MonthNote.holidays` changed from `int` to `double`
- `CalculationService` uses `getVisibleEntries()` to respect hidden projects
- `WorkPeriodTracker.getVisibleEntries()` annotated with `@JsonIgnore`
- Project names lowercased on CSV import

## [2.1.0] - 2026-06-20

### Added
- **Settings view** with language selector and uninstall option, pinned at sidebar bottom
  - Language: switch between English / Português (BR) with instant UI rebuild
  - Uninstall: deletes all data at `~/.countmyhours/`, shows confirmation, closes app
  - Guides user to drag app to Trash after data removal

## [2.0.0] - 2026-06-19

### Added
- **Splash screen** with blinking clock eyes animation (4s minimum)
- **Welcome screen** with language picker on first launch, handcrafted indie software message
- **Internationalization (i18n)** — English and Portuguese (pt-BR), persisted at `~/.countmyhours/locale`
- **Month Balance view** — monthly cards showing worked/expected/extra/accumulated hours
  - Editable vacation days per month (adjusts expected hours)
  - Editable holiday count and observation per month (auto-filled from Brazilian calendar)
  - Custom holidays adjust expected hours calculation
- **Gantt chart** (Project's Timeline) on Timeline view — horizontal bars sorted chronologically
- **Toast notifications** — floating top-right messages with icons (success/error/warning)
- **Dark-themed dialogs** — Alert popups styled to match the app
- **Erase All Data** button with confirmation dialog
- **Sample data auto-load** on first launch (Disney-themed example entries)
- **Dynamic year filters** — Dashboard, Data, and Month Balance views derive years from actual data
- **CSV format validation** — clear error messages for wrong delimiter, missing columns, wrong headers
- New models: `VacationEntry`, `MonthNote`
- `BusinessDayService.getNamedHolidays()` and `getHolidaysInMonth()` with holiday names

### Changed
- **Models converted to Java records**: `WorkHourItem`, `WorkHourSelling`, `ImportRecord`
  - Accessor methods use record style (`date()` instead of `getDate()`)
  - `WorkHourItem.withSourceFile()` for immutable copy
- **Year filters** now default to current year (or most recent year in data)
- **Data view** stays on current view after import/erase (no longer jumps to Dashboard)
- **Status labels** replaced with Toast notifications across Data view
- `CalculationService.MonthlyBalance` now includes `vacationDays` field
- `WorkPeriodTracker` now includes `vacationDays` and `monthNotes` lists
- HSL colors in TimelineView converted to hex (fixes JavaFX CSS warning)

## [1.0.0] - 2026-06-18

### Added
- Project scaffolding: Maven, JavaFX 23, directory structure
- Model classes: WorkHourItem, WorkHourSelling, WorkPeriodTracker, ImportRecord
- BusinessDayService with Brazilian national holidays and São Paulo state holiday
- CsvImportService for semicolon-delimited CSV and XLSX import
- JsonPersistenceService for data persistence at ~/.countmyhours/data.json
- CalculationService for monthly/yearly aggregation and extra hours calculation
- Unit tests with JUnit 5 and Mockito
- Dark theme CSS
- Dashboard view with stats cards and stacked bar chart
- Timeline view with yearly totals and project summary table
- Data entry view with CSV/XLSX import, spreadsheet mode, import history
- Import history with per-file Export and Delete
- Smiling clock logo with transparent background
- macOS .dmg installer via jpackage
