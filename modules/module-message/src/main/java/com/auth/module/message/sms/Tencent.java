package com.auth.module.message.sms;

import lombok.Getter;
import lombok.Setter;

/**
 * 腾讯云短信连接参数（预留，接入腾讯云时在此补充字段）
 *
 * @author Bunny
 */
@Getter
@Setter
public class Tencent {

    /**
     * SecretId
     */
    private String secretId;

    /**
     * SecretKey
     */
    private String secretKey;

    /**
     * 地域，如 ap-guangzhou
     */
    private String region = "ap-guangzhou";

}