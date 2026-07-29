package br.com.corestacks.agende360.messaging.whatsapp.client;

import br.com.corestacks.agende360.messaging.whatsapp.dto.WhatsAppMessageResponse;
import br.com.corestacks.agende360.messaging.whatsapp.dto.WhatsappTemplateMessageRequest;

public interface WhatsAppClient {
    WhatsAppMessageResponse sendTemplate(WhatsappTemplateMessageRequest request);
}