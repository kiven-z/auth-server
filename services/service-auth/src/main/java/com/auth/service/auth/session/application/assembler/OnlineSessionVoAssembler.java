package com.auth.service.auth.session.application.assembler;

import com.auth.module.security.contract.api.UserSessionIndex;
import com.auth.service.auth.session.domain.model.UserOnlineBrief;
import com.auth.service.auth.session.domain.model.vo.OnlineSessionPageVO;
import lombok.experimental.UtilityClass;

/**
 * 在线会话 VO 组装；字段一一对应，复杂度否决 MapStruct。
 *
 * @author Bunny
 */
@UtilityClass
public class OnlineSessionVoAssembler {

	/**
	 * 会话索引与用户快照组装为分页行
	 * @param session 会话索引
	 * @param userBrief 用户展示快照，可为 null
	 * @return 分页行
	 */
	public static OnlineSessionPageVO toPageVo(UserSessionIndex session, UserOnlineBrief userBrief) {
		OnlineSessionPageVO vo = new OnlineSessionPageVO();
		vo.setUserId(session.getUserId());
		vo.setSessionId(session.getSessionId());
		vo.setIpAddress(session.getIpAddress());
		vo.setIpRegion(session.getIpRegion());
		vo.setDeviceType(session.getDeviceType());
		vo.setBrowserType(session.getBrowserType());
		vo.setOsType(session.getOsType());
		vo.setLoginAt(session.getLoginAt());
		vo.setRememberMe(session.getRememberMe());
		if (userBrief != null) {
			vo.setUsername(userBrief.username());
			vo.setNickname(userBrief.nickname());
		}
		return vo;
	}

}
