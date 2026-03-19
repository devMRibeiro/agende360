package com.github.devmribeiro.clipply.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.dto.request.ScheduleRequest;
import com.github.devmribeiro.clipply.application.dto.response.ScheduleResponse;
import com.github.devmribeiro.clipply.application.exception.ConflictException;
import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.model.Schedule;
import com.github.devmribeiro.clipply.application.repository.ScheduleRepository;
import com.github.devmribeiro.clipply.security.util.SecurityUtils;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public List<ScheduleResponse> list() {
        UUID companyId = SecurityUtils.getCompanyId();
        List<Schedule> schedules = scheduleRepository.findByCompanyId(companyId);
        List<ScheduleResponse> result = new ArrayList<ScheduleResponse>(schedules.size());

        for (Schedule schedule : schedules) {
            result.add(new ScheduleResponse(
                schedule.getId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime()
            ));
        }

        return result;
    }

    public void create(ScheduleRequest request) {
        UUID companyId = SecurityUtils.getCompanyId();

        if (scheduleRepository.existsByCompanyIdAndDayOfWeek(companyId, request.dayOfWeek()))
            throw new ConflictException("There is already a schedule for this day of week");

        if (request.startTime().isAfter(request.endTime()) || request.startTime().equals(request.endTime()))
            throw new IllegalArgumentException("startTime must be before endTime");

        Schedule schedule = new Schedule();
        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setCompanyId(companyId);
        scheduleRepository.save(schedule);
    }

    public void update(UUID scheduleId, ScheduleRequest request) {
        UUID companyId = SecurityUtils.getCompanyId();

        Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);

        if (schedule == null)
            throw new IllegalArgumentException("Schedule not found");

        if (!schedule.getCompanyId().equals(companyId))
            throw new IllegalArgumentException("Schedule not found");

        if (request.startTime().isAfter(request.endTime()) || request.startTime().equals(request.endTime()))
            throw new IllegalArgumentException("startTime must be before endTime");

        schedule.setDayOfWeek(request.dayOfWeek());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        scheduleRepository.save(schedule);
    }

    public void delete(UUID scheduleId) {
        UUID companyId = SecurityUtils.getCompanyId();

        Schedule schedule = scheduleRepository.findById(scheduleId).orElse(null);

        if (schedule == null)
            throw new IllegalArgumentException("Schedule not found");

        if (!schedule.getCompanyId().equals(companyId))
            throw new IllegalArgumentException("Schedule not found");

        scheduleRepository.delete(schedule);
    }
}