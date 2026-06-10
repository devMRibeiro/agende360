package br.com.corestacks.http.client;

import br.com.corestacks.http.model.HttpRequest;

/**
 * 
 * HttpClient é uma inteface para que o resto da aplicação não conheça
 * RestClient, WebClient, Apache HttpClient, etc.
 * 
 * Isso é desacoplamento.
 * 
 * @author Michael Ribeiro
 */
public interface HttpClient {

	<T> T post(HttpRequest request, Class<T> responseType);
}