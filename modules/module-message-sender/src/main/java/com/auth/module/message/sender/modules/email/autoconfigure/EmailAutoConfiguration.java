package com.auth.module.message.sender.modules.email.autoconfigure;

import com.auth.module.message.sender.modules.email.transport.EmailAttachmentResolver;
import com.auth.module.message.sender.modules.email.transport.MimeMailTransport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

/**
 * Email transport auto-configuration (JavaMail MIME send).
 *
 * @author Bunny
 */
@AutoConfiguration
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
@ConditionalOnProperty(name = "spring.mail.host")
public class EmailAutoConfiguration {

	/**
	 * @param restTemplate HTTP client for URL attachments
	 * @return attachment resolver
	 */
	@ConditionalOnMissingBean
	@Bean
	EmailAttachmentResolver emailAttachmentResolver(RestTemplate restTemplate) {
		return new EmailAttachmentResolver(restTemplate);
	}

	/**
	 * @param mailSender JavaMail sender
	 * @param attachmentResolver attachment resolver
	 * @return MIME mail transport
	 */
	@ConditionalOnMissingBean
	@Bean
	MimeMailTransport mimeMailTransport(JavaMailSender mailSender, EmailAttachmentResolver attachmentResolver) {
		return new MimeMailTransport(mailSender, attachmentResolver);
	}

}
