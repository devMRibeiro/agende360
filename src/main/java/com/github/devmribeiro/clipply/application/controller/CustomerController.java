package com.github.devmribeiro.clipply.application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.response.CustomerResponse;
import com.github.devmribeiro.clipply.application.service.CustomerService;

@RestController
@RequestMapping("/api/customer")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> list() {
        return ResponseEntity.ok(customerService.list());
    }
}