package com.github.devmribeiro.clipply.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.request.ProductCreateRequest;
import com.github.devmribeiro.clipply.application.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/product")
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

	private final ProductService productService;
	
	public ProductController(ProductService productService) {
		this.productService = productService;
	}
	
	@PostMapping
	public ResponseEntity<Void> createProduct(@RequestBody @Valid ProductCreateRequest request) {
		productService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED.value()).build();
	}
}