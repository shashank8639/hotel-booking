package com.hotelbooking.notification;

import com.hotelbooking.config.MailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * Thymeleaf-backed renderer. Activates only when Thymeleaf is on the classpath
 * and {@code app.mail.template-engine=thymeleaf}.
 * <p>
 * Uses reflection so this module compiles without a Thymeleaf Maven dependency.
 * Templates can use {@code th:text} once migrated; until then, placeholder files still work
 * if processed as raw strings via a simple context (we substitute {@code {{var}}} after process
 * as a bridge, or feed variables into the Thymeleaf context as strings).
 */
@Slf4j
@Component
@ConditionalOnClass(name = "org.thymeleaf.TemplateEngine")
@ConditionalOnProperty(prefix = "app.mail", name = "template-engine", havingValue = "thymeleaf")
@RequiredArgsConstructor
public class ThymeleafEmailTemplateEngine implements EmailTemplateEngine {

    private static final String BASE = "templates/email/";

    private final MailProperties mailProperties;
    private final Object thymeleafEngine = createEngine();

    @Override
    public String render(String templateName, Map<String, String> variables) {
        return render(templateName, variables, mailProperties.getDefaultLocale());
    }

    @Override
    public String render(String templateName, Map<String, String> variables, String locale) {
        String lang = normalizeLocale(locale);
        try {
            String raw = loadLocalized(templateName, lang);
            // Bridge: many of our templates still use {{var}} — apply after Thymeleaf process.
            String processed = processWithThymeleaf(raw, variables);
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String value = entry.getValue() == null ? "" : entry.getValue();
                processed = processed.replace("{{" + entry.getKey() + "}}", value);
            }
            processed = processed.replace("{{>header}}", loadLocalized("fragments/header.html", lang));
            processed = processed.replace("{{>footer}}", loadLocalized("fragments/footer.html", lang));
            // Re-apply vars into fragments
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String value = entry.getValue() == null ? "" : entry.getValue();
                processed = processed.replace("{{" + entry.getKey() + "}}", value);
            }
            return processed;
        } catch (Exception ex) {
            throw new EmailDeliveryException("Thymeleaf email render failed: " + templateName, ex);
        }
    }

    private String processWithThymeleaf(String templateContent, Map<String, String> variables) throws Exception {
        Class<?> contextClass = Class.forName("org.thymeleaf.context.Context");
        Object context = contextClass.getDeclaredConstructor().newInstance();
        Method setVariable = contextClass.getMethod("setVariable", String.class, Object.class);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            setVariable.invoke(context, entry.getKey(), entry.getValue());
        }
        Method process = thymeleafEngine.getClass().getMethod("process", String.class, contextClass);
        // StringTemplateResolver was configured to treat input as template content
        return (String) process.invoke(thymeleafEngine, templateContent, context);
    }

    private static Object createEngine() {
        try {
            Class<?> engineClass = Class.forName("org.thymeleaf.TemplateEngine");
            Object engine = engineClass.getDeclaredConstructor().newInstance();
            Class<?> resolverClass = Class.forName("org.thymeleaf.templateresolver.StringTemplateResolver");
            Object resolver = resolverClass.getDeclaredConstructor().newInstance();
            Method setTemplateMode = resolverClass.getMethod("setTemplateMode", String.class);
            setTemplateMode.invoke(resolver, "HTML");
            Method setCacheable = resolverClass.getMethod("setCacheable", boolean.class);
            setCacheable.invoke(resolver, false);
            Method setResolver = engineClass.getMethod("setTemplateResolver",
                    Class.forName("org.thymeleaf.templateresolver.ITemplateResolver"));
            setResolver.invoke(engine, resolver);
            return engine;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to bootstrap Thymeleaf TemplateEngine reflectively", ex);
        }
    }

    private String loadLocalized(String relativePath, String locale) throws Exception {
        ClassPathResource localized = new ClassPathResource(BASE + locale + "/" + relativePath);
        ClassPathResource resource = localized.exists()
                ? localized
                : new ClassPathResource(BASE + relativePath);
        try (InputStream in = resource.getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        }
    }

    private String normalizeLocale(String locale) {
        if (!StringUtils.hasText(locale)) {
            return mailProperties.getDefaultLocale();
        }
        String tag = locale.trim().toLowerCase(Locale.ROOT);
        int dash = tag.indexOf('-');
        return dash > 0 ? tag.substring(0, dash) : tag;
    }
}
