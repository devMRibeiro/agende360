package br.com.corestacks.agende360.infrastructure.http.config;


import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpConfig {

	@Bean
	public RestClient restClient() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

	    factory.setConnectTimeout(Duration.ofSeconds(10));

	    factory.setReadTimeout(Duration.ofSeconds(30));

	    return RestClient.builder().requestFactory(factory).build();
	}
}