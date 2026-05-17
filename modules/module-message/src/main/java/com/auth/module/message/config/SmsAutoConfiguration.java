package com.auth.module.message.config;

import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.auth.module.message.config.properties.SmsProperties;
import com.auth.module.message.sms.AliyunSmsClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 短信发送配置
 *
 * @author Bunny
 */
@EnableConfigurationProperties({SmsProperties.class})
@Configuration
public class SmsAutoConfiguration {
    /**
     * 阿里云短信客户端（{@code auth.message.sms.provider=aliyun}）
     */
    @Slf4j
    @ConditionalOnClass(AsyncClient.class)
    @ConditionalOnProperty(prefix = "auth.message.sms", name = "enabled", havingValue = "true")
    @ConditionalOnProperty(prefix = "auth.message.sms", name = "provider", havingValue = "aliyun")
    @Configuration(proxyBeanMethods = false)
    static class AliyunSmsClientConfiguration {

        /**
         * 阿里云 Dypnsapi 异步客户端
         *
         * @param smsProperties 短信配置
         * @return 可关闭的 AsyncClient
         */
        @ConditionalOnMissingBean(name = "aliyunSmsAsyncClient")
        @Bean(destroyMethod = "close")
        AsyncClient aliyunSmsAsyncClient(SmsProperties smsProperties) {
            AsyncClient client = AliyunSmsClientFactory.create(smsProperties);
            log.info("Aliyun SMS AsyncClient initialized, region={}, endpoint={}", smsProperties.getAliyun().getRegion(),
                    smsProperties.getAliyun().getEndpoint());
            return client;
        }

    }

    // 接入腾讯云时在此增加：
    // @ConditionalOnProperty(..., name = "provider", havingValue = "tencent")
    // static class TencentSmsClientConfiguration { ... }
}
