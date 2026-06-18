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
01/06/2026;Opus;Medscript;MED-238, PR;8
02/06/2026;Opus;Medscript;MED-238, MED-251, PRs;8
03/06/2026;Opus;OOF;OOF-412, code review;4
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
- Import legacy spreadsheet data (pre-aggregated monthly format, 2017-2025)
- Auto-calculate expected monthly hours based on Brazilian business days
- Dashboard with stats cards and stacked bar chart (filterable by year range)
- Project timeline (Gantt chart) and yearly totals
- Extra hours tracking: gross extras, hours sold, net balance
- Per-project extra hours breakdown with proportional sold-hours attribution
- Hour selling records management
- Dark theme UI
