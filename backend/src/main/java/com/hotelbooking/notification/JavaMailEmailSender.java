package com.hotelbooking.notification;

import com.hotelbooking.config.MailProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * {@code JavaMailSender}-backed transport.
 * <p>
 * Activates when:
 * <ul>
 *   <li>{@code org.springframework.mail.javamail.JavaMailSender} is on the classpath</li>
 *   <li>{@code app.mail.transport=smtp}</li>
 * </ul>
 * Uses reflection so the project compiles without {@code spring-boot-starter-mail}.
 * When the starter is added, Spring Boot can auto-configure a {@code JavaMailSender} bean
 * (typically via {@code spring.mail.*}); this adapter will discover and use it.
 * If no bean exists, a {@code JavaMailSenderImpl} is built from {@link MailProperties}.
 */
@Slf4j
@Component
@ConditionalOnClass(name = "org.springframework.mail.javamail.JavaMailSender")
@ConditionalOnProperty(prefix = "app.mail", name = "transport", havingValue = "smtp")
public class JavaMailEmailSender implements EmailSender {

    private final MailProperties mailProperties;
    private final Object javaMailSender;

    public JavaMailEmailSender(MailProperties mailProperties, ApplicationContext applicationContext) {
        this.mailProperties = mailProperties;
        this.javaMailSender = resolveSender(applicationContext, mailProperties);
    }

    @Override
    public void send(EmailMessage message) {
        log.info("EMAIL REQUEST (smtp) → to={}, cc={}, subject={}",
                message.getTo(), message.getCc(), message.getSubject());
        try {
            Class<?> senderClass = Class.forName("org.springframework.mail.javamail.JavaMailSender");
            Method createMimeMessage = senderClass.getMethod("createMimeMessage");
            Object mimeMessage = createMimeMessage.invoke(javaMailSender);

            Class<?> helperClass = Class.forName("org.springframework.mail.javamail.MimeMessageHelper");
            Constructor<?> helperCtor = helperClass.getConstructor(
                    Class.forName("jakarta.mail.internet.MimeMessage"), boolean.class, String.class);
            Object helper = helperCtor.newInstance(mimeMessage, true, "UTF-8");

            helperClass.getMethod("setFrom", String.class, String.class)
                    .invoke(helper, mailProperties.getFrom(), mailProperties.getFromName());
            helperClass.getMethod("setTo", String.class).invoke(helper, message.getTo());
            if (StringUtils.hasText(message.getCc())) {
                helperClass.getMethod("setCc", String.class).invoke(helper, message.getCc());
            }
            helperClass.getMethod("setSubject", String.class).invoke(helper, message.getSubject());
            helperClass.getMethod("setText", String.class, boolean.class)
                    .invoke(helper, message.getHtmlBody(), true);

            for (EmailMessage.EmailAttachment attachment : message.getAttachments()) {
                Class<?> resourceClass = Class.forName("org.springframework.core.io.ByteArrayResource");
                Object resource = resourceClass.getConstructor(byte[].class)
                        .newInstance((Object) attachment.content());
                helperClass.getMethod("addAttachment", String.class,
                                Class.forName("org.springframework.core.io.InputStreamSource"))
                        .invoke(helper, attachment.filename(), resource);
            }

            senderClass.getMethod("send", Class.forName("jakarta.mail.internet.MimeMessage"))
                    .invoke(javaMailSender, mimeMessage);
            log.info("EMAIL SENT (smtp) → to={}, subject={}", message.getTo(), message.getSubject());
        } catch (Exception ex) {
            log.error("EMAIL FAILED (smtp) → to={}, reason={}", message.getTo(), ex.getMessage(), ex);
            throw new EmailDeliveryException("Failed to deliver email via JavaMailSender to " + message.getTo(), ex);
        }
    }

    private static Object resolveSender(ApplicationContext ctx, MailProperties props) {
        try {
            Class<?> senderType = Class.forName("org.springframework.mail.javamail.JavaMailSender");
            Map<String, ?> beans = ctx.getBeansOfType(senderType);
            if (!beans.isEmpty()) {
                return beans.values().iterator().next();
            }
            Class<?> implClass = Class.forName("org.springframework.mail.javamail.JavaMailSenderImpl");
            Object impl = implClass.getDeclaredConstructor().newInstance();
            implClass.getMethod("setHost", String.class).invoke(impl, props.getHost());
            implClass.getMethod("setPort", int.class).invoke(impl, props.getPort());
            if (StringUtils.hasText(props.getUsername())) {
                implClass.getMethod("setUsername", String.class).invoke(impl, props.getUsername());
            }
            if (StringUtils.hasText(props.getPassword())) {
                implClass.getMethod("setPassword", String.class).invoke(impl, props.getPassword());
            }
            @SuppressWarnings("unchecked")
            var javaProps = (java.util.Properties) implClass.getMethod("getJavaMailProperties").invoke(impl);
            javaProps.put("mail.smtp.auth", String.valueOf(props.isSmtpAuth()));
            javaProps.put("mail.smtp.starttls.enable", String.valueOf(props.isStartTls()));
            return impl;
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to create JavaMailSender reflectively", ex);
        }
    }
}
