package com.github.devmribeiro.clipply.application.service;

import java.util.ArrayList;
import java.util.List;
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
}