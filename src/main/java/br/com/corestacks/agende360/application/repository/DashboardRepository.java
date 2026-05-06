package br.com.corestacks.agende360.application.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.corestacks.agende360.application.dto.DashboardMetricsDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class DashboardRepository {

	@PersistenceContext
	private EntityManager em;
	
	public DashboardMetricsDTO getExpectedRevenueAndTotalAppointments(UUID companyId, LocalDateTime start, LocalDateTime end) {
	    return em.createQuery("""
	        SELECT new DashboardMetricsDTO(
	            COALESCE(SUM(p.price), 0),
	            COUNT(a)
	        )
	        FROM Appointment a
	        INNER JOIN Product p ON p.id = a.productId
	        INNER JOIN Company c ON c.id = a.companyId
	        WHERE 
	            c.id = :companyId
	            AND c.active = true
	            AND a.createdAt BETWEEN :start AND :end
	    """, DashboardMetricsDTO.class)
	    .setParameter("companyId", companyId)
	    .setParameter("start", start)
	    .setParameter("end", end)
	    .getSingleResult();
	}
	
	public Long getTotalAppointmentsConfirmed(UUID companyId, LocalDateTime start, LocalDateTime end) {
		return em.createQuery("""
		        SELECT
		            COUNT(a)
		        FROM Appointment a
		        INNER JOIN Company c ON c.id = a.companyId
		        WHERE 
		            c.id = :companyId
		            AND c.active = true
		            AND a.status NOT IN ('CANCELLED', 'NO_SHOW', 'COMPLETED')
		            AND a.createdAt BETWEEN :start AND :end
		    """, Long.class)
		    .setParameter("companyId", companyId)
		    .setParameter("start", start)
		    .setParameter("end", end)
		    .getSingleResult();
	}
	
	public BigDecimal getRevenueTrend(UUID companyId, LocalDateTime start, LocalDateTime end) {
		return em.createQuery("""
		        SELECT COALESCE(SUM(p.price), 0)
				FROM Appointment a
				INNER JOIN Product p ON p.id = a.productId
				WHERE a.companyId = :companyId
				  AND a.startTime BETWEEN :start AND :end
				  AND a.status NOT IN ('CANCELLED', 'NO_SHOW');
		    """, BigDecimal.class)
		    .setParameter("companyId", companyId)
		    .setParameter("start", start)
		    .setParameter("end", end)
		    .getSingleResult();
	}
	
	public List<Object[]> getTrend(UUID companyId, LocalDateTime start, LocalDateTime end) {
		return em.createQuery("""
				SELECT 
				  TO_CHAR(a.startTime, 'Dy') as label,
				  SUM(p.price) as value
				FROM Appointment a
				JOIN Product p ON p.id = a.productId
				WHERE a.companyId = :companyId
				  AND a.startTime BETWEEN :start AND :end
				GROUP BY label
				""", Object[].class)
				.setParameter("companyId", companyId)
			    .setParameter("start", start)
			    .setParameter("end", end)
			    .getResultList();
	}
	
	public List<Object[]> getTopServices(UUID companyId, LocalDateTime start, LocalDateTime end) {
		return em.createQuery("""
				SELECT 
				  p.name,
				  SUM(p.price),
				  COUNT(a.id)
				FROM Appointment a
				JOIN Product p ON p.id = a.productId
				WHERE a.companyId = :companyId
				  AND a.startTime BETWEEN :start AND :end
				GROUP BY p.name
				ORDER BY SUM(p.price) DESC
				LIMIT 1
				""", Object[].class)
				.setParameter("companyId", companyId)
			    .setParameter("start", start)
			    .setParameter("end", end)
			    .getResultList();
	}
}