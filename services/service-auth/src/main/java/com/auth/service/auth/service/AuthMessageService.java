package com.auth.service.auth.service;

/**
 * 认证消息服务
 *
 * @author Bunny
 */
public interface AuthMessageService {

	/**
	 * 发送邮箱验证码
	 * @param email 邮箱
	 */
	void sendEmailCode(String email);

}
