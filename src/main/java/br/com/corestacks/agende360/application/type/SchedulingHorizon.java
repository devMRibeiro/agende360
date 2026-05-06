package br.com.corestacks.agende360.application.type;

public enum SchedulingHorizon {
	SETE_DIAS(7),
	QUATORZE_DIAS(14),
	TRINTA_DIAS(30),
	SEM_LIMITE(0);
	
	private int value;
	
	private SchedulingHorizon(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
}