# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

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
