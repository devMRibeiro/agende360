package br.com.corestacks.agende360.infrastructure.whatsapp.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.corestacks.agende360.application.model.Appointment;
import br.com.corestacks.agende360.application.model.Company;
import br.com.corestacks.agende360.application.model.Customer;
import br.com.corestacks.agende360.application.model.Product;
import br.com.corestacks.agende360.application.model.User;
import br.com.corestacks.agende360.infrastructure.whatsapp.client.WhatsAppClient;
import br.com.corestacks.agende360.infrastructure.whatsapp.dto.Component;
import br.com.corestacks.agende360.infrastructure.whatsapp.dto.Parameter;
import br.com.corestacks.agende360.infrastructure.whatsapp.dto.WhatsappMessageFactory;

@Service
public class WhatsAppService {

    private final WhatsAppClient whatsAppClient;

    public WhatsAppService(WhatsAppClient whatsAppClient) {
        this.whatsAppClient = whatsAppClient;
    }

    public void sendAppointmentConfirmation(Appointment appointment, Company company, Customer customer, Product product, User professional) {

    	String formattedDate = appointment.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    	String formattedTime = appointment.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    	
        List<Component> components = List.of(
            new Component(
                "body",
                null,
                null,
                List.of(
                    new Parameter("text", customer.getName()),
                    new Parameter("text", formattedDate),
                    new Parameter("text", formattedTime),
                    new Parameter("text", company.getName()),
                    new Parameter("text", product.getName()),
                    new Parameter("text", professional.getName())
                )
            ),
            new Component(
                "button",
                "url",
                "0",
                List.of(
                    new Parameter(
                        "text",
                        company.getSlug() + "/cancel/" + appointment.getToken()
                    )
                )
            )
        );

        whatsAppClient.sendTemplate(WhatsappMessageFactory.createTemplateMessage(
                "55" + customer.getPhone(),
                "appointment_confirmed",
                components
        ));
    }
}