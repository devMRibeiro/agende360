package br.com.corestacks.agende360.infrastructure.whatsapp.enums;

public enum WhatsappConstants {
	MESSAGING_PRODUCT_WHATSAPP("whatsapp"),
	TYPE_MESSAGE("template"),
	TYPE_TEXT("text"),
    COMPONENT_BODY("body"),
    COMPONENT_BUTTON("button"),
    LANGUAGE_PT_BR("pt_BR"),
    SUBTYPE_URL("url");
	
	private final String value;
	
	private WhatsappConstants(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}