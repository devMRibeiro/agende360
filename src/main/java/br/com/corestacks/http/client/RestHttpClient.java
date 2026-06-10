package br.com.corestacks.http.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.corestacks.http.exception.HttpException;
import br.com.corestacks.http.model.HttpRequest;

@Component
public class RestHttpClient implements HttpClient {

	private final RestClient restClient;
	
	public RestHttpClient(RestClient restClient) {
		this.restClient = restClient;
	}
	
	@Override
	public <T> T post(HttpRequest request, Class<T> responseType) {
		
		try {
			
			return restClient.post()
					.uri(request.url())
					.headers(headers -> headers.setAll(request.headers()))
					.body(request.body())
					.retrieve()
					.body(responseType);
			
		} catch (Exception e) {
			throw new HttpException("Error executing HTTP request", e);
		}
	}
}