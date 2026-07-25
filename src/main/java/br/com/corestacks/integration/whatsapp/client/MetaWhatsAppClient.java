package br.com.corestacks.integration.whatsapp.client;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import br.com.corestacks.http.client.HttpClient;
import br.com.corestacks.http.model.HttpRequest;
import br.com.corestacks.integration.whatsapp.dto.WhatsAppMessageResponse;
import br.com.corestacks.integration.whatsapp.dto.WhatsAppProperties;
import br.com.corestacks.integration.whatsapp.dto.WhatsappTemplateMessageRequest;

@Component
public class MetaWhatsAppClient implements WhatsAppClient {

    private final HttpClient httpClient;
    private final WhatsAppProperties properties;

    public MetaWhatsAppClient(HttpClient httpClient,
                              WhatsAppProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
    }

    @Override
    public WhatsAppMessageResponse sendTemplate(WhatsappTemplateMessageRequest request) {

    	Map<String, String> headers = new HashMap<String, String>();
    	headers.put("Authorization", "Bearer " + properties.accessToken());
    	headers.put("Content-Type", "application/json");
    	
        return httpClient.post(new HttpRequest(buildUrl(), headers, request), WhatsAppMessageResponse.class);
    }

    private String buildUrl() {
        return String.format("https://graph.facebook.com/v23.0/%s/messages", properties.phoneNumberId());
    }
}