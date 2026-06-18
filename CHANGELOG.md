# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added
- Project scaffolding: Maven, JavaFX 23, directory structure
- Model classes: WorkHourItem, WorkHourSelling, WorkPeriodTracker
- BusinessDayService with Brazilian national holidays and São Paulo state holiday
- CsvImportService for semicolon-delimited CSV and XLSX import
- JsonPersistenceService for data persistence at ~/.countmyhours/data.json
- CalculationService for monthly/yearly aggregation and extra hours calculation
- Unit tests with JUnit 5 and Mockito
- Dark theme CSS
- Dashboard view with stats cards and stacked bar chart
- Timeline view with yearly totals and project summary table
- Data entry view with CSV/XLSX import and entries table
