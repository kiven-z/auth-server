package com.auth.service.system.admin.service.me.impl;

import com.auth.module.security.autoconfigure.web.SecurityUserUtils;
import com.auth.service.system.admin.model.vo.me.MeUserSessionVO;
import com.auth.service.system.admin.service.me.CurrentUserSessionService;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.authorization.dispatch.query.UserSessionQueryOperations;
import com.auth.service.system.authorization.dispatch.query.UserSessionSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 个人中心会话服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CurrentUserSessionServiceImpl implements CurrentUserSessionService {

	private final UserSessionQueryOperations userSessionQueryOperations;

	private final UserReferenceChecker userReferenceChecker;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MeUserSessionVO> listMySessions() {
		Long userId = SecurityUserUtils.getUserId();
		userReferenceChecker.getExistingActive(userId);

		String currentSessionId = SecurityUserUtils.getSessionId();
		List<UserSessionSnapshot> sessions = userSessionQueryOperations.listActiveSessions(userId);

		return sessions.stream().map(session -> {
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

		userSessionQueryOperations.kickSession(userId, sessionId);
	}

}
