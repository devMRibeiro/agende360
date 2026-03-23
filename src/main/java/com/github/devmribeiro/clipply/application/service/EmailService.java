package com.github.devmribeiro.clipply.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${resend.from.email}")
    private String fromEmail;

    private final RestTemplate restTemplate;

    public EmailService() {
        this.restTemplate = new RestTemplate();
    }

    public void sendAppointmentConfirmation(
    		String toEmail,
    		String customerName,
            String companyName,
            String productName,
            String professionalName,
            String startTime,
            String cancelUrl) {

        String subject = "Agendamento confirmado - " + companyName;

        String html =
            "<h2>Olá, " + customerName + "!</h2>" +
            "<p>Seu agendamento foi confirmado com sucesso.</p>" +
            "<ul>" +
            "<li><strong>Empresa:</strong> " + companyName + "</li>" +
            "<li><strong>Serviço:</strong> " + productName + "</li>" +
            "<li><strong>Profissional:</strong> " + professionalName + "</li>" +
            "<li><strong>Horário:</strong> " + startTime + "</li>" +
            "</ul>" +
            "<p>Caso precise cancelar, <a href=\"" + cancelUrl + "\">clique aqui</a>.</p>";

        send(toEmail, subject, html);
    }

    private void send(String to, String subject, String html) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        Map<String, Object> body = new HashMap<String, Object>();
        body.put("from", fromEmail);
        body.put("to", List.of(to));
        body.put("subject", subject);
        body.put("html", html);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(body, headers);

        restTemplate.postForEntity("https://api.resend.com/emails", entity, String.class);
    }
}