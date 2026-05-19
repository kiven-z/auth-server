package com.auth.service.system.message.config;

import com.auth.service.system.message.config.properties.DingTalkProperties;
import com.auth.service.system.message.config.properties.SmsProperties;
import com.auth.service.system.message.channel.dingtalk.autoconfigure.DingTalkAutoConfiguration;
import com.auth.service.system.message.channel.email.autoconfigure.EmailAutoConfiguration;
import com.auth.service.system.message.channel.sms.dypns.aliyun.AliyunDypnsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 消息渠道基础设施自动配置入口（仅 service-system 使用）
 * <p>
 * {@link EnableConfigurationProperties} 集中注册配置类，便于 IDE 识别 Bean； 具体 Client / Transport
 * 由各子配置按 {@code auth.message.*.enabled} 条件装配。
 * </p>
 *
 * @author Bunny
 */
@AutoConfiguration
@EnableConfigurationProperties({ SmsProperties.class, DingTalkProperties.class })
@Import({ AliyunDypnsAutoConfiguration.class, DingTalkAutoConfiguration.class, EmailAutoConfiguration.class })
public class MessageAutoConfiguration {

}
