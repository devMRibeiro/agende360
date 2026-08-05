package br.com.corestacks.agende360.outbox.handler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.corestacks.agende360.messaging.email.dto.AppointmentConfirmationEmail;
import br.com.corestacks.agende360.messaging.email.service.EmailService;
import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import br.com.corestacks.agende360.outbox.model.OutboxEvent;

public class AppointmentCreatedEmailHandler implements OutboxEventHandler {
	
	private final EmailService emailService;
	
	public AppointmentCreatedEmailHandler(EmailService emailService) {
		this.emailService = emailService;
	}

	@Override
    public boolean supports(OutboxEventType eventType) {
        return eventType == OutboxEventType.APPOINTMENT_CREATED_EMAIL;
    }

	@Override
	public void handle(OutboxEvent outboxEvent) {
		sendEmailConfirmation(new ObjectMapper().convertValue(outboxEvent.getPayload(), AppointmentConfirmationEmail.class));
	}
	
	private void sendEmailConfirmation(AppointmentConfirmationEmail emailDTO) {
        String cancelUrl = emailDTO.baseUrl() + "/appointment/" + emailDTO.companySlug() + "/cancel/" + emailDTO.appointmentToken();
        String formattedTime = emailDTO.appointmentStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        Map<String, String> vars = new HashMap<String, String>();
	    vars.put("COMPANY_NAME", emailDTO.companyName());
	    vars.put("CLIENT_NAME", emailDTO.customerName());
	    vars.put("SERVICE_NAME", emailDTO.productName());
	    vars.put("PROFESSIONAL_NAME", emailDTO.professionalName());
	    vars.put("APPOINTMENT_DATE", formattedTime);
	    vars.put("CANCEL_LINK", cancelUrl);
	    vars.put("YEAR", String.valueOf(LocalDateTime.now().getYear()));
	    vars.put("LOGRADOURO", emailDTO.endereco().getLogradouro());
	    vars.put("NUMERO", emailDTO.endereco().getNumero());
	    vars.put("BAIRRO", emailDTO.endereco().getBairro());
	    vars.put("CIDADE", emailDTO.endereco().getCidade());
	    vars.put("UF", emailDTO.endereco().getUF());
	    vars.put("CEP", emailDTO.endereco().getCep());
	    vars.put("COMPLEMENTO", emailDTO.endereco().getComplemento() == null ? "" : emailDTO.endereco().getComplemento());

        emailService.sendAppointmentConfirmedEmail(emailDTO.customerEmail(), vars);
    }
}