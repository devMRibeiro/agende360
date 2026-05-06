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

import br.com.corestacks.agende360.application.dto.DashboardMetricsDTO;
import br.com.corestacks.agende360.application.dto.DateRange;
import br.com.corestacks.agende360.application.dto.response.DashboardMetricsResponse;
import br.com.corestacks.agende360.application.dto.response.Trend;
import br.com.corestacks.agende360.application.exception.IllegalArgumentException;
import br.com.corestacks.agende360.application.model.Company;
import br.com.corestacks.agende360.application.repository.DashboardRepository;
import br.com.corestacks.agende360.application.type.PeriodFilter;
import br.com.corestacks.agende360.application.type.UserRole;
import br.com.corestacks.agende360.security.model.UserDetailsImpl;
import br.com.corestacks.agende360.security.util.SecurityUtils;

@Service
public class DashboardService {

	private final DashboardRepository dashboardRepository;
	private final CompanyService companyService;
	
	public DashboardService(
			DashboardRepository dashboardRepository,
			CompanyService companyService) {
		this.dashboardRepository = dashboardRepository;
		this.companyService = companyService;
	}
	
	public DashboardMetricsResponse getMetrics(PeriodFilter periodFilter) {
		
		UUID companyId = SecurityUtils.getCompanyId();
		UserDetailsImpl user = SecurityUtils.getAuthenticatedUser();
		Company company = companyService.findByCompanyId(companyId);

		if (!user.getRole().equals(UserRole.ADMIN) || !company.getActive())
			throw new IllegalArgumentException("No permission");

		LocalDateTime now = LocalDateTime.now();
		
		DateRange currentRange = resolveDateRange(now, periodFilter);
		DashboardMetricsDTO current = getExpectedRevenueAndTotalAppointments(currentRange.start(), currentRange.end());

		DateRange previousRange = resolvePreviousRange(currentRange);
		DashboardMetricsDTO previous = getExpectedRevenueAndTotalAppointments(previousRange.start(), previousRange.end());

		BigDecimal appointmentsTrendValue = calculateTrend(BigDecimal.valueOf(current.totalAppointments()), BigDecimal.valueOf(previous.totalAppointments()));
		BigDecimal revenueTrendValue = calculateTrend(current.expectedRevenue(), previous.expectedRevenue());

		Long appointmentConfirmed = dashboardRepository.getTotalAppointmentsConfirmed(companyId, currentRange.start(), currentRange.end());

		Trend appointmentsTrend = new Trend(appointmentsTrendValue, appointmentsTrendValue.signum() >= 0);
		Trend revenueTrend = new Trend(revenueTrendValue, revenueTrendValue.signum() >= 0);
		
		List<String> insights = buildInsights(
				revenueTrend.value(),
				appointmentsTrend.value(),
				dashboardRepository.getTrend(companyId, currentRange.start(), currentRange.end()),
				dashboardRepository.getTopServices(companyId, currentRange.start(), currentRange.end()));
		
		return new DashboardMetricsResponse(
				current.expectedRevenue(),
				current.totalAppointments(),
				appointmentConfirmed,
				null,
				revenueTrend,
				appointmentsTrend,
				insights
		);
	}
	
	private DateRange resolvePreviousRange(DateRange currentRange) {
	    LocalDateTime previousStart = currentRange.start().minusDays(ChronoUnit.DAYS.between(currentRange.start().toLocalDate(), currentRange.end().toLocalDate()) + 1);
	    LocalDateTime previousEnd = currentRange.start().minusSeconds(1);
	    return new DateRange(previousStart, previousEnd);
	}
	
	private List<String> buildInsights(BigDecimal revenueTrendValue, BigDecimal appointmentsTrendValue, List<Object[]> trend, List<Object[]> topServices) {

	    List<String> insights = new ArrayList<String>();

	    // 1. Receita (trend)
	    if (revenueTrendValue.compareTo(BigDecimal.ZERO) > 0) {
	        insights.add("Faturamento cresceu " + revenueTrendValue.abs() + "% em relação ao período anterior");
	    } else if (revenueTrendValue.compareTo(BigDecimal.ZERO) < 0) {
	        insights.add("Faturamento caiu " + revenueTrendValue.abs() + "% em relação ao período anterior");
	    }

	    // 2. Agendamentos (trend)
	    if (appointmentsTrendValue.compareTo(BigDecimal.ZERO) > 0) {
	        insights.add("Número de agendamentos aumentou " + appointmentsTrendValue.abs() + "%");
	    } else if (appointmentsTrendValue.compareTo(BigDecimal.ZERO) < 0) {
	        insights.add("Número de agendamentos caiu " + appointmentsTrendValue.abs() + "%");
	    }

	    // 3. Melhor dia
	    if (trend != null && !trend.isEmpty()) {
	        Object[] best = trend.get(0);

	        for (int i = 1; i < trend.size(); i++) {
	            Object[] current = trend.get(i);

	            BigDecimal value = (BigDecimal) current[1];
	            BigDecimal bestValue = (BigDecimal) best[1];

	            if (value.compareTo(bestValue) > 0) {
	                best = current;
	            }
	        }

	        String label = (String) best[0];
	        insights.add(label + " foi o dia com maior faturamento");
	    }

	    // 4. Serviço mais vendido
	    if (topServices != null && !topServices.isEmpty()) {
	        Object[] top = topServices.get(0);
	        String name = (String) top[0];

	        insights.add(name + " é o serviço mais vendido no período");
	    }

	    return insights;
	}
	
	private DashboardMetricsDTO getExpectedRevenueAndTotalAppointments(LocalDateTime start, LocalDateTime end) {
		return dashboardRepository.getExpectedRevenueAndTotalAppointments(SecurityUtils.getCompanyId(), start, end);
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
	    if (previous.compareTo(BigDecimal.ZERO) == 0)
	        return current.compareTo(BigDecimal.ZERO) == 0
	                ? BigDecimal.ZERO
	                : BigDecimal.valueOf(100);

	    return current.subtract(previous)
	            .divide(previous, 6, RoundingMode.HALF_UP)
	            .multiply(BigDecimal.valueOf(100))
	            .setScale(2, RoundingMode.HALF_UP);
	}
}