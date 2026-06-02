package br.com.corestacks.agende360.application.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.corestacks.agende360.application.dto.request.ProductCreateRequest;
import br.com.corestacks.agende360.application.dto.request.ProductUpdateRequest;
import br.com.corestacks.agende360.application.dto.response.ProductResponse;
import br.com.corestacks.agende360.application.service.ProductService;
import br.com.corestacks.agende360.security.util.SecurityUtils;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/product")
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> list() {
        return ResponseEntity.ok(productService.list(SecurityUtils.getCompanyId(), null, null));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> findById(@PathVariable UUID productId) {
        return ResponseEntity.ok(productService.findById(productId));
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid ProductCreateRequest request) {
        productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Void> update(@PathVariable UUID productId, @RequestBody @Valid ProductUpdateRequest request) {
        productService.update(productId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{productId}/disable")
    public ResponseEntity<Void> disable(@PathVariable UUID productId) {
        productService.disable(productId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{productId}/enable")
    public ResponseEntity<Void> enable(@PathVariable UUID productId) {
        productService.enable(productId);
        return ResponseEntity.ok().build();
    }
}