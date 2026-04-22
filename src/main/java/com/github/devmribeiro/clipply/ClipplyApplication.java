package com.github.devmribeiro.clipply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ClipplyApplication {
	public static void main(String[] args) {
		SpringApplication.run(ClipplyApplication.class, args);
	}
}