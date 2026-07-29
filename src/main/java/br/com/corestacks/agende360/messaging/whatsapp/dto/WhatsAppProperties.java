package br.com.corestacks.agende360.messaging.whatsapp.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "system.whatsapp")
public record WhatsAppProperties(
		String phoneNumberId,
		String accessToken
	) {
}