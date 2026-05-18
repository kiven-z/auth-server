package com.auth.service.auth.support.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 认证场景下 Dypns 验证码短信的 scene_code（与 sys_message_template.channel=SMS_DYPNS_VERIFY_CODE
 * 约定一致）
 *
 * @author Bunny
 */
@Getter
@RequiredArgsConstructor
public enum AuthDypnsVerifyCodeSceneCode {

	/**
	 * 登录短信验证码（阿里云 Dypns 控制台模板，如 100001）
	 */
	LOGIN_VERIFY_CODE("login-sms-code");

	/**
	 * 场景编码，对应 sys_message_template.scene_code 与发送命令 templateCode
	 */
	private final String sceneCode;

}
