package br.com.corestacks.agende360.outbox.handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.corestacks.agende360.application.model.Appointment;
import br.com.corestacks.agende360.application.repository.AppointmentRepository;
import br.com.corestacks.agende360.application.type.AppointmentStatus;
import br.com.corestacks.agende360.messaging.email.dto.AppointmentReminderEmail;
import br.com.corestacks.agende360.messaging.email.service.EmailService;
import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import br.com.corestacks.agende360.outbox.model.OutboxEvent;

@Component
public class EmailAppointmentReminderHandler implements OutboxEventHandler {
	
	private final EmailService emailService;
	private final ObjectMapper objectMapper;
	private final AppointmentRepository appointmentRepository;
	
	public EmailAppointmentReminderHandler(EmailService emailService, ObjectMapper objectMapper, AppointmentRepository appointmentRepository) {
		this.emailService = emailService;
		this.objectMapper = objectMapper;
		this.appointmentRepository = appointmentRepository;
	}

	@Override
    public OutboxEventType supports() {
        return OutboxEventType.EMAIL_APPOINTMENT_REMINDER;
    }

	@Override
	public void handle(OutboxEvent outboxEvent) {
		AppointmentReminderEmail appReminderEmail = objectMapper.convertValue(outboxEvent.getPayload(), AppointmentReminderEmail.class);
		
		String token = appReminderEmail.cancelURL().substring(appReminderEmail.cancelURL().indexOf("/cancel/") + "/cancel/".length());
		
		Appointment appointment = appointmentRepository.findByToken(token);
		
		if (appointment == null || !appointment.getStatus().equals(AppointmentStatus.CONFIRMED))
			return;
		
		sendEmailReminder(outboxEvent.getEventType(), appReminderEmail);
	}
	
	private void sendEmailReminder(OutboxEventType eventType, AppointmentReminderEmail emailDTO) {
        String formattedTime = emailDTO.appointmentStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        Map<String, String> vars = new HashMap<String, String>();
	    vars.put("COMPANY_NAME", emailDTO.companyName());
	    vars.put("CLIENT_NAME", emailDTO.customerName());
	    vars.put("SERVICE_NAME", emailDTO.productName());
	    vars.put("PROFESSIONAL_NAME", emailDTO.professionalName());
	    vars.put("APPOINTMENT_DATE", formattedTime);
	    vars.put("CANCEL_LINK", emailDTO.cancelURL());
	    vars.put("YEAR", String.valueOf(LocalDateTime.now().getYear()));
	    vars.put("LOGRADOURO", emailDTO.endereco().getLogradouro());
	    vars.put("NUMERO", emailDTO.endereco().getNumero());
	    vars.put("BAIRRO", emailDTO.endereco().getBairro());
	    vars.put("CIDADE", emailDTO.endereco().getCidade());
	    vars.put("UF", emailDTO.endereco().getUF());
	    vars.put("CEP", emailDTO.endereco().getCep());
	    vars.put("COMPLEMENTO", emailDTO.endereco().getComplemento() == null ? "" : emailDTO.endereco().getComplemento());

	    if (eventType.equals(OutboxEventType.EMAIL_APPOINTMENT_CONFIRMATION))
	    	emailService.sendAppointmentConfirmationEmail(emailDTO.customerEmail(), vars);
	    else
	    	emailService.sendAppointmentReminderEmail(emailDTO.customerEmail(), vars);
    }
}