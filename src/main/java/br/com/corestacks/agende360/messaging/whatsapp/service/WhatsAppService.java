package br.com.corestacks.agende360.messaging.whatsapp.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.messaging.whatsapp.client.WhatsAppClient;
import br.com.corestacks.agende360.messaging.whatsapp.dto.AppointmentConfirmationMessage;
import br.com.corestacks.agende360.messaging.whatsapp.dto.Component;
import br.com.corestacks.agende360.messaging.whatsapp.dto.Parameter;
import br.com.corestacks.agende360.messaging.whatsapp.dto.WhatsappMessageFactory;

@Service
public class WhatsAppService {

    private final WhatsAppClient whatsAppClient;

    public WhatsAppService(WhatsAppClient whatsAppClient) {
        this.whatsAppClient = whatsAppClient;
    }

    public void sendAppointmentConfirmation(AppointmentConfirmationMessage appointmentDTO) {

    	String formattedDate = appointmentDTO.appointmentDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    	String formattedTime = appointmentDTO.appointmentDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    	
        List<Component> components = List.of(
            new Component(
                "body",
                null,
                null,
                List.of(
                    new Parameter("text", appointmentDTO.customerPhone()),
                    new Parameter("text", formattedDate),
                    new Parameter("text", formattedTime),
                    new Parameter("text", appointmentDTO.companyAddress()),
                    new Parameter("text", appointmentDTO.productName()),
                    new Parameter("text", appointmentDTO.professionalName()),
            		new Parameter("text", appointmentDTO.companyName())
                )
            ),
            new Component(
                "button",
                "url",
                "0",
                List.of(
                    new Parameter(
                        "text",
                        appointmentDTO.companySlug() + "/cancel/" + appointmentDTO.appointmentToken()
                    )
                )
            )
        );

        whatsAppClient.sendTemplate(WhatsappMessageFactory.createTemplateMessage(
                "55" + appointmentDTO.customerPhone(),
                "appointment_confirmed",
                components
        ));
    }
}