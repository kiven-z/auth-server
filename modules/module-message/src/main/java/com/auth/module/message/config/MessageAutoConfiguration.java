package com.auth.module.message.config;

import com.auth.module.message.config.properties.DingTalkProperties;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.modules.sms.aliyun.AliyunDypnsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * 消息模块自动配置入口（契约与配置属性）
 *
 * @author Bunny
 */
@AutoConfiguration
@EnableConfigurationProperties({ SmsProperties.class, DingTalkProperties.class })
@Import({ AliyunDypnsAutoConfiguration.class })
public class MessageAutoConfiguration {

}
