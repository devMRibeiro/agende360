package br.com.corestacks.integration.whatsapp.dto;

import java.util.List;

public record Template(
	    String name,
	    Language language,
	    List<Component> components
	) {
}