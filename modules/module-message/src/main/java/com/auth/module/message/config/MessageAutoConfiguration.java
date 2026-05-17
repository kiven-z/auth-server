package com.auth.module.message.config;

import com.auth.module.message.config.properties.SmsProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 消息配置
 *
 * @author Bunny
 */
@AutoConfiguration
@EnableConfigurationProperties({ SmsProperties.class })
public class MessageAutoConfiguration {

}
