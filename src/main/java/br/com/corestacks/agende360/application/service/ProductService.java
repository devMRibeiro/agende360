package br.com.corestacks.agende360.application.service;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
	
    private final ProductRepository productRepository;
    private final CompanyRepository companyRepository;
    private final Cache<UUID, Map<UUID, Product>> productsCache;
    private final Cache<String, Company> companysCache;
    
//    private final FeatureGateService featureGateService;

    public ProductService(
    		ProductRepository productRepository,
    		CompanyRepository companyRepository,
    		Cache<UUID, Map<UUID, Product>> productsCache,
    		Cache<String, Company> companysCache) {
        this.productRepository = productRepository;
		this.companyRepository = companyRepository;
		this.productsCache = productsCache;
		this.companysCache = companysCache;
    }

    public List<ProductResponse> list(UUID companyId, String slug, Boolean active) {
    	Company company = companysCache.getIfPresent(slug);
    	
    	if (company == null) {
    		
	    	company = companyId != null ? companyRepository.findByCompanyId(companyId) : companyRepository.findBySlug(slug);
	    	
	    	if (company == null || !company.getActive())
	    		throw new IllegalArgumentException("Company not found");
	    	
	    	companysCache.put(company.getSlug(), company);
    	}
    	
        // 1. Busca no cache
        Map<UUID, Product> mapProducts = productsCache.getIfPresent(company.getId());
        
        // 2. Se ainda não existe no cache, faz uma busca no banco e adiciona no cache
        if (mapProducts == null) {
        	mapProducts = new Hashtable<UUID, Product>();
        	LOGGER.info("PRODUCTS: não encontrado no cache. Consultando no banco.");
        	List<Product> products = productRepository.findByCompanyId(company.getId());
        	
        	for (Product p : products)
        		mapProducts.put(p.getId(), p);
        	
        	productsCache.put(company.getId(), mapProducts);
        }
        
        List<ProductResponse> productsResponse = new ArrayList<ProductResponse>();
        
        // 3. Realiza filtro de produtos ativos
        for (Product product : mapProducts.values()) {
        	if (product.getActive())
	        	productsResponse.add(new ProductResponse(
	                product.getId(),
	                product.getName(),
	                product.getDescription(),
	                product.getPrice(),
	                product.getDurationMinutes(),
	                product.getActive()
	            ));
        }
        	
        return productsResponse;
    }
    
    public ProductResponse findById(UUID productId) {
        UUID companyId = SecurityUtils.getCompanyId();

        Product product = productRepository.findByProductId(productId);

        if (product == null)
            throw new IllegalArgumentException("Product not found");

        if (!product.getCompanyId().equals(companyId))
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
        product.setCompanyId(companyId);
        productRepository.save(product);
        productsCache.invalidate(companyId);
    }

    public void update(UUID productId, ProductUpdateRequest request) {
        UUID companyId = SecurityUtils.getCompanyId();

        Product product = productRepository.findByProductId(productId);

        if (product == null)
            throw new IllegalArgumentException("Product not found");

        if (!product.getCompanyId().equals(companyId))
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

        if (!product.getCompanyId().equals(companyId))
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

        if (!product.getCompanyId().equals(companyId))
            throw new IllegalArgumentException("Product not found");

        if (product.getActive())
            throw new IllegalArgumentException("Product is already active");

        product.setActive(true);
        productRepository.save(product);
        productsCache.invalidate(companyId);
    }
}