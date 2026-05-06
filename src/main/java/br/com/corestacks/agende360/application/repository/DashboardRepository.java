package br.com.corestacks.agende360.application.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.corestacks.agende360.application.dto.response.DashboardMetricsDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class DashboardRepository {

	@PersistenceContext
	private EntityManager em;

	public DashboardMetricsDTO getExpectedRevenueAndTotalAppointments(UUID companyId, LocalDateTime start, LocalDateTime end) {
		Object[] row = (Object[]) em.createNativeQuery(
			"SELECT COALESCE(SUM(p.price), 0), COUNT(a.id) " +
			"FROM appointment a " +
			"INNER JOIN product p ON p.id = a.product_id " +
			"INNER JOIN company c ON c.id = a.company_id " +
			"WHERE c.id = :companyId " +
			"AND c.active = true " +
			"AND a.created_at BETWEEN :start AND :end"
		)
		.setParameter("companyId", companyId)
		.setParameter("start", start)
		.setParameter("end", end)
		.getSingleResult();

		BigDecimal expectedRevenue = (BigDecimal) row[0];
		Long totalAppointments = ((Number) row[1]).longValue();

		return new DashboardMetricsDTO(expectedRevenue, totalAppointments);
	}

	public Long getTotalAppointmentsConfirmed(UUID companyId, LocalDateTime start, LocalDateTime end) {
		Object result = em.createNativeQuery(
			"SELECT COUNT(a.id) " +
			"FROM appointment a " +
			"INNER JOIN company c ON c.id = a.company_id " +
			"WHERE c.id = :companyId " +
			"AND c.active = true " +
			"AND a.status NOT IN ('CANCELLED', 'NO_SHOW', 'COMPLETED') " +
			"AND a.created_at BETWEEN :start AND :end"
		)
		.setParameter("companyId", companyId)
		.setParameter("start", start)
		.setParameter("end", end)
		.getSingleResult();

		return ((Number) result).longValue();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getTrend(UUID companyId, LocalDateTime start, LocalDateTime end) {
		return em.createNativeQuery(
			"SELECT TO_CHAR(a.start_time, 'Dy') AS label, COALESCE(SUM(p.price), 0) AS value " +
			"FROM appointment a " +
			"INNER JOIN product p ON p.id = a.product_id " +
			"WHERE a.company_id = :companyId " +
			"AND a.start_time BETWEEN :start AND :end " +
			"AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
			"GROUP BY TO_CHAR(a.start_time, 'Dy') " +
			"ORDER BY MIN(a.start_time)"
		)
		.setParameter("companyId", companyId)
		.setParameter("start", start)
		.setParameter("end", end)
		.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> getTopServices(UUID companyId, LocalDateTime start, LocalDateTime end) {
		return em.createNativeQuery(
			"SELECT p.name, COALESCE(SUM(p.price), 0), COUNT(a.id) " +
			"FROM appointment a " +
			"INNER JOIN product p ON p.id = a.product_id " +
			"WHERE a.company_id = :companyId " +
			"AND a.start_time BETWEEN :start AND :end " +
			"AND a.status NOT IN ('CANCELLED', 'NO_SHOW') " +
			"GROUP BY p.name " +
			"ORDER BY SUM(p.price) DESC " +
			"LIMIT 5"
		)
		.setParameter("companyId", companyId)
		.setParameter("start", start)
		.setParameter("end", end)
		.getResultList();
	}
}