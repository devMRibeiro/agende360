package com.github.devmribeiro.clipply.application.dto.request;

import java.util.UUID;

public record UpdateUserRequest(
		UUID id,
		String name,
		String phone,
		String email
	){
}