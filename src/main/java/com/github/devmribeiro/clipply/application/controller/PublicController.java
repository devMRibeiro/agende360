package com.github.devmribeiro.clipply.application.controller;

import java.time.LocalDate;
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
import com.github.devmribeiro.clipply.application.service.AppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final AppointmentService appointmentService;

    public PublicController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/{slug}/slots")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @PathVariable String slug,
            @RequestParam UUID professionalId,
            @RequestParam UUID productId,
            @RequestParam LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAvailableSlots(slug, professionalId, productId, date));
    }

    @PostMapping("/{slug}/appointment")
    public ResponseEntity<Void> create(
            @PathVariable String slug,
            @RequestBody @Valid AppointmentRequest request) {
        appointmentService.create(slug, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/appointment/cancel/{token}")
    public ResponseEntity<Void> cancel(@PathVariable String token) {
        appointmentService.cancel(token);
        return ResponseEntity.ok().build();
    }
}