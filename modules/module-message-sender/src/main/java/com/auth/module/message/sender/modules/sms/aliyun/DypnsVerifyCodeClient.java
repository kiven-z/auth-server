package com.auth.module.message.sender.modules.sms.aliyun;

import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;

import java.util.concurrent.ExecutionException;

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
	 * @throws ExecutionException 执行异常
	 * @throws InterruptedException 中断异常
	 */
	SendSmsVerifyCodeResponse send(SendSmsVerifyCodeRequest request) throws ExecutionException, InterruptedException;

}
