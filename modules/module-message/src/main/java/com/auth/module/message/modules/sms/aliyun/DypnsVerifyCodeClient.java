package com.auth.module.message.modules.sms.aliyun;

import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;

/**
 * 阿里云 Dypns 验证码发送客户端抽象（便于单测 Mock）
 *
 * @author Bunny
 */
public interface DypnsVerifyCodeClient extends AutoCloseable {

	/**
	 * 调用 SendSmsVerifyCode
	 * @param request 请求参数
	 * @return 响应
	 * @throws Exception 调用异常
	 */
	SendSmsVerifyCodeResponse send(SendSmsVerifyCodeRequest request) throws Exception;

}
