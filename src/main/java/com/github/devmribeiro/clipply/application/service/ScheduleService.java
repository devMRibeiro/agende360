package com.github.devmribeiro.clipply.application.service;

import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.repository.ScheduleRepository;

@Service
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;
	
	public ScheduleService(ScheduleRepository scheduleRepository) {
		this.scheduleRepository = scheduleRepository;
	}
	
	
}