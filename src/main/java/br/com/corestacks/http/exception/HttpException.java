package br.com.corestacks.http.exception;

public class HttpException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public HttpException(String msg, Throwable cause) {
		super(msg, cause);
	}
}