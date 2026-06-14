package com.auth.gateway.exception;

import lombok.Getter;

import java.util.Map;

/**
 * 网关公共业务代码，模块前缀为{@code 00}
 *
 * <p>
 * 故意避免与安全公共段重叠： 安全使用{@code 00401-00406}和{@code 00101}
 * 网关使用{@code 002xx}/{@code 003xx}/{@code 00102+}
 * </p>
 *
 * @author Bunny
 */
@Getter
public enum GatewayResultCodeEnum {

	/**
	 * 路由未找到
	 */
	ROUTE_NOT_FOUND(404, 201, "GATEWAY_ROUTE_NOT_FOUND", "gateway.route.not.found", "No route matched this request."),

	/**
	 * 上游服务超时
	 */
	UPSTREAM_TIMEOUT(504, 102, "GATEWAY_UPSTREAM_TIMEOUT", "gateway.upstream.timeout", "Upstream service timed out."),

	/**
	 * 上游服务连接失败
	 */
	UPSTREAM_CONNECT_FAILED(502, 103, "GATEWAY_UPSTREAM_CONNECT_FAILED", "gateway.upstream.connect.failed",
			"Failed to connect to upstream service."),

	/**
	 * 网关内部错误
	 */
	INTERNAL_ERROR(500, 199, "GATEWAY_INTERNAL_ERROR", "gateway.internal.error", "Gateway internal error: {0}"),

	/**
	 * 未携带访问令牌（网关侧校验）
	 */
	UNAUTHORIZED_TOKEN_MISSING(401, 202, "GATEWAY_TOKEN_MISSING", "gateway.token.missing", "Token is missing."),

	/**
	 * 访问令牌无效（网关侧校验）
	 */
	UNAUTHORIZED_TOKEN_INVALID(401, 203, "GATEWAY_TOKEN_INVALID", "gateway.token.invalid", "Token is invalid."),

	/**
	 * 访问令牌过期（网关侧校验）
	 */
	UNAUTHORIZED_TOKEN_EXPIRED(401, 204, "GATEWAY_TOKEN_EXPIRED", "gateway.token.expired", "Token has expired."),
	/**
	 * 访问令牌类型不匹配（网关侧校验）
	 */
	UNAUTHORIZED_TOKEN_KIND_MISMATCH(401, 205, "GATEWAY_TOKEN_KIND_MISMATCH", "gateway.token.kind.mismatch",
			"Token kind is not supported."),
	/**
	 * 会话缺失（网关侧校验）
	 */
	UNAUTHORIZED_SESSION_MISSING(401, 206, "GATEWAY_SESSION_MISSING", "gateway.session.missing", "Session is missing."),
	/**
	 * 会话禁用（网关侧校验）
	 */
	UNAUTHORIZED_SESSION_DISABLED(401, 207, "GATEWAY_SESSION_DISABLED", "gateway.session.disabled",
			"Session is disabled."),
	/**
	 * 会话数据不一致（网关侧校验）
	 */
	UNAUTHORIZED_SESSION_INCONSISTENT(401, 208, "GATEWAY_SESSION_INCONSISTENT", "gateway.session.inconsistent",
			"Session data inconsistent, please re-authenticate."),
	/**
	 * 并发会话超限（网关侧校验）
	 */
	UNAUTHORIZED_SESSION_LIMIT_EXCEEDED(401, 209, "GATEWAY_SESSION_LIMIT_EXCEEDED", "gateway.session.limit.exceeded",
			"Concurrent session limit exceeded."),
	/**
	 * 权限版本不匹配（网关侧透传）
	 */
	UNAUTHORIZED_PERMISSION_VERSION_MISMATCH(409, 210, "GATEWAY_PERMISSION_VERSION_MISMATCH",
			"gateway.permission.version.mismatch", "Permission version mismatch, please refresh.");

	/**
	 * HTTP状态码
	 */
	private final int httpStatus;

	/**
	 * 业务代码
	 */
	private final int code;

	/**
	 * 错误信息
	 */
	private final String error;

	/**
	 * 国际化键
	 */
	private final String i18nKey;

	/**
	 * 默认消息（英文）
	 */
	private final String defaultMessageEn;

	GatewayResultCodeEnum(int httpStatus, int code, String error, String i18nKey, String defaultMessageEn) {
		this.httpStatus = httpStatus;
		this.code = code;
		this.error = error;
		this.i18nKey = i18nKey;
		this.defaultMessageEn = defaultMessageEn;
	}

	/**
	 * 完整的5位业务代码：{@code 00}模块前缀 + {@code bizCode}，其中{@code bizCode}是{@code CAT*100+SEQ}
	 * <p>
	 * 示例：{@code bizCode=201} -> {@code 00201}
	 * </p>
	 * @return 完整的5位业务代码
	 */
	public int fullBizCode() {
		return 10_000 + code;
	}

	/**
	 * 扩展信息
	 * @param args 参数
	 * @return 扩展信息
	 */
	public Map<String, Object> ext(Object... args) {
		return Map.of("i18nKey", i18nKey, "i18nArgs", args != null ? args : new Object[0]);
	}

}
