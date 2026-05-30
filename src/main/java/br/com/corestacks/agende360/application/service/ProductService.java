package br.com.corestacks.agende360.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;

import br.com.corestacks.agende360.application.dto.request.ProductCreateRequest;
import br.com.corestacks.agende360.application.dto.request.ProductUpdateRequest;
import br.com.corestacks.agende360.application.dto.response.ProductResponse;
import br.com.corestacks.agende360.application.exception.ConflictException;
import br.com.corestacks.agende360.application.exception.IllegalArgumentException;
import br.com.corestacks.agende360.application.model.Company;
import br.com.corestacks.agende360.application.model.Product;
import br.com.corestacks.agende360.application.repository.CompanyRepository;
import br.com.corestacks.agende360.application.repository.ProductRepository;
import br.com.corestacks.agende360.security.util.SecurityUtils;
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final Cache<UUID, List<ProductResponse>> productsCache;
    
//    private final FeatureGateService featureGateService;

    public ProductService(
    		ProductRepository productRepository,
    		CompanyRepository companyRepository,
    		Cache<UUID, List<ProductResponse>> productsCache) {
        this.productRepository = productRepository;
		this.companyRepository = companyRepository;
		this.productsCache = productsCache;
    }

    public List<ProductResponse> list(String slug) {
    	Company company = companyRepository.findBySlug(slug);
    	
    	if (company == null || !company.getActive())
            throw new IllegalArgumentException("Company not found");
    	
    	return list(company.getId(), true);
    }
    
    public List<ProductResponse> list(UUID companyId, Boolean active) {
    	Company company = companyRepository.findByCompanyId(companyId);
    	
        if (company == null || !company.getActive())
            throw new IllegalArgumentException("Company not found");
    	
        List<ProductResponse> result = productsCache.getIfPresent(company.getId());
        
        if (result != null)
        	return result;
        
        List<Product> products = productRepository.findByCompanyIdAndActive(companyId, active);

        result = new ArrayList<ProductResponse>(products.size());
        
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

        productsCache.put(company.getId(), result);
        
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

//        featureGateService.checkServicesLimit(companyId, productRepository.findByCompanyIdAndActive(companyId, true).size());
        
        if (productRepository.existsByNameAndCompanyId(request.name(), companyId))
            throw new ConflictException("There is already a product with that name");

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setDurationMinutes(request.durationMinutes());
        product.setCompany(companyId);
        productRepository.save(product);
        
        productsCache.invalidate(companyId);
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
        productsCache.invalidate(companyId);
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
        productsCache.invalidate(companyId);
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
        productsCache.invalidate(companyId);
    }
}