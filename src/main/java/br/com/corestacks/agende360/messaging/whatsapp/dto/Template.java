package br.com.corestacks.agende360.messaging.whatsapp.dto;

import java.util.List;

public record Template(
	    String name,
	    Language language,
	    List<Component> components
	) {
}