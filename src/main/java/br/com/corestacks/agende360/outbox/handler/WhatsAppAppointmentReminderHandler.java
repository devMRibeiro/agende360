package br.com.corestacks.agende360.outbox.handler;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.corestacks.agende360.messaging.whatsapp.dto.WhatsAppAppointmentReminder;
import br.com.corestacks.agende360.messaging.whatsapp.service.WhatsAppService;
import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import br.com.corestacks.agende360.outbox.model.OutboxEvent;

@Component
public class WhatsAppAppointmentReminderHandler implements OutboxEventHandler {
	
	private final ObjectMapper objectMapper;
	private final WhatsAppService whatsAppService;
	
	public WhatsAppAppointmentReminderHandler(WhatsAppService whatsAppService, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.whatsAppService = whatsAppService;
	}

	@Override
    public OutboxEventType supports() {
        return OutboxEventType.WHATSAPP_APPOINTMENT_REMINDER;
    }

	@Override
	public void handle(OutboxEvent outboxEvent) {
		objectMapper.findAndRegisterModules();
		sendWhatsAppReminder(objectMapper.convertValue(outboxEvent.getPayload(), WhatsAppAppointmentReminder.class));
	}
	
	private void sendWhatsAppReminder(WhatsAppAppointmentReminder wppDTO) {
        whatsAppService.sendAppointmentReminder(
        		new WhatsAppAppointmentReminder(
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