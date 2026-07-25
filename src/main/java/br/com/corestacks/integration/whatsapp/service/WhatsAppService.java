package br.com.corestacks.integration.whatsapp.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.corestacks.integration.whatsapp.client.WhatsAppClient;
import br.com.corestacks.integration.whatsapp.dto.Component;
import br.com.corestacks.integration.whatsapp.dto.WhatsappMessageFactory;
import br.com.corestacks.integration.whatsapp.enums.WhatsAppTemplate;

@Service
public class WhatsAppService {

    private final WhatsAppClient whatsAppClient;

    public WhatsAppService(WhatsAppClient whatsAppClient) {
        this.whatsAppClient = whatsAppClient;
    }

    public void sendAppointmentConfirmation(String telefone, WhatsAppTemplate template, List<Component> components) {
        whatsAppClient.sendTemplate(
        		WhatsappMessageFactory.createTemplateMessage(
	                telefone,
	                template.getValue(),
	                components
				)
		);
    }
}