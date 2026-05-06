package br.com.corestacks.agende360.application.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_settings")
public class CompanySettings extends BaseEntity {

	@Column(nullable = false)
	private UUID companyId;
	
	private Integer schedulingHorizon;

	public Integer getSchedulingHorizon() {
		return schedulingHorizon;
	}

	public void setSchedulingHorizon(Integer schedulingHorizon) {
		this.schedulingHorizon = schedulingHorizon;
	}

	public UUID getCompanyId() {
		return companyId;
	}

	public void setCompanyId(UUID companyId) {
		this.companyId = companyId;
	}
}