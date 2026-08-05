package br.com.corestacks.agende360.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

@Component
public class JacksonConfig {

	@Bean
    public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper()
				   .registerModule(new ParameterNamesModule())
				   .registerModule(new Jdk8Module())
				   .registerModule(new JavaTimeModule()); // new module, NOT JSR310Module        
        return mapper;
    }
}
