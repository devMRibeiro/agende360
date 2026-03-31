package com.github.devmribeiro.clipply.application.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

import com.github.devmribeiro.clipply.application.dto.request.AppointmentRequest;
import com.github.devmribeiro.clipply.application.dto.response.AvailableSlotsResponse;
import com.github.devmribeiro.clipply.application.dto.response.ProductResponse;
import com.github.devmribeiro.clipply.application.dto.response.ProfessionalResponse;
import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.model.Company;
import com.github.devmribeiro.clipply.application.model.Product;
import com.github.devmribeiro.clipply.application.model.User;
import com.github.devmribeiro.clipply.application.repository.CompanyRepository;
import com.github.devmribeiro.clipply.application.repository.ProductRepository;
import com.github.devmribeiro.clipply.application.repository.UserRepository;
import com.github.devmribeiro.clipply.application.service.AppointmentService;
import com.github.devmribeiro.clipply.application.type.UserRole;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final AppointmentService appointmentService;
    private final CompanyRepository companyRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public PublicController(
            AppointmentService appointmentService,
            CompanyRepository companyRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.appointmentService = appointmentService;
        this.companyRepository = companyRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ── Listagem pública de serviços da empresa ───────────
    @GetMapping("/{slug}/products")
    public ResponseEntity<List<ProductResponse>> listProducts(@PathVariable String slug) {
        Company company = companyRepository.findBySlug(slug);

        if (company == null || !company.getActive())
            throw new IllegalArgumentException("Company not found");

        List<Product> products = productRepository.findByCompanyId(company.getId());
        List<ProductResponse> result = new ArrayList<ProductResponse>(products.size());

        int i = 0;
        while (i < products.size()) {
            Product p = products.get(i);
            if (p.getActive()) {
                result.add(new ProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        p.getDurationMinutes(),
                        p.getActive()
                ));
            }
            i++;
        }

        return ResponseEntity.ok(result);
    }

    // ── Listagem pública de profissionais da empresa ──────
    @GetMapping("/{slug}/professionals")
    public ResponseEntity<List<ProfessionalResponse>> listProfessionals(@PathVariable String slug, @RequestParam UUID productId) {

        Company company = companyRepository.findBySlug(slug);

        if (company == null || !company.getActive())
            throw new IllegalArgumentException("Company not found");

        List<User> users = userRepository.findByCompanyId(company.getId());
        List<ProfessionalResponse> result = new ArrayList<ProfessionalResponse>();

        int i = 0;
        while (i < users.size()) {
            User u = users.get(i);
            if (u.getActive() && u.getRole() == UserRole.PROFESSIONAL) {
                result.add(new ProfessionalResponse(u.getId(), u.getName()));
            }
            i++;
        }

        return ResponseEntity.ok(result);
    }

    // ── Slots disponíveis ─────────────────────────────────
    @GetMapping("/{slug}/slots")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @PathVariable String slug,
            @RequestParam UUID professionalId,
            @RequestParam UUID productId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(slug, professionalId, productId, date));
    }

    // ── Criar agendamento ─────────────────────────────────
    @PostMapping("/{slug}/appointment")
    public ResponseEntity<Void> create(@PathVariable String slug, @RequestBody @Valid AppointmentRequest request) {
        appointmentService.create(slug, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ── Cancelar agendamento via token ────────────────────
    @GetMapping("/appointment/cancel/{token}")
    public ResponseEntity<Void> cancel(@PathVariable String token) {
        appointmentService.cancel(token);
        return ResponseEntity.ok().build();
    }
}