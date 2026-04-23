package com.github.devmribeiro.clipply.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.dto.request.ScheduleRequest;
import com.github.devmribeiro.clipply.application.dto.response.ScheduleResponse;
import com.github.devmribeiro.clipply.application.dto.response.ScheduleResponseData;
import com.github.devmribeiro.clipply.application.exception.ConflictException;
import com.github.devmribeiro.clipply.application.exception.IllegalArgumentException;
import com.github.devmribeiro.clipply.application.model.Schedule;
import com.github.devmribeiro.clipply.application.repository.CompanySettingsRespository;
import com.github.devmribeiro.clipply.application.repository.ScheduleRepository;
import com.github.devmribeiro.clipply.security.util.SecurityUtils;

import jakarta.transaction.Transactional;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final CompanySettingsRespository companySettingsRespository;

    public ScheduleService(
    		ScheduleRepository scheduleRepository,
    		CompanySettingsRespository companySettingsRespository) {
        this.scheduleRepository = scheduleRepository;
		this.companySettingsRespository = companySettingsRespository;
    }

    @Transactional
    public ScheduleResponse list() {
        UUID companyId = SecurityUtils.getCompanyId();
        List<Schedule> schedules = scheduleRepository.findByCompanyId(companyId);
        List<ScheduleResponseData> data = new ArrayList<ScheduleResponseData>(schedules.size());

    	Integer horizon = companySettingsRespository.findByCompanyId(companyId).getSchedulingHorizon();
        
        for (Schedule schedule : schedules) {
        	data.add(new ScheduleResponseData(
                schedule.getId(),
                schedule.getDayOfWeek(),
                schedule.getStartTime(),
                schedule.getEndTime()
            ));
        }

        return new ScheduleResponse(horizon, data);
    }

    public void create(ScheduleRequest request) {
        UUID companyId = SecurityUtils.getCompanyId();

        if (request.startTime().isAfter(request.endTime()) || request.startTime().equals(request.endTime()))
            throw new IllegalArgumentException("startTime must be before endTime");

        // UUID.randomUUID() como excludeId garante que nenhum registro existente seja excluído da busca
        List<Schedule> overlapping = scheduleRepository.findOverlapping(
                companyId,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                UUID.randomUUID());

        if (!overlapping.isEmpty())
            throw new ConflictException("This time interval overlaps with an existing schedule for this day");

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

        // Exclui o próprio registro da verificação de sobreposição
        List<Schedule> overlapping = scheduleRepository.findOverlapping(
                companyId,
                request.dayOfWeek(),
                request.startTime(),
                request.endTime(),
                scheduleId);

        if (!overlapping.isEmpty())
            throw new ConflictException("This time interval overlaps with an existing schedule for this day");

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