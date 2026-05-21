package com.auth.service.auth.authentication.application;

import cn.hutool.core.util.BooleanUtil;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.assembler.LoginResponseAssembler;
import com.auth.service.auth.authentication.application.command.AbstractLoginCommand;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditService;
import com.auth.service.auth.authentication.application.port.LoginStrategy;
import com.auth.service.auth.authentication.application.port.TokenIssuerPort;
import com.auth.service.auth.authentication.application.port.UserSessionIndexPort;
import com.auth.service.auth.authentication.application.session.LoginSessionOrchestrator;
import com.auth.service.auth.authentication.domain.model.LoginResult;
import com.auth.service.auth.authentication.domain.model.LogoutSessionHint;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.enums.AuthResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 登录应用服务：策略路由、凭证校验，会话签发委托 {@link LoginSessionOrchestrator}。
 *
 * @author Bunny
 */
@Service
public class LoginApplicationService {

	private final Map<Class<? extends AbstractLoginCommand>, LoginStrategy<?>> strategyRegistry;

	private final LoginSessionOrchestrator loginSessionOrchestrator;

	private final UserSessionIndexPort userSessionIndexPort;

	private final TokenIssuerPort tokenIssuerPort;

	private final LoginAuditService loginAuditService;

	public LoginApplicationService(List<LoginStrategy<?>> strategies, LoginSessionOrchestrator loginSessionOrchestrator,
			UserSessionIndexPort userSessionIndexPort, TokenIssuerPort tokenIssuerPort,
			LoginAuditService loginAuditService) {
		this.strategyRegistry = strategies.stream()
			.collect(Collectors.toUnmodifiableMap(LoginStrategy::supports, Function.identity()));
		this.loginSessionOrchestrator = loginSessionOrchestrator;
		this.userSessionIndexPort = userSessionIndexPort;
		this.tokenIssuerPort = tokenIssuerPort;
		this.loginAuditService = loginAuditService;
	}

	/**
	 * 执行登录并返回已填充访问/刷新令牌的响应。
	 * @param command 登录命令
	 * @param request HTTP 请求
	 * @return 登录响应
	 */
	@SuppressWarnings("unchecked")
	public UserLoginResponse login(AbstractLoginCommand command, HttpServletRequest request) {
		// 查找登录策略并记录审计日志
		LoginStrategy<?> strategy = lookupStrategyWithAudit(command, request);

		// 执行登录并返回登录结果
		LoginResult loginResult = ((LoginStrategy<AbstractLoginCommand>) strategy).authenticate(command, request);

		UserLoginResponse response = LoginResponseAssembler.toUserLoginResponse(loginResult);
		boolean rememberMe = BooleanUtil.isTrue(command.getRememberMe());
		return loginSessionOrchestrator.issueSession(loginResult, response, rememberMe, request);
	}

	/**
	 * 查找登录策略并记录审计日志。
	 * @param command 认证请求
	 * @param request HTTP 请求
	 * @return 登录策略
	 */
	private LoginStrategy<?> lookupStrategyWithAudit(AbstractLoginCommand command, HttpServletRequest request) {
		try {
			// 获取登录策略
			LoginStrategy<?> raw = strategyRegistry.get(command.getClass());
			if (raw == null) {
				raw = strategyRegistry.entrySet()
					.stream()
					.filter(entry -> entry.getKey().isInstance(command))
					.map(Map.Entry::getValue)
					.findFirst()
					.orElseThrow(() -> new AuthBusinessException(AuthResultCode.AUTH_TYPE_UNSUPPORTED));
			}
			return raw;
		}
		catch (AuthBusinessException ex) {
			loginAuditService.auditUnsupportedLoginCommand(request, command, ex.getMessageKey());
			throw ex;
		}
	}

	/**
	 * 登出当前会话（永远不抛出异常）。幂等：会话不存在/索引不存在都视为成功。
	 * @param accessToken Access Token（不含 Bearer 前缀）
	 */
	public Optional<LogoutSessionHint> logout(String accessToken) {
		return tokenIssuerPort.parseAccessTokenSafe(accessToken).map(userToken -> {
			String jti = userToken.getJti();
			Long userId = userToken.getUserId();
			userSessionIndexPort.deleteSession(jti);
			userSessionIndexPort.removeActiveSession(userId, jti);
			return new LogoutSessionHint(userId, jti);
		});
	}

}
