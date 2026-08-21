package br.com.corestacks.agende360.outbox.handler;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.corestacks.agende360.application.model.Appointment;
import br.com.corestacks.agende360.application.repository.AppointmentRepository;
import br.com.corestacks.agende360.application.type.AppointmentStatus;
import br.com.corestacks.agende360.messaging.whatsapp.dto.WhatsAppAppointmentDTO;
import br.com.corestacks.agende360.messaging.whatsapp.service.WhatsAppService;
import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import br.com.corestacks.agende360.outbox.model.OutboxEvent;

@Component
public class WhatsAppAppointmentHandler implements OutboxEventHandler {
	
	private final ObjectMapper objectMapper;
	private final WhatsAppService whatsAppService;
	private final AppointmentRepository appointmentRepository;
	
	public WhatsAppAppointmentHandler(
			WhatsAppService whatsAppService,
			ObjectMapper objectMapper,
			AppointmentRepository appointmentRepository) {
		this.objectMapper = objectMapper;
		this.whatsAppService = whatsAppService;
		this.appointmentRepository = appointmentRepository;
	}

	@Override
    public OutboxEventType supports() {
        return OutboxEventType.WHATSAPP_APPOINTMENT_CONFIRMATION;
    }

	@Override
	public void handle(OutboxEvent outboxEvent) {
		Appointment appointment = appointmentRepository.findByToken(objectMapper.convertValue(outboxEvent.getPayload(), WhatsAppAppointmentDTO.class).appointmentToken());
		
		if (appointment == null || !appointment.getStatus().equals(AppointmentStatus.CONFIRMED))
			return;
		
		sendWhatsAppReminder(outboxEvent.getEventType(), objectMapper.convertValue(outboxEvent.getPayload(), WhatsAppAppointmentDTO.class));
	}
	
	private void sendWhatsAppReminder(OutboxEventType eventType, WhatsAppAppointmentDTO wppDTO) {
        whatsAppService.sendAppointmentMessage(
        		eventType,
        		new WhatsAppAppointmentDTO(
        				wppDTO.customerPhone(),
        				wppDTO.customerName(),
        				wppDTO.appointmentDateTime(),
        				wppDTO.companyAddress(),
        				wppDTO.productName(),
        				wppDTO.productDescription(),
        				wppDTO.professionalName(),
        				wppDTO.companyName(),
        				wppDTO.companySlug(),
        				wppDTO.appointmentToken()
				)
		);
    }
}