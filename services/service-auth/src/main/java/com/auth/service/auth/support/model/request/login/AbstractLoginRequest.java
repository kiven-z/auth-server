package com.auth.service.auth.support.model.request.login;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 登录认证请求基类每种登录方式（用户名密码、邮箱、手机号等）继承此类， 添加各自特有的认证字段，利用多态在 AbstractAuthenticationProvider
 * 中统一处理
 *
 * @author Bunny
 */
@Getter
@Setter
public abstract class AbstractLoginRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

}
