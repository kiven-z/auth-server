package com.auth.module.message.sms;

import lombok.Getter;
import lombok.Setter;

/**
 * 阿里云 SMS / Dypnsapi 连接参数
 *
 * @author Bunny
 */
@Getter
@Setter
public class Aliyun {

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * 区域 ID
     */
    private String region = "cn-hangzhou";

    /**
     * API Endpoint
     */
    private String endpoint = "dypnsapi.aliyuncs.com";

}