package com.auth.module.message.sender.modules.email.autoconfigure;

import com.auth.module.message.sender.modules.email.transport.EmailAttachmentResolver;
import com.auth.module.message.sender.modules.email.transport.MimeMailTransport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;

/**
 * Email transport auto-configuration (JavaMail MIME send).
 * <p>
 * 仅在存在 {@link JavaMailSender} Bean 时生效（需引入 {@code spring-boot-starter-mail} 并配置
 * {@code spring.mail.host}）。避免仅因 {@code spring-context-support} 携带
 * {@link JavaMailSender} 接口类而误装配（例如 auth 服务通过 Feign 委托 system 发信）。
 * </p>
 *
 * @author Bunny
 */
@AutoConfiguration
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
@ConditionalOnBean(JavaMailSender.class)
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
     * @param mailSender         JavaMail sender
     * @param attachmentResolver attachment resolver
     * @return MIME mail transport
     */
    @ConditionalOnMissingBean
    @Bean
    MimeMailTransport mimeMailTransport(JavaMailSender mailSender, EmailAttachmentResolver attachmentResolver) {
        return new MimeMailTransport(mailSender, attachmentResolver);
    }

}
