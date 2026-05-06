package br.com.corestacks.agende360.application.dto.request;

import br.com.corestacks.agende360.application.model.Endereco;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterCompanyRequest(

		@NotBlank(message = "companyName is required")
		String companyName,

		@NotBlank(message = "userName is required")
		String userName,

		@NotBlank
		@Email
		String email,

		@NotBlank
		String document,
		
		@NotBlank
		String phone,
		
		@NotNull
		Endereco endereco
	) {
}