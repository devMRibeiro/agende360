package br.com.corestacks.agende360.outbox.handler;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.corestacks.agende360.messaging.email.dto.CompanyRegistrationEmail;
import br.com.corestacks.agende360.messaging.email.service.EmailService;
import br.com.corestacks.agende360.outbox.enums.OutboxEventType;
import br.com.corestacks.agende360.outbox.model.OutboxEvent;

@Component
public class CompanyRegistrationEmailHandler implements OutboxEventHandler {

	private final EmailService emailService;
	private final ObjectMapper mapper;
	
	public CompanyRegistrationEmailHandler(
			EmailService emailService,
			ObjectMapper mapper) {
		this.emailService = emailService;
		this.mapper = mapper;
	}

	@Override
	public OutboxEventType supports() {
		return OutboxEventType.COMPANY_REGISTRATION_EMAIL;
	}

	@Override
	public void handle(OutboxEvent event) {
		sendAccessCreatedEmail(mapper.convertValue(event.getPayload(), CompanyRegistrationEmail.class));
	}
	
	private void sendAccessCreatedEmail(CompanyRegistrationEmail emailDTO) {
        Map<String, String> vars = Map.of(
                "COMPANY_NAME", emailDTO.companyName(),
                "LINK_PUBLICO", emailDTO.publicLink(),
                "COMPANY_DOCUMENT", emailDTO.companyDoc(),
                "USER_NAME", emailDTO.userName(),
                "USER_EMAIL", emailDTO.userEmail(),
                "TEMP_PASSWORD", emailDTO.passwordTemp(),
                "YEAR", String.valueOf(LocalDateTime.now().getYear())
        );

        emailService.sendUserAccessEmail(emailDTO.userEmail(), vars);
    }
}