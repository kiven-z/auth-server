package com.auth.service.auth.authentication.domain.port;

import com.auth.service.auth.authentication.domain.enums.CredentialDimension;

/**
 * 登录验证码（邮箱/短信）读写端口。
 *
 * @author Bunny
 */
public interface LoginVerificationCodePort {

	/**
	 * 读取已存储的验证码摘要（与登录侧 MD5 摘要比对）。
	 * @param dimension 邮箱或手机号维度
	 * @param target 邮箱地址或手机号
	 * @return 存储的摘要，不存在时为 null
	 */
	String getStoredDigest(CredentialDimension dimension, String target);

	/**
	 * 校验成功后删除验证码，防止重放。
	 * @param dimension 邮箱或手机号维度
	 * @param target 邮箱地址或手机号
	 */
	void deleteCode(CredentialDimension dimension, String target);

}
