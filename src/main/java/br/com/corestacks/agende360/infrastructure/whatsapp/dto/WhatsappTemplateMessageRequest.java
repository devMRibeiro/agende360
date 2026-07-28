package br.com.corestacks.agende360.infrastructure.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsappTemplateMessageRequest(
		
		@JsonProperty("messaging_product")
        String messagingProduct,
        
        String to,
        
        String type,
        
        Template template
	) {
}