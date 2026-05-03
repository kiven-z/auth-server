package com.auth.service.auth.service;

import java.util.List;

import com.auth.service.auth.model.vo.inner.EffectiveCodesVO;

/**
 * 内部认证服务
 *
 * @author Bunny
 */
public interface AuthInnerService {

	/**
	 * 刷新用户缓存
	 * @param userIds 用户ID列表
	 */
	void refreshUserCache(List<Long> userIds);

	/**
	 * 踢出所有会话
	 * @param userIds 用户ID列表
	 */
	void kickAllSessions(List<Long> userIds);

	/**
	 * 解析用户生效角色码与权限码（与登录画像构建逻辑一致，含管理员展开）。
	 * @param userId 用户 ID
	 * @return 角色码与权限码
	 */
	EffectiveCodesVO resolveEffectiveCodes(Long userId);

}
