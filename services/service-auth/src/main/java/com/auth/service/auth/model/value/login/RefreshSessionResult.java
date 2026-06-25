package com.auth.service.auth.model.value.login;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 刷新令牌业务结果：新令牌对 + 会话级「记住我」标记（用于重写 Refresh Cookie）。
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class RefreshSessionResult {

	/**
	 * 访问/刷新令牌对
	 */
	TokenPair tokenPair;

	/**
	 * 登录时是否勾选记住我
	 */
	boolean rememberMe;

}
