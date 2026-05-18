package com.auth.service.auth.support.message;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录 Dypns 验证码短信模板变量（scene_code=login-sms-code，channel=SMS_DYPNS_VERIFY_CODE，变量
 * ${code}、${min}）
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
