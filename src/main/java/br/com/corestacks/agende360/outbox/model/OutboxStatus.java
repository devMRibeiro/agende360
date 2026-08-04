package br.com.corestacks.agende360.outbox.model;

public enum OutboxStatus {
	PENDING,
	PROCESSING,
	PROCESSED,
	ERROR
}