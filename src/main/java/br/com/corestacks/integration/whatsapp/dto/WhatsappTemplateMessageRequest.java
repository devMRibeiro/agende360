package br.com.corestacks.integration.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WhatsappTemplateMessageRequest(
		
		@JsonProperty("messaging_product")
        String messagingProduct,
        
        String to,
        
        String type,
        
        Template template
	) {
}