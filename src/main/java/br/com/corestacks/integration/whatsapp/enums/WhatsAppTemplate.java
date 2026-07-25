package br.com.corestacks.integration.whatsapp.enums;

public enum WhatsAppTemplate {

	APPOINTMENT_CONFIRMED("appointment_confirmed");
	
	private final String value;
	
	private WhatsAppTemplate(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}