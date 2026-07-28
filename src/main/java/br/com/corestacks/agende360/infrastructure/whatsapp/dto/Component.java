package br.com.corestacks.agende360.infrastructure.whatsapp.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Component(
		String type,
		
		@JsonProperty("sub_type")
		String subType,
		
		String index,
		
		List<Parameter> parameters
	){
}