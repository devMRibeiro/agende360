package br.com.corestacks.agende360.outbox.enums;

public enum OutboxEventType {
	EMAIL_APPOINTMENT_CONFIRMATION(7),
    EMAIL_APPOINTMENT_REMINDER(5),

    WHATSAPP_APPOINTMENT_CONFIRMATION(7),
    WHATSAPP_APPOINTMENT_REMINDER(5),

    APPOINTMENT_CANCELLED(10),

    COMPANY_REGISTRATION_EMAIL(3);
	
	private final int maxRetries;
	
	OutboxEventType(int maxRetries) {
		this.maxRetries = maxRetries; 
	}
	
	public int getMaxRetries() {
		return maxRetries;
	}
}