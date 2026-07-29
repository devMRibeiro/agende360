package br.com.corestacks.agende360.messaging.email.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import br.com.corestacks.agende360.messaging.email.engine.EmailTemplateEngine;

@Service
public class EmailService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);
	
	private final Resend resend;
	
	public EmailService(Resend resend) {
		this.resend = resend;
	}
	
	@Value("${resend.from.no-reply}")
	private String from;
	
	private static final String PATH_EMAIL_TEMPLATE_BASE = "emails/template";
	private static final String PATH_EMAIL_TEMPLATE_APPOINTMENT_CONFIRMED = PATH_EMAIL_TEMPLATE_BASE + "/appointment_confirmed.html";
	private static final String PATH_EMAIL_COMPANY_USER_CREATED = PATH_EMAIL_TEMPLATE_BASE + "/company_user_created.html";
	private static final String PATH_EMAIL_RESET_PASSWORD = PATH_EMAIL_TEMPLATE_BASE + "/reset_password.html";
	
	private void send(String path, String to, String subject, Map<String, String> vars) {
		
		if (to == null || to.isBlank()) {
			LOGGER.error("[EMAIL][ERROR] The recipient was not informed");
			return;
		}
		
		UUID requestId = UUID.randomUUID();
		
		LOGGER.info("[EMAIL][START] requestId={} to={} subject={} time={}", requestId, to, subject, LocalDateTime.now());

		CreateEmailOptions params = CreateEmailOptions.builder()
                .from(from)
                .to(to)
                .subject(subject)
                .html(EmailTemplateEngine.render(path, vars))
                .build();
		
		try {
			CreateEmailResponse response = resend.emails().send(params);
			LOGGER.info("[EMAIL][SUCCESS] requestId={} messageId={}", requestId, response.getId());
		} catch (ResendException e) {
			LOGGER.error("[EMAIL][REJECTED] requestId={} statusCode={} motivo={} to={}", requestId, e.getStatusCode(), e.getCause() , to, e);
			throw new RuntimeException("Error sending email", e);
		}
	}

	public void sendAppointmentConfirmedEmail(String to, Map<String, String> vars) {
		send(PATH_EMAIL_TEMPLATE_APPOINTMENT_CONFIRMED, to, "Agendamento Confirmado✅", vars);
	}

	public void sendUserAccessEmail(String to, Map<String, String> vars) {
		send(PATH_EMAIL_COMPANY_USER_CREATED, to, "Cadastrado com Sucesso✅", vars);
	}
	
	public void sendResetPasswordEmail(String to, Map<String, String> vars) {
		send(PATH_EMAIL_RESET_PASSWORD, to, "Redefinição de Senha", vars);
	}
}