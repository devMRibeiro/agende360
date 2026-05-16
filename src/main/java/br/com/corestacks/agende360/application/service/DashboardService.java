package br.com.corestacks.agende360.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.application.dto.response.DashboardMetricsDTO;
import br.com.corestacks.agende360.application.dto.response.DashboardMetricsResponse;
import br.com.corestacks.agende360.application.dto.response.DateRange;
import br.com.corestacks.agende360.application.dto.response.TopServiceItem;
import br.com.corestacks.agende360.application.dto.response.Trend;
import br.com.corestacks.agende360.application.dto.response.TrendPoint;
import br.com.corestacks.agende360.application.exception.IllegalArgumentException;
import br.com.corestacks.agende360.application.model.Company;
import br.com.corestacks.agende360.application.repository.DashboardRepository;
import br.com.corestacks.agende360.application.subscription.service.FeatureGateService;
import br.com.corestacks.agende360.application.type.PeriodFilter;
import br.com.corestacks.agende360.application.type.UserRole;
import br.com.corestacks.agende360.security.model.UserDetailsImpl;
import br.com.corestacks.agende360.security.util.SecurityUtils;

@Service
public class DashboardService {

	private final DashboardRepository dashboardRepository;
	private final CompanyService companyService;
	private final FeatureGateService featureGateService;

	public DashboardService(
			DashboardRepository dashboardRepository,
			CompanyService companyService,
			FeatureGateService featureGateService) {
		this.dashboardRepository = dashboardRepository;
		this.companyService = companyService;
		this.featureGateService = featureGateService;
	}

	public DashboardMetricsResponse getMetrics(PeriodFilter periodFilter) {
		UUID companyId = SecurityUtils.getCompanyId();
		
		featureGateService.checkAdvancedDashboard(companyId);

		UserDetailsImpl user = SecurityUtils.getAuthenticatedUser();
		Company company = companyService.findByCompanyId(companyId);

		if (!user.getRole().equals(UserRole.ADMIN) || !company.getActive())
			throw new IllegalArgumentException("No permission");

		LocalDateTime now = LocalDateTime.now();

		DateRange currentRange = resolveDateRange(now, periodFilter);
		DashboardMetricsDTO current = dashboardRepository.getExpectedRevenueAndTotalAppointments(companyId, currentRange.start(), currentRange.end());

		DateRange previousRange = resolvePreviousRange(currentRange);
		DashboardMetricsDTO previous = dashboardRepository.getExpectedRevenueAndTotalAppointments(companyId, previousRange.start(), previousRange.end());

		BigDecimal appointmentsTrendValue = calculateTrend(BigDecimal.valueOf(current.totalAppointments()), BigDecimal.valueOf(previous.totalAppointments()));

		BigDecimal revenueTrendValue = calculateTrend(current.expectedRevenue(), previous.expectedRevenue());

		Long confirmedAppointments = dashboardRepository.getTotalAppointmentsConfirmed(companyId, currentRange.start(), currentRange.end());

		Trend appointmentsTrend = new Trend(appointmentsTrendValue, appointmentsTrendValue.signum() >= 0);
		Trend revenueTrend = new Trend(revenueTrendValue, revenueTrendValue.signum() >= 0);

		List<Object[]> rawTrend = dashboardRepository.getTrend(companyId, currentRange.start(), currentRange.end());
		List<TrendPoint> chart = buildChart(rawTrend);

		List<Object[]> rawTopServices = dashboardRepository.getTopServices(companyId, currentRange.start(), currentRange.end());
		List<TopServiceItem> topServices = buildTopServices(rawTopServices);

		List<String> insights = buildInsights(revenueTrendValue, appointmentsTrendValue, rawTrend, rawTopServices);

		return new DashboardMetricsResponse(
				current.expectedRevenue(),
				current.totalAppointments(),
				confirmedAppointments,
				revenueTrend,
				appointmentsTrend,
				chart,
				topServices,
				insights
		);
	}

	private List<TrendPoint> buildChart(List<Object[]> rawTrend) {
		List<TrendPoint> chart = new ArrayList<TrendPoint>();
		int i = 0;
		while (i < rawTrend.size()) {
			Object[] row = rawTrend.get(i);
			String label = (String) row[0];
			BigDecimal value = (BigDecimal) row[1];
			chart.add(new TrendPoint(label, value));
			i++;
		}
		return chart;
	}

	private List<TopServiceItem> buildTopServices(List<Object[]> rawTopServices) {
		List<TopServiceItem> result = new ArrayList<TopServiceItem>();
		int i = 0;
		while (i < rawTopServices.size()) {
			Object[] row = rawTopServices.get(i);
			String name = (String) row[0];
			BigDecimal revenue = (BigDecimal) row[1];
			Long count = ((Number) row[2]).longValue();
			result.add(new TopServiceItem(name, revenue, count));
			i++;
		}
		return result;
	}

	private DateRange resolvePreviousRange(DateRange currentRange) {
		long days = ChronoUnit.DAYS.between(currentRange.start().toLocalDate(), currentRange.end().toLocalDate()) + 1;
		LocalDateTime previousStart = currentRange.start().minusDays(days);
		LocalDateTime previousEnd = currentRange.start().minusSeconds(1);
		return new DateRange(previousStart, previousEnd);
	}

	private List<String> buildInsights(
			BigDecimal revenueTrendValue,
			BigDecimal appointmentsTrendValue,
			List<Object[]> trend,
			List<Object[]> topServices) {

		List<String> insights = new ArrayList<String>();

		if (revenueTrendValue.compareTo(BigDecimal.ZERO) > 0) {
			insights.add("Faturamento cresceu " + revenueTrendValue.abs() + "% em relação ao período anterior");
		} else if (revenueTrendValue.compareTo(BigDecimal.ZERO) < 0) {
			insights.add("Faturamento caiu " + revenueTrendValue.abs() + "% em relação ao período anterior");
		}

		if (appointmentsTrendValue.compareTo(BigDecimal.ZERO) > 0) {
			insights.add("Número de agendamentos aumentou " + appointmentsTrendValue.abs() + "%");
		} else if (appointmentsTrendValue.compareTo(BigDecimal.ZERO) < 0) {
			insights.add("Número de agendamentos caiu " + appointmentsTrendValue.abs() + "%");
		}

		if (trend != null && !trend.isEmpty()) {
			Object[] best = trend.get(0);
			int i = 1;
			while (i < trend.size()) {
				Object[] current = trend.get(i);
				BigDecimal currentValue = (BigDecimal) current[1];
				BigDecimal bestValue = (BigDecimal) best[1];
				if (currentValue.compareTo(bestValue) > 0) {
					best = current;
				}
				i++;
			}
			String label = (String) best[0];
			insights.add(label + " foi o dia com maior faturamento");
		}

		if (topServices != null && !topServices.isEmpty()) {
			Object[] top = topServices.get(0);
			String name = (String) top[0];
			insights.add(name + " é o serviço mais vendido no período");
		}

		return insights;
	}

	private DateRange resolveDateRange(LocalDateTime referenceDate, PeriodFilter periodFilter) {
		if (periodFilter.equals(PeriodFilter.TODAY)) {
			return new DateRange(atStartOfDay(referenceDate), atEndOfDay(referenceDate));
		} else if (periodFilter.equals(PeriodFilter.WEEK)) {
			return new DateRange(atStartOfWeek(referenceDate), atEndOfWeek(referenceDate));
		} else if (periodFilter.equals(PeriodFilter.MONTH)) {
			return new DateRange(atStartOfMonth(referenceDate), atEndOfMonth(referenceDate));
		} else if (periodFilter.equals(PeriodFilter.LAST_MONTH)) {
			return new DateRange(atStartOfLastMonth(referenceDate), atEndOfLastMonth(referenceDate));
		}
		throw new IllegalArgumentException("Invalid period");
	}

	private LocalDateTime atStartOfDay(LocalDateTime now) {
		return now.with(LocalTime.MIN);
	}

	private LocalDateTime atEndOfDay(LocalDateTime now) {
		return now.with(LocalTime.MAX);
	}

	private LocalDateTime atStartOfWeek(LocalDateTime now) {
		return atStartOfDay(now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)));
	}

	private LocalDateTime atEndOfWeek(LocalDateTime now) {
		return atEndOfDay(now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY)));
	}

	private LocalDateTime atStartOfMonth(LocalDateTime now) {
		return atStartOfDay(now.withDayOfMonth(1));
	}

	private LocalDateTime atEndOfMonth(LocalDateTime now) {
		return atEndOfDay(now.withDayOfMonth(now.toLocalDate().lengthOfMonth()));
	}

	private LocalDateTime atStartOfLastMonth(LocalDateTime now) {
		return atStartOfDay(now.minusMonths(1).withDayOfMonth(1));
	}

	private LocalDateTime atEndOfLastMonth(LocalDateTime now) {
		LocalDateTime lastMonth = now.minusMonths(1);
		return atEndOfDay(lastMonth.withDayOfMonth(lastMonth.toLocalDate().lengthOfMonth()));
	}

	private BigDecimal calculateTrend(BigDecimal current, BigDecimal previous) {
		if (previous.compareTo(BigDecimal.ZERO) == 0) {
			if (current.compareTo(BigDecimal.ZERO) == 0)
				return BigDecimal.ZERO;

			return BigDecimal.valueOf(100);
		}
		return current.subtract(previous)
				.divide(previous, 6, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.setScale(2, RoundingMode.HALF_UP);
	}
}