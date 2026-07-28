package br.com.corestacks.agende360.infrastructure.whatsapp.dto;

import java.util.List;

import br.com.corestacks.agende360.infrastructure.whatsapp.enums.WhatsappConstants;

public final class WhatsappMessageFactory {

    private WhatsappMessageFactory() { }

    public static WhatsappTemplateMessageRequest createTemplateMessage(String phone, String templateName, List<Component> components) {
        return new WhatsappTemplateMessageRequest(
        		WhatsappConstants.MESSAGING_PRODUCT_WHATSAPP.getValue(),
        		phone,
        		WhatsappConstants.TYPE_MESSAGE.getValue(),
        		new Template(
        				templateName,
        				new Language(WhatsappConstants.LANGUAGE_PT_BR.getValue()
				),
				components)
		);
    }
}