package io.kindledrop.engine.mail;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.nio.file.Path;
import java.util.Properties;

public class KindleMailSender {

    public void sendWithAttachment(
            Smtp smtp,
            String toKindleEmail,
            String subject,
            String text,
            Path attachment,
            String attachmentName
    ) throws Exception {

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(smtp.host());
        sender.setPort(smtp.port());
        sender.setUsername(smtp.user());
        sender.setPassword(smtp.password());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        // TLS/SSL options
        if (smtp.useSSL()) {
            props.put("mail.smtp.ssl.enable", "true");
        } else if (smtp.useStartTLS()) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }

        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");

        MimeMessage msg = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
        helper.setFrom(smtp.from());
        helper.setTo(toKindleEmail);
        helper.setSubject(subject);
        helper.setText(text, false);

        FileSystemResource file = new FileSystemResource(attachment.toFile());
        helper.addAttachment(attachmentName, file);

        sender.send(msg);
    }

    public record Smtp(
            String host,
            int port,
            String user,
            String from,
            boolean useStartTLS,
            boolean useSSL,
            String password
    ) {}
}
