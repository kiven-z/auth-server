package com.auth.service.system.admin.support.role;

import com.auth.service.system.admin.mapper.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 角色关联写入前的存在性校验
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class RoleReferenceChecker {

	private final SysRoleMapper sysRoleMapper;

	/**
	 * 按角色 ID 加载角色，不存在则抛业务异常
	 * @param roleId 角色 ID
	 * @return 角色实体
	 */
	public SysRoleEntity getExistingByRoleId(Long roleId) {
		if (roleId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, roleId);
		}

		SysRoleEntity role = sysRoleMapper.selectById(roleId);
		if (role == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, roleId);
		}
		return role;
	}

}
