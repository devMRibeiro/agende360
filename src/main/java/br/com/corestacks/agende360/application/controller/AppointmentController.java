package br.com.corestacks.agende360.application.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.corestacks.agende360.application.dto.response.AppointmentResponse;
import br.com.corestacks.agende360.application.service.AppointmentService;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> listByCompany() {
        return ResponseEntity.ok(appointmentService.listByCompany());
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppointmentResponse>> listTodayByCompany() {
        return ResponseEntity.ok(appointmentService.listTodayByCompany());
    }

    @GetMapping("/today/me")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ResponseEntity<List<AppointmentResponse>> listTodayByProfessional() {
        return ResponseEntity.ok(appointmentService.listTodayByProfessional());
    }

    @PatchMapping("/{appointmentId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> confirm(@PathVariable UUID appointmentId) {
        appointmentService.confirm(appointmentId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{appointmentId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    public ResponseEntity<Void> complete(@PathVariable UUID appointmentId) {
        appointmentService.complete(appointmentId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{appointmentId}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    public ResponseEntity<Void> noShow(@PathVariable UUID appointmentId) {
        appointmentService.noShow(appointmentId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSIONAL')")
    public ResponseEntity<Void> cancel(@PathVariable UUID appointmentId) {
        appointmentService.cancelByAdmin(appointmentId);
        return ResponseEntity.ok().build();
    }
}