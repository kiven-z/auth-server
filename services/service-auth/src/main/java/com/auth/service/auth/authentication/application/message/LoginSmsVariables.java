package com.auth.service.auth.authentication.application.message;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录验证码短信模板
 *
 * @author Bunny
 */
@UtilityClass
public class LoginSmsVariables {

	/**
	 * 构建 login-sms-code 模板变量
	 * @param code 明文验证码
	 * @param expireMinutes 有效分钟数
	 * @return 变量 Map
	 */
	public static Map<String, Object> build(String code, long expireMinutes) {
		Map<String, Object> variables = new HashMap<>(2);
		variables.put("code", code);
		variables.put("min", expireMinutes);
		return variables;
	}

}
