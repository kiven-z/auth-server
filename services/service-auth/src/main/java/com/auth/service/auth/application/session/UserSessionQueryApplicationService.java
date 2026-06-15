package com.auth.service.auth.application.session;

import com.auth.service.auth.api.dto.session.UserSessionVO;
import com.auth.service.auth.application.port.session.UserSessionQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理端用户活跃会话查询
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class UserSessionQueryApplicationService {

	private final UserSessionQueryPort userSessionQueryPort;

	/**
	 * 查询用户活跃会话列表（管理端 VO）
	 * @param userId 用户 ID
	 * @return 活跃会话 VO 列表
	 */
	public List<UserSessionVO> listActiveSessions(long userId) {
		return userSessionQueryPort.listActiveSessions(userId).stream().map(index -> {
			UserSessionVO vo = new UserSessionVO();
			vo.setUserId(index.getUserId());
			vo.setSessionId(index.getSessionId());
			vo.setIpAddress(index.getIpAddress());
			vo.setIpRegion(index.getIpRegion());
			vo.setDeviceType(index.getDeviceType());
			vo.setBrowserType(index.getBrowserType());
			vo.setOsType(index.getOsType());
			vo.setRememberMe(index.getRememberMe());
			vo.setRefreshTokenExpiresAt(index.getRefreshTokenExpiresAt());
			vo.setLoginAt(index.getLoginAt());
			return vo;
		}).toList();
	}

}
