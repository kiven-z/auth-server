package com.auth.module.message.sms4j.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.module.message.config.properties.SmsBlendProperties;
import org.dromara.sms4j.aliyun.config.AlibabaConfig;
import org.dromara.sms4j.provider.config.BaseConfig;
import org.dromara.sms4j.tencent.config.TencentConfig;

/**
 * 将 {@code auth.message.sms.blends} 映射为 sms4j {@link BaseConfig}
 * FIXME 有点问题
 *
 * @author Bunny
 */
public final class Sms4jBlendConfigMapper {

    private Sms4jBlendConfigMapper() {
    }

    /**
     * 构建 sms4j 厂商配置
     *
     * @param configId blend 标识
     * @param blend    业务配置
     * @return sms4j 配置，不支持的厂商返回 {@code null}
     */
    public static BaseConfig toBaseConfig(String configId, SmsBlendProperties blend) {
        if (blend == null || !blend.isEnabled() || CharSequenceUtil.isBlank(blend.getSupplier())) {
            return null;
        }
        String supplier = blend.getSupplier().trim().toLowerCase();
        return switch (supplier) {
            case "alibaba", "aliyun" -> toAlibabaConfig(configId, blend);
            case "tencent" -> toTencentConfig(configId, blend);
            default -> throw new IllegalStateException("Unsupported sms4j supplier: " + blend.getSupplier()
                    + " for blend " + configId + ". Supported: alibaba, tencent.");
        };
    }

    private static AlibabaConfig toAlibabaConfig(String configId, SmsBlendProperties blend) {
        AlibabaConfig config = new AlibabaConfig();
        config.setConfigId(configId);
        config.setAccessKeyId(blend.getAccessKeyId());
        config.setAccessKeySecret(blend.getAccessKeySecret());
        config.setSignature(blend.getSignature());
        return config;
    }

    private static TencentConfig toTencentConfig(String configId, SmsBlendProperties blend) {
        TencentConfig config = new TencentConfig();
        config.setConfigId(configId);
        config.setAccessKeyId(blend.getAccessKeyId());
        config.setAccessKeySecret(blend.getAccessKeySecret());
        config.setSignature(blend.getSignature());
        config.setSdkAppId(blend.getSdkAppId());
        return config;
    }

}
