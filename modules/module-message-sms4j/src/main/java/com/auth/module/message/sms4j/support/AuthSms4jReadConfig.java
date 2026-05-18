package com.auth.module.message.sms4j.support;

import com.auth.module.message.config.properties.SmsNotificationBlendProperties;
import com.auth.module.message.config.properties.SmsProperties;
import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.provider.config.BaseConfig;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 从 {@link SmsProperties#getNotification()} 向 sms4j 提供厂商配置
 *
 * @author Bunny
 */
public class AuthSms4jReadConfig implements SmsReadConfig {

    private final SmsProperties smsProperties;

    public AuthSms4jReadConfig(SmsProperties smsProperties) {
        this.smsProperties = smsProperties;
    }

    /**
     * 获取供应商配置
     */
    @Override
    public BaseConfig getSupplierConfig(String configId) {
        SmsNotificationBlendProperties blend = smsProperties.getNotification().getBlends().get(configId);
        return Sms4jBlendConfigMapper.toBaseConfig(configId, blend);
    }


    /**
     * 批量注册未使用，由启动时按 configId 逐个 {@link org.dromara.sms4j.core.factory.SmsFactory#createSmsBlend} 注册
     */
    @Override
    public List<BaseConfig> getSupplierConfigList() {
        return null;
    }

    /**
     * 遍历已启用的 blend（启动注册与配置读取共用）
     *
     * @param action 第一个参数为 configId，第二个为 blend 配置
     */
    public void forEachEnabledBlend(BiConsumer<String, SmsNotificationBlendProperties> action) {
        smsProperties.getNotification().getBlends().forEach((configId, blend) -> {
            if (blend != null && blend.isEnabled()) {
                action.accept(configId, blend);
            }
        });
    }

}
