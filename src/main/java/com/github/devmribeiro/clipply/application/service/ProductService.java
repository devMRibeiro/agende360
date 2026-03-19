package com.github.devmribeiro.clipply.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.dto.request.ProductCreateRequest;
import com.github.devmribeiro.clipply.application.exception.ConflictException;
import com.github.devmribeiro.clipply.application.model.Product;
import com.github.devmribeiro.clipply.application.repository.ProductRepository;
import com.github.devmribeiro.clipply.security.util.SecurityUtils;

@Service
public class ProductService {

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public void create(ProductCreateRequest request) {

		UUID companyId = SecurityUtils.getCompanyId();

		if (productRepository.existsByNameAndCompanyId(request.name(), companyId))
			throw new ConflictException("There is already a product with that name");

		Product product = new Product();
		product.setName(request.name());
		product.setDescription(request.description());
		product.setPrice(request.price());
		product.setDurationMinutes(request.durationMinutes());
		product.setCompany(companyId);
		productRepository.save(product);
	}
}