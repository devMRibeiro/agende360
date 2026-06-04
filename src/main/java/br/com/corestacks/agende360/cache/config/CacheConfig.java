package br.com.corestacks.agende360.cache.config;

import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import br.com.corestacks.agende360.application.model.Company;
import br.com.corestacks.agende360.application.model.CompanySettings;
import br.com.corestacks.agende360.application.model.Product;
import br.com.corestacks.agende360.application.model.User;

@Configuration
public class CacheConfig {
	
	private static final long DEFAULT_CACHE_SIZE = 10_000;

	private <K, V> Cache<K, V> buildCache(long maximumSize) {
	    return Caffeine.newBuilder()
	            .maximumSize(maximumSize)
	            .recordStats()
	            .build();
	}
	
	@Bean
	public Cache<UUID, Map<UUID, Product>> productsCache() {
		return buildCache(DEFAULT_CACHE_SIZE);
	}
	
	@Bean
	public Cache<UUID, Map<UUID, User>> usersCache() {
		return buildCache(DEFAULT_CACHE_SIZE);
	}

	@Bean
	public Cache<String, Company> companiesCache() {
		return buildCache(DEFAULT_CACHE_SIZE);
	}

	@Bean
	public Cache<UUID, CompanySettings> companySettingsCache() {
		return buildCache(DEFAULT_CACHE_SIZE);
	}
}