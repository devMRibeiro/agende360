package br.com.corestacks.agende360.cache.config;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import br.com.corestacks.agende360.application.model.Company;
import br.com.corestacks.agende360.application.model.Product;
import br.com.corestacks.agende360.application.model.User;

@Component
public class CacheMonitor {
	
	private static final Logger log = LoggerFactory.getLogger(CacheMonitor.class);

	private final Cache<UUID, Map<UUID, Product>> productsCache;
	private final Cache<UUID, Map<UUID, User>> usersCache;
	private final Cache<String, Company> companiesCache;
	
	public CacheMonitor(
			Cache<UUID, Map<UUID, Product>> productsCache,
			Cache<UUID, Map<UUID, User>> usersCache,
			Cache<String, Company> companiesCache) {
		this.productsCache = productsCache;
		this.usersCache = usersCache;
		this.companiesCache = companiesCache;
	}
	
	@Scheduled(fixedDelay = 300000)
	public void logStatus() {
		logStats("Products", productsCache.stats());
		logStats("Users", usersCache.stats());
		logStats("Companies", companiesCache.stats());
	}
	
	private void logStats(String entity, CacheStats stats) {
		log.info(
				"{}Cache hits={} misses={} hitRate={} evictions={}",
				entity,
				stats.hitCount(),
				stats.missCount(),
				stats.hitRate(),
				stats.evictionCount()
				);
	}
}