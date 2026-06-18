package com.auth.service.auth.application.login.command;

import cn.hutool.core.util.BooleanUtil;
import com.auth.service.auth.application.login.LoginAuditService;
import com.auth.service.auth.application.login.input.BaseLoginCommand;
import com.auth.service.auth.application.port.login.LoginStrategy;
import com.auth.service.auth.application.port.login.TokenPort;
import com.auth.service.auth.application.port.session.SessionPort;
import com.auth.service.auth.domain.authentication.model.CompletedLoginSession;
import com.auth.service.auth.domain.authentication.model.LoginResult;
import com.auth.service.auth.domain.authentication.model.LogoutSessionHint;
import com.auth.service.auth.shared.exception.AuthBusinessException;
import com.auth.service.auth.shared.exception.AuthResultCode;
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

	private final Map<Class<? extends BaseLoginCommand>, LoginStrategy<?>> strategyRegistry;

	private final LoginSessionOrchestrator loginSessionOrchestrator;

	private final SessionPort sessionGateway;

	private final TokenPort tokenGateway;

	private final LoginAuditService loginAuditService;

	public LoginApplicationService(List<LoginStrategy<?>> strategies, LoginSessionOrchestrator loginSessionOrchestrator,
			SessionPort sessionGateway, TokenPort tokenGateway, LoginAuditService loginAuditService) {
		this.strategyRegistry = strategies.stream()
			.collect(Collectors.toUnmodifiableMap(LoginStrategy::supports, Function.identity()));
		this.loginSessionOrchestrator = loginSessionOrchestrator;
		this.sessionGateway = sessionGateway;
		this.tokenGateway = tokenGateway;
		this.loginAuditService = loginAuditService;
	}

	/**
	 * 执行登录并返回已填充访问/刷新令牌的会话读模型。
	 * @param command 登录命令
	 * @param request HTTP 请求
	 * @return 登录会话读模型
	 */
	@SuppressWarnings("unchecked")
	public CompletedLoginSession login(BaseLoginCommand command, HttpServletRequest request) {
		LoginStrategy<?> strategy = lookupStrategyWithAudit(command, request);
		LoginResult loginResult = ((LoginStrategy<BaseLoginCommand>) strategy).authenticate(command, request);
		boolean rememberMe = BooleanUtil.isTrue(command.getRememberMe());
		return loginSessionOrchestrator.issueSession(loginResult, rememberMe, request);
	}

	/**
	 * 查找登录策略并记录审计日志。
	 * @param command 登录命令
	 * @param request HTTP 请求
	 * @return 登录策略
	 */
	private LoginStrategy<?> lookupStrategyWithAudit(BaseLoginCommand command, HttpServletRequest request) {
		try {
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
		return tokenGateway.parseAccessTokenSafe(accessToken).map(userToken -> {
			String jti = userToken.getJti();
			Long userId = userToken.getUserId();

			sessionGateway.terminateSession(userId, jti);
			return new LogoutSessionHint(userId, jti);
		});
	}

}
