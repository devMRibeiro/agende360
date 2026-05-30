package br.com.corestacks.agende360.cache.config;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import br.com.corestacks.agende360.application.dto.response.ProductResponse;

@Configuration
public class CacheConfig {
	
	@Bean
	public Cache<UUID, List<ProductResponse>> servicesCache() {
		return Caffeine.newBuilder()
				.maximumSize(10000)
				.expireAfterWrite(Duration.ofHours(1))
				.recordStats()
				.build();
	}
}