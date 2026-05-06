package br.com.corestacks.agende360.application.exception;

public class ConflictException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConflictException(String message) {
		super(message);
	}
}