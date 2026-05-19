package com.auth.service.system.message.infrastructure.config;

import com.auth.service.system.message.infrastructure.modules.dingtalk.autoconfigure.DingTalkAutoConfiguration;
import com.auth.service.system.message.infrastructure.modules.email.autoconfigure.EmailAutoConfiguration;
import com.auth.service.system.message.infrastructure.modules.email.transport.MimeMailTransport;
import com.auth.service.system.message.infrastructure.modules.sms.dypns.aliyun.AliyunDypnsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 消息模块自动配置入口（渠道基础设施）
 *
 * @author Bunny
 */
@AutoConfiguration
@EnableConfigurationProperties({})
@Import({ AliyunDypnsAutoConfiguration.class, DingTalkAutoConfiguration.class, EmailAutoConfiguration.class,
		MimeMailTransport.class })
public class MessageAutoConfiguration {

}
