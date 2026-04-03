package com.github.devmribeiro.clipply.application.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.github.devmribeiro.clipply.application.util.EmailTemplateBuilder;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.MessageRejectedException;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.SesException;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final SesClient sesClient;

    public EmailService(SesClient sesClient) {
        this.sesClient = sesClient;
    }

    public void sendEmail(String to, String subject, String htmlBody) {

        String requestId = UUID.randomUUID().toString();

        log.info("[EMAIL][START] requestId={} to={} subject={} time={}", requestId, to, subject, LocalDateTime.now());

        try {

            SendEmailRequest request = SendEmailRequest.builder()
                    .source("no-reply@seudominio.com")
                    .destination(Destination.builder()
                            .toAddresses(to)
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder()
                                    .html(Content.builder().data(htmlBody).build())
                                    .build())
                            .build())
                    .build();

            SendEmailResponse response = sesClient.sendEmail(request);

            log.info("[EMAIL][SUCCESS] requestId={} messageId={} statusCode={}", requestId, response.messageId(), response.sdkHttpResponse().statusCode());

        } catch (MessageRejectedException e) {
            log.error("[EMAIL][REJECTED] requestId={} reason={} to={}", requestId, e.awsErrorDetails().errorMessage(), to, e);
            throw new RuntimeException("Email rejected by SES");

        } catch (SesException e) {
            log.error("[EMAIL][SES_ERROR] requestId={} awsMessage={} statusCode={}", requestId, e.awsErrorDetails().errorMessage(), e.statusCode(), e);
            throw new RuntimeException("AWS SES error");

        } catch (Exception e) {
            log.error("[EMAIL][GENERIC_ERROR] requestId={} message={}", requestId, e.getMessage(), e);
            throw new RuntimeException("Unexpected error sending email");
        }
    }
    
    public void sendAppointmentConfirmedEmail(String to, String clientName, String serviceName, String date, String time, String professionalName) {
        sendEmail(to, "Agendamento confirmado", EmailTemplateBuilder.appointmentConfirmed(clientName, serviceName, date, professionalName));
    }
}