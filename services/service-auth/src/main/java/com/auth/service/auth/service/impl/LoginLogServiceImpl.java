package com.auth.service.auth.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.ip.IpAddressService;
import com.auth.common.ip.IpInfo;
import com.auth.common.web.model.entity.UserAgent;
import com.auth.module.security.starter.model.LoginLogEntity;
import com.auth.service.auth.mapper.LoginLogMapper;
import com.auth.service.auth.model.enums.AuthLoginLogResult;
import com.auth.service.auth.model.enums.AuthLoginLogType;
import com.auth.service.auth.service.AuthLoginAuditService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录与安全事件审计持久化实现。
 *
 * @author Bunny
 */
@Slf4j
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLogEntity> implements AuthLoginAuditService {

	private final IpAddressService ipAddressService;

	public LoginLogServiceImpl(IpAddressService ipAddressService) {
		this.ipAddressService = ipAddressService;
	}

	/**
	 * 记录一条认证相关审计（登录、登出、刷新令牌等）。
	 * @param request HTTP 请求，用于 IP/UA；可为 null，此时地域与设备字段不落库
	 * @param eventType 事件类型
	 * @param result 结果码（与 {@code log_login.login_result} 一致）
	 * @param userId 用户 ID，未识别可为 null
	 * @param principal 用户名或邮箱等冗余检索字段
	 * @param failureReason 失败原因（messageKey 或简短说明）；成功时可为 null
	 * @param sessionId 会话 ID（如 jti）；无可为空
	 */
	@Override
	public void recordLoginLog(HttpServletRequest request, AuthLoginLogType eventType, AuthLoginLogResult result,
			Long userId, String principal, String failureReason, String sessionId) {
		LoginLogEntity entity = new LoginLogEntity();

		if (request != null) {
			IpInfo ipInfo = ipAddressService.resolveIpInfo(request);
			UserAgent userAgent = UserAgent.getUserAgent(request);
			entity.setLoginIp(ipInfo.getIpAddr());
			entity.setLoginRegion(ipInfo.getIpRegion());
			entity.setUserAgent(request.getHeader("User-Agent"));
			entity.setDeviceType(userAgent.getDeviceType());
			entity.setOsType(userAgent.getOs());
			entity.setBrowserType(userAgent.getBrowser());
		}

		try {
			entity.setUserId(userId);
			entity.setUsername(CharSequenceUtil.isNotBlank(principal) ? principal : null);
			entity.setLoginType(eventType.name());
			entity.setLoginResult(result.getCode());
			entity.setFailureReason(CharSequenceUtil.isNotBlank(failureReason) ? failureReason : null);
			entity.setLoginTime(LocalDateTime.now());
			entity.setSessionId(CharSequenceUtil.isNotBlank(sessionId) ? sessionId : null);

			// 设置更新用户和创建用户，因为部分登录不存在用户信息
			entity.setCreateUser(userId);
			entity.setUpdateUser(userId);
			entity.setCreateTime(LocalDateTime.now());
			entity.setUpdateTime(LocalDateTime.now());
			baseMapper.insert(entity);
		}
		catch (Exception ex) {
			log.error(
					"Failed to persist auth audit: eventType={}, result={}, userId={}, principal={}, sessionId={}, cause={}",
					eventType, result, userId, principal, sessionId, ex, ex);
		}
	}

}
