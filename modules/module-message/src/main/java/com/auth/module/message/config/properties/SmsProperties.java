package com.auth.module.message.config.properties;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.sms.Aliyun;
import com.auth.module.message.sms.Provider;
import com.auth.module.message.sms.Tencent;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 短信渠道配置（{@code auth.message.sms.*}）
 * <p>
 * 切换厂商时仅需修改 {@link #provider} 及对应子节点（如 {@link #aliyun}、{@link #tencent}），无需改动其他渠道配置类。
 * </p>
 *
 * @author Bunny
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "auth.message.sms")
@Validated
public class SmsProperties {

    /**
     * 是否启用短信渠道
     */
    private boolean enabled = false;

    /**
     * 短信服务商（决定注册哪一套客户端 Bean）
     */
    private Provider provider = Provider.ALIYUN;

    /**
     * 阿里云配置（{@code provider=aliyun} 时生效）
     */
    private Aliyun aliyun = new Aliyun();

    /**
     * 腾讯云配置（{@code provider=tencent} 时生效，接入时补充字段与 Bean）
     */
    private Tencent tencent = new Tencent();

    /**
     * 启用时按 {@link #provider} 校验对应厂商必填项
     */
    @PostConstruct
    public void validate() {
        if (!enabled) {
            return;
        }
        switch (provider) {
            case ALIYUN -> validateAliyun();
            case TENCENT -> validateTencent();
            default -> throw new IllegalStateException("Unsupported auth.message.sms.provider: " + provider);
        }
    }

    private void validateAliyun() {
        if (CharSequenceUtil.isBlank(aliyun.getAccessKeyId())
                || CharSequenceUtil.isBlank(aliyun.getAccessKeySecret())) {
            throw new IllegalStateException(
                    "auth.message.sms.aliyun.access-key-id and access-key-secret are required when provider=aliyun.");
        }
    }

    private void validateTencent() {
        throw new IllegalStateException("auth.message.sms.provider=tencent is not implemented yet.");
    }

}
