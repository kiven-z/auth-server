package com.auth.module.message.sms4j.support;

import com.auth.module.message.config.properties.SmsBlendProperties;
import com.auth.module.message.config.properties.SmsProperties;
import org.dromara.sms4j.core.datainterface.SmsReadConfig;
import org.dromara.sms4j.provider.config.BaseConfig;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 从 {@link SmsProperties#getBlends()} 向 sms4j 提供厂商配置
 * FIXME 待确认
 *
 * @author Bunny
 */
public class AuthSms4jReadConfig implements SmsReadConfig {

    private final SmsProperties smsProperties;

    public AuthSms4jReadConfig(SmsProperties smsProperties) {
        this.smsProperties = smsProperties;
    }

    @Override
    public BaseConfig getSupplierConfig(String configId) {
        SmsBlendProperties blend = smsProperties.getBlends().get(configId);
        return Sms4jBlendConfigMapper.toBaseConfig(configId, blend);
    }

    @Override
    public List<BaseConfig> getSupplierConfigList() {
        return null;
    }

    /**
     * 遍历已启用的 blend（启动注册与配置读取共用）
     *
     * @param action 第一个参数为 configId，第二个为 blend 配置
     */
    public void forEachEnabledBlend(BiConsumer<String, SmsBlendProperties> action) {
        smsProperties.getBlends().forEach((configId, blend) -> {
            if (blend == null || !blend.isEnabled()) {
                return;
            }
            action.accept(configId, blend);
        });
    }

}
