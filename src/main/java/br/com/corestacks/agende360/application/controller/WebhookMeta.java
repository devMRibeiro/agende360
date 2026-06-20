package br.com.corestacks.agende360.application.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/webhook/meta")
public class WebhookMeta {

	@Value("#{SYSTEM.TOKEN-WEBHOOK}")
	private String webhookMetaToken;
	
	@GetMapping
	public ResponseEntity<String> verifyWebhook(
			@RequestParam("hub.mode") String mode,
			@RequestParam("hub.verify_token") String token,
			@RequestParam("hub.challenge") String challenge) {
	    
		if ("subscribe".equals(mode) && webhookMetaToken.equals(token))
	        return ResponseEntity.ok(challenge);

	    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	}
	
	@PostMapping
	public ResponseEntity<Void> receiveEvent(@RequestBody String payload) {
	    System.out.println(payload);
	    return ResponseEntity.ok().build();
	}
}