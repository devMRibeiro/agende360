package br.com.corestacks.agende360.infrastructure.http.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.corestacks.agende360.infrastructure.http.exception.HttpException;
import br.com.corestacks.agende360.infrastructure.http.model.HttpRequest;

@Component
public class RestHttpClient implements HttpClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestHttpClient.class);
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
			LOGGER.error("Erro ao chamar {}", request.url(), e);
			throw new HttpException("Error executing HTTP request", e);
		}
	}

	@Override
	public <T> T get(HttpRequest request, Class<T> responseType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T put(HttpRequest request, Class<T> responseType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void delete(HttpRequest request) {
		// TODO Auto-generated method stub
		
	}
}