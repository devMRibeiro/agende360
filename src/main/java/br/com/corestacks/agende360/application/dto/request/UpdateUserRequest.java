package br.com.corestacks.agende360.application.dto.request;

import java.util.UUID;

public record UpdateUserRequest(
		UUID id,
		String name,
		String phone,
		String email
	){
}