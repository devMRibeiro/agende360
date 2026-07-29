package br.com.corestacks.agende360.application.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

	@Scheduled(cron = "* */1 * * * *") // every a minute
	public void exec() {
		
	}
}

/**
 * 
 * Tabela
 * - Evento
 * 
 * Colunas:
 *  - id
 *  - type (APPOINTMENT_CREATED, etc)
 *  - status (PENDENTE, PROCESSANDO, PROCESSADO, ERRO)
 *  - aggregate_type (APPOINTMENT, COMPANY, etc) -> entidade que gerou o evento
 *  - aggregate_id -> id da entidade
 *  - payload (JSONB) -> dados necessários para processar o evento
 *  - retry_count -> quantidade de tentativas
 *  - created_at
 *  - updated_at 
 * 
 * 
 */
