package com.auth.service.auth.application.port.authorization;

import java.util.List;

/**
 * 用户角色与权限 Grant 查询端口
 *
 * @author Bunny
 */
public interface AuthProfileGrantQueryPort {

	/**
	 * 查询用户角色编码列表
	 * @param userId 用户 ID
	 * @return 角色编码列表
	 */
	List<String> selectRolesByUserId(long userId);

	/**
	 * 查询用户权限编码列表
	 * @param userId 用户 ID
	 * @return 权限编码列表
	 */
	List<String> selectPermissionsByUserId(long userId);

}
