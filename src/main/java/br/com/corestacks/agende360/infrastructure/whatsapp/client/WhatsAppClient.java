package br.com.corestacks.agende360.infrastructure.whatsapp.client;

import br.com.corestacks.agende360.infrastructure.whatsapp.dto.WhatsAppMessageResponse;
import br.com.corestacks.agende360.infrastructure.whatsapp.dto.WhatsappTemplateMessageRequest;

public interface WhatsAppClient {
    WhatsAppMessageResponse sendTemplate(WhatsappTemplateMessageRequest request);
}