package com.auth.service.auth.authentication.application;

import cn.hutool.core.util.BooleanUtil;
import com.auth.service.auth.authentication.api.dto.response.UserLoginResponse;
import com.auth.service.auth.authentication.application.assembler.LoginResponseAssembler;
import com.auth.service.auth.authentication.application.input.credential.AbstractCredentialLogin;
import com.auth.service.auth.authentication.application.login.audit.LoginAuditService;
import com.auth.service.auth.authentication.application.gateway.SessionGateway;
import com.auth.service.auth.authentication.application.gateway.TokenGateway;
import com.auth.service.auth.authentication.application.port.LoginStrategy;
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

	private final Map<Class<? extends AbstractCredentialLogin>, LoginStrategy<?>> strategyRegistry;

	private final LoginSessionOrchestrator loginSessionOrchestrator;

	private final SessionGateway sessionGateway;

	private final TokenGateway tokenGateway;

	private final LoginAuditService loginAuditService;

	public LoginApplicationService(List<LoginStrategy<?>> strategies, LoginSessionOrchestrator loginSessionOrchestrator,
			SessionGateway sessionGateway, TokenGateway tokenGateway, LoginAuditService loginAuditService) {
		this.strategyRegistry = strategies.stream()
			.collect(Collectors.toUnmodifiableMap(LoginStrategy::supports, Function.identity()));
		this.loginSessionOrchestrator = loginSessionOrchestrator;
		this.sessionGateway = sessionGateway;
		this.tokenGateway = tokenGateway;
		this.loginAuditService = loginAuditService;
	}

	/**
	 * 执行登录并返回已填充访问/刷新令牌的响应。
	 * @param credential 登录命令
	 * @param request HTTP 请求
	 * @return 登录响应
	 */
	@SuppressWarnings("unchecked")
	public UserLoginResponse login(AbstractCredentialLogin credential, HttpServletRequest request) {
		// 查找登录策略并记录审计日志
		LoginStrategy<?> strategy = lookupStrategyWithAudit(credential, request);

		// 执行登录并返回登录结果
		LoginResult loginResult = ((LoginStrategy<AbstractCredentialLogin>) strategy).authenticate(credential, request);

		UserLoginResponse response = LoginResponseAssembler.toUserLoginResponse(loginResult);
		boolean rememberMe = BooleanUtil.isTrue(credential.getRememberMe());
		return loginSessionOrchestrator.issueSession(loginResult, response, rememberMe, request);
	}

	/**
	 * 查找登录策略并记录审计日志。
	 * @param credential 凭证登录入站模型
	 * @param request HTTP 请求
	 * @return 登录策略
	 */
	private LoginStrategy<?> lookupStrategyWithAudit(AbstractCredentialLogin credential, HttpServletRequest request) {
		try {
			// 获取登录策略
			LoginStrategy<?> raw = strategyRegistry.get(credential.getClass());
			if (raw == null) {
				raw = strategyRegistry.entrySet()
					.stream()
					.filter(entry -> entry.getKey().isInstance(credential))
					.map(Map.Entry::getValue)
					.findFirst()
					.orElseThrow(() -> new AuthBusinessException(AuthResultCode.AUTH_TYPE_UNSUPPORTED));
			}
			return raw;
		}
		catch (AuthBusinessException ex) {
			loginAuditService.auditUnsupportedLoginCommand(request, credential, ex.getMessageKey());
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
			sessionGateway.deleteSession(jti);
			sessionGateway.removeActiveSession(userId, jti);
			return new LogoutSessionHint(userId, jti);
		});
	}

}
