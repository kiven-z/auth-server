package com.auth.service.auth.support.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 认证服务使用的短信模板编码（与 sys_message_template.scene_code、channel=SMS 约定一致）
 *
 * @author Bunny
 */
@Getter
@RequiredArgsConstructor
public enum AuthSmsTemplateCode {

	/**
	 * 登录短信验证码（阿里云控制台模板 100001）
	 */
	LOGIN_SMS_CODE("login-sms-code");

	private final String templateCode;

}
