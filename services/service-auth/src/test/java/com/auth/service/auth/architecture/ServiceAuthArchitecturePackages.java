package com.auth.service.auth.architecture;

/**
 * service-auth 架构测试用包路径常量。
 *
 * @author Bunny
 */
final class ServiceAuthArchitecturePackages {

	/**
	 * 模块根包（ArchUnit {@code ..} 语法）
	 */
	static final String AUTH_ROOT = "..service.auth..";
	static final String AUTH_API = "..api..";
	static final String AUTH_API_CONTROLLER = "..api.controller..";
	static final String AUTH_APPLICATION = "..application..";
	static final String AUTH_APPLICATION_PORT = "..application.port..";
	static final String AUTH_APPLICATION_QUERY = "..application..query..";
	static final String AUTH_APPLICATION_LOGIN = "..application.login..";
	static final String AUTH_APPLICATION_SESSION = "..application.session..";
	static final String AUTH_APPLICATION_AUTHORIZATION = "..application.authorization..";
	static final String AUTH_DOMAIN = "..domain..";
	static final String AUTH_INFRASTRUCTURE = "..infrastructure..";
	static final String AUTH_CONVERT = "..convert..";
	static final String AUTH_JOB = "..job..";
	static final String AUTH_SHARED = "..shared..";

	/**
	 * SQL 只读投影（Command 侧 application / Port / convert 允许依赖的 infrastructure 子包）
	 */
	static final String AUTH_INFRASTRUCTURE_PROJECTION = "..infrastructure.persistence.projection..";

	/**
	 * 持久化层（QueryService 允许依赖：mapper、projection、SQL 过滤参数等）
	 */
	static final String AUTH_INFRASTRUCTURE_PERSISTENCE = "..infrastructure.persistence..";

	/**
	 * 本模块 HTTP 契约包（不含 {@code module-security-contract}）
	 */
	static final String AUTH_HTTP_API = "com.auth.service.auth.api..";
	static final String SYSTEM_IMPLEMENTATION = "com.auth.service.system..";

	private ServiceAuthArchitecturePackages() {
	}

}
