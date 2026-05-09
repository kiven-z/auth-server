package com.auth.common.core.desensitize;

/**
 * 脱敏类型，与 Hutool {@link cn.hutool.core.util.DesensitizedUtil} 能力对齐，可按项目需要扩展。
 *
 * @author Bunny
 */
public enum DesensitizedType {

    /**
     * 中文姓名
     */
    CHINESE_NAME,

    /**
     * 身份证号
     */
    ID_CARD,

    /**
     * 手机号
     */
    MOBILE_PHONE,

    /**
     * 电子邮箱
     */
    EMAIL,

    /**
     * 银行卡号
     */
    BANK_CARD,

    /**
     * IP 地址（自动识别 IPv4 / IPv6，均使用 Hutool 脱敏规则）
     */
    IP_ADDRESS,

    /**
     * 自定义保留前后缀规则，配合注解上的 prefixLen、suffixLen、symbol
     */
    CUSTOM
}
