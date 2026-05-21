package com.auth.service.auth.authentication.application.message;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证消息模板变量组装。
 *
 * @author Bunny
 */
@UtilityClass
public class AuthMessageVariables {

	/**
	 * 登录时间展示格式
	 */
	private static final DateTimeFormatter LOGIN_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * 构建登录邮箱验证码模板变量（template_code = login-email-code）。
	 * @param username 用户名
	 * @param verificationCode 明文验证码
	 * @param expireMinutes 过期分钟数
	 * @param loginTime 登录时间
	 * @param ipAddr 登录 IP
	 * @param ipRegion 登录归属地
	 * @return 变量 Map
	 */
	public static Map<String, Object> buildLoginEmail(String username, String verificationCode, long expireMinutes,
			LocalDateTime loginTime, String ipAddr, String ipRegion) {
		Map<String, Object> variables = new HashMap<>(6);
		variables.put("username", username);
		variables.put("verificationCode", verificationCode);
		variables.put("expireTime", expireMinutes);
		variables.put("loginTime", loginTime.format(LOGIN_TIME_FORMAT));
		variables.put("ipAddr", ipAddr);
		variables.put("ipRegion", ipRegion);
		return variables;
	}

	/**
	 * 构建登录短信验证码模板变量（scene_code = login-sms-code）。
	 * @param code 明文验证码
	 * @param expireMinutes 有效分钟数
	 * @return 变量 Map
	 */
	public static Map<String, Object> buildLoginSms(String code, long expireMinutes) {
		Map<String, Object> variables = new HashMap<>(2);
		variables.put("code", code);
		variables.put("min", expireMinutes);
		return variables;
	}

}
