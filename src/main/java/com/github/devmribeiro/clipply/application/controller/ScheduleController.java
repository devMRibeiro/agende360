package com.github.devmribeiro.clipply.application.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.devmribeiro.clipply.application.dto.request.ScheduleRequest;
import com.github.devmribeiro.clipply.application.dto.response.ScheduleResponse;
import com.github.devmribeiro.clipply.application.service.ScheduleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schedule")
@PreAuthorize("hasRole('ADMIN')")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> list() {
        return ResponseEntity.ok(scheduleService.list());
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid ScheduleRequest request) {
        scheduleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<Void> update(@PathVariable UUID scheduleId, @RequestBody @Valid ScheduleRequest request) {
        scheduleService.update(scheduleId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> delete(@PathVariable UUID scheduleId) {
        scheduleService.delete(scheduleId);
        return ResponseEntity.ok().build();
    }
}