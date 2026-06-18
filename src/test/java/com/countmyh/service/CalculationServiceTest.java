package com.countmyh.service;

import com.countmyh.model.WorkHourItem;
import com.countmyh.model.WorkHourSelling;
import com.countmyh.model.WorkPeriodTracker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculationServiceTest {

    @Mock
    private BusinessDayService businessDayService;

    private CalculationService service;

    @BeforeEach
    void setUp() {
        service = new CalculationService(businessDayService);
    }

    private WorkPeriodTracker createSampleData() {
        var tracker = new WorkPeriodTracker();
        // June 2026: 3 entries across 2 projects
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "task1", 8));
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 6, 2), "Opus", "Medscript", "task2", 8));
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 6, 3), "Opus", "OOF", "task3", 8));
        // May 2026: 1 entry
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 5, 1), "Opus", "OOF", "task4", 8));
        return tracker;
    }

    @Test
    void shouldCalculateTotalHours() {
        var data = createSampleData();
        assertEquals(32.0, service.getTotalHours(data));
    }

    @Test
    void shouldCountProjectsExcludingAdmin() {
        var tracker = new WorkPeriodTracker();
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "Medscript", "t", 8));
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "OOF", "t", 8));
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 6, 1), "Opus", "admin", "t", 2));

        assertEquals(2, service.getTotalProjects(tracker));
    }

    @Test
    void shouldAggregateMonthlyHoursByProject() {
        var data = createSampleData();

        Map<String, Map<YearMonth, Double>> result = service.getMonthlyHoursByProject(data, 2026, 2026);

        assertEquals(16.0, result.get("Medscript").get(YearMonth.of(2026, 6)));
        assertEquals(8.0, result.get("OOF").get(YearMonth.of(2026, 6)));
        assertEquals(8.0, result.get("OOF").get(YearMonth.of(2026, 5)));
        assertNull(result.get("Medscript").get(YearMonth.of(2026, 5)));
    }

    @Test
    void shouldFilterByYearRange() {
        var tracker = new WorkPeriodTracker();
        tracker.addEntry(new WorkHourItem(LocalDate.of(2025, 1, 1), "A", "P1", "t", 8));
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 1, 1), "A", "P2", "t", 8));

        var result = service.getMonthlyHoursByProject(tracker, 2026, 2026);

        assertNull(result.get("P1"));
        assertNotNull(result.get("P2"));
    }

    @Test
    void shouldCalculateMonthlyTotalWorked() {
        var data = createSampleData();
        Map<YearMonth, Double> result = service.getMonthlyTotalWorked(data);

        assertEquals(24.0, result.get(YearMonth.of(2026, 6)));
        assertEquals(8.0, result.get(YearMonth.of(2026, 5)));
    }

    @Test
    void shouldCalculateYearlyTotals() {
        var data = createSampleData();
        Map<Integer, Double> result = service.getYearlyTotals(data);

        assertEquals(32.0, result.get(2026));
    }

    @Test
    void shouldCalculateMonthlyBalance() {
        when(businessDayService.getExpectedHours(2026, 6)).thenReturn(168.0);
        when(businessDayService.getExpectedHours(2026, 5)).thenReturn(160.0);

        var data = createSampleData();
        var result = service.getMonthlyBalance(data);

        var june = result.get(YearMonth.of(2026, 6));
        assertEquals(24.0, june.worked());
        assertEquals(168.0, june.expected());
        assertEquals(-144.0, june.extra());

        var may = result.get(YearMonth.of(2026, 5));
        assertEquals(8.0, may.worked());
        assertEquals(160.0, may.expected());
        assertEquals(-152.0, may.extra());
    }

    @Test
    void shouldCalculateYearlyBalance() {
        when(businessDayService.getExpectedHours(anyInt(), anyInt())).thenReturn(160.0);

        var tracker = new WorkPeriodTracker();
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 1, 1), "A", "P", "t", 180));
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 2, 1), "A", "P", "t", 170));
        tracker.addHourSelling(new WorkHourSelling(2026, 20, 0, "sold 20h"));

        var result = service.getYearlyBalance(tracker);

        var y2026 = result.get(2026);
        assertEquals(350.0, y2026.worked());
        assertEquals(30.0, y2026.gross());  // (180-160) + (170-160) = 30
        assertEquals(20.0, y2026.sold());
        assertEquals(10.0, y2026.net());    // 30 - 20
        assertEquals("sold 20h", y2026.note());
    }

    @Test
    void shouldCalculateMonthlyAverage() {
        var data = createSampleData();
        // 2 months: 24h + 8h = 32h, avg = 16h
        assertEquals(16.0, service.getMonthlyAverage(data));
    }

    @Test
    void shouldReturnZeroAverageForEmptyData() {
        assertEquals(0.0, service.getMonthlyAverage(new WorkPeriodTracker()));
    }

    @Test
    void shouldCalculateGrossExtra() {
        when(businessDayService.getExpectedHours(2026, 1)).thenReturn(160.0);

        var tracker = new WorkPeriodTracker();
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 1, 1), "A", "P", "t", 180));

        assertEquals(20.0, service.getTotalGrossExtra(tracker));
    }

    @Test
    void shouldCalculateTotalSold() {
        var tracker = new WorkPeriodTracker();
        tracker.addHourSelling(new WorkHourSelling(2024, 70, 0, "note"));
        tracker.addHourSelling(new WorkHourSelling(2023, 12, 0, "note"));

        assertEquals(82.0, service.getTotalSold(tracker));
    }

    @Test
    void shouldCalculateNetBalance() {
        when(businessDayService.getExpectedHours(2026, 1)).thenReturn(160.0);

        var tracker = new WorkPeriodTracker();
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 1, 1), "A", "P", "t", 200));
        tracker.addHourSelling(new WorkHourSelling(2026, 15, 0, "sold"));

        assertEquals(25.0, service.getNetBalance(tracker)); // (200-160) - 15 = 25
    }

    @Test
    void shouldCalculateProjectSummaries() {
        var data = createSampleData();
        var summaries = service.getProjectSummaries(data);

        assertEquals(2, summaries.size());
        // Both have 16h, order among equal totals is not guaranteed
        var byName = summaries.stream()
                .collect(Collectors.toMap(CalculationService.ProjectSummary::project, s -> s));

        var oof = byName.get("OOF");
        assertEquals(16.0, oof.totalHours());
        assertEquals(2, oof.activeMonths());
        assertEquals("Opus", oof.client());

        var med = byName.get("Medscript");
        assertEquals(16.0, med.totalHours());
        assertEquals(1, med.activeMonths());
    }

    @Test
    void shouldCalculateProjectDateRanges() {
        var data = createSampleData();
        var ranges = service.getProjectDateRanges(data);

        assertArrayEquals(
                new YearMonth[]{YearMonth.of(2026, 6), YearMonth.of(2026, 6)},
                ranges.get("Medscript")
        );
        assertArrayEquals(
                new YearMonth[]{YearMonth.of(2026, 5), YearMonth.of(2026, 6)},
                ranges.get("OOF")
        );
    }

    @Test
    void shouldDistributeSoldHoursProportionally() {
        when(businessDayService.getExpectedHours(anyInt(), anyInt())).thenReturn(160.0);

        var tracker = new WorkPeriodTracker();
        // Project A: 180h in Jan (extra=20), Project B: 170h in Jan (extra=10)
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 1, 1), "X", "A", "t", 180));
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 1, 1), "X", "B", "t", 170));
        // Sell 15h in 2026
        tracker.addHourSelling(new WorkHourSelling(2026, 15, 0, "sold"));

        var extras = service.getExtraPerProject(tracker);

        // Total positive extra: A proportional = some value based on (180/350)*30-extra,
        // B proportional = (170/350)*30-extra
        // Actually: monthly total = 350, expected = 160, extra = 190
        // A proportion = 180/350 = 0.514, attributed extra = 190*0.514 = 97.7
        // B proportion = 170/350 = 0.486, attributed extra = 190*0.486 = 92.3
        // Both positive, so sold 15h distributed: A gets 15*(97.7/190)=7.71, B gets 15*(92.3/190)=7.29

        var extraA = extras.get("A");
        var extraB = extras.get("B");

        assertTrue(extraA.grossExtra() > 0);
        assertTrue(extraB.grossExtra() > 0);
        assertTrue(extraA.sold() > 0);
        assertTrue(extraB.sold() > 0);
        assertEquals(15.0, extraA.sold() + extraB.sold(), 0.01);
        assertEquals(extraA.grossExtra() - extraA.sold(), extraA.net(), 0.01);
    }

    @Test
    void shouldNotDistributeSoldToProjectsWithNegativeExtra() {
        when(businessDayService.getExpectedHours(anyInt(), anyInt())).thenReturn(160.0);

        var tracker = new WorkPeriodTracker();
        // Project A: 180h (extra positive), Project B: 10h (extra negative after proportion)
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 1, 1), "X", "A", "t", 180));
        tracker.addEntry(new WorkHourItem(LocalDate.of(2026, 2, 1), "X", "B", "t", 10));
        tracker.addHourSelling(new WorkHourSelling(2026, 5, 0, "sold"));

        var extras = service.getExtraPerProject(tracker);

        // B has negative extra (10h worked vs 160h expected = -150h proportional)
        assertEquals(0.0, extras.get("B").sold(), 0.01);
        // All sold hours go to A
        assertEquals(5.0, extras.get("A").sold(), 0.01);
    }
}
