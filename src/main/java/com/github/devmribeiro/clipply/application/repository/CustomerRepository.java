package com.github.devmribeiro.clipply.application.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.devmribeiro.clipply.application.model.Customer;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

	@Query("SELECT c FROM Customer c WHERE c.id = :id")
	Customer findByCustomerId(UUID id);
	
    Customer findByPhone(String phone);

    boolean existsByPhone(String phone);

    @Query("SELECT c FROM Customer c WHERE c.id IN " +
           "(SELECT a.customerId FROM Appointment a WHERE a.companyId = :companyId)")
    List<Customer> findByCompanyId(UUID companyId);
}