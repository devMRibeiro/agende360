package com.github.devmribeiro.clipply.application.util;

import java.io.InputStream;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertiesLoader {
	@Bean
    public Properties customProperties() throws Exception {

        Properties props = new Properties();

        InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("application.properties");

        props.load(input);

        return props;
    }
}