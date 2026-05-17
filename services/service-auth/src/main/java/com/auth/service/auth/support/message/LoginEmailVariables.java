package com.auth.service.auth.support.message;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录邮件验证码模板变量组装（template_code = login-email-code）
 *
 * @author Bunny
 */
@UtilityClass
public class LoginEmailVariables {

	/**
	 * 登录时间展示格式
	 */
	private static final DateTimeFormatter LOGIN_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/**
	 * 构建 login-email-code 模板变量
	 * @param username 用户名
	 * @param verificationCode 明文验证码
	 * @param expireMinutes 过期分钟数
	 * @param loginTime 登录时间
	 * @param ipAddr 登录 IP
	 * @param ipRegion 登录归属地
	 * @return 变量 Map
	 */
	public static Map<String, Object> build(String username, String verificationCode, long expireMinutes,
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

}
