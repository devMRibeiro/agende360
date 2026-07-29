package br.com.corestacks.agende360.messaging.email.engine;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class EmailTemplateEngine {
	public static String render(String path, Map<String, String> variables) {
		InputStream is = null;
        try {
        	
        	is = EmailTemplateEngine.class.getClassLoader().getResourceAsStream(path);

            if (is == null)
                throw new RuntimeException("Template not found: " + path);

            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        	
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String key = "{{" + entry.getKey() + "}}";
                content = content.replace(key, entry.getValue());
            }

            return content;

        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException("Error processing template", e);
        } finally {
			if (is != null) try { is.close(); } catch (Exception e2) { throw new RuntimeException("Error closing InputStream", e2); }
		}
    }
}