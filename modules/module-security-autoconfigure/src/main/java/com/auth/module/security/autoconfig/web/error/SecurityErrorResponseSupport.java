package com.auth.module.security.autoconfig.web.error;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.utils.ResponseUtil;
import com.auth.module.security.contract.exception.SecurityResultCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

import java.util.Map;

/**
 * 安全错误响应支持
 *
 * @author Bunny
 */
@UtilityClass
public final class SecurityErrorResponseSupport {

	/**
	 * 安全错误属性
	 */
	public static final String ATTR_SECURITY_ERROR = "security.error";

	/**
	 * 解析安全结果代码枚举
	 * @param request 请求
	 * @param fallbackError 回退错误
	 * @return 安全结果代码枚举
	 */
	private static SecurityResultCodeEnum resolveRc(HttpServletRequest request, String fallbackError) {
		Object raw = request != null ? request.getAttribute(ATTR_SECURITY_ERROR) : null;
		String error = raw instanceof String s ? s : null;
		if (error == null || error.isBlank()) {
			error = fallbackError;
		}
		return SecurityResultCodeEnum.fromError(error);
	}

	/**
	 * 写入安全错误响应
	 * @param response 响应
	 * @param rc 安全结果代码枚举
	 */
	private static void write(HttpServletResponse response, SecurityResultCodeEnum rc) {
		if (response == null || rc == null) {
			return;
		}

		response.setStatus(rc.getHttpStatus());
		ResponseUtil.out(response, build(rc));
	}

	/**
	 * 构建安全错误响应体（供 MVC Advice / FilterChain 复用）
	 * @param rc 安全结果代码枚举
	 * @return Result 响应体
	 */
	public static Result<Object> build(SecurityResultCodeEnum rc) {
		if (rc == null) {
			return null;
		}

		String message = rc.getDefaultMessage();
		Result<Object> body = Result.error(rc.getBizCode(), rc.getError(), message);
		body.setExt(rc.ext());

		// 兜底：如果 ext 被覆盖成 null，补齐 i18nKey/i18nArgs
		if (body.getExt() == null) {
			body.setExt(Map.of("i18nKey", rc.getI18nKey(), "i18nArgs", new Object[0]));
		}

		return body;
	}

	/**
	 * 写入安全错误响应
	 * @param request 请求
	 * @param response 响应
	 * @param fallbackError 回退错误
	 */
	public static void write(HttpServletRequest request, HttpServletResponse response, String fallbackError) {
		SecurityResultCodeEnum rc = resolveRc(request, fallbackError);
		write(response, rc);
	}

}