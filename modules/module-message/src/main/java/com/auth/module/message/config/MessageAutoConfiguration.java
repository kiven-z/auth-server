package com.auth.module.message.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 消息模块自动配置入口（按渠道拆分，通过 {@link Import} 聚合）
 *
 * @author Bunny
 */
@AutoConfiguration
@Import({ SmsAutoConfiguration.class })
public class MessageAutoConfiguration {

}
