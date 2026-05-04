package com.auth.service.system.admin.user.service;

import com.auth.service.system.admin.user.model.vo.SysUserProfileResponse;

/**
 * 用户档案查询
 *
 * @author Bunny
 */
public interface SysUserProfileService {

	/**
	 * 查询用户档案（基本信息、部门、岗位）
	 * @param userId 用户ID
	 * @return 聚合结果
	 */
	SysUserProfileResponse getProfile(Long userId);

}
