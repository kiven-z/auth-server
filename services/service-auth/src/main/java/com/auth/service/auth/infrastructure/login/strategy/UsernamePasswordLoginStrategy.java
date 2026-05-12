package com.auth.service.auth.infrastructure.login.strategy;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.auth.exception.AuthBadCredentialsException;
import com.auth.service.auth.exception.AuthBusinessException;
import com.auth.service.auth.exception.AuthResultCode;
import com.auth.service.auth.infrastructure.login.LoginFailureRateLimiter;
import com.auth.service.auth.infrastructure.profile.DefaultAuthProfileServiceImpl;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.model.request.login.UsernamePasswordLoginRequest;
import com.auth.service.auth.service.AuthLoginAuditService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户名密码认证提供者
 *
 * <p>
 * 通过用户名查询用户，使用 {@link PasswordEncoder} 校验明文密码。 密码错误时抛出
 * {@link AuthBadCredentialsException}，由模板方法统一转换为带剩余尝试次数的业务异常。
 * </p>
 *
 * @author Bunny
 */
@Component
public class UsernamePasswordLoginStrategy extends AbstractAuthenticationStrategy<UsernamePasswordLoginRequest> {

	private final UserMapper userMapper;

	/**
	 * Spring Security 密码加密器，用于校验明文密码与数据库中密文的匹配关系
	 */
	private final PasswordEncoder passwordEncoder;

	public UsernamePasswordLoginStrategy(UserMapper userMapper, LoginFailureRateLimiter loginFailureRateLimiter,
			DefaultAuthProfileServiceImpl defaultAuthProfileServiceImpl, PasswordEncoder passwordEncoder,
			AuthLoginAuditService loginLogService) {
		super(loginFailureRateLimiter, defaultAuthProfileServiceImpl, loginLogService);
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	protected AuthLoginLogType loginLogType() {
		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	/**
	 * 解析用户ID
	 * @param auth 用户名密码认证请求
	 * @return 用户ID
	 */
	@Override
	protected UserEntity resolveUser(UsernamePasswordLoginRequest auth) {
		String username = auth.getUsername();
		UserEntity user = userMapper.selectUserByUsername(username);
		if (user == null) {
			// 对外隐藏账号是否存在，统一按"用户名密码错误"处理
			throw new AuthBusinessException(AuthResultCode.USERNAME_OR_PASSWORD_ERROR);
		}
		return user;
	}

	/**
	 * 认证
	 * @param auth 用户名密码认证请求
	 * @param user 已解析的用户
	 */
	@Override
	protected AuthLoginLogType doAuthenticate(UsernamePasswordLoginRequest auth, UserEntity user) {
		String encodedPassword = user.getPassword();

		// 校验明文密码是否匹配密文
		if (!passwordEncoder.matches(auth.getPassword(), encodedPassword)) {
			throw new AuthBadCredentialsException("Password mismatch");
		}

		return AuthLoginLogType.LOGIN_PASSWORD;
	}

	/**
	 * 当前策略支持的认证请求类型（作为注册表 key）
	 * @return UsernamePasswordAuth
	 */
	@Override
	public Class<UsernamePasswordLoginRequest> supports() {
		return UsernamePasswordLoginRequest.class;
	}

}
