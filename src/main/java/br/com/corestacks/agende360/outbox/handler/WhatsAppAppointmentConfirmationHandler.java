package br.com.corestacks.agende360.outbox.handler;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.corestacks.agende360.messaging.whatsapp.dto.WhatsAppAppointmentConfirmation;
import br.com.corestacks.agende360.messaging.whatsapp.service.WhatsAppService;
import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import br.com.corestacks.agende360.outbox.model.OutboxEvent;

@Component
public class WhatsAppAppointmentConfirmationHandler implements OutboxEventHandler {
	
	private final ObjectMapper objectMapper;
	private final WhatsAppService whatsAppService;
	
	public WhatsAppAppointmentConfirmationHandler(WhatsAppService whatsAppService, ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
		this.whatsAppService = whatsAppService;
	}

	@Override
    public OutboxEventType supports() {
        return OutboxEventType.WHATSAPP_APPOINTMENT_CONFIRMATION;
    }

	@Override
	public void handle(OutboxEvent outboxEvent) {
		objectMapper.findAndRegisterModules();
		sendWhatsAppConfirmation(objectMapper.convertValue(outboxEvent.getPayload(), WhatsAppAppointmentConfirmation.class));
	}
	
	private void sendWhatsAppConfirmation(WhatsAppAppointmentConfirmation wppDTO) {
        whatsAppService.sendAppointmentConfirmation(
        		new WhatsAppAppointmentConfirmation(
        				wppDTO.customerPhone(),
        				wppDTO.customerName(),
        				wppDTO.appointmentDateTime(),
        				wppDTO.companyAddress(),
        				wppDTO.productName(),
        				wppDTO.professionalName(),
        				wppDTO.companyName(),
        				wppDTO.companySlug(),
        				wppDTO.appointmentToken()
				)
		);
    }
}