package com.auth.service.system.admin.service.me;

import java.util.List;

import com.auth.service.system.admin.model.vo.me.MeUserSessionVO;

/**
 * 个人中心会话查询与单会话踢出
 *
 * @author Bunny
 */
public interface CurrentUserSessionService {

	/**
	 * 查询当前用户全部活跃会话，并标记当前设备
	 * @return 会话列表
	 */
	List<MeUserSessionVO> listMySessions();

	/**
	 * 踢出当前用户指定会话
	 * @param sessionId 会话 ID（jti）
	 */
	void kickSession(String sessionId);

}
