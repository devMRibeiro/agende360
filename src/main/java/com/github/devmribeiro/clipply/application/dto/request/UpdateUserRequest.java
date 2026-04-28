package com.github.devmribeiro.clipply.application.dto.request;

public record UpdateUserRequest(
		String name,
		String phone,
		Boolean isProfessional
	){
}