package com.auth.service.system.admin.service.me.impl;

import java.util.List;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.model.response.Result;
import com.auth.common.web.resttemplate.FeignUtil;
import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.feign.MeSessionInnerFeignClient;
import com.auth.service.system.admin.feign.dto.UserSessionRemoteDTO;
import com.auth.service.system.admin.model.vo.me.MeUserSessionVO;
import com.auth.service.system.admin.service.me.CurrentUserSessionService;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.common.exception.SystemBusinessException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.auth.service.system.common.exception.code.SystemCommonResultCode.OPERATION_FAILED;
import static com.auth.service.system.common.exception.code.SystemCommonResultCode.SERVICE_UNAVAILABLE;

/**
 * 个人中心会话服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CurrentUserSessionServiceImpl implements CurrentUserSessionService {

	private final MeSessionInnerFeignClient meSessionInnerFeignClient;

	private final UserReferenceChecker userReferenceChecker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MeUserSessionVO> listMySessions() {
		Long userId = SecurityUserUtils.getUserId();
		userReferenceChecker.getExistingActive(userId);

		Result<List<UserSessionRemoteDTO>> result = meSessionInnerFeignClient.listUserSessions(userId);
		if (!FeignUtil.isSuccess(result)) {
			throw new SystemBusinessException(SERVICE_UNAVAILABLE);
		}

		String currentSessionId = SecurityUserUtils.getSessionId();
		List<UserSessionRemoteDTO> sessions = result.getData();

		return CollUtil.emptyIfNull(sessions).stream().map(session -> {
			MeUserSessionVO vo = new MeUserSessionVO();
			vo.setSessionId(session.getSessionId());
			vo.setIpAddress(session.getIpAddress());
			vo.setIpRegion(session.getIpRegion());
			vo.setDeviceType(session.getDeviceType());
			vo.setBrowserType(session.getBrowserType());
			vo.setOsType(session.getOsType());
			vo.setRememberMe(session.getRememberMe());
			vo.setRefreshTokenExpiresAt(session.getRefreshTokenExpiresAt());
			vo.setLoginAt(session.getLoginAt());
			vo.setCurrent(Objects.equals(session.getSessionId(), currentSessionId));
			return vo;
		}).toList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void kickSession(String sessionId) {
		Long userId = SecurityUserUtils.getUserId();
		userReferenceChecker.getExistingActive(userId);

		Result<Void> result = meSessionInnerFeignClient.kickUserSession(userId, sessionId);
		if (!FeignUtil.isSuccess(result)) {
			throw new SystemBusinessException(OPERATION_FAILED);
		}
	}

}
