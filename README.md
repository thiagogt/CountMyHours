# CountMyHours

A JavaFX desktop application for tracking and visualizing worked hours per project over time.

## Prerequisites

- Java 23+ (Zulu JDK recommended)
- Maven 3.8+

## Build & Run

```bash
# Compile
mvn clean compile

# Run the application
mvn javafx:run

# Run tests
mvn test
```

## CSV Input Format

The application imports time entries from semicolon-delimited CSV files:

```
Data;Cliente;Projeto;Item;Hs
01/06/2026;Monsters SA;Wasowski;SCARE-42, door calibration;8
02/06/2026;Monsters SA;Wasowski;SCARE-42, SCARE-51, PRs;8
03/06/2026;Monsters SA;Boo Tracker;BOO-7, laugh energy research;4
04/06/2026;Pixar Inc;Ratatouille;RAT-12, recipe API;8
```

| Column   | Description              | Format       |
|----------|--------------------------|--------------|
| Data     | Date                     | dd/MM/yyyy   |
| Cliente  | Client name              | text         |
| Projeto  | Project name             | text         |
| Item     | Task description         | text         |
| Hs       | Hours worked             | number       |

XLSX files with the same column structure are also supported.

## Features

- Import work hours from CSV/XLSX files
- **Spreadsheet mode**: create a template CSV and open in Numbers/Excel, then reimport
- **Import history**: list of all imported files with Export and Delete per file
- **Dashboard**: stats cards (total hours, projects, monthly average, gross extras, hours sold, net balance) and stacked bar chart filterable by year
- **Timeline**: project Gantt chart, yearly totals bar chart, and project summary table
- **Month Balance**: monthly cards with worked/expected/extra/accumulated hours; editable vacation days and holidays (half-day support) with Brazilian calendar auto-fill
- **Extras**: analytical view with yearly balance chart (gross / sold / net), per-project horizontal bar chart, per-project stacked yearly chart, and summary table — all using proportional sold-hours attribution
- **Hour Selling**: record sold hours per month or per year with spinners; yearly mode also tracks vacation days sold and a note
- Auto-calculate expected monthly hours based on Brazilian business days (national + São Paulo state holiday)
- Internationalization (i18n) — English and Portuguese (pt-BR)
- Splash screen with animated blinking clock logo
- Sample data auto-loaded on first launch (Disney-themed example entries)
- Settings: language selector, project visibility toggle, and full uninstall
- Dark theme UI
- macOS App Store package via GraalVM native image (`./package-appstore-native.sh`)
