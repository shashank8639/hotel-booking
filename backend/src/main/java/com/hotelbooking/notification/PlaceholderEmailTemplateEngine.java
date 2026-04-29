package com.hotelbooking.notification;

import com.hotelbooking.config.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * Lightweight HTML template renderer (classpath templates).
 * <p>
 * Layout:
 * <ul>
 *   <li>{@code templates/email/&lt;locale&gt;/&lt;name&gt;} (i18n)</li>
 *   <li>fallback {@code templates/email/&lt;name&gt;}</li>
 * </ul>
 * Fragments: {@code {{>header}}} / {@code {{>footer}}}.
 */
@Component
@ConditionalOnProperty(prefix = "app.mail", name = "template-engine", havingValue = "placeholder", matchIfMissing = true)
@RequiredArgsConstructor
public class PlaceholderEmailTemplateEngine implements EmailTemplateEngine {

    private static final String BASE = "templates/email/";

    private final MailProperties mailProperties;

    @Override
    public String render(String templateName, Map<String, String> variables) {
        return render(templateName, variables, mailProperties.getDefaultLocale());
    }

    @Override
    public String render(String templateName, Map<String, String> variables, String locale) {
        String lang = normalizeLocale(locale);
        String html = loadLocalized(templateName, lang);
        html = html.replace("{{>header}}", loadLocalized("fragments/header.html", lang));
        html = html.replace("{{>footer}}", loadLocalized("fragments/footer.html", lang));
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue();
            html = html.replace("{{" + entry.getKey() + "}}", value);
        }
        return html;
    }

    private String loadLocalized(String relativePath, String locale) {
        ClassPathResource localized = new ClassPathResource(BASE + locale + "/" + relativePath);
        if (localized.exists()) {
            return read(localized, relativePath);
        }
        return read(new ClassPathResource(BASE + relativePath), relativePath);
    }

    private String read(ClassPathResource resource, String relativePath) {
        try (InputStream in = resource.getInputStream()) {
            return StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new EmailDeliveryException("Unable to load email template: " + relativePath, ex);
        }
    }

    private String normalizeLocale(String locale) {
        if (!StringUtils.hasText(locale)) {
            return mailProperties.getDefaultLocale();
        }
        String tag = locale.trim().toLowerCase(Locale.ROOT);
        int dash = tag.indexOf('-');
        if (dash > 0) {
            tag = tag.substring(0, dash);
        }
        return tag;
    }
}
