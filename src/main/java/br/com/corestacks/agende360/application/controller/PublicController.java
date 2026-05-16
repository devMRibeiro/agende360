package br.com.corestacks.agende360.application.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.corestacks.agende360.application.dto.request.AppointmentRequest;
import br.com.corestacks.agende360.application.dto.response.AvailableSlotsResponse;
import br.com.corestacks.agende360.application.dto.response.CompanyPublicResponse;
import br.com.corestacks.agende360.application.dto.response.ProductResponse;
import br.com.corestacks.agende360.application.dto.response.ProfessionalResponse;
import br.com.corestacks.agende360.application.exception.IllegalArgumentException;
import br.com.corestacks.agende360.application.model.Company;
import br.com.corestacks.agende360.application.model.Product;
import br.com.corestacks.agende360.application.repository.CompanyRepository;
import br.com.corestacks.agende360.application.repository.ProductRepository;
import br.com.corestacks.agende360.application.service.AppointmentService;
import br.com.corestacks.agende360.application.service.CompanySettingsService;
import br.com.corestacks.agende360.application.service.CustomerService;
import br.com.corestacks.agende360.application.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final AppointmentService appointmentService;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final CompanySettingsService companySettingsService;
    private final CustomerService customerService;

    public PublicController(
            AppointmentService appointmentService,
            CompanyRepository companyRepository,
            ProductRepository productRepository,
            UserService userService,
            CompanySettingsService companySettingsService,
            CustomerService customerService) {
        this.appointmentService = appointmentService;
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
        this.userService = userService;
		this.companySettingsService = companySettingsService;
		this.customerService = customerService;
    }

    // Public info company
    @GetMapping("/{slug}")
    public ResponseEntity<CompanyPublicResponse> getCompanyInfo(@PathVariable String slug) {
        Company company = companyRepository.findBySlug(slug);

        if (company == null || !company.getActive())
            throw new IllegalArgumentException("Company not found");

        int horizon = companySettingsService.getShcedulingHorizon(company.getId());

        return ResponseEntity.ok(new CompanyPublicResponse(company.getName(), company.getSlug(), horizon));
    }

    // Public listing of products
    @GetMapping("/{slug}/products")
    public ResponseEntity<List<ProductResponse>> listProducts(@PathVariable String slug) {
        Company company = companyRepository.findBySlug(slug);

        if (company == null || !company.getActive())
            throw new IllegalArgumentException("Company not found");

        List<Product> products = productRepository.findByCompanyIdAndActive(company.getId(), true);
        List<ProductResponse> result = new ArrayList<ProductResponse>(products.size());

        int i = 0;
        while (i < products.size()) {
            Product p = products.get(i);
            if (p.getActive()) {
                result.add(new ProductResponse(
                        p.getId(), p.getName(), p.getDescription(),
                        p.getPrice(), p.getDurationMinutes(), p.getActive()
                ));
            }
            i++;
        }

        return ResponseEntity.ok(result);
    }

    // Public listing for professionals - for now productId is not used
    @GetMapping("/{slug}/professionals")
    public ResponseEntity<List<ProfessionalResponse>> listProfessionals(@PathVariable String slug, @RequestParam UUID productId) {
        return ResponseEntity.ok(userService.listProfessionalsActive(slug));
    }

    // Available slots
    @GetMapping("/{slug}/slots")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @PathVariable String slug,
            @RequestParam UUID professionalId,
            @RequestParam UUID productId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(slug, professionalId, productId, date));
    }

    // Create appointment
    @PostMapping("/{slug}/appointment")
    public ResponseEntity<Void> create(@PathVariable String slug, @RequestBody @Valid AppointmentRequest request) {
        appointmentService.create(slug, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Cancel appointment by token
    @GetMapping("/appointment/cancel/{token}")
    public ResponseEntity<Void> cancel(@PathVariable String token) {
        appointmentService.cancel(token);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/check/customer")
    public ResponseEntity<Map<String, String>> checkCustomerEmail(@RequestBody Map<String, String> request) {
    	return ResponseEntity.ok(customerService.checkCustomerByPhone(request.get("phone")));
    }
}