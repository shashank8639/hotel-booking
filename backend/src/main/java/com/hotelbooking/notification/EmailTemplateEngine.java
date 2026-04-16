package com.hotelbooking.notification;

import java.util.Map;

/**
 * Renders HTML email templates. Default: placeholder engine.
 * Swap to Thymeleaf via {@code app.mail.template-engine=thymeleaf} when Thymeleaf is on the classpath.
 */
public interface EmailTemplateEngine {

    String render(String templateName, Map<String, String> variables);

    String render(String templateName, Map<String, String> variables, String locale);
}
