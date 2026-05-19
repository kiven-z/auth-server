package com.auth.service.system.message.infrastructure.modules.email.autoconfigure;

import com.auth.service.system.message.infrastructure.modules.email.transport.EmailAttachmentResolver;
import com.auth.service.system.message.infrastructure.modules.email.transport.MimeMailTransport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

/**
 * JavaMail MIME 邮件传输自动配置
 *
 * @author Bunny
 */
@AutoConfiguration
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
@ConditionalOnProperty(name = "spring.mail.host")
public class EmailAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	EmailAttachmentResolver emailAttachmentResolver(RestTemplate restTemplate) {
		return new EmailAttachmentResolver(restTemplate);
	}

	@ConditionalOnMissingBean
	@Bean
	MimeMailTransport mimeMailTransport(JavaMailSender mailSender, EmailAttachmentResolver attachmentResolver) {
		return new MimeMailTransport(mailSender, attachmentResolver);
	}

}
