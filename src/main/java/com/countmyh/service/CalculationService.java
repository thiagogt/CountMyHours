package com.countmyh.service;

import com.countmyh.model.WorkHourItem;
import com.countmyh.model.WorkHourSelling;
import com.countmyh.model.WorkPeriodTracker;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class CalculationService {

    private final BusinessDayService businessDayService;

    public CalculationService(BusinessDayService businessDayService) {
        this.businessDayService = businessDayService;
    }

    // -- Aggregation --

    public Map<String, Map<YearMonth, Double>> getMonthlyHoursByProject(WorkPeriodTracker data, int startYear, int endYear) {
        return data.getEntries().stream()
                .filter(e -> e.year() >= startYear && e.year() <= endYear)
                .collect(Collectors.groupingBy(
                        WorkHourItem::project,
                        Collectors.groupingBy(
                                e -> YearMonth.of(e.year(), e.month()),
                                TreeMap::new,
                                Collectors.summingDouble(WorkHourItem::hours)
                        )
                ));
    }

    public Map<YearMonth, Double> getMonthlyTotalWorked(WorkPeriodTracker data) {
        return data.getEntries().stream()
                .collect(Collectors.groupingBy(
                        e -> YearMonth.of(e.year(), e.month()),
                        TreeMap::new,
                        Collectors.summingDouble(WorkHourItem::hours)
                ));
    }

    public Map<Integer, Double> getYearlyTotals(WorkPeriodTracker data) {
        return data.getEntries().stream()
                .collect(Collectors.groupingBy(
                        WorkHourItem::year,
                        TreeMap::new,
                        Collectors.summingDouble(WorkHourItem::hours)
                ));
    }

    // -- Extra Hours --

    public Map<YearMonth, MonthlyBalance> getMonthlyBalance(WorkPeriodTracker data) {
        Map<YearMonth, Double> worked = getMonthlyTotalWorked(data);
        Map<YearMonth, MonthlyBalance> result = new TreeMap<>();

        for (var entry : worked.entrySet()) {
            YearMonth ym = entry.getKey();
            double w = entry.getValue();
            double expected = businessDayService.getExpectedHours(ym.getYear(), ym.getMonthValue());
            result.put(ym, new MonthlyBalance(w, expected, w - expected));
        }
        return result;
    }

    public Map<Integer, YearlyBalance> getYearlyBalance(WorkPeriodTracker data) {
        Map<YearMonth, MonthlyBalance> monthly = getMonthlyBalance(data);
        Map<Integer, WorkHourSelling> sellingByYear = data.getHourSellings().stream()
                .collect(Collectors.toMap(WorkHourSelling::year, s -> s, (a, b) -> a));

        Map<Integer, YearlyBalance> result = new TreeMap<>();

        Map<Integer, Double> yearlyGross = new TreeMap<>();
        Map<Integer, Double> yearlyWorked = new TreeMap<>();

        for (var entry : monthly.entrySet()) {
            int year = entry.getKey().getYear();
            yearlyGross.merge(year, entry.getValue().extra(), Double::sum);
            yearlyWorked.merge(year, entry.getValue().worked(), Double::sum);
        }

        for (var entry : yearlyGross.entrySet()) {
            int year = entry.getKey();
            double gross = entry.getValue();
            WorkHourSelling selling = sellingByYear.get(year);
            double sold = selling != null ? selling.hoursSold() : 0;
            double vacationSold = selling != null ? selling.vacationDaysSold() : 0;
            String note = selling != null ? selling.note() : null;

            result.put(year, new YearlyBalance(
                    yearlyWorked.getOrDefault(year, 0.0),
                    gross, sold, vacationSold, gross - sold, note
            ));
        }
        return result;
    }

    public Map<String, ProjectExtra> getExtraPerProject(WorkPeriodTracker data) {
        Map<YearMonth, MonthlyBalance> monthlyBalance = getMonthlyBalance(data);
        Map<YearMonth, Double> monthlyTotalWorked = getMonthlyTotalWorked(data);
        Map<String, Map<YearMonth, Double>> byProject = getMonthlyHoursByProject(
                data, Integer.MIN_VALUE, Integer.MAX_VALUE);

        Map<Integer, WorkHourSelling> sellingByYear = data.getHourSellings().stream()
                .collect(Collectors.toMap(WorkHourSelling::year, s -> s, (a, b) -> a));

        Map<String, Double> projectGrossExtra = new LinkedHashMap<>();
        Map<String, Double> projectTotalHours = new LinkedHashMap<>();
        Map<String, Map<Integer, Double>> projectYearlyExtra = new LinkedHashMap<>();

        for (var projEntry : byProject.entrySet()) {
            String project = projEntry.getKey();
            double totalHours = 0;
            double grossExtra = 0;
            Map<Integer, Double> yearlyExtra = new TreeMap<>();

            for (var monthEntry : projEntry.getValue().entrySet()) {
                YearMonth ym = monthEntry.getKey();
                double projHours = monthEntry.getValue();
                totalHours += projHours;

                MonthlyBalance balance = monthlyBalance.get(ym);
                double totalWorked = monthlyTotalWorked.getOrDefault(ym, 0.0);
                if (balance != null && totalWorked > 0) {
                    double proportion = projHours / totalWorked;
                    double attributed = balance.extra() * proportion;
                    grossExtra += attributed;
                    yearlyExtra.merge(ym.getYear(), attributed, Double::sum);
                }
            }

            projectGrossExtra.put(project, grossExtra);
            projectTotalHours.put(project, totalHours);
            projectYearlyExtra.put(project, yearlyExtra);
        }

        // Distribute sold hours proportionally across projects with positive extra in that year
        Map<String, Double> projectSold = new LinkedHashMap<>();
        for (String project : projectGrossExtra.keySet()) {
            projectSold.put(project, 0.0);
        }

        for (var sellingEntry : sellingByYear.entrySet()) {
            int year = sellingEntry.getKey();
            double yearSold = sellingEntry.getValue().hoursSold();
            if (yearSold <= 0) continue;

            double totalPositive = 0;
            for (var projEntry : projectYearlyExtra.entrySet()) {
                double ye = projEntry.getValue().getOrDefault(year, 0.0);
                if (ye > 0) totalPositive += ye;
            }

            if (totalPositive > 0) {
                for (var projEntry : projectYearlyExtra.entrySet()) {
                    double ye = projEntry.getValue().getOrDefault(year, 0.0);
                    if (ye > 0) {
                        double share = yearSold * (ye / totalPositive);
                        projectSold.merge(projEntry.getKey(), share, Double::sum);
                    }
                }
            }
        }

        Map<String, ProjectExtra> result = new LinkedHashMap<>();
        for (String project : projectGrossExtra.keySet()) {
            double total = projectTotalHours.get(project);
            double gross = projectGrossExtra.get(project);
            double sold = projectSold.getOrDefault(project, 0.0);
            double net = gross - sold;
            double pct = total > 0 ? (gross / total * 100) : 0;
            result.put(project, new ProjectExtra(total, gross, sold, net, pct, projectYearlyExtra.get(project)));
        }
        return result;
    }

    // -- Summaries --

    public List<ProjectSummary> getProjectSummaries(WorkPeriodTracker data) {
        return data.getEntries().stream()
                .collect(Collectors.groupingBy(WorkHourItem::project))
                .entrySet().stream()
                .map(e -> {
                    String project = e.getKey();
                    List<WorkHourItem> items = e.getValue();
                    String client = items.getFirst().client();
                    double totalHours = items.stream().mapToDouble(WorkHourItem::hours).sum();
                    YearMonth first = items.stream()
                            .map(i -> YearMonth.of(i.year(), i.month()))
                            .min(Comparator.naturalOrder()).orElse(null);
                    YearMonth last = items.stream()
                            .map(i -> YearMonth.of(i.year(), i.month()))
                            .max(Comparator.naturalOrder()).orElse(null);
                    long activeMonths = items.stream()
                            .map(i -> YearMonth.of(i.year(), i.month()))
                            .distinct().count();
                    return new ProjectSummary(project, client, first, last, (int) activeMonths, totalHours);
                })
                .sorted(Comparator.comparingDouble(ProjectSummary::totalHours).reversed())
                .toList();
    }

    public double getTotalHours(WorkPeriodTracker data) {
        return data.getEntries().stream().mapToDouble(WorkHourItem::hours).sum();
    }

    public int getTotalProjects(WorkPeriodTracker data) {
        return (int) data.getEntries().stream()
                .map(WorkHourItem::project)
                .filter(p -> !p.equalsIgnoreCase("admin"))
                .distinct().count();
    }

    public double getMonthlyAverage(WorkPeriodTracker data) {
        Map<YearMonth, Double> monthly = getMonthlyTotalWorked(data);
        if (monthly.isEmpty()) return 0;
        double total = monthly.values().stream().mapToDouble(Double::doubleValue).sum();
        return total / monthly.size();
    }

    public double getTotalGrossExtra(WorkPeriodTracker data) {
        return getMonthlyBalance(data).values().stream()
                .mapToDouble(MonthlyBalance::extra).sum();
    }

    public double getTotalSold(WorkPeriodTracker data) {
        return data.getHourSellings().stream()
                .mapToDouble(WorkHourSelling::hoursSold).sum();
    }

    public double getNetBalance(WorkPeriodTracker data) {
        return getTotalGrossExtra(data) - getTotalSold(data);
    }

    public Map<String, YearMonth[]> getProjectDateRanges(WorkPeriodTracker data) {
        return data.getEntries().stream()
                .collect(Collectors.groupingBy(WorkHourItem::project))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            YearMonth min = e.getValue().stream()
                                    .map(i -> YearMonth.of(i.year(), i.month()))
                                    .min(Comparator.naturalOrder()).orElse(null);
                            YearMonth max = e.getValue().stream()
                                    .map(i -> YearMonth.of(i.year(), i.month()))
                                    .max(Comparator.naturalOrder()).orElse(null);
                            return new YearMonth[]{min, max};
                        }
                ));
    }

    // -- Records --

    public record MonthlyBalance(double worked, double expected, double extra) {}

    public record YearlyBalance(double worked, double gross, double sold, double vacationSold, double net, String note) {}

    public record ProjectExtra(double totalHours, double grossExtra, double sold, double net, double pct,
                               Map<Integer, Double> yearlyBreakdown) {}

    public record ProjectSummary(String project, String client, YearMonth firstMonth, YearMonth lastMonth,
                                 int activeMonths, double totalHours) {}
}
