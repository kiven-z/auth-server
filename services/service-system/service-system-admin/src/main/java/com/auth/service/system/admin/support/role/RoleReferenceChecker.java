package com.auth.service.system.admin.support.role;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.service.system.admin.mapper.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
	 * 按角色编码加载角色，不存在或为空则抛业务异常
	 * @param roleCode 角色编码
	 * @return 角色实体
	 */
	public SysRoleEntity getExistingByRoleCode(String roleCode) {
		if (CharSequenceUtil.isBlank(roleCode)) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, roleCode);
		}

		SysRoleEntity role = sysRoleMapper
			.selectOne(Wrappers.<SysRoleEntity>lambdaQuery().eq(SysRoleEntity::getRoleCode, roleCode).last("LIMIT 1"));
		if (role == null) {
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST, roleCode);
		}
		return role;
	}

}
