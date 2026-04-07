package com.github.devmribeiro.clipply.messaging.engine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class EmailTemplateEngine {
	public static String render(String path, Map<String, String> variables) {
        try {
            String content = Files.readString(Path.of(path));

            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String key = "{{" + entry.getKey() + "}}";
                content = content.replace(key, entry.getValue());
            }

            return content;

        } catch (Exception e) {
            throw new RuntimeException("Error processing template", e);
        }
    }
}