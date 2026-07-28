package br.com.corestacks.agende360.infrastructure.http.client;

import br.com.corestacks.agende360.infrastructure.http.model.HttpRequest;

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

    <T> T get(HttpRequest request, Class<T> responseType);

    <T> T post(HttpRequest request, Class<T> responseType);

    <T> T put(HttpRequest request, Class<T> responseType);

    void delete(HttpRequest request);
}