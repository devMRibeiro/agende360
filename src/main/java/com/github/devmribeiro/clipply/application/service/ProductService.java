package com.github.devmribeiro.clipply.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.dto.request.ProductCreateRequest;
import com.github.devmribeiro.clipply.application.dto.request.ProductUpdateRequest;
import com.github.devmribeiro.clipply.application.dto.response.ProductResponse;
import com.github.devmribeiro.clipply.application.exception.ConflictException;
import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.model.Product;
import com.github.devmribeiro.clipply.application.repository.ProductRepository;
import com.github.devmribeiro.clipply.security.util.SecurityUtils;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> list() {
        UUID companyId = SecurityUtils.getCompanyId();
        List<Product> products = productRepository.findByCompanyId(companyId);
        List<ProductResponse> result = new ArrayList<ProductResponse>(products.size());

        for (Product product : products) {
            result.add(new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getDurationMinutes(),
                product.getActive()
            ));
        }

        return result;
    }

    public ProductResponse findById(UUID productId) {
        UUID companyId = SecurityUtils.getCompanyId();

        Product product = productRepository.findByProductId(productId);

        if (product == null)
            throw new IllegalArgumentException("Product not found");

        if (!product.getCompany().equals(companyId))
            throw new IllegalArgumentException("Product not found");

        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getDurationMinutes(),
            product.getActive()
        );
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

    public void update(UUID productId, ProductUpdateRequest request) {
        UUID companyId = SecurityUtils.getCompanyId();

        Product product = productRepository.findByProductId(productId);

        if (product == null)
            throw new IllegalArgumentException("Product not found");

        if (!product.getCompany().equals(companyId))
            throw new IllegalArgumentException("Product not found");

        if (!product.getName().equals(request.name()) && productRepository.existsByNameAndCompanyId(request.name(), companyId))
            throw new ConflictException("There is already a product with that name");

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setDurationMinutes(request.durationMinutes());
        productRepository.save(product);
    }

    public void disable(UUID productId) {
        UUID companyId = SecurityUtils.getCompanyId();

        Product product = productRepository.findByProductId(productId);

        if (product == null)
            throw new IllegalArgumentException("Product not found");

        if (!product.getCompany().equals(companyId))
            throw new IllegalArgumentException("Product not found");

        if (!product.getActive())
            throw new IllegalArgumentException("Product is already inactive");

        product.setActive(false);
        productRepository.save(product);
    }

    public void enable(UUID productId) {
        UUID companyId = SecurityUtils.getCompanyId();

        Product product = productRepository.findByProductId(productId);
        
        if (product == null)
            throw new IllegalArgumentException("Product not found");

        if (!product.getCompany().equals(companyId))
            throw new IllegalArgumentException("Product not found");

        if (product.getActive())
            throw new IllegalArgumentException("Product is already active");

        product.setActive(true);
        productRepository.save(product);
    }
}