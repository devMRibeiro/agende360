package br.com.corestacks.integration.whatsapp.client;

import br.com.corestacks.integration.whatsapp.dto.WhatsAppMessageResponse;
import br.com.corestacks.integration.whatsapp.dto.WhatsappTemplateMessageRequest;

public interface WhatsAppClient {
    WhatsAppMessageResponse sendTemplate(WhatsappTemplateMessageRequest request);
}