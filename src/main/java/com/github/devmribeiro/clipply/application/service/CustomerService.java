package com.github.devmribeiro.clipply.application.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.dto.response.CustomerResponse;
import com.github.devmribeiro.clipply.application.model.Customer;
import com.github.devmribeiro.clipply.application.repository.CustomerRepository;
import com.github.devmribeiro.clipply.security.util.SecurityUtils;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<CustomerResponse> list() {
        UUID companyId = SecurityUtils.getCompanyId();
        List<Customer> customers = customerRepository.findByCompanyId(companyId);
        List<CustomerResponse> result = new ArrayList<CustomerResponse>(customers.size());

        for (Customer customer : customers) {
            result.add(new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhone()
            ));
        }

        return result;
    }

    // Usado internamente pelo AppointmentService
    public Customer findOrCreate(String name, String phone, String email) {
        Customer customer = customerRepository.findByPhone(phone);

        if (customer != null)
            return customer;

        Customer newCustomer = new Customer();
        newCustomer.setName(name);
        newCustomer.setPhone(phone);
        newCustomer.setEmail(email);
        return customerRepository.save(newCustomer);
    }

    public Customer findById(UUID customerId) {
        return customerRepository.findByCustomerId(customerId);
    }
    
    public Map<String, String> checkCustomerByPhone(String phone) {

    	if (phone == null || phone.length() < 12)
    		throw new IllegalArgumentException("Invalid phone");

    	Customer customer = customerRepository.findByPhone(phone);
    	
    	if (customer == null)
    		return null;

    	Map<String, String> mapCustomerInfo = new HashMap<String, String>();
    	mapCustomerInfo.put("phone", customer.getPhone());
    	mapCustomerInfo.put("email", maskEmail(customer.getEmail()));

    	return mapCustomerInfo;
    }

    private String maskEmail(String email) {

    	if (email == null || !email.contains("@"))
        	return "";

        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 4)
            return local.substring(0, 1) + "************" + local.substring(local.length() - 1) + "@" + domain;

        String start = local.substring(0, 2);
        String end = local.substring(local.length() - 2);

        return start + "************" + end + "@" + domain;
    }
}