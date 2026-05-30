package br.com.corestacks.agende360.application.controller;

import java.time.LocalDate;
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
import br.com.corestacks.agende360.application.service.AppointmentService;
import br.com.corestacks.agende360.application.service.CompanyService;
import br.com.corestacks.agende360.application.service.CompanySettingsService;
import br.com.corestacks.agende360.application.service.CustomerService;
import br.com.corestacks.agende360.application.service.ProductService;
import br.com.corestacks.agende360.application.service.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final CompanyService companyService;
    private final CustomerService customerService;
    private final ProductService productService;

    public PublicController(
            AppointmentService appointmentService,
            UserService userService,
            CompanySettingsService companySettingsService,
            CustomerService customerService,
            ProductService productService,
            CompanyService companyService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
		this.companyService = companyService;
		this.customerService = customerService;
		this.productService = productService;
    }

    // Public info company
    @GetMapping("/{slug}")
    public ResponseEntity<CompanyPublicResponse> getCompanyInfo(@PathVariable String slug) {
        return ResponseEntity.ok(companyService.getCompanyInfo(slug));
    }

    // Public listing of products
    @GetMapping("/{slug}/products")
    public ResponseEntity<List<ProductResponse>> listProducts(@PathVariable String slug) {
        return ResponseEntity.ok(productService.list(slug));
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