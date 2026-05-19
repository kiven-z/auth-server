package com.auth.module.message.modules.email.autoconfigure;

import com.auth.module.message.modules.email.transport.EmailAttachmentResolver;
import com.auth.module.message.modules.email.transport.MimeMailTransport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

/**
 * Email transport auto-configuration (JavaMail MIME send).
 *
 * @author Bunny
 */
@AutoConfiguration
@ConditionalOnClass(JavaMailSender.class)
public class EmailAutoConfiguration {

	/**
	 * @param restTemplate HTTP client for URL attachments
	 * @return attachment resolver
	 */
	@Bean
	@ConditionalOnMissingBean
	EmailAttachmentResolver emailAttachmentResolver(RestTemplate restTemplate) {
		return new EmailAttachmentResolver(restTemplate);
	}

	/**
	 * @param mailSender JavaMail sender
	 * @param attachmentResolver attachment resolver
	 * @return MIME mail transport
	 */
	@Bean
	@ConditionalOnMissingBean
	MimeMailTransport mimeMailTransport(JavaMailSender mailSender, EmailAttachmentResolver attachmentResolver) {
		return new MimeMailTransport(mailSender, attachmentResolver);
	}

}
