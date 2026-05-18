package com.auth.module.message.config;

import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.sms.aliyun.AliyunSmsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 消息模块自动配置入口（按渠道拆分，通过 {@link Import} 聚合）
 *
 * @author Bunny
 */
@AutoConfiguration
@EnableConfigurationProperties({ SmsProperties.class })
@Import({ AliyunSmsAutoConfiguration.class })
public class MessageAutoConfiguration {

}
