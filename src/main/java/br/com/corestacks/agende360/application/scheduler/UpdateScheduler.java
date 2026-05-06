package br.com.corestacks.agende360.application.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.corestacks.agende360.application.service.AppointmentService;

@Component
public class UpdateScheduler {
	
	private final AppointmentService appointmentService;
	
	public UpdateScheduler(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}
	
	@Scheduled(cron = "0 */10 * * * *") // every day at 2:00 AM
	public void run() {
		appointmentService.processDueAppointments();
	}
}